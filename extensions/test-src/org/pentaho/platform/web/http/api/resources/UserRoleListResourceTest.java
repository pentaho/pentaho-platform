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

package org.pentaho.platform.web.http.api.resources;

import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.security.userroledao.AlreadyExistsException;
import org.pentaho.platform.api.engine.security.userroledao.NotFoundException;
import org.pentaho.platform.api.engine.security.userroledao.UncategorizedUserRoleDaoException;
import org.pentaho.platform.web.http.api.resources.services.UserRoleListService;
import org.pentaho.platform.web.http.api.resources.services.UserRoleListService.ValidationFailedException;


public class UserRoleListResourceTest {

  UserRoleListResource userRoleListResource;
  UserRoleListResource notASpyResource;

  @Before
  public void setup() {
    userRoleListResource = spy( new UserRoleListResource() );
    notASpyResource = new UserRoleListResource();
    userRoleListResource.userRoleListService = mock( UserRoleListService.class );
  }

  @After
  public void teardown() {
    userRoleListResource = null;
  }

  @Test
  public void testGetRolesForUser() throws Exception {
    String user = "user";

    String roles = "roles";
    doReturn( roles ).when( userRoleListResource.userRoleListService ).doGetRolesForUser( user );

    Response mockResponse = mock( Response.class );
    doReturn( mockResponse ).when( userRoleListResource ).buildOkResponse( roles, MediaType.APPLICATION_XML );

    Response testResponse = userRoleListResource.getRolesForUser( user );
    assertEquals( mockResponse, testResponse );

    verify( userRoleListResource.userRoleListService, times( 1 ) ).doGetRolesForUser( user );
    verify( userRoleListResource, times( 1 ) ).buildOkResponse( roles, MediaType.APPLICATION_XML );
  }

  @Test
  public void testGetRolesForUserError() throws Exception {
    String user = "user";

    Response mockResponse = mock( Response.class );
    doReturn( mockResponse ).when( userRoleListResource ).buildStatusResponse( Response.Status.UNAUTHORIZED );


    // Test 1
    UserRoleListService.UnauthorizedException mockUnauthorizedException = mock(
        UserRoleListService.UnauthorizedException.class );
    doThrow( mockUnauthorizedException ).when( userRoleListResource.userRoleListService ).doGetRolesForUser( user );

    Response testResponse = userRoleListResource.getRolesForUser( user );
    assertEquals( mockResponse, testResponse );

    // Test 2
    Throwable mockThrowable = mock( RuntimeException.class );
    doThrow( mockThrowable ).when( userRoleListResource.userRoleListService ).doGetRolesForUser( user );

    try {
      userRoleListResource.getRolesForUser( user );
      fail();
    } catch ( WebApplicationException e ) {
      // expected
    }

    verify( userRoleListResource, times( 1 ) ).buildStatusResponse( Response.Status.UNAUTHORIZED );
    verify( userRoleListResource.userRoleListService, times( 2 ) ).doGetRolesForUser( user );
  }

  @Test
  public void testGetUsers() throws Exception {
    UserListWrapper mockWrapper = mock( UserListWrapper.class );
    doReturn( mockWrapper ).when( userRoleListResource.userRoleListService ).getUsers();

    UserListWrapper testWrapper = userRoleListResource.getUsers();
    assertEquals( mockWrapper, testWrapper );

    verify( userRoleListResource.userRoleListService, times( 1 ) ).getUsers();
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
  public void testCreateUser() throws Exception {
    notASpyResource.userRoleListService = mock( UserRoleListService.class );
    Response response = notASpyResource.createUser( new User( "name", "password" ) );
    assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
  }

  @Test
  public void testCreateUserUnauthorizedException() throws Exception {
    UserRoleListService mockService = mock( UserRoleListService.class );
    doThrow( new UnauthorizedException() ).when( mockService ).createUser( any( User.class ) );
    notASpyResource.userRoleListService = mockService;
    try {
      notASpyResource.createUser( new User( "not", "admin" ) );
    } catch ( WebApplicationException e ) {
      assertEquals( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), e.getResponse().getStatus() );
      return;
    }
    Assert.fail();
  }

  @Test
  public void testCreateUserValidationFailed() throws Exception {
    UserRoleListService mockService = mock( UserRoleListService.class );
    doThrow( new ValidationFailedException() ).when( mockService ).createUser( any( User.class ) );
    notASpyResource.userRoleListService = mockService;
    try {
      notASpyResource.createUser( new User( "\\/validation", "failed" ) );
    } catch ( WebApplicationException e ) {
      assertEquals( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), e.getResponse().getStatus() );
      return;
    }
    Assert.fail();
  }

  @Test
  public void testCreateUserDuplicate() throws Exception {
    UserRoleListService mockService = mock( UserRoleListService.class );
    doThrow( new AlreadyExistsException( "message" ) ).when( mockService ).createUser( any( User.class ) );
    notASpyResource.userRoleListService = mockService;
    try {
      notASpyResource.createUser( new User( "user", "duplicate" ) );
    } catch ( WebApplicationException e ) {
      assertEquals( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), e.getResponse().getStatus() );
      return;
    }
    Assert.fail();
  }

  @Test
  public void testDeleteUser() {
    String users = "user1\tuser2\tuser3\t";
    UserRoleListService mockService = mock( UserRoleListService.class );
    notASpyResource.userRoleListService = mockService;

    Response response = notASpyResource.deleteUser( users );
    assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
  }

  @Test
  public void testDeleteUserUnauthorizedException() throws UserRoleListService.UnauthorizedException {
    String users = "user1\tuser2\tuser3\t";
    UserRoleListService mockService = mock( UserRoleListService.class );
    notASpyResource.userRoleListService = mockService;

    doThrow( new UserRoleListService.UnauthorizedException() ).when( mockService ).deleteUsers( anyString() );

    try {
      notASpyResource.deleteUser( users );
    } catch ( WebApplicationException e ) {
      assertEquals( Response.Status.FORBIDDEN.getStatusCode(), e.getResponse().getStatus() );
    }
  }

  @Test
  public void testDeleteUserUncategorizedUserRoleDaoException() throws UserRoleListService.UnauthorizedException {
    String users = "user1\tuser2\tuser3\t";
    UserRoleListService mockService = mock( UserRoleListService.class );
    notASpyResource.userRoleListService = mockService;

    doThrow( new UncategorizedUserRoleDaoException( "expectedTestException" ) ).when( mockService )
      .deleteUsers( anyString() );

    try {
      notASpyResource.deleteUser( users );
    } catch ( WebApplicationException e ) {
      assertEquals( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), e.getResponse().getStatus() );
    }
  }

  @Test
  public void testAssignRolesToUser() {
    String user = "testUser1";
    String roles = "testRole1";
    UserRoleListService mockService = mock( UserRoleListService.class );
    notASpyResource.userRoleListService = mockService;

    Response response = notASpyResource.assignRolesToUser( user, roles );
    assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
  }

  @Test
  public void testAssignRolesToUserUnauthorizedException() throws UserRoleListService.UnauthorizedException {
    String user = "testUser1";
    String roles = "testRole1";
    UserRoleListService mockService = mock( UserRoleListService.class );
    notASpyResource.userRoleListService = mockService;

    doThrow( new UserRoleListService.UnauthorizedException() ).when( mockService )
      .assignRolesToUser( anyString(), anyString() );

    try {
      notASpyResource.assignRolesToUser( user, roles );
    } catch ( WebApplicationException e ) {
      assertEquals( Response.Status.FORBIDDEN.getStatusCode(), e.getResponse().getStatus() );
    }
  }

  @Test
  public void testAssignRolesToUserUncategorizedUserRoleDaoException() throws UserRoleListService.UnauthorizedException {
    String user = "testUser1";
    String roles = "testRole1";
    UserRoleListService mockService = mock( UserRoleListService.class );
    notASpyResource.userRoleListService = mockService;

    doThrow( new UncategorizedUserRoleDaoException( "expectedTestException" ) ).when( mockService )
      .assignRolesToUser( anyString(), anyString() );

    try {
      notASpyResource.assignRolesToUser( user, roles );
    } catch ( WebApplicationException e ) {
      assertEquals( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), e.getResponse().getStatus() );
    }
  }

  @Test
  public void testAssignRolesToUserNotFoundException() throws UserRoleListService.UnauthorizedException {
    String user = "testUser1";
    String roles = "testRole1";
    UserRoleListService mockService = mock( UserRoleListService.class );
    notASpyResource.userRoleListService = mockService;

    doThrow( new NotFoundException( "expectedTestException" ) ).when( mockService )
      .assignRolesToUser( anyString(), anyString() );

    try {
      notASpyResource.assignRolesToUser( user, roles );
    } catch ( WebApplicationException e ) {
      assertEquals( Response.Status.NOT_FOUND.getStatusCode(), e.getResponse().getStatus() );
    }
  }

  @Test
  public void testRemoveRolesFromUser() {
    String user = "testUser1";
    String roles = "testRole1";
    UserRoleListService mockService = mock( UserRoleListService.class );
    notASpyResource.userRoleListService = mockService;

    Response response = notASpyResource.removeRolesFromUser( user, roles );
    assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
  }

  @Test
  public void testRemoveRolesFromUserUnauthorizedException() throws UserRoleListService.UnauthorizedException {
    String user = "testUser1";
    String roles = "testRole1";
    UserRoleListService mockService = mock( UserRoleListService.class );
    notASpyResource.userRoleListService = mockService;

    doThrow( new UserRoleListService.UnauthorizedException() ).when( mockService )
      .removeRolesFromUser( anyString(), anyString() );

    try {
      notASpyResource.removeRolesFromUser( user, roles );
    } catch ( WebApplicationException e ) {
      assertEquals( Response.Status.FORBIDDEN.getStatusCode(), e.getResponse().getStatus() );
    }
  }

  @Test
  public void testRemoveRolesFromUserUncategorizedUserRoleDaoException() throws UserRoleListService.UnauthorizedException {
    String user = "testUser1";
    String roles = "testRole1";
    UserRoleListService mockService = mock( UserRoleListService.class );
    notASpyResource.userRoleListService = mockService;

    doThrow( new UncategorizedUserRoleDaoException( "expectedTestException" ) ).when( mockService )
      .removeRolesFromUser( anyString(), anyString() );

    try {
      notASpyResource.removeRolesFromUser( user, roles );
    } catch ( WebApplicationException e ) {
      assertEquals( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), e.getResponse().getStatus() );
    }
  }

  @Test
  public void testRemoveRolesFromUserNotFoundException() throws UserRoleListService.UnauthorizedException {
    String user = "testUser1";
    String roles = "testRole1";
    UserRoleListService mockService = mock( UserRoleListService.class );
    notASpyResource.userRoleListService = mockService;

    doThrow( new NotFoundException( "expectedTestException" ) ).when( mockService )
      .removeRolesFromUser( anyString(), anyString() );

    try {
      notASpyResource.removeRolesFromUser( user, roles );
    } catch ( WebApplicationException e ) {
      assertEquals( Response.Status.NOT_FOUND.getStatusCode(), e.getResponse().getStatus() );
    }
  }

  @Test
  public void testGetUsersInRole() throws Exception {
    String role = "role";

    String roles = "roles";
    doReturn( roles ).when( userRoleListResource.userRoleListService ).doGetUsersInRole( role );

    Response mockResponse = mock( Response.class );
    doReturn( mockResponse ).when( userRoleListResource ).buildOkResponse( roles, MediaType.APPLICATION_XML );

    Response testResponse = userRoleListResource.getUsersInRole( role );
    assertEquals( mockResponse, testResponse );

    verify( userRoleListResource.userRoleListService, times( 1 ) ).doGetUsersInRole( role );
    verify( userRoleListResource, times( 1 ) ).buildOkResponse( roles, MediaType.APPLICATION_XML );
  }

  @Test
  public void testGetUsersInRoleError() throws Exception {
    String role = "role";

    Response mockResponse = mock( Response.class );
    doReturn( mockResponse ).when( userRoleListResource ).buildStatusResponse( UNAUTHORIZED );

    // Test 1
    UserRoleListService.UnauthorizedException mockUnauthorizedException = mock(
        UserRoleListService.UnauthorizedException.class );
    doThrow( mockUnauthorizedException ).when( userRoleListResource.userRoleListService ).doGetUsersInRole( role );

    Response testResponse = userRoleListResource.getUsersInRole( role );
    assertEquals( mockResponse, testResponse );

    // Test 2
    Throwable mockThrowabe = mock( RuntimeException.class );
    doThrow( mockThrowabe ).when( userRoleListResource.userRoleListService ).doGetUsersInRole( role );

    try {
      userRoleListResource.getUsersInRole( role );
      fail();
    } catch ( WebApplicationException e ) {
      // expected
    }
  }

  @Test
  public void testGetRoles() throws Exception {
    RoleListWrapper mockWrapper = mock( RoleListWrapper.class );
    doReturn( mockWrapper ).when( userRoleListResource.userRoleListService ).getRoles();

    RoleListWrapper testWrapper = userRoleListResource.getRoles();
    assertEquals( mockWrapper, testWrapper );

    verify( userRoleListResource.userRoleListService, times( 1 ) ).getRoles();
  }

  @Test
  public void testGetAllRoles() throws Exception {
    RoleListWrapper mockWrapper = mock( RoleListWrapper.class );
    doReturn( mockWrapper ).when( userRoleListResource.userRoleListService ).getAllRoles();

    RoleListWrapper testWrapper = userRoleListResource.getAllRoles();
    assertEquals( mockWrapper, testWrapper );

    verify( userRoleListResource.userRoleListService, times( 1 ) ).getAllRoles();
  }

  @Test
  public void testGetSystemRoles() throws Exception {
    RoleListWrapper mockWrapper = mock( RoleListWrapper.class );
    doReturn( mockWrapper ).when( userRoleListResource.userRoleListService ).getSystemRoles();

    RoleListWrapper testWrapper = userRoleListResource.getSystemRoles();
    assertEquals( mockWrapper, testWrapper );

    verify( userRoleListResource.userRoleListService, times( 1 ) ).getSystemRoles();
  }

  @Test
  public void testGetExtraRoles() throws Exception {
    RoleListWrapper mockWrapper = mock( RoleListWrapper.class );
    doReturn( mockWrapper ).when( userRoleListResource.userRoleListService ).getExtraRolesList();

    RoleListWrapper testWrapper = userRoleListResource.getExtraRoles();
    assertEquals( mockWrapper, testWrapper );

    verify( userRoleListResource.userRoleListService, times( 1 ) ).getExtraRolesList();
  }

  @Test
  public void testGetPermissionRoles() throws Exception {
    String adminRole = "adminRole";
    userRoleListResource.adminRole = adminRole;

    RoleListWrapper mockWrapper = mock( RoleListWrapper.class );
    doReturn( mockWrapper ).when( userRoleListResource.userRoleListService ).getPermissionRoles( adminRole );

    RoleListWrapper testWrapper = userRoleListResource.getPermissionRoles();
    assertEquals( mockWrapper, testWrapper );

    verify( userRoleListResource.userRoleListService, times( 1 ) ).getPermissionRoles( adminRole );
  }
}
