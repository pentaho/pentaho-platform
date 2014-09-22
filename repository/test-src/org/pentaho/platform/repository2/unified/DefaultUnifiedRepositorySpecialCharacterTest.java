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

package org.pentaho.platform.repository2.unified;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.jackrabbit.api.JackrabbitWorkspace;
import org.apache.jackrabbit.api.security.authorization.PrivilegeManager;
import org.junit.*;
import org.junit.runner.RunWith;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoRole;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoUser;
import org.pentaho.platform.api.engine.security.userroledao.IUserRoleDao;
import org.pentaho.platform.api.locale.IPentahoLocale;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.mt.ITenantManager;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.pentaho.platform.api.repository2.unified.*;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl.Builder;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid.Type;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode.DataPropertyType;
import org.pentaho.platform.api.repository2.unified.data.node.DataNodeRef;
import org.pentaho.platform.api.repository2.unified.data.node.DataProperty;
import org.pentaho.platform.api.repository2.unified.data.node.NodeRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.data.sample.SampleRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.core.mt.Tenant;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.repository.RepositoryFilenameUtils;
import org.pentaho.platform.repository2.ClientRepositoryPaths;
import org.pentaho.platform.repository2.locale.PentahoLocale;
import org.pentaho.platform.repository2.unified.jcr.*;
import org.pentaho.platform.repository2.unified.jcr.JcrRepositoryDumpToFile.Mode;
import org.pentaho.platform.repository2.unified.jcr.jackrabbit.security.TestPrincipalProvider;
import org.pentaho.platform.repository2.unified.jcr.sejcr.CredentialsStrategy;
import org.pentaho.platform.security.policy.rolebased.IRoleAuthorizationPolicyRoleBindingDao;
import org.pentaho.platform.security.policy.rolebased.RoleBindingStruct;
import org.pentaho.platform.security.policy.rolebased.actions.*;
import org.pentaho.platform.security.userroledao.DefaultTenantedPrincipleNameResolver;
import org.pentaho.test.platform.engine.core.MicroPlatform;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.extensions.jcr.JcrCallback;
import org.springframework.extensions.jcr.JcrTemplate;
import org.springframework.extensions.jcr.SessionFactory;
import org.springframework.security.AccessDeniedException;
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

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.jcr.security.AccessControlException;
import javax.jcr.security.Privilege;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Integration test. Tests {@link org.pentaho.platform.repository2.unified.DefaultUnifiedRepository} and {@link org.pentaho.platform.api.engine.IAuthorizationPolicy} fully configured
 * behind Spring Security's method security and Spring's transaction interceptor.
 *
 * <p>
 * Note the RunWith annotation that uses a special runner that knows how to setup a Spring application context. The
 * application context config files are listed in the ContextConfiguration annotation. By implementing
 * {@link org.springframework.context.ApplicationContextAware}, this unit test can access various beans defined in the application context,
 * including the bean under test.
 * </p>
 *
 * @author mlowery
 */
@Ignore
@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration( locations = { "classpath:/repository.spring.xml",
    "classpath:/repository-test-override.spring.xml" } )
@SuppressWarnings( "nls" )
public class DefaultUnifiedRepositorySpecialCharacterTest implements ApplicationContextAware {
  // ~ Static fields/initializers
  // ======================================================================================

  private static final String NAMESPACE_REPOSITORY = "org.pentaho.repository";

  private static final String NAMESPACE_SECURITY = "org.pentaho.security";

  private static final String NAMESPACE_SCHEDULER = "org.pentaho.scheduler";

  private static final String NAMESPACE_PENTAHO = "org.pentaho";

  private static final String NAMESPACE_DOESNOTEXIST = "doesnotexist";

  private static final String RUNTIME_ROLE_ACME_ADMIN = "acme_Admin";

  private static final String RUNTIME_ROLE_ACME_AUTHENTICATED = "acme_Authenticated";

  private final String USERNAME_SUZY = "suzy";

  private final String USERNAME_TIFFANY = "tiffany";

  private final String USERNAME_PAT = "pat";

  private final String USERNAME_ADMIN = "admin";

  private final String USERNAME_GEORGE = "george";

  private final String TENANT_ID_ACME = "acme";

  private final String TENANT_ID_DUFF = "duff";

  // ~ Instance fields
  // =================================================================================================

  private ITenantManager tenantManager;

  private IUnifiedRepository repo;

  private String repositoryAdminUsername;

  private IBackingRepositoryLifecycleManager repositoryLifecyleManager;

  private IBackingRepositoryLifecycleManager defaultBackingRepositoryLifecycleManager;

  /**
   * Used for state verification and test cleanup.
   */
  private JcrTemplate testJcrTemplate;
  private JcrTemplate jcrTemplate;

  private IRoleAuthorizationPolicyRoleBindingDao roleBindingDao;
  private IRoleAuthorizationPolicyRoleBindingDao roleBindingDaoTarget;
  private IRepositoryFileDao repositoryFileDao;
  private IAuthorizationPolicy authorizationPolicy;

  private MicroPlatform mp;

  private ITenantedPrincipleNameResolver userNameUtils = new DefaultTenantedPrincipleNameResolver();

  private ITenantedPrincipleNameResolver roleNameUtils = new DefaultTenantedPrincipleNameResolver(
      DefaultTenantedPrincipleNameResolver.ALTERNATE_DELIMETER );

  private String superAdminRoleName;
  private String tenantAdminRoleName;
  private String tenantAuthenticatedRoleName;
  private String sysAdminUserName;
  private ITenant systemTenant;
  private IPathConversionHelper pathConversionHelper;
  IUserRoleDao userRoleDao;
  IUserRoleDao testUserRoleDao;
  IRepositoryFileAclDao repositoryFileAclDao;
  private static TransactionTemplate jcrTransactionTemplate;
  private TransactionTemplate txnTemplate;

  // ~ Constructors
  // ====================================================================================================

  public DefaultUnifiedRepositorySpecialCharacterTest() throws Exception {
    super();
  }

  // ~ Methods
  // =========================================================================================================

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
    loginAsRepositoryAdmin();
    SimpleJcrTestUtils.deleteItem( testJcrTemplate, ServerRepositoryPaths.getPentahoRootFolderPath() );
    mp = new MicroPlatform();
    // used by DefaultPentahoJackrabbitAccessControlHelper
    mp.defineInstance( "tenantedUserNameUtils", userNameUtils );
    mp.defineInstance( "tenantedRoleNameUtils", roleNameUtils );
    mp.defineInstance("ILockHelper", new DefaultLockHelper(userNameUtils));

    mp.defineInstance( IAuthorizationPolicy.class, authorizationPolicy );
    mp.defineInstance( ITenantManager.class, tenantManager );
    mp.defineInstance( "roleAuthorizationPolicyRoleBindingDaoTarget", roleBindingDaoTarget );
    mp.defineInstance( "repositoryAdminUsername", repositoryAdminUsername );
    mp.defineInstance("RepositoryFileProxyFactory", new RepositoryFileProxyFactory(this.jcrTemplate, this.repositoryFileDao));
    mp.defineInstance("ITenantedPrincipleNameResolver", new DefaultTenantedPrincipleNameResolver());
    // Start the micro-platform
    mp.start();
    loginAsRepositoryAdmin();
    setAclManagement();

    systemTenant =
        tenantManager.createTenant( null, ServerRepositoryPaths.getPentahoRootFolderName(), tenantAdminRoleName,
            tenantAuthenticatedRoleName, "Anonymous" );
    userRoleDao.createUser( systemTenant, sysAdminUserName, "password", "", new String[] { tenantAdminRoleName } );
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

  private void cleanupUserAndRoles( final ITenant tenant ) {
    loginAsRepositoryAdmin();
    for ( IPentahoRole role : testUserRoleDao.getRoles( tenant ) ) {
      testUserRoleDao.deleteRole( role );
    }
    for ( IPentahoUser user : testUserRoleDao.getUsers( tenant ) ) {
      testUserRoleDao.deleteUser( user );
    }
  }

  @After
  public void tearDown() throws Exception {
    // null out fields to get back memory
    authorizationPolicy = null;
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant tenant =
        tenantManager.getTenant( "/" + ServerRepositoryPaths.getPentahoRootFolderName() + "/" + TENANT_ID_ACME );
    if ( tenant != null ) {
      cleanupUserAndRoles( tenant );
    }
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    tenant = tenantManager.getTenant( "/" + ServerRepositoryPaths.getPentahoRootFolderName() + "/" + TENANT_ID_DUFF );
    if ( tenant != null ) {
      cleanupUserAndRoles( tenant );
    }
    cleanupUserAndRoles( systemTenant );
    SimpleJcrTestUtils.deleteItem( testJcrTemplate, ServerRepositoryPaths.getPentahoRootFolderPath() );
    logout();

    repositoryAdminUsername = null;
    tenantAdminRoleName = null;
    tenantAuthenticatedRoleName = null;
    roleBindingDao = null;
    authorizationPolicy = null;
    testJcrTemplate = null;
    tenantManager = null;
    repo = null;
    repositoryLifecyleManager = null;
    defaultBackingRepositoryLifecycleManager = null;
    roleBindingDaoTarget = null;
    repositoryFileDao = null;
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
    repositoryFileAclDao = null;
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

  @Test
  public void testGetFileWithLoadedMaps() throws Exception {
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, "password", "", new String[] { tenantAdminRoleName } );
    logout();

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    final String fileName = "helloworld.[~!@#$%^&*(){}|.,]-=_+|;'?<>~`.sample";
    RepositoryFile newFile =
        createSampleFile( ClientRepositoryPaths.getUserHomeFolderPath( USERNAME_SUZY ), fileName, "blah", false, 123 );
    assertEquals( fileName, newFile.getTitle() );
    RepositoryFile.Builder builder = new RepositoryFile.Builder( newFile );
    final String EN_US_VALUE = "Hello World Sample";
    builder.title( Locale.getDefault().toString(), EN_US_VALUE );
    final String ROOT_LOCALE_VALUE = "Hello World";
    builder.title( RepositoryFile.DEFAULT_LOCALE, ROOT_LOCALE_VALUE );
    final SampleRepositoryFileData modContent = new SampleRepositoryFileData( "blah", false, 123 );
    repo.updateFile( builder.build(), modContent, null );
    RepositoryFile updatedFileWithMaps =
        repo.getFile( ClientRepositoryPaths.getUserHomeFolderPath( USERNAME_SUZY ) + RepositoryFile.SEPARATOR
            + "helloworld.[~!@#$%^&*(){}|.,]-=_+|;'?<>~`.sample", true );

    assertEquals( EN_US_VALUE, updatedFileWithMaps.getLocalePropertiesMap().get( Locale.getDefault().toString() )
        .getProperty( RepositoryFile.FILE_TITLE ) );
    assertEquals( ROOT_LOCALE_VALUE, updatedFileWithMaps.getLocalePropertiesMap().get( RepositoryFile.DEFAULT_LOCALE )
        .getProperty( RepositoryFile.FILE_TITLE ) );
    logout();
  }

  @Test
  public void testLocales() throws Exception {
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, "password", "", new String[] { tenantAdminRoleName } );
    logout();

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );

    // Create file
    final String fileName = "locale.[~!@#$%^&*(){}|.,]-=_+|;'?<>~`.sample";
    RepositoryFile file =
        createSampleFile( ClientRepositoryPaths.getUserHomeFolderPath( USERNAME_SUZY ), fileName, "test", false, 123 );

    // Test filename title matches created file name
    assertEquals( fileName, file.getTitle() );

    final IPentahoLocale SPANISH = new PentahoLocale( new Locale( "es" ) );
    final IPentahoLocale US = new PentahoLocale( Locale.US );
    final String EN_US_TITLE = "Locale Sample";
    final String EN_US_DESCRIPTION = "This is a test for retrieving localized words";
    final String SP_TITLE = "Muestra de Localizacion";
    final String SP_DESCRIPTION = "Esta es una prueba para buscar palabras localizadas";

    RepositoryFile.Builder builder = new RepositoryFile.Builder( file );

    // Set English locale values
    builder.title( US.toString(), EN_US_TITLE );
    builder.description( US.toString(), EN_US_DESCRIPTION );

    // Set Spanish locale values
    builder.title( SPANISH.toString(), SP_TITLE );
    builder.description( SPANISH.toString(), SP_DESCRIPTION );

    // Update file data
    final SampleRepositoryFileData modContent = new SampleRepositoryFileData( "blah", false, 123 );
    repo.updateFile( builder.build(), modContent, null );

    // Retrieve file - gets full map
    RepositoryFile updatedFile = repo.getFile( file.getPath(), true );

    /*
     * Retrieve single result with locale
     */

    // SPANISH
    updatedFile = repo.getFile( file.getPath(), SPANISH );

    assertEquals( SP_TITLE, updatedFile.getTitle() );
    assertEquals( SP_DESCRIPTION, updatedFile.getDescription() );

    // US ENGLISH
    updatedFile = repo.getFile( file.getPath(), US );

    assertEquals( EN_US_TITLE, updatedFile.getTitle() );
    assertEquals( EN_US_DESCRIPTION, updatedFile.getDescription() );

    // ROOT Locale
    updatedFile = repo.getFile( file.getPath(), null );

    assertEquals( EN_US_TITLE, updatedFile.getTitle() );
    assertEquals( EN_US_DESCRIPTION, updatedFile.getDescription() );

    logout();
  }

  @Test
  public void testLocalePropertiesMap() throws Exception {
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, "password", "", new String[] { tenantAdminRoleName } );
    logout();

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );

    // Create file
    final String fileName = "locale.[~!@#$%^&*(){}|.,]-=_+|;'?<>~`.sample";
    RepositoryFile file =
        createSampleFile( ClientRepositoryPaths.getUserHomeFolderPath( USERNAME_SUZY ), fileName, "test", false, 123 );

    // Test filename title matches created file name
    assertEquals( fileName, file.getTitle() );

    final String DEFAULT_LOCALE = "default";
    final IPentahoLocale SPANISH = new PentahoLocale( new Locale( "es" ) );
    final IPentahoLocale US = new PentahoLocale( Locale.US );
    final String TITLE = "title";
    final String DESCRIPTION = "description";
    final String EN_US_TITLE = "Locale Sample";
    final String EN_US_DESCRIPTION = "This is a test for retrieving localized words";
    final String SP_TITLE = "Muestra de Localizacion";
    final String SP_DESCRIPTION = "Esta es una prueba para buscar palabras localizadas";

    RepositoryFile.Builder builder = new RepositoryFile.Builder( file );
    Map<String, Properties> localeMap = new HashMap<String, Properties>();

    // Set English locale values
    final Properties enProperties = new Properties();
    enProperties.setProperty( TITLE, EN_US_TITLE );
    enProperties.setProperty( DESCRIPTION, EN_US_DESCRIPTION );
    localeMap.put( US.toString(), enProperties );

    // Set Spanish locale values
    final Properties esProperties = new Properties();
    esProperties.setProperty( TITLE, SP_TITLE );
    esProperties.setProperty( DESCRIPTION, SP_DESCRIPTION );
    localeMap.put( SPANISH.toString(), esProperties );

    builder.localePropertiesMap( localeMap );

    // Update file data
    final SampleRepositoryFileData modContent = new SampleRepositoryFileData( "blah", false, 123 );
    repo.updateFile( builder.build(), modContent, null );

    // Retrieve file - gets full map
    final RepositoryFile updatedFile = repo.getFile( file.getPath(), true );

    // Assert messages are the same
    Properties ep = updatedFile.getLocalePropertiesMap().get( US.toString() );
    assertEquals( EN_US_TITLE, ep.getProperty( TITLE ) );
    assertEquals( EN_US_DESCRIPTION, ep.getProperty( DESCRIPTION ) );

    Properties sp = updatedFile.getLocalePropertiesMap().get( SPANISH.toString() );
    assertEquals( SP_TITLE, sp.getProperty( TITLE ) );
    assertEquals( SP_DESCRIPTION, sp.getProperty( DESCRIPTION ) );

    // Assert empty rootLocale
    Properties rootLocale = updatedFile.getLocalePropertiesMap().get( DEFAULT_LOCALE );
    assertNotNull( rootLocale );

    final String NEW_TITLE = "new title";
    final String NEW_DESCRIPTION = "new description";
    enProperties.setProperty( TITLE, NEW_TITLE ); // overwrite title
    enProperties.setProperty( DESCRIPTION, NEW_DESCRIPTION ); // overwrite title

    txnTemplate.execute( new TransactionCallbackWithoutResult() {
      public void doInTransactionWithoutResult( final TransactionStatus status ) {

        // assert available locales
        List<Locale> locales = repositoryFileDao.getAvailableLocalesForFile( updatedFile );
        assertEquals( 3, locales.size() ); // includes rootLocale

        // assert correct locale properties
        Properties properties = repositoryFileDao.getLocalePropertiesForFile( updatedFile, "es" );
        assertEquals( SP_TITLE, properties.getProperty( TITLE ) );
        assertEquals( SP_DESCRIPTION, properties.getProperty( DESCRIPTION ) );

        repositoryFileDao.setLocalePropertiesForFile( updatedFile, Locale.US.getLanguage(), enProperties );
      }
    } );

    // Assert updated properties
    RepositoryFile updatedRepoFile = repo.getFile( file.getPath(), true );
    Properties updated_en = updatedRepoFile.getLocalePropertiesMap().get( US.toString() );
    assertEquals( NEW_TITLE, updated_en.getProperty( TITLE ) );
    assertEquals( NEW_DESCRIPTION, updated_en.getProperty( DESCRIPTION ) );

    // test successful delete locale properties
    final RepositoryFile repoFile1 = updatedRepoFile.clone();
    txnTemplate.execute( new TransactionCallbackWithoutResult() {
      public void doInTransactionWithoutResult( final TransactionStatus status ) {
        repositoryFileDao.deleteLocalePropertiesForFile( repoFile1, "es" );
      }
    } );

    // assert deleted locale
    updatedRepoFile = repo.getFile( file.getPath(), true );
    List<Locale> locales = repositoryFileDao.getAvailableLocalesForFile( updatedRepoFile );
    assertEquals( 2, locales.size() );

    // test successful delete locale properties
    final RepositoryFile repoFile2 = updatedRepoFile.clone();
    txnTemplate.execute( new TransactionCallbackWithoutResult() {
      public void doInTransactionWithoutResult( final TransactionStatus status ) {
        repositoryFileDao.deleteLocalePropertiesForFile( repoFile2, "xx" );
      }
    } );

    // locale properties do not exist, no change in available locales
    updatedRepoFile = repo.getFile( file.getPath(), true );
    locales = repositoryFileDao.getAvailableLocalesForFile( updatedRepoFile );
    assertEquals( 2, locales.size() );

    logout();
  }

  /**
   * While they may be filtered from the version history, we still must be able to fetch acl-only changes.
   */
  @Test
  public void testGetAclOnlyVersion() throws Exception {
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, "password", "", new String[] { tenantAdminRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, "password", "", null );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );
    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    defaultBackingRepositoryLifecycleManager.newTenant();

    final String fileName = "helloworld.[~!@#$%^&*(){}|.,]-=_+|;'?<>~`.sample";
    RepositoryFile newFile =
        createSampleFile( ClientRepositoryPaths.getUserHomeFolderPath( USERNAME_SUZY ), fileName, "blah", false, 123,
            true );
    assertEquals( 1, repo.getVersionSummaries( newFile.getId() ).size() );
    RepositoryFileAcl acl = repo.getAcl( newFile.getId() );
    // no change; just want to create a new version
    RepositoryFileAcl updatedAcl = new Builder( acl ).build();
    updatedAcl = repo.updateAcl( updatedAcl );
    assertEquals( 2, repo.getVersionSummaries( newFile.getId() ).size() );
    assertNotNull( repo.getVersionSummary( newFile.getId(), "1.1" ) );
  }

  @Test
  public void testCreateFolder() throws Exception {
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, "password", "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, "password", "", null );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    RepositoryFile parentFolder = repo.getFile( ClientRepositoryPaths.getUserHomeFolderPath( USERNAME_SUZY ) );
    RepositoryFile newFolder = new RepositoryFile.Builder( "[~!@#$%^&*(){}|.,]-=_+|;'?<:>~" ).folder( true ).hidden( true ).build();

    Date beginTime = Calendar.getInstance().getTime();

    // Sleep for 1 second for time comparison
    Thread.sleep( 1000 );
    newFolder = repo.createFolder( parentFolder.getId(), newFolder, null );
    Thread.sleep( 1000 );

    Date endTime = Calendar.getInstance().getTime();
    assertTrue( beginTime.before( newFolder.getCreatedDate() ) );
    assertTrue( endTime.after( newFolder.getCreatedDate() ) );
    assertNotNull( newFolder );
    assertNotNull( newFolder.getId() );
    assertTrue( newFolder.isHidden() );
    assertNotNull( SimpleJcrTestUtils.getItem( testJcrTemplate, ServerRepositoryPaths.getUserHomeFolderPath(
        tenantAcme, USERNAME_SUZY )
        + "/[~!@#$%^&*(){}|.,]-=_+|;'?<:>~" ) );
  }

  @Test( expected = UnifiedRepositoryException.class )
  public void testCreateFileAtRootIllegal() throws Exception {
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, "password", "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, "password", "", null );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    final String dataString = "Hello World!";
    final String encoding = "UTF-8";
    byte[] data = dataString.getBytes( encoding );
    ByteArrayInputStream dataStream = new ByteArrayInputStream( data );
    final String fileName = "helloworld.[~!@#$%^&*(){}|.,]-=_+|;'?<>~`.xaction";
    final SimpleRepositoryFileData content = new SimpleRepositoryFileData( dataStream, encoding, "text/plain" );
    repo.createFile( null, new RepositoryFile.Builder( fileName ).build(), content, null );
  }

  @Test
  public void testCreateSimpleFile() throws Exception {
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, "password", "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, "password", "", null );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    RepositoryFile parentFolder = repo.getFile( ClientRepositoryPaths.getUserHomeFolderPath( USERNAME_SUZY ) );
    final String expectedDataString = "Hello World!";
    final String expectedEncoding = "UTF-8";
    byte[] data = expectedDataString.getBytes( expectedEncoding );
    ByteArrayInputStream dataStream = new ByteArrayInputStream( data );
    final String expectedMimeType = "text/plain";
    final String expectedName = "helloworld.[~!@#$%^&*(){}|.,]-=_+|;'?<>~`.xaction";
    final String expectedAbsolutePath =
        ClientRepositoryPaths.getUserHomeFolderPath( USERNAME_SUZY ) + "/helloworld.[~!@#$%^&*(){}|.,]-=_+|;'?<>~`.xaction";

    final SimpleRepositoryFileData content =
        new SimpleRepositoryFileData( dataStream, expectedEncoding, expectedMimeType );
    Date beginTime = Calendar.getInstance().getTime();
    Thread.sleep( 1000 ); // when the test runs too fast, begin and lastModifiedDate are the same; manual pause

    Calendar cal = Calendar.getInstance( Locale.US );
    SimpleDateFormat df = new SimpleDateFormat( "EEE, d MMM yyyy HH:mm:ss Z", Locale.US );
    cal.setTime( df.parse( "Wed, 4 Jul 2000 12:08:56 -0700" ) );

    RepositoryFile newFile =
        repo.createFile( parentFolder.getId(), new RepositoryFile.Builder( expectedName ).hidden( true ).versioned(
            true ).createdDate( cal.getTime() ).build(), content, null );

    assertEquals( cal.getTime(), repo.getVersionSummaries( newFile.getId() ).get( 0 ).getDate() );

    Date endTime = Calendar.getInstance().getTime();
    assertTrue( beginTime.before( newFile.getLastModifiedDate() ) );
    assertTrue( endTime.after( newFile.getLastModifiedDate() ) );
    assertNotNull( newFile.getId() );
    RepositoryFile foundFile = repo.getFile( expectedAbsolutePath );
    assertNotNull( foundFile );
    assertEquals( expectedName, foundFile.getName() );
    assertEquals( expectedAbsolutePath, foundFile.getPath() );
    assertNotNull( foundFile.getCreatedDate() );
    assertNotNull( foundFile.getLastModifiedDate() );
    assertTrue( foundFile.isHidden() );
    assertTrue( foundFile.getFileSize() > 0 );

    SimpleRepositoryFileData contentFromRepo = repo.getDataForRead( foundFile.getId(), SimpleRepositoryFileData.class );
    assertEquals( expectedEncoding, contentFromRepo.getEncoding() );
    assertEquals( expectedMimeType, contentFromRepo.getMimeType() );
    assertEquals( expectedDataString, IOUtils.toString( contentFromRepo.getStream(), expectedEncoding ) );
  }

  @Test
  public void testCreateSampleFile() throws Exception {
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, "password", "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, "password", "", null );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    final String expectedName = "helloworld.[~!@#$%^&*(){}|.,]-=_+|;'?<>~`.sample";
    final String sampleString = "Ciao World!";
    final boolean sampleBoolean = true;
    final int sampleInteger = 99;
    final String parentFolderPath = ClientRepositoryPaths.getUserHomeFolderPath( USERNAME_SUZY );
    final String expectedAbsolutePath = parentFolderPath + RepositoryFile.SEPARATOR + expectedName;
    RepositoryFile newFile =
        createSampleFile( parentFolderPath, expectedName, sampleString, sampleBoolean, sampleInteger );

    assertNotNull( newFile.getId() );
    RepositoryFile foundFile = repo.getFile( expectedAbsolutePath );
    assertNotNull( foundFile );
    assertEquals( expectedName, foundFile.getName() );
    assertEquals( expectedAbsolutePath, foundFile.getPath() );
    assertNotNull( foundFile.getCreatedDate() );
    assertNotNull( foundFile.getLastModifiedDate() );

    SampleRepositoryFileData data = repo.getDataForRead( foundFile.getId(), SampleRepositoryFileData.class );

    assertEquals( sampleString, data.getSampleString() );
    assertEquals( sampleBoolean, data.getSampleBoolean() );
    assertEquals( sampleInteger, data.getSampleInteger() );
  }

  @Test
  public void testGetReferrers() throws Exception {
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, "password", "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, "password", "", null );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    final String refereeFileName = "referee.[~!@#$%^&*(){}|.,]-=_+|;'?<>~`.sample";
    final String referrerFileName = "referrer.[~!@#$%^&*(){}|.,]-=_+|;'?<>~`.sample";

    final String parentFolderPath = ClientRepositoryPaths.getUserHomeFolderPath( USERNAME_SUZY );
    RepositoryFile parentFolder = repo.getFile( parentFolderPath );

    RepositoryFile refereeFile = createSampleFile( parentFolderPath, refereeFileName, "dfdd", true, 83 );

    DataNode node = new DataNode( "kdjd" );
    node.setProperty( "ddf", "ljsdfkjsdkf" );
    DataNode newChild1 = node.addNode( "herfkmdx" );
    newChild1.setProperty( "urei2", new DataNodeRef( refereeFile.getId() ) );

    NodeRepositoryFileData data = new NodeRepositoryFileData( node );
    repo.createFile( parentFolder.getId(), new RepositoryFile.Builder( referrerFileName ).build(), data, null );

    List<RepositoryFile> referrers = repo.getReferrers( refereeFile.getId() );

    assertNotNull( referrers );
    assertEquals( 1, referrers.size() );
    assertEquals( referrers.get( 0 ).getName(), referrerFileName );
  }

  @Test
  public void testCreateNodeFile() throws Exception {
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, "password", "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, "password", "", null );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    final String expectedName = "helloworld.[~!@#$%^&*(){}|.,]-=_+|;'?<>~`.doesnotmatter";
    final String parentFolderPath = ClientRepositoryPaths.getUserHomeFolderPath( USERNAME_SUZY );
    RepositoryFile parentFolder = repo.getFile( parentFolderPath );
    final String expectedPath = parentFolderPath + RepositoryFile.SEPARATOR + expectedName;
    final String serverPath =
        ServerRepositoryPaths.getTenantRootFolderPath() + parentFolderPath + RepositoryFile.SEPARATOR
            + "helloworld2.[~!@#$%^&*(){}|.,]-=_+|;'?<>~`.sample";

    RepositoryFile sampleFile = createSampleFile( parentFolderPath, "helloworld2.[~!@#$%^&*(){}|.,]-=_+|;'?<>~`.sample", "dfdd", true, 83 );

    final Date EXP_DATE = new Date();

    DataNode node = new DataNode( "kdjd" );
    node.setProperty( "ddf", "ljsdfkjsdkf" );
    DataNode newChild1 = node.addNode( "herfkmdx" );
    newChild1.setProperty( "sdfs", true );
    newChild1.setProperty( "ks3", EXP_DATE );
    newChild1.setProperty( "ids32", 7.32D );
    newChild1.setProperty( "erere3", 9856684583L );
    newChild1.setProperty( "tttss4", "843skdfj33ksaljdfj" );
    newChild1.setProperty( "urei2", new DataNodeRef( sampleFile.getId() ) );
    DataNode newChild2 = node.addNode( RepositoryFilenameUtils.escape( "pppq/qqs2", repo.getReservedChars() ) );
    newChild2.setProperty( RepositoryFilenameUtils.escape( "ttt:ss4", repo.getReservedChars() ), "843skdfj33ksaljdfj" );

    NodeRepositoryFileData data = new NodeRepositoryFileData( node );
    RepositoryFile newFile =
        repo.createFile( parentFolder.getId(), new RepositoryFile.Builder( expectedName ).build(), data, null );

    assertNotNull( newFile.getId() );
    RepositoryFile foundFile = repo.getFile( expectedPath );
    assertNotNull( foundFile );
    assertEquals( expectedName, foundFile.getName() );

    DataNode foundNode = repo.getDataForRead( foundFile.getId(), NodeRepositoryFileData.class ).getNode();

    assertEquals( node.getName(), foundNode.getName() );
    assertNotNull( foundNode.getId() );
    assertEquals( node.getProperty( "ddf" ), foundNode.getProperty( "ddf" ) );
    int actualPropCount = 0;
    for ( DataProperty prop : foundNode.getProperties() ) {
      actualPropCount++;
    }
    assertEquals( 1, actualPropCount );
    assertTrue( foundNode.hasNode( "herfkmdx" ) );
    DataNode foundChild1 = foundNode.getNode( "herfkmdx" );
    assertNotNull( foundChild1.getId() );
    assertEquals( newChild1.getName(), foundChild1.getName() );
    assertEquals( newChild1.getProperty( "sdfs" ), foundChild1.getProperty( "sdfs" ) );
    assertEquals( newChild1.getProperty( "ks3" ), foundChild1.getProperty( "ks3" ) );
    assertEquals( newChild1.getProperty( "ids32" ), foundChild1.getProperty( "ids32" ) );
    assertEquals( newChild1.getProperty( "erere3" ), foundChild1.getProperty( "erere3" ) );
    assertEquals( newChild1.getProperty( "tttss4" ), foundChild1.getProperty( "tttss4" ) );
    assertEquals( newChild1.getProperty( "urei2" ), foundChild1.getProperty( "urei2" ) );

    try {
      repo.deleteFile( sampleFile.getId(), true, null );
      fail();
    } catch ( UnifiedRepositoryException e ) {
      // should fail due to referential integrity (newFile payload has reference to sampleFile)
    }

    actualPropCount = 0;
    for ( DataProperty prop : newChild1.getProperties() ) {
      actualPropCount++;
    }
    assertEquals( 6, actualPropCount );

    assertTrue( foundNode.hasNode( RepositoryFilenameUtils.escape( "pppq/qqs2", repo.getReservedChars() ) ) );
    DataNode foundChild2 = foundNode.getNode( RepositoryFilenameUtils.escape( "pppq/qqs2", repo.getReservedChars() ) );
    assertNotNull( foundChild2.getId() );
    assertEquals( newChild2.getName(), foundChild2.getName() );
    assertEquals( newChild2.getProperty( RepositoryFilenameUtils.escape( "ttt:ss4", repo.getReservedChars() ) ),
        foundChild2.getProperty( RepositoryFilenameUtils.escape( "ttt:ss4", repo.getReservedChars() ) ) );
    actualPropCount = 0;
    for ( DataProperty prop : foundChild2.getProperties() ) {
      actualPropCount++;
    }
    assertEquals( 1, actualPropCount );

    // ordering
    int i = 0;
    for ( DataNode currentNode : foundNode.getNodes() ) {
      if ( i++ == 0 ) {
        assertEquals( newChild1.getName(), currentNode.getName() );
      } else {
        assertEquals( newChild2.getName(), currentNode.getName() );
      }
    }
  }

  @Test
  public void testUpdateFile() throws Exception {
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, "password", "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, "password", "", null );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    final String parentFolderPath = ClientRepositoryPaths.getUserHomeFolderPath( USERNAME_SUZY );
    final String fileName = "helloworld.[~!@#$%^&*(){}|.,]-=_+|;'?<>~`.sample";

    RepositoryFile newFile = createSampleFile( parentFolderPath, fileName, "Hello World!", false, 222 );

    final String modSampleString = "Ciao World!";
    final boolean modSampleBoolean = true;
    final int modSampleInteger = 99;

    final SampleRepositoryFileData modContent =
        new SampleRepositoryFileData( modSampleString, modSampleBoolean, modSampleInteger );

    repo.updateFile( newFile, modContent, null );

    SampleRepositoryFileData modData =
        repo.getDataForRead( repo.getFile(
            ClientRepositoryPaths.getUserHomeFolderPath( USERNAME_SUZY ) + RepositoryFile.SEPARATOR + fileName )
            .getId(), SampleRepositoryFileData.class );

    assertEquals( modSampleString, modData.getSampleString() );
    assertEquals( modSampleBoolean, modData.getSampleBoolean() );
    assertEquals( modSampleInteger, modData.getSampleInteger() );
  }

  @Test
  public void testWriteToPublic() throws Exception {
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, "password", "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );

    final String parentFolderPath = ClientRepositoryPaths.getPublicFolderPath();
    assertNotNull( createSampleFile( parentFolderPath, "helloworld.[~!@#$%^&*(){}|.,]-=_+|;'?<:>~.sample", "Hello World!", false, 500 ) );
  }

  @Test
  public void testLockFile() throws Exception {
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, "password", "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, "password", "", null );
    userRoleDao.createUser( tenantAcme, USERNAME_TIFFANY, "password", "", null );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    final String parentFolderPath =
        ClientRepositoryPaths.getUserHomeFolderPath( PentahoSessionHolder.getSession().getName() );
    RepositoryFile parentFolder = repo.getFile( parentFolderPath );
    final String dataString = "Hello World!";
    final String encoding = "UTF-8";
    byte[] data = dataString.getBytes( encoding );
    ByteArrayInputStream dataStream = new ByteArrayInputStream( data );
    final String mimeType = "text/plain";
    final String fileName = "helloworld.[~!@#$%^&*(){}|.,]-=_+|;'?<:>~.xaction";

    final SimpleRepositoryFileData content = new SimpleRepositoryFileData( dataStream, encoding, mimeType );
    RepositoryFile newFile =
        repo.createFile( parentFolder.getId(), new RepositoryFile.Builder( fileName ).versioned( true ).build(),
            content, null );
    final String clientPath = parentFolderPath + RepositoryFile.SEPARATOR + fileName;
    final String serverPath = ServerRepositoryPaths.getTenantRootFolderPath() + clientPath;
    assertFalse( newFile.isLocked() );
    assertNull( newFile.getLockDate() );
    assertNull( newFile.getLockMessage() );
    assertNull( newFile.getLockOwner() );
    final String lockMessage = "test by :Mat";
    repo.lockFile( newFile.getId(), lockMessage );

    // verify no new versions were created on locking
    assertEquals( 1, repo.getVersionSummaries( newFile.getId() ).size() );

    assertTrue( SimpleJcrTestUtils.isLocked( testJcrTemplate, serverPath ) );
    String ownerInfo = SimpleJcrTestUtils.getString( testJcrTemplate, serverPath + "/jcr:lockOwner" );
    assertEquals( "test by %3AMat", ownerInfo.split( ":" )[2] );
    assertNotNull( new Date( Long.parseLong( ownerInfo.split( ":" )[1] ) ) );

    // test update while locked
    repo.updateFile( repo.getFileById( newFile.getId() ), content, "update by Mat" );

    assertEquals( 2, repo.getVersionSummaries( newFile.getId() ).size() );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    RepositoryFile lockedFile = repo.getFile( clientPath );
    assertTrue( lockedFile.isLocked() );
    assertNotNull( lockedFile.getLockDate() );
    assertEquals( lockMessage, lockedFile.getLockMessage() );
    assertEquals( userNameUtils.getPrincipleId( tenantAcme, USERNAME_SUZY ), lockedFile.getLockOwner() );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );
    assertTrue( repo.canUnlockFile( newFile.getId() ) );
    repo.unlockFile( newFile.getId() );

    assertEquals( 2, repo.getVersionSummaries( newFile.getId() ).size() );
    assertFalse( SimpleJcrTestUtils.isLocked( testJcrTemplate, serverPath ) );
    RepositoryFile unlockedFile = repo.getFile( clientPath );
    assertFalse( unlockedFile.isLocked() );
    assertNull( unlockedFile.getLockDate() );
    assertNull( unlockedFile.getLockMessage() );
    assertNull( unlockedFile.getLockOwner() );

    // make sure lock token node has been removed
    assertNull( SimpleJcrTestUtils.getItem( testJcrTemplate, ServerRepositoryPaths.getUserHomeFolderPath( tenantAcme,
        USERNAME_SUZY )
        + "/.lockTokens/" + newFile.getId() ) );

    // lock it again by suzy
    repo.lockFile( newFile.getId(), lockMessage );

    assertEquals( 2, repo.getVersionSummaries( newFile.getId() ).size() );

    // login as tenant admin; make sure we can unlock
    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    assertTrue( repo.canUnlockFile( newFile.getId() ) );
    repo.unlockFile( newFile.getId() );

    assertEquals( 2, repo.getVersionSummaries( newFile.getId() ).size() );

    RepositoryFile unlockedFile2 = repo.getFile( clientPath );
    assertFalse( unlockedFile2.isLocked() );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );
    // lock it again by suzy
    repo.lockFile( newFile.getId(), lockMessage );

    assertEquals( 2, repo.getVersionSummaries( newFile.getId() ).size() );

  }

  @Test
  public void testUndeleteFile() throws Exception {

    Date testBegin = new Date();

    Thread.sleep( 1000 );

    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, "password", "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, "password", "", null );
    userRoleDao.createUser( tenantAcme, USERNAME_TIFFANY, "password", "", null );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    String parentFolderPath = ClientRepositoryPaths.getUserHomeFolderPath(
      PentahoSessionHolder.getSession().getName() );
    RepositoryFile parentFolder = repo.getFile( parentFolderPath );
    final String fileName = "helloworld.[~!@#$%^&*(){}|.,]-=_+|;'?<:>~.sample";
    RepositoryFile newFile = createSampleFile( parentFolderPath, fileName, "dfdfd", true, 3, true );

    List<RepositoryFile> deletedFiles = repo.getDeletedFiles();
    assertEquals( 0, deletedFiles.size() );
    repo.deleteFile( newFile.getId(), null );

    deletedFiles = repo.getDeletedFiles();
    assertEquals( 1, deletedFiles.size() );

    deletedFiles = repo.getDeletedFiles( parentFolder.getPath() );
    assertEquals( 1, deletedFiles.size() );
    assertTrue( testBegin.before( deletedFiles.get( 0 ).getDeletedDate() ) );
    assertEquals( parentFolder.getPath(), deletedFiles.get( 0 ).getOriginalParentFolderPath() );
    assertEquals( newFile.getId(), deletedFiles.get( 0 ).getId() );

    deletedFiles = repo.getDeletedFiles( parentFolder.getPath(), "*.sample" );
    assertEquals( 1, deletedFiles.size() );
    assertTrue( testBegin.before( deletedFiles.get( 0 ).getDeletedDate() ) );
    assertEquals( parentFolder.getPath(), deletedFiles.get( 0 ).getOriginalParentFolderPath() );

    deletedFiles = repo.getDeletedFiles( parentFolder.getPath(), "*.doesnotexist" );
    assertEquals( 0, deletedFiles.size() );

    deletedFiles = repo.getDeletedFiles();
    assertEquals( 1, deletedFiles.size() );
    assertEquals( parentFolder.getPath(), deletedFiles.get( 0 ).getOriginalParentFolderPath() );
    assertTrue( testBegin.before( deletedFiles.get( 0 ).getDeletedDate() ) );
    assertEquals( newFile, deletedFiles.get( 0 ) );

    login( USERNAME_TIFFANY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );
    // tiffany shouldn't see suzy's deleted file
    assertEquals( 0, repo.getDeletedFiles().size() );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );
    repo.undeleteFile( newFile.getId(), null );
    assertEquals( 0, repo.getDeletedFiles( parentFolder.getPath() ).size() );
    assertEquals( 0, repo.getDeletedFiles().size() );

    newFile = repo.getFileById( newFile.getId() );
    // next two fields only populated when going through the delete-related API calls
    assertNull( newFile.getDeletedDate() );
    assertNull( newFile.getOriginalParentFolderPath() );

    repo.deleteFile( newFile.getId(), null );
    repo.deleteFile( newFile.getId(), true, null ); // permanent delete
    try {
      repo.undeleteFile( newFile.getId(), null );
      fail();
    } catch ( UnifiedRepositoryException e ) {
      //ignore
    }

    // test preservation of original path even if that path no longer exists
    RepositoryFile publicFolder =
        repo.getFile( ClientRepositoryPaths.getUserHomeFolderPath( PentahoSessionHolder.getSession().getName() ) );
    RepositoryFile test1Folder =
        repo.createFolder( publicFolder.getId(), new RepositoryFile.Builder( "test1" ).folder( true ).build(), null );
    newFile = createSampleFile( test1Folder.getPath(), fileName, "dfdfd", true, 3 );
    repo.deleteFile( newFile.getId(), null );
    assertNull( repo.getFile( "/home/suzy/test1/helloworld.[~!@#$%^&*(){}|.,]-=_+|;'?<:>~.sample" ) );
    // rename original parent folder
    repo.moveFile( test1Folder.getId(), ClientRepositoryPaths.getUserHomeFolderPath( PentahoSessionHolder.getSession()
        .getName() )
        + RepositoryFile.SEPARATOR + "test2", null );
    assertNull( repo.getFile( test1Folder.getPath() ) );
    repo.undeleteFile( newFile.getId(), null );
    assertNotNull( repo.getFile( "/home/suzy/test1/helloworld.[~!@#$%^&*(){}|.,]-=_+|;'?<:>~.sample" ) );
    assertNull( repo.getFile( "/home/suzy/test2/helloworld.[~!@#$%^&*(){}|.,]-=_+|;'?<:>~.sample" ) ); // repo should create any missing folders
                                                                        // on undelete
    assertEquals( "/home/suzy/test1/helloworld.[~!@#$%^&*(){}|.,]-=_+|;'?<:>~.sample", repo.getFileById( newFile.getId() ).getPath() );

    // test versioned parent folder
    RepositoryFile test5Folder =
        repo.createFolder( publicFolder.getId(), new RepositoryFile.Builder( "test5" ).folder( true ).versioned( true )
            .build(), null );
    int versionCountBefore = repo.getVersionSummaries( test5Folder.getId() ).size();
    RepositoryFile newFile5 = createSampleFile( test5Folder.getPath(), fileName, "dfdfd", true, 3 );
    repo.deleteFile( newFile5.getId(), null );
    assertTrue( repo.getVersionSummaries( test5Folder.getId() ).size() > versionCountBefore );
    versionCountBefore = repo.getVersionSummaries( test5Folder.getId() ).size();
    repo.undeleteFile( newFile5.getId(), null );
    assertTrue( repo.getVersionSummaries( test5Folder.getId() ).size() > versionCountBefore );

    // test permanent delete without undelete
    RepositoryFile newFile6 =
        createSampleFile( ClientRepositoryPaths.getUserHomeFolderPath( PentahoSessionHolder.getSession().getName() ),
            fileName, "dfdfd", true, 3 );
    repo.deleteFile( newFile6.getId(), true, null );

    // test undelete where path to restored file already exists
    RepositoryFile newFile7 =
        createSampleFile( ClientRepositoryPaths.getUserHomeFolderPath( PentahoSessionHolder.getSession().getName() ),
            fileName, "dfdfd", true, 3 );
    repo.deleteFile( newFile7.getId(), null );
    createSampleFile( ClientRepositoryPaths.getUserHomeFolderPath( PentahoSessionHolder.getSession().getName() ),
        fileName, "dfdfd", true, 3 );

    try {
      repo.undeleteFile( newFile7.getId(), null );
      fail();
    } catch ( UnifiedRepositoryException e ) {
      e.printStackTrace();
    }
  }

  /**
   * Tests that files in legacy trash structure are still found.
   */
  @Test
  public void testUndeleteFileLegacy() throws Exception {
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, "password", "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, "password", "", null );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    final String fileName = "helloworld.[~!@#$%^&*(){}|.,]-=_+|;'?<:>~.sample";
    RepositoryFile publicFolder =
        repo.getFile( ClientRepositoryPaths.getUserHomeFolderPath( PentahoSessionHolder.getSession().getName() ) );
    RepositoryFile test3Folder =
        repo.createFolder( publicFolder.getId(), new RepositoryFile.Builder( "test3" ).folder( true ).build(), null );

    // simulate file(s) in legacy trash structure
    final String suzyHomePath = "/pentaho/acme/home/suzy";
    SimpleJcrTestUtils.addNode( testJcrTemplate, suzyHomePath, ".trash", "pho_nt:pentahoInternalFolder" );
    final String suzyTrashPath = suzyHomePath + "/.trash";
    SimpleJcrTestUtils.addNode( testJcrTemplate, suzyTrashPath, "pho:" + test3Folder.getId(),
        "pho_nt:pentahoInternalFolder" );
    final String suzyTrashFolderIdPath = suzyTrashPath + "/pho:" + test3Folder.getId();
    assertNotNull( SimpleJcrTestUtils.getItem( testJcrTemplate, suzyTrashFolderIdPath ) );
    RepositoryFile newFile3 = createSampleFile( test3Folder.getPath(), fileName, "dfdfd", true, 3, true );
    SimpleJcrTestUtils.addNode( testJcrTemplate, suzyTrashFolderIdPath, "pho:" + newFile3.getId(),
        "pho_nt:pentahoInternalFolder" );
    final String suzyTrashFileIdPath = suzyTrashFolderIdPath + "/pho:" + newFile3.getId();
    assertNotNull( SimpleJcrTestUtils.getItem( testJcrTemplate, suzyTrashFileIdPath ) );
    String absTrashPath = suzyTrashFileIdPath + "/helloworld.[~!@#$%^&*(){}|.,]-=_+|;'?<:>~.sample";
    SimpleJcrTestUtils.move( testJcrTemplate, "/pentaho/acme/home/suzy/test3/helloworld.[~!@#$%^&*(){}|.,]-=_+|;'?<:>~.sample", absTrashPath );
    assertNotNull( SimpleJcrTestUtils.getItem( testJcrTemplate, absTrashPath ) );
    Date expectedDate = new Date();
    SimpleJcrTestUtils.setDate( testJcrTemplate, suzyTrashFileIdPath + "/pho:deletedDate", expectedDate );

    List<RepositoryFile> deletedFiles = repo.getDeletedFiles( test3Folder.getPath() );
    assertEquals( 1, deletedFiles.size() );
    assertEquals( expectedDate, deletedFiles.get( 0 ).getDeletedDate() );
    assertEquals( test3Folder.getPath(), deletedFiles.get( 0 ).getOriginalParentFolderPath() );

    deletedFiles = repo.getDeletedFiles( test3Folder.getPath(), "*.sample" );
    assertEquals( 1, deletedFiles.size() );
    assertEquals( expectedDate, deletedFiles.get( 0 ).getDeletedDate() );
    assertEquals( test3Folder.getPath(), deletedFiles.get( 0 ).getOriginalParentFolderPath() );

    deletedFiles = repo.getDeletedFiles( test3Folder.getPath(), "*.doesnotexist" );
    assertEquals( 0, deletedFiles.size() );

    deletedFiles = repo.getDeletedFiles();
    assertEquals( 1, deletedFiles.size() );
    assertEquals( expectedDate, deletedFiles.get( 0 ).getDeletedDate() );
    assertEquals( test3Folder.getPath(), deletedFiles.get( 0 ).getOriginalParentFolderPath() );

    repo.undeleteFile( newFile3.getId(), null );
    assertNull( SimpleJcrTestUtils.getItem( testJcrTemplate, suzyTrashFileIdPath ) );
    assertNotNull( repo.getFile( newFile3.getPath() ) );

    repo.deleteFile( newFile3.getId(), true, null );
    try {
      repo.getFileById( newFile3.getId() );
      fail();
    } catch ( UnifiedRepositoryException e ) {
      //ignore
    }
  }

  @Test
  public void testDeleteLockedFile() throws Exception {
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, "password", "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, "password", "", null );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    final String parentFolderPath =
        ClientRepositoryPaths.getUserHomeFolderPath( PentahoSessionHolder.getSession().getName() );
    RepositoryFile parentFolder = repo.getFile( parentFolderPath );
    final String dataString = "Hello World!";
    final String encoding = "UTF-8";
    byte[] data = dataString.getBytes( encoding );
    ByteArrayInputStream dataStream = new ByteArrayInputStream( data );
    final String mimeType = "text/plain";
    final String fileName = "helloworld.[~!@#$%^&*(){}|.,]-=_+|;'?<:>~.xaction";

    final SimpleRepositoryFileData content = new SimpleRepositoryFileData( dataStream, encoding, mimeType );
    RepositoryFile newFile =
        repo.createFile( parentFolder.getId(), new RepositoryFile.Builder( fileName ).build(), content, null );
    final String filePath = parentFolderPath + RepositoryFile.SEPARATOR + fileName;
    assertFalse( repo.getFile( filePath ).isLocked() );
    final String lockMessage = "test by Mat";
    repo.lockFile( newFile.getId(), lockMessage );

    repo.deleteFile( newFile.getId(), null );
    // lock only removed when file is permanently deleted
    assertNotNull( SimpleJcrTestUtils.getItem( testJcrTemplate, ServerRepositoryPaths.getUserHomeFolderPath(
        tenantAcme, USERNAME_SUZY )
        + "/.lockTokens/" + newFile.getId() ) );
    repo.undeleteFile( newFile.getId(), null );
    repo.deleteFile( newFile.getId(), null );
    repo.deleteFile( newFile.getId(), true, null );

    // make sure lock token node has been removed
    assertNull( SimpleJcrTestUtils.getItem( testJcrTemplate, ServerRepositoryPaths.getUserHomeFolderPath( tenantAcme,
        USERNAME_SUZY )
        + "/.lockTokens/" + newFile.getId() ) );
  }

  @Test
  public void testDeleteFileAtVersion() throws Exception {
    // Startup and login to repository
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, "password", "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, "password", "", null );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    // Create a simple file
    RepositoryFile parentFolder = repo.getFile( ClientRepositoryPaths.getUserHomeFolderPath( USERNAME_SUZY ) );
    final String expectedDataString = "Hello World!";
    final String expectedModDataString = "Ciao World!";
    final String expectedEncoding = "UTF-8";
    byte[] data = expectedDataString.getBytes( expectedEncoding );
    byte[] modData = expectedModDataString.getBytes( expectedEncoding );
    ByteArrayInputStream dataStream = new ByteArrayInputStream( data );
    ByteArrayInputStream modDataStream = new ByteArrayInputStream( modData );
    final String expectedMimeType = "text/plain";
    final String expectedName = "helloworld.[~!@#$%^&*(){}|.,]-=_+|;'?<:>~.xaction";
    final String expectedAbsolutePath =
        ClientRepositoryPaths.getUserHomeFolderPath( USERNAME_SUZY ) + "/helloworld.[~!@#$%^&*(){}|.,]-=_+|;'?<:>~.xaction";

    final SimpleRepositoryFileData content =
        new SimpleRepositoryFileData( dataStream, expectedEncoding, expectedMimeType );
    RepositoryFile newFile =
        repo.createFile( parentFolder.getId(), new RepositoryFile.Builder( expectedName ).versioned( true ).build(),
            content, null );

    // Make sure the file was created
    RepositoryFile foundFile = repo.getFile( expectedAbsolutePath );
    assertNotNull( foundFile );

    // Modify file
    final SimpleRepositoryFileData modContent =
        new SimpleRepositoryFileData( modDataStream, expectedEncoding, expectedMimeType );
    repo.updateFile( foundFile, modContent, null );

    // Verify versions
    List<VersionSummary> origVerList = repo.getVersionSummaries( foundFile.getId() );
    assertEquals( 2, origVerList.size() );

    SimpleRepositoryFileData result =
        repo.getDataAtVersionForRead( foundFile.getId(), origVerList.get( 0 ).getId(), SimpleRepositoryFileData.class );
    SimpleRepositoryFileData modResult =
        repo.getDataAtVersionForRead( foundFile.getId(), origVerList.get( 1 ).getId(), SimpleRepositoryFileData.class );

    assertEquals( expectedDataString, IOUtils.toString( result.getStream(), expectedEncoding ) );
    assertEquals( expectedModDataString, IOUtils.toString( modResult.getStream(), expectedEncoding ) );

    // Remove first version
    repo.deleteFileAtVersion( foundFile.getId(), origVerList.get( 0 ).getId() );

    // Verify version removal
    List<VersionSummary> newVerList = repo.getVersionSummaries( foundFile.getId() );
    assertEquals( 1, newVerList.size() );

    SimpleRepositoryFileData newModResult =
        repo.getDataAtVersionForRead( foundFile.getId(), newVerList.get( 0 ).getId(), SimpleRepositoryFileData.class );

    assertEquals( expectedModDataString, IOUtils.toString( newModResult.getStream(), expectedEncoding ) );
  }

  @Test
  public void testRestoreFileAtVersion() throws Exception {
    // Startup and login to repository
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, "password", "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, "password", "", null );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    // Create a simple file
    RepositoryFile parentFolder = repo.getFile( ClientRepositoryPaths.getUserHomeFolderPath( USERNAME_SUZY ) );
    final String expectedDataString = "Hello World!";
    final String expectedModDataString = "Ciao World!";
    final String expectedEncoding = "UTF-8";
    byte[] data = expectedDataString.getBytes( expectedEncoding );
    byte[] modData = expectedModDataString.getBytes( expectedEncoding );
    ByteArrayInputStream dataStream = new ByteArrayInputStream( data );
    ByteArrayInputStream modDataStream = new ByteArrayInputStream( modData );
    final String expectedMimeType = "text/plain";
    final String expectedName = "helloworld.[~!@#$%^&*(){}|.,]-=_+|;'?<:>~.xaction";
    final String expectedAbsolutePath =
        ClientRepositoryPaths.getUserHomeFolderPath( USERNAME_SUZY ) + "/helloworld.[~!@#$%^&*(){}|.,]-=_+|;'?<:>~.xaction";

    final SimpleRepositoryFileData content =
        new SimpleRepositoryFileData( dataStream, expectedEncoding, expectedMimeType );
    RepositoryFile newFile =
        repo.createFile( parentFolder.getId(), new RepositoryFile.Builder( expectedName ).versioned( true ).build(),
            content, null );

    // Make sure the file was created
    RepositoryFile foundFile = repo.getFile( expectedAbsolutePath );
    assertNotNull( foundFile );

    // Modify file
    final SimpleRepositoryFileData modContent =
        new SimpleRepositoryFileData( modDataStream, expectedEncoding, expectedMimeType );
    repo.updateFile( foundFile, modContent, null );

    // Verify versions
    List<VersionSummary> origVerList = repo.getVersionSummaries( foundFile.getId() );
    assertEquals( 2, origVerList.size() );

    SimpleRepositoryFileData result =
        repo.getDataAtVersionForRead( foundFile.getId(), origVerList.get( 0 ).getId(), SimpleRepositoryFileData.class );
    SimpleRepositoryFileData modResult =
        repo.getDataAtVersionForRead( foundFile.getId(), origVerList.get( 1 ).getId(), SimpleRepositoryFileData.class );

    assertEquals( expectedDataString, IOUtils.toString( result.getStream(), expectedEncoding ) );
    assertEquals( expectedModDataString, IOUtils.toString( modResult.getStream(), expectedEncoding ) );

    // Restore first version
    repo.restoreFileAtVersion( foundFile.getId(), origVerList.get( 0 ).getId(), "restore version" );

    // Verify version restoration
    List<VersionSummary> newVerList = repo.getVersionSummaries( foundFile.getId() );
    assertEquals( 3, newVerList.size() );

    SimpleRepositoryFileData newOrigResult = repo.getDataForRead( foundFile.getId(), SimpleRepositoryFileData.class );

    assertEquals( expectedDataString, IOUtils.toString( newOrigResult.getStream(), expectedEncoding ) );
  }

  @Test
  public void testGetVersionSummaries() throws Exception {
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, "password", "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, "password", "", null );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    final String parentFolderPath =
        ClientRepositoryPaths.getUserHomeFolderPath( PentahoSessionHolder.getSession().getName() );
    RepositoryFile parentFolder = repo.getFile( parentFolderPath );
    final String dataString = "Hello World!";
    final String encoding = "UTF-8";
    byte[] data = dataString.getBytes( encoding );
    ByteArrayInputStream dataStream = new ByteArrayInputStream( data );
    final String mimeType = "text/plain";
    final String fileName = "helloworld.[~!@#$%^&*(){}|.,]-=_+|;'?<:>~.xaction";
    final SimpleRepositoryFileData content = new SimpleRepositoryFileData( dataStream, encoding, mimeType );
    RepositoryFile newFile =
        repo.createFile( parentFolder.getId(), new RepositoryFile.Builder( fileName ).versioned( true ).build(),
            content, "created helloworld.[~!@#$%^&*(){}|.,]-=_+|;'?<:>~.xaction" );
    repo.updateFile( newFile, content, "update 1" );
    newFile = repo.getFileById( newFile.getId() );
    repo.updateFile( newFile, content, "update 2" );
    newFile = repo.getFileById( newFile.getId() );
    RepositoryFile updatedFile = repo.updateFile( newFile, content, "update 3" );
    List<VersionSummary> versionSummaries = repo.getVersionSummaries( updatedFile.getId() );
    assertNotNull( versionSummaries );
    assertTrue( versionSummaries.size() >= 3 );
    assertEquals( "update 3", versionSummaries.get( versionSummaries.size() - 1 ).getMessage() );
    assertEquals( USERNAME_SUZY, versionSummaries.get( 0 ).getAuthor() );
    System.out.println( versionSummaries );
    System.out.println( versionSummaries.size() );
  }

  @Test
  public void testGetVersionSummary() throws Exception {
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, "password", "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, "password", "", null );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    final String parentFolderPath = ClientRepositoryPaths.getUserHomeFolderPath( USERNAME_SUZY );
    final String fileName = "helloworld.[~!@#$%^&*(){}|.,]-=_+|;'?<:>~.sample";

    final String origSampleString = "Hello World!";
    final boolean origSampleBoolean = false;
    final int origSampleInteger = 1024;

    RepositoryFile newFile =
        createSampleFile( parentFolderPath, fileName, origSampleString, origSampleBoolean, origSampleInteger, true );
    SampleRepositoryFileData newContent = repo.getDataForRead( newFile.getId(), SampleRepositoryFileData.class );

    VersionSummary v1 = repo.getVersionSummary( newFile.getId(), newFile.getVersionId() );
    assertNotNull( v1 );
    assertEquals( USERNAME_SUZY, v1.getAuthor() );
    assertEquals( new Date().getDate(), v1.getDate().getDate() );

    repo.updateFile( newFile, newContent, null );

    // gets last version summary
    VersionSummary v2 = repo.getVersionSummary( newFile.getId(), null );

    assertNotNull( v2 );
    assertEquals( USERNAME_SUZY, v2.getAuthor() );
    assertEquals( new Date().getDate(), v2.getDate().getDate() );
    assertFalse( v1.equals( v2 ) );
    List<VersionSummary> sums = repo.getVersionSummaries( newFile.getId() );
    // unfortunate impl issue that the 3rd version is the one that the user sees as the original file version
    assertEquals( sums.get( 0 ), v1 );
    assertEquals( sums.get( 1 ), v2 );
  }

  @Test
  public void testGetFileByVersionSummary() throws Exception {
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, "password", "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, "password", "", null );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    final String parentFolderPath = ClientRepositoryPaths.getUserHomeFolderPath( USERNAME_SUZY );
    final String fileName = "helloworld.[~!@#$%^&*(){}|.,]-=_+|;'?<:>~.sample";

    final String origSampleString = "Hello World!";
    final boolean origSampleBoolean = false;
    final int origSampleInteger = 1024;

    RepositoryFile newFile =
        createSampleFile( parentFolderPath, fileName, origSampleString, origSampleBoolean, origSampleInteger, true );
    final Serializable fileId = newFile.getId();
    final String absolutePath = newFile.getPath();

    final String modSampleString = "Ciao World!";
    final boolean modSampleBoolean = true;
    final int modSampleInteger = 2048;

    final SampleRepositoryFileData modData =
        new SampleRepositoryFileData( modSampleString, modSampleBoolean, modSampleInteger );

    RepositoryFile.Builder builder = new RepositoryFile.Builder( newFile );
    final String desc = "Hello World description";
    builder.description( RepositoryFile.DEFAULT_LOCALE, desc );
    repo.updateFile( builder.build(), modData, null );

    List<VersionSummary> versionSummaries = repo.getVersionSummaries( newFile.getId() );
    RepositoryFile v1 = repo.getFileAtVersion( newFile.getId(), versionSummaries.get( 0 ).getId() );
    RepositoryFile v2 = repo.getFileAtVersion( newFile.getId(), versionSummaries.get( 1 ).getId() );
    assertEquals( fileName, v1.getName() );
    assertEquals( fileName, v2.getName() );
    assertEquals( fileId, v1.getId() );
    assertEquals( fileId, v2.getId() );
    assertEquals( "1.0", v1.getVersionId() );
    assertEquals( "1.1", v2.getVersionId() );
    assertEquals( absolutePath, v1.getPath() );
    assertEquals( absolutePath, v2.getPath() );
    assertNull( v1.getDescription() );
    assertEquals( desc, v2.getDescription() );

    System.out.println( "or: " + newFile );
    System.out.println( "v1: " + v1 );
    System.out.println( "v2: " + v2 );
    SampleRepositoryFileData c1 =
        repo.getDataAtVersionForRead( v1.getId(), v1.getVersionId(), SampleRepositoryFileData.class );
    SampleRepositoryFileData c2 =
        repo.getDataAtVersionForRead( v2.getId(), v2.getVersionId(), SampleRepositoryFileData.class );
    assertEquals( origSampleString, c1.getSampleString() );
    assertEquals( origSampleBoolean, c1.getSampleBoolean() );
    assertEquals( origSampleInteger, c1.getSampleInteger() );
    assertEquals( modSampleString, c2.getSampleString() );
    assertEquals( modSampleBoolean, c2.getSampleBoolean() );
    assertEquals( modSampleInteger, c2.getSampleInteger() );
  }

  @Test
  @Ignore
  // Failing due to pho:aclManagement not present.
  public
  void testOwnership() throws Exception {
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, "password", "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, "password", "", null );
    userRoleDao.createUser( tenantAcme, USERNAME_TIFFANY, "password", "", null );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    RepositoryFile parentFolder =
        repo.getFile( ClientRepositoryPaths.getUserHomeFolderPath( PentahoSessionHolder.getSession().getName() ) );
    RepositoryFile newFolder = new RepositoryFile.Builder( "[~!@#$%^&*(){}|.,]-=_+|;'?<:>~test" ).folder( true ).versioned( true ).build();
    final String testFolderPath =
        ClientRepositoryPaths.getUserHomeFolderPath( PentahoSessionHolder.getSession().getName() )
            + RepositoryFile.SEPARATOR + "[~!@#$%^&*(){}|.,]-=_+|;'?<:>~test";
    newFolder = repo.createFolder( parentFolder.getId(), newFolder, null );
    assertEquals( new RepositoryFileSid( USERNAME_SUZY ), repo.getAcl( newFolder.getId() ).getOwner() );

    // set acl removing suzy's rights to this folder
    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    RepositoryFileAcl testFolderAcl = repo.getAcl( newFolder.getId() );
    RepositoryFileAcl newAcl =
        new Builder( testFolderAcl ).entriesInheriting( false ).clearAces().build();
    repo.updateAcl( newAcl );
    // but suzy is still the owner--she should be able to "acl" herself back into the folder
    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );
    assertNotNull( repo.getFile( testFolderPath ) );

    // as suzy, change owner to role to which she belongs
    testFolderAcl = repo.getAcl( newFolder.getId() );
    newAcl =
        new Builder( testFolderAcl ).owner(
            new RepositoryFileSid( roleNameUtils.getPrincipleId( tenantAcme, "Authenticated" ),
                Type.ROLE ) ).build();
    repo.updateAcl( newAcl );
    assertNotNull( repo.getFile( testFolderPath ) );
    login( USERNAME_TIFFANY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );
    assertNotNull( repo.getFile( testFolderPath ) );
  }

  @Test
  public void testGetAcl() throws Exception {
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, "password", "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, "password", "", null );
    userRoleDao.createUser( tenantAcme, USERNAME_TIFFANY, "password", "", null );

    defaultBackingRepositoryLifecycleManager.newTenant();
    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    RepositoryFile parentFolder =
        repo.getFile( ClientRepositoryPaths.getUserHomeFolderPath( PentahoSessionHolder.getSession().getName() ) );
    RepositoryFile newFolder = new RepositoryFile.Builder( "[~!@#$%^&*(){}|.,]-=_+|;'?<:>~test" ).folder( true ).versioned( true ).build();
    newFolder = repo.createFolder( parentFolder.getId(), newFolder, null );

    RepositoryFileAcl acl = repo.getAcl( newFolder.getId() );
    assertEquals( true, acl.isEntriesInheriting() );
    assertEquals( new RepositoryFileSid( USERNAME_SUZY ), acl.getOwner() );
    assertEquals( newFolder.getId(), acl.getId() );
    assertTrue( acl.getAces().isEmpty() );
    RepositoryFileAcl newAcl =
        new Builder( acl ).ace( userNameUtils.getPrincipleId( tenantAcme, USERNAME_TIFFANY ),
            Type.USER, RepositoryFilePermission.READ ).entriesInheriting( true ).build();
    RepositoryFileAcl fetchedAcl = repo.updateAcl( newAcl );
    // since isEntriesInheriting is true, ace addition should not have taken
    assertTrue( fetchedAcl.getAces().isEmpty() );
    newAcl =
        new Builder( acl ).ace( userNameUtils.getPrincipleId( tenantAcme, USERNAME_TIFFANY ),
            Type.USER, RepositoryFilePermission.READ ).build(); // calling ace sets
                                                                                  // entriesInheriting to false
    fetchedAcl = repo.updateAcl( newAcl );
    // since isEntriesInheriting is false, ace addition should have taken
    assertFalse( fetchedAcl.getAces().isEmpty() );
  }

  @Test
  public void testCreateFolderWithAcl() throws Exception {
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, "password", "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, "password", "", null );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    RepositoryFile parentFolder =
        repo.getFile( ClientRepositoryPaths.getUserHomeFolderPath( PentahoSessionHolder.getSession().getName() ) );
    RepositoryFile newFolder = new RepositoryFile.Builder( "[~!@#$%^&*(){}|.,]-=_+|;'?<:>~test" ).folder( true ).versioned( true ).build();
    RepositoryFileSid tiffanySid = new RepositoryFileSid(
      userNameUtils.getPrincipleId( tenantAcme, USERNAME_TIFFANY ) );
    RepositoryFileSid suzySid = new RepositoryFileSid( userNameUtils.getPrincipleId( tenantAcme, USERNAME_SUZY ) );
    // tiffany owns it but suzy is creating it
    Builder aclBuilder = new Builder( tiffanySid );
    // need this to be able to fetch acl as suzy
    aclBuilder.ace( suzySid, RepositoryFilePermission.READ );
    newFolder = repo.createFolder( parentFolder.getId(), newFolder, aclBuilder.build(), null );
    RepositoryFileAcl fetchedAcl = repo.getAcl( newFolder.getId() );
    assertEquals( new RepositoryFileSid( USERNAME_TIFFANY ), fetchedAcl.getOwner() );
    assertLocalAceExists( newFolder, new RepositoryFileSid( USERNAME_SUZY ),
      EnumSet.of( RepositoryFilePermission.READ ) );
  }

  @Test
  public void testWriteOnFileToMove() throws Exception {
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, "password", "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, "password", "", null );
    defaultBackingRepositoryLifecycleManager.newTenant();
    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    RepositoryFile parentFolder =
        repo.getFile( ClientRepositoryPaths.getUserHomeFolderPath( PentahoSessionHolder.getSession().getName() ) );
    RepositoryFile srcFolder = new RepositoryFile.Builder( "[~!@#$%^&*(){}|.,]-=_+|;'?<:>~src" ).folder( true ).build();
    RepositoryFile destFolder = new RepositoryFile.Builder( "[~!@#$%^&*(){}|.,]-=_+|;'?<:>~dest" ).folder( true ).build();
    srcFolder = repo.createFolder( parentFolder.getId(), srcFolder, null );
    destFolder = repo.createFolder( parentFolder.getId(), destFolder, null );

    RepositoryFile newFile = createSampleFile( srcFolder.getPath(), "helloworld.[~!@#$%^&*(){}|.,]-=_+|;'?<:>~.sample", "ddfdf", false, 83 );
    RepositoryFileAcl acl =
        new Builder( newFile.getId(), userNameUtils.getPrincipleId( tenantAcme, USERNAME_TIFFANY ),
            Type.USER ).entriesInheriting( false ).ace(
            userNameUtils.getPrincipleId( tenantAcme, USERNAME_SUZY ), Type.USER,
            RepositoryFilePermission.READ ).build();
    repo.updateAcl( acl );
    // at this point, suzy has write access to src and dest folders but only read access to actual file that will
    // be
    // moved; this should fail
    try {
      repo.moveFile( newFile.getId(), destFolder.getPath(), null );
      fail();
    } catch ( UnifiedRepositoryAccessDeniedException e ) {
      //ignore
    }
  }

  /**
   * Tests deleting a file when no delete permission is given to the role
   */
  @Test
  public void testDeleteWhenNoDeletePermissionOnFile() throws Exception {
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, "password", "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    defaultBackingRepositoryLifecycleManager.newTenant();
    RepositoryFile publicFolderFile =
        createSampleFile( repo.getFile(
            ClientRepositoryPaths.getUserHomeFolderPath( PentahoSessionHolder.getSession().getName() ) ).getPath(),
            "helloworld.[~!@#$%^&*(){}|.,]-=_+|;'?<:>~.sample", "ddfdf", false, 83 );
    RepositoryFileAcl publicFolderFileAcl =
        new Builder( publicFolderFile.getId(), userNameUtils.getPrincipleId( tenantAcme,
            USERNAME_ADMIN ), Type.USER ).entriesInheriting( false ).ace(
            new RepositoryFileSid( roleNameUtils.getPrincipleId( tenantAcme, tenantAuthenticatedRoleName ),
                Type.ROLE ), RepositoryFilePermission.READ, RepositoryFilePermission.WRITE ).build();
    repo.updateAcl( publicFolderFileAcl );

    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, "password", "", new String[] { tenantAuthenticatedRoleName } );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    try {
      repo.deleteFile( publicFolderFile.getId(), null );
      fail();
    } catch ( UnifiedRepositoryException e ) {
      assertNotNull( e );
    }

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );

    try {
      repo.deleteFile( publicFolderFile.getId(), null );
      assertTrue( true );
    } catch ( UnifiedRepositoryException e ) {
      fail();
    }
  }

  /**
   * Tests deleting a file when no delete permission is given to the role
   */
  @Test
  public void testWriteWhenNoWritePermissionOnFile() throws Exception {
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, "password", "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    defaultBackingRepositoryLifecycleManager.newTenant();
    RepositoryFile publicFolderFile =
        createSampleFile( repo.getFile(
            ClientRepositoryPaths.getUserHomeFolderPath( PentahoSessionHolder.getSession().getName() ) ).getPath(),
            "helloworld.[~!@#$%^&*(){}|.,]-=_+|;'?<:>~.sample", "ddfdf", false, 83 );
    RepositoryFileAcl publicFolderFileAcl =
        new Builder( publicFolderFile.getId(), userNameUtils.getPrincipleId( tenantAcme,
            USERNAME_ADMIN ), Type.USER ).entriesInheriting( false ).ace(
            new RepositoryFileSid( roleNameUtils.getPrincipleId( tenantAcme, tenantAuthenticatedRoleName ),
                Type.ROLE ), RepositoryFilePermission.READ ).build();
    repo.updateAcl( publicFolderFileAcl );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, "password", "", new String[] { tenantAuthenticatedRoleName } );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    final String modSampleString = "Ciao World!";
    final boolean modSampleBoolean = true;
    final int modSampleInteger = 99;

    final SampleRepositoryFileData modContent =
        new SampleRepositoryFileData( modSampleString, modSampleBoolean, modSampleInteger );

    try {

      repo.updateFile( publicFolderFile, modContent, null );
      fail();
    } catch ( UnifiedRepositoryException e ) {
      assertNotNull( e );
    }

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );

    try {
      repo.updateFile( publicFolderFile, modContent, null );
      assertTrue( true );
    } catch ( UnifiedRepositoryException e ) {
      fail();
    }
  }

  /**
   * Tests Updating the ACL when no GRANT_PERMISSION is assigned
   *
   */
  @Test
  public void testUpdatingPermissionWhenNoGrantPermissionOnFile() throws Exception {

    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, "password", "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, "password", "", new String[] { tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_TIFFANY, "password", ""
      , new String[] { tenantAuthenticatedRoleName } );

    defaultBackingRepositoryLifecycleManager.newTenant();

    RepositoryFile parentFolder =
        repo.getFile( ClientRepositoryPaths.getUserHomeFolderPath( PentahoSessionHolder.getSession().getName() ) );
    RepositoryFile newFolder = new RepositoryFile.Builder( "[~!@#$%^&*(){}|.,]-=_+|;'?<:>~test" ).folder( true ).versioned( true ).build();
    newFolder = repo.createFolder( parentFolder.getId(), newFolder, null );

    RepositoryFileAcl acls = repo.getAcl( newFolder.getId() );

    Builder newAclBuilder = new Builder( acls );
    newAclBuilder.entriesInheriting( false ).ace( userNameUtils.getPrincipleId( tenantAcme, USERNAME_TIFFANY ),
        Type.USER, RepositoryFilePermission.READ );
    repo.updateAcl( newAclBuilder.build() );

    login( USERNAME_TIFFANY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );
    RepositoryFileAcl newAcl = repo.getAcl( newFolder.getId() );

    Builder anotherNewAclBuilder = new Builder( newAcl );
    anotherNewAclBuilder.ace( new RepositoryFileSid( userNameUtils.getPrincipleId( tenantAcme,
        tenantAuthenticatedRoleName ), Type.ROLE ), RepositoryFilePermission.READ,
        RepositoryFilePermission.WRITE, RepositoryFilePermission.DELETE );

    try {
      repo.updateAcl( anotherNewAclBuilder.build() );
      fail();
    } catch ( UnifiedRepositoryException e ) {
      assertNotNull( e );
    }

  }

  @Test
  public void testMoveFile() throws Exception {
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, "password", "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, "password", "", null );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    RepositoryFile parentFolder =
        repo.getFile( ClientRepositoryPaths.getUserHomeFolderPath( PentahoSessionHolder.getSession().getName() ) );
    RepositoryFile moveTest1Folder = new RepositoryFile.Builder( "[~!@#$%^&*(){}|.,]-=_+|;'?<:>~moveTest1" ).folder( true ).versioned( true ).build();
    moveTest1Folder = repo.createFolder( parentFolder.getId(), moveTest1Folder, null );
    RepositoryFile moveTest2Folder = new RepositoryFile.Builder( "[~!@#$%^&*(){}|.,]-=_+|;'?<:>~moveTest2" ).folder( true ).versioned( true ).build();
    moveTest2Folder = repo.createFolder( parentFolder.getId(), moveTest2Folder, null );
    RepositoryFile testFolder = new RepositoryFile.Builder( "[~!@#$%^&*(){}|.,]-=_+|;'?<:>~test" ).folder( true ).build();
    testFolder = repo.createFolder( moveTest1Folder.getId(), testFolder, null );
    // move folder into new folder
    repo.moveFile( testFolder.getId(), moveTest2Folder.getPath(), null );
    assertNull( repo.getFile( ClientRepositoryPaths.getUserHomeFolderPath( PentahoSessionHolder.getSession().getName() )
        + RepositoryFile.SEPARATOR + "[~!@#$%^&*(){}|.,]-=_+|;'?<:>~moveTest1" + RepositoryFile.SEPARATOR + "[~!@#$%^&*(){}|.,]-=_+|;'?<:>~test" ) );
    assertNotNull( repo.getFile( ClientRepositoryPaths.getUserHomeFolderPath( PentahoSessionHolder.getSession()
        .getName() )
        + RepositoryFile.SEPARATOR + "[~!@#$%^&*(){}|.,]-=_+|;'?<:>~moveTest2" + RepositoryFile.SEPARATOR + "[~!@#$%^&*(){}|.,]-=_+|;'?<:>~test" ) );
    // rename within same folder
    repo.moveFile( testFolder.getId(), moveTest2Folder.getPath() + RepositoryFile.SEPARATOR + "[~!@#$%^&*(){}|.,]-=_+|;'?<:>~newTest", null );
    assertNull( repo.getFile( ClientRepositoryPaths.getUserHomeFolderPath( PentahoSessionHolder.getSession().getName() )
        + RepositoryFile.SEPARATOR + "[~!@#$%^&*(){}|.,]-=_+|;'?<:>~moveTest2" + RepositoryFile.SEPARATOR + "[~!@#$%^&*(){}|.,]-=_+|;'?<:>~test" ) );
    assertNotNull( repo.getFile( ClientRepositoryPaths.getUserHomeFolderPath( PentahoSessionHolder.getSession()
        .getName() )
        + RepositoryFile.SEPARATOR + "[~!@#$%^&*(){}|.,]-=_+|;'?<:>~moveTest2" + RepositoryFile.SEPARATOR + "[~!@#$%^&*(){}|.,]-=_+|;'?<:>~newTest" ) );

    RepositoryFile newFile = createSampleFile( moveTest2Folder.getPath(), "helloworld.[~!@#$%^&*(){}|.,]-=_+|;'?<:>~.sample", "ddfdf", false, 83 );
    try {
      repo.moveFile( testFolder.getId(), moveTest2Folder.getPath() + RepositoryFile.SEPARATOR + "doesnotexist"
          + RepositoryFile.SEPARATOR + "newTest2", null );
      fail();
    } catch ( UnifiedRepositoryException e ) {
      // moving a folder to a path with a non-existent parent folder is illegal
    }

    try {
      repo.moveFile( testFolder.getId(), newFile.getPath(), null );
      fail();
    } catch ( UnifiedRepositoryException e ) {
      // moving a folder to a file is illegal
    }
  }

  @Test
  public void testCopyRecursive() throws Exception {
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, "password", "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, "password", "", null );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    RepositoryFile parentFolder =
        repo.getFile( ClientRepositoryPaths.getUserHomeFolderPath( PentahoSessionHolder.getSession().getName() ) );
    RepositoryFile testFolder1 =
        repo.createFolder( parentFolder.getId(), new RepositoryFile.Builder( "[~!@#$%^&*(){}|.,]-=_+|;'?<:>~testfolder1" ).folder( true ).build(),
            null );
    RepositoryFile testFile1 = createSimpleFile( testFolder1.getId(), "[~!@#$%^&*(){}|.,]-=_+|;'?<:>~testfile1" );
    RepositoryFile testFolder2 =
        repo.createFolder( parentFolder.getId(), new RepositoryFile.Builder( "[~!@#$%^&*(){}|.,]-=_+|;'?<:>~testfolder2" ).folder( true ).build(),
            null );
    RepositoryFile testFile2 = createSimpleFile( testFolder2.getId(), "[~!@#$%^&*(){}|.,]-=_+|;'?<:>~testfile2" );
    repo.copyFile( testFolder1.getId(), testFolder2.getPath(), null );
    assertNotNull( repo.getFile( testFolder2.getPath() + RepositoryFile.SEPARATOR + "[~!@#$%^&*(){}|.,]-=_+|;'?<:>~testfile2" ) );
    assertNotNull( repo.getFile( testFolder2.getPath() + RepositoryFile.SEPARATOR + "[~!@#$%^&*(){}|.,]-=_+|;'?<:>~testfolder1" ) );
    assertNotNull( repo.getFile( testFolder2.getPath() + RepositoryFile.SEPARATOR + "[~!@#$%^&*(){}|.,]-=_+|;'?<:>~testfolder1"
        + RepositoryFile.SEPARATOR + "[~!@#$%^&*(){}|.,]-=_+|;'?<:>~testfile1" ) );
  }

  @Test
  public void testAdminCreate() throws Exception {
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, "password", "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, "password", "", null );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );
    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );

    final String expectedName = "helloworld.[~!@#$%^&*(){}|.,]-=_+|;'?<:>~.sample";
    final String sampleString = "Ciao World!";
    final boolean sampleBoolean = true;
    final int sampleInteger = 99;
    final String parentFolderPath = ClientRepositoryPaths.getUserHomeFolderPath( USERNAME_SUZY );
    RepositoryFile newFile =
        createSampleFile( parentFolderPath, expectedName, sampleString, sampleBoolean, sampleInteger );
    RepositoryFileAcl acls = repo.getAcl( newFile.getId() );

    Builder newAclBuilder = new Builder( acls );
    newAclBuilder.entriesInheriting( false ).ace( userNameUtils.getPrincipleId( tenantAcme, USERNAME_SUZY ),
        Type.USER, RepositoryFilePermission.ALL );
    repo.updateAcl( newAclBuilder.build() );

    // newFile = repo.getFile(newFile.getPath());
    JcrRepositoryDumpToFile dumpToFile =
        new JcrRepositoryDumpToFile( testJcrTemplate, jcrTransactionTemplate, repositoryAdminUsername,
            "dumpTestAdminCreate", Mode.CUSTOM );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    try {
      repo.deleteFile( newFile.getId(), null );
    } finally {
      dumpToFile.execute();
    }
  }

  @Test
  public void testGetTree() throws Exception {
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, "password", "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, "password", "", null );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    RepositoryFileTree root = repo.getTree( ClientRepositoryPaths.getRootFolderPath(), 0, null, true );
    assertNotNull( root.getFile() );
    assertNull( root.getChildren() );

    root = repo.getTree( ClientRepositoryPaths.getRootFolderPath(), 1, null, true );
    assertNotNull( root.getFile() );
    assertNotNull( root.getChildren() );
    assertFalse( root.getChildren().isEmpty() );
    assertNull( root.getChildren().get( 0 ).getChildren() );

    root = repo.getTree( ClientRepositoryPaths.getHomeFolderPath(), -1, null, true );
    assertNotNull( root.getFile() );
    assertNotNull( root.getChildren() );
    assertFalse( root.getChildren().isEmpty() );
    assertTrue( root.getChildren().get( 0 ).getChildren().isEmpty() );

    root = repo.getTree( ClientRepositoryPaths.getHomeFolderPath(), -1, "*uz*", true );
    assertEquals( 1, root.getChildren().size() );
  }

  @Test
  public void testGetTreeWithFileTypeFilter() throws Exception {
    RepositoryFileTree root = null;
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );

    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, "password", "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, "password", "", null );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    RepositoryFile publicFolder =
        repo.getFile( ClientRepositoryPaths.getUserHomeFolderPath( PentahoSessionHolder.getSession().getName() ) );
    final String dataString = "Hello World!";
    final String encoding = "UTF-8";
    byte[] data = dataString.getBytes( encoding );
    ByteArrayInputStream dataStream = new ByteArrayInputStream( data );
    final String mimeType = "text/plain";
    final SimpleRepositoryFileData content = new SimpleRepositoryFileData( dataStream, encoding, mimeType );
    RepositoryFile newFile1 =
        repo.createFile( publicFolder.getId(), new RepositoryFile.Builder( "helloworld.[~!@#$%^&*(){}|.,]-=_+|;'?<:>~.xaction" ).versioned( true )
            .hidden( false ).build(), content, null );

    RepositoryFile newFile2 =
        repo.createFolder( publicFolder.getId(), new RepositoryFile.Builder( "[~!@#$%^&*(){}|.,]-=_+|;'?<:>~testFolder" ).versioned( false ).hidden(
            false ).folder( true ).build(), null, null );

    root = repo.getTree( publicFolder.getPath(), 1, "*|FILES", true );
    assertFalse( root.getChildren().isEmpty() );
    assertEquals( 1, root.getChildren().size() );
    assertEquals( "helloworld.[~!@#$%^&*(){}|.,]-=_+|;'?<:>~.xaction", root.getChildren().get( 0 ).getFile().getName() );

    root = repo.getTree( publicFolder.getPath(), 1, "*", true );
    assertFalse( root.getChildren().isEmpty() );
    assertEquals( 2, root.getChildren().size() );

    root = repo.getTree( publicFolder.getPath(), 1, "*|FILES_FOLDERS", true );
    assertFalse( root.getChildren().isEmpty() );
    assertEquals( 2, root.getChildren().size() );

    root = repo.getTree( publicFolder.getPath(), 1, "*|FOLDERS", true );
    assertFalse( root.getChildren().isEmpty() );
    assertEquals( 1, root.getChildren().size() );
    assertEquals( "[~!@#$%^&*(){}|.,]-=_+|;'?<:>~testFolder", root.getChildren().get( 0 ).getFile().getName() );

  }

  @Test
  public void testGetTreeWithShowHidden() throws Exception {
    RepositoryFileTree root = null;
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, "password", "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, "password", "", null );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    RepositoryFile publicFolder =
        repo.getFile( ClientRepositoryPaths.getUserHomeFolderPath( PentahoSessionHolder.getSession().getName() ) );
    final String dataString = "Hello World!";
    final String encoding = "UTF-8";
    byte[] data = dataString.getBytes( encoding );
    ByteArrayInputStream dataStream = new ByteArrayInputStream( data );
    final String mimeType = "text/plain";
    final SimpleRepositoryFileData content = new SimpleRepositoryFileData( dataStream, encoding, mimeType );
    RepositoryFile newFile1 =
        repo.createFile( publicFolder.getId(), new RepositoryFile.Builder( "helloworld.[~!@#$%^&*(){}|.,]-=_+|;'?<:>~.xaction" ).versioned( true )
            .hidden( true ).build(), content, null );
    root = repo.getTree( publicFolder.getPath(), -1, null, true );
    assertFalse( root.getChildren().isEmpty() );
    root = repo.getTree( publicFolder.getPath(), -1, null, false );
    assertTrue( root.getChildren().isEmpty() );
  }

  @Test
  public void testGetDataForReadInBatch_versioned() throws Exception {
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, "password", "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, "password", "", null );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    final String parentFolderPath = ClientRepositoryPaths.getUserHomeFolderPath( USERNAME_SUZY );

    String sampleString1 = "sampleString1";
    String sampleString2 = "sampleString2";

    RepositoryFile newFile1 = createSampleFile( parentFolderPath, "helloworld.[~!@#$%^&*(){}|.,]-=_+|;'?<:>~.sample1", sampleString1, true, 1, true );
    RepositoryFile newFile2 = createSampleFile( parentFolderPath, "[~!@#$%^&*(){}|.,]-=_+|;'?<:>~file2", sampleString2, false, 2 );

    // Update newFile1 to create a new version
    SampleRepositoryFileData updatedContent = new SampleRepositoryFileData( sampleString1 + "mod", true, 1 );
    RepositoryFile modFile1 = repo.updateFile( newFile1, updatedContent, "New Version For Test" );

    assertNotNull( newFile1.getId() );
    assertTrue( newFile1.isVersioned() );
    assertNotNull( newFile2.getId() );
    assertFalse( newFile2.isVersioned() );
    assertNotNull( modFile1.getId() );
    assertTrue( modFile1.isVersioned() );

    // Check that no version provided returns latest
    RepositoryFile lookup1 = new RepositoryFile.Builder( newFile1.getId(), null ).build();
    RepositoryFile lookup2 = new RepositoryFile.Builder( newFile2.getId(), null ).build();

    List<SampleRepositoryFileData> data =
        repo.getDataForReadInBatch( Arrays.asList( lookup1, lookup2 ), SampleRepositoryFileData.class );
    assertEquals( 2, data.size() );
    SampleRepositoryFileData d = data.get( 0 );
    assertEquals( updatedContent.getSampleString(), d.getSampleString() );
    d = data.get( 1 );
    assertEquals( sampleString2, d.getSampleString() );

    // Check that providing a version will fetch it properly
    lookup1 = new RepositoryFile.Builder( newFile1.getId(), null ).versionId( newFile1.getVersionId() ).build();
    lookup2 = new RepositoryFile.Builder( newFile2.getId(), null ).versionId( newFile2.getVersionId() ).build();
    data = repo.getDataForReadInBatch( Arrays.asList( lookup1, lookup2 ), SampleRepositoryFileData.class );
    assertEquals( 2, data.size() );
    d = data.get( 0 );
    assertEquals( sampleString1, d.getSampleString() );
    d = data.get( 1 );
    assertEquals( sampleString2, d.getSampleString() );
  }

  @Test
  public void testMetadata() throws Exception {
    String key1 = "myMetadataString";
    String value1 = "wseyler";

    String key2 = "myMetadataBoolean";
    Boolean value2 = true;

    String key3 = "myMetadataDate";
    Calendar value3 = Calendar.getInstance();

    String key4 = "myMetadataDouble";
    Double value4 = 1234.378283293429;

    String key5 = "myMetadataLong";
    Long value5 = new Long( 12345768 );

    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, "password", "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, "password", "", null );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    final String parentFolderPath = ClientRepositoryPaths.getUserHomeFolderPath( USERNAME_SUZY );

    String sampleString1 = "sampleString1";

    RepositoryFile newFile1 = createSampleFile( parentFolderPath, "helloworld.[~!@#$%^&*(){}|.,]-=_+|;'?<:>~.sample1", sampleString1, true, 1, true );

    Map<String, Serializable> metadataMap = new HashMap<String, Serializable>();
    metadataMap.put( key1, value1 );
    repo.setFileMetadata( newFile1.getId(), metadataMap );
    Map<String, Serializable> savedMap = repo.getFileMetadata( newFile1.getId() );
    assertTrue( savedMap.containsKey( key1 ) );
    assertEquals( value1, savedMap.get( key1 ) );

    metadataMap.put( key2, value2 );
    repo.setFileMetadata( newFile1.getId(), metadataMap );
    savedMap = repo.getFileMetadata( newFile1.getId() );
    assertTrue( savedMap.containsKey( key2 ) );
    assertEquals( value2, savedMap.get( key2 ) );

    metadataMap.put( key3, value3 );
    repo.setFileMetadata( newFile1.getId(), metadataMap );
    savedMap = repo.getFileMetadata( newFile1.getId() );
    assertTrue( savedMap.containsKey( key3 ) );
    assertEquals( value3.getTime().getTime(), ( (Calendar) savedMap.get( key3 ) ).getTime().getTime() );

    metadataMap.put( key4, value4 );
    repo.setFileMetadata( newFile1.getId(), metadataMap );
    savedMap = repo.getFileMetadata( newFile1.getId() );
    assertTrue( savedMap.containsKey( key4 ) );
    assertEquals( value4, savedMap.get( key4 ) );

    metadataMap.put( key5, value5 );
    repo.setFileMetadata( newFile1.getId(), metadataMap );
    savedMap = repo.getFileMetadata( newFile1.getId() );
    assertTrue( savedMap.containsKey( key5 ) );
    assertEquals( value5, savedMap.get( key5 ) );
  }

  @Test
  public void testFileCreator() throws Exception {
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, "password", "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, "password", "", null );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    final String parentFolderPath = ClientRepositoryPaths.getUserHomeFolderPath( USERNAME_SUZY );

    String sampleString1 = "sampleString1";
    String sampleString2 = "sampleString2";

    RepositoryFile newFile1 = createSampleFile( parentFolderPath, "helloworld.[~!@#$%^&*(){}|.,]-=_+|;'?<:>~.sample1", sampleString1, true, 1, true );
    RepositoryFile newFile2 = createSampleFile( parentFolderPath, "helloworld.[~!@#$%^&*(){}|.,]-=_+|;'?<:>~.sample2", sampleString2, true, 1, true );

    RepositoryFile.Builder builder = new RepositoryFile.Builder( newFile1 );
    builder.creatorId( (String) newFile2.getId() );
    final String mimeType = "text/plain";
    final String dataString = "Hello World!";
    final String encoding = "UTF-8";
    byte[] data = dataString.getBytes( encoding );
    ByteArrayInputStream dataStream = new ByteArrayInputStream( data );

    final SimpleRepositoryFileData content = new SimpleRepositoryFileData( dataStream, encoding, mimeType );

    RepositoryFile updatedFile = repo.updateFile( builder.build(), content, null );
    RepositoryFile reconstituedFile = repo.getFileById( updatedFile.getId() );
    assertEquals( reconstituedFile.getCreatorId(), newFile2.getId() );
  }

  @Test
  public void testGetVersionSummaryInBatch() throws Exception {
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, "password", "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, "password", "", null );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    final String parentFolderPath =
        ClientRepositoryPaths.getUserHomeFolderPath( PentahoSessionHolder.getSession().getName() );
    RepositoryFile parentFolder = repo.getFile( parentFolderPath );
    final String dataString = "Hello World!";
    final String encoding = "UTF-8";
    byte[] data = dataString.getBytes( encoding );
    ByteArrayInputStream dataStream = new ByteArrayInputStream( data );
    final String mimeType = "text/plain";
    final String fileName1 = "helloworld1.[~!@#$%^&*(){}|.,]-=_+|;'?<:>~.xaction";
    final String fileName2 = "helloworld2.[~!@#$%^&*(){}|.,]-=_+|;'?<:>~.xaction";
    final SimpleRepositoryFileData content = new SimpleRepositoryFileData( dataStream, encoding, mimeType );
    RepositoryFile newFile1 =
        repo.createFile( parentFolder.getId(), new RepositoryFile.Builder( fileName1 ).versioned( true ).build(),
            content, "created helloworld.xaction" );
    final String createMsg = "created helloworld2.xaction";
    RepositoryFile newFile2 =
        repo.createFile( parentFolder.getId(), new RepositoryFile.Builder( fileName2 ).versioned( true ).build(),
            content, createMsg );
    final String updateMsg1 = "updating 1";
    newFile1 = repo.updateFile( newFile1, content, updateMsg1 );
    // Update file2 but don't save the info. We'll look up the original revision
    repo.updateFile( newFile2, content, "updating 2" );

    // Create a new file with just the Id set so we get the latest revision
    RepositoryFile lookup1 = new RepositoryFile.Builder( newFile1.getId(), null ).build();
    // Create a new file with the original version id and file id for file #2
    RepositoryFile lookup2 =
        new RepositoryFile.Builder( newFile2.getId(), null ).versionId( newFile2.getVersionId() ).build();
    List<VersionSummary> versionSummaries = repo.getVersionSummaryInBatch( Arrays.asList( lookup1, lookup2 ) );
    assertNotNull( versionSummaries );
    assertEquals( 2, versionSummaries.size() );
    VersionSummary summary = versionSummaries.get( 0 );
    // First version summary should be for the latest version of file1
    assertEquals( newFile1.getId(), summary.getVersionedFileId() );
    assertEquals( updateMsg1, summary.getMessage() );
    assertEquals( newFile1.getVersionId(), summary.getId() );
    summary = versionSummaries.get( 1 );
    // Second version summary should be for the first version of file2
    assertEquals( newFile2.getId(), summary.getVersionedFileId() );
    assertEquals( newFile2.getVersionId(), summary.getId() );
    assertEquals( createMsg, summary.getMessage() );
  }

  private RepositoryFile createSampleFile( final String parentFolderPath, final String fileName,
      final String sampleString, final boolean sampleBoolean, final int sampleInteger, boolean versioned )
    throws Exception {
    RepositoryFile parentFolder = repo.getFile( parentFolderPath );
    final SampleRepositoryFileData content = new SampleRepositoryFileData( sampleString, sampleBoolean, sampleInteger );
    return repo.createFile( parentFolder.getId(),
        new RepositoryFile.Builder( fileName ).versioned( versioned ).build(), content, null );
  }

  private RepositoryFile createSampleFile( final String parentFolderPath, final String fileName,
      final String sampleString, final boolean sampleBoolean, final int sampleInteger ) throws Exception {
    return createSampleFile( parentFolderPath, fileName, sampleString, sampleBoolean, sampleInteger, false );
  }

  private RepositoryFile createSimpleFile( final Serializable parentFolderId, final String fileName ) throws Exception {
    final String dataString = "Hello World!";
    final String encoding = "UTF-8";
    byte[] data = dataString.getBytes( encoding );
    ByteArrayInputStream dataStream = new ByteArrayInputStream( data );
    final SimpleRepositoryFileData content = new SimpleRepositoryFileData( dataStream, encoding, "text/plain" );
    return repo.createFile( parentFolderId, new RepositoryFile.Builder( fileName ).build(), content, null );
  }

  private void assertLocalAceExists( final RepositoryFile file, final RepositoryFileSid sid,
      final EnumSet<RepositoryFilePermission> permissions ) {
    RepositoryFileAcl acl = repo.getAcl( file.getId() );

    List<RepositoryFileAce> aces = acl.getAces();
    for ( int i = 0; i < aces.size(); i++ ) {
      RepositoryFileAce ace = aces.get( i );
      if ( sid.equals( ace.getSid() ) && permissions.equals( ace.getPermissions() ) ) {
        return;
      }
    }
    fail();
  }

  private void assertLocalAclEmpty( final RepositoryFile file ) {
    RepositoryFileAcl acl = repo.getAcl( file.getId() );
    assertTrue( acl.getAces().size() == 0 );
  }

  @Override
  public void setApplicationContext( final ApplicationContext applicationContext ) throws BeansException {
    SessionFactory jcrSessionFactory = (SessionFactory) applicationContext.getBean( "jcrSessionFactory" );
    testJcrTemplate = new JcrTemplate( jcrSessionFactory );
    testJcrTemplate.setAllowCreate( true );
    testJcrTemplate.setExposeNativeSession( true );

    jcrTemplate = (JcrTemplate) applicationContext.getBean("jcrTemplate");

    repositoryAdminUsername = (String) applicationContext.getBean( "repositoryAdminUsername" );
    superAdminRoleName = (String) applicationContext.getBean( "superAdminAuthorityName" );
    sysAdminUserName = (String) applicationContext.getBean( "superAdminUserName" );
    tenantAuthenticatedRoleName = (String) applicationContext.getBean( "singleTenantAuthenticatedAuthorityName" );
    tenantAdminRoleName = (String) applicationContext.getBean( "singleTenantAdminAuthorityName" );
    tenantManager = (ITenantManager) applicationContext.getBean( "tenantMgrProxy" );
    pathConversionHelper = (IPathConversionHelper) applicationContext.getBean( "pathConversionHelper" );
    roleBindingDao =
        (IRoleAuthorizationPolicyRoleBindingDao) applicationContext
            .getBean( "roleAuthorizationPolicyRoleBindingDaoTxn" );
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
    testUserRoleDao = userRoleDao;
    repositoryLifecyleManager =
        (IBackingRepositoryLifecycleManager) applicationContext.getBean( "defaultBackingRepositoryLifecycleManager" );
    txnTemplate = (TransactionTemplate) applicationContext.getBean( "jcrTransactionTemplate" );
    TestPrincipalProvider.userRoleDao = testUserRoleDao;
    TestPrincipalProvider.adminCredentialsStrategy =
        (CredentialsStrategy) applicationContext.getBean( "jcrAdminCredentialsStrategy" );
    TestPrincipalProvider.repository = (Repository) applicationContext.getBean( "jcrRepository" );
  }

  protected void loginAsSysTenantAdmin() {
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName } );
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

    createUserHomeFolder( tenant, username );
    defaultBackingRepositoryLifecycleManager.newTenant();
  }

  protected void logout() {
    PentahoSessionHolder.removeSession();
    SecurityContextHolder.getContext().setAuthentication( null );
  }

  protected void loginAsRepositoryAdmin() {
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

  protected ITenant getCurrentTenant() {
    if ( PentahoSessionHolder.getSession() != null ) {
      String tenantId = (String) PentahoSessionHolder.getSession().getAttribute( IPentahoSession.TENANT_ID_KEY );
      return tenantId != null ? new Tenant( tenantId, true ) : null;
    } else {
      return null;
    }
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

  protected String getPrincipalName( String principalId, boolean isUser ) {
    String principalName = null;
    ITenantedPrincipleNameResolver nameUtils = isUser ? userNameUtils : roleNameUtils;
    if ( nameUtils != null ) {
      principalName = nameUtils.getPrincipleName( principalId );
    }
    return principalName;
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
          Builder aclsForUserHomeFolder = null;
          Builder aclsForTenantHomeFolder = null;
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
              RepositoryFileSid ownerSid = new RepositoryFileSid( ownerId, Type.USER );

              String tenantAuthenticatedRoleId = roleNameUtils.getPrincipleId( theTenant, tenantAuthenticatedRoleName );
              RepositoryFileSid tenantAuthenticatedRoleSid =
                  new RepositoryFileSid( tenantAuthenticatedRoleId, Type.ROLE );

              aclsForTenantHomeFolder =
                  new Builder( userSid ).ace( tenantAuthenticatedRoleSid, EnumSet
                      .of( RepositoryFilePermission.READ ) );

              aclsForUserHomeFolder =
                  new Builder( userSid ).ace( ownerSid, EnumSet.of( RepositoryFilePermission.ALL ) );
              tenantHomeFolder =
                  repositoryFileDao.createFolder( tenantRootFolder.getId(), new RepositoryFile.Builder(
                      ServerRepositoryPaths.getTenantHomeFolderName() ).folder( true ).build(), aclsForTenantHomeFolder
                      .build(), "tenant home folder" );
            } else {
              String ownerId = userNameUtils.getPrincipleId( theTenant, username );
              RepositoryFileSid ownerSid = new RepositoryFileSid( ownerId, Type.USER );
              aclsForUserHomeFolder =
                  new Builder( userSid ).ace( ownerSid, EnumSet.of( RepositoryFilePermission.ALL ) );
            }

            // now check if user's home folder exist
            userHomeFolder =
                repositoryFileDao.getFileByAbsolutePath( ServerRepositoryPaths.getUserHomeFolderPath( theTenant,
                    username ) );
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

  @Test
  public void testDeleteUsersFolder() throws Exception {
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, "password", "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, "password", "", null );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    final String parentFolderPath =
        ClientRepositoryPaths.getUserHomeFolderPath( PentahoSessionHolder.getSession().getName() );
    RepositoryFile parentFolder = repo.getFile( parentFolderPath );
    final String dataString = "Hello World!";
    final String encoding = "UTF-8";
    byte[] data = dataString.getBytes( encoding );
    ByteArrayInputStream dataStream = new ByteArrayInputStream( data );
    final String mimeType = "text/plain";
    final String fileName = "helloworld.xaction";

    final SimpleRepositoryFileData content = new SimpleRepositoryFileData( dataStream, encoding, mimeType );
    RepositoryFile newFile =
        repo.createFile( parentFolder.getId(), new RepositoryFile.Builder( fileName ).build(), content, null );
    final String filePath = parentFolderPath + RepositoryFile.SEPARATOR + fileName;

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    try {
      repo.deleteFile( repo.getFile( parentFolderPath ).getId(), null );
    } catch ( Exception e ) {
      e.printStackTrace();
      fail();
    }

  }

  @Test
  public void testDeleteInheritingFolder() throws Exception {
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, "password", "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, "password", "", null );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    final String parentFolderPath =
        ClientRepositoryPaths.getUserHomeFolderPath( PentahoSessionHolder.getSession().getName() );
    RepositoryFile parentFolder = repo.getFile( parentFolderPath );
    final String dataString = "Hello World!";
    final String encoding = "UTF-8";
    byte[] data = dataString.getBytes( encoding );
    ByteArrayInputStream dataStream = new ByteArrayInputStream( data );
    final String mimeType = "text/plain";
    final String fileName = "helloworld.xaction";

    final SimpleRepositoryFileData content = new SimpleRepositoryFileData( dataStream, encoding, mimeType );

    // Try an inheriting folder delete
    //CHECKSTYLE IGNORE AvoidNestedBlocks FOR NEXT 3 LINES
    {
      RepositoryFile newFolder =
          repo.createFolder( parentFolder.getId(), new RepositoryFile.Builder( "testFolder" ).folder( true ).build(),
              null, null );

      RepositoryFileAcl acl = repo.getAcl( newFolder.getId() );

      RepositoryFileAcl newAcl =
          new Builder( acl ).ace( userNameUtils.getPrincipleId( tenantAcme, USERNAME_ADMIN ),
              Type.USER, RepositoryFilePermission.ALL ).entriesInheriting( true ).build();
      repo.updateAcl( newAcl );

      login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
      try {
        repo.deleteFile( newFolder.getId(), null );
      } catch ( Exception e ) {
        e.printStackTrace();
        fail();
      }
    }

    // Now try one not inheriting
    //CHECKSTYLE IGNORE AvoidNestedBlocks FOR NEXT 3 LINES
    {
      RepositoryFile newFolder =
          repo.createFolder( parentFolder.getId(), new RepositoryFile.Builder( "testFolder2" ).folder( true ).build(),
              null, null );

      RepositoryFileAcl acl = repo.getAcl( newFolder.getId() );

      RepositoryFileAcl newAcl =
          new Builder( acl ).clearAces().ace(
              userNameUtils.getPrincipleId( tenantAcme, USERNAME_ADMIN ), Type.USER,
              RepositoryFilePermission.ALL ).entriesInheriting( false ).build();
      repo.updateAcl( newAcl );

      login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
      try {
        repo.deleteFile( newFolder.getId(), null );
      } catch ( Exception e ) {
        e.printStackTrace();
        fail();
      }
    }
  }

  @Test
  public void testDeleteInheritingFile2() throws Exception {
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, "password", "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, "password", "", null );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    final String parentFolderPath =
        ClientRepositoryPaths.getUserHomeFolderPath( PentahoSessionHolder.getSession().getName() );
    RepositoryFile parentFolder = repo.getFile( parentFolderPath );
    final String dataString = "Hello World!";
    final String encoding = "UTF-8";
    byte[] data = dataString.getBytes( encoding );
    ByteArrayInputStream dataStream = new ByteArrayInputStream( data );
    final String mimeType = "text/plain";
    final String fileName = "helloworld.xaction";

    final SimpleRepositoryFileData content = new SimpleRepositoryFileData( dataStream, encoding, mimeType );

    RepositoryFile newFolder = null;
    // Try an inheriting file delete
    //CHECKSTYLE IGNORE AvoidNestedBlocks FOR NEXT 3 LINES
    {
      newFolder =
          repo.createFolder( parentFolder.getId(), new RepositoryFile.Builder( "testFolder" ).folder( true ).build(),
              null, null );

      RepositoryFile newFile =
          repo.createFile( newFolder.getId(), new RepositoryFile.Builder( "testFile" ).folder( false ).build(),
              content, null );

      RepositoryFileAcl acl = repo.getAcl( newFile.getId() );

      RepositoryFileAcl newAcl =
          new Builder( acl ).ace( userNameUtils.getPrincipleId( tenantAcme, USERNAME_ADMIN ),
              Type.USER, RepositoryFilePermission.ALL ).entriesInheriting( true ).build();
      repo.updateAcl( newAcl );

      login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
      try {
        repo.deleteFile( newFile.getId(), null );
      } catch ( Exception e ) {
        e.printStackTrace();
        fail();
      }
    }

    // Now try one not inheriting
    //CHECKSTYLE IGNORE AvoidNestedBlocks FOR NEXT 3 LINES
    {

      RepositoryFile newFile =
          repo.createFile( newFolder.getId(), new RepositoryFile.Builder( "testFile" ).folder( false ).build(),
              content, null );

      RepositoryFileAcl acl = repo.getAcl( newFile.getId() );

      RepositoryFileAcl newAcl =
          new Builder( acl ).ace( userNameUtils.getPrincipleId( tenantAcme, USERNAME_ADMIN ),
              Type.USER, RepositoryFilePermission.ALL ).entriesInheriting( false ).build();
      repo.updateAcl( newAcl );

      login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
      try {
        repo.deleteFile( newFile.getId(), null );
      } catch ( Exception e ) {
        e.printStackTrace();
        fail();
      }
    }
  }

  @Test
  public void testInheritingNodeRemoval() throws Exception {
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, "password", "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, "password", "", null );

    final String parentFolderPath = ClientRepositoryPaths.getPublicFolderPath();
    RepositoryFile parentFolder = repo.getFile( parentFolderPath );

    DataNode node = new DataNode( "kdjd" );
    node.setProperty( "ddf", "ljsdfkjsdkf" );
    DataNode newChild1 = node.addNode( "herfkmdx" );

    NodeRepositoryFileData data = new NodeRepositoryFileData( node );
    RepositoryFile repoFile =
        repo.createFile( parentFolder.getId(), new RepositoryFile.Builder( "test" ).build(), data, null );
    RepositoryFileAcl acl = repo.getAcl( repoFile.getId() );

    RepositoryFileSid suzySid = new RepositoryFileSid( userNameUtils.getPrincipleId( tenantAcme, USERNAME_SUZY ) );
    Builder newAclBuilder =
        new Builder( acl ).ace( suzySid, EnumSet.of( RepositoryFilePermission.READ,
            RepositoryFilePermission.WRITE ) );

    repo.updateAcl( newAclBuilder.build() );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );
    repoFile = repo.getFile( repoFile.getPath() );

    node = new DataNode( "kdjd" );
    node.setProperty( "foo", "bar" );
    newChild1 = node.addNode( "sdfsdf" );

    data = new NodeRepositoryFileData( node );
    repo.updateFile( repoFile, data, "testUpdate" );

  }

}
