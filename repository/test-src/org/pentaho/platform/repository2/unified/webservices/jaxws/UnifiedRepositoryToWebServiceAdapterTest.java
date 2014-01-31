/*!
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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.repository2.unified.webservices.jaxws;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
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
import org.pentaho.platform.api.engine.security.userroledao.IPentahoRole;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoUser;
import org.pentaho.platform.api.engine.security.userroledao.IUserRoleDao;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.mt.ITenantManager;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.pentaho.platform.api.repository2.unified.IBackingRepositoryLifecycleManager;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.repository2.ClientRepositoryPaths;
import org.pentaho.platform.repository2.unified.IRepositoryFileDao;
import org.pentaho.platform.repository2.unified.ServerRepositoryPaths;
import org.pentaho.platform.repository2.unified.jcr.IPathConversionHelper;
import org.pentaho.platform.repository2.unified.jcr.PentahoJcrConstants;
import org.pentaho.platform.repository2.unified.jcr.RepositoryFileProxyFactory;
import org.pentaho.platform.repository2.unified.jcr.SimpleJcrTestUtils;
import org.pentaho.platform.repository2.unified.jcr.jackrabbit.security.TestPrincipalProvider;
import org.pentaho.platform.repository2.unified.jcr.sejcr.CredentialsStrategy;
import org.pentaho.platform.security.policy.rolebased.IRoleAuthorizationPolicyRoleBindingDao;
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

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.jcr.security.AccessControlException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Class Description
 * 
 * @author <a href="mailto:dkincade@pentaho.com">David M. Kincade</a>
 */
@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration( locations = { "classpath:/repository.spring.xml",
    "classpath:/repository-test-override.spring.xml" } )
@SuppressWarnings( "nls" )
public class UnifiedRepositoryToWebServiceAdapterTest implements ApplicationContextAware {
  private UnifiedRepositoryToWebServiceAdapter adapter;
  private String tenantAdminAuthorityName;
  private String tenantAuthenticatedAuthorityName;
  private String sysAdminAuthorityName;
  private IUnifiedRepository repository;
  private IUnifiedRepositoryJaxwsWebService repositoryWS;
  private ITenantManager tenantManager;
  private ITenantManager tenantMgrTxn;
  private String repositoryAdminUsername;
  private JcrTemplate testJcrTemplate;
  private IRoleAuthorizationPolicyRoleBindingDao roleBindingDao;
  private IRoleAuthorizationPolicyRoleBindingDao roleBindingDaoTarget;
  private String sysAdminRoleName;
  private String tenantAdminRoleName;
  private String tenantAuthenticatedRoleName;
  private String sysAdminUserName;
  private IPathConversionHelper pathConversionHelper;
  IUserRoleDao userRoleDao;
  private IBackingRepositoryLifecycleManager manager;
  private IAuthorizationPolicy authorizationPolicy;
  private MicroPlatform mp;
  private IRepositoryFileDao repositoryFileDao;
  private IUnifiedRepository repo;
  private ITenantedPrincipleNameResolver tenantedUserNameUtils;
  private ITenantedPrincipleNameResolver tenantedRoleNameUtils;
  private IRoleAuthorizationPolicyRoleBindingDao roleAuthorizationPolicyRoleBindingDao;
  private static TransactionTemplate jcrTransactionTemplate;
  private boolean startupCalled;
  public static final String MAIN_TENANT_1 = "maintenant1";
  public ITenant systemTenant;

  @BeforeClass
  public static void setUpClass() throws Exception {
    // folder cannot be deleted at teardown shutdown hooks have not yet necessarily completed
    // parent folder must match jcrRepository.homeDir bean property in repository-test-override.spring.xml
    FileUtils.deleteDirectory( new File( "/tmp/jackrabbit-test-TRUNK" ) );
    PentahoSessionHolder.setStrategyName( PentahoSessionHolder.MODE_GLOBAL );
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
    PentahoSessionHolder.setStrategyName( PentahoSessionHolder.MODE_INHERITABLETHREADLOCAL );
  }

  @Before
  public void setUp() throws Exception {
    repositoryWS = new DefaultUnifiedRepositoryJaxwsWebService( repository );
    adapter = new UnifiedRepositoryToWebServiceAdapter( repositoryWS );
    loginAsRepositoryAdmin();
    SimpleJcrTestUtils.deleteItem( testJcrTemplate, ServerRepositoryPaths.getPentahoRootFolderPath() );
    mp = new MicroPlatform();
    // used by DefaultPentahoJackrabbitAccessControlHelper
    mp.defineInstance( "tenantedUserNameUtils", tenantedUserNameUtils );
    mp.defineInstance( "tenantedRoleNameUtils", tenantedRoleNameUtils );
    mp.defineInstance( IAuthorizationPolicy.class, authorizationPolicy );
    mp.defineInstance( ITenantManager.class, tenantManager );
    mp.defineInstance( "roleAuthorizationPolicyRoleBindingDaoTarget", roleBindingDaoTarget );
    mp.defineInstance( "repositoryAdminUsername", repositoryAdminUsername );
    mp.defineInstance( "RepositoryFileProxyFactory", new RepositoryFileProxyFactory(testJcrTemplate, repositoryFileDao) );
    // Start the micro-platform
    mp.start();
    setAclManagement();
    systemTenant =
        tenantMgrTxn.createTenant( null, ServerRepositoryPaths.getPentahoRootFolderName(), tenantAdminAuthorityName,
            tenantAuthenticatedAuthorityName, "Anonymous" );
    userRoleDao.createUser( systemTenant, sysAdminUserName, "password", "", new String[] { tenantAdminAuthorityName } );
    logout();

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

  private void cleanupUserAndRoles( String userName, ITenant systemTenant, ITenant tenant ) {
    login( userName, systemTenant, new String[] { tenantAdminAuthorityName, tenantAuthenticatedAuthorityName } );
    for ( IPentahoRole role : userRoleDao.getRoles( tenant ) ) {
      userRoleDao.deleteRole( role );
    }
    for ( IPentahoUser user : userRoleDao.getUsers( tenant ) ) {
      userRoleDao.deleteUser( user );
    }
    logout();
  }

  /*
   * private void deleteUserRoleAndTenant(ITenant parentTenant, List<ITenant> tenants) { try { if(tenants != null
   * && tenants.size() > 0) { for(ITenant tenant: tenants) { login("admin", tenant, true); for(IPentahoRole
   * role:userRoleDao.getRoles()) { userRoleDao.deleteRole(role); } for(IPentahoUser user:userRoleDao.getUsers()) {
   * userRoleDao.deleteUser(user); } deleteUserRoleAndTenant(tenant, tenantManager.getChildTenants(tenant));
   * logout(); } } else { tenantManager.deleteTenant(parentTenant); } } catch (Throwable e) { // TODO
   * Auto-generated catch block e.printStackTrace(); } //$NON-NLS-1$ //$NON-NLS-2$ }
   */
  @After
  public void tearDown() throws Exception {
    // Deleting all user and roles and tenant
    // null out fields to get back memory
    authorizationPolicy = null;
    loginAsRepositoryAdmin();
    SimpleJcrTestUtils.deleteItem( testJcrTemplate, ServerRepositoryPaths.getPentahoRootFolderPath() );
    logout();
    repositoryAdminUsername = null;
    sysAdminAuthorityName = null;
    sysAdminUserName = null;
    tenantAdminAuthorityName = null;
    tenantAuthenticatedAuthorityName = null;
    authorizationPolicy = null;
    testJcrTemplate = null;
    if ( startupCalled ) {
      manager.shutdown();
    }

    // null out fields to get back memory
    repo = null;
    tenantManager = null;

  }

  protected void loginAsRepositoryAdmin() {
    StandaloneSession pentahoSession = new StandaloneSession( repositoryAdminUsername );
    pentahoSession.setAuthenticated( repositoryAdminUsername );
    final GrantedAuthority[] repositoryAdminAuthorities =
        new GrantedAuthority[] { new GrantedAuthorityImpl( sysAdminAuthorityName ) };
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

  /**
   * Logs in with given username.
   * 
   * @param username
   *          username of user
   * @param tenantId
   *          tenant to which this user belongs
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

  @Override
  public void setApplicationContext( final ApplicationContext applicationContext ) throws BeansException {
    manager = (IBackingRepositoryLifecycleManager) applicationContext.getBean( "backingRepositoryLifecycleManager" );
    SessionFactory jcrSessionFactory = (SessionFactory) applicationContext.getBean( "jcrSessionFactory" );
    testJcrTemplate = new JcrTemplate( jcrSessionFactory );
    testJcrTemplate.setAllowCreate( true );
    testJcrTemplate.setExposeNativeSession( true );
    repositoryAdminUsername = (String) applicationContext.getBean( "repositoryAdminUsername" );
    tenantAuthenticatedAuthorityName = (String) applicationContext.getBean( "singleTenantAuthenticatedAuthorityName" );
    tenantAdminAuthorityName = (String) applicationContext.getBean( "singleTenantAdminAuthorityName" );
    sysAdminAuthorityName = (String) applicationContext.getBean( "superAdminAuthorityName" );
    sysAdminUserName = (String) applicationContext.getBean( "superAdminUserName" );
    authorizationPolicy = (IAuthorizationPolicy) applicationContext.getBean( "authorizationPolicy" );
    roleBindingDaoTarget =
        (IRoleAuthorizationPolicyRoleBindingDao) applicationContext
            .getBean( "roleAuthorizationPolicyRoleBindingDaoTarget" );
    tenantManager = (ITenantManager) applicationContext.getBean( "tenantMgrProxy" );
    tenantMgrTxn = (ITenantManager) applicationContext.getBean( "tenantMgrTxn" );

    userRoleDao = (IUserRoleDao) applicationContext.getBean( "userRoleDao" );
    pathConversionHelper = (IPathConversionHelper) applicationContext.getBean( "pathConversionHelper" );
    roleBindingDao =
        (IRoleAuthorizationPolicyRoleBindingDao) applicationContext.getBean( "roleAuthorizationPolicyRoleBindingDao" );
    roleBindingDaoTarget =
        (IRoleAuthorizationPolicyRoleBindingDao) applicationContext
            .getBean( "roleAuthorizationPolicyRoleBindingDaoTarget" );
    authorizationPolicy = (IAuthorizationPolicy) applicationContext.getBean( "authorizationPolicy" );
    repository = (IUnifiedRepository) applicationContext.getBean( "unifiedRepository" );
    tenantedUserNameUtils = (ITenantedPrincipleNameResolver) applicationContext.getBean( "tenantedUserNameUtils" );
    tenantedRoleNameUtils = (ITenantedPrincipleNameResolver) applicationContext.getBean( "tenantedRoleNameUtils" );
    jcrTransactionTemplate = (TransactionTemplate) applicationContext.getBean( "jcrTransactionTemplate" );
    TestPrincipalProvider.userRoleDao = (IUserRoleDao) applicationContext.getBean( "userRoleDao" );
    TestPrincipalProvider.adminCredentialsStrategy =
        (CredentialsStrategy) applicationContext.getBean( "jcrAdminCredentialsStrategy" );
    TestPrincipalProvider.repository = (Repository) applicationContext.getBean( "jcrRepository" );
  }

  @Test
  public void testFileMetadata() throws Exception {
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminAuthorityName,
      tenantAuthenticatedAuthorityName } );
    ITenant mainTenant_1 =
        tenantManager.createTenant( systemTenant, MAIN_TENANT_1, tenantAdminAuthorityName,
            tenantAuthenticatedAuthorityName, "Anonymous" );
    userRoleDao.createUser( mainTenant_1, "admin", "password", "", new String[] { tenantAdminAuthorityName } );
    login( "admin", mainTenant_1, new String[] { tenantAuthenticatedAuthorityName } );
    RepositoryFile file = repository.getFile( ClientRepositoryPaths.getPublicFolderPath() );
    final RepositoryFile testfile =
        repository.createFile( file.getId(), new RepositoryFile.Builder( "testfile" ).build(),
            new SimpleRepositoryFileData( new ByteArrayInputStream( "test".getBytes() ), "UTF-8",
              "text/plain" ), null );
    //CHECKSTYLE IGNORE AvoidNestedBlocks FOR NEXT 3 LINES
    {
      // Make sure the repository is setup correctly
      assertNotNull( testfile );
      assertNotNull( testfile.getId() );
      final Map<String, Serializable> fileMetadata = repository.getFileMetadata( testfile.getId() );
      assertNotNull( fileMetadata );
      assertEquals( 0, fileMetadata.size() );
    }

    final Map<String, Serializable> metadata = new HashMap<String, Serializable>();
    metadata.put( "sample key", "sample value" );
    metadata.put( "complex key?", "\"an even more 'complex' value\"! {and them some}" );

    adapter.setFileMetadata( testfile.getId(), metadata );
    //CHECKSTYLE IGNORE AvoidNestedBlocks FOR NEXT 3 LINES
    {
      // Make sure the repository sees the metadata
      assertNotNull( testfile );
      assertNotNull( testfile.getId() );
      final Map<String, Serializable> fileMetadata = repository.getFileMetadata( testfile.getId() );
      assertNotNull( fileMetadata );
      assertEquals( 2, fileMetadata.size() );
    }
    //CHECKSTYLE IGNORE AvoidNestedBlocks FOR NEXT 3 LINES
    {
      // Make sure we can get the same metadata back via the web service
      final Map<String, Serializable> fileMetadata = adapter.getFileMetadata( testfile.getId() );
      assertNotNull( fileMetadata );
      assertEquals( 2, fileMetadata.size() );
      assertTrue( StringUtils.equals( "sample value", (String) fileMetadata.get( "sample key" ) ) );
      assertTrue( StringUtils.equals( "\"an even more 'complex' value\"! {and them some}", (String) fileMetadata
          .get( "complex key?" ) ) );
    }

    cleanupUserAndRoles( sysAdminUserName, systemTenant, mainTenant_1 );
    cleanupUserAndRoles( sysAdminUserName, systemTenant, systemTenant );

  }

}
