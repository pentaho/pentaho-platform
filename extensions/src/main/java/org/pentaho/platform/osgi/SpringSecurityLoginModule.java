/*!
 *
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.osgi;

import java.io.IOException;
import java.security.Principal;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;

import org.apache.karaf.jaas.boot.principal.RolePrincipal;
import org.apache.karaf.jaas.boot.principal.UserPrincipal;
import org.apache.karaf.jaas.modules.AbstractKarafLoginModule;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.security.policy.rolebased.actions.AdministerSecurityAction;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

/**
 * JAAS LoginModule which delegates to the Platform's Spring Security
 * {@link org.springframework.security.AuthenticationManager}.
 *
 * If the Authenticated user has AdministerSecurity permissions, they'll be given a synthetic role of "karaf_admin"
 * which provides access to Admin features of Karaf
 *
 * Created by nbaker on 8/26/14.
 */
public class SpringSecurityLoginModule extends AbstractKarafLoginModule {

  public static final String    KARAF_ADMIN           = "karaf_admin";
  private AuthenticationManager authenticationManager = null;
  private IAuthorizationPolicy  authorizationPolicy   = null;

  public SpringSecurityLoginModule() {

  }

  public AuthenticationManager getAuthenticationManager() {
    if ( authenticationManager == null ) {
      authenticationManager = PentahoSystem.get( AuthenticationManager.class );
    }
    return authenticationManager;
  }

  public IAuthorizationPolicy getAuthorizationPolicy() {
    if ( authorizationPolicy == null ) {
      authorizationPolicy = PentahoSystem.get( IAuthorizationPolicy.class );
    }
    return authorizationPolicy;
  }

  public void setAuthenticationManager( AuthenticationManager authenticationManager ) {
    this.authenticationManager = authenticationManager;
  }

  public void setAuthorizationPolicy( IAuthorizationPolicy authorizationPolicy ) {
    this.authorizationPolicy = authorizationPolicy;
  }

  public void initialize( Subject sub, CallbackHandler handler, Map sharedState, Map options ) {
    super.initialize( sub, handler, options );
  }

  public boolean login() throws LoginException {

    org.springframework.security.core.Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if ( authentication != null ) {
      // Obtain the username of the incoming auth request to match against existing authentication on the thread.

      Callback[] callbacks = new Callback[1];
      callbacks[0] = new NameCallback( "User: " );

      try {
        callbackHandler.handle( callbacks );
      } catch ( IOException e ) {
        throw new LoginException( e.getMessage() );
      } catch ( UnsupportedCallbackException e ) {
        throw new LoginException( "Unable to interactively Authenticate with user: " + e.getMessage() );
      }
      // user callback get value
      String name = ( (NameCallback) callbacks[0] ).getName();
      if ( name == null ) {
        throw new LoginException( "User name is null" );
      }

      // If the existing thread-bound authentication does not match, discard it.
      if ( !name.equals( authentication.getName() ) ) {
        // reauthenticate
        authentication = null;
      }

    }

    if ( authentication == null ) {

      Callback[] callbacks = new Callback[2];

      callbacks[0] = new NameCallback( "User: " );
      callbacks[1] = new PasswordCallback( "Password: ", false );
      try {
        callbackHandler.handle( callbacks );
      } catch ( IOException e ) {
        throw new LoginException( e.getMessage() );
      } catch ( UnsupportedCallbackException e ) {
        throw new LoginException( "Unable to interactively Authenticate with user: " + e.getMessage() );
      }

      String name = ( (NameCallback) callbacks[0] ).getName();
      char[] password1 = ( (PasswordCallback) callbacks[1] ).getPassword();

      if ( password1 == null || name == null ) {
        throw new LoginException( "User Name and Password cannot be null" );
      }
      String password = new String( password1 );

      UsernamePasswordAuthenticationToken token =
          new UsernamePasswordAuthenticationToken( name, String.valueOf( password ) );

      IPentahoSession session = new StandaloneSession( name );
      PentahoSessionHolder.setSession( session );
      try {
        // Throws an exception on failure.
        authentication = getAuthenticationManager().authenticate( token );
        if ( authentication != null && !authentication.isAuthenticated() ) {
          throw new IllegalStateException( "Got a bad authentication" );
        }
        if ( authentication == null ) {
          throw new IllegalStateException( "Not Authenticated" );
        }
      } catch ( Exception e ) {
        session.destroy();
        PentahoSessionHolder.removeSession();
        throw new LoginException( e.getMessage() );
      }
    }

    principals = new HashSet<Principal>();
    principals.add( new UserPrincipal( authentication.getName() ) );
    Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
    if ( authorities != null ) {
      for ( GrantedAuthority authority : authorities ) {
        principals.add( new RolePrincipal( authority.getAuthority() ) );
      }
    }

    // AuthorizationPolicy requires a PentahoSession. becomeUSer is the easiest way
    SecurityHelper.getInstance().becomeUser( authentication.getName() );

    // If they have AdministerSecurity, grant the Karaf admin role
    if ( getAuthorizationPolicy().isAllowed( AdministerSecurityAction.NAME ) ) {
      principals.add( new RolePrincipal( KARAF_ADMIN ) );
    }

    succeeded = true;

    return true;
  }

  public boolean abort() throws LoginException {
    clear();
    return true;
  }

  public boolean logout() throws LoginException {
    subject.getPrincipals().removeAll( principals );
    principals.clear();
    return true;
  }

}
