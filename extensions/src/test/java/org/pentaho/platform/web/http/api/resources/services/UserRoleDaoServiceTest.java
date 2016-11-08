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
 * Copyright (c) 2002-2015 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.web.http.api.resources.services;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ISecurityHelper;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoRole;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoUser;
import org.pentaho.platform.api.engine.security.userroledao.IUserRoleDao;
import org.pentaho.platform.api.engine.security.userroledao.NotFoundException;
import org.pentaho.platform.api.engine.security.userroledao.UncategorizedUserRoleDaoException;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.security.policy.rolebased.IRoleAuthorizationPolicyRoleBindingDao;
import org.pentaho.platform.security.policy.rolebased.RoleBindingStruct;
import org.pentaho.platform.web.http.api.resources.LogicalRoleAssignment;
import org.pentaho.platform.web.http.api.resources.LogicalRoleAssignments;
import org.pentaho.platform.web.http.api.resources.RoleListWrapper;
import org.pentaho.platform.web.http.api.resources.SystemRolesMap;
import org.pentaho.platform.web.http.api.resources.User;
import org.pentaho.platform.web.http.api.resources.UserListWrapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.*;

public class UserRoleDaoServiceTest {
  private UserRoleDaoService userRoleService;

  @Before
  public void setUp() throws Exception {
    PentahoSystem.init();
    userRoleService = new UserRoleDaoService();
  }

  @After
  public void tearDown() throws Exception {
    PentahoSystem.clearObjectFactory();
    PentahoSystem.shutdown();
  }

  @Test
  public void testGetUsers() throws Exception {
    List<IPentahoUser> userList = new ArrayList<IPentahoUser>();
    IUserRoleDao roleDao = mock( IUserRoleDao.class );
    when( roleDao.getUsers() ).thenReturn( userList );
    PentahoSystem.registerObject( roleDao );

    UserListWrapper wrapUserList = new UserListWrapper( userList );

    assertEquals( wrapUserList.getUsers(), userRoleService.getUsers().getUsers() );
  }

  @Test
  public void testGetRolesForUser() throws Exception {
    List<IPentahoRole> roleList = new ArrayList<IPentahoRole>();
    IUserRoleDao roleDao = mock( IUserRoleDao.class );
    when( roleDao.getUserRoles( any( ITenant.class), anyString()) ).thenReturn( roleList );
    PentahoSystem.registerObject( roleDao );

    RoleListWrapper wrapRoleList = new RoleListWrapper( roleList );

    IPentahoSession session = mock( IPentahoSession.class );
    String tenantPath = "testPath";
    when(session.getAttribute( IPentahoSession.TENANT_ID_KEY )).thenReturn( tenantPath );
    PentahoSessionHolder.setSession( session );

    assertEquals( wrapRoleList.getRoles(), userRoleService.getRolesForUser( "admin" ).getRoles() );
  }

  @Test
  public void testAssignRoleToUser() throws UserRoleListService.UnauthorizedException {
    String userName = "testUser";
    String roleNames = "Power User\tBusiness User\t";

    //Used by the canAdminister call
    IAuthorizationPolicy policy = mock( IAuthorizationPolicy.class );
    when( policy.isAllowed( anyString() ) ).thenReturn( true );
    PentahoSystem.registerObject( policy );

    //Create session that will generate tenant
    IPentahoSession session = mock( IPentahoSession.class );
    when( session.getAttribute( IPentahoSession.TENANT_ID_KEY ) ).thenReturn( "testTenantPath" );
    PentahoSessionHolder.setSession( session );

    IPentahoRole ceoRole = mock( IPentahoRole.class );
    when( ceoRole.getName() ).thenReturn( "ceo" );
    IPentahoRole ctoRole = mock( IPentahoRole.class );
    when( ctoRole.getName() ).thenReturn( "cto" );
    List<IPentahoRole> roleList = new ArrayList<>();
    roleList.add( ceoRole );
    roleList.add( ctoRole );
    IUserRoleDao roleDao = mock( IUserRoleDao.class );
    when( roleDao.getUserRoles( any( ITenant.class ), anyString() ) ).thenReturn( roleList );
    PentahoSystem.registerObject( roleDao );

    userRoleService.assignRolesToUser( userName, roleNames );
    verify( roleDao ).setUserRoles( any( ITenant.class ), anyString(),
      argThat( new UnorderedArrayMatcher( new String[] { "ceo", "cto", "Power User", "Business User" } ) ) );
  }

  @Test( expected = SecurityException.class )
  public void testAssignRoleToUserUnauthorizedException() throws UserRoleListService.UnauthorizedException {
    String userName = "testUser";
    String roleNames = "Power User\tBusiness User\t";

    //canAdminister should return false
    IAuthorizationPolicy policy = mock( IAuthorizationPolicy.class );
    when( policy.isAllowed( anyString() ) ).thenReturn( false );
    PentahoSystem.registerObject( policy );

    userRoleService.assignRolesToUser( userName, roleNames );
  }

  @Test( expected = NotFoundException.class )
  public void testAssignRoleToUserNotFoundException() throws UserRoleListService.UnauthorizedException {
    String userName = "testUser";
    String roleNames = "Power User\tBusiness User\t";

    //Used by the canAdminister call
    IAuthorizationPolicy policy = mock( IAuthorizationPolicy.class );
    when( policy.isAllowed( anyString() ) ).thenReturn( true );
    PentahoSystem.registerObject( policy );

    //Create session that will generate tenant
    IPentahoSession session = mock( IPentahoSession.class );
    when( session.getAttribute( IPentahoSession.TENANT_ID_KEY ) ).thenReturn( "testTenantPath" );
    PentahoSessionHolder.setSession( session );

    IUserRoleDao roleDao = mock( IUserRoleDao.class );
    when( roleDao.getUserRoles( any( ITenant.class ), anyString() ) ).thenThrow(
      new NotFoundException( "expectedTestException" ) );
    PentahoSystem.registerObject( roleDao );

    userRoleService.assignRolesToUser( userName, roleNames );
  }

  @Test( expected = UncategorizedUserRoleDaoException.class )
  public void testAssignRoleToUserUncategorizedUserRoleDaoException() throws UserRoleListService.UnauthorizedException {
    String userName = "testUser";
    String roleNames = "Power User\tBusiness User\t";

    //Used by the canAdminister call
    IAuthorizationPolicy policy = mock( IAuthorizationPolicy.class );
    when( policy.isAllowed( anyString() ) ).thenReturn( true );
    PentahoSystem.registerObject( policy );

    //Create session that will generate tenant
    IPentahoSession session = mock( IPentahoSession.class );
    when( session.getAttribute( IPentahoSession.TENANT_ID_KEY ) ).thenReturn( "testTenantPath" );
    PentahoSessionHolder.setSession( session );

    IUserRoleDao roleDao = mock( IUserRoleDao.class );
    when( roleDao.getUserRoles( any( ITenant.class ), anyString() ) ).thenThrow(
      new UncategorizedUserRoleDaoException( "expectedTestException" ) );
    PentahoSystem.registerObject( roleDao );

    userRoleService.assignRolesToUser( userName, roleNames );
  }

  @Test
  public void testRemoveRoleFromUser() {
    String userName = "testUser";
    String roleNames = "Power User\tBusiness User\t";

    //Used by the canAdminister call
    IAuthorizationPolicy policy = mock( IAuthorizationPolicy.class );
    when( policy.isAllowed( anyString() ) ).thenReturn( true );
    PentahoSystem.registerObject( policy );

    //Create session that will generate tenant
    IPentahoSession session = mock( IPentahoSession.class );
    when( session.getAttribute( IPentahoSession.TENANT_ID_KEY ) ).thenReturn( "testTenantPath" );
    PentahoSessionHolder.setSession( session );

    IPentahoRole ceoRole = mock( IPentahoRole.class );
    when( ceoRole.getName() ).thenReturn( "ceo" );
    IPentahoRole ctoRole = mock( IPentahoRole.class );
    when( ctoRole.getName() ).thenReturn( "cto" );
    IPentahoRole powerUserRole = mock( IPentahoRole.class );
    when( powerUserRole.getName() ).thenReturn( "Power User" );
    IPentahoRole businessUserRole = mock( IPentahoRole.class );
    when( businessUserRole.getName() ).thenReturn( "Business User" );
    List<IPentahoRole> roleList = new ArrayList<>();
    roleList.add( ceoRole );
    roleList.add( ctoRole );
    roleList.add( powerUserRole );
    roleList.add( businessUserRole );
    IUserRoleDao roleDao = mock( IUserRoleDao.class );
    when( roleDao.getUserRoles( any( ITenant.class ), anyString() ) ).thenReturn( roleList );
    PentahoSystem.registerObject( roleDao );

    userRoleService.removeRolesFromUser( userName, roleNames );
    verify( roleDao ).setUserRoles( any( ITenant.class ), anyString(),
      argThat( new UnorderedArrayMatcher( new String[] { "ceo", "cto" } ) ) );
  }

  @Test( expected = SecurityException.class )
  public void testRemoveRoleFromUserSecurityException() {
    String userName = "testUser";
    String roleNames = "Power User\tBusiness User\t";

    //canAdminister should return false
    IAuthorizationPolicy policy = mock( IAuthorizationPolicy.class );
    when( policy.isAllowed( anyString() ) ).thenReturn( false );
    PentahoSystem.registerObject( policy );

    userRoleService.removeRolesFromUser( userName, roleNames );
  }

  @Test( expected = NotFoundException.class )
  public void testRemoveRoleFromUserNotFoundException() {
    String userName = "testUser";
    String roleNames = "Power User\tBusiness User\t";

    //Used by the canAdminister call
    IAuthorizationPolicy policy = mock( IAuthorizationPolicy.class );
    when( policy.isAllowed( anyString() ) ).thenReturn( true );
    PentahoSystem.registerObject( policy );

    //Create session that will generate tenant
    IPentahoSession session = mock( IPentahoSession.class );
    when( session.getAttribute( IPentahoSession.TENANT_ID_KEY ) ).thenReturn( "testTenantPath" );
    PentahoSessionHolder.setSession( session );

    IUserRoleDao roleDao = mock( IUserRoleDao.class );
    when( roleDao.getUserRoles( any( ITenant.class ), anyString() ) ).thenThrow(
      new NotFoundException( "expectedTestException" ) );
    PentahoSystem.registerObject( roleDao );

    userRoleService.removeRolesFromUser( userName, roleNames );
  }

  @Test( expected = UncategorizedUserRoleDaoException.class )
  public void testRemoveRolesFromUserUncategorizedUserRoleDaoException()
    throws UserRoleListService.UnauthorizedException {
    String userName = "testUser";
    String roleNames = "Power User\tBusiness User\t";

    //Used by the canAdminister call
    IAuthorizationPolicy policy = mock( IAuthorizationPolicy.class );
    when( policy.isAllowed( anyString() ) ).thenReturn( true );
    PentahoSystem.registerObject( policy );

    //Create session that will generate tenant
    IPentahoSession session = mock( IPentahoSession.class );
    when( session.getAttribute( IPentahoSession.TENANT_ID_KEY ) ).thenReturn( "testTenantPath" );
    PentahoSessionHolder.setSession( session );

    IUserRoleDao roleDao = mock( IUserRoleDao.class );
    when( roleDao.getUserRoles( any( ITenant.class ), anyString() ) ).thenThrow(
      new UncategorizedUserRoleDaoException( "expectedTestException" ) );
    PentahoSystem.registerObject( roleDao );

    userRoleService.removeRolesFromUser( userName, roleNames );
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

    userRoleService.deleteUsers( users );
    verify( roleDao, times( 2 ) ).deleteUser( any( IPentahoUser.class ) );
  }

  @Test(expected = SecurityException.class)
  public void testDeleteUsersSecurityException() {
    String users = "admin\tsuzy\t";

    IAuthorizationPolicy policy = mock( IAuthorizationPolicy.class );
    when( policy.isAllowed( anyString() ) ).thenReturn( false );
    PentahoSystem.registerObject( policy );

    userRoleService.deleteUsers( users );
  }

  @Test( expected = UncategorizedUserRoleDaoException.class )
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

    userRoleService.deleteUsers( users );
  }

  @Test
  public void testGetRoles() throws Exception {
    List<IPentahoRole> roleList = new ArrayList<IPentahoRole>();
    IUserRoleDao roleDao = mock( IUserRoleDao.class );
    when( roleDao.getRoles() ).thenReturn( roleList );
    PentahoSystem.registerObject( roleDao );

    IPentahoRole role = mock(IPentahoRole.class);
    when(role.getName()).thenReturn( "testRole" );
    roleList.add( role );
    RoleListWrapper wrapRoleList = new RoleListWrapper( roleList );

    assertEquals( wrapRoleList.getRoles(), userRoleService.getRoles().getRoles() );
  }

  @Test
  public void testGetRoleMembers() {
    IAuthorizationPolicy policy = mock( IAuthorizationPolicy.class );
    when( policy.isAllowed( anyString() ) ).thenReturn( true );
    PentahoSystem.registerObject( policy );

    List<IPentahoUser> userList = new ArrayList<IPentahoUser>();
    IUserRoleDao roleDao = mock( IUserRoleDao.class );
    when( roleDao.getRoleMembers( any( ITenant.class ), anyString() ) ).thenReturn( userList );
    PentahoSystem.registerObject( roleDao );

    IPentahoUser user = mock( IPentahoUser.class );
    when( user.getUsername() ).thenReturn( "admin" );
    userList.add( user );
    UserListWrapper wrapUserList = new UserListWrapper( userList );

    IPentahoSession session = mock( IPentahoSession.class );
    String tenantPath = "testPath";
    when( session.getAttribute( IPentahoSession.TENANT_ID_KEY ) ).thenReturn( tenantPath );
    PentahoSessionHolder.setSession( session );

    assertEquals( wrapUserList.getUsers(), userRoleService.getRoleMembers( "Report Author" ).getUsers() );
  }

  @Test(expected = SecurityException.class)
  public void testGetRoleMembersSecurityException() {
    IAuthorizationPolicy policy = mock( IAuthorizationPolicy.class );
    when( policy.isAllowed( anyString() ) ).thenReturn( false );
    PentahoSystem.registerObject( policy );

    List<IPentahoUser> userList = new ArrayList<IPentahoUser>();
    IUserRoleDao roleDao = mock( IUserRoleDao.class );
    when( roleDao.getRoleMembers( any( ITenant.class ), anyString() ) ).thenReturn( userList );
    PentahoSystem.registerObject( roleDao );

    IPentahoUser user = mock( IPentahoUser.class );
    when( user.getUsername() ).thenReturn( "admin" );
    userList.add( user );
    UserListWrapper wrapUserList = new UserListWrapper( userList );

    IPentahoSession session = mock( IPentahoSession.class );
    String tenantPath = "testPath";
    when( session.getAttribute( IPentahoSession.TENANT_ID_KEY ) ).thenReturn( tenantPath );
    PentahoSessionHolder.setSession( session );

    userRoleService.getRoleMembers( "Report Author" );
  }

  @Test
  public void testDeleteRole() {
    String roles = "role1\trole2\t";

    IAuthorizationPolicy policy = mock( IAuthorizationPolicy.class );
    when( policy.isAllowed( anyString() ) ).thenReturn( true );
    PentahoSystem.registerObject( policy );

    IPentahoRole role = mock( IPentahoRole.class );
    IUserRoleDao roleDao = mock( IUserRoleDao.class );
    when( roleDao.getRole( any( ITenant.class ), anyString() ) ).thenReturn( role );
    PentahoSystem.registerObject( roleDao );

    userRoleService.deleteRoles( roles );
    verify( roleDao, times( 2 ) ).deleteRole( any( IPentahoRole.class ) );
  }

  @Test(expected = SecurityException.class)
  public void testDeleteRolesSecurityException() {
    String roles = "role1\trole2\t";

    IAuthorizationPolicy policy = mock( IAuthorizationPolicy.class );
    when( policy.isAllowed( anyString() ) ).thenReturn( false );
    PentahoSystem.registerObject( policy );

    userRoleService.deleteUsers( roles );
  }

  @Test( expected = UncategorizedUserRoleDaoException.class )
  public void testDeleteRoleUncategorizedUserRoleDaoException() {
    String roles = "role1\trole2\t";

    IAuthorizationPolicy policy = mock( IAuthorizationPolicy.class );
    when( policy.isAllowed( anyString() ) ).thenReturn( true );
    PentahoSystem.registerObject( policy );

    IUserRoleDao roleDao = mock( IUserRoleDao.class );
    when( roleDao.getRole( any( ITenant.class ), anyString() ) ).thenThrow(
      new UncategorizedUserRoleDaoException( "expectedTestException" ) );
    PentahoSystem.registerObject( roleDao );

    userRoleService.deleteRoles( roles );
  }

  @Test
  public void testSetLogicalRoles() {
    String roleName = "testRole";
    ArrayList<String> roleList = new ArrayList<>();
    roleList.add( "org.pentaho.repository.read" );
    roleList.add( "org.pentaho.repository.create" );
    LogicalRoleAssignment roleAssignment = mock( LogicalRoleAssignment.class );
    when( roleAssignment.getRoleName() ).thenReturn( roleName );
    when( roleAssignment.getLogicalRoles() ).thenReturn( roleList );
    ArrayList<LogicalRoleAssignment> roles = new ArrayList<LogicalRoleAssignment>();
    roles.add( roleAssignment );

    LogicalRoleAssignments roleAssignments = mock( LogicalRoleAssignments.class );
    when( roleAssignments.getAssignments() ).thenReturn( roles );

    IAuthorizationPolicy policy = mock( IAuthorizationPolicy.class );
    when( policy.isAllowed( anyString() ) ).thenReturn( true );
    PentahoSystem.registerObject( policy );

    IRoleAuthorizationPolicyRoleBindingDao roleBindingDao = mock( IRoleAuthorizationPolicyRoleBindingDao.class );
    PentahoSystem.registerObject( roleBindingDao );

    userRoleService.setLogicalRoles( roleAssignments );
    verify( roleBindingDao ).setRoleBindings( roleName, roleList );
  }

  @Test( expected = SecurityException.class )
  public void testSetLogicalRolesSecurityException() {
    LogicalRoleAssignments roleAssignments = mock( LogicalRoleAssignments.class );

    IAuthorizationPolicy policy = mock( IAuthorizationPolicy.class );
    when( policy.isAllowed( anyString() ) ).thenReturn( false );
    PentahoSystem.registerObject( policy );

    userRoleService.setLogicalRoles( roleAssignments );
  }

  @Test
  public void testGetRoleBindingStruct() {
    IAuthorizationPolicy policy = mock( IAuthorizationPolicy.class );
    when( policy.isAllowed( anyString() ) ).thenReturn( true );
    PentahoSystem.registerObject( policy );

    Map<String, String> localizedNameEntry = new HashMap<>();
    localizedNameEntry.put( "testEntry", "testValue" );

    Map<String, List<String>> testBindingMap = new HashMap<>();
    testBindingMap.put( "testBinding", new ArrayList<String>() );

    RoleBindingStruct roleBindingStruct = mock( RoleBindingStruct.class );
    roleBindingStruct.logicalRoleNameMap = localizedNameEntry;
    roleBindingStruct.bindingMap = testBindingMap;
    roleBindingStruct.immutableRoles = new HashSet<String>();

    IRoleAuthorizationPolicyRoleBindingDao roleBindingDao = mock( IRoleAuthorizationPolicyRoleBindingDao.class );
    when( roleBindingDao.getRoleBindingStruct( anyString() ) ).thenReturn( roleBindingStruct );
    PentahoSystem.registerObject( roleBindingDao );

    SystemRolesMap validateMap = userRoleService.getRoleBindingStruct( "en" );

    assert ( 1 == validateMap.getLocalizedRoleNames().size() );
    assertEquals( "testEntry", validateMap.getLocalizedRoleNames().get( 0 ).getRoleName() );
    assertEquals( "testValue", validateMap.getLocalizedRoleNames().get( 0 ).getLocalizedName() );
  }

  @Test(expected = SecurityException.class)
  public void testGetRoleBindingStructSecurityException() {
    IAuthorizationPolicy policy = mock( IAuthorizationPolicy.class );
    when( policy.isAllowed( anyString() ) ).thenReturn( false );
    PentahoSystem.registerObject( policy );

    userRoleService.getRoleBindingStruct( "en" );
  }

  private class UnorderedArrayMatcher extends ArgumentMatcher<String[]> {
    private Set<String> correctValues;

    UnorderedArrayMatcher( String[] expected ) {
      correctValues = new HashSet<>( Arrays.asList( expected ) );
    }

    @Override public boolean matches( Object argument ) {
      String[] arguments = (String[]) argument;
      Set < String > returnedValues = new HashSet<>( Arrays.asList( arguments ) );
      return returnedValues.equals( correctValues );
    }
  }
  
  private boolean validationFailed( UserRoleDaoService service, User user ) {
    try {
      service.createUser( user );
    } catch ( UserRoleDaoService.ValidationFailedException e ) {
      return true;
    } catch ( Exception e ) {
      return false;
    }
    return false;
  }

  @Test
  public void testCreateUserValidation() {
    List<String> reservedChars = Arrays.asList( "/,\\,\t,\r,\n".split( "," ) );
    IUnifiedRepository repo = mock( IUnifiedRepository.class );
    doReturn( reservedChars ).when( repo ).getReservedChars();
    PentahoSystem.registerObject( repo );
    UserRoleDaoService service = new UserRoleDaoService();

    // Used by the canAdminister call
    IAuthorizationPolicy policy = mock( IAuthorizationPolicy.class );
    when( policy.isAllowed( anyString() ) ).thenReturn( true );
    PentahoSystem.registerObject( policy );
    
    Assert.assertTrue( validationFailed( service, new User( "\\", "pass" ) ) );
    Assert.assertTrue( validationFailed( service, new User( "/www", "pass" ) ) );
    Assert.assertTrue( validationFailed( service, new User( "\tqwer", "pass" ) ) );
    Assert.assertTrue( validationFailed( service, new User( "\rqwer", "pass" ) ) );
    Assert.assertTrue( validationFailed( service, new User( "qwer\n", "pass" ) ) );
    Assert.assertTrue( validationFailed( service, new User( "normalName", "" ) ) );
    Assert.assertTrue( validationFailed( service, new User( "", "pass" ) ) );
    Assert.assertTrue( validationFailed( service, new User( null, "pass" ) ) );
    Assert.assertTrue( validationFailed( service, new User( "name", null ) ) );
    Assert.assertFalse( validationFailed( service, new User( "normalName", "pass" ) ) );
  }

  private boolean isValidationFailedThrew( String name, String pass, String oldPass ) throws Exception {
    try {
      userRoleService.changeUserPassword( name, pass, oldPass );
    } catch ( UserRoleDaoService.ValidationFailedException ex ) {
      return true;
    }
    return false;
  }

  @Test
  public void testChangePassInvalidInput() throws Exception {
    Assert.assertTrue( isValidationFailedThrew( null, "newpass", "oldpass" ) );
    Assert.assertTrue( isValidationFailedThrew( null, "newpass", "oldpass" ) );
    Assert.assertTrue( isValidationFailedThrew( "", "newpass", "oldpass" ) );
    Assert.assertTrue( isValidationFailedThrew( "name", null, "oldpass" ) );
    Assert.assertTrue( isValidationFailedThrew( "name", "", "oldpass" ) );
    Assert.assertTrue( isValidationFailedThrew( "name", "newpass", null ) );
    Assert.assertTrue( isValidationFailedThrew( "name", "newpass", "" ) );
  }

  @Test
  public void testChangePassSuccess() throws Exception {
    IUserRoleDao roleDao = mock( IUserRoleDao.class );
    IPentahoUser pentahoUser = mock( IPentahoUser.class );
    doReturn( "old pass" ).when( pentahoUser ).getPassword();
    doReturn( pentahoUser ).when( roleDao ).getUser( any( ITenant.class ), anyString() );
    PentahoSystem.registerObject( roleDao );

    SecurityHelper.setMockInstance( mock( ISecurityHelper.class ) );
    userRoleService.changeUserPassword( "name", "newpass", "old pass" );
  }

  @Test( expected = SecurityException.class )
  public void testChangePassWrongName() throws Exception {
    IUserRoleDao roleDao = mock( IUserRoleDao.class );
    doReturn( null ).when( roleDao ).getUser( any( ITenant.class ), anyString() );
    PentahoSystem.registerObject( roleDao );
    userRoleService.changeUserPassword( "name", "newpass", "old pass" );
  }

  @Test( expected = SecurityException.class )
  public void testChangePassWrongPass() throws Exception {
    IUserRoleDao roleDao = mock( IUserRoleDao.class );
    IPentahoUser pentahoUser = mock( IPentahoUser.class );
    doReturn( "wrong old pass" ).when( pentahoUser ).getPassword();
    doReturn( pentahoUser ).when( roleDao ).getUser( any( ITenant.class ), anyString() );
    PentahoSystem.registerObject( roleDao );
    userRoleService.changeUserPassword( "name", "newpass", "old pass" );
  }

  @Test( expected = UserRoleDaoService.ValidationFailedException.class )
  public void testCreateRoleEmptyName() throws Exception {
    // Used by the canAdminister call
    IAuthorizationPolicy policy = mock( IAuthorizationPolicy.class );
    when( policy.isAllowed( anyString() ) ).thenReturn( true );
    PentahoSystem.registerObject( policy );

    userRoleService.createRole( "" );
  }

  @Test( expected = UserRoleDaoService.ValidationFailedException.class )
  public void testCreateRoleNullName() throws Exception {
    // Used by the canAdminister call
    IAuthorizationPolicy policy = mock( IAuthorizationPolicy.class );
    when( policy.isAllowed( anyString() ) ).thenReturn( true );
    PentahoSystem.registerObject( policy );

    userRoleService.createRole( null );
  }

  @Test
  public void testUpdatePassword() throws Exception {
    // Used by the canAdminister call
    IAuthorizationPolicy policy = mock( IAuthorizationPolicy.class );
    when( policy.isAllowed( anyString() ) ).thenReturn( true );
    PentahoSystem.registerObject( policy );
    
    IUserRoleDao roleDao = mock( IUserRoleDao.class );
    PentahoSystem.registerObject( roleDao );
    userRoleService.updatePassword( new User( "name", "password" ) );
  }

  @Test( expected = SecurityException.class )
  public void testUpdatePasswordNotAdmin() throws Exception {
    // Used by the canAdminister call
    IAuthorizationPolicy policy = mock( IAuthorizationPolicy.class );
    when( policy.isAllowed( anyString() ) ).thenReturn( false );
    PentahoSystem.registerObject( policy );

    userRoleService.updatePassword( new User( "name", "password" ) );
  }

}