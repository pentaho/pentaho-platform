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
 * Copyright (c) 2002-2024 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.web.http.api.resources;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.web.http.api.resources.services.UserRoleListService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class UserRoleListResourceTest {

  UserRoleListService userRoleListServiceMock;
  UserRoleListResource userRoleListResource;

  @Before
  public void setup() {
    userRoleListServiceMock = mock( UserRoleListService.class );
    userRoleListResource = spy( new UserRoleListResource( userRoleListServiceMock ) );
  }

  @After
  public void teardown() {
    userRoleListResource = null;
  }

  @Test
  public void testGetRolesForUser() throws Exception {
    String user = "user";
    String role1 = "role_1";
    String role2 = "role_2";

    List<String> listRoles = new ArrayList<>();
    listRoles.add( role1 );
    listRoles.add( role2 );
    doReturn( listRoles ).when( userRoleListServiceMock ).doGetRolesForUser( user );

    RolesWrapper rolesWrapper = userRoleListResource.getRolesForUser( user );

    assertEquals( listRoles.size(), rolesWrapper.getRoles().size() );
    assertEquals( listRoles.get( 0 ), rolesWrapper.getRoles().get( 0 ) );
    assertEquals( listRoles.get( 1 ), rolesWrapper.getRoles().get( 1 ) );

    verify( userRoleListServiceMock, times( 1 ) ).doGetRolesForUser( user );
  }

  @Test
  public void testGetUsers() throws Exception {
    UserListWrapper mockWrapper = mock( UserListWrapper.class );
    doReturn( mockWrapper ).when( userRoleListServiceMock ).getUsers();

    UserListWrapper testWrapper = userRoleListResource.getUsers();
    assertEquals( mockWrapper, testWrapper );

    verify( userRoleListServiceMock, times( 1 ) ).getUsers();
  }

  @Test
  public void testGetPermissionUsers() throws Exception {
    UserListWrapper mockWrapper = mock( UserListWrapper.class );
    doReturn( mockWrapper ).when( userRoleListResource ).getUsers();

    UserListWrapper testWrapper = userRoleListResource.getPermissionUsers();
    assertEquals( mockWrapper, testWrapper );

    verify( userRoleListResource, times( 1 ) ).getUsers();
  }

  @Test
  public void testGetUsersInRole() throws Exception {
    String role = "role";
    String user1 = "user_1";
    String user2 = "user_2";

    List<String> listUsers = new ArrayList<>();
    listUsers.add( user1 );
    listUsers.add( user2 );
    doReturn( listUsers ).when( userRoleListServiceMock ).doGetUsersInRole( role );

    UsersWrapper usersWrapper = userRoleListResource.getUsersInRole( role );

    assertEquals( listUsers.size(), usersWrapper.getUsers().size() );
    assertEquals( listUsers.get( 0 ), usersWrapper.getUsers().get( 0 ) );
    assertEquals( listUsers.get( 1 ), usersWrapper.getUsers().get( 1 ) );

    verify( userRoleListServiceMock, times( 1 ) ).doGetUsersInRole( role );
  }

  @Test
  public void testGetRoles() throws Exception {
    RoleListWrapper mockWrapper = mock( RoleListWrapper.class );
    boolean includeExtraRoles = true;
    doReturn( mockWrapper ).when( userRoleListServiceMock ).getRoles( includeExtraRoles );

    RoleListWrapper testWrapper = userRoleListResource.getRoles( "true" );
    assertEquals( mockWrapper, testWrapper );

    verify( userRoleListServiceMock, times( 1 ) ).getRoles( includeExtraRoles );
  }

  @Test
  public void testGetAllRoles() {
    RoleListWrapper mockWrapper = mock( RoleListWrapper.class );
    doReturn( mockWrapper ).when( userRoleListServiceMock ).getAllRoles( false );

    RoleListWrapper testWrapper = userRoleListResource.getAllRoles( false );
    assertEquals( mockWrapper, testWrapper );

    verify( userRoleListServiceMock, times( 1 ) ).getAllRoles( false );
  }

  @Test
  public void testGetAllRolesWithoutAnonymous() {
    RoleListWrapper mockWrapper = mock( RoleListWrapper.class );
    doReturn( mockWrapper ).when( userRoleListServiceMock ).getAllRoles( true );

    RoleListWrapper testWrapper = userRoleListResource.getAllRoles( true );
    assertEquals( mockWrapper, testWrapper );

    verify( userRoleListServiceMock, times( 1 ) ).getAllRoles( true );
  }

  @Test
  public void testGetSystemRoles() throws Exception {
    RoleListWrapper mockWrapper = mock( RoleListWrapper.class );
    doReturn( mockWrapper ).when( userRoleListServiceMock ).getSystemRoles();

    RoleListWrapper testWrapper = userRoleListResource.getSystemRoles();
    assertEquals( mockWrapper, testWrapper );

    verify( userRoleListServiceMock, times( 1 ) ).getSystemRoles();
  }

  @Test
  public void testGetExtraRoles() throws Exception {
    RoleListWrapper mockWrapper = mock( RoleListWrapper.class );
    doReturn( mockWrapper ).when( userRoleListServiceMock ).getExtraRolesList();

    RoleListWrapper testWrapper = userRoleListResource.getExtraRoles();
    assertEquals( mockWrapper, testWrapper );

    verify( userRoleListServiceMock, times( 1 ) ).getExtraRolesList();
  }

  @Test
  public void testGetPermissionRoles() throws Exception {
    String adminRole = "adminRole";
    doReturn( adminRole ).when( userRoleListServiceMock ).getAdminRole();

    RoleListWrapper mockWrapper = mock( RoleListWrapper.class );
    doReturn( mockWrapper ).when( userRoleListServiceMock ).getPermissionRoles();

    RoleListWrapper testWrapper = userRoleListResource.getPermissionRoles();
    assertEquals( mockWrapper, testWrapper );
  }

  @Test
  public void testGetAdminRole() {
    String adminRole = "adminRole";
    doReturn( adminRole ).when( userRoleListServiceMock ).getAdminRole();

    String result = userRoleListResource.getAdminRole();
    assertEquals( adminRole, result );
  }
}
