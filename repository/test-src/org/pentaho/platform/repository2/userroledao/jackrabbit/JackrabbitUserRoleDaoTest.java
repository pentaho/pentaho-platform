/*
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
 * Copyright 2007 - 2009 Pentaho Corporation.  All rights reserved.
 *
*/
package org.pentaho.platform.repository2.userroledao.jackrabbit;
import static org.junit.Assert.*;
import java.io.File;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.apache.commons.io.FileUtils;
import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.TransientRepository;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.NameFactory;
import org.apache.jackrabbit.spi.commons.name.NameFactoryImpl;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository2.unified.IBackingRepositoryLifecycleManager;
import org.pentaho.platform.api.repository2.unified.ITenantManager;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.security.userroledao.AlreadyExistsException;
import org.pentaho.platform.engine.security.userroledao.DefaultTenantedPrincipleNameUtils;
import org.pentaho.platform.engine.security.userroledao.IPentahoRole;
import org.pentaho.platform.engine.security.userroledao.IPentahoUser;
import org.pentaho.platform.engine.security.userroledao.NotFoundException;
import org.pentaho.platform.engine.security.userroledao.PentahoRole;
import org.pentaho.platform.engine.security.userroledao.PentahoUser;
import org.pentaho.platform.engine.security.userroledao.hibernate.HibernateUserRoleDao;
import org.pentaho.platform.repository2.unified.ServerRepositoryPaths;
import org.pentaho.platform.repository2.unified.jcr.SimpleJcrTestUtils;
import org.pentaho.test.platform.engine.core.MicroPlatform;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.extensions.jcr.JcrTemplate;
import org.springframework.extensions.jcr.SessionFactory;
import org.springframework.extensions.jcr.SessionHolder;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.security.userdetails.User;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Unit test for {@link HibernateUserRoleDao}.
 * 
 * @author mlowery
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/repository.spring.xml",
    "classpath:/repository-test-override.spring.xml" })
@SuppressWarnings("nls")
public class JackrabbitUserRoleDaoTest implements ApplicationContextAware {
  
  public static final String MAIN_TENANT_1 = "maintenant1";
  public static final String SUB_TENANT1_1 = "subtenant11";
  public static final String SUB_TENANT1_2 = "subtenant12";
  public static final String MAIN_TENANT_2 = "maintenant2";
  public static final String SUB_TENANT2_1 = "subtenant21";
  public static final String SUB_TENANT2_2 = "subtenant22";

  public static final String MAIN_TENANT_1_PATH = RepositoryFile.SEPARATOR + ServerRepositoryPaths.getPentahoRootFolderName() + RepositoryFile.SEPARATOR + "maintenant1";
  public static final String SUB_TENANT1_1_PATH = RepositoryFile.SEPARATOR + ServerRepositoryPaths.getPentahoRootFolderName() + RepositoryFile.SEPARATOR + "maintenant1" + RepositoryFile.SEPARATOR + "subtenant11";
  public static final String SUB_TENANT1_2_PATH = RepositoryFile.SEPARATOR + ServerRepositoryPaths.getPentahoRootFolderName() + RepositoryFile.SEPARATOR + "maintenant1" + RepositoryFile.SEPARATOR + "subtenant12";
  public static final String MAIN_TENANT_2_PATH = RepositoryFile.SEPARATOR + ServerRepositoryPaths.getPentahoRootFolderName() + RepositoryFile.SEPARATOR + "maintenant2";
  public static final String SUB_TENANT2_1_PATH = RepositoryFile.SEPARATOR + ServerRepositoryPaths.getPentahoRootFolderName() + RepositoryFile.SEPARATOR + "maintenant2" + RepositoryFile.SEPARATOR + "subtenant21";
  public static final String SUB_TENANT2_2_PATH = RepositoryFile.SEPARATOR + ServerRepositoryPaths.getPentahoRootFolderName() + RepositoryFile.SEPARATOR + "maintenant2" + RepositoryFile.SEPARATOR + "subtenant22";


  public static final String PASSWORD_1 = "password1"; //$NON-NLS-1$
  public static final String PASSWORD_2 = "password2"; //$NON-NLS-1$
  public static final String PASSWORD_3 = "password3"; //$NON-NLS-1$
  public static final String PASSWORD_4 = "password4"; //$NON-NLS-1$
  public static final String PASSWORD_5 = "password5"; //$NON-NLS-1$
  public static final String PASSWORD_6 = "password6"; //$NON-NLS-1$
  public static final String PASSWORD_7 = "password7"; //$NON-NLS-1$
  public static final String PASSWORD_8 = "password8"; //$NON-NLS-1$

  public static final String USER_1 = "joe"; //$NON-NLS-1$
  public static final String USER_2 = "jim"; //$NON-NLS-1$
  public static final String USER_3 = "sally"; //$NON-NLS-1$
  public static final String USER_4 = "suzy"; //$NON-NLS-1$
  public static final String USER_5 = "nancy"; //$NON-NLS-1$
  public static final String USER_6 = "john"; //$NON-NLS-1$
  public static final String USER_7 = "jane"; //$NON-NLS-1$
  public static final String USER_8 = "jerry"; //$NON-NLS-1$
  public static final String UNKNOWN_USER = "unknownUser"; //$NON-NLS-1$
  

  public static final String UNKNOWN_TENANT = "unknownTenant"; //$NON-NLS-1$
  
  public static final String ROLE_1 = "SalesMgr"; //$NON-NLS-1$
  public static final String ROLE_2 = "IT"; //$NON-NLS-1$
  public static final String ROLE_3 = "Sales"; //$NON-NLS-1$
  public static final String ROLE_4 = "Developer"; //$NON-NLS-1$
  public static final String ROLE_5 = "CEO"; //$NON-NLS-1$
  public static final String ROLE_6 = "Finance"; //$NON-NLS-1$
  public static final String ROLE_7 = "Marketing"; //$NON-NLS-1$
  public static final String ROLE_8 = "RegionalMgr"; //$NON-NLS-1$
  public static final String UNKNOWN_ROLE = "unknownRole"; //$NON-NLS-1$
  
  public static final String USER_DESCRIPTION_1 = "User Description 1"; //$NON-NLS-1$
  public static final String USER_DESCRIPTION_2 = "User Description 2"; //$NON-NLS-1$
  public static final String USER_DESCRIPTION_3 = "User Description 3"; //$NON-NLS-1$
  public static final String USER_DESCRIPTION_4 = "User Description 4"; //$NON-NLS-1$
  public static final String USER_DESCRIPTION_5 = "User Description 5"; //$NON-NLS-1$
  public static final String USER_DESCRIPTION_6 = "User Description 6"; //$NON-NLS-1$
  public static final String USER_DESCRIPTION_7 = "User Description 7"; //$NON-NLS-1$
  public static final String USER_DESCRIPTION_8 = "User Description 8"; //$NON-NLS-1$
  public static final String ROLE_DESCRIPTION_1 = "Role Description 1"; //$NON-NLS-1$
  public static final String ROLE_DESCRIPTION_2 = "Role Description 2"; //$NON-NLS-1$
  public static final String ROLE_DESCRIPTION_3 = "Role Description 3"; //$NON-NLS-1$
  public static final String ROLE_DESCRIPTION_4 = "Role Description 4"; //$NON-NLS-1$
  public static final String ROLE_DESCRIPTION_5 = "Role Description 5"; //$NON-NLS-1$
  public static final String ROLE_DESCRIPTION_6 = "Role Description 6"; //$NON-NLS-1$
  public static final String ROLE_DESCRIPTION_7 = "Role Description 7"; //$NON-NLS-1$
  public static final String ROLE_DESCRIPTION_8 = "Role Description 8"; //$NON-NLS-1$

  NameFactory NF = NameFactoryImpl.getInstance();
  Name P_PRINCIPAL_NAME = NF.create(Name.NS_REP_URI, "principalName"); //$NON-NLS-1$
  private boolean startupCalled;
  Repository repository;
  File repositoryDir;
  String pPrincipalName;
  JackrabbitUserRoleDao userRoleDao;
  private ITenantManager tenantManager;
  private String repositoryAdminUsername;
  private String tenantAdminAuthorityNamePattern;
  private String tenantAuthenticatedAuthorityNamePattern;
  private JcrTemplate testJcrTemplate;
  private IBackingRepositoryLifecycleManager manager;
  private IAuthorizationPolicy authorizationPolicy;
  private MicroPlatform mp;


  @BeforeClass
  public static void setUpClass() throws Exception {
    // folder cannot be deleted at teardown shutdown hooks have not yet necessarily completed
    // parent folder must match jcrRepository.homeDir bean property in repository-test-override.spring.xml
    FileUtils.deleteDirectory(new File("/tmp/jackrabbit-test-TRUNK"));
    PentahoSessionHolder.setStrategyName(PentahoSessionHolder.MODE_GLOBAL);
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
    PentahoSessionHolder.setStrategyName(PentahoSessionHolder.MODE_INHERITABLETHREADLOCAL);
  }

  @Before
  public void setUp() throws Exception {
    mp = new MicroPlatform();
    // used by DefaultPentahoJackrabbitAccessControlHelper
    mp.defineInstance(IAuthorizationPolicy.class, authorizationPolicy);
    mp.defineInstance(ITenantManager.class, tenantManager);
    

    // Start the micro-platform
    mp.start();
    logout();
    startupCalled = true;

    repositoryDir = new File(new File(System.getProperty("java.io.tmpdir")), "jackrabbitRepo-" + System.currentTimeMillis()); //$NON-NLS-1$  //$NON-NLS-2$
    repositoryDir.mkdir();
    repository = new TransientRepository(repositoryDir); 
    SessionFactory sessionFactory = new SessionFactory() {      
      public SessionHolder getSessionHolder(Session arg0) {
        return new SessionHolder(arg0);
      }
      
      public Session getSession() throws RepositoryException {
        Session session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));
        return session;
      }
    };
    
    JcrTemplate jcrTemplate =  new JcrTemplate(sessionFactory);
    jcrTemplate.setAllowCreate(true);
    jcrTemplate.setExposeNativeSession(true);
    
    JackrabbitUserRoleDao jackrabbitUserRoleDao = new JackrabbitUserRoleDao(jcrTemplate);
    jackrabbitUserRoleDao.setTenantedUserNameUtils(new DefaultTenantedPrincipleNameUtils());
    jackrabbitUserRoleDao.setTenantedRoleNameUtils(new DefaultTenantedPrincipleNameUtils());
    
    userRoleDao = jackrabbitUserRoleDao;
  }

  @After
  public void tearDown() throws Exception {
    repositoryDir.delete();
    // null out fields to get back memory
    authorizationPolicy = null;
    loginAsRepositoryAdmin();
    SimpleJcrTestUtils.deleteItem(testJcrTemplate, ServerRepositoryPaths.getPentahoRootFolderPath());
    logout();
    repositoryAdminUsername = null;
    tenantAdminAuthorityNamePattern = null;
    tenantAuthenticatedAuthorityNamePattern = null;
    authorizationPolicy = null;
    testJcrTemplate = null;
    if (startupCalled) {
      manager.shutdown();
    }

    tenantManager = null;
    
  }

  protected void loginAsRepositoryAdmin() {
    StandaloneSession pentahoSession = new StandaloneSession(repositoryAdminUsername);
    pentahoSession.setAuthenticated(repositoryAdminUsername);
    final GrantedAuthority[] repositoryAdminAuthorities = new GrantedAuthority[0];
    final String password = "ignored";
    UserDetails repositoryAdminUserDetails = new User(repositoryAdminUsername, password, true, true, true, true,
        repositoryAdminAuthorities);
    Authentication repositoryAdminAuthentication = new UsernamePasswordAuthenticationToken(repositoryAdminUserDetails,
        password, repositoryAdminAuthorities);
    PentahoSessionHolder.setSession(pentahoSession);
    // this line necessary for Spring Security's MethodSecurityInterceptor
    SecurityContextHolder.getContext().setAuthentication(repositoryAdminAuthentication);
  }

  protected void logout() {
    PentahoSessionHolder.removeSession();
    SecurityContextHolder.getContext().setAuthentication(null);
  }

  protected void login(final String username, final String tenantId) {
    login(username, tenantId, false);
  }

  /**
   * Logs in with given username.
   *
   * @param username username of user
   * @param tenantId tenant to which this user belongs
   * @tenantAdmin true to add the tenant admin authority to the user's roles
   */
  protected void login(final String username, final String tenantId, final boolean tenantAdmin) {
    StandaloneSession pentahoSession = new StandaloneSession(username);
    pentahoSession.setAuthenticated(username);
    pentahoSession.setAttribute(IPentahoSession.TENANT_ID_KEY, tenantId);
    final String password = "password";

    List<GrantedAuthority> authList = new ArrayList<GrantedAuthority>();
    authList.add(new GrantedAuthorityImpl(MessageFormat.format(tenantAuthenticatedAuthorityNamePattern, tenantId)));
    if (tenantAdmin) {
      authList.add(new GrantedAuthorityImpl(MessageFormat.format(tenantAdminAuthorityNamePattern, tenantId)));
    }
    GrantedAuthority[] authorities = authList.toArray(new GrantedAuthority[0]);
    UserDetails userDetails = new User(username, password, true, true, true, true, authorities);
    Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, password, authorities);
    PentahoSessionHolder.setSession(pentahoSession);
    // this line necessary for Spring Security's MethodSecurityInterceptor
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
    manager = (IBackingRepositoryLifecycleManager) applicationContext.getBean("backingRepositoryLifecycleManager");
    SessionFactory jcrSessionFactory = (SessionFactory) applicationContext.getBean("jcrSessionFactory");
    testJcrTemplate = new JcrTemplate(jcrSessionFactory);
    testJcrTemplate.setAllowCreate(true);
    testJcrTemplate.setExposeNativeSession(true);
    repositoryAdminUsername = (String) applicationContext.getBean("repositoryAdminUsername");
    tenantAuthenticatedAuthorityNamePattern = (String) applicationContext
        .getBean("tenantAuthenticatedAuthorityNamePattern");
    tenantAdminAuthorityNamePattern = (String) applicationContext.getBean("tenantAdminAuthorityNamePattern");
    authorizationPolicy = (IAuthorizationPolicy) applicationContext
        .getBean("authorizationPolicy");
    tenantManager = (ITenantManager) applicationContext.getBean("defaultTenantManager");
  }

  @Test
  public void testCreateUser() throws Exception {
    Serializable systemTenantId = tenantManager.createSystemTenant(ServerRepositoryPaths.getPentahoRootFolderName());
    Serializable mainTenant_1_Id = tenantManager.createTenant(systemTenantId, MAIN_TENANT_1);
    Serializable mainTenant_2_Id = tenantManager.createTenant(systemTenantId, MAIN_TENANT_2);
    Serializable subTenant1_1_Id = tenantManager.createTenant(mainTenant_1_Id, SUB_TENANT1_1);
    Serializable subTenant1_2_Id = tenantManager.createTenant(mainTenant_1_Id, SUB_TENANT1_2);
    Serializable subTenant2_1_Id = tenantManager.createTenant(mainTenant_2_Id, SUB_TENANT2_1);
    Serializable subTenant2_2_Id = tenantManager.createTenant(mainTenant_2_Id, SUB_TENANT2_2);

    login("systemTenantUser", MAIN_TENANT_1_PATH);
    List<IPentahoUser> users = userRoleDao.getUsers(MAIN_TENANT_1_PATH);
    IPentahoUser pentahoUser = userRoleDao.createUser(MAIN_TENANT_1_PATH, USER_1, PASSWORD_1, USER_DESCRIPTION_1, null);
    pentahoUser = userRoleDao.getUser(MAIN_TENANT_1_PATH, USER_1);
    assertEquals(pentahoUser.getTenant(), MAIN_TENANT_1_PATH);
    assertEquals(pentahoUser.getUsername(), USER_1);
    assertEquals(pentahoUser.getDescription(), USER_DESCRIPTION_1);
    assertEquals(pentahoUser.isEnabled(), true);
    logout();
    login("systemTenantUser", SUB_TENANT2_1_PATH);
    try {
      pentahoUser = userRoleDao.createUser(MAIN_TENANT_1_PATH, USER_1, PASSWORD_1, USER_DESCRIPTION_1, null);
      fail("Exception not thrown");
    } catch(Throwable th) {
      assertNotNull(th);
    }
    logout();
    login("systemTenantUser", MAIN_TENANT_1_PATH);

    SessionImpl session = (SessionImpl)repository.login(new SimpleCredentials("admin", "admin".toCharArray())); //$NON-NLS-1$ //$NON-NLS-2$
    traverseNodes(session.getRootNode(), 0);
    
    users = userRoleDao.getUsers(MAIN_TENANT_1_PATH);
    assertTrue(users.size() == 1);
    pentahoUser = users.get(0);    
    assertEquals(pentahoUser.getTenant(), MAIN_TENANT_1_PATH);
    assertEquals(pentahoUser.getUsername(), USER_1);
    assertEquals(pentahoUser.getDescription(), USER_DESCRIPTION_1);
    assertEquals(pentahoUser.isEnabled(), true);
    
    logout();
    login("systemTenantUser", MAIN_TENANT_2_PATH);
    pentahoUser = userRoleDao.createUser(MAIN_TENANT_2_PATH, USER_2, PASSWORD_2, USER_DESCRIPTION_2, null);
    
    logout();
    login("systemTenantUser", SUB_TENANT1_1_PATH);
    try {
      pentahoUser = userRoleDao.createUser(MAIN_TENANT_2_PATH, USER_2, PASSWORD_2, USER_DESCRIPTION_2, null);
      fail("Exception not thrown");
    } catch(Throwable th) {
      assertNotNull(th);
    }

    logout();
    login("systemTenantUser", MAIN_TENANT_2_PATH);

    pentahoUser = userRoleDao.getUser(MAIN_TENANT_2_PATH, USER_2);
    assertEquals(pentahoUser.getTenant(), MAIN_TENANT_2_PATH);
    assertEquals(pentahoUser.getUsername(), USER_2);
    assertEquals(pentahoUser.getDescription(), USER_DESCRIPTION_2);
    assertEquals(pentahoUser.isEnabled(), true);

    users = userRoleDao.getUsers(MAIN_TENANT_2_PATH);
    assertTrue(users.size() == 1);
    pentahoUser = users.get(0);    
    assertEquals(pentahoUser.getTenant(), MAIN_TENANT_2_PATH);
    assertEquals(pentahoUser.getUsername(), USER_2);
    assertEquals(pentahoUser.getDescription(), USER_DESCRIPTION_2);
    assertEquals(pentahoUser.isEnabled(), true);
    
    logout();
    login("systemTenantUser", SUB_TENANT2_1_PATH);

    pentahoUser = userRoleDao.createUser(USER_3 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + SUB_TENANT2_1_PATH, PASSWORD_3, USER_DESCRIPTION_3, null);
    
    pentahoUser = userRoleDao.getUser(USER_3 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + SUB_TENANT2_1_PATH);
    assertEquals(pentahoUser.getTenant(), SUB_TENANT2_1_PATH);
    assertEquals(pentahoUser.getUsername(), USER_3);
    assertEquals(pentahoUser.getDescription(), USER_DESCRIPTION_3);
    assertEquals(pentahoUser.isEnabled(), true);
    
    logout();
    login("systemTenantUser", SUB_TENANT1_1_PATH);

    try {
      pentahoUser = userRoleDao.createUser(SUB_TENANT2_1_PATH, USER_1, PASSWORD_1, USER_DESCRIPTION_1, null);
      fail("Exception not thrown");
    } catch(Throwable th) {
      assertNotNull(th);
    }

    logout();
    login("systemTenantUser", SUB_TENANT2_1_PATH);

    users = userRoleDao.getUsers(SUB_TENANT2_1_PATH);
    assertTrue(users.size() == 1);
    pentahoUser = users.get(0);    
    assertEquals(pentahoUser.getTenant(), SUB_TENANT2_1_PATH);
    assertEquals(pentahoUser.getUsername(), USER_3);
    assertEquals(pentahoUser.getDescription(), USER_DESCRIPTION_3);
    assertEquals(pentahoUser.isEnabled(), true);
    
    logout();
    login("systemTenantUser", SUB_TENANT1_1_PATH);
    
    pentahoUser = userRoleDao.createUser(USER_4 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + SUB_TENANT1_1_PATH, PASSWORD_4, USER_DESCRIPTION_4, null);
    
    pentahoUser = userRoleDao.getUser(USER_4 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + SUB_TENANT1_1_PATH);
    assertEquals(pentahoUser.getTenant(), SUB_TENANT1_1_PATH);
    assertEquals(pentahoUser.getUsername(), USER_4);
    assertEquals(pentahoUser.getDescription(), USER_DESCRIPTION_4);
    assertEquals(pentahoUser.isEnabled(), true);
    
    logout();
    login("systemTenantUser", SUB_TENANT2_1_PATH);

    try {
      pentahoUser = userRoleDao.createUser(SUB_TENANT1_1_PATH, USER_1, PASSWORD_1, USER_DESCRIPTION_1, null);
      fail("Exception not thrown");
    } catch(Throwable th) {
      assertNotNull(th);
    }

    logout();
    login("systemTenantUser", SUB_TENANT1_1_PATH);

    
    users = userRoleDao.getUsers(SUB_TENANT1_1_PATH);
    assertTrue(users.size() == 1);
    pentahoUser = users.get(0);    
    assertEquals(pentahoUser.getTenant(), SUB_TENANT1_1_PATH);
    assertEquals(pentahoUser.getUsername(), USER_4);
    assertEquals(pentahoUser.getDescription(), USER_DESCRIPTION_4);
    assertEquals(pentahoUser.isEnabled(), true);

    logout();
    login("systemTenantUser", MAIN_TENANT_1_PATH);

    try {
      pentahoUser = userRoleDao.createUser(MAIN_TENANT_1_PATH, USER_1, PASSWORD_1, USER_DESCRIPTION_1, null);
      fail("Exception not thrown");
    } catch (AlreadyExistsException e) {
      // Expected exception
    }
    
    try {
      pentahoUser = userRoleDao.createUser(USER_1 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + MAIN_TENANT_1_PATH, PASSWORD_1, USER_DESCRIPTION_1, null);
      fail("Exception not thrown");
    } catch (AlreadyExistsException e) {
      // Expected exception
    }
  }

  @Test
  public void testCreateRole() throws Exception {
    Serializable systemTenantId = tenantManager.createSystemTenant(ServerRepositoryPaths.getPentahoRootFolderName());
    Serializable mainTenant_1_Id = tenantManager.createTenant(systemTenantId, MAIN_TENANT_1);
    Serializable mainTenant_2_Id = tenantManager.createTenant(systemTenantId, MAIN_TENANT_2);
    Serializable subTenant1_1_Id = tenantManager.createTenant(mainTenant_1_Id, SUB_TENANT1_1);
    Serializable subTenant1_2_Id = tenantManager.createTenant(mainTenant_1_Id, SUB_TENANT1_2);
    Serializable subTenant2_1_Id = tenantManager.createTenant(mainTenant_2_Id, SUB_TENANT2_1);
    Serializable subTenant2_2_Id = tenantManager.createTenant(mainTenant_2_Id, SUB_TENANT2_2);

    login("systemTenantUser", MAIN_TENANT_1_PATH);

    IPentahoRole pentahoRole = userRoleDao.createRole(MAIN_TENANT_1_PATH, ROLE_1, ROLE_DESCRIPTION_1, null);
    
    pentahoRole = userRoleDao.getRole(MAIN_TENANT_1_PATH, ROLE_1);
    assertEquals(pentahoRole.getTenant(), MAIN_TENANT_1_PATH);
    assertEquals(pentahoRole.getName(), ROLE_1);
    assertEquals(pentahoRole.getDescription(), ROLE_DESCRIPTION_1);
    
    List<IPentahoRole> roles = userRoleDao.getRoles(MAIN_TENANT_1_PATH);
    assertTrue(roles.size() == 1);
    pentahoRole = roles.get(0);    
    assertEquals(pentahoRole.getTenant(), MAIN_TENANT_1_PATH);
    assertEquals(pentahoRole.getName(), ROLE_1);
    assertEquals(pentahoRole.getDescription(), ROLE_DESCRIPTION_1);
    logout();
    login("systemTenantUser", SUB_TENANT2_1_PATH);
    try {
      pentahoRole = userRoleDao.createRole(MAIN_TENANT_1_PATH, ROLE_1, ROLE_DESCRIPTION_1, null);
      fail("Exception not thrown");
    } catch(Throwable th) {
      assertNotNull(th);
    }
    logout();
    login("systemTenantUser", MAIN_TENANT_2_PATH);

    pentahoRole = userRoleDao.createRole(MAIN_TENANT_2_PATH, ROLE_1, ROLE_DESCRIPTION_2, null);
    
    pentahoRole = userRoleDao.getRole(MAIN_TENANT_2_PATH, ROLE_1);
    assertEquals(pentahoRole.getTenant(), MAIN_TENANT_2_PATH);
    assertEquals(pentahoRole.getName(), ROLE_1);
    assertEquals(pentahoRole.getDescription(), ROLE_DESCRIPTION_2);
    
    roles = userRoleDao.getRoles(MAIN_TENANT_2_PATH);
    assertTrue(roles.size() == 1);
    pentahoRole = roles.get(0);    
    assertEquals(pentahoRole.getTenant(), MAIN_TENANT_2_PATH);
    assertEquals(pentahoRole.getName(), ROLE_1);
    assertEquals(pentahoRole.getDescription(), ROLE_DESCRIPTION_2);

    logout();
    login("systemTenantUser", SUB_TENANT2_1_PATH);
    try {
      pentahoRole = userRoleDao.createRole(MAIN_TENANT_2_PATH, ROLE_1, ROLE_DESCRIPTION_1, null);
      fail("Exception not thrown");
    } catch(Throwable th) {
      assertNotNull(th);
    }

    userRoleDao.createRole(ROLE_3 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + SUB_TENANT2_1_PATH, ROLE_DESCRIPTION_3, null);
    
    pentahoRole = userRoleDao.getRole(ROLE_3 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + SUB_TENANT2_1_PATH);
    assertEquals(pentahoRole.getTenant(), SUB_TENANT2_1_PATH);
    assertEquals(pentahoRole.getName(), ROLE_3);
    assertEquals(pentahoRole.getDescription(), ROLE_DESCRIPTION_3);
    
    roles = userRoleDao.getRoles(SUB_TENANT2_1_PATH);
    assertTrue(roles.size() == 1);
    pentahoRole = roles.get(0);    
    assertEquals(pentahoRole.getTenant(), SUB_TENANT2_1_PATH);
    assertEquals(pentahoRole.getName(), ROLE_3);
    assertEquals(pentahoRole.getDescription(), ROLE_DESCRIPTION_3);
    
    logout();
    login("systemTenantUser", SUB_TENANT1_1_PATH);
    try {
      pentahoRole = userRoleDao.createRole(SUB_TENANT2_1_PATH, ROLE_3, ROLE_DESCRIPTION_3, null);
      fail("Exception not thrown");
    } catch(Throwable th) {
      assertNotNull(th);
    }

    logout();
    login("systemTenantUser", SUB_TENANT1_1_PATH);
    
    pentahoRole = userRoleDao.createRole(ROLE_4 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + SUB_TENANT1_1_PATH, ROLE_DESCRIPTION_4, null);
    
    pentahoRole = userRoleDao.getRole(ROLE_4 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + SUB_TENANT1_1_PATH);
    assertEquals(pentahoRole.getTenant(), SUB_TENANT1_1_PATH);
    assertEquals(pentahoRole.getName(), ROLE_4);
    assertEquals(pentahoRole.getDescription(), ROLE_DESCRIPTION_4);
    
    roles = userRoleDao.getRoles(SUB_TENANT1_1_PATH);
    assertTrue(roles.size() == 1);
    pentahoRole = roles.get(0);    
    assertEquals(pentahoRole.getTenant(), SUB_TENANT1_1_PATH);
    assertEquals(pentahoRole.getName(), ROLE_4);
    assertEquals(pentahoRole.getDescription(), ROLE_DESCRIPTION_4);

    logout();
    login("systemTenantUser", SUB_TENANT2_1_PATH);
    try {
      pentahoRole = userRoleDao.createRole(SUB_TENANT1_1_PATH, ROLE_3, ROLE_DESCRIPTION_3, null);
      fail("Exception not thrown");
    } catch(Throwable th) {
      assertNotNull(th);
    }

    logout();
    login("systemTenantUser", MAIN_TENANT_1_PATH);

    try {
      pentahoRole = userRoleDao.createRole(MAIN_TENANT_1_PATH, ROLE_1, ROLE_DESCRIPTION_1, null);
      fail("Exception not thrown");
    } catch (AlreadyExistsException e) {
      // Expected exception
    }
    
    try {
      pentahoRole = userRoleDao.createRole(ROLE_1 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + MAIN_TENANT_1_PATH, ROLE_DESCRIPTION_1, null);
      fail("Exception not thrown");
    } catch (AlreadyExistsException e) {
      // Expected exception
    }
    logout();
  }

  @Test
  public void testUpdateUser() throws Exception {
    
    Serializable systemTenantId = tenantManager.createSystemTenant(ServerRepositoryPaths.getPentahoRootFolderName());
    Serializable mainTenant_1_Id = tenantManager.createTenant(systemTenantId, MAIN_TENANT_1);
    Serializable mainTenant_2_Id = tenantManager.createTenant(systemTenantId, MAIN_TENANT_2);

    login("systemTenantUser", MAIN_TENANT_1_PATH);

    IPentahoUser pentahoUser = userRoleDao.createUser(MAIN_TENANT_1_PATH, USER_5, PASSWORD_5, USER_DESCRIPTION_5, null);   
    pentahoUser = userRoleDao.getUser(MAIN_TENANT_1_PATH, USER_5);
    assertEquals(pentahoUser.getDescription(), USER_DESCRIPTION_5);
    String originalPassword = pentahoUser.getPassword();
    String encryptedPassword = originalPassword;
    
    String changedDescription1 = USER_DESCRIPTION_5 + "change1";
    userRoleDao.setUserDescription(MAIN_TENANT_1_PATH, USER_5, changedDescription1);
    pentahoUser = userRoleDao.getUser(USER_5 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + MAIN_TENANT_1_PATH);
    assertEquals(changedDescription1, pentahoUser.getDescription());
    
    String changedDescription2 = USER_DESCRIPTION_5 + "change2";
    userRoleDao.setUserDescription(USER_5 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + MAIN_TENANT_1_PATH, changedDescription2);
    pentahoUser = userRoleDao.getUser(MAIN_TENANT_1_PATH, USER_5);
    assertEquals(changedDescription2, pentahoUser.getDescription());
    
    userRoleDao.setUserDescription(USER_5 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + MAIN_TENANT_1_PATH, null);
    pentahoUser = userRoleDao.getUser(MAIN_TENANT_1_PATH, USER_5);
    assertNull(pentahoUser.getDescription());
    
    try {
      userRoleDao.setUserDescription(null, changedDescription2);
      fail("Exception not thrown");
    } catch (Exception ex) {
      // Expected exception
    }
    
    try {
      userRoleDao.setUserDescription(USER_5, changedDescription2);
    } catch (Exception ex) {
      // Expected exception
    }
        
    try {
      userRoleDao.setUserDescription(MAIN_TENANT_1_PATH, UNKNOWN_USER, changedDescription2);
      fail("Exception not thrown");
    } catch (NotFoundException ex) {
      // Expected exception
    }
    logout();
    login("systemTenantUser", MAIN_TENANT_2_PATH);

    try {
      changedDescription1 = USER_DESCRIPTION_5 + "change1";
      userRoleDao.setUserDescription(MAIN_TENANT_1_PATH, USER_5, changedDescription1);
      fail("Exception not thrown");
    } catch(Throwable th) {
      assertNotNull(th);
    }
    logout();
//    String changedPwd1 = PASSWORD_5 + "change1";
//    userRoleDao.setPassword(TENANT_5, USER_5, changedPwd1);
//    pentahoUser = userRoleDao.getUser(USER_5 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + TENANT_5);
//    assertTrue(!encryptedPassword.equals(pentahoUser.getPassword()));
//    encryptedPassword = pentahoUser.getPassword();
//    
//    String changedPwd2 = PASSWORD_5 + "change2";
//    userRoleDao.setPassword(USER_5 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + TENANT_5, changedPwd2);
//    pentahoUser = userRoleDao.getUser(TENANT_5, USER_5);
//    assertTrue(!encryptedPassword.equals(pentahoUser.getPassword()));
//
//    userRoleDao.setPassword(USER_5 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + TENANT_5, PASSWORD_5);
//    pentahoUser = userRoleDao.getUser(TENANT_5, USER_5);
//    assertTrue(originalPassword.equals(pentahoUser.getPassword()));
  }

  @Test
  public void testUpdateRole() throws Exception {
    Serializable systemTenantId = tenantManager.createSystemTenant(ServerRepositoryPaths.getPentahoRootFolderName());
    Serializable mainTenant_1_Id = tenantManager.createTenant(systemTenantId, MAIN_TENANT_1);
    Serializable mainTenant_2_Id = tenantManager.createTenant(systemTenantId, MAIN_TENANT_2);

    login("systemTenantUser", MAIN_TENANT_1_PATH);

    IPentahoRole pentahoRole = userRoleDao.createRole(MAIN_TENANT_1_PATH, ROLE_5, ROLE_DESCRIPTION_5, null);   
    pentahoRole = userRoleDao.getRole(MAIN_TENANT_1_PATH, ROLE_5);
    assertEquals(pentahoRole.getDescription(), ROLE_DESCRIPTION_5);
    
    String changedDescription1 = ROLE_DESCRIPTION_5 + "change1";
    userRoleDao.setRoleDescription(MAIN_TENANT_1_PATH, ROLE_5, changedDescription1);
    pentahoRole = userRoleDao.getRole(ROLE_5 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + MAIN_TENANT_1_PATH);
    assertEquals(changedDescription1, pentahoRole.getDescription());
    
    String changedDescription2 = ROLE_DESCRIPTION_5 + "change2";
    userRoleDao.setRoleDescription(ROLE_5 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + MAIN_TENANT_1_PATH, changedDescription2);
    pentahoRole = userRoleDao.getRole(MAIN_TENANT_1_PATH, ROLE_5);
    assertEquals(changedDescription2, pentahoRole.getDescription());
    
    userRoleDao.setRoleDescription(ROLE_5 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + MAIN_TENANT_1_PATH, null);
    pentahoRole = userRoleDao.getRole(MAIN_TENANT_1_PATH, ROLE_5);
    assertNull(pentahoRole.getDescription());
    
    try {
      userRoleDao.setRoleDescription(null, changedDescription2);
      fail("Exception not thrown");
    } catch (Exception ex) {
      // Expected exception
    }
    
    try {
      userRoleDao.setRoleDescription(ROLE_5, changedDescription2);
    } catch (Exception ex) {
      // Expected exception
    }
    
    
    try {
      userRoleDao.setRoleDescription(MAIN_TENANT_1_PATH, UNKNOWN_ROLE, changedDescription2);
      fail("Exception not thrown");
    } catch (NotFoundException ex) {
      // Expected exception
    }
    logout();
    login("systemTenantUser", MAIN_TENANT_2_PATH);

    try {
      changedDescription1 = ROLE_DESCRIPTION_5 + "change1";
      userRoleDao.setRoleDescription(MAIN_TENANT_1_PATH, ROLE_5, changedDescription1);
      fail("Exception not thrown");
    } catch(Throwable th) {
      assertNotNull(th);
    }
    logout();
  }

  @Test
  public void testDeleteUser() throws Exception {
    Serializable systemTenantId = tenantManager.createSystemTenant(ServerRepositoryPaths.getPentahoRootFolderName());
    Serializable mainTenant_1_Id = tenantManager.createTenant(systemTenantId, MAIN_TENANT_1);
    Serializable mainTenant_2_Id = tenantManager.createTenant(systemTenantId, MAIN_TENANT_2);

    login("systemTenantUser", MAIN_TENANT_1_PATH);

    IPentahoUser pentahoUser = userRoleDao.createUser(MAIN_TENANT_1_PATH, USER_6, PASSWORD_6, USER_DESCRIPTION_6, null);       
    pentahoUser = userRoleDao.getUser(USER_6 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + MAIN_TENANT_1_PATH);   
    assertNotNull(pentahoUser);
    
    logout();
    login("systemTenantUser", MAIN_TENANT_2_PATH);
    try {
      userRoleDao.deleteUser(pentahoUser);    
      fail("Exception not thrown");
    } catch (Throwable th) {
      assertNotNull(th);
    }    

    logout();
    login("systemTenantUser", MAIN_TENANT_1_PATH);

    userRoleDao.deleteUser(pentahoUser);
    
    pentahoUser = userRoleDao.getUser(USER_6 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + MAIN_TENANT_1_PATH);   
    assertNull(pentahoUser);
    assertEquals(0, userRoleDao.getUsers(MAIN_TENANT_1_PATH).size());
   
    pentahoUser = userRoleDao.createUser(USER_6 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + MAIN_TENANT_1_PATH, PASSWORD_6, USER_DESCRIPTION_6, null);   
    pentahoUser = userRoleDao.getUser(MAIN_TENANT_1_PATH, USER_6);
    
    assertNotNull(pentahoUser);
    
    userRoleDao.deleteUser(pentahoUser);
    
    assertNull(userRoleDao.getUser(USER_6 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + MAIN_TENANT_1_PATH));

    try {
      userRoleDao.deleteUser(pentahoUser);    
      fail("Exception not thrown");
    } catch (NotFoundException e) {
      // Expected exception
    }
    
    try {
      pentahoUser = new PentahoUser(null, USER_6, PASSWORD_6, USER_DESCRIPTION_6, true);
      userRoleDao.deleteUser(pentahoUser);    
      fail("Exception not thrown");
    } catch (Exception ex) {
      // Expected exception
    }
    
    try {
      pentahoUser = new PentahoUser(MAIN_TENANT_1_PATH, null, PASSWORD_6, USER_DESCRIPTION_6, true);
      userRoleDao.deleteUser(pentahoUser);    
      fail("Exception not thrown");
    } catch (NotFoundException e) {
      // Expected exception
    }
    
    try {
      pentahoUser = new PentahoUser(MAIN_TENANT_1_PATH, UNKNOWN_USER, PASSWORD_6, USER_DESCRIPTION_6, true);
      userRoleDao.deleteUser(pentahoUser);    
      fail("Exception not thrown");
    } catch (NotFoundException e) {
      // Expected exception
    }

  }

  @Test
  public void testDeleteRole() throws Exception {
    Serializable systemTenantId = tenantManager.createSystemTenant(ServerRepositoryPaths.getPentahoRootFolderName());
    Serializable mainTenant_1_Id = tenantManager.createTenant(systemTenantId, MAIN_TENANT_1);
    Serializable mainTenant_2_Id = tenantManager.createTenant(systemTenantId, MAIN_TENANT_2);

    login("systemTenantUser", MAIN_TENANT_1_PATH);


    IPentahoRole pentahoRole = userRoleDao.createRole(MAIN_TENANT_1_PATH, ROLE_6, ROLE_DESCRIPTION_6, null);       
    pentahoRole = userRoleDao.getRole(ROLE_6 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + MAIN_TENANT_1_PATH);   
    assertNotNull(pentahoRole);

    logout();
    login("systemTenantUser", MAIN_TENANT_2_PATH);
    try {
      userRoleDao.deleteRole(pentahoRole);
      fail("Exception not thrown");
    } catch (Throwable th) {
      assertNotNull(th);
    }    

    logout();
    login("systemTenantUser", MAIN_TENANT_1_PATH);

    userRoleDao.deleteRole(pentahoRole);
    pentahoRole = userRoleDao.getRole(ROLE_6 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + MAIN_TENANT_1_PATH);   
    assertNull(pentahoRole);
    assertEquals(0, userRoleDao.getRoles(MAIN_TENANT_1_PATH).size());
   
    pentahoRole = userRoleDao.createRole(ROLE_6 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + MAIN_TENANT_1_PATH, ROLE_DESCRIPTION_6, null);   
    pentahoRole = userRoleDao.getRole(MAIN_TENANT_1_PATH, ROLE_6);
    
    assertNotNull(pentahoRole);
    
    userRoleDao.deleteRole(pentahoRole);
    
    assertNull(userRoleDao.getRole(ROLE_6 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + MAIN_TENANT_1_PATH));
    
    try {
      userRoleDao.deleteRole(pentahoRole);    
      fail("Exception not thrown");
    } catch (NotFoundException e) {
      // Expected exception
    }
    
    try {
      pentahoRole = new PentahoRole(null, ROLE_6, ROLE_DESCRIPTION_6);
      userRoleDao.deleteRole(pentahoRole);    
      fail("Exception not thrown");
    } catch (Exception ex) {
      // Expected exception
    }
    
    try {
      pentahoRole = new PentahoRole(MAIN_TENANT_1_PATH, null, ROLE_DESCRIPTION_6);
      userRoleDao.deleteRole(pentahoRole);    
      fail("Exception not thrown");
    } catch (NotFoundException e) {
      // Expected exception
    }
    
    try {
      pentahoRole = new PentahoRole(MAIN_TENANT_1_PATH, UNKNOWN_ROLE, ROLE_DESCRIPTION_6);
      userRoleDao.deleteRole(pentahoRole);    
      fail("Exception not thrown");
    } catch (NotFoundException e) {
      // Expected exception
    }
  }
  
  @Test
  public void testGetUser() throws Exception {
    Serializable systemTenantId = tenantManager.createSystemTenant(ServerRepositoryPaths.getPentahoRootFolderName());
    Serializable mainTenant_1_Id = tenantManager.createTenant(systemTenantId, MAIN_TENANT_1);
    Serializable mainTenant_2_Id = tenantManager.createTenant(systemTenantId, MAIN_TENANT_2);

    login("systemTenantUser", MAIN_TENANT_1_PATH);

    assertNull(userRoleDao.getUser(UNKNOWN_TENANT, UNKNOWN_USER));   
    assertNull(userRoleDao.getUser(UNKNOWN_USER));
  }

  @Test
  public void testGetUsers() throws Exception {
    Serializable systemTenantId = tenantManager.createSystemTenant(ServerRepositoryPaths.getPentahoRootFolderName());
    Serializable mainTenant_1_Id = tenantManager.createTenant(systemTenantId, MAIN_TENANT_1);
    Serializable mainTenant_2_Id = tenantManager.createTenant(systemTenantId, MAIN_TENANT_2);

    login("systemTenantUser", MAIN_TENANT_1_PATH);

    userRoleDao.createUser(MAIN_TENANT_1_PATH, USER_7, PASSWORD_7, USER_DESCRIPTION_7, null);       
    userRoleDao.createUser(MAIN_TENANT_1_PATH, USER_8, PASSWORD_8, USER_DESCRIPTION_8, null);
    List<IPentahoUser> users = userRoleDao.getUsers(MAIN_TENANT_1_PATH);
    assertEquals(2, users.size());
    
    for (IPentahoUser user : users) {
      if (user.getUsername().equals(USER_7)) {
        assertEquals(user.getTenant(), MAIN_TENANT_1_PATH);
        assertEquals(user.getDescription(), USER_DESCRIPTION_7);
        assertEquals(user.isEnabled(), true);
      } else if (user.getUsername().equals(USER_8)) {
        assertEquals(user.getTenant(), MAIN_TENANT_1_PATH);
        assertEquals(user.getDescription(), USER_DESCRIPTION_8);
        assertEquals(user.isEnabled(), true);
      } else {
        fail("Invalid user name");
      }
    }
    try {
      users = userRoleDao.getUsers(UNKNOWN_TENANT);
      fail("Exception not thrown");
    } catch(Throwable th) {
      assertNotNull(th);
    }
  }

  @Test
  public void testGetRoles() throws Exception {
    Serializable systemTenantId = tenantManager.createSystemTenant(ServerRepositoryPaths.getPentahoRootFolderName());
    Serializable mainTenant_1_Id = tenantManager.createTenant(systemTenantId, MAIN_TENANT_1);
    Serializable mainTenant_2_Id = tenantManager.createTenant(systemTenantId, MAIN_TENANT_2);

    login("systemTenantUser", MAIN_TENANT_1_PATH);
    userRoleDao.createRole(MAIN_TENANT_1_PATH, ROLE_7, ROLE_DESCRIPTION_7, null);       
    userRoleDao.createRole(MAIN_TENANT_1_PATH, ROLE_8, ROLE_DESCRIPTION_8, null);
    List<IPentahoRole> roles = userRoleDao.getRoles(MAIN_TENANT_1_PATH);
    assertEquals(2, roles.size());
    
    for (IPentahoRole user : roles) {
      if (user.getName().equals(ROLE_7)) {
        assertEquals(user.getTenant(), MAIN_TENANT_1_PATH);
        assertEquals(user.getDescription(), ROLE_DESCRIPTION_7);
      } else if (user.getName().equals(ROLE_8)) {
        assertEquals(user.getTenant(), MAIN_TENANT_1_PATH);
        assertEquals(user.getDescription(), ROLE_DESCRIPTION_8);
      } else {
        fail("Invalid user name");
      }
    }
    try {
      roles = userRoleDao.getRoles(UNKNOWN_TENANT);
      fail("Exception not thrown");
    } catch(Throwable th) {
      assertNotNull(th);
    }
  }

  @Test
  public void testRoleWithMembers() throws Exception {
    Serializable systemTenantId = tenantManager.createSystemTenant(ServerRepositoryPaths.getPentahoRootFolderName());
    Serializable mainTenant_1_Id = tenantManager.createTenant(systemTenantId, MAIN_TENANT_1);
    Serializable mainTenant_2_Id = tenantManager.createTenant(systemTenantId, MAIN_TENANT_2);
    login("systemTenantUser", MAIN_TENANT_1_PATH);
    
    userRoleDao.createRole(MAIN_TENANT_1_PATH, ROLE_1, ROLE_DESCRIPTION_1, null);       
    userRoleDao.createRole(MAIN_TENANT_1_PATH, ROLE_2, ROLE_DESCRIPTION_2, null);
    userRoleDao.createRole(MAIN_TENANT_1_PATH, ROLE_3, ROLE_DESCRIPTION_3, null);
    userRoleDao.createUser(MAIN_TENANT_1_PATH, USER_1, PASSWORD_1, USER_DESCRIPTION_1, new String[]{ROLE_1});
    userRoleDao.createUser(MAIN_TENANT_1_PATH, USER_2, PASSWORD_2, USER_DESCRIPTION_2, new String[]{ROLE_1, ROLE_2});
    
    List<IPentahoUser> users = userRoleDao.getRoleMembers(MAIN_TENANT_1_PATH, ROLE_2);
    assertEquals(1, users.size());
    assertEquals(USER_2, users.get(0).getUsername());
    
    ArrayList<String> expectedUserNames = new ArrayList<String>();
    expectedUserNames.add(USER_1);
    expectedUserNames.add(USER_2);
    ArrayList<String> actualUserNames = new ArrayList<String>();
    users = userRoleDao.getRoleMembers(ROLE_1 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + MAIN_TENANT_1_PATH);
    for (IPentahoUser user : users) {
      actualUserNames.add(user.getUsername());
    }
    assertEquals(2, actualUserNames.size());
    assertTrue(actualUserNames.containsAll(expectedUserNames));
        
    users = userRoleDao.getRoleMembers(MAIN_TENANT_1_PATH, ROLE_3);
    assertEquals(0, users.size());
    
    userRoleDao.createUser(MAIN_TENANT_1_PATH, USER_5, PASSWORD_5, USER_DESCRIPTION_5, null);       
    userRoleDao.createUser(MAIN_TENANT_1_PATH, USER_6, PASSWORD_6, USER_DESCRIPTION_6, null);
    userRoleDao.createUser(MAIN_TENANT_1_PATH, USER_7, PASSWORD_7, USER_DESCRIPTION_7, null);
    userRoleDao.createRole(MAIN_TENANT_1_PATH, ROLE_5, ROLE_DESCRIPTION_6, new String[]{USER_5});
    userRoleDao.createRole(MAIN_TENANT_1_PATH, ROLE_6, ROLE_DESCRIPTION_7, new String[]{USER_5, USER_6});
    
    List<IPentahoRole> roles = userRoleDao.getUserRoles(MAIN_TENANT_1_PATH, USER_6);
    assertEquals(1, roles.size());
    assertEquals(ROLE_6, roles.get(0).getName());
    
    ArrayList<String> expectedRoleNames = new ArrayList<String>();
    expectedRoleNames.add(ROLE_5);
    expectedRoleNames.add(ROLE_6);
    ArrayList<String> actualRoleNames = new ArrayList<String>();
    roles = userRoleDao.getUserRoles(USER_5 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + MAIN_TENANT_1_PATH);
    for (IPentahoRole role : roles) {
      actualRoleNames.add(role.getName());
    }
    assertEquals(2, actualRoleNames.size());
    assertTrue(actualRoleNames.containsAll(expectedRoleNames));
        
    roles = userRoleDao.getUserRoles(MAIN_TENANT_1_PATH, USER_7);
    assertEquals(0, roles.size());
    
    userRoleDao.setUserRoles(USER_7 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + MAIN_TENANT_1_PATH, new String[]{ROLE_5, ROLE_6});
    roles = userRoleDao.getUserRoles(USER_7 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + MAIN_TENANT_1_PATH);
    actualRoleNames.clear();
    for (IPentahoRole role : roles) {
      actualRoleNames.add(role.getName());
    }
    assertEquals(2, actualRoleNames.size());
    assertTrue(actualRoleNames.containsAll(expectedRoleNames));
    
    userRoleDao.setRoleMembers(ROLE_3 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + MAIN_TENANT_1_PATH, new String[]{USER_1, USER_2});
    users = userRoleDao.getRoleMembers(ROLE_3 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + MAIN_TENANT_1_PATH);
    actualUserNames.clear();
    for (IPentahoUser user : users) {
      actualUserNames.add(user.getUsername());
    }
    assertEquals(2, actualUserNames.size());
    assertTrue(actualUserNames.containsAll(expectedUserNames));
  }

  @Test
  public void testGetRole() throws Exception {
    assertNull(userRoleDao.getRole(UNKNOWN_TENANT, UNKNOWN_ROLE));   
    try {
      userRoleDao.getRole(UNKNOWN_ROLE);
      fail("Exception not thrown");
    } catch (Exception ex) {
      // Exception expected
    }
  }

  private static void traverseNodes(Node node, int currentLevel) throws Exception {
    System.out.println(node.getPath());
    NodeIterator children = node.getNodes();
    while (children.hasNext()) {
        traverseNodes(children.nextNode(), currentLevel + 1);
    }
  }  
}
