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
 *
 *
 * Copyright 2006 - 2014 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.repository2.unified;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.jcr.security.AccessControlException;

import org.apache.commons.io.FileUtils;
import org.apache.jackrabbit.api.JackrabbitWorkspace;
import org.apache.jackrabbit.api.security.authorization.PrivilegeManager;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoRole;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoUser;
import org.pentaho.platform.api.engine.security.userroledao.IUserRoleDao;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.mt.ITenantManager;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.pentaho.platform.api.repository2.unified.IBackingRepositoryLifecycleManager;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid;
import org.pentaho.platform.api.repository2.unified.data.sample.SampleRepositoryFileData;
import org.pentaho.platform.core.mt.Tenant;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.objfac.StandaloneSpringPentahoObjectFactory;
import org.pentaho.platform.repository2.unified.jcr.DefaultLockHelper;
import org.pentaho.platform.repository2.unified.jcr.IPathConversionHelper;
import org.pentaho.platform.repository2.unified.jcr.JcrTenantUtils;
import org.pentaho.platform.repository2.unified.jcr.PentahoJcrConstants;
import org.pentaho.platform.repository2.unified.jcr.RepositoryFileProxyFactory;
import org.pentaho.platform.repository2.unified.jcr.SimpleJcrTestUtils;
import org.pentaho.platform.repository2.unified.jcr.jackrabbit.security.TestPrincipalProvider;
import org.pentaho.platform.repository2.unified.jcr.sejcr.CredentialsStrategy;
import org.pentaho.platform.security.policy.rolebased.IRoleAuthorizationPolicyRoleBindingDao;
import org.pentaho.platform.security.userroledao.DefaultTenantedPrincipleNameResolver;
import org.pentaho.test.platform.engine.core.MicroPlatform;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.extensions.jcr.JcrCallback;
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
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Integration test. Tests {@link DefaultUnifiedRepository} and
 * {@link org.pentaho.platform.api.engine.IAuthorizationPolicy IAuthorizationPolicy} fully configured behind Spring
 * Security's method security and Spring's transaction interceptor.
 * 
 * <p>
 * Note the RunWith annotation that uses a special runner that knows how to setup a Spring application context. The
 * application context config files are listed in the ContextConfiguration annotation. By implementing
 * {@link org.springframework.context.ApplicationContextAware ApplicationContextAware}, this unit test can access
 * various beans defined in the application context, including the bean under test.
 * </p>
 * 
 * This is Base class for all DefaultUnifiedRepository tests
 * 
 * @author mlowery
 * @author Aliaksei_Haidukou
 */
@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration( locations = { "classpath:/repository.spring.xml",
    "classpath:/repository-test-override.spring.xml" } )
@SuppressWarnings( "nls" )
public class DefaultUnifiedRepositoryBase implements ApplicationContextAware {

  protected static final String NAMESPACE_REPOSITORY = "org.pentaho.repository";

  protected static final String NAMESPACE_SECURITY = "org.pentaho.security";

  protected static final String NAMESPACE_SCHEDULER = "org.pentaho.scheduler";

  protected static final String NAMESPACE_PENTAHO = "org.pentaho";

  protected static final String NAMESPACE_DOESNOTEXIST = "doesnotexist";

  protected static final String USERNAME_SUZY = "suzy";

  protected static final String USERNAME_TIFFANY = "tiffany";

  protected static final String USERNAME_PAT = "pat";

  protected static final String USERNAME_ADMIN = "admin";

  protected static final String USERNAME_GEORGE = "george";

  protected static final String TENANT_ID_ACME = "acme";

  protected static final String TENANT_ID_DUFF = "duff";

  protected static final String ANONYMOUS_ROLE_NAME = "Anonymous";

  protected static final String AUTHENTICATED_ROLE_NAME = "Authenticated";

  protected static final String PASSWORD = "password";

  // ~ Instance fields
  // =================================================================================================

  protected ITenantManager tenantManager;

  protected IUnifiedRepository repo;

  protected String repositoryAdminUsername;
  protected String singleTenantAdminUserName;

  protected IBackingRepositoryLifecycleManager defaultBackingRepositoryLifecycleManager;

  /**
   * Used for state verification and test cleanup.
   */
  protected JcrTemplate testJcrTemplate;
  protected JcrTemplate jcrTemplate;

  protected IRoleAuthorizationPolicyRoleBindingDao roleBindingDao;
  protected IRoleAuthorizationPolicyRoleBindingDao roleBindingDaoTarget;
  protected IRepositoryFileDao repositoryFileDao;
  protected IRepositoryFileAclDao repositoryFileAclDao;
  protected IAuthorizationPolicy authorizationPolicy;

  protected MicroPlatform mp;

  protected ITenantedPrincipleNameResolver userNameUtils = new DefaultTenantedPrincipleNameResolver();

  protected ITenantedPrincipleNameResolver roleNameUtils = new DefaultTenantedPrincipleNameResolver(
    DefaultTenantedPrincipleNameResolver.ALTERNATE_DELIMETER );

  protected String superAdminRoleName;
  protected String tenantAdminRoleName;
  protected String tenantAuthenticatedRoleName;
  protected String sysAdminUserName;
  protected ITenant systemTenant;
  protected IPathConversionHelper pathConversionHelper;
  protected IUserRoleDao userRoleDao;
  protected IUserRoleDao testUserRoleDao;
  protected TransactionTemplate jcrTransactionTemplate;
  protected TransactionTemplate txnTemplate;

  @BeforeClass
  public static void setUpClass() throws Exception {
    // folder cannot be deleted at teardown shutdown hooks have not yet necessarily completed
    // parent folder must match jcrRepository.homeDir bean property in repository-test-override.spring.xml
    FileUtils.deleteDirectory( new File( "/tmp/jackrabbit-test-TRUNK" ) );
    PentahoSessionHolder.setStrategyName( PentahoSessionHolder.MODE_GLOBAL );

    // register repository spring context for correct work of <pen:list>
    final StandaloneSpringPentahoObjectFactory pentahoObjectFactory = new StandaloneSpringPentahoObjectFactory();
    GenericApplicationContext appCtx = new GenericApplicationContext();
    XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader( appCtx );
    xmlReader.loadBeanDefinitions( new ClassPathResource( "repository.spring.xml" ) );
    xmlReader.loadBeanDefinitions( new ClassPathResource( "repository-test-override.spring.xml" ) );
    pentahoObjectFactory.init( null, appCtx );
    PentahoSystem.registerObjectFactory( pentahoObjectFactory );
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
    PentahoSessionHolder.setStrategyName( PentahoSessionHolder.MODE_INHERITABLETHREADLOCAL );
  }

  @Before
  public void setUp() throws Exception {
    loginAsRepositoryAdmin();
    SimpleJcrTestUtils.deleteItem( testJcrTemplate, ServerRepositoryPaths.getPentahoRootFolderPath() );
    mp = new MicroPlatform( getSolutionPath() );
    // used by DefaultPentahoJackrabbitAccessControlHelper
    mp.defineInstance( "tenantedUserNameUtils", userNameUtils );
    mp.defineInstance( "tenantedRoleNameUtils", roleNameUtils );
    mp.defineInstance( "ILockHelper", new DefaultLockHelper( userNameUtils ) );

    mp.defineInstance( IAuthorizationPolicy.class, authorizationPolicy );
    mp.defineInstance( ITenantManager.class, tenantManager );
    mp.defineInstance( "roleAuthorizationPolicyRoleBindingDaoTarget", roleBindingDaoTarget );
    mp.defineInstance( "repositoryAdminUsername", repositoryAdminUsername );
    mp.defineInstance( "RepositoryFileProxyFactory", new RepositoryFileProxyFactory( this.jcrTemplate,
      this.repositoryFileDao ) );
    mp.defineInstance( "ITenantedPrincipleNameResolver", new DefaultTenantedPrincipleNameResolver() );
    mp.defineInstance("useMultiByteEncoding", Boolean.FALSE );
    mp.defineInstance( IUnifiedRepository.class, repo );
    mp.defineInstance( IRepositoryFileAclDao.class, repositoryFileAclDao );
    IUserRoleListService userRoleListService = mock( IUserRoleListService.class );
    when( userRoleListService.getRolesForUser( any( ITenant.class ), anyString() ) ).thenReturn(
      Arrays.asList( tenantAdminRoleName, AUTHENTICATED_ROLE_NAME ) );
    mp.defineInstance( IUserRoleListService.class, userRoleListService );
    mp.defineInstance( "singleTenantAdminUserName", singleTenantAdminUserName );
    // Start the micro-platform
    mp.start();
    loginAsRepositoryAdmin();
    setAclManagement();

    systemTenant =
      tenantManager.createTenant( null, ServerRepositoryPaths.getPentahoRootFolderName(), tenantAdminRoleName,
        tenantAuthenticatedRoleName, ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( systemTenant, sysAdminUserName, PASSWORD, "", new String[] { tenantAdminRoleName } );
    logout();
  }

  @After
  public void tearDown() throws Exception {
    // null out fields to get back memory
    authorizationPolicy = null;
    loginAsSysTenantAdmin();
    ITenant tenant =
      tenantManager.getTenant( "/" + ServerRepositoryPaths.getPentahoRootFolderName() + "/" + TENANT_ID_ACME );
    if ( tenant != null ) {
      cleanupUserAndRoles( tenant );
    }
    loginAsSysTenantAdmin();
    tenant = tenantManager.getTenant( "/" + ServerRepositoryPaths.getPentahoRootFolderName() + "/" + TENANT_ID_DUFF );
    if ( tenant != null ) {
      cleanupUserAndRoles( tenant );
    }
    cleanupUserAndRoles( systemTenant );
    SimpleJcrTestUtils.deleteItem( testJcrTemplate, ServerRepositoryPaths.getPentahoRootFolderPath() );
    logout();

    repositoryAdminUsername = null;
    singleTenantAdminUserName = null;
    tenantAdminRoleName = null;
    tenantAuthenticatedRoleName = null;
    roleBindingDao = null;
    authorizationPolicy = null;
    testJcrTemplate = null;
    tenantManager = null;
    repo = null;
    defaultBackingRepositoryLifecycleManager = null;
    roleBindingDaoTarget = null;
    repositoryFileDao = null;
    repositoryFileAclDao = null;
    authorizationPolicy = null;
    mp = null;
    superAdminRoleName = null;
    tenantAdminRoleName = null;
    tenantAuthenticatedRoleName = null;
    sysAdminUserName = null;
    systemTenant = null;
    pathConversionHelper = null;
    userRoleDao = null;
    testUserRoleDao = null;
    jcrTransactionTemplate = null;
    txnTemplate = null;
  }

  @Test
  public void testOnStartup() throws Exception {
    loginAsSysTenantAdmin();
    // make sure pentaho root folder exists
    final String rootFolderPath = ServerRepositoryPaths.getPentahoRootFolderPath();
    assertNotNull( SimpleJcrTestUtils.getItem( testJcrTemplate, rootFolderPath ) );
  }

  protected void loginAsSysTenantAdmin() {
    login( sysAdminUserName, systemTenant, new String[]{ tenantAdminRoleName, tenantAuthenticatedRoleName } );
  }

  /**
   * Logs in with given username.
   * 
   * @param username
   *          username of user
   * @param tenant
   *          tenant to which this user belongs
   * @param roles
   *          user roles
   */
  public void login( final String username, final ITenant tenant, String[] roles ) {
    StandaloneSession pentahoSession = new StandaloneSession( username );
    pentahoSession.setAuthenticated( tenant.getId(), username );
    PentahoSessionHolder.setSession( pentahoSession );
    pentahoSession.setAttribute( IPentahoSession.TENANT_ID_KEY, tenant.getId() );

    List<GrantedAuthority> authList = new ArrayList<GrantedAuthority>();

    for ( String roleName : roles ) {
      authList.add( new GrantedAuthorityImpl( roleName ) );
    }
    GrantedAuthority[] authorities = authList.toArray( new GrantedAuthority[0] );
    UserDetails userDetails = new User( username, PASSWORD, true, true, true, true, authorities );
    Authentication auth = new UsernamePasswordAuthenticationToken( userDetails, PASSWORD, authorities );
    PentahoSessionHolder.setSession( pentahoSession );
    // this line necessary for Spring Security's MethodSecurityInterceptor
    SecurityContextHolder.getContext().setAuthentication( auth );

    createUserHomeFolder( tenant, username );
    defaultBackingRepositoryLifecycleManager.newTenant();
  }

  public void loginAsRepositoryAdmin() {
    StandaloneSession pentahoSession = new StandaloneSession( repositoryAdminUsername );
    pentahoSession.setAuthenticated( repositoryAdminUsername );
    final GrantedAuthority[] repositoryAdminAuthorities =
      new GrantedAuthority[] { new GrantedAuthorityImpl( superAdminRoleName ) };
    final String password = "ignored";
    UserDetails repositoryAdminUserDetails =
      new User( repositoryAdminUsername, password, true, true, true, true, repositoryAdminAuthorities );
    Authentication repositoryAdminAuthentication =
      new UsernamePasswordAuthenticationToken( repositoryAdminUserDetails, password, repositoryAdminAuthorities );
    PentahoSessionHolder.setSession( pentahoSession );
    // this line necessary for Spring Security's MethodSecurityInterceptor
    SecurityContextHolder.getContext().setAuthentication( repositoryAdminAuthentication );
  }

  protected void logout() {
    PentahoSessionHolder.removeSession();
    SecurityContextHolder.getContext().setAuthentication( null );
  }

  protected void createUserHomeFolder( final ITenant theTenant, final String theUsername ) {
    IPentahoSession origPentahoSession = PentahoSessionHolder.getSession();
    Authentication origAuthentication = SecurityContextHolder.getContext().getAuthentication();
    StandaloneSession pentahoSession = new StandaloneSession( repositoryAdminUsername );
    pentahoSession.setAuthenticated( null, repositoryAdminUsername );
    PentahoSessionHolder.setSession( pentahoSession );
    try {
      txnTemplate.execute( new TransactionCallbackWithoutResult() {
        public void doInTransactionWithoutResult( final TransactionStatus status ) {
          RepositoryFileAcl.Builder aclsForUserHomeFolder = null;
          RepositoryFileAcl.Builder aclsForTenantHomeFolder = null;
          ITenant tenant = null;
          String username = null;
          if ( theTenant == null ) {
            tenant = getTenant( username, true );
            username = getPrincipalName( theUsername, true );
          } else {
            tenant = theTenant;
            username = theUsername;
          }
          if ( tenant == null || tenant.getId() == null ) {
            tenant = getCurrentTenant();
          }
          if ( tenant == null || tenant.getId() == null ) {
            tenant = JcrTenantUtils.getDefaultTenant();
          }
          RepositoryFile userHomeFolder = null;
          String userId = userNameUtils.getPrincipleId( theTenant, username );
          final RepositoryFileSid userSid = new RepositoryFileSid( userId );
          RepositoryFile tenantHomeFolder = null;
          RepositoryFile tenantRootFolder = null;
          // Get the Tenant Root folder. If the Tenant Root folder does not exist then exit.
          tenantRootFolder =
            repositoryFileDao.getFileByAbsolutePath( ServerRepositoryPaths.getTenantRootFolderPath( theTenant ) );
          if ( tenantRootFolder != null ) {
            // Try to see if Tenant Home folder exist
            tenantHomeFolder =
              repositoryFileDao.getFileByAbsolutePath( ServerRepositoryPaths.getTenantHomeFolderPath( theTenant ) );
            if ( tenantHomeFolder == null ) {
              String ownerId = userNameUtils.getPrincipleId( theTenant, username );
              RepositoryFileSid ownerSid = new RepositoryFileSid( ownerId, RepositoryFileSid.Type.USER );

              String tenantAuthenticatedRoleId = roleNameUtils.getPrincipleId( theTenant, tenantAuthenticatedRoleName );
              RepositoryFileSid tenantAuthenticatedRoleSid =
                new RepositoryFileSid( tenantAuthenticatedRoleId, RepositoryFileSid.Type.ROLE );

              aclsForTenantHomeFolder =
                new RepositoryFileAcl.Builder( userSid ).ace( tenantAuthenticatedRoleSid, EnumSet
                  .of( RepositoryFilePermission.READ ) );

              aclsForUserHomeFolder =
                new RepositoryFileAcl.Builder( userSid ).ace( ownerSid, EnumSet.of( RepositoryFilePermission.ALL ) );
              tenantHomeFolder =
                repositoryFileDao.createFolder( tenantRootFolder.getId(), new RepositoryFile.Builder(
                  ServerRepositoryPaths.getTenantHomeFolderName() ).folder( true ).build(), aclsForTenantHomeFolder
                  .build(), "tenant home folder" );
            } else {
              String ownerId = userNameUtils.getPrincipleId( theTenant, username );
              RepositoryFileSid ownerSid = new RepositoryFileSid( ownerId, RepositoryFileSid.Type.USER );
              aclsForUserHomeFolder =
                new RepositoryFileAcl.Builder( userSid ).ace( ownerSid, EnumSet.of( RepositoryFilePermission.ALL ) );
            }

            // now check if user's home folder exist
            userHomeFolder =
              repositoryFileDao.getFileByAbsolutePath( ServerRepositoryPaths
                .getUserHomeFolderPath( theTenant, username ) );
            if ( userHomeFolder == null ) {
              userHomeFolder =
                repositoryFileDao.createFolder( tenantHomeFolder.getId(), new RepositoryFile.Builder( username )
                  .folder( true ).build(), aclsForUserHomeFolder.build(), "user home folder" ); //$NON-NLS-1$
            }
          }
        }
      } );
    } finally {
      // Switch our identity back to the original user.
      PentahoSessionHolder.setSession( origPentahoSession );
      SecurityContextHolder.getContext().setAuthentication( origAuthentication );
    }
  }

  protected String getPrincipalName( String principalId, boolean isUser ) {
    String principalName = null;
    ITenantedPrincipleNameResolver nameUtils = isUser ? userNameUtils : roleNameUtils;
    if ( nameUtils != null ) {
      principalName = nameUtils.getPrincipleName( principalId );
    }
    return principalName;
  }

  protected ITenant getTenant( String principalId, boolean isUser ) {
    ITenant tenant = null;
    ITenantedPrincipleNameResolver nameUtils = isUser ? userNameUtils : roleNameUtils;
    if ( nameUtils != null ) {
      tenant = nameUtils.getTenant( principalId );
    }
    if ( tenant == null || tenant.getId() == null ) {
      tenant = getCurrentTenant();
    }
    return tenant;
  }

  protected ITenant getCurrentTenant() {
    if ( PentahoSessionHolder.getSession() != null ) {
      String tenantId = (String) PentahoSessionHolder.getSession().getAttribute( IPentahoSession.TENANT_ID_KEY );
      return tenantId != null ? new Tenant( tenantId, true ) : null;
    } else {
      return null;
    }
  }

  public ITenant getSystemTenant() {
    return systemTenant;
  }

  public ITenant createTenant( ITenant parentTenant, String tenantName ) {
    return tenantManager.createTenant( parentTenant, tenantName, tenantAdminRoleName, tenantAuthenticatedRoleName,
      ANONYMOUS_ROLE_NAME );
  }

  public IPentahoUser createUser( ITenant tenant, String username, String password, String... roles ) {
    return userRoleDao.createUser( tenant, username, password, "", roles );
  }

  protected RepositoryFile createSampleFile( final String parentFolderPath, final String fileName,
    final String sampleString, final boolean sampleBoolean, final int sampleInteger, boolean versioned )
    throws Exception {
    RepositoryFile parentFolder = repo.getFile( parentFolderPath );
    final SampleRepositoryFileData content = new SampleRepositoryFileData( sampleString, sampleBoolean, sampleInteger );
    return repo.createFile( parentFolder.getId(),
      new RepositoryFile.Builder( fileName ).versioned( versioned ).build(), content, null );
  }

  protected RepositoryFile createSampleFile( final String parentFolderPath, final String fileName,
    final String sampleString, final boolean sampleBoolean, final int sampleInteger ) throws Exception {
    return createSampleFile( parentFolderPath, fileName, sampleString, sampleBoolean, sampleInteger, false );
  }

  public String getTenantAdminRoleName() {
    return tenantAdminRoleName;
  }

  private void setAclManagement() {
    testJcrTemplate.execute( new JcrCallback() {
      @Override
      public Object doInJcr( Session session ) throws IOException, RepositoryException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants( session );
        Workspace workspace = session.getWorkspace();
        PrivilegeManager privilegeManager = ( (JackrabbitWorkspace) workspace ).getPrivilegeManager();
        try {
          privilegeManager.getPrivilege( pentahoJcrConstants.getPHO_ACLMANAGEMENT_PRIVILEGE() );
        } catch ( AccessControlException ace ) {
          privilegeManager.registerPrivilege( pentahoJcrConstants.getPHO_ACLMANAGEMENT_PRIVILEGE(), false,
            new String[0] );
        }
        session.save();
        return null;
      }
    } );
  }

  public void cleanupUserAndRoles( final ITenant tenant ) {
    loginAsRepositoryAdmin();
    for ( IPentahoRole role : testUserRoleDao.getRoles( tenant ) ) {
      testUserRoleDao.deleteRole( role );
    }
    for ( IPentahoUser user : testUserRoleDao.getUsers( tenant ) ) {
      testUserRoleDao.deleteUser( user );
    }
  }

  public MicroPlatform getMp() {

    return mp;
  }

  @Override
  public void setApplicationContext( ApplicationContext applicationContext ) throws BeansException {
    SessionFactory jcrSessionFactory = (SessionFactory) applicationContext.getBean( "jcrSessionFactory" );
    testJcrTemplate = new JcrTemplate( jcrSessionFactory );
    testJcrTemplate.setAllowCreate( true );
    testJcrTemplate.setExposeNativeSession( true );

    jcrTemplate = (JcrTemplate) applicationContext.getBean( "jcrTemplate" );

    repositoryAdminUsername = (String) applicationContext.getBean( "repositoryAdminUsername" );
    singleTenantAdminUserName = (String) applicationContext.getBean( "singleTenantAdminUserName" );
    superAdminRoleName = (String) applicationContext.getBean( "superAdminAuthorityName" );
    sysAdminUserName = (String) applicationContext.getBean( "superAdminUserName" );
    tenantAuthenticatedRoleName = (String) applicationContext.getBean( "singleTenantAuthenticatedAuthorityName" );
    tenantAdminRoleName = (String) applicationContext.getBean( "singleTenantAdminAuthorityName" );
    tenantManager = (ITenantManager) applicationContext.getBean( "tenantMgrProxy" );
    pathConversionHelper = (IPathConversionHelper) applicationContext.getBean( "pathConversionHelper" );
    roleBindingDao =
      (IRoleAuthorizationPolicyRoleBindingDao) applicationContext.getBean( "roleAuthorizationPolicyRoleBindingDaoTxn" );
    roleBindingDaoTarget =
      (IRoleAuthorizationPolicyRoleBindingDao) applicationContext
        .getBean( "roleAuthorizationPolicyRoleBindingDaoTarget" );
    authorizationPolicy = (IAuthorizationPolicy) applicationContext.getBean( "authorizationPolicy" );
    repo = (IUnifiedRepository) applicationContext.getBean( "unifiedRepository" );
    userRoleDao = (IUserRoleDao) applicationContext.getBean( "userRoleDao" );
    jcrTransactionTemplate = (TransactionTemplate) applicationContext.getBean( "jcrTransactionTemplate" );
    defaultBackingRepositoryLifecycleManager =
      (IBackingRepositoryLifecycleManager) applicationContext.getBean( "defaultBackingRepositoryLifecycleManager" );
    repositoryFileDao = (IRepositoryFileDao) applicationContext.getBean( "repositoryFileDao" );
    repositoryFileAclDao = (IRepositoryFileAclDao) applicationContext.getBean( "repositoryFileAclDao" );
    testUserRoleDao = userRoleDao;
    txnTemplate = (TransactionTemplate) applicationContext.getBean( "jcrTransactionTemplate" );
    TestPrincipalProvider.userRoleDao = testUserRoleDao;
    TestPrincipalProvider.adminCredentialsStrategy =
      (CredentialsStrategy) applicationContext.getBean( "jcrAdminCredentialsStrategy" );
    TestPrincipalProvider.repository = (Repository) applicationContext.getBean( "jcrRepository" );
  }

  protected String getSolutionPath() {
    return null;
  }
}
