package org.pentaho.platform.engine.security.userroledao.ws;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.IAclHolder;
import org.pentaho.platform.api.engine.IAclVoter;
import org.pentaho.platform.api.engine.IPentahoAclEntry;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.security.userroledao.AlreadyExistsException;
import org.pentaho.platform.engine.security.userroledao.IPentahoRole;
import org.pentaho.platform.engine.security.userroledao.IPentahoUser;
import org.pentaho.platform.engine.security.userroledao.IUserRoleDao;
import org.pentaho.platform.engine.security.userroledao.NotFoundException;
import org.pentaho.platform.engine.security.userroledao.PentahoRole;
import org.pentaho.platform.engine.security.userroledao.PentahoUser;
import org.pentaho.platform.engine.security.userroledao.UncategorizedUserRoleDaoException;
import org.pentaho.test.platform.engine.core.MicroPlatform;
import org.springframework.dao.DataAccessException;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.acl.AclEntry;
import org.springframework.security.providers.encoding.PasswordEncoder;

@SuppressWarnings("nls")
public class UserRoleWebServiceTest {
  private MicroPlatform microPlatform;
  private static boolean isAdmin = false;
  private static List<IPentahoUser> users = new ArrayList<IPentahoUser>();
  private static List<IPentahoRole> roles = new ArrayList<IPentahoRole>();
  
  public static class UserRoleDaoMock implements IUserRoleDao {

    public void createRole(IPentahoRole newRole) throws AlreadyExistsException, UncategorizedUserRoleDaoException {
      roles.add(newRole);
    }

    public void createUser(IPentahoUser newUser) throws AlreadyExistsException, UncategorizedUserRoleDaoException {
      users.add(newUser);
    }

    public void deleteRole(IPentahoRole role) throws NotFoundException, UncategorizedUserRoleDaoException {
      roles.remove(role);
    }

    public void deleteUser(IPentahoUser user) throws NotFoundException, UncategorizedUserRoleDaoException {
      users.remove(user);
    }

    public IPentahoRole getRole(String name) throws UncategorizedUserRoleDaoException {
      for (IPentahoRole role : roles) {
        if (role.getName().equals(name)) {
          return role;
        }
      }
      return null;
    }

    public List<IPentahoRole> getRoles() throws UncategorizedUserRoleDaoException {
      return roles;
    }

    public IPentahoUser getUser(String name) throws UncategorizedUserRoleDaoException {
      for (IPentahoUser user : users) {
        if (user.getUsername().equals(name)) {
          return user;
        }
      }
      return null;
    }

    public List<IPentahoUser> getUsers() throws UncategorizedUserRoleDaoException {
      return users;
    }

    public void updateRole(IPentahoRole role) throws NotFoundException, UncategorizedUserRoleDaoException {
      // TODO Auto-generated method stub
      
    }

    public void updateUser(IPentahoUser user) throws NotFoundException, UncategorizedUserRoleDaoException {
      // TODO Auto-generated method stub
      
    }
    
  }
  
  public static class AclVoterMock implements IAclVoter {

    public GrantedAuthority getAdminRole() {
      return null;
    }

    public IPentahoAclEntry getEffectiveAcl(IPentahoSession session, IAclHolder holder) {
      return null;
    }

    public AclEntry[] getEffectiveAcls(IPentahoSession session, IAclHolder holder) {
      return null;
    }

    public boolean hasAccess(IPentahoSession session, IAclHolder holder, int mask) {
      return false;
    }

    public boolean isGranted(IPentahoSession session, GrantedAuthority role) {
      return false;
    }

    public boolean isPentahoAdministrator(IPentahoSession session) {
      return isAdmin;
    }

    public void setAdminRole(GrantedAuthority value) {
      // TODO Auto-generated method stub
      
    }
  }
  
  public static class PasswordEncoderMock implements PasswordEncoder {

    public String encodePassword(String rawPass, Object salt) throws DataAccessException {
      return rawPass;
    }

    public boolean isPasswordValid(String encPass, String rawPass, Object salt) throws DataAccessException {
      return true;
    }
    
  }
  
  
  @Before
  public void init0() {
    microPlatform = new MicroPlatform();
    microPlatform.define("txnUserRoleDao", UserRoleDaoMock.class);
    microPlatform.define(IAclVoter.class, AclVoterMock.class);
    microPlatform.define("passwordEncoder", PasswordEncoderMock.class);

    PentahoUser testUser1 = new PentahoUser("test1", "test", "test", true);
    PentahoUser testUser2 = new PentahoUser("test2", "test", "test", true);
    
    PentahoRole testRole1 = new PentahoRole("testRole1", "test role");
    PentahoRole testRole2 = new PentahoRole("testRole2", "test role");
    users.clear();
    users.add(testUser1);
    users.add(testUser2);
    roles.clear();
    roles.add(testRole1);
    roles.add(testRole2);
    
    testUser1.addRole(testRole1);
    testRole1.addUser(testUser1);
    testUser2.addRole(testRole2);
    testRole2.addUser(testUser2);
    
    isAdmin = false;
  }
  
  public IUserRoleWebService getUserRoleWebService() {
    return new UserRoleWebService();
  }
  
  @Test
  public void testGetUserRoleSecurityInfo() throws Exception {
    IUserRoleWebService service = getUserRoleWebService();
    
    try {
      service.getUserRoleSecurityInfo();
      Assert.fail();
    } catch (UserRoleException e) {
      // should this be 0001, not admin?
      Assert.assertTrue(e.getMessage().indexOf("ERROR_0001") >= 0);
    }
    
    isAdmin = true;
    
    UserRoleSecurityInfo info = service.getUserRoleSecurityInfo();
    
    Assert.assertNotNull(info);
    Assert.assertEquals(2, info.getRoles().size());
    Assert.assertEquals(2, info.getUsers().size());
    Assert.assertEquals(2, info.getAssignments().size());
  }
  
  @Test
  public void testCreateRole() throws Exception {
    IUserRoleWebService service = getUserRoleWebService();
    ProxyPentahoRole role = new ProxyPentahoRole("role");
    role.setDescription("testing");
    try {
      service.createRole(role);
      Assert.fail();
    } catch (UserRoleException e) {
      Assert.assertTrue("ERROR_0001 not found in " + e.getMessage(), e.getMessage().indexOf("ERROR_0001") >= 0);
    }
    
    isAdmin = true;
    
    service.createRole(role);
    // the last role should have the same name and description
    IPentahoRole roleVerified = roles.get(roles.size() - 1);
    Assert.assertEquals("role", roleVerified.getName());
    Assert.assertEquals("testing", roleVerified.getDescription());
  }
  
  @Test
  public void testCreateUser() throws Exception {
    IUserRoleWebService service = getUserRoleWebService();
    ProxyPentahoUser user = new ProxyPentahoUser();
    user.setName("test");
    user.setEnabled(true);
    user.setPassword("test");
    user.setDescription("testing");
    try {
      service.createUser(user);
      Assert.fail();
    } catch (UserRoleException e) {
      Assert.assertTrue("ERROR_0001 not found in " + e.getMessage(), e.getMessage().indexOf("ERROR_0001") >= 0);
    }
    
    isAdmin = true;
    
    service.createUser(user);
    // the last role should have the same name and description
    IPentahoUser userVerified = users.get(users.size() - 1);
    Assert.assertEquals("test", userVerified.getUsername());
    Assert.assertEquals("test", userVerified.getPassword());
    Assert.assertEquals(true, userVerified.isEnabled());
    Assert.assertEquals("testing", userVerified.getDescription());
  }
  
  @Test
  public void testGetUsers() throws Exception {
    IUserRoleWebService service = getUserRoleWebService();
    try {
      service.getUsers();
      Assert.fail();
    } catch (UserRoleException e) {
      // should this be 0001, not admin?
      Assert.assertTrue("ERROR_0001 not found in " + e.getMessage(), e.getMessage().indexOf("ERROR_0001") >= 0);
    }
    
    isAdmin = true;
    
    ProxyPentahoUser userObjs[] = service.getUsers();
    
    Assert.assertNotNull(userObjs);
    Assert.assertEquals(2, userObjs.length);
    
  }

  @Test
  public void testGetRoles() throws Exception {
    IUserRoleWebService service = getUserRoleWebService();
    try {
      service.getRoles();
      Assert.fail();
    } catch (UserRoleException e) {
      Assert.assertTrue("ERROR_0001 not found in " + e.getMessage(), e.getMessage().indexOf("ERROR_0001") >= 0);
    }
    
    isAdmin = true;
    
    ProxyPentahoRole roleObjs[] = service.getRoles();
    
    Assert.assertNotNull(roleObjs);
    Assert.assertEquals(2, roleObjs.length);
    
  }

  @Test
  public void testGetUser() throws Exception {
    IUserRoleWebService service = getUserRoleWebService();
    try {
      service.getUser(null);
      Assert.fail();
    } catch (UserRoleException e) {
      Assert.assertTrue("ERROR_0001 not found in " + e.getMessage(), e.getMessage().indexOf("ERROR_0001") >= 0);
    }
    
    isAdmin = true;
    
    ProxyPentahoUser userObj = service.getUser("test1");
    
    Assert.assertNotNull(userObj);
    Assert.assertEquals("test1", userObj.getName());
    
  }

  @Test
  public void testDeleteRoles() throws Exception {
    IUserRoleWebService service = getUserRoleWebService();
    ProxyPentahoRole rolesObj[] = new ProxyPentahoRole[1];
    rolesObj[0] = new ProxyPentahoRole("testRole1");
    try {
      service.deleteRoles(rolesObj);
      Assert.fail();
    } catch (UserRoleException e) {
      Assert.assertTrue("ERROR_0001 not found in " + e.getMessage(), e.getMessage().indexOf("ERROR_0001") >= 0);
    }
    
    isAdmin = true;
    
    service.deleteRoles(rolesObj);
    
    Assert.assertEquals(1, roles.size());
  }
  
  @Test
  public void testDeleteUsers() throws Exception {
    IUserRoleWebService service = getUserRoleWebService();
    ProxyPentahoUser usersObj[] = new ProxyPentahoUser[1];
    usersObj[0] = new ProxyPentahoUser();
    usersObj[0].setName("test1");
    try {
      service.deleteUsers(usersObj);
      Assert.fail();
    } catch (UserRoleException e) {
      Assert.assertTrue("ERROR_0001 not found in " + e.getMessage(), e.getMessage().indexOf("ERROR_0001") >= 0);
    }
    
    isAdmin = true;
    
    service.deleteUsers(usersObj);
    
    Assert.assertEquals(1, users.size());
  }
  
  @Test
  public void testGetRolesForUser() throws UserRoleException {
    IUserRoleWebService service = getUserRoleWebService();
    ProxyPentahoUser userObj = new ProxyPentahoUser();
    userObj.setName("test1");
    try {
      service.getRolesForUser(userObj);
      Assert.fail();
    } catch (UserRoleException e) {
      Assert.assertTrue("ERROR_0001 not found in " + e.getMessage(), e.getMessage().indexOf("ERROR_0001") >= 0);
    }
    
    isAdmin = true;
    
    ProxyPentahoRole roles[] = service.getRolesForUser(userObj);
    
    Assert.assertEquals(1, roles.length);
  }
  
  @Test
  public void testGetUsersForRole() throws UserRoleException {
    IUserRoleWebService service = getUserRoleWebService();
    ProxyPentahoRole roleObj = new ProxyPentahoRole("testRole1");
    try {
      service.getUsersForRole(roleObj);
      Assert.fail();
    } catch (UserRoleException e) {
      Assert.assertTrue("ERROR_0001 not found in " + e.getMessage(), e.getMessage().indexOf("ERROR_0001") >= 0);
    }
    
    isAdmin = true;
    
    ProxyPentahoUser userObjs[] = service.getUsersForRole(roleObj);
    
    Assert.assertEquals(1, userObjs.length);
  }

  @Test
  public void testSetRoles() throws UserRoleException {
    IUserRoleWebService service = getUserRoleWebService();
    ProxyPentahoUser userObj = new ProxyPentahoUser();
    userObj.setName("test1");

    ProxyPentahoRole rolesObj[] = new ProxyPentahoRole[1];
    rolesObj[0] = new ProxyPentahoRole("testRole2");

    try {
      service.setRoles(userObj, rolesObj);
      Assert.fail();
    } catch (UserRoleException e) {
      Assert.assertTrue("ERROR_0001 not found in " + e.getMessage(), e.getMessage().indexOf("ERROR_0001") >= 0);
    }
    
    isAdmin = true;
    
    Assert.assertEquals("testRole1", ((IPentahoRole)users.get(0).getRoles().toArray()[0]).getName());
    
    service.setRoles(userObj, rolesObj);
    
    Assert.assertEquals("testRole2", ((IPentahoRole)users.get(0).getRoles().toArray()[0]).getName());
  }
  
  @Test
  public void testSetUsers() throws UserRoleException {
    IUserRoleWebService service = getUserRoleWebService();
    ProxyPentahoRole roleObj = new ProxyPentahoRole("testRole1");

    ProxyPentahoUser usersObj[] = new ProxyPentahoUser[1];
    usersObj[0] = new ProxyPentahoUser();
    usersObj[0].setName("test2");

    try {
      service.setUsers(roleObj, usersObj);
      Assert.fail();
    } catch (UserRoleException e) {
      Assert.assertTrue("ERROR_0001 not found in " + e.getMessage(), e.getMessage().indexOf("ERROR_0001") >= 0);
    }
    
    isAdmin = true;
    
    Assert.assertEquals("test1", ((IPentahoUser)roles.get(0).getUsers().toArray()[0]).getUsername());
    
    service.setUsers(roleObj, usersObj);
    
    Assert.assertEquals("test2", ((IPentahoUser)roles.get(0).getUsers().toArray()[0]).getUsername());
  }
  
  @Test
  public void testUpdateUser() throws UserRoleException {
    IUserRoleWebService service = getUserRoleWebService();
    ProxyPentahoUser userObj = new ProxyPentahoUser();
    userObj.setName("test1");
    userObj.setDescription("testUpdateUser");
    userObj.setPassword("newpass");
    
    try {
      service.updateUser(userObj);
      Assert.fail();
    } catch (UserRoleException e) {
      Assert.assertTrue("ERROR_0001 not found in " + e.getMessage(), e.getMessage().indexOf("ERROR_0001") >= 0);
    }
    
    isAdmin = true;
    
    Assert.assertEquals("test", users.get(0).getDescription());

    service.updateUser(userObj);
    
    Assert.assertEquals("testUpdateUser", users.get(0).getDescription());
  }
  
  @Test
  public void testUpdateRoleObject() throws UserRoleException {
    IUserRoleWebService service = getUserRoleWebService();
    ProxyPentahoRole roleObj = new ProxyPentahoRole("testRole1");
    roleObj.setDescription("testUpdateRoleObject");
    
    try {
      service.updateRoleObject(roleObj);
      Assert.fail();
    } catch (UserRoleException e) {
      Assert.assertTrue("ERROR_0001 not found in " + e.getMessage(), e.getMessage().indexOf("ERROR_0001") >= 0);
    }
    
    isAdmin = true;
    
    Assert.assertEquals("test role", roles.get(0).getDescription());

    service.updateRoleObject(roleObj);
    
    Assert.assertEquals("testUpdateRoleObject", roles.get(0).getDescription());
  }

  @Test
  public void testUpdateRole() throws UserRoleException {
    IUserRoleWebService service = getUserRoleWebService();
    ProxyPentahoRole roleObj = new ProxyPentahoRole("testRole1");
    roleObj.setDescription("testUpdateRoleObject");
    List<String> usernames = new ArrayList<String>();
    try {
      service.updateRole("testRole1", "testUpdateRoleObject", usernames);
      Assert.fail();
    } catch (UserRoleException e) {
      Assert.assertTrue("ERROR_0001 not found in " + e.getMessage(), e.getMessage().indexOf("ERROR_0001") >= 0);
    }
    
    isAdmin = true;
    
    Assert.assertEquals("test role", roles.get(0).getDescription());

    service.updateRole("testRole1", "testUpdateRoleObject", usernames);
    
    Assert.assertEquals("testUpdateRoleObject", roles.get(0).getDescription());
  }
  
}
