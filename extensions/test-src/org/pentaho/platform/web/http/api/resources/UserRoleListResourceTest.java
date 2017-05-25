/*!
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
 * Copyright (c) 2002-2017 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.web.http.api.resources;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.junit.Assert;
import org.pentaho.platform.web.http.api.resources.services.UserRoleListService;

import java.util.ArrayList;
import java.util.List;


public class UserRoleListResourceTest {

  UserRoleListResource userRoleListResource;

  @Before
  public void setup() {
    userRoleListResource = Mockito.spy( new UserRoleListResource() );
    userRoleListResource.userRoleListService = Mockito.mock( UserRoleListService.class );
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
    Mockito.doReturn( listRoles ).when( userRoleListResource.userRoleListService ).doGetRolesForUser( user );

    RolesWrapper rolesWrapper = userRoleListResource.getRolesForUser( user );

    Assert.assertEquals( listRoles.size(), rolesWrapper.getRoles().size() );
    Assert.assertEquals( listRoles.get( 0 ), rolesWrapper.getRoles().get( 0 ) );
    Assert.assertEquals( listRoles.get( 1 ), rolesWrapper.getRoles().get( 1 ) );

    Mockito.verify( userRoleListResource.userRoleListService, Mockito.times( 1 ) ).doGetRolesForUser( user );
  }

  @Test
  public void testGetUsers() throws Exception {
    UserListWrapper mockWrapper = Mockito.mock( UserListWrapper.class );
    Mockito.doReturn( mockWrapper ).when( userRoleListResource.userRoleListService ).getUsers();

    UserListWrapper testWrapper = userRoleListResource.getUsers();
    Assert.assertEquals( mockWrapper, testWrapper );

    Mockito.verify( userRoleListResource.userRoleListService, Mockito.times( 1 ) ).getUsers();
  }

  @Test
  public void testGetPermissionUsers() throws Exception {
    UserListWrapper mockWrapper = Mockito.mock( UserListWrapper.class );
    Mockito.doReturn( mockWrapper ).when( userRoleListResource ).getUsers();

    UserListWrapper testWrapper = userRoleListResource.getPermissionUsers();
    Assert.assertEquals( mockWrapper, testWrapper );

    Mockito.verify( userRoleListResource, Mockito.times( 1 ) ).getUsers();
  }

  @Test
  public void testGetUsersInRole() throws Exception {
    String role = "role";
    String user1 = "user_1";
    String user2 = "user_2";

    List<String> listUsers = new ArrayList<>();
    listUsers.add( user1 );
    listUsers.add( user2 );
    Mockito.doReturn( listUsers ).when( userRoleListResource.userRoleListService ).doGetUsersInRole( role );

    UsersWrapper usersWrapper = userRoleListResource.getUsersInRole( role );

    Assert.assertEquals( listUsers.size(), usersWrapper.getUsers().size() );
    Assert.assertEquals( listUsers.get( 0 ), usersWrapper.getUsers().get( 0 ) );
    Assert.assertEquals( listUsers.get( 1 ), usersWrapper.getUsers().get( 1 ) );

    Mockito.verify( userRoleListResource.userRoleListService, Mockito.times( 1 ) ).doGetUsersInRole( role );
  }

  @Test
  public void testGetRoles() throws Exception {
    RoleListWrapper mockWrapper = Mockito.mock( RoleListWrapper.class );
    boolean includeExtraRoles = true;
    Mockito.doReturn( mockWrapper ).when( userRoleListResource.userRoleListService ).getRoles( includeExtraRoles );

    RoleListWrapper testWrapper = userRoleListResource.getRoles( "true" );
    Assert.assertEquals( mockWrapper, testWrapper );

    Mockito.verify( userRoleListResource.userRoleListService, Mockito.times( 1 ) ).getRoles( includeExtraRoles );
  }

  @Test
  public void testGetAllRoles() throws Exception {
    RoleListWrapper mockWrapper = Mockito.mock( RoleListWrapper.class );
    Mockito.doReturn( mockWrapper ).when( userRoleListResource.userRoleListService ).getAllRoles();

    RoleListWrapper testWrapper = userRoleListResource.getAllRoles();
    Assert.assertEquals( mockWrapper, testWrapper );

    Mockito.verify( userRoleListResource.userRoleListService, Mockito.times( 1 ) ).getAllRoles();
  }

  @Test
  public void testGetSystemRoles() throws Exception {
    RoleListWrapper mockWrapper = Mockito.mock( RoleListWrapper.class );
    Mockito.doReturn( mockWrapper ).when( userRoleListResource.userRoleListService ).getSystemRoles();

    RoleListWrapper testWrapper = userRoleListResource.getSystemRoles();
    Assert.assertEquals( mockWrapper, testWrapper );

    Mockito.verify( userRoleListResource.userRoleListService, Mockito.times( 1 ) ).getSystemRoles();
  }

  @Test
  public void testGetExtraRoles() throws Exception {
    RoleListWrapper mockWrapper = Mockito.mock( RoleListWrapper.class );
    Mockito.doReturn( mockWrapper ).when( userRoleListResource.userRoleListService ).getExtraRolesList();

    RoleListWrapper testWrapper = userRoleListResource.getExtraRoles();
    Assert.assertEquals( mockWrapper, testWrapper );

    Mockito.verify( userRoleListResource.userRoleListService, Mockito.times( 1 ) ).getExtraRolesList();
  }

  @Test
  public void testGetPermissionRoles() throws Exception {
    String adminRole = "adminRole";
    userRoleListResource.adminRole = adminRole;

    RoleListWrapper mockWrapper = Mockito.mock( RoleListWrapper.class );
    Mockito.doReturn( mockWrapper ).when( userRoleListResource.userRoleListService ).getPermissionRoles( adminRole );

    RoleListWrapper testWrapper = userRoleListResource.getPermissionRoles();
    Assert.assertEquals( mockWrapper, testWrapper );

    Mockito.verify( userRoleListResource.userRoleListService, Mockito.times( 1 ) ).getPermissionRoles( adminRole );
  }
}
