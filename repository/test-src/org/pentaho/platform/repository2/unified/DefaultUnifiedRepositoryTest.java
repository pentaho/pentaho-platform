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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.jcr.security.AccessControlException;
import javax.jcr.security.Privilege;

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
import org.pentaho.platform.api.locale.IPentahoLocale;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.mt.ITenantManager;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.pentaho.platform.api.repository2.unified.IBackingRepositoryLifecycleManager;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAce;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl.Builder;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid.Type;
import org.pentaho.platform.api.repository2.unified.RepositoryFileTree;
import org.pentaho.platform.api.repository2.unified.UnifiedRepositoryAccessDeniedException;
import org.pentaho.platform.api.repository2.unified.UnifiedRepositoryException;
import org.pentaho.platform.api.repository2.unified.UnifiedRepositoryMalformedNameException;
import org.pentaho.platform.api.repository2.unified.VersionSummary;
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
import org.pentaho.platform.repository2.unified.jcr.DefaultLockHelper;
import org.pentaho.platform.repository2.unified.jcr.IPathConversionHelper;
import org.pentaho.platform.repository2.unified.jcr.JcrRepositoryDumpToFile;
import org.pentaho.platform.repository2.unified.jcr.JcrRepositoryDumpToFile.Mode;
import org.pentaho.platform.repository2.unified.jcr.JcrTenantUtils;
import org.pentaho.platform.repository2.unified.jcr.PentahoJcrConstants;
import org.pentaho.platform.repository2.unified.jcr.RepositoryFileProxyFactory;
import org.pentaho.platform.repository2.unified.jcr.SimpleJcrTestUtils;
import org.pentaho.platform.repository2.unified.jcr.jackrabbit.security.TestPrincipalProvider;
import org.pentaho.platform.repository2.unified.jcr.sejcr.CredentialsStrategy;
import org.pentaho.platform.security.policy.rolebased.IRoleAuthorizationPolicyRoleBindingDao;
import org.pentaho.platform.security.policy.rolebased.RoleBindingStruct;
import org.pentaho.platform.security.policy.rolebased.actions.AdministerSecurityAction;
import org.pentaho.platform.security.policy.rolebased.actions.PublishAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryCreateAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryReadAction;
import org.pentaho.platform.security.policy.rolebased.actions.SchedulerAction;
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

/**
 * Integration test. Tests {@link DefaultUnifiedRepository} and {@link IAuthorizationPolicy} fully configured
 * behind Spring Security's method security and Spring's transaction interceptor.
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
@Ignore
@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration( locations = { "classpath:/repository.spring.xml",
    "classpath:/repository-test-override.spring.xml" } )
@SuppressWarnings( "nls" )
public class DefaultUnifiedRepositoryTest implements ApplicationContextAware {
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

  public DefaultUnifiedRepositoryTest() throws Exception {
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
    final String fileName = "helloworld.sample";
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
            + "helloworld.sample", true );

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
    final String fileName = "locale.sample";
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
    final String fileName = "locale.sample";
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
   * This test method depends on {@code DefaultRepositoryEventHandler} behavior.
   */
  @Test
  public void testOnNewUser() throws Exception {
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, "password", "", new String[] { tenantAdminRoleName } );
    logout();

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );

    RepositoryFile suzyHomeFolder = repo.getFile( ClientRepositoryPaths.getUserHomeFolderPath( USERNAME_SUZY ) );
    assertNotNull( suzyHomeFolder );
    assertNotNull( SimpleJcrTestUtils.getItem( testJcrTemplate, ServerRepositoryPaths.getTenantRootFolderPath() ) );
    assertNotNull( SimpleJcrTestUtils.getItem( testJcrTemplate, ServerRepositoryPaths.getTenantPublicFolderPath() ) );
    assertNotNull( SimpleJcrTestUtils.getItem( testJcrTemplate, ServerRepositoryPaths.getTenantHomeFolderPath() ) );
    final String suzyFolderPath = ServerRepositoryPaths.getUserHomeFolderPath( tenantAcme, USERNAME_SUZY );
    assertNotNull( SimpleJcrTestUtils.getItem( testJcrTemplate, suzyFolderPath ) );
  }

  /**
   * This test method depends on {@code DefaultBackingRepositoryLifecycleManager} behavior.
   */
  @Test
  public void testAclsOnDefaultFolders() throws Exception {
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, "password", "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, "password", "", null );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    final RepositoryFileSid suzySid = new RepositoryFileSid( USERNAME_SUZY, RepositoryFileSid.Type.USER );
    final RepositoryFileSid acmeAuthenticatedAuthoritySid =
        new RepositoryFileSid( tenantAuthenticatedRoleName, RepositoryFileSid.Type.ROLE );
    final RepositoryFileSid sysAdminSid = new RepositoryFileSid( sysAdminUserName, RepositoryFileSid.Type.USER );
    final RepositoryFileSid tenantAdminSid = new RepositoryFileSid( USERNAME_ADMIN, RepositoryFileSid.Type.USER );
    final RepositoryFileSid tenantCreatorSid = new RepositoryFileSid( sysAdminUserName, RepositoryFileSid.Type.USER );

    RepositoryFile file = tenantManager.getTenantRootFolder( tenantAcme );
    String tenantRootFolderAbsPath = pathConversionHelper.relToAbs( file.getPath() );
    // pentaho root folder
    assertTrue( SimpleJcrTestUtils.hasPrivileges( testJcrTemplate, tenantRootFolderAbsPath, Privilege.JCR_READ ) );
    // TODO mlowery possible issue
    assertTrue( SimpleJcrTestUtils.hasPrivileges( testJcrTemplate, tenantRootFolderAbsPath,
        Privilege.JCR_READ_ACCESS_CONTROL ) );

    assertFalse( SimpleJcrTestUtils.hasPrivileges( testJcrTemplate, tenantRootFolderAbsPath, Privilege.JCR_WRITE ) );
    // TODO mlowery possible issue
    assertFalse( SimpleJcrTestUtils.hasPrivileges( testJcrTemplate, tenantRootFolderAbsPath,
        Privilege.JCR_MODIFY_ACCESS_CONTROL ) );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    assertTrue( SimpleJcrTestUtils.hasPrivileges( testJcrTemplate, tenantRootFolderAbsPath, Privilege.JCR_READ ) );
    assertTrue( SimpleJcrTestUtils.hasPrivileges( testJcrTemplate, tenantRootFolderAbsPath,
        Privilege.JCR_READ_ACCESS_CONTROL ) );
    assertTrue( SimpleJcrTestUtils.hasPrivileges( testJcrTemplate, tenantRootFolderAbsPath, Privilege.JCR_WRITE ) );
    assertTrue( SimpleJcrTestUtils.hasPrivileges( testJcrTemplate, tenantRootFolderAbsPath,
        Privilege.JCR_MODIFY_ACCESS_CONTROL ) );
    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    // tenant root folder
    // there is no ace that gives authenticated acme users access to /pentaho/acme; it's in logic on the server
    assertFalse( repo.getAcl( repo.getFile( ClientRepositoryPaths.getRootFolderPath() )
      .getId() ).isEntriesInheriting() );
    assertEquals( tenantCreatorSid, repo.getAcl( repo.getFile( ClientRepositoryPaths.getRootFolderPath() ).getId() )
        .getOwner() );
    assertTrue( SimpleJcrTestUtils.hasPrivileges( testJcrTemplate, ServerRepositoryPaths.getTenantRootFolderPath(),
        Privilege.JCR_READ ) );
    assertTrue( SimpleJcrTestUtils.hasPrivileges( testJcrTemplate, ServerRepositoryPaths.getTenantRootFolderPath(),
        Privilege.JCR_READ_ACCESS_CONTROL ) );

    assertFalse( repo.getAcl( repo.getFile( ClientRepositoryPaths.getPublicFolderPath() ).getId() )
        .isEntriesInheriting() );

    // tenant public folder
    assertLocalAceExists( repo.getFile( ClientRepositoryPaths.getPublicFolderPath() ), acmeAuthenticatedAuthoritySid,
        EnumSet.of( RepositoryFilePermission.READ ) );
    assertEquals( tenantCreatorSid, repo.getAcl( repo.getFile( ClientRepositoryPaths.getPublicFolderPath() ).getId() )
        .getOwner() );
    assertTrue( SimpleJcrTestUtils.hasPrivileges( testJcrTemplate, ServerRepositoryPaths.getTenantPublicFolderPath(),
        Privilege.JCR_READ ) );
    assertTrue( SimpleJcrTestUtils.hasPrivileges( testJcrTemplate, ServerRepositoryPaths.getTenantPublicFolderPath(),
        Privilege.JCR_READ_ACCESS_CONTROL ) );

    // tenant home folder
    assertFalse( repo.getAcl( repo.getFile( ClientRepositoryPaths.getHomeFolderPath() )
      .getId() ).isEntriesInheriting() );
    assertLocalAceExists( repo.getFile( ClientRepositoryPaths.getHomeFolderPath() ), acmeAuthenticatedAuthoritySid,
        EnumSet.of( RepositoryFilePermission.READ ) );
    assertEquals( tenantCreatorSid, repo.getAcl( repo.getFile( ClientRepositoryPaths.getHomeFolderPath() ).getId() )
        .getOwner() );
    assertTrue( SimpleJcrTestUtils.hasPrivileges( testJcrTemplate, ServerRepositoryPaths.getTenantHomeFolderPath(),
        Privilege.JCR_READ ) );
    assertTrue( SimpleJcrTestUtils.hasPrivileges( testJcrTemplate, ServerRepositoryPaths.getTenantHomeFolderPath(),
        Privilege.JCR_READ_ACCESS_CONTROL ) );

    Serializable fileId = repo.getFile( ClientRepositoryPaths.getEtcFolderPath() ).getId();
    assertLocalAceExists( repo.getFile( ClientRepositoryPaths.getEtcFolderPath() ), acmeAuthenticatedAuthoritySid,
        EnumSet.of( RepositoryFilePermission.READ ) );
    assertEquals( tenantCreatorSid, repo.getAcl( repo.getFile( ClientRepositoryPaths.getEtcFolderPath() ).getId() )
        .getOwner() );
    assertTrue( SimpleJcrTestUtils.hasPrivileges( testJcrTemplate, ServerRepositoryPaths.getTenantEtcFolderPath(),
        Privilege.JCR_READ ) );
    assertTrue( SimpleJcrTestUtils.hasPrivileges( testJcrTemplate, ServerRepositoryPaths.getTenantEtcFolderPath(),
        Privilege.JCR_READ_ACCESS_CONTROL ) );

    // suzy home folder
    assertEquals( suzySid, repo.getAcl(
        repo.getFile( ClientRepositoryPaths.getUserHomeFolderPath( USERNAME_SUZY ) ).getId() ).getOwner() );
    assertTrue( SimpleJcrTestUtils.hasPrivileges( testJcrTemplate, ServerRepositoryPaths.getUserHomeFolderPath(
        tenantAcme, USERNAME_SUZY ), Privilege.JCR_ALL ) );

    // tenant etc/pdi folder
    final String pdiPath = ClientRepositoryPaths.getEtcFolderPath() + RepositoryFile.SEPARATOR + "pdi";
    assertTrue( repo.getAcl( repo.getFile( pdiPath ).getId() ).isEntriesInheriting() );
    assertLocalAclEmpty( repo.getFile( pdiPath ) );
    assertEquals( tenantCreatorSid, repo.getAcl( repo.getFile( pdiPath ).getId() ).getOwner() );

    // tenant etc/databases folder
    final String databasesPath = pdiPath + RepositoryFile.SEPARATOR + "databases";
    assertTrue( repo.getAcl( repo.getFile( databasesPath ).getId() ).isEntriesInheriting() );
    assertLocalAclEmpty( repo.getFile( databasesPath ) );
    assertEquals( tenantCreatorSid, repo.getAcl( repo.getFile( databasesPath ).getId() ).getOwner() );

    // tenant etc/slaveServers folder
    final String slaveServersPath = pdiPath + RepositoryFile.SEPARATOR + "slaveServers";
    assertTrue( repo.getAcl( repo.getFile( slaveServersPath ).getId() ).isEntriesInheriting() );
    assertLocalAclEmpty( repo.getFile( slaveServersPath ) );
    assertEquals( tenantCreatorSid, repo.getAcl( repo.getFile( slaveServersPath ).getId() ).getOwner() );

    // tenant etc/clusterSchemas folder
    final String clusterSchemasPath = pdiPath + RepositoryFile.SEPARATOR + "clusterSchemas";
    assertTrue( repo.getAcl( repo.getFile( clusterSchemasPath ).getId() ).isEntriesInheriting() );
    assertLocalAclEmpty( repo.getFile( clusterSchemasPath ) );
    assertEquals( tenantCreatorSid, repo.getAcl( repo.getFile( clusterSchemasPath ).getId() ).getOwner() );

    // tenant etc/partitionSchemas folder
    final String partitionSchemasPath = pdiPath + RepositoryFile.SEPARATOR + "partitionSchemas";
    assertTrue( repo.getAcl( repo.getFile( partitionSchemasPath ).getId() ).isEntriesInheriting() );
    assertLocalAclEmpty( repo.getFile( partitionSchemasPath ) );
    assertEquals( tenantCreatorSid, repo.getAcl( repo.getFile( partitionSchemasPath ).getId() ).getOwner() );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    assertTrue( SimpleJcrTestUtils.hasPrivileges( testJcrTemplate, ServerRepositoryPaths.getUserHomeFolderPath(
        tenantAcme, USERNAME_SUZY ), Privilege.JCR_WRITE ) );

    assertLocalAceExists( repo.getFile( ClientRepositoryPaths.getPublicFolderPath() ), acmeAuthenticatedAuthoritySid,
        EnumSet.of( RepositoryFilePermission.READ ) );

    // Test admin access ot tenant public folder
    assertEquals( tenantCreatorSid, repo.getAcl( repo.getFile( ClientRepositoryPaths.getPublicFolderPath() ).getId() )
        .getOwner() );
    assertTrue( SimpleJcrTestUtils.hasPrivileges( testJcrTemplate, ServerRepositoryPaths.getTenantPublicFolderPath(),
        Privilege.JCR_READ ) );
    assertTrue( SimpleJcrTestUtils.hasPrivileges( testJcrTemplate, ServerRepositoryPaths.getTenantPublicFolderPath(),
        Privilege.JCR_READ_ACCESS_CONTROL ) );
    assertTrue( SimpleJcrTestUtils.hasPrivileges( testJcrTemplate, ServerRepositoryPaths.getTenantPublicFolderPath(),
        Privilege.JCR_ADD_CHILD_NODES ) );
    assertTrue( SimpleJcrTestUtils.hasPrivileges( testJcrTemplate, ServerRepositoryPaths.getTenantPublicFolderPath(),
        Privilege.JCR_MODIFY_PROPERTIES ) );
    assertTrue( SimpleJcrTestUtils.hasPrivileges( testJcrTemplate, ServerRepositoryPaths.getTenantPublicFolderPath(),
        Privilege.JCR_NODE_TYPE_MANAGEMENT ) );
    assertTrue( SimpleJcrTestUtils.hasPrivileges( testJcrTemplate, ServerRepositoryPaths.getTenantPublicFolderPath(),
        Privilege.JCR_MODIFY_ACCESS_CONTROL ) );

  }

  @Test
  public void testGetFileAccessDenied() throws Exception {
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, "password", "", new String[] { tenantAdminRoleName } );
    ITenant tenantDuff =
        tenantManager.createTenant( systemTenant, TENANT_ID_DUFF, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantDuff, USERNAME_ADMIN, "password", "", new String[] { tenantAdminRoleName } );
    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, "password", "", null );
    userRoleDao.createUser( tenantAcme, USERNAME_TIFFANY, "password", "", null );

    login( USERNAME_ADMIN, tenantDuff, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantDuff, USERNAME_PAT, "password", "", null );

    login( USERNAME_TIFFANY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );
    RepositoryFile tiffanyHomeFolder = repo.getFile( ClientRepositoryPaths.getUserHomeFolderPath( USERNAME_TIFFANY ) );
    assertNotNull( tiffanyHomeFolder );
    assertNotNull( repo.createFolder( tiffanyHomeFolder.getId(), new RepositoryFile.Builder( "test" ).folder( true )
        .build(), null ) );
    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );
    final String acmeTenantRootFolderPath = ClientRepositoryPaths.getRootFolderPath();
    final String homeFolderPath = ClientRepositoryPaths.getHomeFolderPath();
    final String tiffanyFolderPath = homeFolderPath + "/tiffany";
    JcrRepositoryDumpToFile dumpToFile =
        new JcrRepositoryDumpToFile( testJcrTemplate, jcrTransactionTemplate, repositoryAdminUsername,
            "c:/build/testrepo_7", Mode.CUSTOM );
    // dumpToFile.execute();
    // read access for suzy on home
    assertNotNull( repo.getFile( homeFolderPath ) );
    // no read access for suzy on tiffany's folder
    assertNull( repo.getFile( tiffanyFolderPath ) );
    // no read access for suzy on subfolder of tiffany's folder
    final String tiffanySubFolderPath = tiffanyFolderPath + "/test";
    assertNull( repo.getFile( tiffanySubFolderPath ) );
    // make sure Pat can't see acme folder (pat is in the duff tenant)
    login( USERNAME_PAT, tenantDuff, new String[] { tenantAuthenticatedRoleName } );
    assertNull( SimpleJcrTestUtils
        .getItem( testJcrTemplate, ServerRepositoryPaths.getTenantRootFolderPath( tenantAcme ) ) );
    assertFalse( SimpleJcrTestUtils.hasPrivileges( testJcrTemplate, ServerRepositoryPaths
        .getTenantRootFolderPath( tenantAcme ), Privilege.JCR_READ ) );
    assertFalse( SimpleJcrTestUtils.hasPrivileges( testJcrTemplate, ServerRepositoryPaths
        .getTenantRootFolderPath( tenantAcme ), Privilege.JCR_READ_ACCESS_CONTROL ) );
  }

  @Test
  public void testGetFileAdmin() throws Exception {
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, "password", "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_TIFFANY, "password", "", null );

    login( USERNAME_TIFFANY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    RepositoryFile tiffanyHomeFolder = repo.getFile( ClientRepositoryPaths.getUserHomeFolderPath( USERNAME_TIFFANY ) );
    repo.createFolder( tiffanyHomeFolder.getId(), new RepositoryFile.Builder( "test" ).folder( true ).build(), null );
    RepositoryFileAcl acl = repo.getAcl( tiffanyHomeFolder.getId() );
    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    assertNotNull( repo.getFile( ClientRepositoryPaths.getUserHomeFolderPath( USERNAME_TIFFANY ) ) );
    assertNotNull( repo.getFile( ClientRepositoryPaths.getUserHomeFolderPath( USERNAME_TIFFANY ) + "/test" ) );
  }

  @Test
  public void testStopThenStartInheriting() throws Exception {
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, "password", "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_TIFFANY, "password", "", null );

    login( USERNAME_TIFFANY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    RepositoryFile tiffanyHomeFolder = repo.getFile( ClientRepositoryPaths.getUserHomeFolderPath( USERNAME_TIFFANY ) );
    RepositoryFile testFolder =
        repo.createFolder( tiffanyHomeFolder.getId(), new RepositoryFile.Builder( "test" )
          .folder( true ).build(), null );
    RepositoryFileAcl acl = repo.getAcl( testFolder.getId() );
    RepositoryFileAcl updatedAcl = new RepositoryFileAcl.Builder( acl ).entriesInheriting( false ).build();
    updatedAcl = repo.updateAcl( updatedAcl );
    assertFalse( updatedAcl.isEntriesInheriting() );
    updatedAcl = new RepositoryFileAcl.Builder( updatedAcl ).entriesInheriting( true ).build();
    updatedAcl = repo.updateAcl( updatedAcl );
    assertTrue( updatedAcl.isEntriesInheriting() );
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

    final String fileName = "helloworld.sample";
    RepositoryFile newFile =
        createSampleFile( ClientRepositoryPaths.getUserHomeFolderPath( USERNAME_SUZY ), fileName, "blah", false, 123,
            true );
    assertEquals( 1, repo.getVersionSummaries( newFile.getId() ).size() );
    RepositoryFileAcl acl = repo.getAcl( newFile.getId() );
    // no change; just want to create a new version
    RepositoryFileAcl updatedAcl = new RepositoryFileAcl.Builder( acl ).build();
    updatedAcl = repo.updateAcl( updatedAcl );
    assertEquals( 2, repo.getVersionSummaries( newFile.getId() ).size() );
    assertNotNull( repo.getVersionSummary( newFile.getId(), "1.1" ) );
  }

  @Test
  public void testGetFileNotExist() throws Exception {
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, "password", "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_TIFFANY, "password", "", null );

    login( USERNAME_TIFFANY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    RepositoryFile file2 = repo.getFile( "/doesnotexist" );
    assertNull( file2 );
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
    RepositoryFile newFolder = new RepositoryFile.Builder( "test" ).folder( true ).hidden( true ).build();

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
        + "/test" ) );
  }

  @Test
  public void testCreateFolderWithAtSymbol() throws Exception {
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, "password", "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, "password", "", null );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    RepositoryFile parentFolder = repo.getFile( ClientRepositoryPaths.getUserHomeFolderPath( USERNAME_SUZY ) );
    RepositoryFile newFolder = new RepositoryFile.Builder( "me@example.com" ).folder( true ).build();
    newFolder = repo.createFolder( parentFolder.getId(), newFolder, null );
    assertNotNull( newFolder );
    assertNotNull( repo.getFile( ClientRepositoryPaths.getUserHomeFolderPath( USERNAME_SUZY ) + "/me@example.com" ) );
    assertEquals( "me@example.com", repo.getFile(
        ClientRepositoryPaths.getUserHomeFolderPath( USERNAME_SUZY ) + "/me@example.com" ).getName() );
  }

  @Test( expected = UnifiedRepositoryAccessDeniedException.class )
  public void testCreateFolderAccessDenied() throws Exception {
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, "password", "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, "password", "", null );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    RepositoryFile parentFolder = repo.getFile( ClientRepositoryPaths.getRootFolderPath() );
    RepositoryFile newFolder = new RepositoryFile.Builder( "test" ).folder( true ).build();
    repo.createFolder( parentFolder.getId(), newFolder, null );
  }

  @Test( expected = UnifiedRepositoryException.class )
  public void testCreateFolderAtRootIllegal() throws Exception {
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, "password", "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, "password", "", null );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    RepositoryFile newFolder = new RepositoryFile.Builder( "test" ).folder( true ).build();
    repo.createFolder( null, newFolder, null );
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
    final String fileName = "helloworld.xaction";
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
    final String expectedName = "helloworld.xaction";
    final String expectedAbsolutePath =
        ClientRepositoryPaths.getUserHomeFolderPath( USERNAME_SUZY ) + "/helloworld.xaction";

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

    final String expectedName = "helloworld.sample";
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

    final String refereeFileName = "referee.sample";
    final String referrerFileName = "referrer.sample";

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
  public void testMissingRef() throws Exception {
    // if a user does not have permission to a reference, it is removed from the node structure and
    // replaced with a missing link. previous releases would throw an exception.

    // create a file that suzy does not have permission to
    // create a file that suzy has permission to but references the one she doesn't
    // load the file as suzy, make sure no exceptions occur and that the node is a missing reference
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, "password", "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, "password", "", null );

    DataNode node = new DataNode( "kdjd" );
    RepositoryFile sampleFile =
        createSampleFile( ClientRepositoryPaths.getPublicFolderPath(), "helloworld2.sample", "dfdd", true, 83 );
    RepositoryFileAcl acl = repo.getAcl( sampleFile.getId() );
    RepositoryFileAcl newAcl = new RepositoryFileAcl.Builder( acl ).entriesInheriting( false ).clearAces().build();
    repo.updateAcl( newAcl );
    node.setProperty( "urei2", new DataNodeRef( sampleFile.getId() ) );
    final String parentFolderPath = ClientRepositoryPaths.getPublicFolderPath();
    final String expectedName = "helloworld.doesnotmatter";
    RepositoryFile parentFolder = repo.getFile( parentFolderPath );
    assertNotNull( parentFolder );
    final String expectedPath = parentFolderPath + RepositoryFile.SEPARATOR + expectedName;
    NodeRepositoryFileData data = new NodeRepositoryFileData( node );
    RepositoryFile newFile =
        repo.createFile( parentFolder.getId(), new RepositoryFile.Builder( expectedName ).build(), data, null );
    assertNotNull( newFile.getId() );

    // now check that the ref is missing

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    RepositoryFile foundFile = repo.getFile( expectedPath );
    assertNotNull( foundFile );

    DataNode foundNode = repo.getDataForRead( newFile.getId(), NodeRepositoryFileData.class ).getNode();
    DataProperty d = foundNode.getProperty( "urei2" );
    assertNotNull( d );
    assertTrue( d.getType() == DataPropertyType.REF );
    assertTrue( d.getRef().getId() == DataNodeRef.REF_MISSING );

    // now change permissions back so she can get access to the node, confirm things are back to normal

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    newAcl = new RepositoryFileAcl.Builder( acl ).entriesInheriting( true ).clearAces().build();
    repo.updateAcl( newAcl );
    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );
    foundFile = repo.getFile( expectedPath );
    assertNotNull( foundFile );

    foundNode = repo.getDataForRead( newFile.getId(), NodeRepositoryFileData.class ).getNode();
    d = foundNode.getProperty( "urei2" );
    assertNotNull( d );
    assertTrue( d.getType() == DataPropertyType.REF );
    assertTrue( d.getRef().getId().equals( sampleFile.getId() ) );
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

    final String expectedName = "helloworld.doesnotmatter";
    final String parentFolderPath = ClientRepositoryPaths.getUserHomeFolderPath( USERNAME_SUZY );
    RepositoryFile parentFolder = repo.getFile( parentFolderPath );
    final String expectedPath = parentFolderPath + RepositoryFile.SEPARATOR + expectedName;
    final String serverPath =
        ServerRepositoryPaths.getTenantRootFolderPath() + parentFolderPath + RepositoryFile.SEPARATOR
            + "helloworld2.sample";

    RepositoryFile sampleFile = createSampleFile( parentFolderPath, "helloworld2.sample", "dfdd", true, 83 );

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
  public void testCheckName() throws Exception {
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, "password", "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, "password", "", null );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    IRepositoryFileData data =
        new SimpleRepositoryFileData( new ByteArrayInputStream( new byte[0] ), null, "application/octet-stream" );
    NodeRepositoryFileData badNodeData = new NodeRepositoryFileData( new DataNode( "a/b" ) );
    DataNode node = new DataNode( "hello" );
    node.setProperty( "hello:world", "whatever" );
    NodeRepositoryFileData badNodeData2 = new NodeRepositoryFileData( node );

    final String parentFolderPath =
        ClientRepositoryPaths.getUserHomeFolderPath( PentahoSessionHolder.getSession().getName() );
    RepositoryFile parentFolder = repo.getFile( parentFolderPath );
    try {
      repo.createFolder( parentFolder.getId(), new RepositoryFile.Builder( "a/b" ).folder( true ).build(), null );
      fail();
    } catch ( UnifiedRepositoryMalformedNameException e ) {
      //ignore
    }
    try {
      repo.createFile( parentFolder.getId(), new RepositoryFile.Builder( "a:b" ).build(), data, null );
      fail();
    } catch ( UnifiedRepositoryMalformedNameException e ) {
      //ignore
    }
    try {
      repo.createFile( parentFolder.getId(), new RepositoryFile.Builder( "." ).build(), data, null );
      fail();
    } catch ( UnifiedRepositoryMalformedNameException e ) {
      //ignore
    }
    try {
      repo.createFile( parentFolder.getId(), new RepositoryFile.Builder( ".." ).build(), data, null );
      fail();
    } catch ( UnifiedRepositoryMalformedNameException e ) {
      //ignore
    }
    try {
      repo.createFile( parentFolder.getId(), new RepositoryFile.Builder( "/" ).build(), data, null );
      fail();
    } catch ( UnifiedRepositoryMalformedNameException e ) {
      //ignore
    }
    try {
      repo.createFile( parentFolder.getId(), new RepositoryFile.Builder( "a[0]" ).build(), data, null );
      fail();
    } catch ( UnifiedRepositoryMalformedNameException e ) {
      //ignore
    }
    try {
      repo.createFile( parentFolder.getId(), new RepositoryFile.Builder( "a*b" ).build(), data, null );
      fail();
    } catch ( UnifiedRepositoryMalformedNameException e ) {
      //ignore
    }
    try {
      repo.createFile( parentFolder.getId(), new RepositoryFile.Builder( " hello" ).build(), data, null );
      fail();
    } catch ( UnifiedRepositoryMalformedNameException e ) {
      //ignore
    }
    try {
      repo.createFile( parentFolder.getId(), new RepositoryFile.Builder( "hello " ).build(), data, null );
      fail();
    } catch ( UnifiedRepositoryMalformedNameException e ) {
      //ignore
    }
    try {
      repo.createFile( parentFolder.getId(), new RepositoryFile.Builder( "\t\t\t" ).build(), data, null );
      fail();
    } catch ( UnifiedRepositoryMalformedNameException e ) {
      //ignore
    }
    try {
      repo.createFile( parentFolder.getId(), new RepositoryFile.Builder( " " ).build(), data, null );
      fail();
    } catch ( UnifiedRepositoryMalformedNameException e ) {
      //ignore
    }
    try {
      repo.createFile( parentFolder.getId(), new RepositoryFile.Builder( "" ).build(), data, null );
      fail();
    } catch ( UnifiedRepositoryMalformedNameException e ) {
      //ignore
    }
    try {
      repo.createFile( parentFolder.getId(), new RepositoryFile.Builder( "\"hello\"" ).build(), data, null );
      fail();
    } catch ( UnifiedRepositoryMalformedNameException e ) {
      //ignore
    }
    try {
      repo.createFile( parentFolder.getId(), new RepositoryFile.Builder( "'hello'" ).build(), data, null );
      fail();
    } catch ( UnifiedRepositoryMalformedNameException e ) {
      //ignore
    }
    try {
      repo.createFile( parentFolder.getId(), new RepositoryFile.Builder( "he||o" ).build(), data, null );
      fail();
    } catch ( UnifiedRepositoryMalformedNameException e ) {
      //ignore
    }
    try {
      repo.createFile( parentFolder.getId(), new RepositoryFile.Builder( "hello" ).build(), badNodeData, null );
      fail();
    } catch ( UnifiedRepositoryMalformedNameException e ) {
      //ignore
    }
    try {
      repo.createFile( parentFolder.getId(), new RepositoryFile.Builder( "hello" ).build(), badNodeData2, null );
      fail();
    } catch ( UnifiedRepositoryMalformedNameException e ) {
      //ignore
    }
    repo.createFile( parentFolder.getId(), new RepositoryFile.Builder( "%hello%" ).build(), data, null );
    repo.createFile( parentFolder.getId(), new RepositoryFile.Builder( "hello world" ).build(), data, null );
    repo.createFile( parentFolder.getId(), new RepositoryFile.Builder( "hello\\world" ).build(), data, null );
  }

  @Test( expected = UnifiedRepositoryException.class )
  public void testCreateFileUnrecognizedContentType() throws Exception {
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, "password", "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, "password", "", null );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    RepositoryFile parentFolder = repo.getFile( ClientRepositoryPaths.getUserHomeFolderPath( USERNAME_SUZY ) );
    IRepositoryFileData content = new IRepositoryFileData() {
      @Override
      public long getDataSize() {
        // TODO Auto-generated method stub
        return 0;
      }
    };
    repo.createFile( parentFolder.getId(), new RepositoryFile.Builder( "helloworld.xaction" ).build(), content, null );
  }

  @Test
  public void testGetChildren() throws Exception {
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, "password", "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, "password", "", null );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    List<RepositoryFile> children =
        repo.getChildren( repo.getFile( ClientRepositoryPaths.getRootFolderPath() ).getId() );
    assertEquals( 3, children.size() );

    ArrayList<String> checkFolders = new ArrayList<String>( Arrays.asList( new String[] { "public", "etc", "home" } ) );
    for ( RepositoryFile f0 : children ) {
      if ( checkFolders.contains( f0.getName() ) ) {
        checkFolders.remove( f0.getName() );
      } else {
        fail( "Unknown file present" );
      }
    }
    if ( checkFolders.size() != 0 ) {
      fail( "All root folders not present" );
    }
    children = repo.getChildren( repo.getFile( ClientRepositoryPaths.getRootFolderPath() ).getId(), null);
    assertEquals( 3, children.size() );
    children = repo.getChildren( repo.getFile( ClientRepositoryPaths.getRootFolderPath() ).getId(), "*");
    assertEquals( 3, children.size() );
    children = repo.getChildren( repo.getFile( ClientRepositoryPaths.getRootFolderPath() ).getId(), "*me");
    assertEquals( 1, children.size() );
    children = repo.getChildren( repo.getFile( ClientRepositoryPaths.getRootFolderPath() ).getId(), "*Z*");
    assertEquals( 0, children.size() );
  }

  /**
   * A user should only be able to see his home folder (unless your the admin).
   */
  // @Test
  public void testListHomeFolders() throws Exception {
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, "password", "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, "password", "", null );
    userRoleDao.createUser( tenantAcme, USERNAME_TIFFANY, "password", "", null );

    login( USERNAME_TIFFANY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    List<RepositoryFile> children =
        repo.getChildren( repo.getFile( ClientRepositoryPaths.getHomeFolderPath() ).getId() );
    assertEquals( 1, children.size() );
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
    final String fileName = "helloworld.sample";

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
  public void testUpdateFolder() throws Exception {
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, "password", "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, "password", "", null );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    RepositoryFile parentFolder = repo.getFile( ClientRepositoryPaths.getUserHomeFolderPath( USERNAME_SUZY ) );
    RepositoryFile newFolder = new RepositoryFile.Builder( "test" ).folder( true ).hidden( true ).build();

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
        + "/test" ) );

    RepositoryFile updateNewFolder = new RepositoryFile.Builder( newFolder ).folder( true ).hidden( false ).build();

    Date updateBeginTime = Calendar.getInstance().getTime();

    // Sleep for 1 second for time comparison
    Thread.sleep( 1000 );
    updateNewFolder = repo.updateFolder( updateNewFolder, null );
    Thread.sleep( 1000 );

    assertNotNull( updateNewFolder );
    assertNotNull( updateNewFolder.getId() );
    assertTrue( !updateNewFolder.isHidden() );
    assertNotNull( SimpleJcrTestUtils.getItem( testJcrTemplate, ServerRepositoryPaths.getUserHomeFolderPath(
        tenantAcme, USERNAME_SUZY )
        + "/test" ) );
  }

  /**
   * Create a versioned file then update it with invalid data and the checkout that we did before setting the data
   * should be rolled back.
   */
  @Test
  public void testTransactionRollback() throws Exception {
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, "password", "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, "password", "", null );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    final String expectedName = "helloworld.sample";
    final String sampleString = "Ciao World!";
    final boolean sampleBoolean = true;
    final int sampleInteger = 99;
    final String parentFolderPath = ClientRepositoryPaths.getUserHomeFolderPath( USERNAME_SUZY );
    final String expectedAbsolutePath =
        ServerRepositoryPaths.getTenantRootFolderPath() + parentFolderPath + RepositoryFile.SEPARATOR + expectedName;
    RepositoryFile newFile =
        createSampleFile( parentFolderPath, expectedName, sampleString, sampleBoolean, sampleInteger, true );
    assertNotNull( SimpleJcrTestUtils.getItem( testJcrTemplate, expectedAbsolutePath ) );

    try {
      repo.updateFile( newFile, new IRepositoryFileData() {
        @Override
        public long getDataSize() {
          return 0;
        }
      }, null );
      fail( "expected UnifiedRepositoryException" );
    } catch ( UnifiedRepositoryException e ) {
      //ignore
    }
    assertFalse( SimpleJcrTestUtils.isCheckedOut( testJcrTemplate, expectedAbsolutePath ) );
  }

  @Test( expected = UnifiedRepositoryException.class )
  public void testCreateDuplicateFolder() throws Exception {
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, "password", "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, "password", "", null );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    RepositoryFile parentFolder = repo.getFile( ClientRepositoryPaths.getUserHomeFolderPath( USERNAME_SUZY ) );
    RepositoryFile newFolder = new RepositoryFile.Builder( "test" ).folder( true ).build();
    newFolder = repo.createFolder( parentFolder.getId(), newFolder, null );
    assertNotNull( SimpleJcrTestUtils.getItem( testJcrTemplate, ServerRepositoryPaths.getUserHomeFolderPath(
        tenantAcme, USERNAME_SUZY )
        + "/test" ) );
    RepositoryFile anotherFolder = new RepositoryFile.Builder( "test" ).folder( true ).build();
    newFolder = repo.createFolder( parentFolder.getId(), anotherFolder, null );
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
    assertNotNull( createSampleFile( parentFolderPath, "helloworld.sample", "Hello World!", false, 500 ) );
  }

  @Test
  public void testCreateVersionedFolder() throws Exception {
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, "password", "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, "password", "", null );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    RepositoryFile parentFolder = repo.getFile( ClientRepositoryPaths.getUserHomeFolderPath( USERNAME_SUZY ) );
    RepositoryFile newFolder = new RepositoryFile.Builder( "test" ).folder( true ).versioned( true ).build();
    newFolder = repo.createFolder( parentFolder.getId(), newFolder, null );
    assertTrue( newFolder.isVersioned() );
    assertNotNull( newFolder.getVersionId() );
    RepositoryFile newFolder2 =
        repo.createFolder( newFolder.getId(), new RepositoryFile.Builder( "test2" ).folder( true ).build(), null );
    RepositoryFile newFile = createSampleFile( newFolder2.getPath(), "helloworld.sample", "sdfdf", false, 5 );
    repo.lockFile( newFile.getId(), "lock within versioned folder" );
    repo.unlockFile( newFile.getId() );
  }

  @Test
  public void testCreateVersionedFile() throws Exception {
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, "password", "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, "password", "", null );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    final String parentFolderPath = ClientRepositoryPaths.getUserHomeFolderPath( USERNAME_SUZY );
    RepositoryFile parentFolder = repo.getFile( parentFolderPath );

    final String dataString = "Hello World!";
    final String encoding = "UTF-8";
    byte[] data = dataString.getBytes( encoding );
    ByteArrayInputStream dataStream = new ByteArrayInputStream( data );
    final String mimeType = "text/plain";
    final String fileName = "helloworld.xaction";

    final SimpleRepositoryFileData content = new SimpleRepositoryFileData( dataStream, encoding, mimeType );
    RepositoryFile newFile =
        repo.createFile( parentFolder.getId(), new RepositoryFile.Builder( fileName ).versioned( true ).build(),
            content, null );
    assertTrue( newFile.isVersioned() );
    assertNotNull( newFile.getVersionId() );
    final String filePath =
        ServerRepositoryPaths.getUserHomeFolderPath( tenantAcme, USERNAME_SUZY ) + RepositoryFile.SEPARATOR + fileName;
    int versionCount = SimpleJcrTestUtils.getVersionCount( testJcrTemplate, filePath );
    assertTrue( versionCount > 0 );
    repo.updateFile( newFile, content, null );
    try {
      repo.updateFile( newFile, content, null );
      fail();
    } catch ( UnifiedRepositoryException e ) {
      //ignore
    }

    assertTrue( SimpleJcrTestUtils.getVersionCount( testJcrTemplate, filePath ) > versionCount );
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
    final String fileName = "helloworld.xaction";

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
    final String fileName = "helloworld.sample";
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
    assertNull( repo.getFile( "/home/suzy/test1/helloworld.sample" ) );
    // rename original parent folder
    repo.moveFile( test1Folder.getId(), ClientRepositoryPaths.getUserHomeFolderPath( PentahoSessionHolder.getSession()
        .getName() )
        + RepositoryFile.SEPARATOR + "test2", null );
    assertNull( repo.getFile( test1Folder.getPath() ) );
    repo.undeleteFile( newFile.getId(), null );
    assertNotNull( repo.getFile( "/home/suzy/test1/helloworld.sample" ) );
    assertNull( repo.getFile( "/home/suzy/test2/helloworld.sample" ) ); // repo should create any missing folders
                                                                        // on undelete
    assertEquals( "/home/suzy/test1/helloworld.sample", repo.getFileById( newFile.getId() ).getPath() );

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

    final String fileName = "helloworld.sample";
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
    String absTrashPath = suzyTrashFileIdPath + "/helloworld.sample";
    SimpleJcrTestUtils.move( testJcrTemplate, "/pentaho/acme/home/suzy/test3/helloworld.sample", absTrashPath );
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

  /**
   * This test exists to prove that the server wasn't the source of a problem. I'm leaving it in.
   */
  @Test
  public void testWeird1() throws Exception {
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, "password", "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, "password", "", null );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    RepositoryFile parentFolder = repo.getFile( ClientRepositoryPaths.getUserHomeFolderPath( USERNAME_SUZY ) );
    RepositoryFile newFolder = new RepositoryFile.Builder( "test" ).folder( true ).build();
    RepositoryFile testFolder = repo.createFolder( parentFolder.getId(), newFolder, null );

    final String fileName = "helloworld.sample";
    RepositoryFile newFile = createSampleFile( testFolder.getPath(), fileName, "dfdfd", true, 3 );

    assertNotNull( repo.getFile( testFolder.getPath() ) );
    assertNotNull( repo.getFile( newFile.getPath() ) );

    repo.deleteFile( testFolder.getId(), null );

    // make sure it's gone
    assertNull( repo.getFile( testFolder.getPath() ) );

    RepositoryFile testFolder2 = repo.createFolder( parentFolder.getId(), newFolder, null );

    // make sure ID is different for new folder
    assertFalse( testFolder.getId().equals( testFolder2.getId() ) );

    assertNotNull( repo.getFile( testFolder2.getPath() ) );
    assertNull( repo.getFile( newFile.getPath() ) );
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
    final String fileName = "helloworld.xaction";

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
    final String expectedName = "helloworld.xaction";
    final String expectedAbsolutePath =
        ClientRepositoryPaths.getUserHomeFolderPath( USERNAME_SUZY ) + "/helloworld.xaction";

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
    final String expectedName = "helloworld.xaction";
    final String expectedAbsolutePath =
        ClientRepositoryPaths.getUserHomeFolderPath( USERNAME_SUZY ) + "/helloworld.xaction";

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
    final String fileName = "helloworld.xaction";
    final SimpleRepositoryFileData content = new SimpleRepositoryFileData( dataStream, encoding, mimeType );
    RepositoryFile newFile =
        repo.createFile( parentFolder.getId(), new RepositoryFile.Builder( fileName ).versioned( true ).build(),
            content, "created helloworld.xaction" );
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
  public void testCircumventApiToGetVersionHistoryNodeAccessDenied() throws Exception {
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, "password", "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, "password", "", null );
    userRoleDao.createUser( tenantAcme, USERNAME_TIFFANY, "password", "", null );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    RepositoryFile parentFolder = repo.getFile( ClientRepositoryPaths.getUserHomeFolderPath( USERNAME_SUZY ) );
    RepositoryFile newFolder = new RepositoryFile.Builder( "test" ).folder( true ).versioned( true ).build();
    newFolder = repo.createFolder( parentFolder.getId(), newFolder, null );
    final String absPath =
        ServerRepositoryPaths.getUserHomeFolderPath( tenantAcme, USERNAME_SUZY ) + RepositoryFile.SEPARATOR + "test";
    String versionHistoryAbsPath = SimpleJcrTestUtils.getVersionHistoryNodePath( testJcrTemplate, absPath );
    login( USERNAME_TIFFANY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );
    assertNull( SimpleJcrTestUtils.getItem( testJcrTemplate, versionHistoryAbsPath ) );
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
    final String fileName = "helloworld.sample";

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
    final String fileName = "helloworld.sample";

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
    RepositoryFile newFolder = new RepositoryFile.Builder( "test" ).folder( true ).versioned( true ).build();
    final String testFolderPath =
        ClientRepositoryPaths.getUserHomeFolderPath( PentahoSessionHolder.getSession().getName() )
            + RepositoryFile.SEPARATOR + "test";
    newFolder = repo.createFolder( parentFolder.getId(), newFolder, null );
    assertEquals( new RepositoryFileSid( USERNAME_SUZY ), repo.getAcl( newFolder.getId() ).getOwner() );

    // set acl removing suzy's rights to this folder
    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    RepositoryFileAcl testFolderAcl = repo.getAcl( newFolder.getId() );
    RepositoryFileAcl newAcl =
        new RepositoryFileAcl.Builder( testFolderAcl ).entriesInheriting( false ).clearAces().build();
    repo.updateAcl( newAcl );
    // but suzy is still the owner--she should be able to "acl" herself back into the folder
    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );
    assertNotNull( repo.getFile( testFolderPath ) );

    // as suzy, change owner to role to which she belongs
    testFolderAcl = repo.getAcl( newFolder.getId() );
    newAcl =
        new RepositoryFileAcl.Builder( testFolderAcl ).owner(
            new RepositoryFileSid( roleNameUtils.getPrincipleId( tenantAcme, "Authenticated" ),
                RepositoryFileSid.Type.ROLE ) ).build();
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
    RepositoryFile newFolder = new RepositoryFile.Builder( "test" ).folder( true ).versioned( true ).build();
    newFolder = repo.createFolder( parentFolder.getId(), newFolder, null );

    RepositoryFileAcl acl = repo.getAcl( newFolder.getId() );
    assertEquals( true, acl.isEntriesInheriting() );
    assertEquals( new RepositoryFileSid( USERNAME_SUZY ), acl.getOwner() );
    assertEquals( newFolder.getId(), acl.getId() );
    assertTrue( acl.getAces().isEmpty() );
    RepositoryFileAcl newAcl =
        new RepositoryFileAcl.Builder( acl ).ace( userNameUtils.getPrincipleId( tenantAcme, USERNAME_TIFFANY ),
            RepositoryFileSid.Type.USER, RepositoryFilePermission.READ ).entriesInheriting( true ).build();
    RepositoryFileAcl fetchedAcl = repo.updateAcl( newAcl );
    // since isEntriesInheriting is true, ace addition should not have taken
    assertTrue( fetchedAcl.getAces().isEmpty() );
    newAcl =
        new RepositoryFileAcl.Builder( acl ).ace( userNameUtils.getPrincipleId( tenantAcme, USERNAME_TIFFANY ),
            RepositoryFileSid.Type.USER, RepositoryFilePermission.READ ).build(); // calling ace sets
                                                                                  // entriesInheriting to false
    fetchedAcl = repo.updateAcl( newAcl );
    // since isEntriesInheriting is false, ace addition should have taken
    assertFalse( fetchedAcl.getAces().isEmpty() );
  }

  @Test
  public void testGetAcl2() throws Exception {
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
    RepositoryFile newFolder = new RepositoryFile.Builder( "test" ).folder( true ).versioned( true ).build();
    newFolder = repo.createFolder( parentFolder.getId(), newFolder, null );
    RepositoryFileAcl acl = repo.getAcl( newFolder.getId() );
    RepositoryFileAcl newAcl =
        new RepositoryFileAcl.Builder( acl ).entriesInheriting( false ).ace(
            new RepositoryFileSid( userNameUtils.getPrincipleId( tenantAcme, USERNAME_SUZY ) ),
            RepositoryFilePermission.ALL ).build();
    repo.updateAcl( newAcl );
    RepositoryFileAcl fetchedAcl = repo.getAcl( newFolder.getId() );
    assertEquals( 1, fetchedAcl.getAces().size() );
  }

  @Test
  public void testHasAccess() throws Exception {
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, "password", "", new String[] { tenantAdminRoleName } );
    ITenant tenantDuff =
        tenantManager.createTenant( systemTenant, TENANT_ID_DUFF, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantDuff, USERNAME_ADMIN, "password", "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, "password", "", null );

    login( USERNAME_ADMIN, tenantDuff, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantDuff, USERNAME_PAT, "password", "", null );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );
    assertTrue( repo.hasAccess( ClientRepositoryPaths.getUserHomeFolderPath( PentahoSessionHolder.getSession()
        .getName() ), EnumSet.of( RepositoryFilePermission.READ ) ) );

    login( USERNAME_PAT, tenantDuff, new String[] { tenantAuthenticatedRoleName } );
    assertFalse( SimpleJcrTestUtils.hasPrivileges( testJcrTemplate, ServerRepositoryPaths
        .getTenantPublicFolderPath( tenantAcme ), Privilege.JCR_READ ) );

    // false is returned if path does not exist
    assertFalse( repo.hasAccess( ClientRepositoryPaths.getRootFolderPath() + "doesnotexist", EnumSet
        .of( RepositoryFilePermission.READ ) ) );
  }

  @Test
  public void testGetEffectiveAces() throws Exception {
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

    RepositoryFile acmePublicFolder =
        repo.getFile( ClientRepositoryPaths.getUserHomeFolderPath( PentahoSessionHolder.getSession().getName() ) );
    List<RepositoryFileAce> expectedEffectiveAces1 = repo.getEffectiveAces( acmePublicFolder.getId() );
    RepositoryFile newFolder = new RepositoryFile.Builder( "test" ).folder( true ).versioned( true ).build();
    newFolder = repo.createFolder( acmePublicFolder.getId(), newFolder, null );
    assertEquals( expectedEffectiveAces1, repo.getEffectiveAces( newFolder.getId() ) );

    RepositoryFileAcl acl = repo.getAcl( newFolder.getId() );
    RepositoryFileAcl newAcl =
        new RepositoryFileAcl.Builder( acl ).entriesInheriting( false ).ace(
            new RepositoryFileSid( userNameUtils.getPrincipleId( tenantAcme, USERNAME_SUZY ) ),
            RepositoryFilePermission.ALL ).ace(
            new RepositoryFileSid( userNameUtils.getPrincipleId( tenantAcme, USERNAME_TIFFANY ) ),
            RepositoryFilePermission.READ ).build();
    repo.updateAcl( newAcl );

    List<RepositoryFileAce> expectedEffectiveAces2 = new ArrayList<RepositoryFileAce>();
    expectedEffectiveAces2.add( new RepositoryFileAce( new RepositoryFileSid( USERNAME_SUZY ), EnumSet
        .of( RepositoryFilePermission.ALL ) ) );
    expectedEffectiveAces2.add( new RepositoryFileAce( new RepositoryFileSid( USERNAME_TIFFANY ), EnumSet
        .of( RepositoryFilePermission.READ ) ) );
    assertEquals( expectedEffectiveAces2, repo.getEffectiveAces( newFolder.getId() ) );

    assertEquals( expectedEffectiveAces2, repo.getEffectiveAces( newFolder.getId(), false ) );

    assertEquals( expectedEffectiveAces1, repo.getEffectiveAces( newFolder.getId(), true ) );
  }

  @Test
  public void testUpdateAcl() throws Exception {
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
    RepositoryFile newFolder = new RepositoryFile.Builder( "test" ).folder( true ).versioned( true ).build();
    newFolder = repo.createFolder( parentFolder.getId(), newFolder, null );
    RepositoryFileAcl acl = repo.getAcl( newFolder.getId() );

    RepositoryFileAcl.Builder newAclBuilder = new RepositoryFileAcl.Builder( acl );
    RepositoryFileSid tiffanySid = new RepositoryFileSid(
      userNameUtils.getPrincipleId( tenantAcme, USERNAME_TIFFANY ) );
    newAclBuilder.owner( tiffanySid );
    repo.updateAcl( newAclBuilder.build() );
    RepositoryFileAcl fetchedAcl = repo.getAcl( newFolder.getId() );
    assertEquals( new RepositoryFileSid( USERNAME_TIFFANY ), fetchedAcl.getOwner() );
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
    RepositoryFile newFolder = new RepositoryFile.Builder( "test" ).folder( true ).versioned( true ).build();
    RepositoryFileSid tiffanySid = new RepositoryFileSid(
      userNameUtils.getPrincipleId( tenantAcme, USERNAME_TIFFANY ) );
    RepositoryFileSid suzySid = new RepositoryFileSid( userNameUtils.getPrincipleId( tenantAcme, USERNAME_SUZY ) );
    // tiffany owns it but suzy is creating it
    RepositoryFileAcl.Builder aclBuilder = new RepositoryFileAcl.Builder( tiffanySid );
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
    RepositoryFile srcFolder = new RepositoryFile.Builder( "src" ).folder( true ).build();
    RepositoryFile destFolder = new RepositoryFile.Builder( "dest" ).folder( true ).build();
    srcFolder = repo.createFolder( parentFolder.getId(), srcFolder, null );
    destFolder = repo.createFolder( parentFolder.getId(), destFolder, null );

    RepositoryFile newFile = createSampleFile( srcFolder.getPath(), "helloworld.sample", "ddfdf", false, 83 );
    RepositoryFileAcl acl =
        new RepositoryFileAcl.Builder( newFile.getId(), userNameUtils.getPrincipleId( tenantAcme, USERNAME_TIFFANY ),
            RepositoryFileSid.Type.USER ).entriesInheriting( false ).ace(
            userNameUtils.getPrincipleId( tenantAcme, USERNAME_SUZY ), RepositoryFileSid.Type.USER,
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
   * Tests parent ACL's contribution to decision. // This test is bogus, it doesn't actually try the delete
   */
  @Test
  public void testDeleteInheritingFile() throws Exception {
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, "password", "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, "password", "", null );
    defaultBackingRepositoryLifecycleManager.newTenant();
    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    RepositoryFile newFile =
        createSampleFile( repo.getFile( ClientRepositoryPaths.getUserHomeFolderPath( USERNAME_SUZY ) ).getPath(),
            "helloworld.sample", "ddfdf", false, 83 );
    RepositoryFileAcl acl =
        new RepositoryFileAcl.Builder( newFile.getId(), userNameUtils.getPrincipleId( tenantAcme, USERNAME_SUZY ),
            RepositoryFileSid.Type.USER ).entriesInheriting( false ).build();
    repo.updateAcl( acl );
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
            "helloworld.sample", "ddfdf", false, 83 );
    RepositoryFileAcl publicFolderFileAcl =
        new RepositoryFileAcl.Builder( publicFolderFile.getId(), userNameUtils.getPrincipleId( tenantAcme,
            USERNAME_ADMIN ), RepositoryFileSid.Type.USER ).entriesInheriting( false ).ace(
            new RepositoryFileSid( roleNameUtils.getPrincipleId( tenantAcme, tenantAuthenticatedRoleName ),
                RepositoryFileSid.Type.ROLE ), RepositoryFilePermission.READ, RepositoryFilePermission.WRITE ).build();
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
            "helloworld.sample", "ddfdf", false, 83 );
    RepositoryFileAcl publicFolderFileAcl =
        new RepositoryFileAcl.Builder( publicFolderFile.getId(), userNameUtils.getPrincipleId( tenantAcme,
            USERNAME_ADMIN ), RepositoryFileSid.Type.USER ).entriesInheriting( false ).ace(
            new RepositoryFileSid( roleNameUtils.getPrincipleId( tenantAcme, tenantAuthenticatedRoleName ),
                RepositoryFileSid.Type.ROLE ), RepositoryFilePermission.READ ).build();
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
    RepositoryFile newFolder = new RepositoryFile.Builder( "test" ).folder( true ).versioned( true ).build();
    newFolder = repo.createFolder( parentFolder.getId(), newFolder, null );

    RepositoryFileAcl acls = repo.getAcl( newFolder.getId() );

    RepositoryFileAcl.Builder newAclBuilder = new RepositoryFileAcl.Builder( acls );
    newAclBuilder.entriesInheriting( false ).ace( userNameUtils.getPrincipleId( tenantAcme, USERNAME_TIFFANY ),
        RepositoryFileSid.Type.USER, RepositoryFilePermission.READ );
    repo.updateAcl( newAclBuilder.build() );

    login( USERNAME_TIFFANY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );
    RepositoryFileAcl newAcl = repo.getAcl( newFolder.getId() );

    RepositoryFileAcl.Builder anotherNewAclBuilder = new RepositoryFileAcl.Builder( newAcl );
    anotherNewAclBuilder.ace( new RepositoryFileSid( userNameUtils.getPrincipleId( tenantAcme,
        tenantAuthenticatedRoleName ), RepositoryFileSid.Type.ROLE ), RepositoryFilePermission.READ,
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
    RepositoryFile moveTest1Folder = new RepositoryFile.Builder( "moveTest1" ).folder( true ).versioned( true ).build();
    moveTest1Folder = repo.createFolder( parentFolder.getId(), moveTest1Folder, null );
    RepositoryFile moveTest2Folder = new RepositoryFile.Builder( "moveTest2" ).folder( true ).versioned( true ).build();
    moveTest2Folder = repo.createFolder( parentFolder.getId(), moveTest2Folder, null );
    RepositoryFile testFolder = new RepositoryFile.Builder( "test" ).folder( true ).build();
    testFolder = repo.createFolder( moveTest1Folder.getId(), testFolder, null );
    // move folder into new folder
    repo.moveFile( testFolder.getId(), moveTest2Folder.getPath(), null );
    assertNull( repo.getFile( ClientRepositoryPaths.getUserHomeFolderPath( PentahoSessionHolder.getSession().getName() )
        + RepositoryFile.SEPARATOR + "moveTest1" + RepositoryFile.SEPARATOR + "test" ) );
    assertNotNull( repo.getFile( ClientRepositoryPaths.getUserHomeFolderPath( PentahoSessionHolder.getSession()
        .getName() )
        + RepositoryFile.SEPARATOR + "moveTest2" + RepositoryFile.SEPARATOR + "test" ) );
    // rename within same folder
    repo.moveFile( testFolder.getId(), moveTest2Folder.getPath() + RepositoryFile.SEPARATOR + "newTest", null );
    assertNull( repo.getFile( ClientRepositoryPaths.getUserHomeFolderPath( PentahoSessionHolder.getSession().getName() )
        + RepositoryFile.SEPARATOR + "moveTest2" + RepositoryFile.SEPARATOR + "test" ) );
    assertNotNull( repo.getFile( ClientRepositoryPaths.getUserHomeFolderPath( PentahoSessionHolder.getSession()
        .getName() )
        + RepositoryFile.SEPARATOR + "moveTest2" + RepositoryFile.SEPARATOR + "newTest" ) );

    RepositoryFile newFile = createSampleFile( moveTest2Folder.getPath(), "helloworld.sample", "ddfdf", false, 83 );
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

  /**
   * Jackrabbit will throw a javax.jcr.ItemExistsException ("colliding with same-named existing node") error.
   */
  @Test( expected = UnifiedRepositoryException.class )
  public void testCopyFileOverwrite() throws Exception {
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
    RepositoryFile testFile1 = createSimpleFile( parentFolder.getId(), "testfile1" );
    RepositoryFile testFile2 = createSimpleFile( parentFolder.getId(), "testfile2" );
    repo.copyFile( testFile1.getId(), testFile2.getPath(), null );
  }

  /**
   * Jackrabbit will throw a javax.jcr.ItemExistsException ("colliding with same-named existing node") error.
   */
  @Test( expected = UnifiedRepositoryException.class )
  public void testCopyFolderOverwrite() throws Exception {
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
        repo.createFolder( parentFolder.getId(), new RepositoryFile.Builder( "testfolder1" ).folder( true ).build(),
            null );
    RepositoryFile testFolder1Child =
        repo.createFolder( testFolder1.getId(), new RepositoryFile.Builder( "testfolder1" ).folder( true ).build(),
            null );
    repo.copyFile( testFolder1Child.getId(), parentFolder.getPath(), null );
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
        repo.createFolder( parentFolder.getId(), new RepositoryFile.Builder( "testfolder1" ).folder( true ).build(),
            null );
    RepositoryFile testFile1 = createSimpleFile( testFolder1.getId(), "testfile1" );
    RepositoryFile testFolder2 =
        repo.createFolder( parentFolder.getId(), new RepositoryFile.Builder( "testfolder2" ).folder( true ).build(),
            null );
    RepositoryFile testFile2 = createSimpleFile( testFolder2.getId(), "testfile2" );
    repo.copyFile( testFolder1.getId(), testFolder2.getPath(), null );
    assertNotNull( repo.getFile( testFolder2.getPath() + RepositoryFile.SEPARATOR + "testfile2" ) );
    assertNotNull( repo.getFile( testFolder2.getPath() + RepositoryFile.SEPARATOR + "testfolder1" ) );
    assertNotNull( repo.getFile( testFolder2.getPath() + RepositoryFile.SEPARATOR + "testfolder1"
        + RepositoryFile.SEPARATOR + "testfile1" ) );
  }

  @Test
  public void testCopyFile() throws Exception {
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
    RepositoryFile copyTest1Folder = new RepositoryFile.Builder( "copyTest1" ).folder( true ).versioned( true ).build();
    RepositoryFileSid fileOwnerSid = new RepositoryFileSid( userNameUtils.getPrincipleId( tenantAcme, USERNAME_SUZY ) );
    copyTest1Folder =
        repo.createFolder( parentFolder.getId(), copyTest1Folder,
            new RepositoryFileAcl.Builder( fileOwnerSid ).build(), null );
    RepositoryFile copyTest2Folder = new RepositoryFile.Builder( "copyTest2" ).folder( true ).versioned( true ).build();
    copyTest2Folder =
        repo.createFolder( parentFolder.getId(), copyTest2Folder,
            new RepositoryFileAcl.Builder( fileOwnerSid ).build(), null );
    RepositoryFile testFolder = new RepositoryFile.Builder( "test" ).folder( true ).build();
    testFolder =
        repo.createFolder( copyTest1Folder.getId(), testFolder, new RepositoryFileAcl.Builder( fileOwnerSid ).build(),
            null );
    // copy folder into new folder
    repo.copyFile( testFolder.getId(), copyTest2Folder.getPath(), null );
    assertNotNull( repo.getFile( ClientRepositoryPaths.getUserHomeFolderPath( PentahoSessionHolder.getSession()
        .getName() )
        + RepositoryFile.SEPARATOR + "copyTest1" + RepositoryFile.SEPARATOR + "test" ) );
    assertNotNull( repo.getFile( ClientRepositoryPaths.getUserHomeFolderPath( PentahoSessionHolder.getSession()
        .getName() )
        + RepositoryFile.SEPARATOR + "copyTest2" + RepositoryFile.SEPARATOR + "test" ) );
    // copy folder into new folder and rename
    repo.copyFile( testFolder.getId(), copyTest2Folder.getPath() + RepositoryFile.SEPARATOR + "newTest2", null );
    assertNotNull( repo.getFile( ClientRepositoryPaths.getUserHomeFolderPath( PentahoSessionHolder.getSession()
        .getName() )
        + RepositoryFile.SEPARATOR + "copyTest1" + RepositoryFile.SEPARATOR + "test" ) );
    assertNotNull( repo.getFile( ClientRepositoryPaths.getUserHomeFolderPath( PentahoSessionHolder.getSession()
        .getName() )
        + RepositoryFile.SEPARATOR + "copyTest2" + RepositoryFile.SEPARATOR + "newTest2" ) );

    // copy within same folder
    repo.copyFile( testFolder.getId(), copyTest2Folder.getPath() + RepositoryFile.SEPARATOR + "newTest", null );
    assertNotNull( repo.getFile( ClientRepositoryPaths.getUserHomeFolderPath( PentahoSessionHolder.getSession()
        .getName() )
        + RepositoryFile.SEPARATOR + "copyTest2" + RepositoryFile.SEPARATOR + "test" ) );
    assertNotNull( repo.getFile( ClientRepositoryPaths.getUserHomeFolderPath( PentahoSessionHolder.getSession()
        .getName() )
        + RepositoryFile.SEPARATOR + "copyTest2" + RepositoryFile.SEPARATOR + "newTest" ) );

    RepositoryFile newFile = createSampleFile( copyTest2Folder.getPath(), "helloworld.sample", "ddfdf", false, 83 );
    try {
      repo.copyFile( testFolder.getId(), copyTest2Folder.getPath() + RepositoryFile.SEPARATOR + "doesnotexist"
          + RepositoryFile.SEPARATOR + "newTest2", null );
      fail();
    } catch ( UnifiedRepositoryException e ) {
      // copying a folder to a path with a non-existent parent folder is illegal
    }

    try {
      repo.copyFile( testFolder.getId(), newFile.getPath(), null );
      fail();
    } catch ( UnifiedRepositoryException e ) {
      // copying a folder to a file is illegal
    }
    JcrRepositoryDumpToFile dumpToFile =
        new JcrRepositoryDumpToFile( testJcrTemplate, jcrTransactionTemplate, repositoryAdminUsername,
            "c:/build/testrepo_17", Mode.CUSTOM );
    dumpToFile.execute();
  }

  @Test
  public void testGetRoot() throws Exception {
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, "password", "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, "password", "", null );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    RepositoryFile rootFolder = repo.getFile( "/" );
    assertNotNull( rootFolder );
    assertEquals( "", rootFolder.getName() );
    assertNotNull( rootFolder.getId() );
    assertNotNull( repo.getChildren( rootFolder.getId() ) );
    RepositoryFileAcl rootFolderAcl = repo.getAcl( rootFolder.getId() );
  }

  @Test
  public void testDeleteSid() throws Exception {
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant tenantDuff =
        tenantManager.createTenant( systemTenant, TENANT_ID_DUFF, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantDuff, USERNAME_ADMIN, "password", "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantDuff, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    IPentahoUser userGeorge = userRoleDao.createUser( tenantDuff, USERNAME_GEORGE, "password", "", null );
    userRoleDao.createUser( tenantDuff, USERNAME_PAT, "password", "", null );

    login( USERNAME_GEORGE, tenantDuff, new String[] { tenantAuthenticatedRoleName } );

    RepositoryFile parentFolder =
        repo.getFile( ClientRepositoryPaths.getUserHomeFolderPath( PentahoSessionHolder.getSession().getName() ) );
    RepositoryFile newFile = createSampleFile( parentFolder.getPath(), "hello.xaction", "", false, 2, false );
    RepositoryFileAcl acls = repo.getAcl( newFile.getId() );

    RepositoryFileAcl.Builder newAclBuilder = new RepositoryFileAcl.Builder( acls );
    newAclBuilder.entriesInheriting( false ).ace( userNameUtils.getPrincipleId( tenantDuff, USERNAME_PAT ),
        RepositoryFileSid.Type.USER, RepositoryFilePermission.ALL );
    repo.updateAcl( newAclBuilder.build() );

    login( USERNAME_PAT, tenantDuff, new String[] { tenantAuthenticatedRoleName } );

    userRoleDao.deleteUser( userGeorge );
    login( USERNAME_PAT, tenantDuff, new String[] { tenantAuthenticatedRoleName } );
    // TestPrincipalProvider.enableGeorgeAndDuff(false); simulate delete of george who is owner and explicitly in
    // ACE
    RepositoryFile fetchedFile = repo.getFileById( newFile.getId() );
    assertEquals( USERNAME_GEORGE, repo.getAcl( fetchedFile.getId() ).getOwner().getName() );
    assertEquals( RepositoryFileSid.Type.USER, repo.getAcl( fetchedFile.getId() ).getOwner().getType() );

    RepositoryFileAcl updatedAcl = repo.getAcl( newFile.getId() );

    boolean foundGeorge = false;

    for ( RepositoryFileAce ace : updatedAcl.getAces() ) {
      if ( USERNAME_GEORGE.equals( ace.getSid().getName() ) ) {
        foundGeorge = true;
      }
    }
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

    final String expectedName = "helloworld.sample";
    final String sampleString = "Ciao World!";
    final boolean sampleBoolean = true;
    final int sampleInteger = 99;
    final String parentFolderPath = ClientRepositoryPaths.getUserHomeFolderPath( USERNAME_SUZY );
    RepositoryFile newFile =
        createSampleFile( parentFolderPath, expectedName, sampleString, sampleBoolean, sampleInteger );
    RepositoryFileAcl acls = repo.getAcl( newFile.getId() );

    RepositoryFileAcl.Builder newAclBuilder = new RepositoryFileAcl.Builder( acls );
    newAclBuilder.entriesInheriting( false ).ace( userNameUtils.getPrincipleId( tenantAcme, USERNAME_SUZY ),
        RepositoryFileSid.Type.USER, RepositoryFilePermission.ALL );
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
        repo.createFile( publicFolder.getId(), new RepositoryFile.Builder( "helloworld.xaction" ).versioned( true )
            .hidden( false ).build(), content, null );

    RepositoryFile newFile2 =
        repo.createFolder( publicFolder.getId(), new RepositoryFile.Builder( "testFolder" ).versioned( false ).hidden(
            false ).folder( true ).build(), null, null );

    root = repo.getTree( publicFolder.getPath(), 1, "*|FILES", true );
    assertFalse( root.getChildren().isEmpty() );
    assertEquals( 1, root.getChildren().size() );
    assertEquals( "helloworld.xaction", root.getChildren().get( 0 ).getFile().getName() );

    root = repo.getTree( publicFolder.getPath(), 1, "*", true );
    assertFalse( root.getChildren().isEmpty() );
    assertEquals( 2, root.getChildren().size() );

    root = repo.getTree( publicFolder.getPath(), 1, "*|FILES_FOLDERS", true );
    assertFalse( root.getChildren().isEmpty() );
    assertEquals( 2, root.getChildren().size() );

    root = repo.getTree( publicFolder.getPath(), 1, "*|FOLDERS", true );
    assertFalse( root.getChildren().isEmpty() );
    assertEquals( 1, root.getChildren().size() );
    assertEquals( "testFolder", root.getChildren().get( 0 ).getFile().getName() );

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
        repo.createFile( publicFolder.getId(), new RepositoryFile.Builder( "helloworld.xaction" ).versioned( true )
            .hidden( true ).build(), content, null );
    root = repo.getTree( publicFolder.getPath(), -1, null, true );
    assertFalse( root.getChildren().isEmpty() );
    root = repo.getTree( publicFolder.getPath(), -1, null, false );
    assertTrue( root.getChildren().isEmpty() );
  }

  @Test
  public void testGetDataForReadInBatch_unversioned() throws Exception {
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

    RepositoryFile newFile1 = createSampleFile( parentFolderPath, "helloworld.sample1", sampleString1, true, 1 );
    RepositoryFile newFile2 = createSampleFile( parentFolderPath, "file2", sampleString2, false, 2 );

    assertNotNull( newFile1.getId() );
    assertNull( newFile1.getVersionId() );
    assertNotNull( newFile2.getId() );
    assertNull( newFile2.getVersionId() );

    List<SampleRepositoryFileData> data =
        repo.getDataForReadInBatch( Arrays.asList( newFile1, newFile2 ), SampleRepositoryFileData.class );
    assertEquals( 2, data.size() );
    SampleRepositoryFileData d = data.get( 0 );
    assertEquals( sampleString1, d.getSampleString() );
    d = data.get( 1 );
    assertEquals( sampleString2, d.getSampleString() );
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

    RepositoryFile newFile1 = createSampleFile( parentFolderPath, "helloworld.sample1", sampleString1, true, 1, true );
    RepositoryFile newFile2 = createSampleFile( parentFolderPath, "file2", sampleString2, false, 2 );

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

    RepositoryFile newFile1 = createSampleFile( parentFolderPath, "helloworld.sample1", sampleString1, true, 1, true );

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

    RepositoryFile newFile1 = createSampleFile( parentFolderPath, "helloworld.sample1", sampleString1, true, 1, true );
    RepositoryFile newFile2 = createSampleFile( parentFolderPath, "helloworld.sample2", sampleString2, true, 1, true );

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
    final String fileName1 = "helloworld1.xaction";
    final String fileName2 = "helloworld2.xaction";
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

  @Test
  public void testGetReservedChars() throws Exception {
    assertFalse( repo.getReservedChars().isEmpty() );
  }

  public void testMagicAcesNotCached() throws Exception {
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, "password", "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, "password", "", null );
    userRoleDao.createUser( tenantAcme, USERNAME_TIFFANY, "password", "", null );

    assertNotNull( repo.getFile( ClientRepositoryPaths.getUserHomeFolderPath( USERNAME_SUZY ) ) );
    login( USERNAME_TIFFANY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );
    assertNull( repo.getFile( ClientRepositoryPaths.getUserHomeFolderPath( USERNAME_SUZY ) ) );
  }

  @Test( expected = AccessDeniedException.class )
  public void testRoleAuthorizationPolicyAdministerSecurityAccessDenied() throws Exception {
    loginAsSysTenantAdmin();
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, "password", "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, "password", "", null );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    roleBindingDao.setRoleBindings( "Authenticated", Arrays.asList( new String[] { RepositoryReadAction.NAME } ) );
  }

  @Test
  public void testRoleAuthorizationPolicyNoBoundLogicalRoles() throws Exception {
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, "password", "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );

    assertEquals( Arrays.asList( new String[] { RepositoryReadAction.NAME, SchedulerAction.NAME,
      RepositoryCreateAction.NAME } ), roleBindingDao.getBoundLogicalRoleNames( Arrays.asList( new String[] {
        "Authenticated", "ceo" } ) ) );
  }

  @Test
  public void testRoleAuthorizationPolicyGetAllowedActions() throws Exception {
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, "password", "", new String[] { tenantAdminRoleName } );
    ITenant tenantDuff =
        tenantManager.createTenant( systemTenant, TENANT_ID_DUFF, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantDuff, USERNAME_ADMIN, "password", "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, "password", "", null );

    login( USERNAME_ADMIN, tenantDuff, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantDuff, USERNAME_PAT, "password", "", null );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    // test with null namespace
    List<String> allowedActions = authorizationPolicy.getAllowedActions( null );

    assertEquals( 3, allowedActions.size() );
    assertTrue( allowedActions.contains( RepositoryReadAction.NAME ) );
    assertTrue( allowedActions.contains( RepositoryCreateAction.NAME ) );
    assertTrue( allowedActions.contains( SchedulerAction.NAME ) );

    // test with explicit namespace
    allowedActions = authorizationPolicy.getAllowedActions( NAMESPACE_REPOSITORY );
    assertEquals( 2, allowedActions.size() );
    assertTrue( allowedActions.contains( RepositoryReadAction.NAME ) );
    assertTrue( allowedActions.contains( RepositoryCreateAction.NAME ) );

    // test with scheduler
    allowedActions = authorizationPolicy.getAllowedActions( NAMESPACE_SCHEDULER );
    assertEquals( 1, allowedActions.size() );
    assertTrue( allowedActions.contains( SchedulerAction.NAME ) );

    // test with bogus namespace
    allowedActions = authorizationPolicy.getAllowedActions( NAMESPACE_DOESNOTEXIST );
    assertEquals( 0, allowedActions.size() );

    // login with pat (in tenant duff); pat is granted "Authenticated" so he is allowed
    login( USERNAME_PAT, tenantDuff, new String[] { tenantAuthenticatedRoleName } );
    allowedActions = authorizationPolicy.getAllowedActions( null );
    assertEquals( 3, allowedActions.size() );
    assertTrue( allowedActions.contains( RepositoryReadAction.NAME ) );
    assertTrue( allowedActions.contains( RepositoryCreateAction.NAME ) );
    assertTrue( allowedActions.contains( SchedulerAction.NAME ) );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    allowedActions = authorizationPolicy.getAllowedActions( NAMESPACE_REPOSITORY );
    assertEquals( 2, allowedActions.size() );
    assertTrue( allowedActions.contains( RepositoryReadAction.NAME ) );
    assertTrue( allowedActions.contains( RepositoryCreateAction.NAME ) );
    // test with scheduler
    allowedActions = authorizationPolicy.getAllowedActions( NAMESPACE_SCHEDULER );
    assertEquals( 1, allowedActions.size() );
    assertTrue( allowedActions.contains( SchedulerAction.NAME ) );
    allowedActions = authorizationPolicy.getAllowedActions( NAMESPACE_SECURITY );
    assertEquals( 2, allowedActions.size() );
    assertTrue( allowedActions.contains( AdministerSecurityAction.NAME ) );

    allowedActions = authorizationPolicy.getAllowedActions( NAMESPACE_PENTAHO );
    assertEquals( 5, allowedActions.size() );
  }

  @Test
  public void testRoleAuthorizationPolicyTenants() throws Exception {
    ITenant tenantAcme = null;
    List<String> origLogicalRoles = null;
    try {
      login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
      origLogicalRoles =
          roleBindingDao.getBoundLogicalRoleNames( Arrays.asList( new String[] { "acme_Authenticated" } ) );
      tenantAcme =
          tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
              "Anonymous" );
      userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, "password", "", new String[] { tenantAdminRoleName } );
      ITenant tenantDuff =
          tenantManager.createTenant( systemTenant, TENANT_ID_DUFF, tenantAdminRoleName, tenantAuthenticatedRoleName,
              "Anonymous" );
      userRoleDao.createUser( tenantDuff, USERNAME_ADMIN, "password", "", new String[] { tenantAdminRoleName } );

      login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
      userRoleDao.createUser( tenantAcme, USERNAME_SUZY, "password", "", null );

      login( USERNAME_ADMIN, tenantDuff, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
      userRoleDao.createUser( tenantDuff, USERNAME_PAT, "password", "", null );
      assertEquals( 5, authorizationPolicy.getAllowedActions( null ).size() );

      login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );
      assertEquals( 3, authorizationPolicy.getAllowedActions( null ).size() );

      // login with admin (in tenant acme)
      login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
      roleBindingDao
          .setRoleBindings( tenantAuthenticatedRoleName, Arrays.asList( new String[] { RepositoryReadAction.NAME,
            RepositoryCreateAction.NAME, SchedulerAction.NAME, AdministerSecurityAction.NAME } ) );
      assertEquals( 5, authorizationPolicy.getAllowedActions( null ).size() );

      // login with pat (in tenant duff)
      login( USERNAME_PAT, tenantDuff, new String[] { tenantAuthenticatedRoleName } );
      assertEquals( 3, authorizationPolicy.getAllowedActions( null ).size() );

      // login with suzy again (in tenant acme); expect additional action for suzy
      login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );
      assertEquals( 4, authorizationPolicy.getAllowedActions( null ).size() );
    } finally {
      login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
      // must do it this way in order to reset the cache
      roleBindingDao.setRoleBindings( tenantAuthenticatedRoleName, origLogicalRoles );
    }
  }

  @Test
  public void testRoleAuthorizationPolicyIsAllowed() throws Exception {
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, "password", "", new String[] { tenantAdminRoleName } );
    ITenant tenantDuff =
        tenantManager.createTenant( systemTenant, TENANT_ID_DUFF, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantDuff, USERNAME_ADMIN, "password", "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, "password", "", null );

    login( USERNAME_ADMIN, tenantDuff, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantDuff, USERNAME_PAT, "password", "", null );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    assertTrue( authorizationPolicy.isAllowed( RepositoryReadAction.NAME ) );
    assertTrue( authorizationPolicy.isAllowed( RepositoryCreateAction.NAME ) );
    assertTrue( authorizationPolicy.isAllowed( AdministerSecurityAction.NAME ) );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );
    assertTrue( authorizationPolicy.isAllowed( RepositoryReadAction.NAME ) );
    assertTrue( authorizationPolicy.isAllowed( RepositoryCreateAction.NAME ) );
    assertFalse( authorizationPolicy.isAllowed( AdministerSecurityAction.NAME ) );

    login( USERNAME_PAT, tenantDuff, new String[] { tenantAuthenticatedRoleName } );
    assertTrue( authorizationPolicy.isAllowed( RepositoryReadAction.NAME ) );
    assertTrue( authorizationPolicy.isAllowed( RepositoryCreateAction.NAME ) );
    assertFalse( authorizationPolicy.isAllowed( AdministerSecurityAction.NAME ) );
  }

  @Test
  public void testRoleAuthorizationPolicyRemoveImmutableBinding() throws Exception {
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, "password", "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    try {
      roleBindingDao.setRoleBindings( tenantAdminRoleName, Arrays.asList( new String[] { RepositoryReadAction.NAME,
        RepositoryCreateAction.NAME } ) );
      fail();
    } catch ( Exception e ) {
      //ignored
    }
  }

  @Test
  // @Test(expected = AccessDeniedException.class)
  public
  void testRoleAuthorizationPolicyGetRoleBindingStructAccessDenied() throws Exception {
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, "password", "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, "password", "", null );

    // login with user that is not allowed to "administer security"
    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );
    roleBindingDao.getRoleBindingStruct( Locale.getDefault().toString() );
  }

  /**
   * Please keep this test the last of the testRoleAuthorizationPolicy* since it adds a binding that cannot be
   * deleted, only set to no associated logical roles.
   */
  @Test
  public void testRoleAuthorizationPolicyGetRoleBindingStruct() throws Exception {
    ITenant tenantAcme = null;
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            "Anonymous" );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, "password", "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    RoleBindingStruct struct = roleBindingDao.getRoleBindingStruct( Locale.getDefault().toString() );
    assertNotNull( struct );
    assertNotNull( struct.bindingMap );
    assertEquals( 3, struct.bindingMap.size() );
    assertEquals( Arrays.asList( new String[] { RepositoryReadAction.NAME, RepositoryCreateAction.NAME,
      SchedulerAction.NAME, AdministerSecurityAction.NAME, PublishAction.NAME } ), struct.bindingMap
        .get( superAdminRoleName ) );
    assertEquals( Arrays.asList( new String[] { RepositoryReadAction.NAME, RepositoryCreateAction.NAME,
      SchedulerAction.NAME, AdministerSecurityAction.NAME, PublishAction.NAME } ), struct.bindingMap
        .get( tenantAdminRoleName ) );
    assertEquals( Arrays.asList( new String[] { RepositoryReadAction.NAME, RepositoryCreateAction.NAME,
      SchedulerAction.NAME } ), struct.bindingMap.get( tenantAuthenticatedRoleName ) );
    roleBindingDao.setRoleBindings( "whatever", Arrays.asList( new String[] { "org.pentaho.p1.reader" } ) );

    struct = roleBindingDao.getRoleBindingStruct( Locale.getDefault().toString() );
    assertEquals( 4, struct.bindingMap.size() );
    assertEquals( Arrays.asList( new String[] { "org.pentaho.p1.reader" } ), struct.bindingMap.get( "whatever" ) );

    assertNotNull( struct.logicalRoleNameMap );
    assertEquals( 5, struct.logicalRoleNameMap.size() );
    assertEquals( "Create Content", struct.logicalRoleNameMap.get( RepositoryCreateAction.NAME ) );

    assertNotNull( struct.immutableRoles );
    assertEquals( 2, struct.immutableRoles.size() );
    assertTrue( struct.immutableRoles.contains( superAdminRoleName ) );
    assertTrue( struct.immutableRoles.contains( tenantAdminRoleName ) );
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
              RepositoryFileSid ownerSid = new RepositoryFileSid( ownerId, Type.USER );
              aclsForUserHomeFolder =
                  new RepositoryFileAcl.Builder( userSid ).ace( ownerSid, EnumSet.of( RepositoryFilePermission.ALL ) );
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
          new RepositoryFileAcl.Builder( acl ).ace( userNameUtils.getPrincipleId( tenantAcme, USERNAME_ADMIN ),
              RepositoryFileSid.Type.USER, RepositoryFilePermission.ALL ).entriesInheriting( true ).build();
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
          new RepositoryFileAcl.Builder( acl ).clearAces().ace(
              userNameUtils.getPrincipleId( tenantAcme, USERNAME_ADMIN ), RepositoryFileSid.Type.USER,
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
          new RepositoryFileAcl.Builder( acl ).ace( userNameUtils.getPrincipleId( tenantAcme, USERNAME_ADMIN ),
              RepositoryFileSid.Type.USER, RepositoryFilePermission.ALL ).entriesInheriting( true ).build();
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
          new RepositoryFileAcl.Builder( acl ).ace( userNameUtils.getPrincipleId( tenantAcme, USERNAME_ADMIN ),
              RepositoryFileSid.Type.USER, RepositoryFilePermission.ALL ).entriesInheriting( false ).build();
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
    RepositoryFileAcl.Builder newAclBuilder =
        new RepositoryFileAcl.Builder( acl ).ace( suzySid, EnumSet.of( RepositoryFilePermission.READ,
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
