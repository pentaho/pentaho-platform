/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.test.platform.web.http.api;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.ServletDeploymentContext;
import org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
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
import org.pentaho.platform.api.repository2.unified.IRepositoryVersionManager;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.core.mt.Tenant;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.boot.PlatformInitializationException;
import org.pentaho.platform.plugin.services.importer.NameBaseMimeResolver;
import org.pentaho.platform.repository2.ClientRepositoryPaths;
import org.pentaho.platform.repository2.mt.RepositoryTenantManager;
import org.pentaho.platform.repository2.unified.DefaultRepositoryVersionManager;
import org.pentaho.platform.repository2.unified.IRepositoryFileDao;
import org.pentaho.platform.repository2.unified.ServerRepositoryPaths;
import org.pentaho.platform.repository2.unified.jcr.RepositoryFileProxyFactory;
import org.pentaho.platform.repository2.unified.jcr.SimpleJcrTestUtils;
import org.pentaho.platform.repository2.unified.jcr.jackrabbit.security.TestPrincipalProvider;
import org.pentaho.platform.repository2.unified.jcr.sejcr.CredentialsStrategy;
import org.pentaho.platform.security.policy.rolebased.IRoleAuthorizationPolicyRoleBindingDao;
import org.pentaho.platform.security.policy.rolebased.RoleAuthorizationPolicy;
import org.pentaho.platform.security.userroledao.service.UserRoleDaoUserDetailsService;
import org.pentaho.platform.security.userroledao.service.UserRoleDaoUserRoleListService;
import org.pentaho.test.platform.engine.core.MicroPlatform;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.extensions.jcr.JcrTemplate;
import org.springframework.extensions.jcr.SessionFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.jcr.Repository;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static jakarta.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;
import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN;
import static org.junit.Assert.assertEquals;
import static org.pentaho.test.platform.web.http.api.JerseyTestUtil.assertResponse;

@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration( locations = { "classpath:/repository.spring.xml",
  "classpath:/repository-test-override.spring.xml" } )
@SuppressWarnings( "nls" )
public class DirectoryResourceIT extends JerseyTest implements ApplicationContextAware {

  private static MicroPlatform mp = new MicroPlatform();

  private static ResourceConfig config = new ResourceConfig().packages( "org.pentaho.platform.web.http.api.resources" );
  private static ServletDeploymentContext servletDeploymentContext =
    ServletDeploymentContext.forServlet( new ServletContainer( config ) )
      .contextPath( "api" )
      .build();

  public static final String MAIN_TENANT_1 = "maintenant1";

  private IUnifiedRepository repo;

  private IUserRoleListService userRoleListService;

  private boolean startupCalled;

  private String repositoryAdminUsername;

  private String adminAuthorityName;

  private String authenticatedAuthorityName;

  private JcrTemplate testJcrTemplate;

  private IBackingRepositoryLifecycleManager manager;

  private IPluginManager pluginManager;
  private IAuthorizationPolicy authorizationPolicy;

  IUserRoleDao userRoleDao;

  private ITenantManager tenantManager;
  private String sysAdminAuthorityName;
  private String sysAdminUserName;
  private IRepositoryFileDao repositoryFileDao;
  private ITenantedPrincipleNameResolver tenantedRoleNameUtils;
  private ITenantedPrincipleNameResolver tenantedUserNameUtils;
  private IRoleAuthorizationPolicyRoleBindingDao roleBindingDaoTarget;
  public static final String SYSTEM_PROPERTY = "spring.security.strategy";

  public DirectoryResourceIT() throws Exception {
    super();
    mp.setFullyQualifiedServerUrl( getBaseUri() + servletDeploymentContext.getContextPath() + "/" );
  }

  protected DeploymentContext configureDeployment() {
    return servletDeploymentContext;
  }

  protected TestContainerFactory getTestContainerFactory() {
    return new GrizzlyWebTestContainerFactory();
  }

  @BeforeClass
  public static void beforeClass() throws Exception {
    System.setProperty( SYSTEM_PROPERTY, "MODE_GLOBAL" );
    PentahoSessionHolder.setStrategyName( PentahoSessionHolder.MODE_GLOBAL );

    FileUtils.deleteDirectory( new File( "/tmp/jackrabbit-test-TRUNK" ) );
    SecurityContextHolder.setStrategyName( SecurityContextHolder.MODE_GLOBAL );
  }

  @AfterClass
  public static void afterClass() {
    PentahoSessionHolder.setStrategyName( PentahoSessionHolder.MODE_GLOBAL );
    SecurityContextHolder.setStrategyName( SecurityContextHolder.MODE_GLOBAL );
  }

  private void cleanupUserAndRoles( final ITenant tenant ) {
    loginAsRepositoryAdmin();
    for ( IPentahoRole role : userRoleDao.getRoles( tenant ) ) {
      userRoleDao.deleteRole( role );
    }
    for ( IPentahoUser user : userRoleDao.getUsers( tenant ) ) {
      userRoleDao.deleteUser( user );
    }
  }

  @Before
  public void beforeTest() throws PlatformInitializationException {
    mp = new MicroPlatform();
    // used by DefaultPentahoJackrabbitAccessControlHelper
    mp.defineInstance( IPluginManager.class, pluginManager );
    mp.defineInstance( IAuthorizationPolicy.class, authorizationPolicy );
    mp.defineInstance( ITenantManager.class, tenantManager );
    mp.define( ITenant.class, Tenant.class );
    mp.defineInstance( "roleAuthorizationPolicyRoleBindingDaoTarget", roleBindingDaoTarget );
    mp.defineInstance( IRoleAuthorizationPolicyRoleBindingDao.class, roleBindingDaoTarget );
    mp.defineInstance( "tenantedUserNameUtils", tenantedUserNameUtils );
    mp.defineInstance( "tenantedRoleNameUtils", tenantedRoleNameUtils );
    mp.defineInstance( "repositoryAdminUsername", repositoryAdminUsername );
    mp.define( IRoleAuthorizationPolicyRoleBindingDao.class, RoleAuthorizationPolicy.class, Scope.GLOBAL );
    mp.define( ITenantManager.class, RepositoryTenantManager.class, Scope.GLOBAL );
    mp.defineInstance( "singleTenantAdminAuthorityName", new String( "Administrator" ) );
    mp.defineInstance( "RepositoryFileProxyFactory",
      new RepositoryFileProxyFactory( this.testJcrTemplate, this.repositoryFileDao ) );

    DefaultRepositoryVersionManager defaultRepositoryVersionManager = new DefaultRepositoryVersionManager();
    defaultRepositoryVersionManager.setPlatformMimeResolver( new NameBaseMimeResolver() );
    mp.defineInstance( IRepositoryVersionManager.class, defaultRepositoryVersionManager );
    UserRoleDaoUserDetailsService userDetailsService = new UserRoleDaoUserDetailsService();
    userDetailsService.setUserRoleDao( userRoleDao );
    List<String> systemRoles = new ArrayList<String>();
    systemRoles.add( "Admin" );
    List<String> extraRoles = Arrays.asList( new String[] { "Authenticated", "Anonymous" } );
    String adminRole = "Admin";

    userRoleListService =
      new UserRoleDaoUserRoleListService( userRoleDao, userDetailsService, tenantedUserNameUtils, systemRoles,
        extraRoles, adminRole );
    ( (UserRoleDaoUserRoleListService) userRoleListService ).setUserRoleDao( userRoleDao );
    ( (UserRoleDaoUserRoleListService) userRoleListService ).setUserDetailsService( userDetailsService );

    mp.defineInstance( IUserRoleListService.class, userRoleListService );
    mp.start();
    logout();
    startupCalled = true;
    SecurityContextHolder.setStrategyName( SecurityContextHolder.MODE_GLOBAL );
  }

  @After
  public void afterTest() throws Exception {
    clearRoleBindings();
    // null out fields to get back memory
    authorizationPolicy = null;
    loginAsRepositoryAdmin();
    SimpleJcrTestUtils.deleteItem( testJcrTemplate, ServerRepositoryPaths.getPentahoRootFolderPath() );
    logout();
    repositoryAdminUsername = null;
    adminAuthorityName = null;
    authenticatedAuthorityName = null;
    pluginManager = null;
    authorizationPolicy = null;
    testJcrTemplate = null;
    if ( startupCalled ) {
      manager.shutdown();
    }
    mp.stop();
    // null out fields to get back memory
    repo = null;
  }

  protected void clearRoleBindings() throws Exception {
    loginAsRepositoryAdmin();
  }

  protected void createTestFile( String pathId, String text ) {
    WebTarget webTarget = target();
    Response response =
      webTarget.path( "repo/files/" + pathId ).request( TEXT_PLAIN ).put( Entity.entity( text, TEXT_PLAIN ) );
    assertResponse( response, Status.OK );
  }

  protected void createTestFileBinary( String pathId, byte[] data ) {
    WebTarget webTarget = target();
    Response response =
      webTarget.path( "repo/files/" + pathId ).request( APPLICATION_OCTET_STREAM )
        .put( Entity.entity( data, APPLICATION_OCTET_STREAM ) );
    assertResponse( response, Status.OK );
  }

  protected void createTestFolder( String pathId ) {
    WebTarget webTarget = target();
    // webResource.path("repo/dirs/" + pathId).put();
    Response response =
      webTarget.path( "repo/dirs/" + pathId ).request( TEXT_PLAIN ).put( Entity.entity( "", TEXT_PLAIN ) );
    assertResponse( response, Status.OK );
  }

  public void setApplicationContext( final ApplicationContext applicationContext ) throws BeansException {
    manager = (IBackingRepositoryLifecycleManager) applicationContext.getBean( "backingRepositoryLifecycleManager" );
    SessionFactory jcrSessionFactory = (SessionFactory) applicationContext.getBean( "jcrSessionFactory" );
    testJcrTemplate = new JcrTemplate( jcrSessionFactory );
    testJcrTemplate.setAllowCreate( true );
    testJcrTemplate.setExposeNativeSession( true );
    repositoryAdminUsername = (String) applicationContext.getBean( "repositoryAdminUsername" );
    authenticatedAuthorityName = (String) applicationContext.getBean( "singleTenantAuthenticatedAuthorityName" );
    adminAuthorityName = (String) applicationContext.getBean( "singleTenantAdminAuthorityName" );
    sysAdminAuthorityName = (String) applicationContext.getBean( "superAdminAuthorityName" );
    sysAdminUserName = (String) applicationContext.getBean( "superAdminUserName" );
    pluginManager = (IPluginManager) applicationContext.getBean( "IPluginManager" );
    authorizationPolicy = (IAuthorizationPolicy) applicationContext.getBean( "authorizationPolicy" );
    roleBindingDaoTarget =
      (IRoleAuthorizationPolicyRoleBindingDao) applicationContext
        .getBean( "roleAuthorizationPolicyRoleBindingDaoTarget" );
    tenantManager = (ITenantManager) applicationContext.getBean( "tenantMgrTxn" );
    repositoryFileDao = (IRepositoryFileDao) applicationContext.getBean( "repositoryFileDao" );
    userRoleDao = (IUserRoleDao) applicationContext.getBean( "userRoleDaoTxn" );
    tenantedUserNameUtils = (ITenantedPrincipleNameResolver) applicationContext.getBean( "tenantedUserNameUtils" );
    tenantedRoleNameUtils = (ITenantedPrincipleNameResolver) applicationContext.getBean( "tenantedRoleNameUtils" );
    repo = (IUnifiedRepository) applicationContext.getBean( "unifiedRepository" );
    TestPrincipalProvider.userRoleDao = (IUserRoleDao) applicationContext.getBean( "userRoleDaoTxn" );
    TestPrincipalProvider.adminCredentialsStrategy =
      (CredentialsStrategy) applicationContext.getBean( "jcrAdminCredentialsStrategy" );
    TestPrincipalProvider.repository = (Repository) applicationContext.getBean( "jcrRepository" );
  }

  protected void loginAsRepositoryAdmin() {
    StandaloneSession pentahoSession = new StandaloneSession( repositoryAdminUsername );
    pentahoSession.setAuthenticated( repositoryAdminUsername );
    final List<GrantedAuthority> repositoryAdminAuthorities =
      Arrays.asList( new GrantedAuthority[] { new SimpleGrantedAuthority( sysAdminAuthorityName ) } );
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
   * @param tenant   tenant to which this user belongs
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
      authList.add( new SimpleGrantedAuthority( roleName ) );
    }
    UserDetails userDetails = new User( username, password, true, true, true, true, authList );
    Authentication auth = new UsernamePasswordAuthenticationToken( userDetails, password, authList );
    PentahoSessionHolder.setSession( pentahoSession );
    // this line necessary for Spring Security's MethodSecurityInterceptor
    SecurityContextHolder.getContext().setAuthentication( auth );
  }

  /**
   * Logs in with given username.
   *
   * @param username username of user
   * @param tenant   tenant to which this user belongs
   * @tenantAdmin true to add the tenant admin authority to the user's roles
   */
  protected void login( final String username, final ITenant tenant, final boolean tenantAdmin ) {
    StandaloneSession pentahoSession = new StandaloneSession( username );
    pentahoSession.setAuthenticated( username );
    pentahoSession.setAttribute( IPentahoSession.TENANT_ID_KEY, tenant.getId() );
    final String password = "password";

    List<GrantedAuthority> authList = new ArrayList<GrantedAuthority>();
    authList.add( new SimpleGrantedAuthority( authenticatedAuthorityName ) );
    if ( tenantAdmin ) {
      authList.add( new SimpleGrantedAuthority( adminAuthorityName ) );
    }
    UserDetails userDetails = new User( username, password, true, true, true, true, authList );
    Authentication auth = new UsernamePasswordAuthenticationToken( userDetails, password, authList );
    PentahoSessionHolder.setSession( pentahoSession );
    // this line necessary for Spring Security's MethodSecurityInterceptor
    SecurityContextHolder.getContext().setAuthentication( auth );
  }

  @Test
  public void testCreateDir() {
    loginAsRepositoryAdmin();
    ITenant systemTenant =
      tenantManager.createTenant( null, ServerRepositoryPaths.getPentahoRootFolderName(), adminAuthorityName,
        authenticatedAuthorityName, "Anonymous" );
    userRoleDao.createUser( systemTenant, sysAdminUserName, "password", "", new String[] { adminAuthorityName } );
    ITenant mainTenant_1 =
      tenantManager.createTenant( systemTenant, MAIN_TENANT_1, adminAuthorityName, authenticatedAuthorityName,
        "Anonymous" );
    userRoleDao.createUser( mainTenant_1, "admin", "password", "", new String[] { adminAuthorityName } );
    try {
      login( "admin", mainTenant_1, new String[] { authenticatedAuthorityName } );

      // set object in PentahoSystem
      mp.defineInstance( IUnifiedRepository.class, repo );

      WebTarget webTarget = target();
      String publicFolderPath = ClientRepositoryPaths.getPublicFolderPath();
      String newDirPathId = publicFolderPath.replaceAll( "/", ":" ) + ":testDir";

      // Create directory. Success is expected.
      webTarget.path( "repo/dirs/" + newDirPathId ).request().put( Entity.entity( "", TEXT_PLAIN ) );

      // Create duplicate directory. CONFLICT (409) is expected.
      try {
        webTarget.path( "repo/dirs/" + newDirPathId ).request().put( Entity.entity( "", TEXT_PLAIN ) );
      } catch ( WebApplicationException e ) {
        assertEquals( Response.Status.CONFLICT.getStatusCode(), e.getResponse().getStatus() );
      }
    } catch ( AssertionError assertion ) {
      throw assertion;
    } catch ( Throwable ex ) {
      TestCase.fail( ex.getMessage() );
    } finally {
      cleanupUserAndRoles( mainTenant_1 );
      cleanupUserAndRoles( systemTenant );
      logout();
    }
  }

  @Test
  public void testCreateDir_RootLevel() {
    loginAsRepositoryAdmin();
    ITenant systemTenant =
      tenantManager.createTenant( null, ServerRepositoryPaths.getPentahoRootFolderName(), adminAuthorityName,
        authenticatedAuthorityName, "Anonymous" );
    userRoleDao.createUser( systemTenant, sysAdminUserName, "password", "", new String[] { adminAuthorityName } );
    ITenant mainTenant_1 =
      tenantManager.createTenant( systemTenant, MAIN_TENANT_1, adminAuthorityName, authenticatedAuthorityName,
        "Anonymous" );
    userRoleDao.createUser( mainTenant_1, "admin", "password", "", new String[] { adminAuthorityName } );
    try {
      login( "admin", mainTenant_1, new String[] { authenticatedAuthorityName } );

      // set object in PentahoSystem
      mp.defineInstance( IUnifiedRepository.class, repo );

      WebTarget webTarget = target();
      String rootLevelDirPathId = ":testRootLevelDir";

      // Create duplicate directory. FORBIDDEN (403) is expected.
      try {
        webTarget.path( "repo/dirs/" + rootLevelDirPathId ).request().put( Entity.entity( "", TEXT_PLAIN ) );
      } catch ( WebApplicationException e ) {
        assertEquals( Response.Status.FORBIDDEN.getStatusCode(), e.getResponse().getStatus() );
      }
    } catch ( AssertionError assertion ) {
      throw assertion;
    } catch ( Throwable ex ) {
      TestCase.fail( ex.getMessage() );
    } finally {
      cleanupUserAndRoles( mainTenant_1 );
      cleanupUserAndRoles( systemTenant );
      logout();
    }
  }

  @Test
  public void testCreateDir_ServerError() {
    loginAsRepositoryAdmin();
    ITenant systemTenant =
      tenantManager.createTenant( null, ServerRepositoryPaths.getPentahoRootFolderName(), adminAuthorityName,
        authenticatedAuthorityName, "Anonymous" );
    userRoleDao.createUser( systemTenant, sysAdminUserName, "password", "", new String[] { adminAuthorityName } );
    ITenant mainTenant_1 =
      tenantManager.createTenant( systemTenant, MAIN_TENANT_1, adminAuthorityName, authenticatedAuthorityName,
        "Anonymous" );
    userRoleDao.createUser( mainTenant_1, "admin", "password", "", new String[] { adminAuthorityName } );
    try {
      login( "admin", mainTenant_1, new String[] { authenticatedAuthorityName } );

      // set object in PentahoSystem
      mp.defineInstance( IUnifiedRepository.class, repo );

      WebTarget webTarget = target();
      String invalidPathId = "/////";

      // Invalid path id. INTERNAL_SERVER_ERROR (500) is expected.
      try {
        webTarget.path( "repo/dirs/" + invalidPathId ).request().put( Entity.entity( "", TEXT_PLAIN ) );
      } catch ( WebApplicationException e ) {
        assertEquals( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), e.getResponse().getStatus() );
      }
    } catch ( AssertionError assertion ) {
      throw assertion;
    } catch ( Throwable ex ) {
      TestCase.fail( ex.getMessage() );
    } finally {
      cleanupUserAndRoles( mainTenant_1 );
      cleanupUserAndRoles( systemTenant );
      logout();
    }
  }
}
