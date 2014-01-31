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

package org.pentaho.platform.repository2.unified.webservices.jaxws;

import com.sun.xml.ws.developer.JAXWSProperties;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.jackrabbit.api.JackrabbitWorkspace;
import org.apache.jackrabbit.api.security.authorization.PrivilegeManager;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
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
import org.pentaho.platform.api.repository2.unified.RepositoryFileAce;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid;
import org.pentaho.platform.api.repository2.unified.RepositoryFileTree;
import org.pentaho.platform.api.repository2.unified.VersionSummary;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode.DataPropertyType;
import org.pentaho.platform.api.repository2.unified.data.node.NodeRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.core.mt.Tenant;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.repository2.ClientRepositoryPaths;
import org.pentaho.platform.repository2.unified.IRepositoryFileDao;
import org.pentaho.platform.repository2.unified.ServerRepositoryPaths;
import org.pentaho.platform.repository2.unified.jcr.IPathConversionHelper;
import org.pentaho.platform.repository2.unified.jcr.JcrRepositoryDumpToFile;
import org.pentaho.platform.repository2.unified.jcr.RepositoryFileProxyFactory;
import org.pentaho.platform.repository2.unified.jcr.JcrRepositoryDumpToFile.Mode;
import org.pentaho.platform.repository2.unified.jcr.PentahoJcrConstants;
import org.pentaho.platform.repository2.unified.jcr.SimpleJcrTestUtils;
import org.pentaho.platform.repository2.unified.jcr.jackrabbit.security.TestPrincipalProvider;
import org.pentaho.platform.repository2.unified.jcr.sejcr.CredentialsStrategy;
import org.pentaho.platform.repository2.unified.lifecycle.DefaultBackingRepositoryLifecycleManager;
import org.pentaho.platform.security.policy.rolebased.IRoleAuthorizationPolicyRoleBindingDao;
import org.pentaho.platform.security.userroledao.DefaultTenantedPrincipleNameResolver;
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
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPBinding;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests marshalling, unmarshalling, and {@code UnifiedRepositoryToWebServiceAdapter}. Do not test unified
 * repository logic in this class; just make sure args serialize back and forth correctly and that the adapter is
 * translating to the right calls.
 * 
 * @author mlowery
 */

@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration( locations = { "classpath:/repository.spring.xml",
    "classpath:/repository-test-override.spring.xml" } )
@SuppressWarnings( "nls" )
public class DefaultUnifiedRepositoryJaxwsWebServiceTest implements ApplicationContextAware {
  // ~ Static fields/initializers
  // ======================================================================================

  // ~ Instance fields
  // =================================================================================================

  private static final String USERNAME_SUZY = "suzy";

  /**
   * Server-side IUnifiedRepository.
   */
  private IUnifiedRepository _repo;

  /**
   * Client-side IUnifiedRepository.
   */
  private IUnifiedRepository repo;

  private IAuthorizationPolicy authorizationPolicy;

  private MicroPlatform mp;

  private String sysAdminAuthorityName;

  private JcrTemplate testJcrTemplate;

  private IBackingRepositoryLifecycleManager manager;

  private String tenantAuthenticatedAuthorityName;

  private String tenantAdminAuthorityName;

  private String repositoryAdminUsername;

  private ITenantedPrincipleNameResolver userNameUtils = new DefaultTenantedPrincipleNameResolver();

  private static final String ENC = "UTF-8";

  private static final String MIME_PLAIN = "text/plain";

  private ITenantManager tenantManager;
  private JcrTemplate adminTemplate;
  private IRoleAuthorizationPolicyRoleBindingDao roleBindingDao;
  private IRoleAuthorizationPolicyRoleBindingDao roleBindingDaoTarget;
  private ITenantedPrincipleNameResolver roleNameUtils = new DefaultTenantedPrincipleNameResolver(
      DefaultTenantedPrincipleNameResolver.ALTERNATE_DELIMETER );
  private String sysAdminRoleName;
  private String tenantAdminRoleName;
  private String tenantAuthenticatedRoleName;
  private String sysAdminUserName;
  private ITenant systemTenant;
  private IPathConversionHelper pathConversionHelper;
  IUserRoleDao userRoleDao;
  private static TransactionTemplate jcrTransactionTemplate;
  private ITenantedPrincipleNameResolver tenantedUserNameUtils;
  private boolean startupCalled;
  private final String USERNAME_TIFFANY = "tiffany";

  private final String USERNAME_PAT = "pat";

  private final String USERNAME_ADMIN = "admin";

  private final String USERNAME_GEORGE = "george";

  private final String TENANT_ID_ACME = "acme";

  private final String TENANT_ID_DUFF = "duff";
  public static final String SYSTEM_PROPERTY = "spring.security.strategy";
  DefaultBackingRepositoryLifecycleManager defaultBackingRepositoryLifecycleManager;
  private IRepositoryFileDao repositoryFileDao;
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

  // ~ Constructors
  // ====================================================================================================

  public DefaultUnifiedRepositoryJaxwsWebServiceTest() throws Exception {
    super();
  }

  @Before
  public void setUp() throws Exception {
    System.setProperty( SYSTEM_PROPERTY, "MODE_GLOBAL" );

    String address = "http://localhost:9000/repo";
    Endpoint.publish( address, new DefaultUnifiedRepositoryJaxwsWebService( repo ) );

    Service service =
        Service.create( new URL( "http://localhost:9000/repo?wsdl" ), new QName( "http://www.pentaho.org/ws/1.0",
            "unifiedRepository" ) );

    IUnifiedRepositoryJaxwsWebService repoWebService = service.getPort( IUnifiedRepositoryJaxwsWebService.class );

    // next two lines not needed since we manually populate filters
    // ((BindingProvider) repoWebService).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, "suzy");
    // ((BindingProvider) repoWebService).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, "password");
    // accept cookies to maintain session on server
    ( (BindingProvider) repoWebService ).getRequestContext().put( BindingProvider.SESSION_MAINTAIN_PROPERTY, true );
    // support streaming binary data
    ( (BindingProvider) repoWebService ).getRequestContext().put( JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE,
        8192 );
    SOAPBinding binding = (SOAPBinding) ( (BindingProvider) repoWebService ).getBinding();
    binding.setMTOMEnabled( true );

    repo = new UnifiedRepositoryToWebServiceAdapter( repoWebService );

    mp = new MicroPlatform();
    // used by DefaultPentahoJackrabbitAccessControlHelper
    mp.defineInstance( "tenantedUserNameUtils", userNameUtils );
    mp.defineInstance( "tenantedRoleNameUtils", roleNameUtils );
    mp.defineInstance( IAuthorizationPolicy.class, authorizationPolicy );
    mp.defineInstance( ITenantManager.class, tenantManager );
    mp.defineInstance( "roleAuthorizationPolicyRoleBindingDaoTarget", roleBindingDaoTarget );
    mp.defineInstance( "repositoryAdminUsername", repositoryAdminUsername );
    mp.defineInstance( "RepositoryFileProxyFactory", new RepositoryFileProxyFactory(testJcrTemplate, repositoryFileDao) );
    // Start the micro-platform
    mp.start();
    loginAsRepositoryAdmin();
    setAclManagement();
    systemTenant =
        tenantManager.createTenant( null, ServerRepositoryPaths.getPentahoRootFolderName(), tenantAdminAuthorityName,
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

  @Test
  public void testDummy() {

  }

  @Ignore
  public void testEverything() throws Exception {
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminAuthorityName,
      tenantAuthenticatedAuthorityName } );
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminAuthorityName,
            tenantAuthenticatedAuthorityName, "Anonymous" );
    userRoleDao.createUser( systemTenant, USERNAME_SUZY, "password", "", new String[] { tenantAdminAuthorityName } );
    logout();
    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedAuthorityName } );
    defaultBackingRepositoryLifecycleManager.newUser( tenantAcme, USERNAME_SUZY );
    System.out.println( "getFile" );
    JcrRepositoryDumpToFile dumpToFile =
        new JcrRepositoryDumpToFile( testJcrTemplate, jcrTransactionTemplate, repositoryAdminUsername,
            "c:/build/testrepo_9", Mode.CUSTOM );
    dumpToFile.execute();
    RepositoryFile f = repo.getFile( ClientRepositoryPaths.getUserHomeFolderPath( USERNAME_SUZY ) );
    assertNotNull( f.getId() );
    assertEquals( ClientRepositoryPaths.getUserHomeFolderPath( USERNAME_SUZY ), f.getPath() );
    assertNotNull( f.getCreatedDate() );
    assertEquals( "public", f.getName() );
    assertTrue( f.isFolder() );

    System.out.println( "getFileById" );
    assertNotNull( repo.getFileById( f.getId() ) );

    System.out.println( "createFolder" );
    RepositoryFile folder1 =
        repo.createFolder( f.getId(), new RepositoryFile.Builder( "folder1" ).folder( true ).build(), null );
    assertNotNull( folder1 );
    assertEquals( "folder1", folder1.getName() );
    assertNotNull( folder1.getId() );

    NodeRepositoryFileData data = makeNodeRepositoryFileData1();
    System.out.println( "createFile" );
    RepositoryFile file1 =
        repo.createFile( folder1.getId(), new RepositoryFile.Builder( "file1.whatever" ).versioned( true ).build(),
            data, null );
    assertNotNull( file1 );
    assertNotNull( file1.getId() );

    System.out.println( "getDataForRead" );
    NodeRepositoryFileData file1Data = repo.getDataForRead( file1.getId(), NodeRepositoryFileData.class );
    assertNotNull( file1Data );
    assertEquals( "testNode", file1Data.getNode().getName() );
    assertEquals( "hello world", file1Data.getNode().getProperty( "prop1" ).getString() );
    assertEquals( false, file1Data.getNode().getProperty( "prop2" ).getBoolean() );
    assertEquals( DataPropertyType.BOOLEAN, file1Data.getNode().getProperty( "prop2" ).getType() );
    assertEquals( 12L, file1Data.getNode().getProperty( "prop3" ).getLong() );

    System.out.println( "createFile (binary)" );
    SimpleRepositoryFileData simpleData =
        new SimpleRepositoryFileData( new ByteArrayInputStream( "Hello World!".getBytes( "UTF-8" ) ), "UTF-8",
            "text/plain" );
    RepositoryFile simpleFile =
        repo.createFile( folder1.getId(), new RepositoryFile.Builder( "file2.whatever" ).versioned( true ).build(),
            simpleData, null );

    Serializable simpleVersion = simpleFile.getVersionId();

    System.out.println( "getDataForRead (binary)" );
    SimpleRepositoryFileData simpleFileData = repo.getDataForRead( simpleFile.getId(), SimpleRepositoryFileData.class );
    assertNotNull( simpleFileData );
    assertEquals( "Hello World!", IOUtils.toString( simpleFileData.getStream(), simpleFileData.getEncoding() ) );
    assertEquals( "text/plain", simpleFileData.getMimeType() );
    assertEquals( "UTF-8", simpleFileData.getEncoding() );

    System.out.println( "updateFile (binary)" );
    simpleData =
        new SimpleRepositoryFileData( new ByteArrayInputStream( "Ciao World!".getBytes( "UTF-8" ) ), "UTF-8",
            "text/plain" );
    simpleFile = repo.updateFile( simpleFile, simpleData, null );
    assertNotNull( simpleFile.getLastModifiedDate() );

    System.out.println( "getDataForRead (binary)" );
    simpleFileData = repo.getDataForRead( simpleFile.getId(), SimpleRepositoryFileData.class );
    assertNotNull( simpleFileData );
    assertEquals( "Ciao World!", IOUtils.toString( simpleFileData.getStream(), simpleFileData.getEncoding() ) );

    System.out.println( "getDataForReadAtVersion (binary)" );
    simpleFileData = repo.getDataAtVersionForRead( simpleFile.getId(), simpleVersion, SimpleRepositoryFileData.class );
    assertNotNull( simpleFileData );
    assertEquals( "Hello World!", IOUtils.toString( simpleFileData.getStream(), simpleFileData.getEncoding() ) );

    System.out.println( "getChildren" );
    List<RepositoryFile> folder1Children = repo.getChildren( folder1.getId() );
    assertNotNull( folder1Children );
    assertEquals( 2, folder1Children.size() );
    System.out.println( "getChildren" );
    List<RepositoryFile> folder1ChildrenFiltered = repo.getChildren( folder1.getId(), "*.sample");
    assertNotNull( folder1ChildrenFiltered );
    assertEquals( 0, folder1ChildrenFiltered.size() );
    System.out.println( "getDeletedFiles" );
    assertEquals( 0, repo.getDeletedFiles().size() );
    System.out.println( "deleteFile" );
    repo.deleteFile( file1.getId(), null );
    System.out.println( "getDeletedFiles" );
    assertEquals( 0, repo.getDeletedFiles( folder1.getPath(), "*.sample" ).size() );

    System.out.println( "hasAccess" );
    assertFalse( repo.hasAccess( "/pentaho", EnumSet.of( RepositoryFilePermission.WRITE ) ) );

    System.out.println( "getEffectiveAces" );
    List<RepositoryFileAce> folder1EffectiveAces = repo.getEffectiveAces( folder1.getId() );
    assertEquals( 1, folder1EffectiveAces.size() );

    System.out.println( "getAcl" );
    RepositoryFileAcl folder1Acl = repo.getAcl( folder1.getId() );
    assertEquals( "suzy", folder1Acl.getOwner().getName() );

    System.out.println( "updateAcl" );
    RepositoryFileAcl updatedFolder1Acl =
        repo.updateAcl( new RepositoryFileAcl.Builder( folder1Acl ).entriesInheriting( false ).ace(
            userNameUtils.getPrincipleId( new Tenant( "duff", true ), "suzy" ), RepositoryFileSid.Type.USER,
            RepositoryFilePermission.ALL ).build() );
    assertNotNull( updatedFolder1Acl );
    assertEquals( 1, updatedFolder1Acl.getAces().size() );

    System.out.println( "lockFile" );
    assertFalse( file1.isLocked() );
    repo.lockFile( file1.getId(), "I locked this file" );
    System.out.println( "canUnlockFile" );
    assertTrue( repo.canUnlockFile( file1.getId() ) );
    System.out.println( "unlockFile" );
    repo.unlockFile( file1.getId() );

    System.out.println( "moveFile" );
    repo.moveFile( file1.getId(), ClientRepositoryPaths.getUserHomeFolderPath( USERNAME_SUZY ) + "/folder1", null );
    System.out.println( "copyFile" );
    repo.copyFile( file1.getId(), ClientRepositoryPaths.getUserHomeFolderPath( USERNAME_SUZY )
        + "/folder1/fileB.whatever", null );

    System.out.println( "getVersionSummaries" );
    List<VersionSummary> versionSummaries = repo.getVersionSummaries( file1.getId() );
    assertNotNull( versionSummaries );
    assertTrue( versionSummaries.size() >= 2 );
    assertEquals( "suzy", versionSummaries.get( 0 ).getAuthor() );

    System.out.println( "getVersionSummary" );
    VersionSummary versionSummary = repo.getVersionSummary( file1.getId(), null );
    assertNotNull( versionSummary );
    assertNotNull( versionSummary.getId() );

    System.out.println( "getFileAtVersion" );
    RepositoryFile file1AtVersion = repo.getFileAtVersion( file1.getId(), versionSummary.getId() );
    assertNotNull( file1AtVersion );
    assertEquals( versionSummary.getId(), file1AtVersion.getVersionId() );

    System.out.println( "getTree" );
    RepositoryFileTree tree = repo.getTree( ClientRepositoryPaths.getRootFolderPath(), -1, null, true );
    assertNotNull( tree.getFile().getId() );

    System.out.println( "getDataForReadInBatch" );
    List<NodeRepositoryFileData> result =
        repo.getDataForReadInBatch( Arrays.asList( file1, simpleFile ), NodeRepositoryFileData.class );
    assertEquals( 2, result.size() );

    System.out.println( "getVersionSummaryInBatch" );
    List<VersionSummary> vResult = repo.getVersionSummaryInBatch( Arrays.asList( file1, simpleFile ) );
    assertEquals( 2, result.size() );

    System.out.println( "getReservedChars" );
    assertFalse( repo.getReservedChars().isEmpty() );
  }

  private NodeRepositoryFileData makeNodeRepositoryFileData1() {
    DataNode node = new DataNode( "testNode" );
    node.setProperty( "prop1", "hello world" );
    node.setProperty( "prop2", false );
    node.setProperty( "prop3", 12L );
    return new NodeRepositoryFileData( node );
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
    sysAdminAuthorityName = (String) applicationContext.getBean( "superAdminAuthorityName" );
    tenantAuthenticatedAuthorityName = (String) applicationContext.getBean( "singleTenantAuthenticatedAuthorityName" );
    tenantAdminAuthorityName = (String) applicationContext.getBean( "singleTenantAdminAuthorityName" );
    sysAdminUserName = (String) applicationContext.getBean( "superAdminUserName" );
    authorizationPolicy = (IAuthorizationPolicy) applicationContext.getBean( "authorizationPolicy" );
    tenantManager = (ITenantManager) applicationContext.getBean( "tenantMgrProxy" );
    userRoleDao = (IUserRoleDao) applicationContext.getBean( "userRoleDao" );
    repositoryFileDao = (IRepositoryFileDao) applicationContext.getBean( "repositoryFileDao" );
    pathConversionHelper = (IPathConversionHelper) applicationContext.getBean( "pathConversionHelper" );
    roleBindingDaoTarget =
        (IRoleAuthorizationPolicyRoleBindingDao) applicationContext
            .getBean( "roleAuthorizationPolicyRoleBindingDaoTarget" );
    roleBindingDao =
        (IRoleAuthorizationPolicyRoleBindingDao) applicationContext
            .getBean( "roleAuthorizationPolicyRoleBindingDaoTxn" );
    authorizationPolicy = (IAuthorizationPolicy) applicationContext.getBean( "authorizationPolicy" );
    defaultBackingRepositoryLifecycleManager =
        (DefaultBackingRepositoryLifecycleManager) applicationContext
            .getBean( "defaultBackingRepositoryLifecycleManager" );
    tenantedUserNameUtils = (ITenantedPrincipleNameResolver) applicationContext.getBean( "tenantedUserNameUtils" );
    jcrTransactionTemplate = (TransactionTemplate) applicationContext.getBean( "jcrTransactionTemplate" );
    TestPrincipalProvider.userRoleDao = (IUserRoleDao) applicationContext.getBean( "userRoleDao" );
    TestPrincipalProvider.adminCredentialsStrategy =
        (CredentialsStrategy) applicationContext.getBean( "jcrAdminCredentialsStrategy" );
    TestPrincipalProvider.repository = (Repository) applicationContext.getBean( "jcrRepository" );
  }

}
