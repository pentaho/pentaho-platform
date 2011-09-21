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
package org.pentaho.platform.security.policy.rolebased;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.repository2.unified.DefaultUnifiedRepository;
import org.pentaho.platform.repository2.unified.JackrabbitRepositoryTestBase;
import org.pentaho.platform.repository2.unified.ServerRepositoryPaths;
import org.pentaho.platform.repository2.unified.jcr.SimpleJcrTestUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.AccessDeniedException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * JUnit 4 test. Not actually a unit test as it tests the {@link DefaultUnifiedRepository} fully configured behind 
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
public class RoleAuthorizationPolicyTest extends JackrabbitRepositoryTestBase implements ApplicationContextAware {

  private static final String NAMESPACE_REPOSITORY = "org.pentaho.repository";

  private static final String NAMESPACE_SECURITY = "org.pentaho.security";

  private static final String NAMESPACE_PENTAHO = "org.pentaho";

  private static final String NAMESPACE_DOESNOTEXIST = "doesnotexist";

  private static final String RUNTIME_ROLE_ACME_ADMIN = "acme_Admin";

  private static final String RUNTIME_ROLE_ACME_AUTHENTICATED = "acme_Authenticated";

  private static final String LOGICAL_ROLE_SECURITY_ADMINISTRATOR = "org.pentaho.di.securityAdministrator";

  private static final String LOGICAL_ROLE_CREATOR = "org.pentaho.di.creator";

  private static final String LOGICAL_ROLE_READER = "org.pentaho.di.reader";

  private static final String ACTION_READ = "org.pentaho.repository.read";

  private static final String ACTION_CREATE = "org.pentaho.repository.create";

  private static final String ACTION_ADMINISTER_SECURITY = "org.pentaho.security.administerSecurity";

  // ~ Static fields/initializers ======================================================================================

  // ~ Instance fields =================================================================================================

  private IAuthorizationPolicy policy;

  // ~ Constructors ====================================================================================================

  public RoleAuthorizationPolicyTest() {
    super();
  }

  // ~ Methods =========================================================================================================

  @BeforeClass
  public static void setUpClass() throws Exception {
    // unfortunate reference to superclass
    JackrabbitRepositoryTestBase.setUpClass();
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
    JackrabbitRepositoryTestBase.tearDownClass();
  }

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
  }

  @Override
  @After
  public void tearDown() throws Exception {
    clearRoleBindings();
    // null out fields to get back memory
    policy = null;
    super.tearDown();
  }

  protected void clearRoleBindings() throws Exception {
    loginAsRepositoryAdmin();
    SimpleJcrTestUtils.deleteItem(testJcrTemplate, ServerRepositoryPaths.getTenantRootFolderPath(TENANT_ID_ACME)
        + ".authz");
    SimpleJcrTestUtils.deleteItem(testJcrTemplate, ServerRepositoryPaths.getTenantRootFolderPath(TENANT_ID_DUFF)
        + ".authz");
  }

  @Test(expected = AccessDeniedException.class)
  public void testAdministerSecurityAccessDenied() throws Exception {
    manager.startup();
    login(USERNAME_SUZY, TENANT_ID_ACME);
    roleBindingDao.setRoleBindings("acme_Authenticated", Arrays.asList(new String[] { LOGICAL_ROLE_READER }));
  }
  
  @Test
  public void testNoBoundLogicalRoles() throws Exception {
    manager.startup();
    login(USERNAME_JOE, TENANT_ID_ACME);
    assertEquals(Arrays.asList(new String[] { LOGICAL_ROLE_READER, LOGICAL_ROLE_CREATOR }), roleBindingDao.getBoundLogicalRoleNames(Arrays.asList(new String[] { "acme_Authenticated", "acme_ceo" })));
  }

  @Test
  public void testGetAllowedActions() throws Exception {
    manager.startup();
    // login with suzy (in tenant acme)
    login(USERNAME_SUZY, TENANT_ID_ACME);

    // test with null namespace
    List<String> allowedActions = policy.getAllowedActions(null);

    assertEquals(2, allowedActions.size());
    assertTrue(allowedActions.contains(ACTION_READ));
    assertTrue(allowedActions.contains(ACTION_CREATE));

    // test with explicit namespace
    allowedActions = policy.getAllowedActions(NAMESPACE_REPOSITORY);
    assertEquals(2, allowedActions.size());
    assertTrue(allowedActions.contains(ACTION_READ));
    assertTrue(allowedActions.contains(ACTION_CREATE));

    // test with bogus namespace
    allowedActions = policy.getAllowedActions(NAMESPACE_DOESNOTEXIST);
    assertEquals(0, allowedActions.size());

    // login with pat (in tenant duff); pat is granted "Authenticated" so he is allowed
    login(USERNAME_PAT, TENANT_ID_DUFF);
    allowedActions = policy.getAllowedActions(null);
    assertTrue(allowedActions.contains(ACTION_READ));
    assertTrue(allowedActions.contains(ACTION_CREATE));

    login(USERNAME_JOE, TENANT_ID_ACME, true);
    allowedActions = policy.getAllowedActions(NAMESPACE_REPOSITORY);
    assertEquals(2, allowedActions.size());
    assertTrue(allowedActions.contains(ACTION_READ));
    assertTrue(allowedActions.contains(ACTION_CREATE));
    allowedActions = policy.getAllowedActions(NAMESPACE_SECURITY);
    assertEquals(1, allowedActions.size());
    assertTrue(allowedActions.contains(ACTION_ADMINISTER_SECURITY));

    allowedActions = policy.getAllowedActions(NAMESPACE_PENTAHO);
    assertEquals(3, allowedActions.size());
  }

  @Test
  public void testIsAllowed() throws Exception {
    manager.startup();
    // login with user that is allowed to "administer security"
    login(USERNAME_JOE, TENANT_ID_ACME, true);
    assertTrue(policy.isAllowed(ACTION_READ));
    assertTrue(policy.isAllowed(ACTION_CREATE));
    assertTrue(policy.isAllowed(ACTION_ADMINISTER_SECURITY));

    login(USERNAME_SUZY, TENANT_ID_ACME);
    assertTrue(policy.isAllowed(ACTION_READ));
    assertTrue(policy.isAllowed(ACTION_CREATE));
    assertFalse(policy.isAllowed(ACTION_ADMINISTER_SECURITY));

    login(USERNAME_PAT, TENANT_ID_DUFF);
    assertTrue(policy.isAllowed(ACTION_READ));
    assertTrue(policy.isAllowed(ACTION_CREATE));
    assertFalse(policy.isAllowed(ACTION_ADMINISTER_SECURITY));
  }

  @Test
  public void testRemoveImmutableBinding() throws Exception {
    manager.startup();
    // login with user that is allowed to "administer security"
    login(USERNAME_JOE, TENANT_ID_ACME, true);
    try {
      roleBindingDao.setRoleBindings(RUNTIME_ROLE_ACME_ADMIN, Arrays.asList(new String[] { LOGICAL_ROLE_READER,
          LOGICAL_ROLE_CREATOR }));
      fail();
    } catch (Exception e) {

    }
  }

  @Test
  public void testGetRoleBindingStruct() throws Exception {
    manager.startup();
    // login with user that is allowed to "administer security"
    login(USERNAME_JOE, TENANT_ID_ACME, true);
    RoleBindingStruct struct = roleBindingDao.getRoleBindingStruct(Locale.getDefault().toString());
    assertNotNull(struct);
    assertNotNull(struct.bindingMap);
    assertEquals(4, struct.bindingMap.size());
    assertEquals(Arrays.asList(new String[] { LOGICAL_ROLE_READER, LOGICAL_ROLE_CREATOR,
        LOGICAL_ROLE_SECURITY_ADMINISTRATOR }), struct.bindingMap.get(RUNTIME_ROLE_ACME_ADMIN));
    assertEquals(Arrays.asList(new String[] { LOGICAL_ROLE_READER, LOGICAL_ROLE_CREATOR }), struct.bindingMap
        .get(RUNTIME_ROLE_ACME_AUTHENTICATED));
    roleBindingDao.setRoleBindings("whatever", Arrays.asList(new String[] { "org.pentaho.p1.reader" }));

    struct = roleBindingDao.getRoleBindingStruct(Locale.getDefault().toString());
    assertEquals(5, struct.bindingMap.size());
    assertEquals(Arrays.asList(new String[] { "org.pentaho.p1.reader" }), struct.bindingMap.get("whatever"));

    assertNotNull(struct.logicalRoleNameMap);
    assertEquals(3, struct.logicalRoleNameMap.size());
    assertEquals("Create Content", struct.logicalRoleNameMap.get(LOGICAL_ROLE_CREATOR));
  }

  @Test(expected = AccessDeniedException.class)
  public void testGetRoleBindingStructAccessDenied() throws Exception {
    manager.startup();
    // login with user that is not allowed to "administer security"
    login(USERNAME_SUZY, TENANT_ID_ACME);
    roleBindingDao.getRoleBindingStruct(Locale.getDefault().toString());
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    super.setApplicationContext(applicationContext);
    policy = (IAuthorizationPolicy) applicationContext.getBean("authorizationPolicy");
  }

}
