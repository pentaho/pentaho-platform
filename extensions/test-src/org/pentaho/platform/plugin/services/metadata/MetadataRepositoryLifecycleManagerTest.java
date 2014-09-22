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
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.plugin.services.metadata;

import static junit.framework.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.pentaho.platform.api.engine.IConfiguration;
import org.pentaho.platform.api.engine.IPentahoDefinableObjectFactory.Scope;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPluginManager;
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
import org.pentaho.platform.config.SystemConfig;
import org.pentaho.platform.core.mt.Tenant;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.boot.PlatformInitializationException;
import org.pentaho.platform.plugin.services.pluginmgr.DefaultPluginManager;
import org.pentaho.platform.repository2.ClientRepositoryPaths;
import org.pentaho.platform.repository2.unified.IRepositoryFileDao;
import org.pentaho.platform.repository2.unified.ServerRepositoryPaths;
import org.pentaho.platform.repository2.unified.jcr.JcrRepositoryDumpToFile;
import org.pentaho.platform.repository2.unified.jcr.JcrRepositoryDumpToFile.Mode;
import org.pentaho.platform.repository2.unified.jcr.PentahoJcrConstants;
import org.pentaho.platform.repository2.unified.jcr.RepositoryFileProxyFactory;
import org.pentaho.platform.repository2.unified.jcr.SimpleJcrTestUtils;
import org.pentaho.platform.repository2.unified.jcr.jackrabbit.security.TestPrincipalProvider;
import org.pentaho.platform.repository2.unified.jcr.sejcr.CredentialsStrategy;
import org.pentaho.platform.security.policy.rolebased.IRoleAuthorizationPolicyRoleBindingDao;
import org.pentaho.platform.security.userroledao.service.UserRoleDaoUserDetailsService;
import org.pentaho.platform.security.userroledao.service.UserRoleDaoUserRoleListService;
import org.pentaho.test.platform.engine.core.MicroPlatform;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
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
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Class Description User: dkincade
 */
@RunWith ( SpringJUnit4ClassRunner.class )
@ContextConfiguration ( locations = { "classpath:/repository.spring.xml",
    "classpath:/repository-test-override.spring.xml" } )
@SuppressWarnings ( "nls" )
public class MetadataRepositoryLifecycleManagerTest implements ApplicationContextAware {
  public static final String REPOSITORY_ADMIN_USERNAME = "pentahoRepoAdmin";

  public static final String SINGLE_TENANT_AUTHENTICATED_AUTHORITY_NAME = "Authenticated";

  private static final String TEST_TENANT_ID = "Pentaho";

  private static final String TEST_USER_ID = "admin";

  private static MicroPlatform mp = new MicroPlatform();

  public static final String MAIN_TENANT_1 = "maintenant1";

  private IUnifiedRepository repo;

  private IUserRoleListService userRoleListService;

  private boolean startupCalled;

  private String repositoryAdminUsername;

  private String adminAuthorityName;

  private String tenantAuthenticatedAuthorityName;

  private JcrTemplate testJcrTemplate;

  private IBackingRepositoryLifecycleManager manager;

  private IAuthorizationPolicy authorizationPolicy;

  private IBackingRepositoryLifecycleManager metadataRepositoryLifecycleManager;

  IUserRoleDao userRoleDao;

  private ITenantManager tenantManager;

  private String sysAdminAuthorityName;

  private String sysAdminUserName;

  private String superAdminRoleName;

  private IRepositoryFileDao repositoryFileDao;

  private Repository repository = null;

  private ITenant systemTenant = null;

  private ITenant testTenant;

  private ITenantedPrincipleNameResolver tenantedRoleNameUtils;

  private ITenantedPrincipleNameResolver tenantedUserNameUtils;

  private IRoleAuthorizationPolicyRoleBindingDao roleBindingDaoTarget;
  private static TransactionTemplate jcrTransactionTemplate;
  private JcrTemplate jcrTemplate;

  public static final String SYSTEM_PROPERTY = "spring.security.strategy";

  @BeforeClass
  public static void beforeClass() throws Exception {
    System.setProperty( SYSTEM_PROPERTY, "MODE_GLOBAL" );
    PentahoSessionHolder.setStrategyName( PentahoSessionHolder.MODE_GLOBAL );
    FileUtils.deleteDirectory( new File( "/tmp/jackrabbit-test-TRUNK" ) );
    SecurityContextHolder.setStrategyName( SecurityContextHolder.MODE_GLOBAL );
  }

  @AfterClass
  public static void afterClass() {
    PentahoSessionHolder.setStrategyName( PentahoSessionHolder.MODE_INHERITABLETHREADLOCAL );
  }

  @Before
  public void beforeTest() throws PlatformInitializationException {
    System.setProperty( SYSTEM_PROPERTY, "MODE_INHERITABLETHREADLOCAL" );
    mp = new MicroPlatform();
    mp.defineInstance( "tenantedUserNameUtils", tenantedUserNameUtils );
    mp.define( IPluginManager.class, DefaultPluginManager.class, Scope.GLOBAL );
    mp.defineInstance( IAuthorizationPolicy.class, authorizationPolicy );
    mp.defineInstance( ITenantManager.class, tenantManager );
    mp.define( ITenant.class, Tenant.class );
    mp.defineInstance( "roleAuthorizationPolicyRoleBindingDaoTarget", roleBindingDaoTarget );
    mp.defineInstance( IRoleAuthorizationPolicyRoleBindingDao.class, roleBindingDaoTarget );
    mp.defineInstance( "tenantedUserNameUtils", tenantedUserNameUtils );
    mp.defineInstance( "tenantedRoleNameUtils", tenantedRoleNameUtils );
    mp.defineInstance( "repositoryAdminUsername", repositoryAdminUsername );
    mp.define( IConfiguration.class, SystemConfig.class );
    mp.defineInstance( "RepositoryFileProxyFactory", new RepositoryFileProxyFactory( this.jcrTemplate, this.repositoryFileDao ) );
    UserRoleDaoUserDetailsService userDetailsService = new UserRoleDaoUserDetailsService();
    userDetailsService.setUserRoleDao( userRoleDao );
    List<String> systemRoles = new ArrayList<String>();
    systemRoles.add( "Administrator" );
    List<String> extraRoles = Arrays.asList( new String[]{"Authenticated", "Anonymous"} );
    String adminRole = "Admin";
    userRoleListService =
        new UserRoleDaoUserRoleListService( userRoleDao, userDetailsService, tenantedUserNameUtils, systemRoles,
            extraRoles, adminRole );
    ( (UserRoleDaoUserRoleListService) userRoleListService ).setUserRoleDao( userRoleDao );
    ( (UserRoleDaoUserRoleListService) userRoleListService ).setUserDetailsService( userDetailsService );
    mp.defineInstance( IUserRoleListService.class, userRoleListService );
    mp.start();
    loginAsRepositoryAdmin();
    setAclManagement();
    logout();
    startupCalled = true;
  }

  @After
  public void afterTest() throws Exception {
    // null out fields to get back memory
    loginAsRepositoryAdmin();
    SimpleJcrTestUtils.deleteItem( testJcrTemplate, ServerRepositoryPaths.getPentahoRootFolderPath() );
    logout();

    repositoryAdminUsername = null;
    adminAuthorityName = null;
    tenantAuthenticatedAuthorityName = null;
    authorizationPolicy = null;
    testJcrTemplate = null;
    if ( startupCalled ) {
      manager.shutdown();
    }

    // null out fields to get back memory
    repo = null;
  }

  private void cleanupUserAndRoles( final ITenant tenant ) {
    loginAsRepositoryAdmin();
    for ( IPentahoRole role : userRoleDao.getRoles( tenant ) ) {
      try {
        userRoleDao.deleteRole( role );
      } catch ( Exception e ) {
        // System role deletion will throw Exception
      }
    }
    for ( IPentahoUser user : userRoleDao.getUsers( tenant ) ) {
      userRoleDao.deleteUser( user );
    }
  }

  @Test
  public void testDoNewTenant() throws Exception {
    loginAsRepositoryAdmin();
    ITenant systemTenant =
        tenantManager.createTenant( null, ServerRepositoryPaths.getPentahoRootFolderName(), adminAuthorityName,
            tenantAuthenticatedAuthorityName, "Anonymous" );
    userRoleDao.createUser( systemTenant, sysAdminUserName, "password", "", new String[]{adminAuthorityName} );
    ITenant mainTenant_1 =
        tenantManager.createTenant( systemTenant, MAIN_TENANT_1, adminAuthorityName, tenantAuthenticatedAuthorityName,
            "Anonymous" );
    userRoleDao.createUser( mainTenant_1, "admin", "password", "",
        new String[]{adminAuthorityName, tenantAuthenticatedAuthorityName} );
    login( "admin", mainTenant_1, new String[]{adminAuthorityName, tenantAuthenticatedAuthorityName} );
    JcrRepositoryDumpToFile dumpToFile =
        new JcrRepositoryDumpToFile( testJcrTemplate, jcrTransactionTemplate, repositoryAdminUsername,
            "c:/build/testrepo_3", Mode.CUSTOM );
    dumpToFile.execute();
    metadataRepositoryLifecycleManager.newTenant( mainTenant_1 );
    String metadataPath = ClientRepositoryPaths.getEtcFolderPath() + "/metadata";
    RepositoryFile metadataRepositoryPath = repo.getFile( metadataPath );
    assertTrue( metadataRepositoryPath.getPath() != null );

    // Nothing should change if we run it again
    metadataRepositoryLifecycleManager.newTenant( mainTenant_1 );
    metadataPath = ClientRepositoryPaths.getEtcFolderPath() + "/metadata";
    metadataRepositoryPath = repo.getFile( metadataPath );
    assertTrue( metadataRepositoryPath.getPath() != null );
    cleanupUserAndRoles( mainTenant_1 );
    cleanupUserAndRoles( systemTenant );
  }

  @Test
  public void testDoNewUser() throws Exception {
    // Nothing to test
    metadataRepositoryLifecycleManager.newUser( null, null );
  }

  @Test
  public void testDoShutdown() throws Exception {
    // Nothing to test
    metadataRepositoryLifecycleManager.shutdown();
  }

  @Test
  public void testDoStartup() throws Exception {
    // Nothing to test
    loginAsRepositoryAdmin();
    metadataRepositoryLifecycleManager.startup();
  }

  @Override
  public void setApplicationContext( final ApplicationContext applicationContext ) throws BeansException {
    manager = (IBackingRepositoryLifecycleManager) applicationContext.getBean( "backingRepositoryLifecycleManager" );
    SessionFactory jcrSessionFactory = (SessionFactory) applicationContext.getBean( "jcrSessionFactory" );
    testJcrTemplate = new JcrTemplate( jcrSessionFactory );
    testJcrTemplate.setAllowCreate( true );
    testJcrTemplate.setExposeNativeSession( true );
    repositoryAdminUsername = (String) applicationContext.getBean( "repositoryAdminUsername" );
    tenantAuthenticatedAuthorityName = (String) applicationContext.getBean( "singleTenantAuthenticatedAuthorityName" );
    adminAuthorityName = (String) applicationContext.getBean( "singleTenantAdminAuthorityName" );
    sysAdminAuthorityName = (String) applicationContext.getBean( "superAdminAuthorityName" );
    sysAdminUserName = (String) applicationContext.getBean( "superAdminUserName" );
    superAdminRoleName = (String) applicationContext.getBean( "superAdminAuthorityName" );
    authorizationPolicy = (IAuthorizationPolicy) applicationContext.getBean( "authorizationPolicy" );
    roleBindingDaoTarget =
        (IRoleAuthorizationPolicyRoleBindingDao) applicationContext
            .getBean( "roleAuthorizationPolicyRoleBindingDaoTarget" );
    tenantManager = (ITenantManager) applicationContext.getBean( "tenantMgrTxn" );
    repositoryFileDao = (IRepositoryFileDao) applicationContext.getBean( "repositoryFileDao" );
    userRoleDao = (IUserRoleDao) applicationContext.getBean( "userRoleDao" );
    tenantedUserNameUtils = (ITenantedPrincipleNameResolver) applicationContext.getBean( "tenantedUserNameUtils" );
    tenantedRoleNameUtils = (ITenantedPrincipleNameResolver) applicationContext.getBean( "tenantedRoleNameUtils" );
    metadataRepositoryLifecycleManager =
        (IBackingRepositoryLifecycleManager) applicationContext.getBean( "metadataRepositoryLifecycleManager" );
    jcrTransactionTemplate = (TransactionTemplate) applicationContext.getBean( "jcrTransactionTemplate" );
    repo = (IUnifiedRepository) applicationContext.getBean( "unifiedRepository" );
    TestPrincipalProvider.userRoleDao = userRoleDao;
    TestPrincipalProvider.adminCredentialsStrategy =
        (CredentialsStrategy) applicationContext.getBean( "jcrAdminCredentialsStrategy" );
    TestPrincipalProvider.repository = (Repository) applicationContext.getBean( "jcrRepository" );
    jcrTemplate = (JcrTemplate) applicationContext.getBean( "jcrTemplate" );
  }

  protected void loginAsRepositoryAdmin() {
    StandaloneSession pentahoSession = new StandaloneSession( repositoryAdminUsername );
    pentahoSession.setAuthenticated( repositoryAdminUsername );
    final GrantedAuthority[] repositoryAdminAuthorities =
        new GrantedAuthority[]{new GrantedAuthorityImpl( superAdminRoleName )};
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

  protected void login( final String username, final ITenant tenant ) {
    login( username, tenant, false );
  }

  /**
   * Logs in with given username.
   *
   * @param username username of user
   * @param tenantId tenant to which this user belongs
   * @tenantAdmin true to add the tenant admin authority to the user's roles
   */
  protected void login( final String username, final ITenant tenant, String[] roles ) {
    StandaloneSession pentahoSession = new StandaloneSession( username );
    pentahoSession.setAuthenticated( tenant.getId(), username );
    PentahoSessionHolder.setSession( pentahoSession );
    pentahoSession.setAttribute( IPentahoSession.TENANT_ID_KEY, tenant.getId() );
    final String password = "password";

    List<GrantedAuthority> authList = new ArrayList<GrantedAuthority>();

    for ( String roleName : roles ) {
      authList.add( new GrantedAuthorityImpl( roleName ) );
    }
    GrantedAuthority[] authorities = authList.toArray( new GrantedAuthority[0] );
    UserDetails userDetails = new User( username, password, true, true, true, true, authorities );
    Authentication auth = new UsernamePasswordAuthenticationToken( userDetails, password, authorities );
    PentahoSessionHolder.setSession( pentahoSession );
    // this line necessary for Spring Security's MethodSecurityInterceptor
    SecurityContextHolder.getContext().setAuthentication( auth );
  }

  /**
   * Logs in with given username.
   *
   * @param username username of user
   * @param tenantId tenant to which this user belongs
   * @tenantAdmin true to add the tenant admin authority to the user's roles
   */
  protected void login( final String username, final ITenant tenant, final boolean tenantAdmin ) {
    StandaloneSession pentahoSession = new StandaloneSession( username );
    pentahoSession.setAuthenticated( username );
    pentahoSession.setAttribute( IPentahoSession.TENANT_ID_KEY, tenant.getId() );
    final String password = "password";

    List<GrantedAuthority> authList = new ArrayList<GrantedAuthority>();
    authList.add( new GrantedAuthorityImpl( tenantAuthenticatedAuthorityName ) );
    if ( tenantAdmin ) {
      authList.add( new GrantedAuthorityImpl( adminAuthorityName ) );
    }
    GrantedAuthority[] authorities = authList.toArray( new GrantedAuthority[0] );
    UserDetails userDetails = new User( username, password, true, true, true, true, authorities );
    Authentication auth = new UsernamePasswordAuthenticationToken( userDetails, password, authorities );
    PentahoSessionHolder.setSession( pentahoSession );
    // this line necessary for Spring Security's MethodSecurityInterceptor
    SecurityContextHolder.getContext().setAuthentication( auth );
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

}
