package org.apache.jackrabbit.core;

import java.security.Principal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.jcr.AccessDeniedException;
import javax.jcr.Credentials;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.security.auth.Subject;

import org.apache.jackrabbit.api.security.principal.PrincipalIterator;
import org.apache.jackrabbit.api.security.principal.PrincipalManager;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.jackrabbit.core.config.AccessManagerConfig;
import org.apache.jackrabbit.core.config.LoginModuleConfig;
import org.apache.jackrabbit.core.config.SecurityConfig;
import org.apache.jackrabbit.core.config.SecurityManagerConfig;
import org.apache.jackrabbit.core.config.WorkspaceConfig;
import org.apache.jackrabbit.core.config.WorkspaceSecurityConfig;
import org.apache.jackrabbit.core.security.AMContext;
import org.apache.jackrabbit.core.security.AccessManager;
import org.apache.jackrabbit.core.security.DefaultAccessManager;
import org.apache.jackrabbit.core.security.JackrabbitSecurityManager;
import org.apache.jackrabbit.core.security.SecurityConstants;
import org.apache.jackrabbit.core.security.authentication.AuthContext;
import org.apache.jackrabbit.core.security.authentication.AuthContextProvider;
import org.apache.jackrabbit.core.security.authorization.AccessControlProvider;
import org.apache.jackrabbit.core.security.authorization.AccessControlProviderFactory;
import org.apache.jackrabbit.core.security.authorization.AccessControlProviderFactoryImpl;
import org.apache.jackrabbit.core.security.authorization.WorkspaceAccessManager;
import org.apache.jackrabbit.core.security.principal.AdminPrincipal;
import org.apache.jackrabbit.core.security.principal.PrincipalIteratorAdapter;
import org.apache.jackrabbit.core.security.principal.PrincipalManagerImpl;
import org.apache.jackrabbit.core.security.principal.PrincipalProvider;
import org.apache.jackrabbit.core.security.principal.PrincipalProviderRegistry;
import org.apache.jackrabbit.core.security.principal.ProviderRegistryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copy of {@code org.apache.jackrabbit.core.DefaultSecurityManager} from Jackrabbit 1.6.0 with modifications.
 * 
 * <p>Modifications</p>
 * <ul>
 * <li>
 * Creates a lenient WorkspaceAccessManager.
 * </li>
 * <li>
 * Creates a dummy PrincipalProvider.
 * </li>
 * <li>
 * Does NOT provide a UserManager. By not providing a UserManager, we avoid searching the repository for principals.
 * </li>
 * </ul>
 * 
 * @author mlowery
 */
public class PentahoSecurityManager implements JackrabbitSecurityManager {

  // TODO: should rather be placed in the core.security package. However protected access to SystemSession required to move here.
  /**
   * the default logger
   */
  private static final Logger log = LoggerFactory.getLogger(PentahoSecurityManager.class);

  /**
   * Flag indicating if the security manager was properly initialized.
   */
  private boolean initialized;

  /**
   * the repository implementation
   */
  private RepositoryImpl repository;

  /**
   * session on the system workspace.
   */
  private SystemSession securitySession;

  /**
   * System Sessions PrincipalMangager used for internal access to Principals
   */
  private PrincipalManager systemPrincipalManager;

  /**
   * The user id of the administrator. The value is retrieved from
   * configuration. If the config entry is missing a default id is used (see
   * {@link SecurityConstants#ADMIN_ID}).
   */
  private String adminId;

  /**
   * The user id of the anonymous user. The value is retrieved from
   * configuration. If the config entry is missing a default id is used (see
   * {@link SecurityConstants#ANONYMOUS_ID}).
   */
  private String anonymousId;

  /**
   * Contains the access control providers per workspace.
   * key = name of the workspace,
   * value = {@link AccessControlProvider}
   */
  private final Map acProviders = new HashMap();

  /**
   * the AccessControlProviderFactory
   */
  private AccessControlProviderFactory acProviderFactory;

  /**
   * the configured WorkspaceAccessManager
   */
  private WorkspaceAccessManager workspaceAccessManager;

  /**
   * the principal provider registry
   */
  private PrincipalProviderRegistry principalProviderRegistry;

  /**
   * factory for login-context {@see Repository#login())
   */
  private AuthContextProvider authContextProvider;

  //------------------------------------------< JackrabbitSecurityManager >---
  /**
   * @see JackrabbitSecurityManager#init(Repository, Session)
   */
  public synchronized void init(Repository repository, Session systemSession) throws RepositoryException {
    if (initialized) {
      throw new IllegalStateException("already initialized");
    }
    if (!(repository instanceof RepositoryImpl)) {
      throw new RepositoryException("RepositoryImpl expected");
    }
    if (!(systemSession instanceof SystemSession)) {
      throw new RepositoryException("SystemSession expected");
    }

    securitySession = (SystemSession) systemSession;
    this.repository = (RepositoryImpl) repository;

    SecurityConfig config = this.repository.getConfig().getSecurityConfig();
    LoginModuleConfig loginModConf = config.getLoginModuleConfig();

    // build AuthContextProvider based on appName + optional LoginModuleConfig
    authContextProvider = new AuthContextProvider(config.getAppName(), loginModConf);
    if (authContextProvider.isLocal()) {
      log.info("init: use Repository Login-Configuration for " + config.getAppName());
    } else if (authContextProvider.isJAAS()) {
      log.info("init: use JAAS login-configuration for " + config.getAppName());
    } else {
      String msg = "Neither JAAS nor RepositoryConfig contained a valid Configuriation for " + config.getAppName();
      log.error(msg);
      throw new RepositoryException(msg);
    }

    Properties[] moduleConfig = authContextProvider.getModuleConfig();

    // retrieve default-ids (admin and anomymous) from login-module-configuration.
    for (int i = 0; i < moduleConfig.length; i++) {
      if (moduleConfig[i].containsKey(LoginModuleConfig.PARAM_ADMIN_ID)) {
        adminId = moduleConfig[i].getProperty(LoginModuleConfig.PARAM_ADMIN_ID);
      }
      if (moduleConfig[i].containsKey(LoginModuleConfig.PARAM_ANONYMOUS_ID)) {
        anonymousId = moduleConfig[i].getProperty(LoginModuleConfig.PARAM_ANONYMOUS_ID);
      }
    }
    // fallback:
    if (adminId == null) {
      log.debug("No adminID defined in LoginModule/JAAS config -> using default.");
      adminId = SecurityConstants.ADMIN_ID;
    }
    if (anonymousId == null) {
      log.debug("No anonymousID defined in LoginModule/JAAS config -> using default.");
      anonymousId = SecurityConstants.ANONYMOUS_ID;
    }

    // init default ac-provider-factory
    acProviderFactory = new AccessControlProviderFactoryImpl();
    acProviderFactory.init(securitySession);

    // create the evalutor for workspace access
    SecurityManagerConfig smc = config.getSecurityManagerConfig();
    if (smc != null && smc.getWorkspaceAccessConfig() != null) {
      workspaceAccessManager = (WorkspaceAccessManager) smc.getWorkspaceAccessConfig().newInstance();
    } else {
      // fallback -> the default implementation
      log.debug("No WorkspaceAccessManager configured; using default.");
      workspaceAccessManager = new WorkspaceAccessManagerImpl();
    }
    workspaceAccessManager.init(securitySession);

    // initialize principa-provider registry
    // 1) create default
    PrincipalProvider defaultPP = new DummyPrincipalProvider();
    defaultPP.init(new Properties());
    // 2) create registry instance
    principalProviderRegistry = new ProviderRegistryImpl(defaultPP);
    // 3) register all configured principal providers.
    for (int i = 0; i < moduleConfig.length; i++) {
      principalProviderRegistry.registerProvider(moduleConfig[i]);
    }

    // create the principal manager for the security workspace
    systemPrincipalManager = new PrincipalManagerImpl(securitySession, principalProviderRegistry.getProviders());

    initialized = true;
  }

  /**
   * @see JackrabbitSecurityManager#dispose(String)
   */
  public void dispose(String workspaceName) {
    checkInitialized();
    synchronized (acProviders) {
      AccessControlProvider prov = (AccessControlProvider) acProviders.remove(workspaceName);
      if (prov != null) {
        prov.close();
      }
    }
  }

  /**
   * @see JackrabbitSecurityManager#close()
   */
  public void close() {
    checkInitialized();
    synchronized (acProviders) {
      Iterator itr = acProviders.values().iterator();
      while (itr.hasNext()) {
        ((AccessControlProvider) itr.next()).close();
      }
      acProviders.clear();
    }
  }

  /**
   * @see JackrabbitSecurityManager#getAccessManager(Session,AMContext)
   */
  public AccessManager getAccessManager(Session session, AMContext amContext) throws RepositoryException {
    checkInitialized();
    AccessManagerConfig amConfig = repository.getConfig().getSecurityConfig().getAccessManagerConfig();
    try {
      String wspName = session.getWorkspace().getName();
      AccessControlProvider pp = getAccessControlProvider(wspName);
      AccessManager accessMgr;
      if (amConfig == null) {
        log
            .debug("No configuration entry for AccessManager. Using org.apache.jackrabbit.core.security.DefaultAccessManager");
        accessMgr = new DefaultAccessManager();
      } else {
        accessMgr = (AccessManager) amConfig.newInstance();
      }

      accessMgr.init(amContext, pp, workspaceAccessManager);
      return accessMgr;
    } catch (AccessDeniedException e) {
      // re-throw
      throw e;
    } catch (Exception e) {
      // wrap in RepositoryException
      String msg = "Failed to instantiate AccessManager (" + amConfig.getClassName() + ")";
      log.error(msg, e);
      throw new RepositoryException(msg, e);
    }
  }

  /**
   * @see JackrabbitSecurityManager#getPrincipalManager(Session)
   */
  public synchronized PrincipalManager getPrincipalManager(Session session) throws RepositoryException {
    checkInitialized();
    if (session == securitySession) {
      return systemPrincipalManager;
    } else if (session instanceof SessionImpl) {
      SessionImpl sImpl = (SessionImpl) session;
      return new PrincipalManagerImpl(sImpl, principalProviderRegistry.getProviders());
    } else {
      throw new RepositoryException("Internal error: SessionImpl expected.");
    }
  }

  /**
   * @see JackrabbitSecurityManager#getUserManager(Session)
   */
  public UserManager getUserManager(Session session) throws RepositoryException {
    throw new UnsupportedOperationException();
  }

  /**
   * @see JackrabbitSecurityManager#getUserID(Subject)
   */
  public String getUserID(Subject subject) throws RepositoryException {
    checkInitialized();
    /* shortcut if the subject contains the AdminPrincipal in which case
       the userID is already known. */
    if (!subject.getPrincipals(AdminPrincipal.class).isEmpty()) {
      return adminId;
    }
    /*
     Retrieve userID from the subject.
     Since the subject may contain multiple principals and the principal
     name must not be equals to the UserID by definition, the userID
     may either be obtained from the login-credentials or from the
     user manager. in the latter case the set of principals present with
     the specified subject is used to search for the user.
    */
    String uid = null;
    // try simple access to userID over SimpleCredentials first.
    Iterator creds = subject.getPublicCredentials(SimpleCredentials.class).iterator();
    if (creds.hasNext()) {
      SimpleCredentials sc = (SimpleCredentials) creds.next();
      uid = sc.getUserID();
    } else {
      throw new UnsupportedOperationException();
    }
    return uid;
  }

  /**
   * Creates an AuthContext for the given {@link Credentials} and
   * {@link Subject}.<br>
   * This includes selection of application specific LoginModules and
   * initialization with credentials and Session to System-Workspace
   *
   * @return an {@link AuthContext} for the given Credentials, Subject
   * @throws RepositoryException in other exceptional repository states
   */
  public AuthContext getAuthContext(Credentials creds, Subject subject) throws RepositoryException {
    checkInitialized();
    return authContextProvider.getAuthContext(creds, subject, securitySession, principalProviderRegistry, adminId,
        anonymousId);
  }

  //--------------------------------------------------------------------------
  /**
   * Returns the access control provider for the specified
   * <code>workspaceName</code>.
   *
   * @param workspaceName Name of the workspace.
   * @return access control provider
   * @throws NoSuchWorkspaceException If no workspace with 'workspaceName' exists.
   * @throws RepositoryException
   */
  private AccessControlProvider getAccessControlProvider(String workspaceName) throws NoSuchWorkspaceException,
      RepositoryException {
    checkInitialized();
    synchronized (acProviders) {
      AccessControlProvider provider = (AccessControlProvider) acProviders.get(workspaceName);
      if (provider == null) {
        SystemSession systemSession = repository.getSystemSession(workspaceName);
        WorkspaceConfig conf = repository.getConfig().getWorkspaceConfig(workspaceName);
        WorkspaceSecurityConfig secConf = (conf == null) ? null : conf.getSecurityConfig();
        provider = acProviderFactory.createProvider(systemSession, secConf);
        acProviders.put(workspaceName, provider);
      }
      return provider;
    }
  }

  private void checkInitialized() {
    if (!initialized) {
      throw new IllegalStateException("Not initialized");
    }
  }

  //------------------------------------------------------< inner classes >---
  /**
   * <code>WorkspaceAccessManager</code> that always grants.
   */
  private class WorkspaceAccessManagerImpl implements SecurityConstants, WorkspaceAccessManager {

    //-----------------------------------------< WorkspaceAccessManager >---
    /**
     * {@inheritDoc}
     * @param securitySession
     */
    public void init(Session securitySession) throws RepositoryException {
      // nothing to do here.
    }

    /**
     * {@inheritDoc}
     */
    public void close() throws RepositoryException {
      // nothing to do here.
    }

    /**
     * {@inheritDoc}
     */
    public boolean grants(Set principals, String workspaceName) throws RepositoryException {
      return true;
    }
  }

  /**
   * Since PrincipalProviderRegistry must be constructed with at least one PrincipalProvider, give it this dummy one.
   */
  private class DummyPrincipalProvider implements PrincipalProvider {

    public boolean canReadPrincipal(Session session, Principal principalToRead) {
      return true;
    }

    public void close() {

    }

    public PrincipalIterator findPrincipals(String simpleFilter) {
      throw new UnsupportedOperationException();
    }

    public PrincipalIterator findPrincipals(String simpleFilter, int searchType) {
      throw new UnsupportedOperationException();
    }

    public PrincipalIterator getGroupMembership(Principal principal) {
      return PrincipalIteratorAdapter.EMPTY;
    }

    public Principal getPrincipal(String principalName) {
      return null;
    }

    public PrincipalIterator getPrincipals(int searchType) {
      throw new UnsupportedOperationException();
    }

    public void init(Properties options) {
    }

  }
}
