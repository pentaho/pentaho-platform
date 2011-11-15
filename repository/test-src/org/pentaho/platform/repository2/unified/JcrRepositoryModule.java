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

import org.apache.commons.io.FileUtils;
import org.pentaho.platform.api.engine.IAclHolder;
import org.pentaho.platform.api.engine.IAclVoter;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IPentahoAclEntry;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository2.unified.IBackingRepositoryLifecycleManager;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.repository2.unified.jcr.SimpleJcrTestUtils;
import org.pentaho.platform.repository2.unified.lifecycle.AbstractBackingRepositoryLifecycleManager;
import org.pentaho.platform.security.policy.rolebased.IRoleAuthorizationPolicyRoleBindingDao;
import org.pentaho.test.platform.engine.core.MicroPlatform;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.extensions.jcr.JcrTemplate;
import org.springframework.extensions.jcr.SessionFactory;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.acl.AclEntry;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.security.userdetails.User;
import org.springframework.security.userdetails.UserDetails;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("nls")
public class JcrRepositoryModule implements MicroPlatform.RepositoryModule {

  // ~ Static fields/initializers ======================================================================================

  public static final String USERNAME_SUZY = "suzy";

  public static final String USERNAME_TIFFANY = "tiffany";

  public static final String USERNAME_PAT = "pat";

  public static final String USERNAME_JOE = "joe";

  public static final String USERNAME_GEORGE = "george";

  public static final String TENANT_ID_ACME = "tenant0";

  public static final String TENANT_ID_DUFF = "tenant0";

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

  protected MicroPlatform mp;

  protected File tempDir;

  @Override
  public void setOwner(MicroPlatform mp) {
    this.mp = mp;
  }

  @Override
  public void up() throws Exception {
    tempDir = createTempDirectory();
    System.out.println("Created temp dir " + tempDir.getAbsolutePath());

    DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
    XmlBeanDefinitionReader bdr = new XmlBeanDefinitionReader(beanFactory);
    bdr.loadBeanDefinitions(new ClassPathResource("repository.spring.xml"));
    bdr.loadBeanDefinitions(new ClassPathResource("repository-test-security.spring.xml"));
    BeanDefinitionBuilder beanBuildr = BeanDefinitionBuilder
        .rootBeanDefinition("org.springframework.extensions.jcr.jackrabbit.RepositoryFactoryBean");
    beanBuildr.setScope(BeanDefinition.SCOPE_SINGLETON);
    beanBuildr.addPropertyValue("configuration", new ClassPathResource("/jackrabbit-test-repo.xml"));
    beanBuildr.addPropertyValue("homeDir", new FileSystemResource(tempDir));
    beanFactory.registerBeanDefinition("jcrRepository", beanBuildr.getBeanDefinition());
    
    SessionFactory jcrSessionFactory = (SessionFactory) beanFactory.getBean("jcrSessionFactory");

    PentahoSessionHolder.setStrategyName(PentahoSessionHolder.MODE_GLOBAL);
    SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_GLOBAL);

    manager = (IBackingRepositoryLifecycleManager) beanFactory.getBean("backingRepositoryLifecycleManager");

    testJcrTemplate = new JcrTemplate(jcrSessionFactory);
    testJcrTemplate.setAllowCreate(true);
    testJcrTemplate.setExposeNativeSession(true);
    repositoryAdminUsername = (String) beanFactory.getBean("repositoryAdminUsername");
    tenantAuthenticatedAuthorityNamePattern = (String) beanFactory.getBean("tenantAuthenticatedAuthorityNamePattern");
    tenantAdminAuthorityNamePattern = (String) beanFactory.getBean("tenantAdminAuthorityNamePattern");
    roleBindingDao = (IRoleAuthorizationPolicyRoleBindingDao) beanFactory
        .getBean("roleAuthorizationPolicyRoleBindingDao");
    defaultBackingRepositoryLifecycleManager = (AbstractBackingRepositoryLifecycleManager) beanFactory
        .getBean("defaultBackingRepositoryLifecycleManager");
    pdiBackingRepositoryLifecycleManager = (AbstractBackingRepositoryLifecycleManager) beanFactory
        .getBean("pdiBackingRepositoryLifecycleManager");

    mp.defineInstance(IUnifiedRepository.class, beanFactory.getBean("unifiedRepository"));
    // used by DefaultPentahoJackrabbitAccessControlHelper
    mp.define(IAuthorizationPolicy.class, DelegatingAuthorizationPolicy.class);
    mp.defineInstance(IAuthorizationPolicy.class, beanFactory.getBean("authorizationPolicy"));

    // Make sure we know the name of the admin role
    mp.defineInstance(IAclVoter.class, new JackrabbitRepositoryTestBase.MockAclVoter());
  }

  @Override
  public void login(final String username, final String tenantId) {
    manager.startup();
    login(username, tenantId, false);
  }

  @Override
  public void logout() {
    try {
      manager.shutdown();
      PentahoSessionHolder.removeSession();
      SecurityContextHolder.getContext().setAuthentication(null);
    } catch (Throwable t) {

    }
  }

  @Override
  public void down() {
    try {
      if (repositoryAdminUsername != null) { //error condition.. stuff wasn't initialized properly, so skip this
        loginAsRepositoryAdmin();
        SimpleJcrTestUtils.deleteItem(testJcrTemplate, ServerRepositoryPaths.getPentahoRootFolderPath());

        logout();
        repositoryAdminUsername = null;
        tenantAdminAuthorityNamePattern = null;
        tenantAuthenticatedAuthorityNamePattern = null;
        roleBindingDao = null;
        defaultBackingRepositoryLifecycleManager = null;
        pdiBackingRepositoryLifecycleManager = null;
        testJcrTemplate = null;
      }
    } finally {
      FileUtils.deleteQuietly(tempDir);
    }
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

  /**
   * Allow PentahoSystem to create this class but it in turn delegates to the authorizationPolicy fetched from Spring's
   * ApplicationContext.
   */
  public class DelegatingAuthorizationPolicy implements IAuthorizationPolicy {

    private IAuthorizationPolicy authorizationPolicy;

    public DelegatingAuthorizationPolicy(IAuthorizationPolicy authorizationPolicy) {
      this.authorizationPolicy = authorizationPolicy;
    }

    public List<String> getAllowedActions(final String actionNamespace) {
      return authorizationPolicy.getAllowedActions(actionNamespace);
    }

    public boolean isAllowed(final String actionName) {
      return authorizationPolicy.isAllowed(actionName);
    }

  }

  public static File createTempDirectory() throws IOException {
    final File temp;

    temp = File.createTempFile(JcrRepositoryModule.class.getSimpleName(), Long.toString(System.nanoTime()) + ".d");

    if (!(temp.delete())) {
      throw new IOException("Temp dir already exists and couldn't delete it: " + temp.getAbsolutePath());
    }

    if (!(temp.mkdir())) {
      throw new IOException("Could not create temp dir: " + temp.getAbsolutePath());
    }

    return (temp);
  }
}
