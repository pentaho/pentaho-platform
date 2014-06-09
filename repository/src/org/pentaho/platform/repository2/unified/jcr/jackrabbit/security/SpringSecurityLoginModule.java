/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.repository2.unified.jcr.jackrabbit.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jackrabbit.core.security.authentication.AbstractLoginModule;
import org.apache.jackrabbit.core.security.authentication.Authentication;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository2.unified.jcr.jackrabbit.security.messages.Messages;
import org.springframework.security.AuthenticationException;
import org.springframework.security.AuthenticationManager;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;

import javax.jcr.Credentials;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import java.security.Principal;
import java.security.acl.Group;
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

    authenticationManager = getAuthenticationManager( callbackHandler, session, options );
  }

  // Static caching as this class is instanced over and over
  protected static AuthenticationManager authManager = null;

  protected AuthenticationManager getAuthenticationManager( final CallbackHandler callbackHandler,
      final Session session, final Map options ) {
    if ( authManager == null && PentahoSystem.getInitializedOK() ) {
      authManager = PentahoSystem.get( AuthenticationManager.class );
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
      org.springframework.security.Authentication authentication =
        SecurityContextHolder.getContext().getAuthentication();
      if( authentication != null && authentication.getName().equals( simpleCredentials.getUserID() ) ) {
        // see if there's already an active Authentication for this user.
        authenticated = true;
      } else {
        // delegate to Spring Security
        authenticationManager.authenticate( token );
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
    if ( principal == null || principal instanceof Group ) {
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
