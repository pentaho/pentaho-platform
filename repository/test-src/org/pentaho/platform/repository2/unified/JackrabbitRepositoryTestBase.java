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

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository2.unified.IBackingRepositoryLifecycleManager;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.repository2.unified.jcr.SimpleJcrTestUtils;
import org.pentaho.platform.repository2.unified.lifecycle.AbstractBackingRepositoryLifecycleManager;
import org.pentaho.platform.security.policy.rolebased.IRoleAuthorizationPolicyRoleBindingDao;
import org.pentaho.test.platform.engine.core.MicroPlatform;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.extensions.jcr.JcrTemplate;
import org.springframework.extensions.jcr.SessionFactory;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.security.userdetails.User;
import org.springframework.security.userdetails.UserDetails;

@SuppressWarnings("nls")
public class JackrabbitRepositoryTestBase {

  // ~ Static fields/initializers ======================================================================================

  protected final String USERNAME_SUZY = "suzy";

  protected final String USERNAME_TIFFANY = "tiffany";

  protected final String USERNAME_PAT = "pat";

  protected final String USERNAME_JOE = "joe";

  protected final String USERNAME_GEORGE = "george";

  protected final String TENANT_ID_ACME = "acme";

  protected final String TENANT_ID_DUFF = "duff";

  // ~ Instance fields =================================================================================================

  protected String repositoryAdminUsername;

  protected String tenantAdminAuthorityNamePattern;

  protected String tenantAuthenticatedAuthorityNamePattern;

  /**
   * Used for state verification and test cleanup.
   */
  protected JcrTemplate testJcrTemplate;

  protected IBackingRepositoryLifecycleManager manager;

  protected IRoleAuthorizationPolicyRoleBindingDao roleBindingDao;

  protected AbstractBackingRepositoryLifecycleManager defaultBackingRepositoryLifecycleManager;

  protected AbstractBackingRepositoryLifecycleManager pdiBackingRepositoryLifecycleManager;
  
  protected static IAuthorizationPolicy authorizationPolicy;

  protected static MicroPlatform mp = new MicroPlatform();

  // ~ Constructors ====================================================================================================

  public JackrabbitRepositoryTestBase() {
    super();
  }

  public JackrabbitRepositoryTestBase(MicroPlatform mp) {
    super();
    JackrabbitRepositoryTestBase.mp = mp;
  }

  // ~ Methods =========================================================================================================

  public static void setUpClass() throws Exception {
    MicroPlatform mp = new MicroPlatform();
    // used by DefaultPentahoJackrabbitAccessControlHelper
    mp.define(IAuthorizationPolicy.class, DelegatingAuthorizationPolicy.class);
    mp.start();

    // folder cannot be deleted at teardown shutdown hooks have not yet necessarily completed
    // parent folder must match jcrRepository.homeDir bean property in repository-test-override.spring.xml
    FileUtils.deleteDirectory(new File("/tmp/jackrabbit-test"));
    PentahoSessionHolder.setStrategyName(PentahoSessionHolder.MODE_GLOBAL);
  }

  public static void tearDownClass() throws Exception {
  }

  public void setUp() throws Exception {
    logout();
  }

  public void tearDown() throws Exception {
    loginAsRepositoryAdmin();
    SimpleJcrTestUtils.deleteItem(testJcrTemplate, ServerRepositoryPaths.getPentahoRootFolderPath());
    logout();
    repositoryAdminUsername = null;
    tenantAdminAuthorityNamePattern = null;
    tenantAuthenticatedAuthorityNamePattern = null;
    roleBindingDao = null;
    defaultBackingRepositoryLifecycleManager = null;
    pdiBackingRepositoryLifecycleManager = null;
    authorizationPolicy = null;
    testJcrTemplate = null;
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
    roleBindingDao = (IRoleAuthorizationPolicyRoleBindingDao) applicationContext
        .getBean("roleAuthorizationPolicyRoleBindingDao");
    defaultBackingRepositoryLifecycleManager = (AbstractBackingRepositoryLifecycleManager) applicationContext
        .getBean("defaultBackingRepositoryLifecycleManager");
    pdiBackingRepositoryLifecycleManager = (AbstractBackingRepositoryLifecycleManager) applicationContext
        .getBean("pdiBackingRepositoryLifecycleManager");
    authorizationPolicy = (IAuthorizationPolicy) applicationContext
    .getBean("authorizationPolicy");
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

  /**
   * Allow PentahoSystem to create this class but it in turn delegates to the authorizationPolicy fetched from Spring's
   * ApplicationContext.
   */
  public static class DelegatingAuthorizationPolicy implements IAuthorizationPolicy {

    public List<String> getAllowedActions(final String actionNamespace) {
      return authorizationPolicy.getAllowedActions(actionNamespace);
    }

    public boolean isAllowed(final String actionName) {
      return authorizationPolicy.isAllowed(actionName);
    }

  }
  
  public IBackingRepositoryLifecycleManager getManager() {
    return manager;
  }
}
