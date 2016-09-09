/*
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
 * Copyright 2016 Pentaho Corporation. All rights reserved.
 */

package org.pentaho.platform.osgi;

import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.security.policy.rolebased.actions.AdministerSecurityAction;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class SpringSecurityLoginModuleTest {

  private static class AuthenticationManagerMatcher extends ArgumentMatcher<Authentication> {

    private String user;

    public AuthenticationManagerMatcher( String user ) {

      this.user = user;
    }

    public boolean matches( Object auth ) {
      return auth instanceof Authentication && ( (Authentication) auth ).getName().equals( user );
    }
  }

  @Test
  public void testLogin() throws Exception {

    // instances and mocks
    Subject subject = new Subject();
    TestCallbackHandler testCallbackHandler = new TestCallbackHandler( "joe" );
    SpringSecurityLoginModule loginModule = new SpringSecurityLoginModule();
    AuthenticationManager authenticationManager = mock( AuthenticationManager.class );
    IUserRoleListService userRoleListService = mock( IUserRoleListService.class );
    IAuthorizationPolicy authorizationPolicy = mock( IAuthorizationPolicy.class );
    Authentication authentication = mock( Authentication.class );
    Collection authorities = Arrays.asList( new GrantedAuthority[] { new SimpleGrantedAuthority( "Authenticated" ),
      new SimpleGrantedAuthority( "Administrator" ) } );

    Authentication authentication2 = mock( Authentication.class );
    Collection authorities2 = Arrays.asList(
      new GrantedAuthority[] { new SimpleGrantedAuthority( "Authenticated" ), new SimpleGrantedAuthority( "ceo" ) } );

    //
    PentahoSystem.registerObject( userRoleListService, IUserRoleListService.class );

    when( authorizationPolicy.isAllowed( AdministerSecurityAction.NAME ) ).thenReturn( true ).thenReturn( true )
      .thenReturn( false );
    when( authentication.getAuthorities() ).thenReturn( authorities );
    when( authentication.getName() ).thenReturn( "joe" );
    when( authentication.isAuthenticated() ).thenReturn( true );
    when( authentication2.getAuthorities() ).thenReturn( authorities2 );
    when( authentication2.getName() ).thenReturn( "pat" );
    when( authentication2.isAuthenticated() ).thenReturn( true );
    when( authenticationManager.authenticate( argThat( new AuthenticationManagerMatcher( "joe" ) ) ) ).thenReturn(
      authentication );
    when( authenticationManager.authenticate( argThat( new AuthenticationManagerMatcher( "pat" ) ) ) ).thenReturn(
      authentication );
    when( authenticationManager.authenticate( argThat( new AuthenticationManagerMatcher( "suzy" ) ) ) )
      .thenThrow( new UsernameNotFoundException( "Error" ) );

    when( userRoleListService.getRolesForUser( null, "joe" ) ).thenReturn( Arrays
      .<String>asList( "Authenticated", "Administrator" ) );
    when( userRoleListService.getRolesForUser( null, "pat" ) ).thenReturn( Arrays
      .<String>asList( "Authenticated", "ceo" ) );

    loginModule.setAuthenticationManager( authenticationManager );
    loginModule.setAuthorizationPolicy( authorizationPolicy );

    // start tests
    loginModule.initialize( subject, testCallbackHandler, Collections.emptyMap(), Collections.emptyMap() );
    loginModule.login();
    loginModule.commit();

    // joe should get the extra karaf_admin role
    verify( authenticationManager ).authenticate( argThat( new AuthenticationManagerMatcher( "joe" ) ) );
    assertEquals( 4, subject.getPrincipals().size() );
    subject.getPrincipals().toArray()[ 3 ].equals( "karaf_admin" );

    loginModule.logout();
    assertEquals( 0, subject.getPrincipals().size() );


    loginModule.login();
    loginModule.commit();
    assertEquals( 4, subject.getPrincipals().size() );

    // Suzy is not found
    testCallbackHandler = new TestCallbackHandler( "suzy" );
    loginModule.initialize( subject, testCallbackHandler, Collections.emptyMap(), Collections.emptyMap() );
    try {
      loginModule.login();
      fail( "Should have thrown a UsernameNotFoundException exception" );
    } catch ( LoginException ex ) {
      /* No-op */
    }

    // pat is found, but not an admin
    testCallbackHandler = new TestCallbackHandler( "pat" );
    loginModule.initialize( subject, testCallbackHandler, Collections.emptyMap(), Collections.emptyMap() );

    loginModule.logout();
    loginModule.login();
    loginModule.commit();
    assertEquals( 3, subject.getPrincipals().size() );

    assertTrue( loginModule.abort() );

  }

  @Test
  public void testExceptions() throws Exception {
    // clear any authentication
    SecurityContextHolder.getContext().setAuthentication( null );

    Subject subject = new Subject();


    TestCallbackHandler testCallbackHandler = new TestCallbackHandler( "joe" );
    SpringSecurityLoginModule loginModule = new SpringSecurityLoginModule();
    AuthenticationManager authenticationManager = mock( AuthenticationManager.class );
    IUserRoleListService userRoleListService = mock( IUserRoleListService.class );
    IAuthorizationPolicy authorizationPolicy = mock( IAuthorizationPolicy.class );
    Authentication authentication = mock( Authentication.class );
    Collection authorities = Arrays.asList( new GrantedAuthority[] { new SimpleGrantedAuthority( "Authenticated" ),
      new SimpleGrantedAuthority( "Administrator" ) } );
    Authentication authentication2 = mock( Authentication.class );
    Collection authorities2 = Arrays.asList(
      new GrantedAuthority[] { new SimpleGrantedAuthority( "Authenticated" ), new SimpleGrantedAuthority( "ceo" ) } );

    PentahoSystem.registerObject( userRoleListService, IUserRoleListService.class );

    when( authorizationPolicy.isAllowed( AdministerSecurityAction.NAME ) ).thenReturn( true ).thenReturn( true )
      .thenReturn( false );
    when( authentication.getAuthorities() ).thenReturn( authorities );
    when( authentication.getName() ).thenReturn( "joe" );
    when( authentication.isAuthenticated() ).thenReturn( true );
    when( authentication2.getAuthorities() ).thenReturn( authorities2 );
    when( authentication2.getName() ).thenReturn( "pat" );
    when( authentication2.isAuthenticated() ).thenReturn( true );
    when( authenticationManager.authenticate( argThat( new AuthenticationManagerMatcher( "joe" ) ) ) ).thenReturn(
      authentication );
    when( authenticationManager.authenticate( argThat( new AuthenticationManagerMatcher( "pat" ) ) ) ).thenReturn(
      authentication );
    when( authenticationManager.authenticate( argThat( new AuthenticationManagerMatcher( "suzy" ) ) ) )
      .thenThrow( new UsernameNotFoundException( "Error" ) );
    when( userRoleListService.getRolesForUser( null, "joe" ) ).thenReturn( Arrays
      .<String>asList( "Authenticated", "Administrator" ) );
    when( userRoleListService.getRolesForUser( null, "pat" ) ).thenReturn( Arrays
      .<String>asList( "Authenticated", "ceo" ) );

    loginModule.setAuthenticationManager( authenticationManager );
    loginModule.setAuthorizationPolicy( authorizationPolicy );

    // test a successful run
    loginModule.initialize( subject, testCallbackHandler, Collections.emptyMap(), Collections.emptyMap() );
    loginModule.login();
    loginModule.commit();

    verify( authenticationManager ).authenticate( argThat( new AuthenticationManagerMatcher( "joe" ) ) );
    assertEquals( 4, subject.getPrincipals().size() );
    subject.getPrincipals().toArray()[ 3 ].equals( "karaf_admin" );

    // now test exceptions

    // Test with Authentication bound to thread
    testCallbackHandler = new TestCallbackHandler( "ioe" );
    loginModule.initialize( subject, testCallbackHandler, Collections.emptyMap(), Collections.emptyMap() );
    try {
      loginModule.login();
      fail( "Should have thrown IOException" );
    } catch ( LoginException ioe ) {
      /* No-op */
    }

    // UnsupportedCallbackException thrown by underlying system
    testCallbackHandler = new TestCallbackHandler( "unsupported" );
    loginModule.initialize( subject, testCallbackHandler, Collections.emptyMap(), Collections.emptyMap() );
    try {
      loginModule.login();
      fail( "Should have thrown UnsupportedCallbackException" );
    } catch ( LoginException ioe ) {
      /* No-op */
    }
    SecurityContextHolder.getContext().setAuthentication( null );

    // IOException thrown by underlying system
    testCallbackHandler = new TestCallbackHandler( "ioe" );
    loginModule.initialize( subject, testCallbackHandler, Collections.emptyMap(), Collections.emptyMap() );
    try {
      loginModule.login();
      fail( "Should have thrown IOException" );
    } catch ( LoginException ioe ) {
      /* No-op */
    }

    testCallbackHandler = new TestCallbackHandler( "unsupported" );
    loginModule.initialize( subject, testCallbackHandler, Collections.emptyMap(), Collections.emptyMap() );
    try {
      loginModule.login();
      fail( "Should have thrown UnsupportedCallbackException" );
    } catch ( LoginException ioe ) {
      /* No-op */
    }

  }

  private static class TestCallbackHandler implements CallbackHandler {
    private String name;

    private TestCallbackHandler( String name ) {

      this.name = name;
    }

    @Override public void handle( Callback[] callbacks ) throws IOException, UnsupportedCallbackException {

      // Eceptionals
      if ( name.equals( "ioe" ) ) {
        throw new IOException();
      } else if ( name.equals( "unsupported" ) ) {
        throw new UnsupportedCallbackException( callbacks[ 0 ] );
      }


      NameCallback nameCallback = (NameCallback) callbacks[ 0 ];
      PasswordCallback passwordCallback = ( callbacks.length > 1 ) ? (PasswordCallback) callbacks[ 1 ] : null;

      nameCallback.setName( name );
      if ( passwordCallback != null ) {
        passwordCallback.setPassword( "password".toCharArray() );
      }


    }
  }

}
