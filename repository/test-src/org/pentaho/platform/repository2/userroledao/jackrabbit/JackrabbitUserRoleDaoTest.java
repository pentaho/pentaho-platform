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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
import org.pentaho.platform.api.repository2.unified.ITenant;
import org.pentaho.platform.api.repository2.unified.ITenantManager;
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
import org.pentaho.platform.repository2.unified.IRepositoryFileDao;
import org.pentaho.platform.repository2.unified.ServerRepositoryPaths;
import org.pentaho.platform.repository2.unified.Tenant;
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
  public static final String SUB_TENANT1_1_1 = "subtenant111";
  public static final String SUB_TENANT1_1_2 = "subtenant112";
  public static final String SUB_TENANT1_2 = "subtenant12";
  public static final String SUB_TENANT1_2_1 = "subtenant121";
  public static final String SUB_TENANT1_2_2 = "subtenant122";
  public static final String MAIN_TENANT_2 = "maintenant2";
  public static final String SUB_TENANT2_1 = "subtenant21";
  public static final String SUB_TENANT2_1_1 = "subtenant111";
  public static final String SUB_TENANT2_1_2 = "subtenant112";
  public static final String SUB_TENANT2_2 = "subtenant22";
  public static final String SUB_TENANT2_2_1 = "subtenant111";
  public static final String SUB_TENANT2_2_2 = "subtenant112";

  public static final String PASSWORD_1 = "password1"; //$NON-NLS-1$
  public static final String PASSWORD_2 = "password2"; //$NON-NLS-1$
  public static final String PASSWORD_3 = "password3"; //$NON-NLS-1$
  public static final String PASSWORD_4 = "password4"; //$NON-NLS-1$
  public static final String PASSWORD_5 = "password5"; //$NON-NLS-1$
  public static final String PASSWORD_6 = "password6"; //$NON-NLS-1$
  public static final String PASSWORD_7 = "password7"; //$NON-NLS-1$
  public static final String PASSWORD_8 = "password8"; //$NON-NLS-1$
  public static final String PASSWORD_9 = "password9"; //$NON-NLS-1$
  public static final String PASSWORD_10 = "password10"; //$NON-NLS-1$
  public static final String PASSWORD_11 = "password11"; //$NON-NLS-1$
  public static final String PASSWORD_12 = "password12"; //$NON-NLS-1$
  public static final String PASSWORD_13 = "password13"; //$NON-NLS-1$
  public static final String PASSWORD_14 = "password14"; //$NON-NLS-1$

  public static final String USER_1 = "joe"; //$NON-NLS-1$
  public static final String USER_2 = "jim"; //$NON-NLS-1$
  public static final String USER_3 = "sally"; //$NON-NLS-1$
  public static final String USER_4 = "suzy"; //$NON-NLS-1$
  public static final String USER_5 = "nancy"; //$NON-NLS-1$
  public static final String USER_6 = "john"; //$NON-NLS-1$
  public static final String USER_7 = "jane"; //$NON-NLS-1$
  public static final String USER_8 = "jerry"; //$NON-NLS-1$
  public static final String USER_9 = "tom"; //$NON-NLS-1$
  public static final String USER_10 = "johny"; //$NON-NLS-1$
  public static final String USER_11 = "mary"; //$NON-NLS-1$
  public static final String USER_12 = "jill"; //$NON-NLS-1$
  public static final String USER_13 = "jack"; //$NON-NLS-1$
  public static final String USER_14 = "jeremy"; //$NON-NLS-1$

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
  public static final String ROLE_9 = "CTO"; //$NON-NLS-1$
  public static final String ROLE_10 = "CFO"; //$NON-NLS-1$
  public static final String ROLE_11 = "CMO"; //$NON-NLS-1$
  public static final String ROLE_12 = "CIO"; //$NON-NLS-1$
  public static final String ROLE_13 = "COO"; //$NON-NLS-1$
  public static final String ROLE_14 = "CSO"; //$NON-NLS-1$

  public static final String UNKNOWN_ROLE = "unknownRole"; //$NON-NLS-1$
  
  public static final String USER_DESCRIPTION_1 = "User Description 1"; //$NON-NLS-1$
  public static final String USER_DESCRIPTION_2 = "User Description 2"; //$NON-NLS-1$
  public static final String USER_DESCRIPTION_3 = "User Description 3"; //$NON-NLS-1$
  public static final String USER_DESCRIPTION_4 = "User Description 4"; //$NON-NLS-1$
  public static final String USER_DESCRIPTION_5 = "User Description 5"; //$NON-NLS-1$
  public static final String USER_DESCRIPTION_6 = "User Description 6"; //$NON-NLS-1$
  public static final String USER_DESCRIPTION_7 = "User Description 7"; //$NON-NLS-1$
  public static final String USER_DESCRIPTION_8 = "User Description 8"; //$NON-NLS-1$
  public static final String USER_DESCRIPTION_9 = "User Description 9"; //$NON-NLS-1$
  public static final String USER_DESCRIPTION_10 = "User Description 10"; //$NON-NLS-1$
  public static final String USER_DESCRIPTION_11 = "User Description 11"; //$NON-NLS-1$
  public static final String USER_DESCRIPTION_12 = "User Description 12"; //$NON-NLS-1$
  public static final String USER_DESCRIPTION_13 = "User Description 13"; //$NON-NLS-1$
  public static final String USER_DESCRIPTION_14 = "User Description 14"; //$NON-NLS-1$
  
  
  public static final String ROLE_DESCRIPTION_1 = "Role Description 1"; //$NON-NLS-1$
  public static final String ROLE_DESCRIPTION_2 = "Role Description 2"; //$NON-NLS-1$
  public static final String ROLE_DESCRIPTION_3 = "Role Description 3"; //$NON-NLS-1$
  public static final String ROLE_DESCRIPTION_4 = "Role Description 4"; //$NON-NLS-1$
  public static final String ROLE_DESCRIPTION_5 = "Role Description 5"; //$NON-NLS-1$
  public static final String ROLE_DESCRIPTION_6 = "Role Description 6"; //$NON-NLS-1$
  public static final String ROLE_DESCRIPTION_7 = "Role Description 7"; //$NON-NLS-1$
  public static final String ROLE_DESCRIPTION_8 = "Role Description 8"; //$NON-NLS-1$

  public static final String ROLE_DESCRIPTION_9 = "Role Description 9"; //$NON-NLS-1$
  public static final String ROLE_DESCRIPTION_10 = "Role Description 10"; //$NON-NLS-1$
  public static final String ROLE_DESCRIPTION_11 = "Role Description 11"; //$NON-NLS-1$
  public static final String ROLE_DESCRIPTION_12 = "Role Description 12"; //$NON-NLS-1$
  public static final String ROLE_DESCRIPTION_13 = "Role Description 13"; //$NON-NLS-1$
  public static final String ROLE_DESCRIPTION_14 = "Role Description 14"; //$NON-NLS-1$

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
  private IRepositoryFileDao repositoryFileDao;

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
    mp.define(ITenant.class, Tenant.class);

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
    
    JackrabbitUserRoleDao jackrabbitUserRoleDao = new JackrabbitUserRoleDao(jcrTemplate, repositoryFileDao);
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
    repositoryFileDao = (IRepositoryFileDao) applicationContext.getBean("repositoryFileDao");
  }

  
  @Test
  public void testGetUserWithSubTenant() throws Exception {
    ITenant systemTenant = tenantManager.createSystemTenant(ServerRepositoryPaths.getPentahoRootFolderName());
    ITenant mainTenant_1 = tenantManager.createTenant(systemTenant, MAIN_TENANT_1);
    ITenant mainTenant_2 = tenantManager.createTenant(systemTenant, MAIN_TENANT_2);
    ITenant subTenant1_1 = tenantManager.createTenant(mainTenant_1, SUB_TENANT1_1);
    ITenant subTenant1_2 = tenantManager.createTenant(mainTenant_1, SUB_TENANT1_2);
    ITenant subTenant1_1_1 = tenantManager.createTenant(subTenant1_1, SUB_TENANT1_1_1);
    ITenant subTenant1_1_2 = tenantManager.createTenant(subTenant1_1, SUB_TENANT1_1_2);
    ITenant subTenant1_2_1 = tenantManager.createTenant(subTenant1_2, SUB_TENANT1_2_1);
    ITenant subTenant1_2_2 = tenantManager.createTenant(subTenant1_2, SUB_TENANT1_2_2);
    ITenant subTenant2_1 = tenantManager.createTenant(mainTenant_2, SUB_TENANT2_1);
    ITenant subTenant2_2 = tenantManager.createTenant(mainTenant_2, SUB_TENANT2_2);
    ITenant subTenant2_1_1 = tenantManager.createTenant(subTenant2_1, SUB_TENANT2_1_1);
    ITenant subTenant2_1_2 = tenantManager.createTenant(subTenant2_1, SUB_TENANT2_1_2);
    ITenant subTenant2_2_1 = tenantManager.createTenant(subTenant2_2, SUB_TENANT2_2_1);
    ITenant subTenant2_2_2 = tenantManager.createTenant(subTenant2_2, SUB_TENANT2_2_2);
    
    login("systemTenantUser", mainTenant_1.getPath());
    IPentahoUser pentahoUser = userRoleDao.createUser(mainTenant_1.getPath(), USER_1, PASSWORD_1, USER_DESCRIPTION_1, null);
    pentahoUser = userRoleDao.createUser(subTenant1_1.getPath(), USER_2, PASSWORD_2, USER_DESCRIPTION_2, null);
    pentahoUser = userRoleDao.createUser(subTenant1_2.getPath(), USER_3, PASSWORD_3, USER_DESCRIPTION_3, null);
    pentahoUser = userRoleDao.createUser(subTenant1_1_1.getPath(), USER_4, PASSWORD_2, USER_DESCRIPTION_2, null);
    pentahoUser = userRoleDao.createUser(subTenant1_1_2.getPath(), USER_5, PASSWORD_3, USER_DESCRIPTION_3, null);
    pentahoUser = userRoleDao.createUser(subTenant1_2_1.getPath(), USER_6, PASSWORD_2, USER_DESCRIPTION_2, null);
    pentahoUser = userRoleDao.createUser(subTenant1_2_2.getPath(), USER_7, PASSWORD_3, USER_DESCRIPTION_3, null);
    Session session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));

    List<IPentahoUser> usersWithSubTenant = userRoleDao.getUsers(mainTenant_1.getPath(), true);
    assertEquals(usersWithSubTenant.size(), 7);
    List<IPentahoUser> usersWithoutSubTenant = userRoleDao.getUsers(mainTenant_1.getPath(), false);
    assertEquals(usersWithoutSubTenant.size(), 1);


    usersWithSubTenant = userRoleDao.getUsers(subTenant1_1.getPath(), true);
    assertEquals(usersWithSubTenant.size(), 3);

    usersWithSubTenant = userRoleDao.getUsers(subTenant1_2.getPath(), true);
    assertEquals(usersWithSubTenant.size(), 3);

    usersWithoutSubTenant = userRoleDao.getUsers(subTenant1_1.getPath(), false);
    assertEquals(usersWithoutSubTenant.size(), 1);

    usersWithoutSubTenant = userRoleDao.getUsers(subTenant1_2.getPath(), false);
    assertEquals(usersWithoutSubTenant.size(), 1);

    logout();

    login("systemTenantUser", mainTenant_2.getPath());
    
    pentahoUser = userRoleDao.createUser(mainTenant_2.getPath(), USER_8, PASSWORD_8, USER_DESCRIPTION_8, null);
    pentahoUser = userRoleDao.createUser(subTenant2_1.getPath(), USER_9, PASSWORD_9, USER_DESCRIPTION_9, null);
    pentahoUser = userRoleDao.createUser(subTenant2_2.getPath(), USER_10, PASSWORD_10, USER_DESCRIPTION_10, null);
    pentahoUser = userRoleDao.createUser(subTenant2_1_1.getPath(), USER_11, PASSWORD_11, USER_DESCRIPTION_11, null);
    pentahoUser = userRoleDao.createUser(subTenant2_1_2.getPath(), USER_12, PASSWORD_12, USER_DESCRIPTION_12, null);
    pentahoUser = userRoleDao.createUser(subTenant2_2_1.getPath(), USER_13, PASSWORD_13, USER_DESCRIPTION_13, null);
    pentahoUser = userRoleDao.createUser(subTenant2_2_2.getPath(), USER_14, PASSWORD_14, USER_DESCRIPTION_14, null);

    usersWithSubTenant = userRoleDao.getUsers(mainTenant_2.getPath(), true);
    assertEquals(usersWithSubTenant.size(), 7);
    usersWithoutSubTenant = userRoleDao.getUsers(mainTenant_2.getPath(), false);
    assertEquals(usersWithoutSubTenant.size(), 1);

    usersWithSubTenant = userRoleDao.getUsers(subTenant2_1.getPath(), true);
    assertEquals(usersWithSubTenant.size(), 3);

    usersWithSubTenant = userRoleDao.getUsers(subTenant2_2.getPath(), true);
    assertEquals(usersWithSubTenant.size(), 3);

    usersWithoutSubTenant = userRoleDao.getUsers(subTenant2_1.getPath(), false);
    assertEquals(usersWithoutSubTenant.size(), 1);

    usersWithoutSubTenant = userRoleDao.getUsers(subTenant2_2.getPath(), false);
    assertEquals(usersWithoutSubTenant.size(), 1);

    logout();
    
  }  

  @Test
  public void testGetRolesWithSubTenant() throws Exception {
    ITenant systemTenantId = tenantManager.createSystemTenant(ServerRepositoryPaths.getPentahoRootFolderName());
    ITenant mainTenant_1 = tenantManager.createTenant(systemTenantId, MAIN_TENANT_1);
    ITenant mainTenant_2 = tenantManager.createTenant(systemTenantId, MAIN_TENANT_2);
    ITenant subTenant1_1 = tenantManager.createTenant(mainTenant_1, SUB_TENANT1_1);
    ITenant subTenant1_2 = tenantManager.createTenant(mainTenant_1, SUB_TENANT1_2);
    ITenant subTenant1_1_1 = tenantManager.createTenant(subTenant1_1, SUB_TENANT1_1_1);
    ITenant subTenant1_1_2 = tenantManager.createTenant(subTenant1_1, SUB_TENANT1_1_2);
    ITenant subTenant1_2_1 = tenantManager.createTenant(subTenant1_2, SUB_TENANT1_2_1);
    ITenant subTenant1_2_2 = tenantManager.createTenant(subTenant1_2, SUB_TENANT1_2_2);
    ITenant subTenant2_1 = tenantManager.createTenant(mainTenant_2, SUB_TENANT2_1);
    ITenant subTenant2_2 = tenantManager.createTenant(mainTenant_2, SUB_TENANT2_2);
    ITenant subTenant2_1_1 = tenantManager.createTenant(subTenant2_1, SUB_TENANT2_1_1);
    ITenant subTenant2_1_2 = tenantManager.createTenant(subTenant2_1, SUB_TENANT2_1_2);
    ITenant subTenant2_2_1 = tenantManager.createTenant(subTenant2_2, SUB_TENANT2_2_1);
    ITenant subTenant2_2_2 = tenantManager.createTenant(subTenant2_2, SUB_TENANT2_2_2);
    
    login("systemTenantUser", mainTenant_1.getPath());
    IPentahoRole pentahoRole = userRoleDao.createRole(mainTenant_1.getPath(), ROLE_1, ROLE_DESCRIPTION_1, null);
    pentahoRole = userRoleDao.createRole(subTenant1_1.getPath(), ROLE_2, ROLE_DESCRIPTION_2, null);
    pentahoRole = userRoleDao.createRole(subTenant1_2.getPath(), ROLE_3, ROLE_DESCRIPTION_3, null);
    pentahoRole = userRoleDao.createRole(subTenant1_1_1.getPath(), ROLE_4, ROLE_DESCRIPTION_4, null);
    pentahoRole = userRoleDao.createRole(subTenant1_1_2.getPath(), ROLE_5, ROLE_DESCRIPTION_5, null);
    pentahoRole = userRoleDao.createRole(subTenant1_2_1.getPath(), ROLE_6, ROLE_DESCRIPTION_6, null);
    pentahoRole = userRoleDao.createRole(subTenant1_2_2.getPath(), ROLE_7, ROLE_DESCRIPTION_7, null);
        
    List<IPentahoRole> rolesWithSubTenant = userRoleDao.getRoles(mainTenant_1.getPath(), true);
    assertEquals(rolesWithSubTenant.size(), 7);
    List<IPentahoRole> rolesWithoutSubTenant = userRoleDao.getRoles(mainTenant_1.getPath(), false);
    assertEquals(rolesWithoutSubTenant.size(), 1);
    
    logout();

    login("systemTenantUser", mainTenant_2.getPath());
    
    pentahoRole = userRoleDao.createRole(mainTenant_2.getPath(), ROLE_8, ROLE_DESCRIPTION_8, null);
    pentahoRole = userRoleDao.createRole(subTenant2_1.getPath(), ROLE_9, ROLE_DESCRIPTION_9, null);
    pentahoRole = userRoleDao.createRole(subTenant2_2.getPath(), ROLE_10, ROLE_DESCRIPTION_10, null);
    pentahoRole = userRoleDao.createRole(subTenant2_1_1.getPath(), ROLE_11, ROLE_DESCRIPTION_11, null);
    pentahoRole = userRoleDao.createRole(subTenant2_1_2.getPath(), ROLE_12, ROLE_DESCRIPTION_12, null);
    pentahoRole = userRoleDao.createRole(subTenant2_2_1.getPath(), ROLE_13, ROLE_DESCRIPTION_13, null);
    pentahoRole = userRoleDao.createRole(subTenant2_2_2.getPath(), ROLE_14, ROLE_DESCRIPTION_14, null);
 

    rolesWithSubTenant = userRoleDao.getRoles(mainTenant_2.getPath(), true);
    assertEquals(rolesWithSubTenant.size(), 7);
    rolesWithoutSubTenant = userRoleDao.getRoles(mainTenant_2.getPath(), false);
    assertEquals(rolesWithoutSubTenant.size(), 1);

    rolesWithSubTenant = userRoleDao.getRoles(subTenant2_1.getPath(), true);
    assertEquals(rolesWithSubTenant.size(), 3);

    rolesWithSubTenant = userRoleDao.getRoles(subTenant2_2.getPath(), true);
    assertEquals(rolesWithSubTenant.size(), 3);

    rolesWithoutSubTenant = userRoleDao.getRoles(subTenant2_1.getPath(), false);
    assertEquals(rolesWithoutSubTenant.size(), 1);

    rolesWithoutSubTenant = userRoleDao.getRoles(subTenant2_2.getPath(), false);
    assertEquals(rolesWithoutSubTenant.size(), 1);
    logout();
  }  

  @Test
  public void testCreateUser() throws Exception {
    ITenant systemTenantId = tenantManager.createSystemTenant(ServerRepositoryPaths.getPentahoRootFolderName());
    ITenant mainTenant_1 = tenantManager.createTenant(systemTenantId, MAIN_TENANT_1);
    ITenant mainTenant_2 = tenantManager.createTenant(systemTenantId, MAIN_TENANT_2);
    ITenant subTenant1_1 = tenantManager.createTenant(mainTenant_1, SUB_TENANT1_1);
    ITenant subTenant1_2 = tenantManager.createTenant(mainTenant_1, SUB_TENANT1_2);
    ITenant subTenant2_1 = tenantManager.createTenant(mainTenant_2, SUB_TENANT2_1);
    ITenant subTenant2_2 = tenantManager.createTenant(mainTenant_2, SUB_TENANT2_2);

    login("systemTenantUser", mainTenant_1.getPath());
    List<IPentahoUser> users = userRoleDao.getUsers(mainTenant_1.getPath());
    IPentahoUser pentahoUser = userRoleDao.createUser(mainTenant_1.getPath(), USER_1, PASSWORD_1, USER_DESCRIPTION_1, null);
    pentahoUser = userRoleDao.getUser(mainTenant_1.getPath(), USER_1);
    assertEquals(pentahoUser.getTenant(), mainTenant_1.getPath());
    assertEquals(pentahoUser.getUsername(), USER_1);
    assertEquals(pentahoUser.getDescription(), USER_DESCRIPTION_1);
    assertEquals(pentahoUser.isEnabled(), true);
    logout();
    login("systemTenantUser", subTenant2_1.getPath());
    try {
      pentahoUser = userRoleDao.createUser(mainTenant_1.getPath(), USER_1, PASSWORD_1, USER_DESCRIPTION_1, null);
      fail("Exception not thrown");
    } catch(Throwable th) {
      assertNotNull(th);
    }
    logout();
    login("systemTenantUser", mainTenant_1.getPath());

    SessionImpl session = (SessionImpl)repository.login(new SimpleCredentials("admin", "admin".toCharArray())); //$NON-NLS-1$ //$NON-NLS-2$
    users = userRoleDao.getUsers(mainTenant_1.getPath());
    assertTrue(users.size() == 1);
    pentahoUser = users.get(0);    
    assertEquals(pentahoUser.getTenant(), mainTenant_1.getPath());
    assertEquals(pentahoUser.getUsername(), USER_1);
    assertEquals(pentahoUser.getDescription(), USER_DESCRIPTION_1);
    assertEquals(pentahoUser.isEnabled(), true);
    
    logout();
    login("systemTenantUser", mainTenant_2.getPath());
    pentahoUser = userRoleDao.createUser(mainTenant_2.getPath(), USER_2, PASSWORD_2, USER_DESCRIPTION_2, null);
    
    logout();
    login("systemTenantUser", subTenant1_1.getPath());
    try {
      pentahoUser = userRoleDao.createUser(mainTenant_2.getPath(), USER_2, PASSWORD_2, USER_DESCRIPTION_2, null);
      fail("Exception not thrown");
    } catch(Throwable th) {
      assertNotNull(th);
    }

    logout();
    login("systemTenantUser", mainTenant_2.getPath());

    pentahoUser = userRoleDao.getUser(mainTenant_2.getPath(), USER_2);
    assertEquals(pentahoUser.getTenant(), mainTenant_2.getPath());
    assertEquals(pentahoUser.getUsername(), USER_2);
    assertEquals(pentahoUser.getDescription(), USER_DESCRIPTION_2);
    assertEquals(pentahoUser.isEnabled(), true);

    users = userRoleDao.getUsers(mainTenant_2.getPath());
    assertTrue(users.size() == 1);
    pentahoUser = users.get(0);    
    assertEquals(pentahoUser.getTenant(), mainTenant_2.getPath());
    assertEquals(pentahoUser.getUsername(), USER_2);
    assertEquals(pentahoUser.getDescription(), USER_DESCRIPTION_2);
    assertEquals(pentahoUser.isEnabled(), true);
    
    logout();
    login("systemTenantUser", subTenant2_1.getPath());

    pentahoUser = userRoleDao.createUser(USER_3 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + subTenant2_1.getPath(), PASSWORD_3, USER_DESCRIPTION_3, null);
    
    pentahoUser = userRoleDao.getUser(USER_3 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + subTenant2_1.getPath());
    assertEquals(pentahoUser.getTenant(), subTenant2_1.getPath());
    assertEquals(pentahoUser.getUsername(), USER_3);
    assertEquals(pentahoUser.getDescription(), USER_DESCRIPTION_3);
    assertEquals(pentahoUser.isEnabled(), true);
    
    logout();
    login("systemTenantUser", subTenant1_1.getPath());

    try {
      pentahoUser = userRoleDao.createUser(subTenant2_1.getPath(), USER_1, PASSWORD_1, USER_DESCRIPTION_1, null);
      fail("Exception not thrown");
    } catch(Throwable th) {
      assertNotNull(th);
    }

    logout();
    login("systemTenantUser", subTenant2_1.getPath());

    users = userRoleDao.getUsers(subTenant2_1.getPath());
    assertTrue(users.size() == 1);
    pentahoUser = users.get(0);    
    assertEquals(pentahoUser.getTenant(), subTenant2_1.getPath());
    assertEquals(pentahoUser.getUsername(), USER_3);
    assertEquals(pentahoUser.getDescription(), USER_DESCRIPTION_3);
    assertEquals(pentahoUser.isEnabled(), true);
    
    logout();
    login("systemTenantUser", subTenant1_1.getPath());
    
    pentahoUser = userRoleDao.createUser(USER_4 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + subTenant1_1.getPath(), PASSWORD_4, USER_DESCRIPTION_4, null);
    
    pentahoUser = userRoleDao.getUser(USER_4 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + subTenant1_1.getPath());
    assertEquals(pentahoUser.getTenant(), subTenant1_1.getPath());
    assertEquals(pentahoUser.getUsername(), USER_4);
    assertEquals(pentahoUser.getDescription(), USER_DESCRIPTION_4);
    assertEquals(pentahoUser.isEnabled(), true);
    
    logout();
    login("systemTenantUser", subTenant2_1.getPath());

    try {
      pentahoUser = userRoleDao.createUser(subTenant1_1.getPath(), USER_1, PASSWORD_1, USER_DESCRIPTION_1, null);
      fail("Exception not thrown");
    } catch(Throwable th) {
      assertNotNull(th);
    }

    logout();
    login("systemTenantUser", subTenant1_1.getPath());

    
    users = userRoleDao.getUsers(subTenant1_1.getPath());
    assertTrue(users.size() == 1);
    pentahoUser = users.get(0);    
    assertEquals(pentahoUser.getTenant(), subTenant1_1.getPath());
    assertEquals(pentahoUser.getUsername(), USER_4);
    assertEquals(pentahoUser.getDescription(), USER_DESCRIPTION_4);
    assertEquals(pentahoUser.isEnabled(), true);

    logout();
    login("systemTenantUser", mainTenant_1.getPath());

    try {
      pentahoUser = userRoleDao.createUser(mainTenant_1.getPath(), USER_1, PASSWORD_1, USER_DESCRIPTION_1, null);
      fail("Exception not thrown");
    } catch (AlreadyExistsException e) {
      // Expected exception
    }
    
    try {
      pentahoUser = userRoleDao.createUser(USER_1 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + mainTenant_1.getPath(), PASSWORD_1, USER_DESCRIPTION_1, null);
      fail("Exception not thrown");
    } catch (AlreadyExistsException e) {
      // Expected exception
    }
  }

  @Test
  public void testCreateRole() throws Exception {
    ITenant systemTenantId = tenantManager.createSystemTenant(ServerRepositoryPaths.getPentahoRootFolderName());
    ITenant mainTenant_1 = tenantManager.createTenant(systemTenantId, MAIN_TENANT_1);
    ITenant mainTenant_2 = tenantManager.createTenant(systemTenantId, MAIN_TENANT_2);
    ITenant subTenant1_1 = tenantManager.createTenant(mainTenant_1, SUB_TENANT1_1);
    ITenant subTenant1_2 = tenantManager.createTenant(mainTenant_1, SUB_TENANT1_2);
    ITenant subTenant2_1 = tenantManager.createTenant(mainTenant_2, SUB_TENANT2_1);
    ITenant subTenant2_2 = tenantManager.createTenant(mainTenant_2, SUB_TENANT2_2);

    login("systemTenantUser", mainTenant_1.getPath());

    IPentahoRole pentahoRole = userRoleDao.createRole(mainTenant_1.getPath(), ROLE_1, ROLE_DESCRIPTION_1, null);
    
    pentahoRole = userRoleDao.getRole(mainTenant_1.getPath(), ROLE_1);
    assertEquals(pentahoRole.getTenant(), mainTenant_1.getPath());
    assertEquals(pentahoRole.getName(), ROLE_1);
    assertEquals(pentahoRole.getDescription(), ROLE_DESCRIPTION_1);
    
    List<IPentahoRole> roles = userRoleDao.getRoles(mainTenant_1.getPath());
    assertTrue(roles.size() == 1);
    pentahoRole = roles.get(0);    
    assertEquals(pentahoRole.getTenant(), mainTenant_1.getPath());
    assertEquals(pentahoRole.getName(), ROLE_1);
    assertEquals(pentahoRole.getDescription(), ROLE_DESCRIPTION_1);
    logout();
    login("systemTenantUser", mainTenant_1.getPath());
    try {
      pentahoRole = userRoleDao.createRole(mainTenant_1.getPath(), ROLE_1, ROLE_DESCRIPTION_1, null);
      fail("Exception not thrown");
    } catch(Throwable th) {
      assertNotNull(th);
    }
    logout();
    login("systemTenantUser", mainTenant_2.getPath());

    pentahoRole = userRoleDao.createRole(mainTenant_2.getPath(), ROLE_1, ROLE_DESCRIPTION_2, null);
    
    pentahoRole = userRoleDao.getRole(mainTenant_2.getPath(), ROLE_1);
    assertEquals(pentahoRole.getTenant(), mainTenant_2.getPath());
    assertEquals(pentahoRole.getName(), ROLE_1);
    assertEquals(pentahoRole.getDescription(), ROLE_DESCRIPTION_2);
    
    roles = userRoleDao.getRoles(mainTenant_2.getPath());
    assertTrue(roles.size() == 1);
    pentahoRole = roles.get(0);    
    assertEquals(pentahoRole.getTenant(), mainTenant_2.getPath());
    assertEquals(pentahoRole.getName(), ROLE_1);
    assertEquals(pentahoRole.getDescription(), ROLE_DESCRIPTION_2);

    logout();
    login("systemTenantUser", subTenant2_1.getPath());
    try {
      pentahoRole = userRoleDao.createRole(mainTenant_2.getPath(), ROLE_1, ROLE_DESCRIPTION_1, null);
      fail("Exception not thrown");
    } catch(Throwable th) {
      assertNotNull(th);
    }

    userRoleDao.createRole(ROLE_3 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + subTenant2_1.getPath(), ROLE_DESCRIPTION_3, null);
    
    pentahoRole = userRoleDao.getRole(ROLE_3 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + subTenant2_1.getPath());
    assertEquals(pentahoRole.getTenant(), subTenant2_1.getPath());
    assertEquals(pentahoRole.getName(), ROLE_3);
    assertEquals(pentahoRole.getDescription(), ROLE_DESCRIPTION_3);
    
    roles = userRoleDao.getRoles(subTenant2_1.getPath());
    assertTrue(roles.size() == 1);
    pentahoRole = roles.get(0);    
    assertEquals(pentahoRole.getTenant(), subTenant2_1.getPath());
    assertEquals(pentahoRole.getName(), ROLE_3);
    assertEquals(pentahoRole.getDescription(), ROLE_DESCRIPTION_3);
    
    logout();
    login("systemTenantUser", subTenant1_1.getPath());
    try {
      pentahoRole = userRoleDao.createRole(subTenant2_1.getPath(), ROLE_3, ROLE_DESCRIPTION_3, null);
      fail("Exception not thrown");
    } catch(Throwable th) {
      assertNotNull(th);
    }

    logout();
    login("systemTenantUser", subTenant1_1.getPath());
    
    pentahoRole = userRoleDao.createRole(ROLE_4 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + subTenant1_1.getPath(), ROLE_DESCRIPTION_4, null);
    
    pentahoRole = userRoleDao.getRole(ROLE_4 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + subTenant1_1.getPath());
    assertEquals(pentahoRole.getTenant(), subTenant1_1.getPath());
    assertEquals(pentahoRole.getName(), ROLE_4);
    assertEquals(pentahoRole.getDescription(), ROLE_DESCRIPTION_4);
    
    roles = userRoleDao.getRoles(subTenant1_1.getPath());
    assertTrue(roles.size() == 1);
    pentahoRole = roles.get(0);    
    assertEquals(pentahoRole.getTenant(), subTenant1_1.getPath());
    assertEquals(pentahoRole.getName(), ROLE_4);
    assertEquals(pentahoRole.getDescription(), ROLE_DESCRIPTION_4);

    logout();
    login("systemTenantUser", subTenant2_1.getPath());
    try {
      pentahoRole = userRoleDao.createRole(subTenant1_1.getPath(), ROLE_3, ROLE_DESCRIPTION_3, null);
      fail("Exception not thrown");
    } catch(Throwable th) {
      assertNotNull(th);
    }

    logout();
    login("systemTenantUser", mainTenant_1.getPath());

    try {
      pentahoRole = userRoleDao.createRole(mainTenant_1.getPath(), ROLE_1, ROLE_DESCRIPTION_1, null);
      fail("Exception not thrown");
    } catch (AlreadyExistsException e) {
      // Expected exception
    }
    
    try {
      pentahoRole = userRoleDao.createRole(ROLE_1 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + mainTenant_1.getPath(), ROLE_DESCRIPTION_1, null);
      fail("Exception not thrown");
    } catch (AlreadyExistsException e) {
      // Expected exception
    }
    logout();
  }

  @Test
  public void testUpdateUser() throws Exception {
    
    ITenant systemTenantId = tenantManager.createSystemTenant(ServerRepositoryPaths.getPentahoRootFolderName());
    ITenant mainTenant_1 = tenantManager.createTenant(systemTenantId, MAIN_TENANT_1);
    ITenant mainTenant_2 = tenantManager.createTenant(systemTenantId, MAIN_TENANT_2);

    login("systemTenantUser", mainTenant_1.getPath());

    IPentahoUser pentahoUser = userRoleDao.createUser(mainTenant_1.getPath(), USER_5, PASSWORD_5, USER_DESCRIPTION_5, null);   
    pentahoUser = userRoleDao.getUser(mainTenant_1.getPath(), USER_5);
    assertEquals(pentahoUser.getDescription(), USER_DESCRIPTION_5);
    String originalPassword = pentahoUser.getPassword();
    String encryptedPassword = originalPassword;
    
    String changedDescription1 = USER_DESCRIPTION_5 + "change1";
    userRoleDao.setUserDescription(mainTenant_1.getPath(), USER_5, changedDescription1);
    pentahoUser = userRoleDao.getUser(USER_5 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + mainTenant_1.getPath());
    assertEquals(changedDescription1, pentahoUser.getDescription());
    
    String changedDescription2 = USER_DESCRIPTION_5 + "change2";
    userRoleDao.setUserDescription(USER_5 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + mainTenant_1.getPath(), changedDescription2);
    pentahoUser = userRoleDao.getUser(mainTenant_1.getPath(), USER_5);
    assertEquals(changedDescription2, pentahoUser.getDescription());
    
    userRoleDao.setUserDescription(USER_5 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + mainTenant_1.getPath(), null);
    pentahoUser = userRoleDao.getUser(mainTenant_1.getPath(), USER_5);
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
      userRoleDao.setUserDescription(mainTenant_1.getPath(), UNKNOWN_USER, changedDescription2);
      fail("Exception not thrown");
    } catch (NotFoundException ex) {
      // Expected exception
    }
    logout();
    login("systemTenantUser", mainTenant_2.getPath());

    try {
      changedDescription1 = USER_DESCRIPTION_5 + "change1";
      userRoleDao.setUserDescription(mainTenant_1.getPath(), USER_5, changedDescription1);
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
    ITenant systemTenantId = tenantManager.createSystemTenant(ServerRepositoryPaths.getPentahoRootFolderName());
    ITenant mainTenant_1 = tenantManager.createTenant(systemTenantId, MAIN_TENANT_1);
    ITenant mainTenant_2 = tenantManager.createTenant(systemTenantId, MAIN_TENANT_2);

    login("systemTenantUser", mainTenant_1.getPath());

    IPentahoRole pentahoRole = userRoleDao.createRole(mainTenant_1.getPath(), ROLE_5, ROLE_DESCRIPTION_5, null);   
    pentahoRole = userRoleDao.getRole(mainTenant_1.getPath(), ROLE_5);
    assertEquals(pentahoRole.getDescription(), ROLE_DESCRIPTION_5);
    
    String changedDescription1 = ROLE_DESCRIPTION_5 + "change1";
    userRoleDao.setRoleDescription(mainTenant_1.getPath(), ROLE_5, changedDescription1);
    pentahoRole = userRoleDao.getRole(ROLE_5 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + mainTenant_1.getPath());
    assertEquals(changedDescription1, pentahoRole.getDescription());
    
    String changedDescription2 = ROLE_DESCRIPTION_5 + "change2";
    userRoleDao.setRoleDescription(ROLE_5 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + mainTenant_1.getPath(), changedDescription2);
    pentahoRole = userRoleDao.getRole(mainTenant_1.getPath(), ROLE_5);
    assertEquals(changedDescription2, pentahoRole.getDescription());
    
    userRoleDao.setRoleDescription(ROLE_5 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + mainTenant_1.getPath(), null);
    pentahoRole = userRoleDao.getRole(mainTenant_1.getPath(), ROLE_5);
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
      userRoleDao.setRoleDescription(mainTenant_1.getPath(), UNKNOWN_ROLE, changedDescription2);
      fail("Exception not thrown");
    } catch (NotFoundException ex) {
      // Expected exception
    }
    logout();
    login("systemTenantUser", mainTenant_2.getPath());

    try {
      changedDescription1 = ROLE_DESCRIPTION_5 + "change1";
      userRoleDao.setRoleDescription(mainTenant_1.getPath(), ROLE_5, changedDescription1);
      fail("Exception not thrown");
    } catch(Throwable th) {
      assertNotNull(th);
    }
    logout();
  }

  @Test
  public void testDeleteUser() throws Exception {
    ITenant systemTenantId = tenantManager.createSystemTenant(ServerRepositoryPaths.getPentahoRootFolderName());
    ITenant mainTenant_1 = tenantManager.createTenant(systemTenantId, MAIN_TENANT_1);
    ITenant mainTenant_2 = tenantManager.createTenant(systemTenantId, MAIN_TENANT_2);

    login("systemTenantUser", mainTenant_1.getPath());

    IPentahoUser pentahoUser = userRoleDao.createUser(mainTenant_1.getPath(), USER_6, PASSWORD_6, USER_DESCRIPTION_6, null);       
    pentahoUser = userRoleDao.getUser(USER_6 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + mainTenant_1.getPath());   
    assertNotNull(pentahoUser);
    
    logout();
    login("systemTenantUser", mainTenant_2.getPath());
    try {
      userRoleDao.deleteUser(pentahoUser);    
      fail("Exception not thrown");
    } catch (Throwable th) {
      assertNotNull(th);
    }    

    logout();
    login("systemTenantUser", mainTenant_1.getPath());

    userRoleDao.deleteUser(pentahoUser);
    
    pentahoUser = userRoleDao.getUser(USER_6 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + mainTenant_1.getPath());   
    assertNull(pentahoUser);
    assertEquals(0, userRoleDao.getUsers(mainTenant_1.getPath()).size());
   
    pentahoUser = userRoleDao.createUser(USER_6 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + mainTenant_1.getPath(), PASSWORD_6, USER_DESCRIPTION_6, null);   
    pentahoUser = userRoleDao.getUser(mainTenant_1.getPath(), USER_6);
    
    assertNotNull(pentahoUser);
    
    userRoleDao.deleteUser(pentahoUser);
    
    assertNull(userRoleDao.getUser(USER_6 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + mainTenant_1.getPath()));

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
      pentahoUser = new PentahoUser(mainTenant_1.getPath(), null, PASSWORD_6, USER_DESCRIPTION_6, true);
      userRoleDao.deleteUser(pentahoUser);    
      fail("Exception not thrown");
    } catch (NotFoundException e) {
      // Expected exception
    }
    
    try {
      pentahoUser = new PentahoUser(mainTenant_1.getPath(), UNKNOWN_USER, PASSWORD_6, USER_DESCRIPTION_6, true);
      userRoleDao.deleteUser(pentahoUser);    
      fail("Exception not thrown");
    } catch (NotFoundException e) {
      // Expected exception
    }

  }

  @Test
  public void testDeleteRole() throws Exception {
    ITenant systemTenantId = tenantManager.createSystemTenant(ServerRepositoryPaths.getPentahoRootFolderName());
    ITenant mainTenant_1 = tenantManager.createTenant(systemTenantId, MAIN_TENANT_1);
    ITenant mainTenant_2 = tenantManager.createTenant(systemTenantId, MAIN_TENANT_2);

    login("systemTenantUser", mainTenant_1.getPath());


    IPentahoRole pentahoRole = userRoleDao.createRole(mainTenant_1.getPath(), ROLE_6, ROLE_DESCRIPTION_6, null);       
    pentahoRole = userRoleDao.getRole(ROLE_6 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + mainTenant_1.getPath());   
    assertNotNull(pentahoRole);

    logout();
    login("systemTenantUser", mainTenant_2.getPath());
    try {
      userRoleDao.deleteRole(pentahoRole);
      fail("Exception not thrown");
    } catch (Throwable th) {
      assertNotNull(th);
    }    

    logout();
    login("systemTenantUser", mainTenant_1.getPath());

    userRoleDao.deleteRole(pentahoRole);
    pentahoRole = userRoleDao.getRole(ROLE_6 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + mainTenant_1.getPath());   
    assertNull(pentahoRole);
    assertEquals(0, userRoleDao.getRoles(mainTenant_1.getPath()).size());
   
    pentahoRole = userRoleDao.createRole(ROLE_6 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + mainTenant_1.getPath(), ROLE_DESCRIPTION_6, null);   
    pentahoRole = userRoleDao.getRole(mainTenant_1.getPath(), ROLE_6);
    
    assertNotNull(pentahoRole);
    
    userRoleDao.deleteRole(pentahoRole);
    
    assertNull(userRoleDao.getRole(ROLE_6 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + mainTenant_1.getPath()));
    
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
      pentahoRole = new PentahoRole(mainTenant_1.getPath(), null, ROLE_DESCRIPTION_6);
      userRoleDao.deleteRole(pentahoRole);    
      fail("Exception not thrown");
    } catch (NotFoundException e) {
      // Expected exception
    }
    
    try {
      pentahoRole = new PentahoRole(mainTenant_1.getPath(), UNKNOWN_ROLE, ROLE_DESCRIPTION_6);
      userRoleDao.deleteRole(pentahoRole);    
      fail("Exception not thrown");
    } catch (NotFoundException e) {
      // Expected exception
    }
  }
  
  @Test
  public void testGetUser() throws Exception {
    ITenant systemTenantId = tenantManager.createSystemTenant(ServerRepositoryPaths.getPentahoRootFolderName());
    ITenant mainTenant_1 = tenantManager.createTenant(systemTenantId, MAIN_TENANT_1);
    ITenant mainTenant_2 = tenantManager.createTenant(systemTenantId, MAIN_TENANT_2);

    login("systemTenantUser", mainTenant_1.getPath());

    assertNull(userRoleDao.getUser(UNKNOWN_TENANT, UNKNOWN_USER));   
    assertNull(userRoleDao.getUser(UNKNOWN_USER));
  }

  @Test
  public void testGetUsers() throws Exception {
    ITenant systemTenantId = tenantManager.createSystemTenant(ServerRepositoryPaths.getPentahoRootFolderName());
    ITenant mainTenant_1 = tenantManager.createTenant(systemTenantId, MAIN_TENANT_1);
    ITenant mainTenant_2 = tenantManager.createTenant(systemTenantId, MAIN_TENANT_2);

    login("systemTenantUser", mainTenant_1.getPath());

    userRoleDao.createUser(mainTenant_1.getPath(), USER_7, PASSWORD_7, USER_DESCRIPTION_7, null);       
    userRoleDao.createUser(mainTenant_1.getPath(), USER_8, PASSWORD_8, USER_DESCRIPTION_8, null);
    List<IPentahoUser> users = userRoleDao.getUsers(mainTenant_1.getPath());
    assertEquals(2, users.size());
    
    for (IPentahoUser user : users) {
      if (user.getUsername().equals(USER_7)) {
        assertEquals(user.getTenant(), mainTenant_1.getPath());
        assertEquals(user.getDescription(), USER_DESCRIPTION_7);
        assertEquals(user.isEnabled(), true);
      } else if (user.getUsername().equals(USER_8)) {
        assertEquals(user.getTenant(), mainTenant_1.getPath());
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
    ITenant systemTenantId = tenantManager.createSystemTenant(ServerRepositoryPaths.getPentahoRootFolderName());
    ITenant mainTenant_1 = tenantManager.createTenant(systemTenantId, MAIN_TENANT_1);
    ITenant mainTenant_2 = tenantManager.createTenant(systemTenantId, MAIN_TENANT_2);

    login("systemTenantUser", mainTenant_1.getPath());
    userRoleDao.createRole(mainTenant_1.getPath(), ROLE_7, ROLE_DESCRIPTION_7, null);       
    userRoleDao.createRole(mainTenant_1.getPath(), ROLE_8, ROLE_DESCRIPTION_8, null);
    List<IPentahoRole> roles = userRoleDao.getRoles(mainTenant_1.getPath());
    assertEquals(2, roles.size());
    
    for (IPentahoRole user : roles) {
      if (user.getName().equals(ROLE_7)) {
        assertEquals(user.getTenant(), mainTenant_1.getPath());
        assertEquals(user.getDescription(), ROLE_DESCRIPTION_7);
      } else if (user.getName().equals(ROLE_8)) {
        assertEquals(user.getTenant(), mainTenant_1.getPath());
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
    ITenant systemTenantId = tenantManager.createSystemTenant(ServerRepositoryPaths.getPentahoRootFolderName());
    ITenant mainTenant_1 = tenantManager.createTenant(systemTenantId, MAIN_TENANT_1);
    ITenant mainTenant_2 = tenantManager.createTenant(systemTenantId, MAIN_TENANT_2);
    login("systemTenantUser", mainTenant_1.getPath());
    
    userRoleDao.createRole(mainTenant_1.getPath(), ROLE_1, ROLE_DESCRIPTION_1, null);       
    userRoleDao.createRole(mainTenant_1.getPath(), ROLE_2, ROLE_DESCRIPTION_2, null);
    userRoleDao.createRole(mainTenant_1.getPath(), ROLE_3, ROLE_DESCRIPTION_3, null);
    userRoleDao.createUser(mainTenant_1.getPath(), USER_1, PASSWORD_1, USER_DESCRIPTION_1, new String[]{ROLE_1});
    userRoleDao.createUser(mainTenant_1.getPath(), USER_2, PASSWORD_2, USER_DESCRIPTION_2, new String[]{ROLE_1, ROLE_2});
    
    List<IPentahoUser> users = userRoleDao.getRoleMembers(mainTenant_1.getPath(), ROLE_2);
    assertEquals(1, users.size());
    assertEquals(USER_2, users.get(0).getUsername());
    
    ArrayList<String> expectedUserNames = new ArrayList<String>();
    expectedUserNames.add(USER_1);
    expectedUserNames.add(USER_2);
    ArrayList<String> actualUserNames = new ArrayList<String>();
    users = userRoleDao.getRoleMembers(ROLE_1 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + mainTenant_1.getPath());
    for (IPentahoUser user : users) {
      actualUserNames.add(user.getUsername());
    }
    assertEquals(2, actualUserNames.size());
    assertTrue(actualUserNames.containsAll(expectedUserNames));
        
    users = userRoleDao.getRoleMembers(mainTenant_1.getPath(), ROLE_3);
    assertEquals(0, users.size());
    
    userRoleDao.createUser(mainTenant_1.getPath(), USER_5, PASSWORD_5, USER_DESCRIPTION_5, null);       
    userRoleDao.createUser(mainTenant_1.getPath(), USER_6, PASSWORD_6, USER_DESCRIPTION_6, null);
    userRoleDao.createUser(mainTenant_1.getPath(), USER_7, PASSWORD_7, USER_DESCRIPTION_7, null);
    userRoleDao.createRole(mainTenant_1.getPath(), ROLE_5, ROLE_DESCRIPTION_6, new String[]{USER_5});
    userRoleDao.createRole(mainTenant_1.getPath(), ROLE_6, ROLE_DESCRIPTION_7, new String[]{USER_5, USER_6});
    
    List<IPentahoRole> roles = userRoleDao.getUserRoles(mainTenant_1.getPath(), USER_6);
    assertEquals(1, roles.size());
    assertEquals(ROLE_6, roles.get(0).getName());
    
    ArrayList<String> expectedRoleNames = new ArrayList<String>();
    expectedRoleNames.add(ROLE_5);
    expectedRoleNames.add(ROLE_6);
    ArrayList<String> actualRoleNames = new ArrayList<String>();
    roles = userRoleDao.getUserRoles(USER_5 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + mainTenant_1.getPath());
    for (IPentahoRole role : roles) {
      actualRoleNames.add(role.getName());
    }
    assertEquals(2, actualRoleNames.size());
    assertTrue(actualRoleNames.containsAll(expectedRoleNames));
        
    roles = userRoleDao.getUserRoles(mainTenant_1.getPath(), USER_7);
    assertEquals(0, roles.size());
    
    userRoleDao.setUserRoles(USER_7 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + mainTenant_1.getPath(), new String[]{ROLE_5, ROLE_6});
    roles = userRoleDao.getUserRoles(USER_7 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + mainTenant_1.getPath());
    actualRoleNames.clear();
    for (IPentahoRole role : roles) {
      actualRoleNames.add(role.getName());
    }
    assertEquals(2, actualRoleNames.size());
    assertTrue(actualRoleNames.containsAll(expectedRoleNames));
    
    userRoleDao.setRoleMembers(ROLE_3 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + mainTenant_1.getPath(), new String[]{USER_1, USER_2});
    users = userRoleDao.getRoleMembers(ROLE_3 + DefaultTenantedPrincipleNameUtils.DEFAULT_DELIMETER + mainTenant_1.getPath());
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
