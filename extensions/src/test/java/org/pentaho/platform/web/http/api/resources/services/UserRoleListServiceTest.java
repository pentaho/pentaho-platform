/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.platform.web.http.api.resources.services;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.web.http.api.resources.RoleListWrapper;
import org.pentaho.platform.web.http.api.resources.UserListWrapper;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class UserRoleListServiceTest {

  private UserRoleListService userRoleListService;

  @Before
  public void setUp() {
    userRoleListService = spy( new UserRoleListService() );
  }

  @After
  public void cleanup() {
    userRoleListService = null;
  }

  @Test
  public void testDoGetRolesForUser() throws Exception {
    String user = "TestUser";
    doReturn( true ).when( userRoleListService ).canAdminister();
    List<String> testRoles = new ArrayList<>();
    testRoles.add( "Role_1" );
    testRoles.add( "Role_2" );
    doReturn( testRoles ).when( userRoleListService ).getRolesForUser( user );
    List<String> receivedRoles = userRoleListService.doGetRolesForUser( user );
    assertTrue( receivedRoles.size() == 2 );
    assertEquals( testRoles.get( 0 ), receivedRoles.get( 0 ) );
    assertEquals( testRoles.get( 1 ), receivedRoles.get( 1 ) );

    try {
      doReturn( false ).when( userRoleListService ).canAdminister();
      userRoleListService.doGetRolesForUser( user );
    } catch ( Exception e ) {
      assertTrue( e instanceof UserRoleListService.UnauthorizedException );
    }
  }

  @Test
  public void testDoGetUsersInRole() throws Exception {
    String role = "TestRole";
    doReturn( true ).when( userRoleListService ).canAdminister();
    List<String> testUsers = new ArrayList<>();
    testUsers.add( "User_1" );
    testUsers.add( "User_2" );
    doReturn( testUsers ).when( userRoleListService ).getUsersInRole( role );
    List<String> receivedUsers = userRoleListService.getUsersInRole( role );
    assertTrue( receivedUsers.size() == 2 );
    assertEquals( testUsers.get( 0 ), receivedUsers.get( 0 ) );
    assertEquals( testUsers.get( 1 ), receivedUsers.get( 1 ) );

    try {
      doReturn( false ).when( userRoleListService ).canAdminister();
      userRoleListService.doGetUsersInRole( role );
    } catch ( Exception e ) {
      assertTrue( e instanceof UserRoleListService.UnauthorizedException );
    }
  }

  @Test
  public void testGetUsers() {
    IUserRoleListService service = mock( IUserRoleListService.class );
    doReturn( service ).when( userRoleListService ).getUserRoleListService();

    List<String> users = new ArrayList<String>();
    users.add( "admin" );
    users.add( "joe" );
    users.add( "suzy" );
    doReturn( users ).when( service ).getAllUsers();

    UserListWrapper usersWrapper = userRoleListService.getUsers();
    assertTrue( usersWrapper.getUsers().size() == 3 );
  }

  @Test
  public void testDoGetRoles() {
    List<String> roles = new ArrayList<String>();
    roles.add( "ROLE1" );
    roles.add( "ROLE2" );

    IUserRoleListService userRoleListService1 = mock( IUserRoleListService.class );

    doReturn( userRoleListService1 ).when( userRoleListService ).getUserRoleListService();
    doReturn( roles ).when( userRoleListService1 ).getAllRoles();

    RoleListWrapper roleListWrapper = userRoleListService.getRoles();

    verify( userRoleListService ).getUserRoleListService();
    verify( userRoleListService1 ).getAllRoles();

    assertEquals( roles, roleListWrapper.getRoles() );
  }

  @Test
  public void testGetPermissionRoles() {

    List<String> roles = new ArrayList<String>();
    roles.add( "ROLE1" );
    roles.add( "ROLE2" );
    roles.add( "ADMIN_ROLE" );

    IUserRoleListService userRoleListService1 = mock( IUserRoleListService.class );
    doReturn( userRoleListService1 ).when( userRoleListService ).getUserRoleListService();
    doReturn( roles ).when( userRoleListService1 ).getAllRoles();

    ArrayList<String> extraRoles = new ArrayList<String>();
    extraRoles.add( "EXTRA_ROLE1" );
    extraRoles.add( "EXTRA_ROLE2" );
    userRoleListService.setExtraRoles( extraRoles );
    userRoleListService.setAdminRole( "ADMIN_ROLE" );

    RoleListWrapper roleWrapper = userRoleListService.getPermissionRoles();
    assertTrue( !roleWrapper.getRoles().contains( "ADMIN_ROLE" ) );
    assertTrue( roleWrapper.getRoles().size() == 4 );
  }

  @Test
  public void testDoGetAllRoles() {
    List<String> roles = new ArrayList<String>();
    roles.add( "ROLE1" );
    roles.add( "ROLE2" );

    List<String> extraRoles = new ArrayList<String>();
    extraRoles.add( "ROLE3" );
    extraRoles.add( "ROLE4" );

    IUserRoleListService userRoleListService1 = mock( IUserRoleListService.class );

    doReturn( userRoleListService1 ).when( userRoleListService ).getUserRoleListService();
    doReturn( roles ).when( userRoleListService1 ).getAllRoles();
    doReturn( extraRoles ).when( userRoleListService ).getExtraRoles();

    RoleListWrapper roleListWrapper = userRoleListService.getAllRoles();

    verify( userRoleListService ).getUserRoleListService();
    verify( userRoleListService1 ).getAllRoles();
    verify( userRoleListService ).getExtraRoles();

    assertEquals( 4, roleListWrapper.getRoles().size() );
  }

  @Test
  public void testDoGetExtraRolesList() {
    List<String> extraRoles = new ArrayList<String>();
    extraRoles.add( "ROLE1" );
    extraRoles.add( "ROLE2" );
    extraRoles.add( "ROLE3" );
    extraRoles.add( "ROLE4" );

    doReturn( extraRoles ).when( userRoleListService ).getExtraRoles();

    RoleListWrapper roleListWrapper = userRoleListService.getExtraRolesList();

    verify( userRoleListService ).getExtraRoles();

    assertEquals( 4, roleListWrapper.getRoles().size() );
    assertEquals( extraRoles, roleListWrapper.getRoles() );
  }

  @Test
  public void testAllRolesExcludingAnonymous() {
    final String ANONYMOUS = "Anonymous";
    List<String> roles = new ArrayList<String>();
    roles.add( "ROLE1" );
    roles.add( "ROLE2" );
    roles.add( "ROLE3" );
    roles.add( "ROLE4" );
    roles.add( ANONYMOUS );

    IUserRoleListService userRoleListService1 = mock( IUserRoleListService.class );
    doReturn( userRoleListService1 ).when( userRoleListService ).getUserRoleListService();
    doReturn( roles ).when( userRoleListService1 ).getAllRoles();
    doReturn( ANONYMOUS ).when( userRoleListService ).getAnonymousRole();

    RoleListWrapper roleListWrapper = userRoleListService.getAllRoles( true );

    verify( userRoleListService ).getAllRoles( true );

    assertEquals( 4, roleListWrapper.getRoles().size() );
    assertFalse( roleListWrapper.getRoles().contains( ANONYMOUS ) );
  }
}
