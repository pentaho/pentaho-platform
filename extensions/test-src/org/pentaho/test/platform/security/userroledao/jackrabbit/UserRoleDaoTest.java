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
package org.pentaho.test.platform.security.userroledao.jackrabbit;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Repository;

import org.apache.commons.io.FileUtils;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.NameFactory;
import org.apache.jackrabbit.spi.commons.name.NameFactoryImpl;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.security.userroledao.AlreadyExistsException;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoRole;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoUser;
import org.pentaho.platform.api.engine.security.userroledao.IUserRoleDao;
import org.pentaho.platform.api.engine.security.userroledao.NotFoundException;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.mt.ITenantManager;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.pentaho.platform.api.repository2.unified.IBackingRepositoryLifecycleManager;
import org.pentaho.platform.core.mt.Tenant;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.repository2.unified.IRepositoryFileDao;
import org.pentaho.platform.repository2.unified.ServerRepositoryPaths;
import org.pentaho.platform.repository2.unified.jcr.SimpleJcrTestUtils;
import org.pentaho.platform.repository2.unified.jcr.jackrabbit.security.TestPrincipalProvider;
import org.pentaho.platform.repository2.unified.jcr.sejcr.CredentialsStrategy;
import org.pentaho.platform.security.policy.rolebased.IRoleAuthorizationPolicyRoleBindingDao;
import org.pentaho.platform.security.userroledao.DefaultTenantedPrincipleNameResolver;
import org.pentaho.platform.security.userroledao.PentahoRole;
import org.pentaho.platform.security.userroledao.PentahoUser;
import org.pentaho.test.platform.engine.core.MicroPlatform;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.extensions.jcr.JcrTemplate;
import org.springframework.extensions.jcr.SessionFactory;
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
 * Unit test for {@link UserRoleDao}.
 * 
 * @author mlowery
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/repository.spring.xml",
    "classpath:/repository-test-override.spring.xml" })
@SuppressWarnings("nls")
public class UserRoleDaoTest implements ApplicationContextAware {
  
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
  

  public static final ITenant UNKNOWN_TENANT = new Tenant("unknownTenant", true); //$NON-NLS-1$
  
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
  String pPrincipalName;
  IUserRoleDao userRoleDaoProxy;
  IUserRoleDao userRoleDaoTestProxy;
  private ITenantManager tenantManager;
  private String repositoryAdminUsername;
  private String tenantAdminRoleName;
  private String tenantAuthenticatedRoleName;
  private String sysAdminRoleName;
  private String sysAdminUserName;
  private JcrTemplate testJcrTemplate;
  private IBackingRepositoryLifecycleManager manager;
  private IRoleAuthorizationPolicyRoleBindingDao roleBindingDaoTarget;
  private IAuthorizationPolicy authorizationPolicy;
  private MicroPlatform mp;
  private IRepositoryFileDao repositoryFileDao;
  private ITenantedPrincipleNameResolver tenantedRoleNameUtils;
  private ITenantedPrincipleNameResolver tenantedUserNameUtils;
  
  private ITenant systemTenant;
  private ITenant mainTenant_1;  
  private ITenant mainTenant_2;
  private ITenant subTenant1_1;
  private ITenant subTenant1_2;
  private ITenant subTenant1_1_1;
  private ITenant subTenant1_1_2;
  private ITenant subTenant1_2_1;
  private ITenant subTenant1_2_2;
  private ITenant subTenant2_1;
  private ITenant subTenant2_2;
  private ITenant subTenant2_1_1;
  private ITenant subTenant2_1_2;
  private ITenant subTenant2_2_1;
  private ITenant subTenant2_2_2;
  
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
    mp.defineInstance("tenantedUserNameUtils", tenantedUserNameUtils);
    mp.defineInstance("tenantedRoleNameUtils", tenantedRoleNameUtils);
    mp.defineInstance("roleAuthorizationPolicyRoleBindingDaoTarget", roleBindingDaoTarget);

    // Start the micro-platform
    mp.start();
    logout();
    startupCalled = true;
  }

  @After
  public void tearDown() throws Exception {
    // null out fields to get back memory
    authorizationPolicy = null;
    pPrincipalName = null;
    userRoleDaoProxy = null;
    userRoleDaoTestProxy = null;
    tenantManager = null;
    repositoryAdminUsername = null;
    tenantAdminRoleName = null;
    tenantAuthenticatedRoleName = null;
    sysAdminRoleName = null;
    sysAdminUserName = null;
    testJcrTemplate = null;
    roleBindingDaoTarget = null;
    authorizationPolicy = null;
    mp = null;
    repositoryFileDao = null;
    tenantedRoleNameUtils = null;
    tenantedUserNameUtils = null;
    systemTenant = null;
    mainTenant_1 = null;  
    mainTenant_2 = null;
    subTenant1_1 = null;
    subTenant1_2 = null;
    subTenant1_1_1 = null;
    subTenant1_1_2 = null;
    subTenant1_2_1 = null;
    subTenant1_2_2 = null;
    subTenant2_1 = null;
    subTenant2_2 = null;
    subTenant2_1_1 = null;
    subTenant2_1_2 = null;
    subTenant2_2_1 = null;
    subTenant2_2_2 = null;
    if (startupCalled) {
      manager.shutdown();
    }
    tenantManager = null;
  }

  private void cleanupUserAndRoles(final ITenant tenant) {
    loginAsRepositoryAdmin();
    for (IPentahoRole role : userRoleDaoTestProxy.getRoles(tenant)) {
      userRoleDaoTestProxy.deleteRole(role);
    }
    for (IPentahoUser user : userRoleDaoTestProxy.getUsers(tenant)) {
      userRoleDaoTestProxy.deleteUser(user);
    }
  }
  
  protected void loginAsRepositoryAdmin() {
    StandaloneSession pentahoSession = new StandaloneSession(repositoryAdminUsername);
    pentahoSession.setAuthenticated(repositoryAdminUsername);
    final GrantedAuthority[] repositoryAdminAuthorities = new GrantedAuthority[]{new GrantedAuthorityImpl(sysAdminRoleName)};
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


  protected void login(final String username, final ITenant tenant, String[] roles) {
    StandaloneSession pentahoSession = new StandaloneSession(username);
    pentahoSession.setAuthenticated(tenant.getId(), username);
    PentahoSessionHolder.setSession(pentahoSession);
    pentahoSession.setAttribute(IPentahoSession.TENANT_ID_KEY, tenant.getId());
    final String password = "password";

    List<GrantedAuthority> authList = new ArrayList<GrantedAuthority>();

    for (String roleName : roles) {
      authList.add(new GrantedAuthorityImpl(roleName));
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
    tenantAuthenticatedRoleName = (String) applicationContext
        .getBean("tenantAuthenticatedAuthorityNamePattern");
    tenantAdminRoleName = (String) applicationContext.getBean("tenantAdminAuthorityNamePattern");
    roleBindingDaoTarget = (IRoleAuthorizationPolicyRoleBindingDao) applicationContext.getBean("roleAuthorizationPolicyRoleBindingDaoTarget");
    sysAdminRoleName = (String) applicationContext.getBean("superAdminAuthorityName");
    sysAdminUserName = (String) applicationContext.getBean("superAdminUserName");
    authorizationPolicy = (IAuthorizationPolicy) applicationContext
        .getBean("authorizationPolicy");
    tenantManager = (ITenantManager) applicationContext.getBean("tenantMgrProxy");
    repositoryFileDao = (IRepositoryFileDao) applicationContext.getBean("repositoryFileDao");
    userRoleDaoProxy = (IUserRoleDao) applicationContext.getBean("userRoleDaoTxn");
    userRoleDaoTestProxy = (IUserRoleDao) applicationContext.getBean("userRoleDaoTxn");
    tenantedUserNameUtils = (ITenantedPrincipleNameResolver) applicationContext.getBean("tenantedUserNameUtils");
    tenantedRoleNameUtils = (ITenantedPrincipleNameResolver) applicationContext.getBean("tenantedRoleNameUtils");
    TestPrincipalProvider.userRoleDao = (IUserRoleDao) applicationContext.getBean("userRoleDaoTxn");
    TestPrincipalProvider.adminCredentialsStrategy = (CredentialsStrategy) applicationContext.getBean("jcrAdminCredentialsStrategy");
    TestPrincipalProvider.repository = (Repository)applicationContext.getBean("jcrRepository");
  }

  
  @Test
  public void testDummy() {
    
  }
  
  @Ignore
  public void testGetUserWithSubTenant() throws Exception {
    loginAsRepositoryAdmin();
    systemTenant = tenantManager.createTenant(null, ServerRepositoryPaths.getPentahoRootFolderName(), tenantAdminRoleName, tenantAuthenticatedRoleName, "Anonymous");
    userRoleDaoProxy.createUser(systemTenant, sysAdminUserName, "password", "", new String[]{tenantAdminRoleName});
    login(sysAdminUserName, systemTenant, new String[]{tenantAdminRoleName, tenantAuthenticatedRoleName});
    
    mainTenant_1 = tenantManager.createTenant(systemTenant, MAIN_TENANT_1, tenantAdminRoleName, tenantAuthenticatedRoleName, "Anonymous");
    userRoleDaoProxy.createUser(mainTenant_1, "joe", "password", "", new String[]{tenantAdminRoleName});
    
    mainTenant_2 = tenantManager.createTenant(systemTenant, MAIN_TENANT_2, tenantAdminRoleName, tenantAuthenticatedRoleName, "Anonymous");
    userRoleDaoProxy.createUser(mainTenant_2, "joe", "password", "", new String[]{tenantAdminRoleName});
    
    subTenant1_1 = tenantManager.createTenant(mainTenant_1, SUB_TENANT1_1, tenantAdminRoleName, tenantAuthenticatedRoleName, "Anonymous");
    userRoleDaoProxy.createUser(subTenant1_1, "joe", "password", "", new String[]{tenantAdminRoleName});
    
    subTenant1_2 = tenantManager.createTenant(mainTenant_1, SUB_TENANT1_2, tenantAdminRoleName, tenantAuthenticatedRoleName, "Anonymous");
    userRoleDaoProxy.createUser(subTenant1_2, "joe", "password", "", new String[]{tenantAdminRoleName});
    
    subTenant1_1_1 = tenantManager.createTenant(subTenant1_1, SUB_TENANT1_1_1, tenantAdminRoleName, tenantAuthenticatedRoleName, "Anonymous");
    userRoleDaoProxy.createUser(subTenant1_1_1, "joe", "password", "", new String[]{tenantAdminRoleName});
    
    subTenant1_1_2 = tenantManager.createTenant(subTenant1_1, SUB_TENANT1_1_2, tenantAdminRoleName, tenantAuthenticatedRoleName, "Anonymous");
    userRoleDaoProxy.createUser(subTenant1_1_2, "joe", "password", "", new String[]{tenantAdminRoleName});
    
    subTenant1_2_1 = tenantManager.createTenant(subTenant1_2, SUB_TENANT1_2_1, tenantAdminRoleName, tenantAuthenticatedRoleName, "Anonymous");
    userRoleDaoProxy.createUser(subTenant1_2_1, "joe", "password", "", new String[]{tenantAdminRoleName});
    
    subTenant1_2_2 = tenantManager.createTenant(subTenant1_2, SUB_TENANT1_2_2, tenantAdminRoleName, tenantAuthenticatedRoleName, "Anonymous");
    userRoleDaoProxy.createUser(subTenant1_2_2, "joe", "password", "", new String[]{tenantAdminRoleName});
    
    subTenant2_1 = tenantManager.createTenant(mainTenant_2, SUB_TENANT2_1, tenantAdminRoleName, tenantAuthenticatedRoleName, "Anonymous");
    userRoleDaoProxy.createUser(subTenant2_1, "joe", "password", "", new String[]{tenantAdminRoleName});
    
    subTenant2_2 = tenantManager.createTenant(mainTenant_2, SUB_TENANT2_2, tenantAdminRoleName, tenantAuthenticatedRoleName, "Anonymous");
    userRoleDaoProxy.createUser(subTenant2_2, "joe", "password", "", new String[]{tenantAdminRoleName});
    
    subTenant2_1_1 = tenantManager.createTenant(subTenant2_1, SUB_TENANT2_1_1, tenantAdminRoleName, tenantAuthenticatedRoleName, "Anonymous");
    userRoleDaoProxy.createUser(subTenant2_1_1, "joe", "password", "", new String[]{tenantAdminRoleName});
    
    subTenant2_1_2 = tenantManager.createTenant(subTenant2_1, SUB_TENANT2_1_2, tenantAdminRoleName, tenantAuthenticatedRoleName, "Anonymous");
    userRoleDaoProxy.createUser(subTenant2_1_2, "joe", "password", "", new String[]{tenantAdminRoleName});
    
    subTenant2_2_1 = tenantManager.createTenant(subTenant2_2, SUB_TENANT2_2_1, tenantAdminRoleName, tenantAuthenticatedRoleName, "Anonymous");
    userRoleDaoProxy.createUser(subTenant2_2_1, "joe", "password", "", new String[]{tenantAdminRoleName});
    
    subTenant2_2_2 = tenantManager.createTenant(subTenant2_2, SUB_TENANT2_2_2, tenantAdminRoleName, tenantAuthenticatedRoleName, "Anonymous");
    userRoleDaoProxy.createUser(subTenant2_2_2, "joe", "password", "", new String[]{tenantAdminRoleName});
    
    login("joe", mainTenant_1, new String[]{tenantAdminRoleName, tenantAuthenticatedRoleName});
    IPentahoUser pentahoUser = userRoleDaoProxy.createUser(subTenant1_1, USER_2, PASSWORD_2, USER_DESCRIPTION_2, null);
    pentahoUser = userRoleDaoProxy.createUser(subTenant1_2, USER_3, PASSWORD_3, USER_DESCRIPTION_3, null);
    pentahoUser = userRoleDaoProxy.createUser(subTenant1_1_1, USER_4, PASSWORD_2, USER_DESCRIPTION_2, null);
    pentahoUser = userRoleDaoProxy.createUser(subTenant1_1_2, USER_5, PASSWORD_3, USER_DESCRIPTION_3, null);
    pentahoUser = userRoleDaoProxy.createUser(subTenant1_2_1, USER_6, PASSWORD_2, USER_DESCRIPTION_2, null);
    pentahoUser = userRoleDaoProxy.createUser(subTenant1_2_2, USER_7, PASSWORD_3, USER_DESCRIPTION_3, null);

    int DEFAULT_TENANT_USER_COUNT = 1;
    int DEFAULT_TENANT_COUNT = 6;
    List<IPentahoUser> usersWithSubTenant = userRoleDaoProxy.getUsers(mainTenant_1, true);
    assertEquals(usersWithSubTenant.size(), 7 + DEFAULT_TENANT_USER_COUNT * DEFAULT_TENANT_COUNT);
    List<IPentahoUser> usersWithoutSubTenant = userRoleDaoProxy.getUsers(mainTenant_1, false);
    assertEquals(usersWithoutSubTenant.size(), 1);

    DEFAULT_TENANT_COUNT = 3;
    usersWithSubTenant = userRoleDaoProxy.getUsers(subTenant1_1, true);
    assertEquals(usersWithSubTenant.size(), 3 + DEFAULT_TENANT_USER_COUNT * DEFAULT_TENANT_COUNT);

    usersWithSubTenant = userRoleDaoProxy.getUsers(subTenant1_2, true);
    assertEquals(usersWithSubTenant.size(), 3 + DEFAULT_TENANT_USER_COUNT * DEFAULT_TENANT_COUNT);

    usersWithoutSubTenant = userRoleDaoProxy.getUsers(subTenant1_1, false);
    assertEquals(usersWithoutSubTenant.size(), 1 + DEFAULT_TENANT_USER_COUNT);

    usersWithoutSubTenant = userRoleDaoProxy.getUsers(subTenant1_2, false);
    assertEquals(usersWithoutSubTenant.size(), 1 + DEFAULT_TENANT_USER_COUNT);

    logout();

    login("joe", mainTenant_2, new String[]{tenantAdminRoleName, tenantAuthenticatedRoleName});
    
    pentahoUser = userRoleDaoProxy.createUser(mainTenant_2, USER_8, PASSWORD_8, USER_DESCRIPTION_8, null);
    pentahoUser = userRoleDaoProxy.createUser(subTenant2_1, USER_9, PASSWORD_9, USER_DESCRIPTION_9, null);
    pentahoUser = userRoleDaoProxy.createUser(subTenant2_2, USER_10, PASSWORD_10, USER_DESCRIPTION_10, null);
    pentahoUser = userRoleDaoProxy.createUser(subTenant2_1_1, USER_11, PASSWORD_11, USER_DESCRIPTION_11, null);
    pentahoUser = userRoleDaoProxy.createUser(subTenant2_1_2, USER_12, PASSWORD_12, USER_DESCRIPTION_12, null);
    pentahoUser = userRoleDaoProxy.createUser(subTenant2_2_1, USER_13, PASSWORD_13, USER_DESCRIPTION_13, null);
    pentahoUser = userRoleDaoProxy.createUser(subTenant2_2_2, USER_14, PASSWORD_14, USER_DESCRIPTION_14, null);

    DEFAULT_TENANT_USER_COUNT = 1;
    DEFAULT_TENANT_COUNT = 7;

    usersWithSubTenant = userRoleDaoProxy.getUsers(mainTenant_2, true);
    assertEquals(usersWithSubTenant.size(), 7 + DEFAULT_TENANT_USER_COUNT * DEFAULT_TENANT_COUNT);
    usersWithoutSubTenant = userRoleDaoProxy.getUsers(mainTenant_2, false);
    assertEquals(usersWithoutSubTenant.size(), 1+ DEFAULT_TENANT_USER_COUNT);

    DEFAULT_TENANT_COUNT = 3;
    usersWithSubTenant = userRoleDaoProxy.getUsers(subTenant2_1, true);
    assertEquals(usersWithSubTenant.size(), 3 + DEFAULT_TENANT_USER_COUNT * DEFAULT_TENANT_COUNT);

    usersWithSubTenant = userRoleDaoProxy.getUsers(subTenant2_2, true);
    assertEquals(usersWithSubTenant.size(), 3 + DEFAULT_TENANT_USER_COUNT * DEFAULT_TENANT_COUNT);

    usersWithoutSubTenant = userRoleDaoProxy.getUsers(subTenant2_1, false);
    assertEquals(usersWithoutSubTenant.size(), 1 + DEFAULT_TENANT_USER_COUNT);

    usersWithoutSubTenant = userRoleDaoProxy.getUsers(subTenant2_2, false);
    assertEquals(usersWithoutSubTenant.size(), 1 + DEFAULT_TENANT_USER_COUNT);

    logout();
    
    cleanupUserAndRoles(mainTenant_1);
    cleanupUserAndRoles(mainTenant_2);
    cleanupUserAndRoles(subTenant1_1);
    cleanupUserAndRoles(subTenant1_1_1);
    cleanupUserAndRoles(subTenant1_1_2);
    cleanupUserAndRoles(subTenant1_2);
    cleanupUserAndRoles(subTenant1_2_1);
    cleanupUserAndRoles(subTenant1_2_2);
    cleanupUserAndRoles(subTenant2_1);
    cleanupUserAndRoles(subTenant2_1_1);
    cleanupUserAndRoles(subTenant2_1_2);
    cleanupUserAndRoles(subTenant2_2);
    cleanupUserAndRoles(subTenant2_2_1);
    cleanupUserAndRoles(subTenant2_2_2);
    cleanupUserAndRoles(systemTenant);
   }  

  @Ignore
  public void testGetRolesWithSubTenant() throws Exception {
    loginAsRepositoryAdmin();
    systemTenant = tenantManager.createTenant(null, ServerRepositoryPaths.getPentahoRootFolderName(), tenantAdminRoleName, tenantAuthenticatedRoleName, "Anonymous");
    userRoleDaoProxy.createUser(systemTenant, sysAdminUserName, "password", "", new String[]{tenantAdminRoleName});
    
    login(sysAdminUserName, systemTenant, new String[]{tenantAdminRoleName, tenantAuthenticatedRoleName});
    
    mainTenant_1 = tenantManager.createTenant(systemTenant, MAIN_TENANT_1, tenantAdminRoleName, tenantAuthenticatedRoleName, "Anonymous");
    userRoleDaoProxy.createUser(mainTenant_1, "joe", "password", "", new String[]{tenantAdminRoleName});
    
    mainTenant_2 = tenantManager.createTenant(systemTenant, MAIN_TENANT_2, tenantAdminRoleName, tenantAuthenticatedRoleName, "Anonymous");
    userRoleDaoProxy.createUser(mainTenant_2, "joe", "password", "", new String[]{tenantAdminRoleName});
    
    subTenant1_1 = tenantManager.createTenant(mainTenant_1, SUB_TENANT1_1, tenantAdminRoleName, tenantAuthenticatedRoleName, "Anonymous");
    userRoleDaoProxy.createUser(subTenant1_1, "joe", "password", "", new String[]{tenantAdminRoleName});
    
    subTenant1_2 = tenantManager.createTenant(mainTenant_1, SUB_TENANT1_2, tenantAdminRoleName, tenantAuthenticatedRoleName, "Anonymous");
    userRoleDaoProxy.createUser(subTenant1_2, "joe", "password", "", new String[]{tenantAdminRoleName});
    
    subTenant1_1_1 = tenantManager.createTenant(subTenant1_1, SUB_TENANT1_1_1, tenantAdminRoleName, tenantAuthenticatedRoleName, "Anonymous");
    userRoleDaoProxy.createUser(subTenant1_1_1, "joe", "password", "", new String[]{tenantAdminRoleName});
    
    subTenant1_1_2 = tenantManager.createTenant(subTenant1_1, SUB_TENANT1_1_2, tenantAdminRoleName, tenantAuthenticatedRoleName, "Anonymous");
    userRoleDaoProxy.createUser(subTenant1_1_2, "joe", "password", "", new String[]{tenantAdminRoleName});
    
    subTenant1_2_1 = tenantManager.createTenant(subTenant1_2, SUB_TENANT1_2_1, tenantAdminRoleName, tenantAuthenticatedRoleName, "Anonymous");
    userRoleDaoProxy.createUser(subTenant1_2_1, "joe", "password", "", new String[]{tenantAdminRoleName});
    
    subTenant1_2_2 = tenantManager.createTenant(subTenant1_2, SUB_TENANT1_2_2, tenantAdminRoleName, tenantAuthenticatedRoleName, "Anonymous");
    userRoleDaoProxy.createUser(subTenant1_2_2, "joe", "password", "", new String[]{tenantAdminRoleName});
    
    subTenant2_1 = tenantManager.createTenant(mainTenant_2, SUB_TENANT2_1, tenantAdminRoleName, tenantAuthenticatedRoleName, "Anonymous");
    userRoleDaoProxy.createUser(subTenant2_1, "joe", "password", "", new String[]{tenantAdminRoleName});
    
    subTenant2_2 = tenantManager.createTenant(mainTenant_2, SUB_TENANT2_2, tenantAdminRoleName, tenantAuthenticatedRoleName, "Anonymous");
    userRoleDaoProxy.createUser(subTenant2_2, "joe", "password", "", new String[]{tenantAdminRoleName});
    
    subTenant2_1_1 = tenantManager.createTenant(subTenant2_1, SUB_TENANT2_1_1, tenantAdminRoleName, tenantAuthenticatedRoleName, "Anonymous");
    userRoleDaoProxy.createUser(subTenant2_1_1, "joe", "password", "", new String[]{tenantAdminRoleName});
    
    subTenant2_1_2 = tenantManager.createTenant(subTenant2_1, SUB_TENANT2_1_2, tenantAdminRoleName, tenantAuthenticatedRoleName, "Anonymous");
    userRoleDaoProxy.createUser(subTenant2_1_2, "joe", "password", "", new String[]{tenantAdminRoleName});
    
    subTenant2_2_1 = tenantManager.createTenant(subTenant2_2, SUB_TENANT2_2_1, tenantAdminRoleName, tenantAuthenticatedRoleName, "Anonymous");
    userRoleDaoProxy.createUser(subTenant2_2_1, "joe", "password", "", new String[]{tenantAdminRoleName});
    
    subTenant2_2_2 = tenantManager.createTenant(subTenant2_2, SUB_TENANT2_2_2, tenantAdminRoleName, tenantAuthenticatedRoleName, "Anonymous");
    userRoleDaoProxy.createUser(subTenant2_2_2, "joe", "password", "", new String[]{tenantAdminRoleName});
    
    login("joe", mainTenant_1, new String[]{tenantAdminRoleName, tenantAuthenticatedRoleName});
    IPentahoRole pentahoRole = userRoleDaoProxy.createRole(mainTenant_1, ROLE_1, ROLE_DESCRIPTION_1, null);
    pentahoRole = userRoleDaoProxy.createRole(subTenant1_1, ROLE_2, ROLE_DESCRIPTION_2, null);
    pentahoRole = userRoleDaoProxy.createRole(subTenant1_2, ROLE_3, ROLE_DESCRIPTION_3, null);
    pentahoRole = userRoleDaoProxy.createRole(subTenant1_1_1, ROLE_4, ROLE_DESCRIPTION_4, null);
    pentahoRole = userRoleDaoProxy.createRole(subTenant1_1_2, ROLE_5, ROLE_DESCRIPTION_5, null);
    pentahoRole = userRoleDaoProxy.createRole(subTenant1_2_1, ROLE_6, ROLE_DESCRIPTION_6, null);
    pentahoRole = userRoleDaoProxy.createRole(subTenant1_2_2, ROLE_7, ROLE_DESCRIPTION_7, null);
    
    int DEFAULT_ROLE_COUNT = 3;
    int TOTAL_ROLE_COUNT = 7;
    List<IPentahoRole> rolesWithSubTenant = userRoleDaoProxy.getRoles(mainTenant_1, true);
    assertEquals(rolesWithSubTenant.size(), TOTAL_ROLE_COUNT + DEFAULT_ROLE_COUNT * TOTAL_ROLE_COUNT);
    List<IPentahoRole> rolesWithoutSubTenant = userRoleDaoProxy.getRoles(mainTenant_1, false);
    assertEquals(rolesWithoutSubTenant.size(), 1 + DEFAULT_ROLE_COUNT);
    
    logout();

    login("joe", mainTenant_2, new String[]{tenantAdminRoleName, tenantAuthenticatedRoleName});
    
    pentahoRole = userRoleDaoProxy.createRole(mainTenant_2, ROLE_8, ROLE_DESCRIPTION_8, null);
    pentahoRole = userRoleDaoProxy.createRole(subTenant2_1, ROLE_9, ROLE_DESCRIPTION_9, null);
    pentahoRole = userRoleDaoProxy.createRole(subTenant2_2, ROLE_10, ROLE_DESCRIPTION_10, null);
    pentahoRole = userRoleDaoProxy.createRole(subTenant2_1_1, ROLE_11, ROLE_DESCRIPTION_11, null);
    pentahoRole = userRoleDaoProxy.createRole(subTenant2_1_2, ROLE_12, ROLE_DESCRIPTION_12, null);
    pentahoRole = userRoleDaoProxy.createRole(subTenant2_2_1, ROLE_13, ROLE_DESCRIPTION_13, null);
    pentahoRole = userRoleDaoProxy.createRole(subTenant2_2_2, ROLE_14, ROLE_DESCRIPTION_14, null);
 

    rolesWithSubTenant = userRoleDaoProxy.getRoles(mainTenant_2, true);
    assertEquals(rolesWithSubTenant.size(), TOTAL_ROLE_COUNT + DEFAULT_ROLE_COUNT * TOTAL_ROLE_COUNT);
    rolesWithoutSubTenant = userRoleDaoProxy.getRoles(mainTenant_2, false);
    assertEquals(rolesWithoutSubTenant.size(), 1 + DEFAULT_ROLE_COUNT);
    
    TOTAL_ROLE_COUNT =  3;
    
    rolesWithSubTenant = userRoleDaoProxy.getRoles(subTenant2_1, true);
    assertEquals(rolesWithSubTenant.size(), TOTAL_ROLE_COUNT + DEFAULT_ROLE_COUNT * TOTAL_ROLE_COUNT);

    rolesWithSubTenant = userRoleDaoProxy.getRoles(subTenant2_2, true);
    assertEquals(rolesWithSubTenant.size(), TOTAL_ROLE_COUNT + DEFAULT_ROLE_COUNT * TOTAL_ROLE_COUNT);

    rolesWithoutSubTenant = userRoleDaoProxy.getRoles(subTenant2_1, false);
    assertEquals(rolesWithoutSubTenant.size(), 1 + DEFAULT_ROLE_COUNT);

    rolesWithoutSubTenant = userRoleDaoProxy.getRoles(subTenant2_2, false);
    assertEquals(rolesWithoutSubTenant.size(), 1 + DEFAULT_ROLE_COUNT);
    logout();
    
    cleanupUserAndRoles(mainTenant_1);
    cleanupUserAndRoles(mainTenant_2);
    cleanupUserAndRoles(subTenant1_1);
    cleanupUserAndRoles(subTenant1_1_1);
    cleanupUserAndRoles(subTenant1_1_2);
    cleanupUserAndRoles(subTenant1_2);
    cleanupUserAndRoles(subTenant1_2_1);
    cleanupUserAndRoles(subTenant1_2_2);
    cleanupUserAndRoles(subTenant2_1);
    cleanupUserAndRoles(subTenant2_1_1);
    cleanupUserAndRoles(subTenant2_1_2);
    cleanupUserAndRoles(subTenant2_2);
    cleanupUserAndRoles(subTenant2_2_1);
    cleanupUserAndRoles(subTenant2_2_2);
    cleanupUserAndRoles(systemTenant);
  }  

  @Ignore
  public void testCreateUser() throws Exception {
    loginAsRepositoryAdmin();
    systemTenant = tenantManager.createTenant(null, ServerRepositoryPaths.getPentahoRootFolderName(), tenantAdminRoleName, tenantAuthenticatedRoleName, "Anonymous");
    userRoleDaoProxy.createUser(systemTenant, sysAdminUserName, "password", "", new String[]{tenantAdminRoleName});
    
    login(sysAdminUserName, systemTenant, new String[]{tenantAdminRoleName, tenantAuthenticatedRoleName});
    
    mainTenant_1 = tenantManager.createTenant(systemTenant, MAIN_TENANT_1, tenantAdminRoleName, tenantAuthenticatedRoleName, "Anonymous");
    userRoleDaoProxy.createUser(mainTenant_1, "joe", "password", "", new String[]{tenantAdminRoleName});
    
    mainTenant_2 = tenantManager.createTenant(systemTenant, MAIN_TENANT_2, tenantAdminRoleName, tenantAuthenticatedRoleName, "Anonymous");
    userRoleDaoProxy.createUser(mainTenant_2, "joe", "password", "", new String[]{tenantAdminRoleName});
    
    subTenant1_1 = tenantManager.createTenant(mainTenant_1, SUB_TENANT1_1, tenantAdminRoleName, tenantAuthenticatedRoleName, "Anonymous");
    userRoleDaoProxy.createUser(subTenant1_1, "joe", "password", "", new String[]{tenantAdminRoleName});
    
    subTenant1_2 = tenantManager.createTenant(mainTenant_1, SUB_TENANT1_2, tenantAdminRoleName, tenantAuthenticatedRoleName, "Anonymous");
    userRoleDaoProxy.createUser(subTenant1_2, "joe", "password", "", new String[]{tenantAdminRoleName});
    
    subTenant2_1 = tenantManager.createTenant(mainTenant_2, SUB_TENANT2_1, tenantAdminRoleName, tenantAuthenticatedRoleName, "Anonymous");
    userRoleDaoProxy.createUser(subTenant2_1, "joe", "password", "", new String[]{tenantAdminRoleName});
    
    subTenant2_2 = tenantManager.createTenant(mainTenant_2, SUB_TENANT2_2, tenantAdminRoleName, tenantAuthenticatedRoleName, "Anonymous");
    userRoleDaoProxy.createUser(subTenant2_2, "joe", "password", "", new String[]{tenantAdminRoleName});
    

    login("joe", mainTenant_1, new String[]{tenantAdminRoleName, tenantAuthenticatedRoleName});
    List<IPentahoUser> users = userRoleDaoProxy.getUsers(mainTenant_1);
    IPentahoUser pentahoUser = userRoleDaoProxy.createUser(mainTenant_1, USER_2, PASSWORD_2, USER_DESCRIPTION_2, null);
    pentahoUser = userRoleDaoProxy.getUser(mainTenant_1, USER_2);
    assertEquals(pentahoUser.getTenant(), mainTenant_1);
    assertEquals(pentahoUser.getUsername(), USER_2);
    assertEquals(pentahoUser.getDescription(), USER_DESCRIPTION_2);
    assertEquals(pentahoUser.isEnabled(), true);
    logout();
    login("joe", subTenant2_1, new String[]{tenantAdminRoleName, tenantAuthenticatedRoleName});
    try {
      pentahoUser = userRoleDaoProxy.createUser(mainTenant_1, USER_2, PASSWORD_2, USER_DESCRIPTION_2, null);
      fail("Exception not thrown");
    } catch(Throwable th) {
      assertNotNull(th);
    }
    logout();
    login("joe", mainTenant_1, new String[]{tenantAdminRoleName, tenantAuthenticatedRoleName});

    users = userRoleDaoProxy.getUsers(mainTenant_1);
    int DEFAULT_USER_COUNT = 1;
    assertTrue(users.size() == 1 + DEFAULT_USER_COUNT);
    pentahoUser = users.get(1);    
    assertEquals(pentahoUser.getTenant(), mainTenant_1);
    assertEquals(pentahoUser.getUsername(), USER_2);
    assertEquals(pentahoUser.getDescription(), USER_DESCRIPTION_2);
    assertEquals(pentahoUser.isEnabled(), true);
    
    logout();
    login("joe", mainTenant_2, new String[]{tenantAdminRoleName, tenantAuthenticatedRoleName});
    pentahoUser = userRoleDaoProxy.createUser(mainTenant_2, USER_2, PASSWORD_2, USER_DESCRIPTION_2, null);
    
    logout();
    login("joe", subTenant1_1, new String[]{tenantAdminRoleName, tenantAuthenticatedRoleName});
    try {
      pentahoUser = userRoleDaoProxy.createUser(mainTenant_2, USER_2, PASSWORD_2, USER_DESCRIPTION_2, null);
      fail("Exception not thrown");
    } catch(Throwable th) {
      assertNotNull(th);
    }

    logout();
    login("joe", mainTenant_2, new String[]{tenantAdminRoleName, tenantAuthenticatedRoleName});

    pentahoUser = userRoleDaoProxy.getUser(mainTenant_2, USER_2);
    assertEquals(pentahoUser.getTenant(), mainTenant_2);
    assertEquals(pentahoUser.getUsername(), USER_2);
    assertEquals(pentahoUser.getDescription(), USER_DESCRIPTION_2);
    assertEquals(pentahoUser.isEnabled(), true);

    users = userRoleDaoProxy.getUsers(mainTenant_2);
    assertTrue(users.size() == 1+DEFAULT_USER_COUNT);
    pentahoUser = users.get(1);    
    assertEquals(pentahoUser.getTenant(), mainTenant_2);
    assertEquals(pentahoUser.getUsername(), USER_2);
    assertEquals(pentahoUser.getDescription(), USER_DESCRIPTION_2);
    assertEquals(pentahoUser.isEnabled(), true);
    
    logout();
    login("joe", subTenant2_1, new String[]{tenantAdminRoleName, tenantAuthenticatedRoleName});

    pentahoUser = userRoleDaoProxy.createUser(null, USER_3 + DefaultTenantedPrincipleNameResolver.DEFAULT_DELIMETER + subTenant2_1.getRootFolderAbsolutePath(), PASSWORD_3, USER_DESCRIPTION_3, null);
    
    pentahoUser = userRoleDaoProxy.getUser(null, USER_3 + DefaultTenantedPrincipleNameResolver.DEFAULT_DELIMETER + subTenant2_1.getRootFolderAbsolutePath());
    assertEquals(pentahoUser.getTenant(), subTenant2_1);
    assertEquals(pentahoUser.getUsername(), USER_3);
    assertEquals(pentahoUser.getDescription(), USER_DESCRIPTION_3);
    assertEquals(pentahoUser.isEnabled(), true);
    
    logout();
    login("joe", subTenant1_1, new String[]{tenantAdminRoleName, tenantAuthenticatedRoleName});

    try {
      pentahoUser = userRoleDaoProxy.createUser(subTenant2_1, USER_1, PASSWORD_1, USER_DESCRIPTION_1, null);
      fail("Exception not thrown");
    } catch(Throwable th) {
      assertNotNull(th);
    }

    logout();
    login("joe", subTenant2_1, new String[]{tenantAdminRoleName, tenantAuthenticatedRoleName});

    users = userRoleDaoProxy.getUsers(subTenant2_1);
    assertTrue(users.size() == 1 + DEFAULT_USER_COUNT);
    pentahoUser = users.get(1);    
    assertEquals(pentahoUser.getTenant(), subTenant2_1);
    assertEquals(pentahoUser.getUsername(), USER_3);
    assertEquals(pentahoUser.getDescription(), USER_DESCRIPTION_3);
    assertEquals(pentahoUser.isEnabled(), true);
    
    logout();
    login("joe", subTenant1_1, new String[]{tenantAdminRoleName, tenantAuthenticatedRoleName});
    
    pentahoUser = userRoleDaoProxy.createUser(null, USER_4 + DefaultTenantedPrincipleNameResolver.DEFAULT_DELIMETER + subTenant1_1.getRootFolderAbsolutePath(), PASSWORD_4, USER_DESCRIPTION_4, null);
    
    pentahoUser = userRoleDaoProxy.getUser(null, USER_4 + DefaultTenantedPrincipleNameResolver.DEFAULT_DELIMETER + subTenant1_1.getRootFolderAbsolutePath());
    assertEquals(pentahoUser.getTenant(), subTenant1_1);
    assertEquals(pentahoUser.getUsername(), USER_4);
    assertEquals(pentahoUser.getDescription(), USER_DESCRIPTION_4);
    assertEquals(pentahoUser.isEnabled(), true);
    
    logout();
    login("joe", subTenant2_1, new String[]{tenantAdminRoleName, tenantAuthenticatedRoleName});

    try {
      pentahoUser = userRoleDaoProxy.createUser(subTenant1_1, USER_1, PASSWORD_1, USER_DESCRIPTION_1, null);
      fail("Exception not thrown");
    } catch(Throwable th) {
      assertNotNull(th);
    }

    logout();
    login("joe", subTenant1_1, new String[]{tenantAdminRoleName, tenantAuthenticatedRoleName});

    
    users = userRoleDaoProxy.getUsers(subTenant1_1);
    assertTrue(users.size() == 1 + DEFAULT_USER_COUNT);
    pentahoUser = users.get(1);    
    assertEquals(pentahoUser.getTenant(), subTenant1_1);
    assertEquals(pentahoUser.getUsername(), USER_4);
    assertEquals(pentahoUser.getDescription(), USER_DESCRIPTION_4);
    assertEquals(pentahoUser.isEnabled(), true);

    logout();
    login("joe", mainTenant_1, new String[]{tenantAdminRoleName, tenantAuthenticatedRoleName});

    try {
      pentahoUser = userRoleDaoProxy.createUser(mainTenant_1, USER_1, PASSWORD_1, USER_DESCRIPTION_1, null);
      fail("Exception not thrown");
    } catch (AlreadyExistsException e) {
      // Expected exception
    }
    
    try {
      pentahoUser = userRoleDaoProxy.createUser(null, USER_1 + DefaultTenantedPrincipleNameResolver.DEFAULT_DELIMETER + mainTenant_1.getRootFolderAbsolutePath(), PASSWORD_1, USER_DESCRIPTION_1, null);
      fail("Exception not thrown");
    } catch (AlreadyExistsException e) {
      // Expected exception
    }
    
    cleanupUserAndRoles(mainTenant_1);
    cleanupUserAndRoles(mainTenant_2);
    cleanupUserAndRoles(subTenant1_1);
    cleanupUserAndRoles(subTenant1_2);
    cleanupUserAndRoles(subTenant2_1);
    cleanupUserAndRoles(subTenant2_2);
    cleanupUserAndRoles(systemTenant);
    cleanupUserAndRoles(systemTenant);
  }

  @Ignore
  public void testCreateRole() throws Exception {
    loginAsRepositoryAdmin();
    systemTenant = tenantManager.createTenant(null, ServerRepositoryPaths.getPentahoRootFolderName(), tenantAdminRoleName, tenantAuthenticatedRoleName, "Anonymous");
    userRoleDaoProxy.createUser(systemTenant, sysAdminUserName, "password", "", new String[]{tenantAdminRoleName});
    
    login(sysAdminUserName, systemTenant, new String[]{tenantAdminRoleName, tenantAuthenticatedRoleName});
    
    mainTenant_1 = tenantManager.createTenant(systemTenant, MAIN_TENANT_1, tenantAdminRoleName, tenantAuthenticatedRoleName, "Anonymous");
    userRoleDaoProxy.createUser(mainTenant_1, "joe", "password", "", new String[]{tenantAdminRoleName});
    
    mainTenant_2 = tenantManager.createTenant(systemTenant, MAIN_TENANT_2, tenantAdminRoleName, tenantAuthenticatedRoleName, "Anonymous");
    userRoleDaoProxy.createUser(mainTenant_2, "joe", "password", "", new String[]{tenantAdminRoleName});
    
    subTenant1_1 = tenantManager.createTenant(mainTenant_1, SUB_TENANT1_1, tenantAdminRoleName, tenantAuthenticatedRoleName, "Anonymous");
    userRoleDaoProxy.createUser(subTenant1_1, "joe", "password", "", new String[]{tenantAdminRoleName});
    
    subTenant1_2 = tenantManager.createTenant(mainTenant_1, SUB_TENANT1_2, tenantAdminRoleName, tenantAuthenticatedRoleName, "Anonymous");
    userRoleDaoProxy.createUser(subTenant1_2, "joe", "password", "", new String[]{tenantAdminRoleName});
    
    subTenant2_1 = tenantManager.createTenant(mainTenant_2, SUB_TENANT2_1, tenantAdminRoleName, tenantAuthenticatedRoleName, "Anonymous");
    userRoleDaoProxy.createUser(subTenant2_1, "joe", "password", "", new String[]{tenantAdminRoleName});
    
    subTenant2_2 = tenantManager.createTenant(mainTenant_2, SUB_TENANT2_2, tenantAdminRoleName, tenantAuthenticatedRoleName, "Anonymous");
    userRoleDaoProxy.createUser(subTenant2_2, "joe", "password", "", new String[]{tenantAdminRoleName});
    

    login("joe", mainTenant_1, new String[]{tenantAdminRoleName, tenantAuthenticatedRoleName});

    IPentahoRole pentahoRole = userRoleDaoProxy.createRole(mainTenant_1, ROLE_1, ROLE_DESCRIPTION_1, null);
    
    pentahoRole = userRoleDaoProxy.getRole(mainTenant_1, ROLE_1);
    assertEquals(pentahoRole.getTenant(), mainTenant_1);
    assertEquals(pentahoRole.getName(), ROLE_1);
    assertEquals(pentahoRole.getDescription(), ROLE_DESCRIPTION_1);
    int DEFAULT_ROLE_COUNT = 3;
    List<IPentahoRole> roles = userRoleDaoProxy.getRoles(mainTenant_1);
    assertTrue(roles.size() == 1 + DEFAULT_ROLE_COUNT);
    pentahoRole = roles.get(3);    
    assertEquals(pentahoRole.getTenant(), mainTenant_1);
    assertEquals(pentahoRole.getName(), ROLE_1);
    assertEquals(pentahoRole.getDescription(), ROLE_DESCRIPTION_1);
    logout();
    login("joe", mainTenant_1, new String[]{tenantAdminRoleName, tenantAuthenticatedRoleName});
    try {
      pentahoRole = userRoleDaoProxy.createRole(mainTenant_1, ROLE_1, ROLE_DESCRIPTION_1, null);
      fail("Exception not thrown");
    } catch(Throwable th) {
      assertNotNull(th);
    }
    logout();
    login("joe", mainTenant_2, new String[]{tenantAdminRoleName, tenantAuthenticatedRoleName});

    pentahoRole = userRoleDaoProxy.createRole(mainTenant_2, ROLE_1, ROLE_DESCRIPTION_2, null);
    
    pentahoRole = userRoleDaoProxy.getRole(mainTenant_2, ROLE_1);
    assertEquals(pentahoRole.getTenant(), mainTenant_2);
    assertEquals(pentahoRole.getName(), ROLE_1);
    assertEquals(pentahoRole.getDescription(), ROLE_DESCRIPTION_2);
    
    roles = userRoleDaoProxy.getRoles(mainTenant_2);
    assertTrue(roles.size() == 1 + DEFAULT_ROLE_COUNT);
    pentahoRole = roles.get(3);    
    assertEquals(pentahoRole.getTenant(), mainTenant_2);
    assertEquals(pentahoRole.getName(), ROLE_1);
    assertEquals(pentahoRole.getDescription(), ROLE_DESCRIPTION_2);

    logout();
    login("joe", subTenant2_1, new String[]{tenantAdminRoleName, tenantAuthenticatedRoleName});
    try {
      pentahoRole = userRoleDaoProxy.createRole(mainTenant_2, ROLE_1, ROLE_DESCRIPTION_1, null);
      fail("Exception not thrown");
    } catch(Throwable th) {
      assertNotNull(th);
    }

    userRoleDaoProxy.createRole(null, ROLE_3 + DefaultTenantedPrincipleNameResolver.DEFAULT_DELIMETER + subTenant2_1.getRootFolderAbsolutePath(), ROLE_DESCRIPTION_3, null);
    
    pentahoRole = userRoleDaoProxy.getRole(null, ROLE_3 + DefaultTenantedPrincipleNameResolver.DEFAULT_DELIMETER + subTenant2_1.getRootFolderAbsolutePath());
    assertEquals(pentahoRole.getTenant(), subTenant2_1);
    assertEquals(pentahoRole.getName(), ROLE_3);
    assertEquals(pentahoRole.getDescription(), ROLE_DESCRIPTION_3);
    
    roles = userRoleDaoProxy.getRoles(subTenant2_1);
    assertTrue(roles.size() == 1 + DEFAULT_ROLE_COUNT);
    pentahoRole = roles.get(3);    
    assertEquals(pentahoRole.getTenant(), subTenant2_1);
    assertEquals(pentahoRole.getName(), ROLE_3);
    assertEquals(pentahoRole.getDescription(), ROLE_DESCRIPTION_3);
    
    logout();
    login("joe", subTenant1_1, new String[]{tenantAdminRoleName, tenantAuthenticatedRoleName});
    try {
      pentahoRole = userRoleDaoProxy.createRole(subTenant2_1, ROLE_3, ROLE_DESCRIPTION_3, null);
      fail("Exception not thrown");
    } catch(Throwable th) {
      assertNotNull(th);
    }

    logout();
    login("joe", subTenant1_1, new String[]{tenantAdminRoleName, tenantAuthenticatedRoleName});
    
    pentahoRole = userRoleDaoProxy.createRole(null, ROLE_4 + DefaultTenantedPrincipleNameResolver.DEFAULT_DELIMETER + subTenant1_1.getRootFolderAbsolutePath(), ROLE_DESCRIPTION_4, null);
    
    pentahoRole = userRoleDaoProxy.getRole(null, ROLE_4 + DefaultTenantedPrincipleNameResolver.DEFAULT_DELIMETER + subTenant1_1.getRootFolderAbsolutePath());
    assertEquals(pentahoRole.getTenant(), subTenant1_1);
    assertEquals(pentahoRole.getName(), ROLE_4);
    assertEquals(pentahoRole.getDescription(), ROLE_DESCRIPTION_4);
    
    roles = userRoleDaoProxy.getRoles(subTenant1_1);
    assertTrue(roles.size() == 1 + DEFAULT_ROLE_COUNT);
    pentahoRole = roles.get(3);    
    assertEquals(pentahoRole.getTenant(), subTenant1_1);
    assertEquals(pentahoRole.getName(), ROLE_4);
    assertEquals(pentahoRole.getDescription(), ROLE_DESCRIPTION_4);

    logout();
    login("joe", subTenant2_1, new String[]{tenantAdminRoleName, tenantAuthenticatedRoleName});
    try {
      pentahoRole = userRoleDaoProxy.createRole(subTenant1_1, ROLE_3, ROLE_DESCRIPTION_3, null);
      fail("Exception not thrown");
    } catch(Throwable th) {
      assertNotNull(th);
    }

    logout();
    login("joe", mainTenant_1, new String[]{tenantAdminRoleName, tenantAuthenticatedRoleName});

    try {
      pentahoRole = userRoleDaoProxy.createRole(mainTenant_1, ROLE_1, ROLE_DESCRIPTION_1, null);
      fail("Exception not thrown");
    } catch (AlreadyExistsException e) {
      // Expected exception
    }
    
    try {
      pentahoRole = userRoleDaoProxy.createRole(null, ROLE_1 + DefaultTenantedPrincipleNameResolver.DEFAULT_DELIMETER + mainTenant_1.getRootFolderAbsolutePath(), ROLE_DESCRIPTION_1, null);
      fail("Exception not thrown");
    } catch (AlreadyExistsException e) {
      // Expected exception
    }
    logout();
    
    cleanupUserAndRoles(mainTenant_1);
    cleanupUserAndRoles(mainTenant_2);
    cleanupUserAndRoles(subTenant1_1);
    cleanupUserAndRoles(subTenant1_2);
    cleanupUserAndRoles(subTenant2_1);
    cleanupUserAndRoles(subTenant2_2);
    cleanupUserAndRoles(systemTenant);
    cleanupUserAndRoles(systemTenant);
  }

  @Ignore
  public void testUpdateUser() throws Exception {
    loginAsRepositoryAdmin();
    systemTenant = tenantManager.createTenant(null, ServerRepositoryPaths.getPentahoRootFolderName(), tenantAdminRoleName, tenantAuthenticatedRoleName, "Anonymous");
    userRoleDaoProxy.createUser(systemTenant, sysAdminUserName, "password", "", new String[]{tenantAdminRoleName});
    
    login(sysAdminUserName, systemTenant, new String[]{tenantAdminRoleName, tenantAuthenticatedRoleName});
    
    mainTenant_1 = tenantManager.createTenant(systemTenant, MAIN_TENANT_1, tenantAdminRoleName, tenantAuthenticatedRoleName, "Anonymous");
    userRoleDaoProxy.createUser(mainTenant_1, "joe", "password", "", new String[]{tenantAdminRoleName});
    
    mainTenant_2 = tenantManager.createTenant(systemTenant, MAIN_TENANT_2, tenantAdminRoleName, tenantAuthenticatedRoleName, "Anonymous");
    userRoleDaoProxy.createUser(mainTenant_2, "joe", "password", "", new String[]{tenantAdminRoleName});

    login("joe", mainTenant_1, new String[]{tenantAdminRoleName, tenantAuthenticatedRoleName});

    IPentahoUser pentahoUser = userRoleDaoProxy.createUser(mainTenant_1, USER_5, PASSWORD_5, USER_DESCRIPTION_5, null);   
    pentahoUser = userRoleDaoProxy.getUser(mainTenant_1, USER_5);
    assertEquals(pentahoUser.getDescription(), USER_DESCRIPTION_5);
    String originalPassword = pentahoUser.getPassword();
    String encryptedPassword = originalPassword;
    
    String changedDescription1 = USER_DESCRIPTION_5 + "change1";
    userRoleDaoProxy.setUserDescription(mainTenant_1, USER_5, changedDescription1);
    pentahoUser = userRoleDaoProxy.getUser(null, USER_5 + DefaultTenantedPrincipleNameResolver.DEFAULT_DELIMETER + mainTenant_1.getRootFolderAbsolutePath());
    assertEquals(changedDescription1, pentahoUser.getDescription());
    
    String changedDescription2 = USER_DESCRIPTION_5 + "change2";
    userRoleDaoProxy.setUserDescription(null, USER_5 + DefaultTenantedPrincipleNameResolver.DEFAULT_DELIMETER + mainTenant_1.getRootFolderAbsolutePath(), changedDescription2);
    pentahoUser = userRoleDaoProxy.getUser(mainTenant_1, USER_5);
    assertEquals(changedDescription2, pentahoUser.getDescription());
    
    userRoleDaoProxy.setUserDescription(null, USER_5 + DefaultTenantedPrincipleNameResolver.DEFAULT_DELIMETER + mainTenant_1.getRootFolderAbsolutePath(), null);
    pentahoUser = userRoleDaoProxy.getUser(mainTenant_1, USER_5);
    assertNull(pentahoUser.getDescription());
    
    try {
      userRoleDaoProxy.setUserDescription(null, null, changedDescription2);
      fail("Exception not thrown");
    } catch (Exception ex) {
      // Expected exception
    }
    
    try {
      userRoleDaoProxy.setUserDescription(null, USER_5, changedDescription2);
    } catch (Exception ex) {
      // Expected exception
    }
        
    try {
      userRoleDaoProxy.setUserDescription(mainTenant_1, UNKNOWN_USER, changedDescription2);
      fail("Exception not thrown");
    } catch (NotFoundException ex) {
      // Expected exception
    }
    logout();
    login("joe", mainTenant_2, new String[]{tenantAdminRoleName, tenantAuthenticatedRoleName});

    try {
      changedDescription1 = USER_DESCRIPTION_5 + "change1";
      userRoleDaoProxy.setUserDescription(mainTenant_1, USER_5, changedDescription1);
      fail("Exception not thrown");
    } catch(Throwable th) {
      assertNotNull(th);
    }
    logout();
    
    cleanupUserAndRoles(mainTenant_1);
    cleanupUserAndRoles(mainTenant_2);
    cleanupUserAndRoles(systemTenant);
    cleanupUserAndRoles(systemTenant);
  }

  @Ignore
  public void testUpdateRole() throws Exception {
    loginAsRepositoryAdmin();
    systemTenant = tenantManager.createTenant(null, ServerRepositoryPaths.getPentahoRootFolderName(), tenantAdminRoleName, tenantAuthenticatedRoleName, "Anonymous");
    userRoleDaoProxy.createUser(systemTenant, sysAdminUserName, "password", "", new String[]{tenantAdminRoleName});
    
    login(sysAdminUserName, systemTenant, new String[]{tenantAdminRoleName, tenantAuthenticatedRoleName});
    
    mainTenant_1 = tenantManager.createTenant(systemTenant, MAIN_TENANT_1, tenantAdminRoleName, tenantAuthenticatedRoleName, "Anonymous");
    userRoleDaoProxy.createUser(mainTenant_1, "joe", "password", "", new String[]{tenantAdminRoleName});
    
    mainTenant_2 = tenantManager.createTenant(systemTenant, MAIN_TENANT_2, tenantAdminRoleName, tenantAuthenticatedRoleName, "Anonymous");
    userRoleDaoProxy.createUser(mainTenant_2, "joe", "password", "", new String[]{tenantAdminRoleName});

    login("joe", mainTenant_1, new String[]{tenantAdminRoleName, tenantAuthenticatedRoleName});

    IPentahoRole pentahoRole = userRoleDaoProxy.createRole(mainTenant_1, ROLE_5, ROLE_DESCRIPTION_5, null);   
    pentahoRole = userRoleDaoProxy.getRole(mainTenant_1, ROLE_5);
    assertEquals(pentahoRole.getDescription(), ROLE_DESCRIPTION_5);
    
    String changedDescription1 = ROLE_DESCRIPTION_5 + "change1";
    userRoleDaoProxy.setRoleDescription(mainTenant_1, ROLE_5, changedDescription1);
    pentahoRole = userRoleDaoProxy.getRole(null, ROLE_5 + DefaultTenantedPrincipleNameResolver.DEFAULT_DELIMETER + mainTenant_1.getRootFolderAbsolutePath());
    assertEquals(changedDescription1, pentahoRole.getDescription());
    
    String changedDescription2 = ROLE_DESCRIPTION_5 + "change2";
    userRoleDaoProxy.setRoleDescription(null, ROLE_5 + DefaultTenantedPrincipleNameResolver.DEFAULT_DELIMETER + mainTenant_1.getRootFolderAbsolutePath(), changedDescription2);
    pentahoRole = userRoleDaoProxy.getRole(mainTenant_1, ROLE_5);
    assertEquals(changedDescription2, pentahoRole.getDescription());
    
    userRoleDaoProxy.setRoleDescription(null, ROLE_5 + DefaultTenantedPrincipleNameResolver.DEFAULT_DELIMETER + mainTenant_1.getRootFolderAbsolutePath(), null);
    pentahoRole = userRoleDaoProxy.getRole(mainTenant_1, ROLE_5);
    assertNull(pentahoRole.getDescription());
    
    try {
      userRoleDaoProxy.setRoleDescription(null, null, changedDescription2);
      fail("Exception not thrown");
    } catch (Exception ex) {
      // Expected exception
    }
    
    try {
      userRoleDaoProxy.setRoleDescription(null, ROLE_5, changedDescription2);
    } catch (Exception ex) {
      // Expected exception
    }
    
    
    try {
      userRoleDaoProxy.setRoleDescription(mainTenant_1, UNKNOWN_ROLE, changedDescription2);
      fail("Exception not thrown");
    } catch (NotFoundException ex) {
      // Expected exception
    }
    logout();
    login("joe", mainTenant_2, new String[]{tenantAdminRoleName, tenantAuthenticatedRoleName});

    try {
      changedDescription1 = ROLE_DESCRIPTION_5 + "change1";
      userRoleDaoProxy.setRoleDescription(mainTenant_1, ROLE_5, changedDescription1);
      fail("Exception not thrown");
    } catch(Throwable th) {
      assertNotNull(th);
    }
    logout();
    cleanupUserAndRoles(mainTenant_1);
    cleanupUserAndRoles(mainTenant_2);
    cleanupUserAndRoles(systemTenant);
  }

  @Ignore
  public void testDeleteUser() throws Exception {
    int DEFAULT_TENANT_USER = 1;
    loginAsRepositoryAdmin();
    systemTenant = tenantManager.createTenant(null, ServerRepositoryPaths.getPentahoRootFolderName(), tenantAdminRoleName, tenantAuthenticatedRoleName, "Anonymous");
    userRoleDaoProxy.createUser(systemTenant, sysAdminUserName, "password", "", new String[]{tenantAdminRoleName});
    
    login(sysAdminUserName, systemTenant, new String[]{tenantAdminRoleName, tenantAuthenticatedRoleName});
    
    mainTenant_1 = tenantManager.createTenant(systemTenant, MAIN_TENANT_1, tenantAdminRoleName, tenantAuthenticatedRoleName, "Anonymous");
    userRoleDaoProxy.createUser(mainTenant_1, "joe", "password", "", new String[]{tenantAdminRoleName});
    
    mainTenant_2 = tenantManager.createTenant(systemTenant, MAIN_TENANT_2, tenantAdminRoleName, tenantAuthenticatedRoleName, "Anonymous");
    userRoleDaoProxy.createUser(mainTenant_2, "joe", "password", "", new String[]{tenantAdminRoleName});

    login("joe", mainTenant_1, new String[]{tenantAdminRoleName, tenantAuthenticatedRoleName});

    IPentahoUser pentahoUser = userRoleDaoProxy.createUser(mainTenant_1, USER_6, PASSWORD_6, USER_DESCRIPTION_6, null);       
    pentahoUser = userRoleDaoProxy.getUser(null, USER_6 + DefaultTenantedPrincipleNameResolver.DEFAULT_DELIMETER + mainTenant_1.getRootFolderAbsolutePath());   
    assertNotNull(pentahoUser);
    
    logout();
    login("joe", mainTenant_2, new String[]{tenantAdminRoleName, tenantAuthenticatedRoleName});
    try {
      userRoleDaoProxy.deleteUser(pentahoUser);    
      fail("Exception not thrown");
    } catch (Throwable th) {
      assertNotNull(th);
    }    

    logout();
    login("joe", mainTenant_1, new String[]{tenantAdminRoleName, tenantAuthenticatedRoleName});

    userRoleDaoProxy.deleteUser(pentahoUser);
    
    pentahoUser = userRoleDaoProxy.getUser(null, USER_6 + DefaultTenantedPrincipleNameResolver.DEFAULT_DELIMETER + mainTenant_1.getRootFolderAbsolutePath());   
    assertNull(pentahoUser);
    assertEquals(DEFAULT_TENANT_USER, userRoleDaoProxy.getUsers(mainTenant_1).size());
   
    pentahoUser = userRoleDaoProxy.createUser(null, USER_6 + DefaultTenantedPrincipleNameResolver.DEFAULT_DELIMETER + mainTenant_1.getRootFolderAbsolutePath(), PASSWORD_6, USER_DESCRIPTION_6, null);   
    pentahoUser = userRoleDaoProxy.getUser(mainTenant_1, USER_6);
    
    assertNotNull(pentahoUser);
    
    userRoleDaoProxy.deleteUser(pentahoUser);
    
    assertNull(userRoleDaoProxy.getUser(null, USER_6 + DefaultTenantedPrincipleNameResolver.DEFAULT_DELIMETER + mainTenant_1.getRootFolderAbsolutePath()));

    try {
      userRoleDaoProxy.deleteUser(pentahoUser);    
      fail("Exception not thrown");
    } catch (NotFoundException e) {
      // Expected exception
    }
    
    try {
      pentahoUser = new PentahoUser(null, USER_6, PASSWORD_6, USER_DESCRIPTION_6, true);
      userRoleDaoProxy.deleteUser(pentahoUser);    
      fail("Exception not thrown");
    } catch (Exception ex) {
      // Expected exception
    }
    
    try {
      pentahoUser = new PentahoUser(mainTenant_1, null, PASSWORD_6, USER_DESCRIPTION_6, true);
      userRoleDaoProxy.deleteUser(pentahoUser);    
      fail("Exception not thrown");
    } catch (NotFoundException e) {
      // Expected exception
    }
    
    try {
      pentahoUser = new PentahoUser(mainTenant_1, UNKNOWN_USER, PASSWORD_6, USER_DESCRIPTION_6, true);
      userRoleDaoProxy.deleteUser(pentahoUser);    
      fail("Exception not thrown");
    } catch (NotFoundException e) {
      // Expected exception
    }
    cleanupUserAndRoles(mainTenant_1);
    cleanupUserAndRoles(mainTenant_2);
    cleanupUserAndRoles(systemTenant);
  }

  @Ignore
  public void testDeleteRole() throws Exception {
    int DEFAULT_ROLE_COUNT = 3;
    loginAsRepositoryAdmin();
    systemTenant = tenantManager.createTenant(null, ServerRepositoryPaths.getPentahoRootFolderName(), tenantAdminRoleName, tenantAuthenticatedRoleName, "Anonymous");
    userRoleDaoProxy.createUser(systemTenant, sysAdminUserName, "password", "", new String[]{tenantAdminRoleName});
    
    login(sysAdminUserName, systemTenant, new String[]{tenantAdminRoleName, tenantAuthenticatedRoleName});
    
    mainTenant_1 = tenantManager.createTenant(systemTenant, MAIN_TENANT_1, tenantAdminRoleName, tenantAuthenticatedRoleName, "Anonymous");
    userRoleDaoProxy.createUser(mainTenant_1, "joe", "password", "", new String[]{tenantAdminRoleName});
    
    mainTenant_2 = tenantManager.createTenant(systemTenant, MAIN_TENANT_2, tenantAdminRoleName, tenantAuthenticatedRoleName, "Anonymous");
    userRoleDaoProxy.createUser(mainTenant_2, "joe", "password", "", new String[]{tenantAdminRoleName});

    login("joe", mainTenant_1, new String[]{tenantAdminRoleName, tenantAuthenticatedRoleName});


    IPentahoRole pentahoRole = userRoleDaoProxy.createRole(mainTenant_1, ROLE_6, ROLE_DESCRIPTION_6, null);       
    pentahoRole = userRoleDaoProxy.getRole(null, ROLE_6 + DefaultTenantedPrincipleNameResolver.DEFAULT_DELIMETER + mainTenant_1.getRootFolderAbsolutePath());   
    assertNotNull(pentahoRole);

    logout();
    login("joe", mainTenant_2, new String[]{tenantAdminRoleName, tenantAuthenticatedRoleName});
    try {
      userRoleDaoProxy.deleteRole(pentahoRole);
      fail("Exception not thrown");
    } catch (Throwable th) {
      assertNotNull(th);
    }    

    logout();
    login("joe", mainTenant_1, new String[]{tenantAdminRoleName, tenantAuthenticatedRoleName});

    userRoleDaoProxy.deleteRole(pentahoRole);
    pentahoRole = userRoleDaoProxy.getRole(null, ROLE_6 + DefaultTenantedPrincipleNameResolver.DEFAULT_DELIMETER + mainTenant_1.getRootFolderAbsolutePath());   
    assertNull(pentahoRole);
    assertEquals(DEFAULT_ROLE_COUNT, userRoleDaoProxy.getRoles(mainTenant_1).size());
   
    pentahoRole = userRoleDaoProxy.createRole(null, ROLE_6 + DefaultTenantedPrincipleNameResolver.DEFAULT_DELIMETER + mainTenant_1.getRootFolderAbsolutePath(), ROLE_DESCRIPTION_6, null);   
    pentahoRole = userRoleDaoProxy.getRole(mainTenant_1, ROLE_6);
    
    assertNotNull(pentahoRole);
    
    userRoleDaoProxy.deleteRole(pentahoRole);
    
    assertNull(userRoleDaoProxy.getRole(null, ROLE_6 + DefaultTenantedPrincipleNameResolver.DEFAULT_DELIMETER + mainTenant_1.getRootFolderAbsolutePath()));
    
    try {
      userRoleDaoProxy.deleteRole(pentahoRole);    
      fail("Exception not thrown");
    } catch (NotFoundException e) {
      // Expected exception
    }
    
    try {
      pentahoRole = new PentahoRole(null, ROLE_6, ROLE_DESCRIPTION_6);
      userRoleDaoProxy.deleteRole(pentahoRole);    
      fail("Exception not thrown");
    } catch (Exception ex) {
      // Expected exception
    }
    
    try {
      pentahoRole = new PentahoRole(mainTenant_1, null, ROLE_DESCRIPTION_6);
      userRoleDaoProxy.deleteRole(pentahoRole);    
      fail("Exception not thrown");
    } catch (NotFoundException e) {
      // Expected exception
    }
    
    try {
      pentahoRole = new PentahoRole(mainTenant_1, UNKNOWN_ROLE, ROLE_DESCRIPTION_6);
      userRoleDaoProxy.deleteRole(pentahoRole);    
      fail("Exception not thrown");
    } catch (NotFoundException e) {
      // Expected exception
    }
    cleanupUserAndRoles(mainTenant_1);
    cleanupUserAndRoles(mainTenant_2);
    cleanupUserAndRoles(systemTenant);
  }
  
  @Ignore
  public void testGetUser() throws Exception {
    loginAsRepositoryAdmin();
    systemTenant = tenantManager.createTenant(null, ServerRepositoryPaths.getPentahoRootFolderName(), tenantAdminRoleName, tenantAuthenticatedRoleName, "Anonymous");
    userRoleDaoProxy.createUser(systemTenant, sysAdminUserName, "password", "", new String[]{tenantAdminRoleName});
    
    login(sysAdminUserName, systemTenant, new String[]{tenantAdminRoleName, tenantAuthenticatedRoleName});
    
    mainTenant_1 = tenantManager.createTenant(systemTenant, MAIN_TENANT_1, tenantAdminRoleName, tenantAuthenticatedRoleName, "Anonymous");
    userRoleDaoProxy.createUser(mainTenant_1, "joe", "password", "", new String[]{tenantAdminRoleName});
    
    mainTenant_2 = tenantManager.createTenant(systemTenant, MAIN_TENANT_2, tenantAdminRoleName, tenantAuthenticatedRoleName, "Anonymous");
    userRoleDaoProxy.createUser(mainTenant_2, "joe", "password", "", new String[]{tenantAdminRoleName});

    login("joe", mainTenant_1, new String[]{tenantAdminRoleName, tenantAuthenticatedRoleName});

    assertNull(userRoleDaoProxy.getUser(UNKNOWN_TENANT, UNKNOWN_USER));   
    assertNull(userRoleDaoProxy.getUser(null, UNKNOWN_USER));
    cleanupUserAndRoles(mainTenant_1);
    cleanupUserAndRoles(mainTenant_2);
    cleanupUserAndRoles(systemTenant);
  }

  @Ignore
  public void testGetUsers() throws Exception {
    int DEFAULT_USER_COUNT = 1;
    loginAsRepositoryAdmin();
    systemTenant = tenantManager.createTenant(null, ServerRepositoryPaths.getPentahoRootFolderName(), tenantAdminRoleName, tenantAuthenticatedRoleName, "Anonymous");
    userRoleDaoProxy.createUser(systemTenant, sysAdminUserName, "password", "", new String[]{tenantAdminRoleName});
    
    login(sysAdminUserName, systemTenant, new String[]{tenantAdminRoleName, tenantAuthenticatedRoleName});
    
    mainTenant_1 = tenantManager.createTenant(systemTenant, MAIN_TENANT_1, tenantAdminRoleName, tenantAuthenticatedRoleName, "Anonymous");
    userRoleDaoProxy.createUser(mainTenant_1, "joe", "password", "", new String[]{tenantAdminRoleName});
    
    mainTenant_2 = tenantManager.createTenant(systemTenant, MAIN_TENANT_2, tenantAdminRoleName, tenantAuthenticatedRoleName, "Anonymous");
    userRoleDaoProxy.createUser(mainTenant_2, "joe", "password", "", new String[]{tenantAdminRoleName});

    login("joe", mainTenant_1, new String[]{tenantAdminRoleName, tenantAuthenticatedRoleName});

    userRoleDaoProxy.createUser(mainTenant_1, USER_7, PASSWORD_7, USER_DESCRIPTION_7, null);       
    userRoleDaoProxy.createUser(mainTenant_1, USER_8, PASSWORD_8, USER_DESCRIPTION_8, null);
    List<IPentahoUser> users = userRoleDaoProxy.getUsers(mainTenant_1);
    assertEquals(2 + DEFAULT_USER_COUNT, users.size());
    
    for (IPentahoUser user : users) {
      if (user.getUsername().equals(USER_1)) {
        assertEquals(user.getTenant(), mainTenant_1);
        assertEquals(user.isEnabled(), true);
      } else if (user.getUsername().equals(USER_7)) {
        assertEquals(user.getTenant(), mainTenant_1);
        assertEquals(user.getDescription(), USER_DESCRIPTION_7);
        assertEquals(user.isEnabled(), true);
      } else if (user.getUsername().equals(USER_8)) {
        assertEquals(user.getTenant(), mainTenant_1);
        assertEquals(user.getDescription(), USER_DESCRIPTION_8);
        assertEquals(user.isEnabled(), true);
      } else {
        fail("Invalid user name");
      }
    }
    try {
      users = userRoleDaoProxy.getUsers(UNKNOWN_TENANT);
      fail("Exception not thrown");
    } catch(Throwable th) {
      assertNotNull(th);
    }
    cleanupUserAndRoles(mainTenant_1);
    cleanupUserAndRoles(mainTenant_2);
    cleanupUserAndRoles(systemTenant);
  }

  @Ignore
  public void testGetRoles() throws Exception {
    int DEFAULT_ROLE_COUNT = 3;
    loginAsRepositoryAdmin();
    systemTenant = tenantManager.createTenant(null, ServerRepositoryPaths.getPentahoRootFolderName(), tenantAdminRoleName, tenantAuthenticatedRoleName, "Anonymous");
    userRoleDaoProxy.createUser(systemTenant, sysAdminUserName, "password", "", new String[]{tenantAdminRoleName});
    
    login(sysAdminUserName, systemTenant, new String[]{tenantAdminRoleName, tenantAuthenticatedRoleName});
    
    mainTenant_1 = tenantManager.createTenant(systemTenant, MAIN_TENANT_1, tenantAdminRoleName, tenantAuthenticatedRoleName, "Anonymous");
    userRoleDaoProxy.createUser(mainTenant_1, "joe", "password", "", new String[]{tenantAdminRoleName});
    
    mainTenant_2 = tenantManager.createTenant(systemTenant, MAIN_TENANT_2, tenantAdminRoleName, tenantAuthenticatedRoleName, "Anonymous");
    userRoleDaoProxy.createUser(mainTenant_2, "joe", "password", "", new String[]{tenantAdminRoleName});

    login("joe", mainTenant_1, new String[]{tenantAdminRoleName, tenantAuthenticatedRoleName});
    userRoleDaoProxy.createRole(mainTenant_1, ROLE_7, ROLE_DESCRIPTION_7, null);       
    userRoleDaoProxy.createRole(mainTenant_1, ROLE_8, ROLE_DESCRIPTION_8, null);
    List<IPentahoRole> roles = userRoleDaoProxy.getRoles(mainTenant_1);
    assertEquals(2 + DEFAULT_ROLE_COUNT, roles.size());
    
    for (IPentahoRole user : roles) {
      if (user.getName().equals(ROLE_7)) {
        assertEquals(user.getTenant(), mainTenant_1);
        assertEquals(user.getDescription(), ROLE_DESCRIPTION_7);
      } else if (user.getName().equals(ROLE_8)) {
        assertEquals(user.getTenant(), mainTenant_1);
        assertEquals(user.getDescription(), ROLE_DESCRIPTION_8);
      } 
    }
    try {
      roles = userRoleDaoProxy.getRoles(UNKNOWN_TENANT);
      fail("Exception not thrown");
    } catch(Throwable th) {
      assertNotNull(th);
    }
    cleanupUserAndRoles(mainTenant_1);
    cleanupUserAndRoles(mainTenant_2);
    cleanupUserAndRoles(systemTenant);
  }

  @Ignore
  public void testRoleWithMembers() throws Exception {
    loginAsRepositoryAdmin();
    systemTenant = tenantManager.createTenant(null, ServerRepositoryPaths.getPentahoRootFolderName(), tenantAdminRoleName, tenantAuthenticatedRoleName, "Anonymous");
    userRoleDaoProxy.createUser(systemTenant, sysAdminUserName, "password", "", new String[]{tenantAdminRoleName});
    
    login(sysAdminUserName, systemTenant, new String[]{tenantAdminRoleName, tenantAuthenticatedRoleName});
    
    mainTenant_1 = tenantManager.createTenant(systemTenant, MAIN_TENANT_1, tenantAdminRoleName, tenantAuthenticatedRoleName, "Anonymous");
    userRoleDaoProxy.createUser(mainTenant_1, "joe", "password", "", new String[]{tenantAdminRoleName});
    
    mainTenant_2 = tenantManager.createTenant(systemTenant, MAIN_TENANT_2, tenantAdminRoleName, tenantAuthenticatedRoleName, "Anonymous");
    userRoleDaoProxy.createUser(mainTenant_2, "joe", "password", "", new String[]{tenantAdminRoleName});
    
    login("joe", mainTenant_1, new String[]{tenantAdminRoleName, tenantAuthenticatedRoleName});
    
    userRoleDaoProxy.createRole(mainTenant_1, ROLE_1, ROLE_DESCRIPTION_1, null);       
    userRoleDaoProxy.createRole(mainTenant_1, ROLE_2, ROLE_DESCRIPTION_2, null);
    userRoleDaoProxy.createRole(mainTenant_1, ROLE_3, ROLE_DESCRIPTION_3, null);
    userRoleDaoProxy.createUser(mainTenant_1, USER_2, PASSWORD_2, USER_DESCRIPTION_2, new String[]{ROLE_1});
    userRoleDaoProxy.createUser(mainTenant_1, USER_3, PASSWORD_3, USER_DESCRIPTION_3, new String[]{ROLE_1, ROLE_2});
    
    List<IPentahoUser> users = userRoleDaoProxy.getRoleMembers(mainTenant_1, ROLE_2);
    assertEquals(1, users.size());
    assertEquals(USER_3, users.get(0).getUsername());
    
    ArrayList<String> expectedUserNames = new ArrayList<String>();
    expectedUserNames.add(USER_2);
    expectedUserNames.add(USER_3);
    ArrayList<String> actualUserNames = new ArrayList<String>();
    users = userRoleDaoProxy.getRoleMembers(null, ROLE_1 + DefaultTenantedPrincipleNameResolver.DEFAULT_DELIMETER + mainTenant_1.getRootFolderAbsolutePath());
    for (IPentahoUser user : users) {
      actualUserNames.add(user.getUsername());
    }
    assertEquals(2, actualUserNames.size());
    assertTrue(actualUserNames.containsAll(expectedUserNames));
        
    users = userRoleDaoProxy.getRoleMembers(mainTenant_1, ROLE_3);
    assertEquals(0, users.size());
    
    userRoleDaoProxy.createUser(mainTenant_1, USER_5, PASSWORD_5, USER_DESCRIPTION_5, null);       
    userRoleDaoProxy.createUser(mainTenant_1, USER_6, PASSWORD_6, USER_DESCRIPTION_6, null);
    userRoleDaoProxy.createUser(mainTenant_1, USER_7, PASSWORD_7, USER_DESCRIPTION_7, null);
    userRoleDaoProxy.createRole(mainTenant_1, ROLE_5, ROLE_DESCRIPTION_6, new String[]{USER_5});
    userRoleDaoProxy.createRole(mainTenant_1, ROLE_6, ROLE_DESCRIPTION_7, new String[]{USER_5, USER_6});
    
    ArrayList<String> expectedRoleNames = new ArrayList<String>();
    expectedRoleNames.add(ROLE_6);
    expectedRoleNames.add(tenantAuthenticatedRoleName);
    ArrayList<String> actualRoleNames = new ArrayList<String>();
    List<IPentahoRole> roles = userRoleDaoProxy.getUserRoles(mainTenant_1, USER_6);
    for (IPentahoRole role : roles) {
      actualRoleNames.add(role.getName());
    }
    assertEquals(2, roles.size());
    assertTrue(actualRoleNames.containsAll(expectedRoleNames));
    
    expectedRoleNames = new ArrayList<String>();
    expectedRoleNames.add(ROLE_5);
    expectedRoleNames.add(ROLE_6);
    expectedRoleNames.add(tenantAuthenticatedRoleName);
    actualRoleNames = new ArrayList<String>();
    roles = userRoleDaoProxy.getUserRoles(null, USER_5 + DefaultTenantedPrincipleNameResolver.DEFAULT_DELIMETER + mainTenant_1.getRootFolderAbsolutePath());
    for (IPentahoRole role : roles) {
      actualRoleNames.add(role.getName());
    }
    assertEquals(3, actualRoleNames.size());
    assertTrue(actualRoleNames.containsAll(expectedRoleNames));
        
    roles = userRoleDaoProxy.getUserRoles(mainTenant_1, USER_7);
    assertEquals(1, roles.size());
    assertEquals(tenantAuthenticatedRoleName, roles.get(0).getName());
    
    userRoleDaoProxy.setUserRoles(null, USER_7 + DefaultTenantedPrincipleNameResolver.DEFAULT_DELIMETER + mainTenant_1.getRootFolderAbsolutePath(), new String[]{ROLE_5, ROLE_6});
    roles = userRoleDaoProxy.getUserRoles(null, USER_7 + DefaultTenantedPrincipleNameResolver.DEFAULT_DELIMETER + mainTenant_1.getRootFolderAbsolutePath());
    actualRoleNames.clear();
    for (IPentahoRole role : roles) {
      actualRoleNames.add(role.getName());
    }
    assertEquals(3, actualRoleNames.size());
    assertTrue(actualRoleNames.containsAll(expectedRoleNames));
    
    expectedUserNames = new ArrayList<String>();
    expectedUserNames.add(USER_1);
    expectedUserNames.add(USER_2);
    expectedRoleNames.add(tenantAuthenticatedRoleName);
    userRoleDaoProxy.setRoleMembers(null, ROLE_3 + DefaultTenantedPrincipleNameResolver.DEFAULT_DELIMETER + mainTenant_1.getRootFolderAbsolutePath(), new String[]{USER_1, USER_2});
    users = userRoleDaoProxy.getRoleMembers(null, ROLE_3 + DefaultTenantedPrincipleNameResolver.DEFAULT_DELIMETER + mainTenant_1.getRootFolderAbsolutePath());
    actualUserNames.clear();
    for (IPentahoUser user : users) {
      actualUserNames.add(user.getUsername());
    }
    assertEquals(2, actualUserNames.size());
    assertTrue(actualUserNames.containsAll(expectedUserNames));

    cleanupUserAndRoles(mainTenant_1);
    cleanupUserAndRoles(mainTenant_2);
    cleanupUserAndRoles(systemTenant);
  }

  @Ignore
  public void testGetRole() throws Exception {
    loginAsRepositoryAdmin();
    systemTenant = tenantManager.createTenant(null, ServerRepositoryPaths.getPentahoRootFolderName(), tenantAdminRoleName, tenantAuthenticatedRoleName, "Anonymous");
    userRoleDaoProxy.createUser(systemTenant, sysAdminUserName, "password", "", new String[]{tenantAdminRoleName});
    
    login(sysAdminUserName, systemTenant, new String[]{tenantAdminRoleName, tenantAuthenticatedRoleName});
    
    mainTenant_1 = tenantManager.createTenant(systemTenant, MAIN_TENANT_1, tenantAdminRoleName, tenantAuthenticatedRoleName, "Anonymous");
    userRoleDaoProxy.createUser(mainTenant_1, "joe", "password", "", new String[]{tenantAdminRoleName});
    
    mainTenant_2 = tenantManager.createTenant(systemTenant, MAIN_TENANT_2, tenantAdminRoleName, tenantAuthenticatedRoleName, "Anonymous");
    userRoleDaoProxy.createUser(mainTenant_2, "joe", "password", "", new String[]{tenantAdminRoleName});
    
    login("joe", mainTenant_1, new String[]{tenantAdminRoleName, tenantAuthenticatedRoleName});

    assertNull(userRoleDaoProxy.getRole(UNKNOWN_TENANT, UNKNOWN_ROLE));   
    assertNull(userRoleDaoProxy.getRole(null, UNKNOWN_ROLE));
    cleanupUserAndRoles(mainTenant_1);
    cleanupUserAndRoles(mainTenant_2);
    cleanupUserAndRoles(systemTenant);
  }

  private static void traverseNodes(Node node, int currentLevel) throws Exception {
    System.out.println(node.getPath());
    NodeIterator children = node.getNodes();
    while (children.hasNext()) {
        traverseNodes(children.nextNode(), currentLevel + 1);
    }
  }  
}
