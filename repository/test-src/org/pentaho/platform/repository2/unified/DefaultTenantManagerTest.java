/*
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
 */
package org.pentaho.platform.repository2.unified;

import static org.junit.Assert.*;

import java.io.File;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
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
import org.pentaho.platform.repository2.unified.jcr.SimpleJcrTestUtils;
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
 * Integration test. Tests {@link DefaultUnifiedRepository} and {@link IAuthorizationPolicy} fully configured behind 
 * Spring Security's method security and Spring's transaction interceptor.
 * 
 * <p>
 * Note the RunWith annotation that uses a special runner that knows how to setup a Spring application context. The 
 * application context config files are listed in the ContextConfiguration annotation. By implementing 
 * {@link ApplicationContextAware}, this unit test can access various beans defined in the application context, 
 * including the bean under test.
 * </p>
 * 
 * @author mlowery
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/repository.spring.xml",
    "classpath:/repository-test-override.spring.xml" })
@SuppressWarnings("nls")
public class DefaultTenantManagerTest implements ApplicationContextAware {
  // ~ Static fields/initializers ======================================================================================
  private final String USERNAME_JOE = "joe";
  private final String TENANT_ID_ACME = "acme";
  private final String TENANT_ID_APPLE = "apple";
  private final String TENANT_ID_MICROSOFT = "microsoft";
  
  public static final String SYSTEM_ADMIN ="admin";

  public static final String MAIN_TENANT_1_PATH = RepositoryFile.SEPARATOR + ServerRepositoryPaths.getPentahoRootFolderName() + RepositoryFile.SEPARATOR + "maintenant1";
  public static final String SUB_TENANT1_1_PATH = RepositoryFile.SEPARATOR + ServerRepositoryPaths.getPentahoRootFolderName() + RepositoryFile.SEPARATOR + "maintenant1" + RepositoryFile.SEPARATOR + "subtenant11";
  public static final String SUB_TENANT1_2_PATH = RepositoryFile.SEPARATOR + ServerRepositoryPaths.getPentahoRootFolderName() + RepositoryFile.SEPARATOR + "maintenant1" + RepositoryFile.SEPARATOR + "subtenant12";
  public static final String MAIN_TENANT_2_PATH = RepositoryFile.SEPARATOR + ServerRepositoryPaths.getPentahoRootFolderName() + RepositoryFile.SEPARATOR + "maintenant2";
  public static final String SUB_TENANT2_1_PATH = RepositoryFile.SEPARATOR + ServerRepositoryPaths.getPentahoRootFolderName() + RepositoryFile.SEPARATOR + "maintenant2" + RepositoryFile.SEPARATOR + "subtenant21";
  public static final String SUB_TENANT2_2_PATH = RepositoryFile.SEPARATOR + ServerRepositoryPaths.getPentahoRootFolderName() + RepositoryFile.SEPARATOR + "maintenant2" + RepositoryFile.SEPARATOR + "subtenant22";

  
  public static final String MAIN_TENANT_1 = "maintenant1";
  public static final String SUB_TENANT1_1 = "subtenant11";
  public static final String SUB_TENANT1_2 = "subtenant12";
  public static final String MAIN_TENANT_2 = "maintenant2";
  public static final String SUB_TENANT2_1 = "subtenant21";
  public static final String SUB_TENANT2_2 = "subtenant22";

  // ~ Instance fields =================================================================================================

  private boolean startupCalled;

  private String repositoryAdminUsername;

  private String tenantAdminAuthorityNamePattern;

  private String tenantAuthenticatedAuthorityNamePattern;

  /**
   * Used for state verification and test cleanup.
   */
  private JcrTemplate testJcrTemplate;

  private IBackingRepositoryLifecycleManager manager;

  private IAuthorizationPolicy authorizationPolicy;
  
  private MicroPlatform mp;
  
  private IUnifiedRepository repo;

  private ITenantManager tenantManager;
  
  // ~ Constructors ==================================================================================================== 

  public DefaultTenantManagerTest() throws Exception {
    super();
  }

  // ~ Methods =========================================================================================================

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

    // Start the micro-platform
    mp.start();
    logout();
    startupCalled = true;
  }

  @After
  public void tearDown() throws Exception {
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

    // null out fields to get back memory
    repo = null;
    tenantManager = null;
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
    repo = (IUnifiedRepository) applicationContext.getBean("unifiedRepository");
    tenantManager = (ITenantManager) applicationContext.getBean("defaultTenantManager");
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

    manager.newTenant();
    manager.newUser();
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

  private void setUpRoleBindings() {
  }

  @Test
  public void testCreateSystemTenant() {
    Serializable systemTenantId = tenantManager.createSystemTenant(ServerRepositoryPaths.getPentahoRootFolderName());
    assertNotNull(systemTenantId);
//    assert(ServerRepositoryPaths.getPentahoRootFolderPath().endsWith(systemTenant.getName()));
  }
  
  @Test
  public void testCreateTenant() {
    // This line is equivalent to manager.startup();
    Serializable systemTenantId = tenantManager.createSystemTenant(ServerRepositoryPaths.getPentahoRootFolderName());
    assertNotNull(systemTenantId);
    assertTrue(tenantManager.isTenantRoot(systemTenantId));
    assertTrue(tenantManager.isTenantEnabled(systemTenantId));
    Serializable tenantRootId = tenantManager.createTenant(systemTenantId, TENANT_ID_ACME);
    assertNotNull(tenantRootId);
    assertTrue(tenantManager.isTenantRoot(tenantRootId));
    assertTrue(tenantManager.isTenantEnabled(tenantRootId));
    Serializable subTenantRootId = tenantManager.createTenant(tenantRootId, TENANT_ID_APPLE);
    assertNotNull(subTenantRootId);
    assertTrue(tenantManager.isTenantRoot(subTenantRootId));
    assertTrue(tenantManager.isTenantEnabled(subTenantRootId));
    List<Serializable> childTenants = tenantManager.getChildTenants(tenantRootId);
    assertTrue(childTenants.size() == 1);
    assertTrue(childTenants.get(0).equals(subTenantRootId));
  }
  
  @Test
  public void testEnableDisableTenant() {
    // This line is equivalent to manager.startup();
    Serializable systemTenantId = tenantManager.createSystemTenant(ServerRepositoryPaths.getPentahoRootFolderName());
    assertNotNull(systemTenantId);
    Serializable tenantRootId = tenantManager.createTenant(systemTenantId, TENANT_ID_ACME);
    assertNotNull(tenantRootId);
    assertTrue(tenantManager.isTenantRoot(tenantRootId));
    assertTrue(tenantManager.isTenantEnabled(tenantRootId));
    tenantManager.enableTenant(tenantRootId, false);
    assertTrue(!tenantManager.isTenantEnabled(tenantRootId));
    tenantManager.enableTenant(tenantRootId, true);
    assertTrue(tenantManager.isTenantEnabled(tenantRootId));
  }
  
  @Test
  public void testIsTenantRoot() {
    Serializable systemTenantId = tenantManager.createSystemTenant(ServerRepositoryPaths.getPentahoRootFolderName());
    assertNotNull(systemTenantId);
    assertTrue(tenantManager.isTenantRoot(systemTenantId));
    assertTrue(tenantManager.isTenantEnabled(systemTenantId));
    Serializable tenantRootId = tenantManager.createTenant(systemTenantId, TENANT_ID_ACME);
    assertNotNull(tenantRootId);
    assertTrue(tenantManager.isTenantRoot(tenantRootId));
    login(USERNAME_JOE, TENANT_ID_ACME);
    List<RepositoryFile> children = repo.getChildren(tenantRootId);
    for (RepositoryFile aFile : children) {
      assertTrue(!tenantManager.isTenantRoot(aFile.getId()));
    }
  }
  
  @Test
  public void testIsSubTenant() {
    Serializable systemTenantId = tenantManager.createSystemTenant(ServerRepositoryPaths.getPentahoRootFolderName());
    Serializable mainTenant_1_Id = tenantManager.createTenant(systemTenantId, MAIN_TENANT_1);
    Serializable mainTenant_2_Id = tenantManager.createTenant(systemTenantId, MAIN_TENANT_2);
    Serializable subTenant1_1_Id = tenantManager.createTenant(mainTenant_1_Id, SUB_TENANT1_1);
    Serializable subTenant1_2_Id = tenantManager.createTenant(mainTenant_1_Id, SUB_TENANT1_2);
    Serializable subTenant2_1_Id = tenantManager.createTenant(mainTenant_2_Id, SUB_TENANT2_1);
    Serializable subTenant2_2_Id = tenantManager.createTenant(mainTenant_2_Id, SUB_TENANT2_2);
    assertTrue(tenantManager.isSubTenant(MAIN_TENANT_1_PATH, MAIN_TENANT_1_PATH));
    assertTrue(tenantManager.isSubTenant(MAIN_TENANT_2_PATH, MAIN_TENANT_2_PATH));
    assertTrue(tenantManager.isSubTenant(MAIN_TENANT_1_PATH, SUB_TENANT1_2_PATH));
    assertTrue(tenantManager.isSubTenant(MAIN_TENANT_1_PATH, SUB_TENANT1_1_PATH));
    assertFalse(tenantManager.isSubTenant(MAIN_TENANT_1_PATH, SUB_TENANT2_1_PATH));
    assertFalse(tenantManager.isSubTenant(MAIN_TENANT_1_PATH, SUB_TENANT2_2_PATH));
    assertFalse(tenantManager.isSubTenant(MAIN_TENANT_2_PATH, SUB_TENANT1_2_PATH));
    assertFalse(tenantManager.isSubTenant(MAIN_TENANT_2_PATH, SUB_TENANT1_1_PATH));
    assertTrue(tenantManager.isSubTenant(MAIN_TENANT_2_PATH, SUB_TENANT2_1_PATH));
    assertTrue(tenantManager.isSubTenant(MAIN_TENANT_2_PATH, SUB_TENANT2_2_PATH));
    assertTrue(tenantManager.isSubTenant(SUB_TENANT2_2_PATH, SUB_TENANT2_2_PATH));
    assertTrue(tenantManager.isSubTenant(SUB_TENANT1_2_PATH, SUB_TENANT1_2_PATH));

  }
  
//  @Test
//  public void testGetChildrenTenants() {
////    manager.startup();
//    tenantManager.createSystemTenant(ServerRepositoryPaths.getPentahoRootFolderPath());
//    setUpRoleBindings();
//    RepositoryFile tenantRoot = tenantManager.createTenant("", TENANT_ID_ACME);
//    assertNotNull(tenantRoot);
//    assertEquals(tenantRoot.getName(), TENANT_ID_ACME);
//    boolean isTenanted = tenantManager.isTenantRoot(tenantRoot.getId());
//    assertTrue(isTenanted);
//    boolean isTenantEnabled = tenantManager.isTenantEnabled(tenantRoot.getId());
//    assertTrue(isTenantEnabled);
//    RepositoryFile subTenantRoot1 = tenantManager.createTenant(TENANT_ID_ACME, TENANT_ID_APPLE);
//    isTenanted = tenantManager.isTenantRoot(subTenantRoot1.getId());
//    assertTrue(isTenanted);
//    isTenantEnabled = tenantManager.isTenantEnabled(subTenantRoot1.getId());
//    assertTrue(isTenantEnabled);
//    RepositoryFile subTenantRoot2 = tenantManager.createTenant(TENANT_ID_ACME, TENANT_ID_MICROSOFT);
//    isTenanted = tenantManager.isTenantRoot(subTenantRoot2.getId());
//    assertTrue(isTenanted);
//    isTenantEnabled = tenantManager.isTenantEnabled(subTenantRoot2.getId());
//    assertTrue(isTenantEnabled);
//    List<RepositoryFile> tenantChildren = tenantManager.getChildTenants(TENANT_ID_ACME);
//    assertTrue(tenantChildren.size() == 2);
//  }
}
