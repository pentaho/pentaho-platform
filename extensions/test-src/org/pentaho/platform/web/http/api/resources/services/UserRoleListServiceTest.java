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

package org.pentaho.platform.web.http.api.resources.services;

import java.util.ArrayList;
import java.util.List;
import org.mockito.Mockito;
import org.junit.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.web.http.api.resources.RoleListWrapper;
import org.pentaho.platform.web.http.api.resources.UserListWrapper;

public class UserRoleListServiceTest {

  private UserRoleListService userRoleListService;

  @Before
  public void setUp() {
    userRoleListService = Mockito.spy( new UserRoleListService() );
  }

  @After
  public void cleanup() {
    userRoleListService = null;
  }

  @Test
  public void testDoGetRolesForUser() throws Exception {
    String user = "TestUser";
    Mockito.doReturn( true ).when( userRoleListService ).canAdminister();
    List<String> testRoles = new ArrayList<>();
    testRoles.add( "Role_1" );
    testRoles.add( "Role_2" );
    Mockito.doReturn( testRoles ).when( userRoleListService ).getRolesForUser( user );
    List<String> receivedRoles = userRoleListService.doGetRolesForUser( user );
    Assert.assertTrue( receivedRoles.size() == 2 );
    Assert.assertEquals( testRoles.get( 0 ), receivedRoles.get( 0 ) );
    Assert.assertEquals( testRoles.get( 1 ), receivedRoles.get( 1 ) );

    try {
      Mockito.doReturn( false ).when( userRoleListService ).canAdminister();
      userRoleListService.doGetRolesForUser( user );
    } catch ( Exception e ) {
      Assert.assertTrue( e instanceof UserRoleListService.UnauthorizedException );
    }
  }

  @Test
  public void testDoGetUsersInRole() throws Exception {
    String role = "TestRole";
    Mockito.doReturn( true ).when( userRoleListService ).canAdminister();
    List<String> testUsers = new ArrayList<>();
    testUsers.add( "User_1" );
    testUsers.add( "User_2" );
    Mockito.doReturn( testUsers ).when( userRoleListService ).getUsersInRole( role );
    List<String> receivedUsers = userRoleListService.getUsersInRole( role );
    Assert.assertTrue( receivedUsers.size() == 2 );
    Assert.assertEquals( testUsers.get( 0 ), receivedUsers.get( 0 ) );
    Assert.assertEquals( testUsers.get( 1 ), receivedUsers.get( 1 ) );

    try {
      Mockito.doReturn( false ).when( userRoleListService ).canAdminister();
      userRoleListService.doGetUsersInRole( role );
    } catch ( Exception e ) {
      Assert.assertTrue( e instanceof UserRoleListService.UnauthorizedException );
    }
  }

  @Test
  public void testGetUsers() {
    IUserRoleListService service = Mockito.mock( IUserRoleListService.class );
    Mockito.doReturn( service ).when( userRoleListService ).getUserRoleListService();

    List<String> users = new ArrayList<String>();
    users.add( "admin" );
    users.add( "joe" );
    users.add( "suzy" );
    Mockito.doReturn( users ).when( service ).getAllUsers();

    UserListWrapper usersWrapper = userRoleListService.getUsers();
    Assert.assertTrue( usersWrapper.getUsers().size() == 3 );
  }

  @Test
  public void testDoGetRoles() {
    List<String> roles = new ArrayList<String>();
    roles.add( "ROLE1" );
    roles.add( "ROLE2" );

    IUserRoleListService userRoleListService1 = Mockito.mock( IUserRoleListService.class );

    Mockito.doReturn( userRoleListService1 ).when( userRoleListService ).getUserRoleListService();
    Mockito.doReturn( roles ).when( userRoleListService1 ).getAllRoles();

    RoleListWrapper roleListWrapper = userRoleListService.getRoles();

    Mockito.verify( userRoleListService ).getUserRoleListService();
    Mockito.verify( userRoleListService1 ).getAllRoles();

    Assert.assertEquals( roles, roleListWrapper.getRoles() );
  }

  @Test
  public void testGetPermissionRoles() {

    List<String> roles = new ArrayList<String>();
    roles.add( "ROLE1" );
    roles.add( "ROLE2" );
    roles.add( "ADMIN_ROLE" );

    IUserRoleListService userRoleListService1 = Mockito.mock( IUserRoleListService.class );
    Mockito.doReturn( userRoleListService1 ).when( userRoleListService ).getUserRoleListService();
    Mockito.doReturn( roles ).when( userRoleListService1 ).getAllRoles();

    ArrayList<String> extraRoles = new ArrayList<String>();
    extraRoles.add( "EXTRA_ROLE1" );
    extraRoles.add( "EXTRA_ROLE2" );
    userRoleListService.setExtraRoles( extraRoles );

    RoleListWrapper roleWrapper = userRoleListService.getPermissionRoles( "ADMIN_ROLE" );
    Assert.assertTrue( !roleWrapper.getRoles().contains( "ADMIN_ROLE" ) );
    Assert.assertTrue( roleWrapper.getRoles().size() == 4 );
  }

  @Test
  public void testDoGetAllRoles() {
    List<String> roles = new ArrayList<String>();
    roles.add( "ROLE1" );
    roles.add( "ROLE2" );

    List<String> extraRoles = new ArrayList<String>();
    extraRoles.add( "ROLE3" );
    extraRoles.add( "ROLE4" );

    IUserRoleListService userRoleListService1 = Mockito.mock( IUserRoleListService.class );

    Mockito.doReturn( userRoleListService1 ).when( userRoleListService ).getUserRoleListService();
    Mockito.doReturn( roles ).when( userRoleListService1 ).getAllRoles();
    Mockito.doReturn( extraRoles ).when( userRoleListService ).getExtraRoles();

    RoleListWrapper roleListWrapper = userRoleListService.getAllRoles();

    Mockito.verify( userRoleListService ).getUserRoleListService();
    Mockito.verify( userRoleListService1 ).getAllRoles();
    Mockito.verify( userRoleListService ).getExtraRoles();

    Assert.assertEquals( 4, roleListWrapper.getRoles().size() );
  }

  @Test
  public void testDoGetExtraRolesList() {
    List<String> extraRoles = new ArrayList<String>();
    extraRoles.add( "ROLE1" );
    extraRoles.add( "ROLE2" );
    extraRoles.add( "ROLE3" );
    extraRoles.add( "ROLE4" );

    Mockito.doReturn( extraRoles ).when( userRoleListService ).getExtraRoles();

    RoleListWrapper roleListWrapper = userRoleListService.getExtraRolesList();

    Mockito.verify( userRoleListService ).getExtraRoles();

    Assert.assertEquals( 4, roleListWrapper.getRoles().size() );
    Assert.assertEquals( extraRoles, roleListWrapper.getRoles() );
  }
}
