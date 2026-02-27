/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.platform.repository2.unified.jcr.jackrabbit.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jackrabbit.core.security.authentication.AbstractLoginModule;
import org.apache.jackrabbit.core.security.authentication.Authentication;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository2.unified.jcr.jackrabbit.security.messages.Messages;
import org.springframework.security.authentication.ProviderNotFoundException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import javax.jcr.Credentials;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import java.security.Principal;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A Jackrabbit {@code LoginModule} that delegates to a Spring Security {@link AuthenticationManager}. Also, adds
 * more checks to the pre-authentication scenario.
 * 
 * @author mlowery
 */
public class SpringSecurityLoginModule extends AbstractLoginModule {

  // ~ Static fields/initializers
  // ======================================================================================

  private static final Log logger = LogFactory.getLog( SpringSecurityLoginModule.class );

  /**
   * Comma separated list of known tokens. If a Credentials instance has a preauthentication token, it must match
   * one of the values in this list. Ideally, there is a token per application. In this way, other applications are
   * unaffected should a token have to be blacklisted.
   */
  private static final String KEY_PRE_AUTHENTICATION_TOKENS = "preAuthenticationTokens"; //$NON-NLS-1$

  private static final String PRE_AUTHENTICATION_TOKEN_SEPARATOR = ","; //$NON-NLS-1$

  /**
   * When there's no AuthenticationManager available in PentahoSystem, this one will be returned.
   * It's sole purpose is to throw an Exception whenever an Authentication attempt is made so a
   * NPE doesn't occur.
   */
  protected static final AuthenticationManager NULL_AUTHENTICATION_MANAGER = new AuthenticationManager() {
    @Override public org.springframework.security.core.Authentication authenticate(
        org.springframework.security.core.Authentication authentication ) throws AuthenticationException {
      throw new ProviderNotFoundException( "Authentication Manager not present in PentahoSystem." );
    }
  };

  // ~ Instance fields
  // =================================================================================================

  private AuthenticationManager authenticationManager;

  private static Set<String> preAuthenticationTokens = new HashSet<String>();

  // ~ Constructors
  // ====================================================================================================

  public SpringSecurityLoginModule() {
    super();
  }

  // ~ Methods
  // =========================================================================================================

  /**
   * {@inheritDoc}
   */
  @Override
  protected void doInit( final CallbackHandler callbackHandler, final Session session, final Map options )
    throws LoginException {

    if ( options.containsKey( KEY_PRE_AUTHENTICATION_TOKENS ) ) {

      String preAuthenticationTokensString = (String) options.get( KEY_PRE_AUTHENTICATION_TOKENS );
      String[] tokens = preAuthenticationTokensString.split( PRE_AUTHENTICATION_TOKEN_SEPARATOR );

      if ( tokens.length == 0 ) {
        throw new LoginException( Messages.getInstance().getString(
            "AbstractPentahoLoginModule.ERROR_0001_PRE_AUTH_TOKENS_MALFORMED", KEY_PRE_AUTHENTICATION_TOKENS ) ); //$NON-NLS-1$
      }

      for ( String token : tokens ) {
        preAuthenticationTokens.add( token.trim() );
      }

      logger.debug( "preAuthenticationTokens=" + preAuthenticationTokens ); //$NON-NLS-1$
    }

  }

  // Static caching as this class is instanced over and over
  protected static AuthenticationManager authManager = null;


  protected AuthenticationManager getAuthenticationManager(  ) {
    if ( authManager == null && PentahoSystem.getInitializedOK() ) {
      authManager = PentahoSystem.get( AuthenticationManager.class );
    }
    if ( authManager == null ) {
      return NULL_AUTHENTICATION_MANAGER;
    }
    return authManager;
  }

  /**
   * {@inheritDoc}
   * 
   * Creates a {@code UsernamePasswordAuthenticationToken} from the given {@code principal} and {@code credentials}
   * and passes to Spring Security {@code AuthenticationManager}.
   */
  @Override
  protected Authentication getAuthentication( final Principal principal, final Credentials credentials )
    throws RepositoryException {

    // only handles SimpleCredential instances; DefaultLoginModule behaves the same way (albeit indirectly)
    if ( !( credentials instanceof SimpleCredentials ) ) {
      logger.debug( "credentials not instance of SimpleCredentials; returning null" ); //$NON-NLS-1$
      return null;
    }

    SimpleCredentials simpleCredentials = (SimpleCredentials) credentials;

    UsernamePasswordAuthenticationToken token =
        new UsernamePasswordAuthenticationToken( simpleCredentials.getUserID(), String.valueOf( simpleCredentials
            .getPassword() ) );

    boolean authenticated = false;

    try {
      org.springframework.security.core.Authentication authentication =
        SecurityContextHolder.getContext().getAuthentication();
      if ( authentication != null && authentication.getName().equals( simpleCredentials.getUserID() ) ) {
        // see if there's already an active Authentication for this user.
        authenticated = true;
      } else {
        // delegate to Spring Security
        getAuthenticationManager().authenticate( token );
        authenticated = true;
      }
    } catch ( AuthenticationException e ) {
      logger.debug( "authentication exception", e ); //$NON-NLS-1$
    }

    final boolean authenticateResult = authenticated;

    return new Authentication() {
      public boolean canHandle( Credentials credentials ) {
        // this is decided earlier in getAuthentication
        return true;
      }

      public boolean authenticate( Credentials credentials ) throws RepositoryException {
        return authenticateResult;
      }
    };

  }

  /**
   * {@inheritDoc}
   * 
   * <p>
   * Implementation copied from {@link org.apache.jackrabbit.core.security.simple.SimpleLoginModule}. Delegates to
   * a {@code PrincipalProvider}.
   * </p>
   */
  @Override
  protected Principal getPrincipal( final Credentials credentials ) {
    String userId = getUserID( credentials );
    Principal principal = principalProvider.getPrincipal( userId );
    if ( principal == null || principal instanceof SpringSecurityRolePrincipal ) {
      // no matching user principal
      return null;
    } else {
      return principal;
    }
  }

  /**
   * {@inheritDoc}
   * 
   * <p>
   * Not implemented.
   * </p>
   */
  @Override
  protected boolean impersonate( final Principal principal, final Credentials credentials ) throws RepositoryException,
    LoginException {
    throw new UnsupportedOperationException();
  }

  @Override
  protected boolean isPreAuthenticated( final Credentials creds ) {
    if ( super.isPreAuthenticated( creds ) ) {
      SimpleCredentials simpleCreds = (SimpleCredentials) creds;
      String preAuth = (String) simpleCreds.getAttribute( getPreAuthAttributeName() );
      boolean preAuthenticated = preAuthenticationTokens.contains( preAuth );
      if ( preAuthenticated ) {
        if ( logger.isDebugEnabled() ) {
          logger.debug( simpleCreds.getUserID() + " is pre-authenticated" ); //$NON-NLS-1$
        }
      } else {
        if ( logger.isDebugEnabled() ) {
          logger.debug( "pre-authentication token rejected" ); //$NON-NLS-1$
        }
      }
      return preAuthenticated;
    }
    return false;
  }

}
