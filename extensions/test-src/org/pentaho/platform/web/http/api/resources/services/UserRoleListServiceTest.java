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
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoUser;
import org.pentaho.platform.api.engine.security.userroledao.IUserRoleDao;
import org.pentaho.platform.api.engine.security.userroledao.UncategorizedUserRoleDaoException;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.security.DefaultRoleComparator;
import org.pentaho.platform.engine.security.DefaultUsernameComparator;
import org.pentaho.platform.web.http.api.resources.RoleListWrapper;
import org.pentaho.platform.web.http.api.resources.UserListWrapper;

public class UserRoleListServiceTest {

  private UserRoleListService userRoleListServiceSpy;
  private UserRoleListService userRoleListService;

  @BeforeClass
  public static void startup() {
    PentahoSystem.init();
  }

  @AfterClass
  public static void shutdown() {
    PentahoSystem.shutdown();
  }


  @Before
  public void setUp() {
    userRoleListServiceSpy = spy( new UserRoleListService() );

    userRoleListService = new UserRoleListService();
    userRoleListService.setExtraRoles( new ArrayList<String>() );
    userRoleListService.setSystemRoles( new ArrayList<String>() );
    userRoleListService.setRoleComparator( mock( DefaultRoleComparator.class ) );
    userRoleListService.setUserComparator( mock( DefaultUsernameComparator.class ) );
  }

  @After
  public void cleanup() {
    userRoleListServiceSpy = null;
    PentahoSystem.clearObjectFactory();
  }

  @Test
  public void testDoGetRolesForUser() throws Exception {
    doReturn( "admin, guest" ).when( userRoleListServiceSpy ).getRolesForUser( "Administrator" );
    String roles = userRoleListServiceSpy.doGetRolesForUser( "Administrator" );
    assertTrue( roles.length() > 0 );
  }

  @Test
  public void testDoGetUsersInRole() throws Exception {
    doReturn( true ).when( userRoleListServiceSpy ).canAdminister();
    doReturn( "Administrator, Guest" ).when( userRoleListServiceSpy ).getUsersInRole( "admin" );
    String users = userRoleListServiceSpy.doGetUsersInRole( "admin" );
    assertTrue( users.length() > 0 );

    try {
      doReturn( false ).when( userRoleListServiceSpy ).canAdminister();
      userRoleListServiceSpy.doGetUsersInRole( "unauthorized" );
    } catch ( Exception e ) {
      assertTrue( e instanceof UserRoleListService.UnauthorizedException );
    }
  }

  @Test
  public void testGetUsers() {
    IUserRoleListService service = mock( IUserRoleListService.class );
    doReturn( service ).when( userRoleListServiceSpy ).getUserRoleListService();

    List<String> users = new ArrayList<String>();
    users.add( "admin" );
    users.add( "joe" );
    users.add( "suzy" );
    doReturn( users ).when( service ).getAllUsers();

    UserListWrapper usersWrapper = userRoleListServiceSpy.getUsers();
    assertTrue( usersWrapper.getUsers().size() == 3 );
  }

  @Test
  public void testDeleteUsers() {
    String users = "admin\tsuzy\t";

    IAuthorizationPolicy policy = mock( IAuthorizationPolicy.class );
    when( policy.isAllowed( anyString() ) ).thenReturn( true );
    PentahoSystem.registerObject( policy );

    IPentahoUser user = mock( IPentahoUser.class );
    IUserRoleDao roleDao = mock( IUserRoleDao.class );
    when( roleDao.getUser( any( ITenant.class ), anyString() ) ).thenReturn( user );
    PentahoSystem.registerObject( roleDao );

    try {
      userRoleListService.deleteUsers( users );
    } catch ( UserRoleListService.UnauthorizedException e ) {
      e.printStackTrace();
    }

    verify( roleDao, times( 2 ) ).deleteUser( any( IPentahoUser.class ) );
  }

  @Test
  public void testDeleteUsersUnauthorizedException() {
    String users = "admin\tsuzy\t";

    IAuthorizationPolicy policy = mock( IAuthorizationPolicy.class );
    when( policy.isAllowed( anyString() ) ).thenReturn( false );
    PentahoSystem.registerObject( policy );

    try {
      userRoleListService.deleteUsers( users );
    } catch ( UserRoleListService.UnauthorizedException e ) {
      //Expected test failure
    }
  }

  @Test
  public void testDeleteUsersUncategorizedUserRoleDaoException() {
    String users = "admin\tsuzy\t";

    IAuthorizationPolicy policy = mock( IAuthorizationPolicy.class );
    when( policy.isAllowed( anyString() ) ).thenReturn( true );
    PentahoSystem.registerObject( policy );

    IPentahoUser user = mock( IPentahoUser.class );
    IUserRoleDao roleDao = mock( IUserRoleDao.class );
    when( roleDao.getUser( any( ITenant.class ), anyString() ) )
      .thenThrow( new UncategorizedUserRoleDaoException( "expectedTestException" ) );
    PentahoSystem.registerObject( roleDao );

    try {
      userRoleListService.deleteUsers( users );
    } catch ( UncategorizedUserRoleDaoException e ) {
      //Expected test failure
    } catch ( UserRoleListService.UnauthorizedException e ) {
      fail(); // We should not be reaching this line of code
    }
  }

  @Test
  public void testDoGetRoles() {
    List<String> roles = new ArrayList<String>();
    roles.add( "ROLE1" );
    roles.add( "ROLE2" );

    IUserRoleListService userRoleListService1 = mock( IUserRoleListService.class );

    doReturn( userRoleListService1 ).when( userRoleListServiceSpy ).getUserRoleListService();
    doReturn( roles ).when( userRoleListService1 ).getAllRoles();

    RoleListWrapper roleListWrapper = userRoleListServiceSpy.getRoles();

    verify( userRoleListServiceSpy ).getUserRoleListService();
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
    doReturn( userRoleListService1 ).when( userRoleListServiceSpy ).getUserRoleListService();
    doReturn( roles ).when( userRoleListService1 ).getAllRoles();

    ArrayList<String> extraRoles = new ArrayList<String>();
    extraRoles.add( "EXTRA_ROLE1" );
    extraRoles.add( "EXTRA_ROLE2" );
    userRoleListServiceSpy.setExtraRoles( extraRoles );

    RoleListWrapper roleWrapper = userRoleListServiceSpy.getPermissionRoles( "ADMIN_ROLE" );
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

    doReturn( userRoleListService1 ).when( userRoleListServiceSpy ).getUserRoleListService();
    doReturn( roles ).when( userRoleListService1 ).getAllRoles();
    doReturn( extraRoles ).when( userRoleListServiceSpy ).getExtraRoles();

    RoleListWrapper roleListWrapper = userRoleListServiceSpy.getAllRoles();

    verify( userRoleListServiceSpy ).getUserRoleListService();
    verify( userRoleListService1 ).getAllRoles();
    verify( userRoleListServiceSpy ).getExtraRoles();

    assertEquals( 4, roleListWrapper.getRoles().size() );
  }

  @Test
  public void testDoGetExtraRolesList() {
    List<String> extraRoles = new ArrayList<String>();
    extraRoles.add( "ROLE1" );
    extraRoles.add( "ROLE2" );
    extraRoles.add( "ROLE3" );
    extraRoles.add( "ROLE4" );

    doReturn( extraRoles ).when( userRoleListServiceSpy ).getExtraRoles();

    RoleListWrapper roleListWrapper = userRoleListServiceSpy.getExtraRolesList();

    verify( userRoleListServiceSpy ).getExtraRoles();

    assertEquals( 4, roleListWrapper.getRoles().size() );
    assertEquals( extraRoles, roleListWrapper.getRoles() );
  }
}
