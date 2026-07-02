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

package org.pentaho.platform.engine.security.authorization.spring;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRole;
import org.pentaho.platform.engine.security.authorization.core.AuthorizationRole;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SpringAuthenticationAuthorizationUserTest {

  private Authentication mockAuthentication;
  private UserDetails mockUserDetails;

  private GrantedAuthority authority1;
  private GrantedAuthority authority2;
  private GrantedAuthority authority3;

  @Before
  public void setUp() {
    mockAuthentication = mock( Authentication.class );
    mockUserDetails = mock( UserDetails.class );

    // Create test authorities
    authority1 = new SimpleGrantedAuthority( "ROLE_USER" );
    authority2 = new SimpleGrantedAuthority( "ROLE_ADMIN" );
    authority3 = new SimpleGrantedAuthority( "ROLE_MANAGER" );
  }

  // region Constructor Tests
  @Test( expected = IllegalArgumentException.class )
  public void testConstructorWithNullAuthenticationThrows() {
    //noinspection DataFlowIssue
    new SpringAuthenticationAuthorizationUser( null );
  }

  @Test
  public void testConstructorWithValidAuthenticationSucceeds() {
    when( mockAuthentication.getPrincipal() ).thenReturn( "test-user" );
    when( mockAuthentication.getAuthorities() ).thenReturn( Collections.emptyList() );

    var user = new SpringAuthenticationAuthorizationUser( mockAuthentication );

    assertNotNull( "User should be created successfully", user );
  }
  // endregion

  // region getName() Tests
  @Test
  public void testGetNameWithUserDetailsPrincipalReturnsUsername() {
    String expectedUsername = "test-user";
    when( mockUserDetails.getUsername() ).thenReturn( expectedUsername );
    when( mockAuthentication.getPrincipal() ).thenReturn( mockUserDetails );

    var user = new SpringAuthenticationAuthorizationUser( mockAuthentication );

    String actualName = user.getName();

    assertEquals( expectedUsername, actualName );
    verify( mockUserDetails, times( 1 ) ).getUsername();
  }

  @Test
  public void testGetNameWithStringPrincipalReturnsToString() {
    String expectedName = "simple-user";
    when( mockAuthentication.getPrincipal() ).thenReturn( expectedName );

    var user = new SpringAuthenticationAuthorizationUser( mockAuthentication );

    String actualName = user.getName();

    assertEquals( expectedName, actualName );
  }

  @Test
  public void testGetNameWithCustomPrincipalReturnsToString() {
    Object customPrincipal = new Object() {
      @Override
      public String toString() {
        return "custom-principal";
      }
    };

    when( mockAuthentication.getPrincipal() ).thenReturn( customPrincipal );

    var user = new SpringAuthenticationAuthorizationUser( mockAuthentication );

    String actualName = user.getName();

    assertEquals( "custom-principal", actualName );
  }
  // endregion

  // region getRoles() Tests
  @Test
  public void testGetRolesWithEmptyAuthoritiesReturnsEmptySet() {
    when( mockAuthentication.getAuthorities() ).thenReturn( Collections.emptyList() );
    when( mockAuthentication.getPrincipal() ).thenReturn( "test-user" );

    var user = new SpringAuthenticationAuthorizationUser( mockAuthentication );

    Set<IAuthorizationRole> roles = user.getRoles();

    assertNotNull( roles );
    assertTrue( roles.isEmpty() );
  }

  @Test
  public void testGetRolesWithMultipleAuthoritiesReturnsMultipleRoles() {
    Collection<GrantedAuthority> authorities = Arrays.asList( authority1, authority2, authority3 );
    doReturn( authorities ).when( mockAuthentication ).getAuthorities();
    when( mockAuthentication.getPrincipal() ).thenReturn( "test-user" );

    var user = new SpringAuthenticationAuthorizationUser( mockAuthentication );

    Set<IAuthorizationRole> roles = user.getRoles();

    assertEquals( 3, roles.size() );

    // Convert to array to check order preservation.
    IAuthorizationRole[] roleArray = roles.toArray( new IAuthorizationRole[ 0 ] );
    assertEquals( "ROLE_USER", roleArray[ 0 ].getName() );
    assertEquals( "ROLE_ADMIN", roleArray[ 1 ].getName() );
    assertEquals( "ROLE_MANAGER", roleArray[ 2 ].getName() );
  }

  @Test
  public void testBuildRolesHandlesDuplicateAuthorities() {
    // Test behavior with duplicate authorities
    GrantedAuthority auth1 = new SimpleGrantedAuthority( "ROLE_USER" );
    GrantedAuthority auth2 = new SimpleGrantedAuthority( "ROLE_ADMIN" );
    GrantedAuthority auth3 = new SimpleGrantedAuthority( "ROLE_USER" ); // Duplicate

    doReturn( Arrays.asList( auth1, auth2, auth3 ) ).when( mockAuthentication ).getAuthorities();
    when( mockAuthentication.getPrincipal() ).thenReturn( "test-user" );

    var user = new SpringAuthenticationAuthorizationUser( mockAuthentication );

    Set<IAuthorizationRole> roles = user.getRoles();

    assertEquals( 2, roles.size() );

    boolean hasUserRole = roles.stream().anyMatch( role -> "ROLE_USER".equals( role.getName() ) );
    boolean hasAdminRole = roles.stream().anyMatch( role -> "ROLE_ADMIN".equals( role.getName() ) );

    assertTrue( hasUserRole );
    assertTrue( hasAdminRole );
  }

  @Test
  public void testGetRolesReturnsUnmodifiableSet() {
    doReturn( Collections.singletonList( authority1 ) ).when( mockAuthentication ).getAuthorities();
    when( mockAuthentication.getPrincipal() ).thenReturn( "test-user" );

    var user = new SpringAuthenticationAuthorizationUser( mockAuthentication );

    Set<IAuthorizationRole> roles = user.getRoles();

    // Attempt to modify the returned set should throw UnsupportedOperationException
    try {
      roles.add( new AuthorizationRole( "ROLE_NEW" ) );
      fail( "Should have thrown UnsupportedOperationException" );
    } catch ( UnsupportedOperationException e ) {
      // Expected behavior
    }
  }

  @Test
  public void testGetRolesCachesRoles() {
    when( mockAuthentication.getPrincipal() ).thenReturn( "test-user" );
    doReturn( Arrays.asList( authority1, authority2 ) ).when( mockAuthentication ).getAuthorities();

    var user = new SpringAuthenticationAuthorizationUser( mockAuthentication );

    Set<IAuthorizationRole> roles1 = user.getRoles();
    Set<IAuthorizationRole> roles2 = user.getRoles();

    assertSame( roles1, roles2 );
  }
  // endregion
}
