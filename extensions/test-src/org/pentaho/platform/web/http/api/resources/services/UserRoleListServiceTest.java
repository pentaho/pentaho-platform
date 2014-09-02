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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.web.http.api.resources.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

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
    userRoleListService = spy( new UserRoleListService() );
  }

  @After
  public void cleanup() {
    userRoleListService = null;
  }

  @Test
  public void testDoGetRolesForUser() throws Exception {
    doReturn( true ).when( userRoleListService ).canAdminister();
    doReturn( "admin, guest" ).when( userRoleListService ).getRolesForUser( "Administrator" );
    String roles = userRoleListService.doGetRolesForUser( "Administrator" );
    assertTrue( roles.length() > 0 );

    try {
      doReturn( false ).when( userRoleListService ).canAdminister();
      userRoleListService.doGetRolesForUser( "unauthorized" );
    } catch ( Exception e ) {
      assertTrue( e instanceof UserRoleListService.UnauthorizedException );
    }
  }

  @Test
  public void testDoGetUsersInRole() throws Exception {
    doReturn( true ).when( userRoleListService ).canAdminister();
    doReturn( "Administrator, Guest" ).when( userRoleListService ).getUsersInRole( "admin" );
    String users = userRoleListService.doGetUsersInRole( "admin" );
    assertTrue( users.length() > 0 );

    try {
      doReturn( false ).when( userRoleListService ).canAdminister();
      userRoleListService.doGetUsersInRole( "unauthorized" );
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

    RoleListWrapper roleWrapper = userRoleListService.getPermissionRoles( "ADMIN_ROLE" );
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
}
