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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import java.util.ArrayList;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.security.userroledao.AlreadyExistsException;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoRole;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoUser;
import org.pentaho.platform.api.engine.security.userroledao.NotFoundException;
import org.pentaho.platform.api.engine.security.userroledao.UncategorizedUserRoleDaoException;
import org.pentaho.platform.api.mt.ITenantManager;
import org.pentaho.platform.security.policy.rolebased.IRoleAuthorizationPolicyRoleBindingDao;
import org.pentaho.platform.web.http.api.resources.services.UserRoleDaoService;

public class UserRoleDaoResourceTest {
  private UserRoleDaoResource userRoleResource;

  //Mocks
  private IRoleAuthorizationPolicyRoleBindingDao roleBindingDao;
  private ITenantManager tenantManager;
  private ArrayList<String> systemRoles;
  private String adminRole;
  private UserRoleDaoService userRoleService;

  @Before
  public void setUp() throws Exception {
    roleBindingDao = mock( IRoleAuthorizationPolicyRoleBindingDao.class );
    tenantManager = mock( ITenantManager.class );
    systemRoles = new ArrayList<String>();
    adminRole = "MockSession";
    userRoleService = mock( UserRoleDaoService.class );
    userRoleResource =
      new UserRoleDaoResource( roleBindingDao, tenantManager, systemRoles, adminRole, userRoleService );
  }

  @Test
  public void testGetUsers() throws Exception {
    UserListWrapper userListWrapper = new UserListWrapper( new ArrayList<IPentahoUser>() );
    when( userRoleService.getUsers() ).thenReturn( userListWrapper );

    assertEquals( userListWrapper, userRoleResource.getUsers() );
  }

  @Test
  public void testGetUsersError() throws Exception {
    try {
      when( userRoleService.getUsers() ).thenThrow( new Exception() );
    } catch ( WebApplicationException e ) {
      assertEquals( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), e.getResponse().getStatus() );
    }
  }

  @Test
  public void testGetRolesForUser() throws Exception {
    String user = "admin";
    RoleListWrapper roleListWrapper = new RoleListWrapper( new ArrayList<IPentahoRole>() );
    when( userRoleService.getRolesForUser( user ) ).thenReturn( roleListWrapper );

    assertEquals( roleListWrapper, userRoleResource.getRolesForUser( user ) );
  }

  @Test
  public void testGetRolesForUserError() throws Exception {
    String user = "admin";
    RoleListWrapper roleListWrapper = new RoleListWrapper( new ArrayList<IPentahoRole>() );
    when( userRoleService.getRolesForUser( user ) ).thenReturn( roleListWrapper );

    assertEquals( roleListWrapper, userRoleResource.getRolesForUser( user ) );
    try {
      when( userRoleService.getRolesForUser( user ) )
        .thenThrow( new UncategorizedUserRoleDaoException( "testException" ) );
    } catch ( WebApplicationException e ) {
      assertEquals( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), e.getResponse().getStatus() );
    }
  }

  @Test
  public void testAssignRoleToUser() {
    String user = "testUser1";
    String roles = "testRole1";

    userRoleResource = spy( userRoleResource );
    IPentahoSession session = mock( IPentahoSession.class );
    doReturn( user ).when( session ).getName();
    doReturn( session ).when( userRoleResource ).getSession();
    doNothing().when( userRoleResource ).updateRolesForCurrentSession();

    Response response = userRoleResource.assignRolesToUser( user, roles );
    assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
  }

  @Test
  public void testAssignRoleToUserSecurityException() {
    String user = "testUser1";
    String roles = "testRole1";
    doThrow( new SecurityException() ).when( userRoleService )
      .assignRolesToUser( anyString(), anyString() );

    try {
      userRoleResource.assignRolesToUser( user, roles );
    } catch ( WebApplicationException e ) {
      assertEquals( Response.Status.FORBIDDEN.getStatusCode(), e.getResponse().getStatus() );
    }
  }

  @Test
  public void testAssignRoleToUserUncategorizedUserRoleDaoException() {
    String user = "testUser1";
    String roles = "testRole1";
    doThrow( new UncategorizedUserRoleDaoException( "expectedTestException" ) ).when( userRoleService )
      .assignRolesToUser( anyString(), anyString() );

    try {
      userRoleResource.assignRolesToUser( user, roles );
    } catch ( WebApplicationException e ) {
      assertEquals( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), e.getResponse().getStatus() );
    }
  }

  @Test
  public void testAssignRoleToUserNotFoundException() {
    String user = "testUser1";
    String roles = "testRole1";

    doThrow( new NotFoundException( "expectedTestException" ) ).when( userRoleService )
      .assignRolesToUser( anyString(), anyString() );

    try {
      userRoleResource.assignRolesToUser( user, roles );
    } catch ( WebApplicationException e ) {
      assertEquals( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), e.getResponse().getStatus() );
    }
  }

  @Test
  public void testRemoveRolesFromUser() {
    String user = "testUser1";
    String roles = "testRole1";

    userRoleResource = spy( userRoleResource );
    IPentahoSession session = mock( IPentahoSession.class );
    doReturn( user ).when( session ).getName();
    doReturn( session ).when( userRoleResource ).getSession();
    doNothing().when( userRoleResource ).updateRolesForCurrentSession();

    Response response = userRoleResource.removeRolesFromUser( user, roles );
    assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
  }

  @Test
  public void testRemoveRolesFromUserSecurityException() {
    String user = "testUser1";
    String roles = "testRole1";

    doThrow( new SecurityException( "expectedTestException" ) ).when( userRoleService )
      .removeRolesFromUser( anyString(), anyString() );

    try {
      userRoleResource.removeRolesFromUser( user, roles );
    } catch ( WebApplicationException e ) {
      assertEquals( Response.Status.FORBIDDEN.getStatusCode(), e.getResponse().getStatus() );
      assertNotNull( e.getResponse().getEntity() );
    }
  }

  @Test
  public void testRemoveRolesFromUserUncategorizedUserRoleDaoException() {
    String user = "testUser1";
    String roles = "testRole1";

    doThrow( new UncategorizedUserRoleDaoException( "expectedTestException" ) ).when( userRoleService )
      .removeRolesFromUser( anyString(), anyString() );

    try {
      userRoleResource.removeRolesFromUser( user, roles );
    } catch ( WebApplicationException e ) {
      assertEquals( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), e.getResponse().getStatus() );
      assertNotNull( e.getResponse().getEntity() );
    }
  }

  @Test
  public void testRemoveRolesFromUserNotFoundException() {
    String user = "testUser1";
    String roles = "testRole1";

    doThrow( new NotFoundException( "expectedTestException" ) ).when( userRoleService )
      .removeRolesFromUser( anyString(), anyString() );

    try {
      userRoleResource.removeRolesFromUser( user, roles );
    } catch ( WebApplicationException e ) {
      assertEquals( Response.Status.NOT_FOUND.getStatusCode(), e.getResponse().getStatus() );
      assertNotNull( e.getResponse().getEntity() );
    }
  }

  @Test
  public void testDeleteUser() {
    String users = "user1\tuser2\tuser3\t";

    Response response = userRoleResource.deleteUsers( users );
    assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
  }

  @Test
  public void testDeleteUserUnauthorizedException() {
    String users = "user1\tuser2\tuser3\t";

    doThrow( new SecurityException() ).when( userRoleService ).deleteUsers( anyString() );

    try {
      Response response = userRoleResource.deleteUsers( users );
    } catch ( WebApplicationException e ) {
      assertEquals( Response.Status.FORBIDDEN.getStatusCode(), e.getResponse().getStatus() );
    }
  }

  @Test
  public void testDeleteUserUncategorizedUserRoleDaoException() {
    String users = "user1\tuser2\tuser3\t";

    doThrow( new UncategorizedUserRoleDaoException( "expectedTestException" ) ).when( userRoleService )
      .deleteUsers( anyString() );

    try {
      Response response = userRoleResource.deleteUsers( users );
    } catch ( WebApplicationException e ) {
      assertEquals( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), e.getResponse().getStatus() );
    }
  }

  @Test
  public void testGetRoles() throws Exception {
    RoleListWrapper roleListWrapper = new RoleListWrapper( new ArrayList<IPentahoRole>() );
    when( userRoleService.getRoles() ).thenReturn( roleListWrapper );

    assertEquals( roleListWrapper, userRoleResource.getRoles() );
  }

  @Test
  public void testGetRolesError() throws Exception {
    try {
      when( userRoleService.getRoles() ).thenThrow( new UncategorizedUserRoleDaoException( "expected" ) );
      userRoleResource.getRoles();
    } catch ( WebApplicationException e ) {
      assertEquals( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), e.getResponse().getStatus() );
    }
  }

  @Test
  public void testGetRoleMembers() throws Exception {
    String role = "Report Author";
    UserListWrapper userListWrapper = new UserListWrapper( new ArrayList<IPentahoUser>() );
    when( userRoleService.getRoleMembers( role ) ).thenReturn( userListWrapper );

    assertEquals( userListWrapper, userRoleResource.getRoleMembers( role ) );
  }

  @Test
  public void testGetRoleMembersSecurityException() throws Exception {
    String role = "Report Author";
    when( userRoleService.getRoleMembers( role ) ).thenThrow( new SecurityException() );
    try {
      userRoleResource.getRoleMembers( role );
    } catch ( WebApplicationException e ) {
      assertEquals( Response.Status.FORBIDDEN.getStatusCode(), e.getResponse().getStatus() );
    }
  }

  @Test
  public void testGetRoleMembersUncategorizedUserRoleDaoException() throws Exception {
    String role = "Report Author";
    when( userRoleService.getRoleMembers( role ) ).thenThrow( new UncategorizedUserRoleDaoException( "expectedException" ) );

    try {
      userRoleResource.getRoleMembers( role );
    } catch ( WebApplicationException e ) {
      assertEquals( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), e.getResponse().getStatus() );
    }
  }

  @Test
  public void testDeleteRole() {
    String roleList = "role1\trole2";
    userRoleResource = spy( userRoleResource );
    doNothing().when( userRoleResource ).updateRolesForCurrentSession();
    assertEquals( Response.Status.OK.getStatusCode(), userRoleResource.deleteRoles( roleList ).getStatus() );
  }

  @Test
  public void testDeleteRoleSecurityException() {
    String roles = "role1\trole2";

    doThrow( new SecurityException() ).when( userRoleService ).deleteRoles( anyString() );

    try {
      userRoleResource.deleteRoles( roles );
    } catch ( WebApplicationException e ) {
      assertEquals( Response.Status.FORBIDDEN.getStatusCode(), e.getResponse().getStatus() );
    }
  }

  @Test
  public void testDeleteRoleUncategorizedUserRoleDaoException() {
    String roles = "role1\trole2";

    doThrow( new UncategorizedUserRoleDaoException( "expectedTestException" ) ).when( userRoleService )
      .deleteRoles( anyString() );

    try {
      userRoleResource.deleteRoles( roles );
    } catch ( WebApplicationException e ) {
      assertEquals( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), e.getResponse().getStatus() );
    }
  }

  @Test
  public void testGetRoleBindingStruct() {
    String locale = "en";

    SystemRolesMap systemRoles = mock( SystemRolesMap.class );
    when( userRoleService.getRoleBindingStruct( anyString() ) ).thenReturn( systemRoles );

    assertEquals( systemRoles, userRoleResource.getRoleBindingStruct( locale ) );
  }

  @Test
  public void testGetRoleBindingStructSecurityException() {
    String locale = "en";

    when( userRoleService.getRoleBindingStruct( anyString() ) ).thenThrow( new SecurityException() );
    try {
      userRoleResource.getRoleBindingStruct( locale );
    } catch ( WebApplicationException e ) {
      assertEquals( Response.Status.FORBIDDEN.getStatusCode(), e.getResponse().getStatus() );
    }
  }

  @Test
  public void testSetLogicalRoles() {
    LogicalRoleAssignments logicalRoles = mock( LogicalRoleAssignments.class );
    userRoleResource.setLogicalRoles( logicalRoles );
    verify( userRoleService ).setLogicalRoles( logicalRoles );
  }

  @Test
  public void testSetLogicalRolesSecurityException() {
    LogicalRoleAssignments logicalRoles = mock( LogicalRoleAssignments.class );
    try {
      userRoleResource.setLogicalRoles( logicalRoles );
    } catch ( WebApplicationException e ) {
      assertEquals( Response.Status.FORBIDDEN.getStatusCode(), e.getResponse().getStatus() );
    }
  }

  @Test
  public void testCreateUser() throws Exception {
    Response response = userRoleResource.createUser( new User( "name", "password" ) );
    assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
  }

  @Test
  public void testCreateUserUnauthorizedException() throws Exception {
    UserRoleDaoService mockService = mock( UserRoleDaoService.class );
    doThrow( new SecurityException() ).when( mockService ).createUser( any( User.class ) );
    UserRoleDaoResource resource =
      new UserRoleDaoResource( roleBindingDao, tenantManager, systemRoles, adminRole, mockService );
    try {
      resource.createUser( new User( "not", "admin" ) );
    } catch ( WebApplicationException e ) {
      assertEquals( Response.Status.FORBIDDEN.getStatusCode(), e.getResponse().getStatus() );
    }
  }

  @Test
  public void testCreateUserValidationFailed() throws Exception {
    UserRoleDaoService mockService = mock( UserRoleDaoService.class );
    doThrow( new UserRoleDaoService.ValidationFailedException() ).when( mockService ).createUser( any( User.class ) );
    UserRoleDaoResource resource =
      new UserRoleDaoResource( roleBindingDao, tenantManager, systemRoles, adminRole, mockService );
    try {
      resource.createUser( new User( "\\/validation", "failed" ) );
    } catch ( WebApplicationException e ) {
      assertEquals( Response.Status.BAD_REQUEST.getStatusCode(), e.getResponse().getStatus() );
    }
  }

  @Test
  public void testCreateUserDuplicate() throws Exception {
    UserRoleDaoService mockService = mock( UserRoleDaoService.class );
    doThrow( new AlreadyExistsException( "message" ) ).when( mockService ).createUser( any( User.class ) );
    UserRoleDaoResource resource =
      new UserRoleDaoResource( roleBindingDao, tenantManager, systemRoles, adminRole, mockService );
    try {
      resource.createUser( new User( "user", "duplicate" ) );
    } catch ( WebApplicationException e ) {
      assertEquals( Response.Status.PRECONDITION_FAILED.getStatusCode(), e.getResponse().getStatus() );
    }
  }

  private void changePassException( Exception ex, int expectedStatus, String name, String newPass, String oldPass )
    throws Exception {
    UserRoleDaoService mockService = mock( UserRoleDaoService.class );
    doThrow( ex ).when( mockService ).changeUserPassword( anyString(), anyString(), anyString() );
    UserRoleDaoResource resource =
      new UserRoleDaoResource( roleBindingDao, tenantManager, systemRoles, adminRole, mockService );
    try {
      resource.changeUserPassword( new ChangePasswordUser( name, newPass, oldPass ) );
    } catch ( WebApplicationException exception ) {
      assertEquals( expectedStatus, exception.getResponse().getStatus() );
    }
  }

  @Test
  public void testChangePasswordSuccess() throws Exception {
    Response response = userRoleResource.changeUserPassword( new ChangePasswordUser( "name", "newPass", "oldPass" ) );
    assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
  }

  @Test
  public void testChangePasswordWrongName() throws Exception {
    changePassException( new SecurityException(), Response.Status.FORBIDDEN.getStatusCode(), "wrong_name",
      "newPass", "oldPass" );
  }

  @Test
  public void testChangePasswordWrongPass() throws Exception {
    changePassException( new SecurityException(), Response.Status.FORBIDDEN.getStatusCode(), "name",
      "wrong_newPass", "oldPass" );
  }

  @Test
  public void testChangePasswordInvalidInput() throws Exception {
    changePassException( new UserRoleDaoService.ValidationFailedException(), Response.Status.BAD_REQUEST
      .getStatusCode(), null, null, "oldPass" );
  }

  @Test
  public void testChangePasswordInternalError() throws Exception {
    changePassException( new Exception(), Response.Status.PRECONDITION_FAILED
      .getStatusCode(), null, null, "oldPass" );
  }

  @Test
  public void testCreateRole() throws Exception {
    Response response = userRoleResource.createRole( "newRole", null );
    assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
  }

  @Test
  public void testCreateRoleFormParam() throws Exception {
    Response response = userRoleResource.createRole( null, "newRole" );
    assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
  }

  @Test
  public void testCreateRoleSecurityException() throws Exception {
    UserRoleDaoService mockService = mock( UserRoleDaoService.class );
    doThrow( new SecurityException() ).when( mockService ).createRole( anyString() );
    UserRoleDaoResource resource =
      new UserRoleDaoResource( roleBindingDao, tenantManager, systemRoles, adminRole, mockService );
    try {
      resource.createRole( "anyRoleName", null );
    } catch ( WebApplicationException e ) {
      assertEquals( Response.Status.FORBIDDEN.getStatusCode(), e.getResponse().getStatus() );
    }
  }

  @Test
  public void testCreateRoleEmptyName() throws Exception {
    UserRoleDaoService mockService = mock( UserRoleDaoService.class );
    doThrow( new UserRoleDaoService.ValidationFailedException() ).when( mockService ).createRole( anyString() );
    UserRoleDaoResource resource =
      new UserRoleDaoResource( roleBindingDao, tenantManager, systemRoles, adminRole, mockService );
    try {
      resource.createRole( "", null );
    } catch ( WebApplicationException e ) {
      assertEquals( Response.Status.BAD_REQUEST.getStatusCode(), e.getResponse().getStatus() );
    }
  }

  @Test
  public void testUpdatePassword() throws Exception {
    Response response = userRoleResource.updatePassword( new User( "name", "newPassword" ) );
    assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
  }

  @Test
  public void testUpdatePasswordNotAdmin() throws Exception {
    UserRoleDaoService mockService = mock( UserRoleDaoService.class );
    doThrow( new SecurityException() ).when( mockService ).updatePassword( any( User.class ) );
    UserRoleDaoResource resource =
      new UserRoleDaoResource( roleBindingDao, tenantManager, systemRoles, adminRole, mockService );
    try {
      resource.updatePassword( new User( "name", "newPassword" ) );
    } catch ( WebApplicationException e ) {
      assertEquals( Response.Status.FORBIDDEN.getStatusCode(), e.getResponse().getStatus() );
    }
  }

}
