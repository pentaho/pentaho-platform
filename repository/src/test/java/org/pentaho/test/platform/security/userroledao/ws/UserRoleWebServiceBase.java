/*!
 *
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright (c) 2002-2021 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.test.platform.security.userroledao.ws;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.security.userroledao.AlreadyExistsException;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoRole;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoUser;
import org.pentaho.platform.api.engine.security.userroledao.IUserRoleDao;
import org.pentaho.platform.api.engine.security.userroledao.NotFoundException;
import org.pentaho.platform.api.engine.security.userroledao.UncategorizedUserRoleDaoException;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.security.policy.rolebased.actions.AdministerSecurityAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryCreateAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryReadAction;
import org.pentaho.platform.security.userroledao.PentahoRole;
import org.pentaho.platform.security.userroledao.PentahoUser;
import org.pentaho.platform.security.userroledao.ws.IUserRoleWebService;
import org.pentaho.platform.security.userroledao.ws.ProxyPentahoRole;
import org.pentaho.platform.security.userroledao.ws.ProxyPentahoUser;
import org.pentaho.platform.security.userroledao.ws.UserRoleException;
import org.pentaho.platform.security.userroledao.ws.UserRoleSecurityInfo;
import org.pentaho.platform.security.userroledao.ws.UserRoleWebService;
import org.pentaho.test.platform.engine.core.MicroPlatform;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings( "nls" )
public class UserRoleWebServiceBase {
  private MicroPlatform microPlatform;
  private static HashSet<IPentahoUser> users = new HashSet<IPentahoUser>();
  private static HashSet<IPentahoRole> roles = new HashSet<IPentahoRole>();
  private static Map<IPentahoUser, Set<IPentahoRole>> userRolesMap = new HashMap<IPentahoUser, Set<IPentahoRole>>();
  private static Map<IPentahoRole, Set<IPentahoUser>> roleMembersMap = new HashMap<IPentahoRole, Set<IPentahoUser>>();
  private static final String USER_ROLE_DAO_TXN = "userRoleDaoTxn";
  private IAuthorizationPolicy mockPolicy = null;
  private static final String SOLUTION_PATH = "src/test/resources/solution";

  public static class UserRoleDaoMock implements IUserRoleDao {

    class TestTenant implements ITenant {
      private static final long serialVersionUID = 7753150663858677669L;

      public boolean isEnabled() {
        return true;
      }

      String absPath;

      public TestTenant( String tentantRootFolderAbsPath ) {
        absPath = tentantRootFolderAbsPath;
      }

      @Override
      public String getId() {
        return absPath;
      }

      public String getRootFolderAbsolutePath() {
        return absPath;
      }

      @Override
      public String getName() {
        return absPath.substring( absPath.lastIndexOf( "/" ) );
      }

      @Override
      public boolean equals( Object obj ) {
        // TODO Auto-generated method stub
        return obj instanceof TestTenant && getId().equals( ( (ITenant) obj ).getId() );
      }

    }

    private ITenant getDefaultTenant() {
      return new TestTenant( "/pentaho" );
    }

    @Override
    public IPentahoRole createRole( ITenant tenant, String roleName, String description, String[] memberUserNames )
      throws AlreadyExistsException, UncategorizedUserRoleDaoException {
      if ( tenant == null ) {
        tenant = getDefaultTenant();
      }
      IPentahoRole role = getRole( tenant, roleName );
      if ( role == null ) {
        role = new PentahoRole( tenant, roleName, description );
        roles.add( role );
        setRoleMembers( tenant, roleName, memberUserNames );
      } else {
        throw new AlreadyExistsException( "" );
      }
      return role;
    }

    @Override
    public IPentahoUser createUser( ITenant tenant, String username, String password, String description,
        String[] roleNames ) throws AlreadyExistsException, UncategorizedUserRoleDaoException {
      if ( tenant == null ) {
        tenant = getDefaultTenant();
      }
      IPentahoUser user = getUser( tenant, username );
      if ( user == null ) {
        user = new PentahoUser( tenant, username, password, description, true );
        users.add( user );
        setUserRoles( tenant, username, roleNames );
      }
      return user;
    }

    @Override
    public List<IPentahoUser> getRoleMembers( ITenant tenant, String roleName )
      throws UncategorizedUserRoleDaoException {
      if ( tenant == null ) {
        tenant = getDefaultTenant();
      }
      Set<IPentahoUser> members = null;
      IPentahoRole pentahoRole = getRole( tenant, roleName );
      if ( pentahoRole != null ) {
        members = roleMembersMap.get( pentahoRole );
      }
      return members == null ? new ArrayList<IPentahoUser>() : new ArrayList<IPentahoUser>( members );
    }

    @Override
    public List<IPentahoRole> getUserRoles( ITenant tenant, String userName ) throws UncategorizedUserRoleDaoException {
      if ( tenant == null ) {
        tenant = getDefaultTenant();
      }

      Set<IPentahoRole> roles = null;
      IPentahoUser pentahoUser = getUser( tenant, userName );
      if ( pentahoUser != null ) {
        roles = userRolesMap.get( pentahoUser );
      }
      return roles == null ? new ArrayList<IPentahoRole>() : new ArrayList<IPentahoRole>( roles );
    }

    @Override
    public void setPassword( ITenant tenant, String userName, String password ) throws NotFoundException,
      UncategorizedUserRoleDaoException {
      if ( tenant == null ) {
        tenant = getDefaultTenant();
      }
      IPentahoUser user = getUser( tenant, userName );
      if ( user != null ) {
        user.setPassword( password );
      } else {
        throw new NotFoundException( "" );
      }
    }

    @Override
    public void setRoleDescription( ITenant tenant, String roleName, String description ) throws NotFoundException,
      UncategorizedUserRoleDaoException {
      if ( tenant == null ) {
        tenant = getDefaultTenant();
      }

      IPentahoRole role = getRole( tenant, roleName );
      if ( role != null ) {
        role.setDescription( description );
      } else {
        throw new NotFoundException( "" );
      }
    }

    @Override
    public void setRoleMembers( ITenant tenant, String roleName, String[] memberUserNames ) throws NotFoundException,
      UncategorizedUserRoleDaoException {

      if ( tenant == null ) {
        tenant = getDefaultTenant();
      }

      IPentahoRole role = getRole( tenant, roleName );
      if ( role == null ) {
        throw new NotFoundException( "" );
      }

      for ( Set<IPentahoRole> assignedRoles : userRolesMap.values() ) {
        assignedRoles.remove( role );
      }

      for ( IPentahoRole tmpRole : roles ) {
        if ( tmpRole.getName().equals( roleName ) && tmpRole.getTenant().equals( tenant ) ) {
          role = tmpRole;
          break;
        }
      }

      Set<IPentahoUser> members = new HashSet<IPentahoUser>();
      if ( memberUserNames == null ) {
        memberUserNames = new String[0];
      }
      for ( String memberName : memberUserNames ) {
        IPentahoUser user = getUser( tenant, memberName );
        if ( user != null ) {
          members.add( user );
        }
      }

      roleMembersMap.put( role, members );

      for ( IPentahoUser user : members ) {
        Set<IPentahoRole> assignedRoles = userRolesMap.get( user );
        if ( assignedRoles == null ) {
          assignedRoles = new HashSet<IPentahoRole>();
          userRolesMap.put( user, assignedRoles );
        }
        assignedRoles.add( role );
      }

    }

    @Override
    public void setUserDescription( ITenant tenant, String userName, String description ) throws NotFoundException,
      UncategorizedUserRoleDaoException {
      if ( tenant == null ) {
        tenant = getDefaultTenant();
      }
      IPentahoUser user = getUser( tenant, userName );
      if ( user != null ) {
        user.setDescription( description );
      } else {
        throw new NotFoundException( "" );
      }
    }

    @Override
    public void setUserRoles( ITenant tenant, String userName, String[] roleNames ) throws NotFoundException,
      UncategorizedUserRoleDaoException {
      if ( tenant == null ) {
        tenant = getDefaultTenant();
      }

      IPentahoUser targetUser = getUser( tenant, userName );
      if ( targetUser == null ) {
        throw new NotFoundException( "" );
      }

      for ( Set<IPentahoUser> assignedMembers : roleMembersMap.values() ) {
        assignedMembers.remove( targetUser );
      }

      for ( IPentahoUser tmpUser : users ) {
        if ( tmpUser.getUsername().equals( userName ) && tmpUser.getTenant().equals( tenant ) ) {
          targetUser = tmpUser;
          break;
        }
      }

      Set<IPentahoRole> assignedRoles = new HashSet<IPentahoRole>();
      if ( roleNames == null ) {
        roleNames = new String[0];
      }
      for ( String roleName : roleNames ) {
        IPentahoRole role = getRole( tenant, roleName );
        if ( role != null ) {
          assignedRoles.add( role );
        }
      }

      userRolesMap.put( targetUser, assignedRoles );

      for ( IPentahoRole role : assignedRoles ) {
        Set<IPentahoUser> assignedMembers = roleMembersMap.get( role );
        if ( assignedMembers == null ) {
          assignedMembers = new HashSet<IPentahoUser>();
          roleMembersMap.put( role, assignedMembers );
        }
        assignedMembers.add( targetUser );
      }
    }

    @Override
    public void deleteRole( IPentahoRole role ) throws NotFoundException, UncategorizedUserRoleDaoException {
      IPentahoRole realRole = getRole( role.getTenant(), role.getName() );
      if ( realRole != null ) {
        roles.remove( realRole );
        roleMembersMap.remove( realRole );
        for ( Set<IPentahoRole> assignedRoles : userRolesMap.values() ) {
          assignedRoles.remove( realRole );
        }
      } else {
        throw new NotFoundException( "" );
      }
    }

    @Override
    public void deleteUser( IPentahoUser user ) throws NotFoundException, UncategorizedUserRoleDaoException {
      IPentahoUser realUser = getUser( user.getTenant(), user.getUsername() );
      if ( realUser != null ) {
        users.remove( realUser );
        userRolesMap.remove( realUser );
        for ( Set<IPentahoUser> assignedUsers : roleMembersMap.values() ) {
          assignedUsers.remove( realUser );
        }
      } else {
        throw new NotFoundException( "" );
      }
    }

    @Override
    public IPentahoRole getRole( ITenant tenant, String name ) throws UncategorizedUserRoleDaoException {
      if ( tenant == null ) {
        tenant = getDefaultTenant();
      }

      for ( IPentahoRole role : roles ) {
        if ( role.getName().equals( name ) && role.getTenant().equals( tenant ) ) {
          return role;
        }
      }
      return null;
    }

    @Override
    public List<IPentahoRole> getRoles() throws UncategorizedUserRoleDaoException {
      return getRoles( getDefaultTenant() );
    }

    @Override
    public List<IPentahoRole> getRoles( ITenant tenant, boolean includeSubtenants )
      throws UncategorizedUserRoleDaoException {
      return getRoles( tenant );
    }

    @Override
    public List<IPentahoRole> getRoles( ITenant tenant ) throws UncategorizedUserRoleDaoException {
      ArrayList<IPentahoRole> result = new ArrayList<IPentahoRole>();
      for ( IPentahoRole role : roles ) {
        if ( role.getTenant().equals( tenant ) ) {
          result.add( role );
        }
      }
      return result;
    }

    @Override
    public IPentahoUser getUser( ITenant tenant, String name ) throws UncategorizedUserRoleDaoException {
      if ( tenant == null ) {
        tenant = getDefaultTenant();
      }

      for ( IPentahoUser user : users ) {
        if ( user.getUsername().equals( name ) && user.getTenant().equals( tenant ) ) {
          return user;
        }
      }
      return null;
    }

    @Override
    public List<IPentahoUser> getUsers() throws UncategorizedUserRoleDaoException {
      return getUsers( getDefaultTenant() );
    }

    @Override
    public List<IPentahoUser> getUsers( ITenant tenant, boolean includeSubtenants )
      throws UncategorizedUserRoleDaoException {
      return getUsers( tenant );
    }

    @Override
    public List<IPentahoUser> getUsers( ITenant tenant ) throws UncategorizedUserRoleDaoException {
      ArrayList<IPentahoUser> result = new ArrayList<IPentahoUser>();
      for ( IPentahoUser user : users ) {
        if ( user.getTenant().equals( tenant ) ) {
          result.add( user );
        }
      }
      return result;
    }
  }

  public static class PasswordEncoderMock implements PasswordEncoder {

    @Override public String encode( CharSequence charSequence ) {
      return charSequence.toString();
    }

    @Override public boolean matches( CharSequence charSequence, String s ) {
      return true;
    }
  }

  protected void mockUserAsAdmin( boolean isAdminUser ) {
    when( mockPolicy.isAllowed( RepositoryReadAction.NAME ) ).thenReturn( isAdminUser );
    when( mockPolicy.isAllowed( RepositoryCreateAction.NAME ) ).thenReturn( isAdminUser );
    when( mockPolicy.isAllowed( AdministerSecurityAction.NAME ) ).thenReturn( isAdminUser );
  }

  @Before
  public void init0() {
    microPlatform = new MicroPlatform( getSolutionPath() );
    microPlatform.define( USER_ROLE_DAO_TXN, UserRoleDaoMock.class );

    mockPolicy = mock( IAuthorizationPolicy.class );
    mockUserAsAdmin( false /* start out as non-admin */ );

    microPlatform.define( IAuthorizationPolicy.class.getSimpleName(), mockPolicy );

    microPlatform.define( "passwordEncoder", PasswordEncoderMock.class );

    UserRoleDaoMock userRoleDao = PentahoSystem.get( UserRoleDaoMock.class, USER_ROLE_DAO_TXN, null );

    users.clear();
    roles.clear();

    userRoleDao.createUser( null, "test1", "test", "test", null );
    userRoleDao.createUser( null, "test2", "test", "test", null );

    userRoleDao.createRole( null, "testRole1", "test role", new String[] { "test1" } );
    userRoleDao.createRole( null, "testRole2", "test role", new String[] { "test2" } );

  }

  @After
  public void destroy() {
    mockPolicy = null;
  }

  public IUserRoleWebService getUserRoleWebService() {
    return new UserRoleWebService();
  }

  @Test
  public void testGetUserRoleSecurityInfo() throws Exception {
    IUserRoleWebService service = getUserRoleWebService();
    mockUserAsAdmin( false /* non-admin user */ );

    try {
      service.getUserRoleSecurityInfo();
      Assert.fail();
    } catch ( UserRoleException e ) {
      // should this be 0001, not admin?
      Assert.assertTrue( e.getMessage().indexOf( "ERROR_0001" ) >= 0 );
    }

    mockUserAsAdmin( true /* admin user */ );

    UserRoleSecurityInfo info = service.getUserRoleSecurityInfo();

    Assert.assertNotNull( info );
    Assert.assertEquals( 2, info.getRoles().size() );
    Assert.assertEquals( 2, info.getUsers().size() );
    Assert.assertEquals( 2, info.getAssignments().size() );
  }

  @Test
  public void testCreateRole() throws Exception {
    UserRoleDaoMock userRoleDao = PentahoSystem.get( UserRoleDaoMock.class, USER_ROLE_DAO_TXN, null );

    IUserRoleWebService service = getUserRoleWebService();
    mockUserAsAdmin( false /* non-admin user */ );
    ProxyPentahoRole role = new ProxyPentahoRole( "role" );
    role.setDescription( "testing" );
    try {
      service.createRole( role );
      Assert.fail();
    } catch ( UserRoleException e ) {
      Assert.assertTrue( "ERROR_0001 not found in " + e.getMessage(), e.getMessage().indexOf( "ERROR_0001" ) >= 0 );
    }

    mockUserAsAdmin( true /* admin user */ );

    service.createRole( role );

    // the last role should have the same name and description
    IPentahoRole roleVerified = userRoleDao.getRole( null, "role" );
    Assert.assertNotNull( roleVerified );
    Assert.assertEquals( "role", roleVerified.getName() );
    Assert.assertEquals( "testing", roleVerified.getDescription() );
  }

  @Test
  public void testCreateUser() throws Exception {
    UserRoleDaoMock userRoleDao = PentahoSystem.get( UserRoleDaoMock.class, USER_ROLE_DAO_TXN, null );

    IUserRoleWebService service = getUserRoleWebService();
    mockUserAsAdmin( false /* non-admin user */ );
    ProxyPentahoUser user = new ProxyPentahoUser();
    user.setName( "test" );
    user.setEnabled( true );
    user.setPassword( "test" );
    user.setDescription( "testing" );
    try {
      service.createUser( user );
      Assert.fail();
    } catch ( UserRoleException e ) {
      Assert.assertTrue( "ERROR_0001 not found in " + e.getMessage(), e.getMessage().indexOf( "ERROR_0001" ) >= 0 );
    }

    mockUserAsAdmin( true /* admin user */ );

    service.createUser( user );

    // the last role should have the same name and description
    IPentahoUser userVerified = userRoleDao.getUser( null, "test" );
    Assert.assertNotNull( userVerified );
    Assert.assertEquals( "test", userVerified.getUsername() );
    Assert.assertEquals( "test", userVerified.getPassword() );
    Assert.assertEquals( true, userVerified.isEnabled() );
    Assert.assertEquals( "testing", userVerified.getDescription() );
  }

  @Test
  public void testGetUsers() throws Exception {
    IUserRoleWebService service = getUserRoleWebService();
    mockUserAsAdmin( false /* non-admin user */ );
    try {
      service.getUsers();
      Assert.fail();
    } catch ( UserRoleException e ) {
      // should this be 0001, not admin?
      Assert.assertTrue( "ERROR_0001 not found in " + e.getMessage(), e.getMessage().indexOf( "ERROR_0001" ) >= 0 );
    }

    mockUserAsAdmin( true /* admin user */ );

    ProxyPentahoUser[] userObjs = service.getUsers();

    Assert.assertNotNull( userObjs );
    Assert.assertEquals( 2, userObjs.length );

  }

  @Test
  public void testGetRoles() throws Exception {
    IUserRoleWebService service = getUserRoleWebService();
    mockUserAsAdmin( false /* non-admin user */ );
    try {
      service.getRoles();
      Assert.fail();
    } catch ( UserRoleException e ) {
      Assert.assertTrue( "ERROR_0001 not found in " + e.getMessage(), e.getMessage().indexOf( "ERROR_0001" ) >= 0 );
    }

    mockUserAsAdmin( true /* admin user */ );

    ProxyPentahoRole[] roleObjs = service.getRoles();

    Assert.assertNotNull( roleObjs );
    Assert.assertEquals( 2, roleObjs.length );

  }

  @Test
  public void testGetUser() throws Exception {
    IUserRoleWebService service = getUserRoleWebService();
    mockUserAsAdmin( false /* non-admin user */ );
    try {
      service.getUser( null );
      Assert.fail();
    } catch ( UserRoleException e ) {
      Assert.assertTrue( "ERROR_0001 not found in " + e.getMessage(), e.getMessage().indexOf( "ERROR_0001" ) >= 0 );
    }

    mockUserAsAdmin( true /* admin user */ );

    ProxyPentahoUser userObj = service.getUser( "test1" );

    Assert.assertNotNull( userObj );
    Assert.assertEquals( "test1", userObj.getName() );

  }

  @Test
  public void testDeleteRoles() throws Exception {
    IUserRoleWebService service = getUserRoleWebService();
    mockUserAsAdmin( false /* non-admin user */ );
    ProxyPentahoRole[] rolesObj = new ProxyPentahoRole[1];
    rolesObj[0] = new ProxyPentahoRole( "testRole1" );
    try {
      service.deleteRoles( rolesObj );
      Assert.fail();
    } catch ( UserRoleException e ) {
      Assert.assertTrue( "ERROR_0001 not found in " + e.getMessage(), e.getMessage().indexOf( "ERROR_0001" ) >= 0 );
    }

    mockUserAsAdmin( true /* admin user */ );

    service.deleteRoles( rolesObj );

    Assert.assertEquals( 1, roles.size() );
  }

  @Test
  public void testDeleteUsers() throws Exception {
    IUserRoleWebService service = getUserRoleWebService();
    mockUserAsAdmin( false /* non-admin user */ );
    ProxyPentahoUser[] usersObj = new ProxyPentahoUser[1];
    usersObj[0] = new ProxyPentahoUser();
    usersObj[0].setName( "test1" );
    try {
      service.deleteUsers( usersObj );
      Assert.fail();
    } catch ( UserRoleException e ) {
      Assert.assertTrue( "ERROR_0001 not found in " + e.getMessage(), e.getMessage().indexOf( "ERROR_0001" ) >= 0 );
    }

    mockUserAsAdmin( true /* admin user */ );

    service.deleteUsers( usersObj );

    Assert.assertEquals( 1, users.size() );
  }

  @Test
  public void testGetRolesForUser() throws UserRoleException {
    IUserRoleWebService service = getUserRoleWebService();
    mockUserAsAdmin( false /* non-admin user */ );
    ProxyPentahoUser userObj = new ProxyPentahoUser();
    userObj.setName( "test1" );
    try {
      service.getRolesForUser( userObj );
      Assert.fail();
    } catch ( UserRoleException e ) {
      Assert.assertTrue( "ERROR_0001 not found in " + e.getMessage(), e.getMessage().indexOf( "ERROR_0001" ) >= 0 );
    }

    mockUserAsAdmin( true /* admin user */ );

    ProxyPentahoRole[] roles = service.getRolesForUser( userObj );

    Assert.assertEquals( 1, roles.length );
  }

  @Test
  public void testGetUsersForRole() throws UserRoleException {
    IUserRoleWebService service = getUserRoleWebService();
    mockUserAsAdmin( false /* non-admin user */ );
    ProxyPentahoRole roleObj = new ProxyPentahoRole( "testRole1" );
    try {
      service.getUsersForRole( roleObj );
      Assert.fail();
    } catch ( UserRoleException e ) {
      Assert.assertTrue( "ERROR_0001 not found in " + e.getMessage(), e.getMessage().indexOf( "ERROR_0001" ) >= 0 );
    }

    mockUserAsAdmin( true /* admin user */ );

    ProxyPentahoUser[] userObjs = service.getUsersForRole( roleObj );

    Assert.assertEquals( 1, userObjs.length );
  }

  @Test
  public void testSetRoles() throws UserRoleException {
    UserRoleDaoMock userRoleDao = PentahoSystem.get( UserRoleDaoMock.class, USER_ROLE_DAO_TXN, null );

    IUserRoleWebService service = getUserRoleWebService();
    mockUserAsAdmin( false /* non-admin user */ );
    ProxyPentahoUser userObj = new ProxyPentahoUser();
    userObj.setName( "test1" );

    ProxyPentahoRole[] rolesObj = new ProxyPentahoRole[1];
    rolesObj[0] = new ProxyPentahoRole( "testRole2" );

    try {
      service.setRoles( userObj, rolesObj );
      Assert.fail();
    } catch ( UserRoleException e ) {
      Assert.assertTrue( "ERROR_0001 not found in " + e.getMessage(), e.getMessage().indexOf( "ERROR_0001" ) >= 0 );
    }

    mockUserAsAdmin( true /* admin user */ );

    userRoleDao.getUserRoles( null, "test1" );
    Assert.assertEquals( "testRole1", userRoleDao.getUserRoles( null, "test1" ).get( 0 ).getName() );

    service.setRoles( userObj, rolesObj );

    Assert.assertEquals( "testRole2", userRoleDao.getUserRoles( null, "test1" ).get( 0 ).getName() );
  }

  @Test
  public void testSetUsers() throws UserRoleException {
    UserRoleDaoMock userRoleDao = PentahoSystem.get( UserRoleDaoMock.class, USER_ROLE_DAO_TXN, null );

    IUserRoleWebService service = getUserRoleWebService();
    mockUserAsAdmin( false /* non-admin user */ );
    ProxyPentahoRole roleObj = new ProxyPentahoRole( "testRole1" );

    ProxyPentahoUser[] usersObj = new ProxyPentahoUser[1];
    usersObj[0] = new ProxyPentahoUser();
    usersObj[0].setName( "test2" );

    try {
      service.setUsers( roleObj, usersObj );
      Assert.fail();
    } catch ( UserRoleException e ) {
      Assert.assertTrue( "ERROR_0001 not found in " + e.getMessage(), e.getMessage().indexOf( "ERROR_0001" ) >= 0 );
    }

    mockUserAsAdmin( true /* admin user */ );

    Assert.assertEquals( "test1", userRoleDao.getRoleMembers( null, "testRole1" ).get( 0 ).getUsername() );

    service.setUsers( roleObj, usersObj );

    Assert.assertEquals( "test2", userRoleDao.getRoleMembers( null, "testRole1" ).get( 0 ).getUsername() );
  }

  @Test
  public void testUpdateUser() throws UserRoleException {
    UserRoleDaoMock userRoleDao = PentahoSystem.get( UserRoleDaoMock.class, USER_ROLE_DAO_TXN, null );

    IUserRoleWebService service = getUserRoleWebService();
    mockUserAsAdmin( false /* non-admin user */ );
    ProxyPentahoUser userObj = new ProxyPentahoUser();
    userObj.setName( "test1" );
    userObj.setDescription( "testUpdateUser" );
    userObj.setPassword( "newpass" );

    try {
      service.updateUser( userObj );
      Assert.fail();
    } catch ( UserRoleException e ) {
      Assert.assertTrue( "ERROR_0001 not found in " + e.getMessage(), e.getMessage().indexOf( "ERROR_0001" ) >= 0 );
    }

    mockUserAsAdmin( true /* admin user */ );

    Assert.assertEquals( "test", userRoleDao.getUser( null, "test1" ).getDescription() );

    service.updateUser( userObj );

    Assert.assertEquals( "testUpdateUser", userRoleDao.getUser( null, "test1" ).getDescription() );
  }

  @Test
  public void testUpdateRoleObject() throws UserRoleException {
    UserRoleDaoMock userRoleDao = PentahoSystem.get( UserRoleDaoMock.class, USER_ROLE_DAO_TXN, null );

    IUserRoleWebService service = getUserRoleWebService();
    mockUserAsAdmin( false /* non-admin user */ );
    ProxyPentahoRole roleObj = new ProxyPentahoRole( "testRole1" );
    roleObj.setDescription( "testUpdateRoleObject" );

    try {
      service.updateRoleObject( roleObj );
      Assert.fail();
    } catch ( UserRoleException e ) {
      Assert.assertTrue( "ERROR_0001 not found in " + e.getMessage(), e.getMessage().indexOf( "ERROR_0001" ) >= 0 );
    }

    mockUserAsAdmin( true /* admin user */ );

    Assert.assertEquals( "test role", userRoleDao.getRole( null, "testRole1" ).getDescription() );

    service.updateRoleObject( roleObj );

    Assert.assertEquals( "testUpdateRoleObject", userRoleDao.getRole( null, "testRole1" ).getDescription() );
  }

  @Test
  public void testUpdateRole() throws UserRoleException {
    UserRoleDaoMock userRoleDao = PentahoSystem.get( UserRoleDaoMock.class, USER_ROLE_DAO_TXN, null );

    IUserRoleWebService service = getUserRoleWebService();
    mockUserAsAdmin( false /* non-admin user */ );
    ProxyPentahoRole roleObj = new ProxyPentahoRole( "testRole1" );
    roleObj.setDescription( "testUpdateRoleObject" );
    List<String> usernames = new ArrayList<String>();
    try {
      service.updateRole( "testRole1", "testUpdateRoleObject", usernames );
      Assert.fail();
    } catch ( UserRoleException e ) {
      Assert.assertTrue( "ERROR_0001 not found in " + e.getMessage(), e.getMessage().indexOf( "ERROR_0001" ) >= 0 );
    }

    mockUserAsAdmin( true /* admin user */ );

    Assert.assertEquals( "test role", userRoleDao.getRole( null, "testRole1" ).getDescription() );

    service.updateRole( "testRole1", "testUpdateRoleObject", usernames );

    Assert.assertEquals( "testUpdateRoleObject", userRoleDao.getRole( null, "testRole1" ).getDescription() );
  }

  protected String getSolutionPath() {
    return SOLUTION_PATH;
  }

  @Test
  public void testCreateDuplicateRole() throws UserRoleException {
    UserRoleDaoMock userRoleDao = Mockito.mock( UserRoleDaoMock.class );
    Mockito.doThrow( new AlreadyExistsException( "That role already exists." ) ).when( userRoleDao ).
            createRole( Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any() );

    UserRoleWebService userRoleWebService = new UserRoleWebService() {
      @Override
      protected IUserRoleDao getDao() throws UserRoleException {
        return userRoleDao;
      }
    };

    ProxyPentahoRole proxyPentahoRole = new ProxyPentahoRole( "testrole" );
    try {
      userRoleWebService.createRole( proxyPentahoRole );
      Assert.fail();
    } catch ( AlreadyExistsException e ) {
      Assert.assertEquals( 0, e.getStackTrace().length );
    }
  }

  @Test
  public void testCreateDuplicateUser() throws UserRoleException {
    UserRoleDaoMock userRoleDao = Mockito.mock( UserRoleDaoMock.class );
    Mockito.doThrow( new AlreadyExistsException( "That user name already exists." ) ).when( userRoleDao ).
            createUser(  Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any() );

    UserRoleWebService userRoleWebService = new UserRoleWebService() {
      @Override
      protected IUserRoleDao getDao() throws UserRoleException {
        return userRoleDao;
      }
    };

    ProxyPentahoUser proxyPentahoUser = new ProxyPentahoUser();
    proxyPentahoUser.setName( "test" );
    proxyPentahoUser.setEnabled( true );
    proxyPentahoUser.setPassword( "test" );
    proxyPentahoUser.setDescription( "testing" );
    try {
      userRoleWebService.createUser( proxyPentahoUser );
      Assert.fail();
    } catch ( AlreadyExistsException e ) {
      Assert.assertEquals( 0, e.getStackTrace().length );
    }
  }

  @Test
  public void testCreateBlankRole() throws UserRoleException {
    UserRoleWebService userRoleWebService = new UserRoleWebService();
    ProxyPentahoRole proxyPentahoRole = new ProxyPentahoRole();
    try {
      userRoleWebService.createRole( proxyPentahoRole );
      Assert.fail();
    } catch ( UserRoleException e ) {
      Assert.assertEquals( 0, e.getStackTrace().length );
    }
  }

  @Test
  public void testCreateBlankUser() throws UserRoleException {
    UserRoleWebService userRoleWebService = new UserRoleWebService();
    ProxyPentahoUser proxyPentahoUser = new ProxyPentahoUser();
    try {
      userRoleWebService.createUser( proxyPentahoUser );
      Assert.fail();
    } catch ( UserRoleException e ) {
      Assert.assertEquals( 0, e.getStackTrace().length );
    }
  }

}
