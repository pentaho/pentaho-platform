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
 * Copyright (c) 2002-2021 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.web.http.api.resources.services;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IConfiguration;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ISecurityHelper;
import org.pentaho.platform.api.engine.ISystemConfig;
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
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UserRoleDaoServiceTest {
  private UserRoleDaoService userRoleService;

  private static final  String SESSION_USER_NAME = "lucyInTheSky";
  private static final String SESSION_USER_PASSWORD = "oldLucyPassword";

  private static final String OTHER_USER_NAME = "motherMary";
  private static final String OTHER_USER_PASSWORD = "oldMotherMaryPassword";

  private static final String A_NEW_PASSWORD = "correct_horse_battery_staple";
  private static final String A_WRONG_PASSWORD = "123456";

  @Before
  public void setUp() throws Exception {
    PentahoSystem.init();

    SecurityHelper.setMockInstance( mock( ISecurityHelper.class ) );
    Properties props = mock( Properties.class );
    when( props.getProperty( UserRoleDaoService.PUC_USER_PASSWORD_LENGTH ) ).thenReturn( "0" );
    when( props.getProperty( UserRoleDaoService.PUC_USER_PASSWORD_REQUIRE_SPECIAL_CHARACTER ) ).thenReturn( "false" );
    IConfiguration config = mock( IConfiguration.class );
    when( config.getProperties() ).thenReturn( props );
    ISystemConfig sysConfig = mock( ISystemConfig.class );
    when( sysConfig.getConfiguration( "security" ) ).thenReturn( config );

    userRoleService = new UserRoleDaoService();
    userRoleService.setSystemConfig( sysConfig );
  }

  @After
  public void tearDown() throws Exception {
    PentahoSessionHolder.removeSession();
    PentahoSystem.clearObjectFactory();
    PentahoSystem.shutdown();
  }

  @Test
  public void testGetUsers() throws Exception {
    List<IPentahoUser> userList = new ArrayList<>();
    IUserRoleDao roleDao = mock( IUserRoleDao.class );
    when( roleDao.getUsers() ).thenReturn( userList );
    PentahoSystem.registerObject( roleDao );

    UserListWrapper wrapUserList = new UserListWrapper( userList );

    assertEquals( wrapUserList.getUsers(), userRoleService.getUsers().getUsers() );
  }

  @Test
  public void testGetRolesForUser() {
    List<IPentahoRole> roleList = new ArrayList<>();
    IUserRoleDao roleDao = mock( IUserRoleDao.class );
    when( roleDao.getUserRoles( any( ITenant.class ), nullable( String.class ) ) ).thenReturn( roleList );
    PentahoSystem.registerObject( roleDao );

    RoleListWrapper wrapRoleList = new RoleListWrapper( roleList );

    IPentahoSession session = mock( IPentahoSession.class );
    String tenantPath = "testPath";
    when( session.getAttribute( IPentahoSession.TENANT_ID_KEY ) ).thenReturn( tenantPath );
    PentahoSessionHolder.setSession( session );

    setupMockSessionUser( SESSION_USER_NAME, true );

    assertEquals( wrapRoleList.getRoles(), userRoleService.getRolesForUser( "admin" ).getRoles() );
  }

  @Test
  public void testAssignRoleToUser() {
    String userName = "testUser";
    String roleNames = "Power User\tBusiness User\t";

    setupMockSessionUser( SESSION_USER_NAME, true );

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
    when( roleDao.getUserRoles( any( ITenant.class ), nullable( String.class ) ) ).thenReturn( roleList );
    PentahoSystem.registerObject( roleDao );

    userRoleService.assignRolesToUser( userName, roleNames );
    verify( roleDao ).setUserRoles( any( ITenant.class ), nullable( String.class ),
      argThat( new UnorderedArrayMatcher( new String[]{"ceo", "cto", "Power User", "Business User"} ) ) );
  }

  @Test( expected = SecurityException.class )
  public void testAssignRoleToUserUnauthorizedException() {
    String userName = "testUser";
    String roleNames = "Power User\tBusiness User\t";

    setupMockSessionUser( SESSION_USER_NAME, false );

    userRoleService.assignRolesToUser( userName, roleNames );
  }

  @Test( expected = NotFoundException.class )
  public void testAssignRoleToUserNotFoundException() {
    String userName = "testUser";
    String roleNames = "Power User\tBusiness User\t";

    setupMockSessionUser( SESSION_USER_NAME, true );

    //Create session that will generate tenant
    IPentahoSession session = mock( IPentahoSession.class );
    when( session.getAttribute( IPentahoSession.TENANT_ID_KEY ) ).thenReturn( "testTenantPath" );
    PentahoSessionHolder.setSession( session );

    IUserRoleDao roleDao = mock( IUserRoleDao.class );
    when( roleDao.getUserRoles( any( ITenant.class ), nullable( String.class ) ) ).thenThrow(
      new NotFoundException( "expectedTestException" ) );
    PentahoSystem.registerObject( roleDao );

    userRoleService.assignRolesToUser( userName, roleNames );
  }

  @Test( expected = UncategorizedUserRoleDaoException.class )
  public void testAssignRoleToUserUncategorizedUserRoleDaoException() {
    String userName = "testUser";
    String roleNames = "Power User\tBusiness User\t";

    setupMockSessionUser( SESSION_USER_NAME, true );

    //Create session that will generate tenant
    IPentahoSession session = mock( IPentahoSession.class );
    when( session.getAttribute( IPentahoSession.TENANT_ID_KEY ) ).thenReturn( "testTenantPath" );
    PentahoSessionHolder.setSession( session );

    IUserRoleDao roleDao = mock( IUserRoleDao.class );
    when( roleDao.getUserRoles( any( ITenant.class ), nullable( String.class ) ) ).thenThrow(
      new UncategorizedUserRoleDaoException( "expectedTestException" ) );
    PentahoSystem.registerObject( roleDao );

    userRoleService.assignRolesToUser( userName, roleNames );
  }

  @Test
  public void testRemoveRoleFromUser() {
    String userName = "testUser";
    String roleNames = "Power User\tBusiness User\t";

    setupMockSessionUser( SESSION_USER_NAME, true );

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
    when( roleDao.getUserRoles( any( ITenant.class ), nullable( String.class ) ) ).thenReturn( roleList );
    PentahoSystem.registerObject( roleDao );

    userRoleService.removeRolesFromUser( userName, roleNames );
    verify( roleDao ).setUserRoles( any( ITenant.class ), nullable( String.class ),
      argThat( new UnorderedArrayMatcher( new String[]{"ceo", "cto"} ) ) );
  }

  @Test( expected = SecurityException.class )
  public void testRemoveRoleFromUserSecurityException() {
    String userName = "testUser";
    String roleNames = "Power User\tBusiness User\t";

    setupMockSessionUser( SESSION_USER_NAME, false );

    userRoleService.removeRolesFromUser( userName, roleNames );
  }

  @Test( expected = NotFoundException.class )
  public void testRemoveRoleFromUserNotFoundException() {
    String userName = "testUser";
    String roleNames = "Power User\tBusiness User\t";

    setupMockSessionUser( SESSION_USER_NAME, true );

    //Create session that will generate tenant
    IPentahoSession session = mock( IPentahoSession.class );
    when( session.getAttribute( IPentahoSession.TENANT_ID_KEY ) ).thenReturn( "testTenantPath" );
    PentahoSessionHolder.setSession( session );

    IUserRoleDao roleDao = mock( IUserRoleDao.class );
    when( roleDao.getUserRoles( any( ITenant.class ), nullable( String.class ) ) ).thenThrow(
      new NotFoundException( "expectedTestException" ) );
    PentahoSystem.registerObject( roleDao );

    userRoleService.removeRolesFromUser( userName, roleNames );
  }

  @Test( expected = UncategorizedUserRoleDaoException.class )
  public void testRemoveRolesFromUserUncategorizedUserRoleDaoException() {
    String userName = "testUser";
    String roleNames = "Power User\tBusiness User\t";

    setupMockSessionUser( SESSION_USER_NAME, true );

    //Create session that will generate tenant
    IPentahoSession session = mock( IPentahoSession.class );
    when( session.getAttribute( IPentahoSession.TENANT_ID_KEY ) ).thenReturn( "testTenantPath" );
    PentahoSessionHolder.setSession( session );

    IUserRoleDao roleDao = mock( IUserRoleDao.class );
    when( roleDao.getUserRoles( any( ITenant.class ), nullable( String.class ) ) ).thenThrow(
      new UncategorizedUserRoleDaoException( "expectedTestException" ) );
    PentahoSystem.registerObject( roleDao );

    userRoleService.removeRolesFromUser( userName, roleNames );
  }

  @Test
  public void testDeleteUsers() {
    String users = "admin\tsuzy\t";

    setupMockSessionUser( SESSION_USER_NAME, true );

    IPentahoUser user = mock( IPentahoUser.class );
    IUserRoleDao roleDao = mock( IUserRoleDao.class );
    when( roleDao.getUser( nullable( ITenant.class ), nullable( String.class ) ) ).thenReturn( user );
    PentahoSystem.registerObject( roleDao );

    userRoleService.deleteUsers( users );
    verify( roleDao, times( 2 ) ).deleteUser( nullable( IPentahoUser.class ) );
  }

  @Test( expected = SecurityException.class )
  public void testDeleteUsersSecurityException() {
    String users = "admin\tsuzy\t";

    setupMockSessionUser( SESSION_USER_NAME, false );

    userRoleService.deleteUsers( users );
  }

  @Test( expected = UncategorizedUserRoleDaoException.class )
  public void testDeleteUsersUncategorizedUserRoleDaoException() {
    String users = "admin\tsuzy\t";

    setupMockSessionUser( SESSION_USER_NAME, true );

    IPentahoUser user = mock( IPentahoUser.class );
    IUserRoleDao roleDao = mock( IUserRoleDao.class );
    when( roleDao.getUser( nullable( ITenant.class ), nullable( String.class ) ) )
      .thenThrow( new UncategorizedUserRoleDaoException( "expectedTestException" ) );
    PentahoSystem.registerObject( roleDao );

    userRoleService.deleteUsers( users );
  }

  @Test
  public void testGetRoles() throws Exception {
    List<IPentahoRole> roleList = new ArrayList<>();
    IUserRoleDao roleDao = mock( IUserRoleDao.class );
    when( roleDao.getRoles() ).thenReturn( roleList );
    PentahoSystem.registerObject( roleDao );

    IPentahoRole role = mock( IPentahoRole.class );
    when( role.getName() ).thenReturn( "testRole" );
    roleList.add( role );
    RoleListWrapper wrapRoleList = new RoleListWrapper( roleList );

    assertEquals( wrapRoleList.getRoles(), userRoleService.getRoles().getRoles() );
  }

  @Test
  public void testGetRoleMembers() {
    setupMockSessionUser( SESSION_USER_NAME, true );

    List<IPentahoUser> userList = new ArrayList<>();
    IUserRoleDao roleDao = mock( IUserRoleDao.class );
    when( roleDao.getRoleMembers( any( ITenant.class ), nullable( String.class ) ) ).thenReturn( userList );
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

  @Test( expected = SecurityException.class )
  public void testGetRoleMembersSecurityException() {
    setupMockSessionUser( SESSION_USER_NAME, false );

    List<IPentahoUser> userList = new ArrayList<>();
    IUserRoleDao roleDao = mock( IUserRoleDao.class );
    when( roleDao.getRoleMembers( any( ITenant.class ), nullable( String.class ) ) ).thenReturn( userList );
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

    setupMockSessionUser( SESSION_USER_NAME, true );

    IPentahoRole role = mock( IPentahoRole.class );
    IUserRoleDao roleDao = mock( IUserRoleDao.class );
    when( roleDao.getRole( nullable( ITenant.class ), nullable( String.class ) ) ).thenReturn( role );
    PentahoSystem.registerObject( roleDao );

    userRoleService.deleteRoles( roles );
    verify( roleDao, times( 2 ) ).deleteRole( nullable( IPentahoRole.class ) );
  }

  @Test( expected = SecurityException.class )
  public void testDeleteRolesSecurityException() {
    String roles = "role1\trole2\t";

    setupMockSessionUser( SESSION_USER_NAME, false );

    userRoleService.deleteUsers( roles );
  }

  @Test( expected = UncategorizedUserRoleDaoException.class )
  public void testDeleteRoleUncategorizedUserRoleDaoException() {
    String roles = "role1\trole2\t";

    setupMockSessionUser( SESSION_USER_NAME, true );

    IUserRoleDao roleDao = mock( IUserRoleDao.class );
    when( roleDao.getRole( nullable( ITenant.class ), nullable( String.class ) ) ).thenThrow(
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
    ArrayList<LogicalRoleAssignment> roles = new ArrayList<>();
    roles.add( roleAssignment );

    LogicalRoleAssignments roleAssignments = mock( LogicalRoleAssignments.class );
    when( roleAssignments.getAssignments() ).thenReturn( roles );

    setupMockSessionUser( SESSION_USER_NAME, true );

    IRoleAuthorizationPolicyRoleBindingDao roleBindingDao = mock( IRoleAuthorizationPolicyRoleBindingDao.class );
    PentahoSystem.registerObject( roleBindingDao );

    userRoleService.setLogicalRoles( roleAssignments );
    verify( roleBindingDao ).setRoleBindings( roleName, roleList );
  }

  @Test( expected = SecurityException.class )
  public void testSetLogicalRolesSecurityException() {
    LogicalRoleAssignments roleAssignments = mock( LogicalRoleAssignments.class );

    setupMockSessionUser( SESSION_USER_NAME, false );

    userRoleService.setLogicalRoles( roleAssignments );
  }

  @Test
  public void testGetRoleBindingStruct() {
    setupMockSessionUser( SESSION_USER_NAME, true );

    Map<String, String> localizedNameEntry = new HashMap<>();
    localizedNameEntry.put( "testEntry", "testValue" );

    Map<String, List<String>> testBindingMap = new HashMap<>();
    testBindingMap.put( "testBinding", new ArrayList<>() );

    RoleBindingStruct roleBindingStruct = mock( RoleBindingStruct.class );
    roleBindingStruct.logicalRoleNameMap = localizedNameEntry;
    roleBindingStruct.bindingMap = testBindingMap;
    roleBindingStruct.immutableRoles = new HashSet<>();

    IRoleAuthorizationPolicyRoleBindingDao roleBindingDao = mock( IRoleAuthorizationPolicyRoleBindingDao.class );
    when( roleBindingDao.getRoleBindingStruct( nullable( String.class ) ) ).thenReturn( roleBindingStruct );
    PentahoSystem.registerObject( roleBindingDao );

    SystemRolesMap validateMap = userRoleService.getRoleBindingStruct( "en" );

    assert ( 1 == validateMap.getLocalizedRoleNames().size() );
    assertEquals( "testEntry", validateMap.getLocalizedRoleNames().get( 0 ).getRoleName() );
    assertEquals( "testValue", validateMap.getLocalizedRoleNames().get( 0 ).getLocalizedName() );
  }

  @Test( expected = SecurityException.class )
  public void testGetRoleBindingStructSecurityException() {
    setupMockSessionUser( SESSION_USER_NAME, false );

    userRoleService.getRoleBindingStruct( "en" );
  }

  private static class UnorderedArrayMatcher implements ArgumentMatcher<String[]> {
    private final Set<String> correctValues;

    UnorderedArrayMatcher( String[] expected ) {
      correctValues = new HashSet<>( Arrays.asList( expected ) );
    }

    @Override public boolean matches( String[] argument ) {
      String[] arguments = argument;
      Set<String> returnedValues = new HashSet<>( Arrays.asList( arguments ) );
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

    setupMockSessionUser( SESSION_USER_NAME, true );

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
  public void testChangePassByAdminSuccess() throws Exception {
    setupMockSessionUser( SESSION_USER_NAME, true );

    IUserRoleDao roleDao = registerMockUserRoleDao();
    addMockUserToUserRoleDao( roleDao, OTHER_USER_NAME, OTHER_USER_PASSWORD );

    userRoleService.changeUserPassword( OTHER_USER_NAME, A_NEW_PASSWORD, OTHER_USER_PASSWORD );
  }

  @Test
  public void testChangeOwnPasswordSuccess() throws Exception {
    setupMockSessionUser( SESSION_USER_NAME, false );

    IUserRoleDao roleDao = registerMockUserRoleDao();
    addMockUserToUserRoleDao( roleDao, SESSION_USER_NAME, SESSION_USER_PASSWORD );

    userRoleService.changeUserPassword( SESSION_USER_NAME, A_NEW_PASSWORD, SESSION_USER_PASSWORD );
  }

  @Test( expected = SecurityException.class )
  public void testChangePasswordNoPermissions() throws Exception {
    setupMockSessionUser( SESSION_USER_NAME, false );

    IUserRoleDao roleDao = registerMockUserRoleDao();
    addMockUserToUserRoleDao( roleDao, OTHER_USER_NAME, OTHER_USER_PASSWORD );

    userRoleService.changeUserPassword( OTHER_USER_NAME, A_NEW_PASSWORD, OTHER_USER_PASSWORD );
  }

  @Test( expected = SecurityException.class )
  public void testChangePassWrongName() throws Exception {
    // tests when the username can't be found in the UserRoleDao,
    // even thou it is the current session username (not sure when this can happen)

    setupMockSessionUser( SESSION_USER_NAME, false );

    registerMockUserRoleDao();

    userRoleService.changeUserPassword( OTHER_USER_NAME, A_NEW_PASSWORD, OTHER_USER_PASSWORD );
  }

  @Test( expected = SecurityException.class )
  public void testChangePassWrongPass() throws Exception {
    setupMockSessionUser( SESSION_USER_NAME, true );

    IUserRoleDao roleDao = registerMockUserRoleDao();
    addMockUserToUserRoleDao( roleDao, OTHER_USER_NAME, OTHER_USER_PASSWORD );

    userRoleService.changeUserPassword( OTHER_USER_NAME, A_NEW_PASSWORD, A_WRONG_PASSWORD );
  }

  @Test( expected = UserRoleDaoService.ValidationFailedException.class )
  public void testCreateRoleEmptyName() throws Exception {
    setupMockSessionUser( SESSION_USER_NAME, true );

    userRoleService.createRole( "" );
  }

  @Test( expected = UserRoleDaoService.ValidationFailedException.class )
  public void testCreateRoleNullName() throws Exception {
    setupMockSessionUser( SESSION_USER_NAME, true );

    userRoleService.createRole( null );
  }

  @Test
  public void testUpdatePasswordWithAdminCredentials() throws Exception {
    setupMockSessionUser( SESSION_USER_NAME, true );

    AuthenticationProvider authenticationProvider = registerMockAuthenticationProvider();
    addMockUserToAuthenticationProvider( authenticationProvider, SESSION_USER_NAME, SESSION_USER_PASSWORD );

    IUserRoleDao roleDao = registerMockUserRoleDao();
    addMockUserToUserRoleDao( roleDao, OTHER_USER_NAME, OTHER_USER_PASSWORD );

    userRoleService.updatePassword( new User( OTHER_USER_NAME, A_NEW_PASSWORD ), SESSION_USER_PASSWORD );

    verify( roleDao, times( 1 ) ).setPassword( nullable( ITenant.class ), eq( OTHER_USER_NAME ), eq( A_NEW_PASSWORD ) );
  }

  @Test
  public void testSelfUpdatePasswordWithAdminCredentials() throws Exception {
    setupMockSessionUser( SESSION_USER_NAME, true );

    AuthenticationProvider authenticationProvider = registerMockAuthenticationProvider();
    addMockUserToAuthenticationProvider( authenticationProvider, SESSION_USER_NAME, SESSION_USER_PASSWORD );

    IUserRoleDao roleDao = registerMockUserRoleDao();
    addMockUserToUserRoleDao( roleDao, SESSION_USER_NAME, SESSION_USER_PASSWORD );

    userRoleService.updatePassword( new User( SESSION_USER_NAME, A_NEW_PASSWORD ), SESSION_USER_PASSWORD );

    verify( roleDao, times( 1 ) ).setPassword( nullable( ITenant.class ), eq( SESSION_USER_NAME ), eq( A_NEW_PASSWORD ) );
  }

  @Test( expected = SecurityException.class )
  public void testUpdatePasswordWithWrongAdminCredentials() throws Exception {
    setupMockSessionUser( SESSION_USER_NAME, true );

    AuthenticationProvider authenticationProvider = registerMockAuthenticationProvider();
    addMockUserToAuthenticationProvider( authenticationProvider, SESSION_USER_NAME, SESSION_USER_PASSWORD );

    IUserRoleDao roleDao = registerMockUserRoleDao();
    addMockUserToUserRoleDao( roleDao, OTHER_USER_NAME, OTHER_USER_PASSWORD );

    userRoleService.updatePassword( new User( OTHER_USER_NAME, A_NEW_PASSWORD ), A_WRONG_PASSWORD );
  }

  @Test( expected = SecurityException.class )
  public void testUpdatePasswordWithNotAdminCredentials() throws Exception {
    setupMockSessionUser( SESSION_USER_NAME, false );

    AuthenticationProvider authenticationProvider = registerMockAuthenticationProvider();
    addMockUserToAuthenticationProvider( authenticationProvider, SESSION_USER_NAME, SESSION_USER_PASSWORD );

    IUserRoleDao roleDao = registerMockUserRoleDao();
    addMockUserToUserRoleDao( roleDao, OTHER_USER_NAME, OTHER_USER_PASSWORD );

    userRoleService.updatePassword( new User( OTHER_USER_NAME, A_NEW_PASSWORD ), SESSION_USER_PASSWORD );
  }

  @Test( expected = SecurityException.class )
  public void testSelfUpdatePasswordWithNotAdminCredentials() throws Exception {
    setupMockSessionUser( SESSION_USER_NAME, false );

    AuthenticationProvider authenticationProvider = registerMockAuthenticationProvider();
    addMockUserToAuthenticationProvider( authenticationProvider, SESSION_USER_NAME, SESSION_USER_PASSWORD );

    IUserRoleDao roleDao = registerMockUserRoleDao();
    addMockUserToUserRoleDao( roleDao, SESSION_USER_NAME, SESSION_USER_PASSWORD );

    userRoleService.updatePassword( new User( SESSION_USER_NAME, A_NEW_PASSWORD ), SESSION_USER_PASSWORD );
  }

  @Test
  public void testUpdatePassword() throws Exception {
    setupMockSessionUser( SESSION_USER_NAME, true );

    IUserRoleDao roleDao = registerMockUserRoleDao();
    addMockUserToUserRoleDao( roleDao, SESSION_USER_NAME, SESSION_USER_PASSWORD );
    addMockUserToUserRoleDao( roleDao, OTHER_USER_NAME, OTHER_USER_PASSWORD );

    userRoleService.updatePassword( new User( OTHER_USER_NAME, A_NEW_PASSWORD ) );

    verify( roleDao, times( 1 ) ).setPassword( nullable( ITenant.class ), eq( OTHER_USER_NAME ), eq( A_NEW_PASSWORD ) );
  }

  @Test
  public void testSelfUpdatePassword() throws Exception {
    setupMockSessionUser( SESSION_USER_NAME, true );

    IUserRoleDao roleDao = registerMockUserRoleDao();
    addMockUserToUserRoleDao( roleDao, SESSION_USER_NAME, SESSION_USER_PASSWORD );

    userRoleService.updatePassword( new User( SESSION_USER_NAME, A_NEW_PASSWORD ) );

    verify( roleDao, times( 1 ) ).setPassword( nullable( ITenant.class ), eq( SESSION_USER_NAME ), eq( A_NEW_PASSWORD ) );
  }

  @Test( expected = SecurityException.class )
  public void testUpdatePasswordNotAdmin() throws Exception {
    setupMockSessionUser( SESSION_USER_NAME, false );

    IUserRoleDao roleDao = registerMockUserRoleDao();
    addMockUserToUserRoleDao( roleDao, SESSION_USER_NAME, SESSION_USER_PASSWORD );
    addMockUserToUserRoleDao( roleDao, OTHER_USER_NAME, OTHER_USER_PASSWORD );

    userRoleService.updatePassword( new User( OTHER_USER_NAME, A_NEW_PASSWORD ) );
  }

  @Test( expected = SecurityException.class )
  public void testSelfUpdatePasswordNotAdmin() throws Exception {
    setupMockSessionUser( SESSION_USER_NAME, false );

    IUserRoleDao roleDao = registerMockUserRoleDao();
    addMockUserToUserRoleDao( roleDao, SESSION_USER_NAME, SESSION_USER_PASSWORD );

    userRoleService.updatePassword( new User( SESSION_USER_NAME, A_NEW_PASSWORD ) );
  }

  private void setupMockSessionUser( String username, boolean canAdminister ) {
    registerMockSession( username );

    // Used by the canAdminister call
    registerMockAuthorizationPolicy( canAdminister );
  }

  private IPentahoSession registerMockSession( String username ) {
    final IPentahoSession pentahoSession = mock( IPentahoSession.class );
    when( pentahoSession.getName() ).thenReturn( username );

    PentahoSessionHolder.setSession( pentahoSession );

    return pentahoSession;
  }

  private IUserRoleDao registerMockUserRoleDao() {
    IUserRoleDao roleDao = mock( IUserRoleDao.class );
    PentahoSystem.registerObject( roleDao );
    return roleDao;
  }

  private void addMockUserToUserRoleDao( IUserRoleDao roleDao, String username, String password ) {
    IPentahoUser pentahoUser = createMockPentahoUser( username, password );

    doReturn( pentahoUser ).when( roleDao ).getUser( nullable( ITenant.class ), eq( username ) );
  }

  private AuthenticationProvider registerMockAuthenticationProvider() {
    AuthenticationProvider authenticationProvider = mock( AuthenticationProvider.class );
    PentahoSystem.registerObject( authenticationProvider );
    return authenticationProvider;
  }

  private void addMockUserToAuthenticationProvider( AuthenticationProvider authenticationProvider, String username, String password ) {
    Authentication mockAuthentication = mock( Authentication.class );
    doReturn( true ).when( mockAuthentication ).isAuthenticated();

    doAnswer( invocationOnMock -> {
      UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) invocationOnMock.getArguments()[0];
      if ( token.getName().equals( username ) && token.getCredentials().equals( password ) ) {
        return mockAuthentication;
      }

      throw mock( AuthenticationException.class );
    } ).when( authenticationProvider ).authenticate( any( UsernamePasswordAuthenticationToken.class ) );
  }

  private IPentahoUser createMockPentahoUser( String username, String password ) {
    IPentahoUser pentahoUser = mock( IPentahoUser.class );
    doReturn( username ).when( pentahoUser ).getUsername();
    doReturn( password ).when( pentahoUser ).getPassword();

    return pentahoUser;
  }

  private IAuthorizationPolicy registerMockAuthorizationPolicy( final boolean isAllowed ) {
    IAuthorizationPolicy policy = mock( IAuthorizationPolicy.class );
    when( policy.isAllowed( nullable( String.class ) ) ).thenReturn( isAllowed );

    PentahoSystem.registerObject( policy );

    return policy;
  }

}
