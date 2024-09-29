/*!
 *
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
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.repository2.unified;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pentaho.platform.api.locale.IPentahoLocale;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.IRepositoryVersionManager;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid;
import org.pentaho.platform.api.repository2.unified.RepositoryFileTree;
import org.pentaho.platform.api.repository2.unified.RepositoryRequest;
import org.pentaho.platform.api.repository2.unified.UnifiedRepositoryAccessDeniedException;
import org.pentaho.platform.api.repository2.unified.UnifiedRepositoryException;
import org.pentaho.platform.api.repository2.unified.UnifiedRepositoryMalformedNameException;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode.DataPropertyType;
import org.pentaho.platform.api.repository2.unified.data.node.DataNodeRef;
import org.pentaho.platform.api.repository2.unified.data.node.DataProperty;
import org.pentaho.platform.api.repository2.unified.data.node.NodeRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.data.sample.SampleRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.repository2.ClientRepositoryPaths;
import org.pentaho.platform.repository2.locale.PentahoLocale;
import org.pentaho.platform.repository2.unified.jcr.JcrRepositoryDumpToFile;
import org.pentaho.platform.repository2.unified.jcr.JcrRepositoryDumpToFile.Mode;
import org.pentaho.platform.repository2.unified.jcr.JcrRepositoryFileUtils;
import org.pentaho.platform.repository2.unified.jcr.JcrStringHelper;
import org.pentaho.platform.repository2.unified.jcr.SimpleJcrTestUtils;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;

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
 * This is part of tests for create/modify/delete repository content and some utilities
 * 
 * @author mlowery
 * @author Aliaksei_Haidukou
 */
@RunWith( SpringJUnit4ClassRunner.class )
@SuppressWarnings( "nls" )
public class DefaultUnifiedRepositoryContentIT extends DefaultUnifiedRepositoryBase {
  // ~ Static fields/initializers
  // ======================================================================================

  // ~ Constructors
  // ====================================================================================================

  public DefaultUnifiedRepositoryContentIT() throws Exception {
    super();
  }

  // ~ Methods
  // =========================================================================================================
  @Override
  @Before
  public void setUp() throws Exception {
    super.initialize( Boolean.TRUE );

    IRepositoryVersionManager mockRepositoryVersionManager = mock( IRepositoryVersionManager.class );
    when( mockRepositoryVersionManager.isVersioningEnabled( anyString() ) ).thenReturn( true );
    when( mockRepositoryVersionManager.isVersionCommentEnabled( anyString() ) ).thenReturn( false );
    JcrRepositoryFileUtils.setRepositoryVersionManager( mockRepositoryVersionManager );
  }

  @Test
  public void testGetFileWithLoadedMaps() throws Exception {
    loginAsSysTenantAdmin();
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, PASSWORD, "", new String[] { tenantAdminRoleName } );
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
    loginAsSysTenantAdmin();
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, PASSWORD, "", new String[] { tenantAdminRoleName } );
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
    assertNotNull( updatedFile.getLocalePropertiesMap() );
    assertEquals( 3, updatedFile.getLocalePropertiesMap().size() );
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
    LocaleHelper.setLocale( US.getLocale() );
    updatedFile = repo.getFile( file.getPath(), null );

    assertEquals( EN_US_TITLE, updatedFile.getTitle() );
    assertEquals( EN_US_DESCRIPTION, updatedFile.getDescription() );

    logout();
  }

  @Test
  public void testLocalePropertiesMap() throws Exception {
    loginAsSysTenantAdmin();
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, PASSWORD, "", new String[] { tenantAdminRoleName } );
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
      @Override
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
      @Override
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
      @Override
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

  @Test
  public void testGetFileNotExist() throws Exception {
    loginAsSysTenantAdmin();
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_TIFFANY, PASSWORD, "", null );

    login( USERNAME_TIFFANY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    RepositoryFile file2 = repo.getFile( "/doesnotexist" );
    assertNull( file2 );
  }

  @Test
  public void testCreateFolder() throws Exception {
    loginAsSysTenantAdmin();
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, PASSWORD, "", null );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    RepositoryFile parentFolder = repo.getFile( ClientRepositoryPaths.getUserHomeFolderPath( USERNAME_SUZY ) );
    RepositoryFile newFolder =
        new RepositoryFile.Builder( "test" ).folder( true ).title( "title" ).hidden( true ).build();

    Date beginTime = Calendar.getInstance().getTime();

    // Sleep for 1 second for time comparison
    Thread.sleep( 1000 );
    newFolder = repo.createFolder( parentFolder.getId(), newFolder, null );
    Thread.sleep( 1000 );

    LocaleHelper.setLocale( new Locale( "de" ) );
    newFolder = repo.getFileById( newFolder.getId() ); // new request after change Locale

    Date endTime = Calendar.getInstance().getTime();
    assertTrue( beginTime.before( newFolder.getCreatedDate() ) );
    assertTrue( endTime.after( newFolder.getCreatedDate() ) );
    assertNotNull( newFolder );
    assertNotNull( newFolder.getId() );
    assertTrue( newFolder.isHidden() );
    assertFalse( newFolder.isAclNode() );
    assertEquals( "title", newFolder.getTitle() );
    assertNotNull( SimpleJcrTestUtils.getItem( testJcrTemplate, ServerRepositoryPaths.getUserHomeFolderPath(
        tenantAcme, USERNAME_SUZY )
        + "/test" ) );
  }

  @Test
  public void testCreateShadowFolder() throws Exception {
    loginAsSysTenantAdmin();
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, PASSWORD, "", null );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    RepositoryFile parentFolder = repo.getFile( ClientRepositoryPaths.getUserHomeFolderPath( USERNAME_SUZY ) );
    RepositoryFile newFolder = new RepositoryFile.Builder( "test" ).folder( true ).aclNode( true ).build();

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
    assertTrue( newFolder.isAclNode() );
    assertNotNull( SimpleJcrTestUtils.getItem( testJcrTemplate, ServerRepositoryPaths.getUserHomeFolderPath(
        tenantAcme, USERNAME_SUZY )
        + "/test" ) );
  }

  @Test
  public void testCreateFolderWithAtSymbol() throws Exception {
    loginAsSysTenantAdmin();
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, PASSWORD, "", null );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    RepositoryFile parentFolder = repo.getFile( ClientRepositoryPaths.getUserHomeFolderPath( USERNAME_SUZY ) );
    RepositoryFile newFolder = new RepositoryFile.Builder( "me@example.com" ).folder( true ).build();
    newFolder = repo.createFolder( parentFolder.getId(), newFolder, null );
    assertNotNull( newFolder );
    assertNotNull( repo.getFile( ClientRepositoryPaths.getUserHomeFolderPath( USERNAME_SUZY ) + "/me@example.com" ) );
    assertEquals( "me@example.com", repo.getFile(
        ClientRepositoryPaths.getUserHomeFolderPath( USERNAME_SUZY ) + "/me@example.com" ).getName() );
  }

  @Test( expected = UnifiedRepositoryException.class )
  public void testCreateFolderAtRootIllegal() throws Exception {
    loginAsSysTenantAdmin();
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, PASSWORD, "", null );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    RepositoryFile newFolder = new RepositoryFile.Builder( "test" ).folder( true ).build();
    repo.createFolder( null, newFolder, null );
  }

  @Test( expected = UnifiedRepositoryException.class )
  public void testCreateFileAtRootIllegal() throws Exception {
    loginAsSysTenantAdmin();
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, PASSWORD, "", null );

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
    loginAsSysTenantAdmin();
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, PASSWORD, "", null );

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
    loginAsSysTenantAdmin();
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, PASSWORD, "", null );

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
    loginAsSysTenantAdmin();
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, PASSWORD, "", null );

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
    loginAsSysTenantAdmin();
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, PASSWORD, "", null );

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
    loginAsSysTenantAdmin();
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, PASSWORD, "", null );

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
    DataNode newChild2 = node.addNode( JcrStringHelper.fileNameEncode( "pppq/qqs2" ) );
    newChild2.setProperty( JcrStringHelper.fileNameEncode( "ttt*ss4" ), "843skdfj33ksaljdfj" );

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

    assertTrue( foundNode.hasNode( JcrStringHelper.fileNameEncode( "pppq/qqs2" ) ) );
    DataNode foundChild2 = foundNode.getNode( JcrStringHelper.fileNameEncode( "pppq/qqs2" ) );
    assertNotNull( foundChild2.getId() );
    assertEquals( newChild2.getName(), foundChild2.getName() );
    assertEquals( newChild2.getProperty( JcrStringHelper.fileNameEncode( "ttt:ss4" ) ), foundChild2
        .getProperty( JcrStringHelper.fileNameEncode( "ttt:ss4" ) ) );
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
  public void testCreateACLNodeFile() throws Exception {
    loginAsSysTenantAdmin();
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, PASSWORD, "", null );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    final String expectedName = "aclnode";
    final String parentFolderPath = ClientRepositoryPaths.getUserHomeFolderPath( USERNAME_SUZY );
    RepositoryFile parentFolder = repo.getFile( parentFolderPath );
    final String expectedPath = parentFolderPath + RepositoryFile.SEPARATOR + expectedName;

    DataNode node = new DataNode( "kdjd" );
    DataNode newChild1 = node.addNode( "herfkmdx" );
    DataNode newChild2 = node.addNode( JcrStringHelper.fileNameEncode( "pppq/qqs2" ) );
    newChild2.setProperty( JcrStringHelper.fileNameEncode( "ttt*ss4" ), "843skdfj33ksaljdfj" );

    NodeRepositoryFileData data = new NodeRepositoryFileData( node );
    RepositoryFile newFile =
        repo.createFile( parentFolder.getId(), new RepositoryFile.Builder( expectedName ).aclNode( true ).build(), data,
            null );

    assertNotNull( newFile.getId() );
    RepositoryFile foundFile = repo.getFile( expectedPath );
    assertNotNull( foundFile );
    assertEquals( expectedName, foundFile.getName() );

    DataNode foundNode = repo.getDataForRead( foundFile.getId(), NodeRepositoryFileData.class ).getNode();

    assertEquals( node.getName(), foundNode.getName() );
    assertNotNull( foundNode.getId() );
    assertTrue( foundNode.hasNode( "herfkmdx" ) );
    DataNode foundChild1 = foundNode.getNode( "herfkmdx" );
    assertNotNull( foundChild1.getId() );
    assertEquals( newChild1.getName(), foundChild1.getName() );
    assertEquals( newChild1.getProperty( "shadow" ), foundChild1.getProperty( "shadow" ) );
  }

  @Test
  public void testCheckName() throws Exception {
    loginAsSysTenantAdmin();
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, PASSWORD, "", null );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    char[] jcrEncodedSymbols = { '%', '/', ':', '[', ']', '*', '|', '\t', '\r', '\n' };
    Set<Character> generalSetSymbols = new LinkedHashSet<Character>();
    for ( char c : jcrEncodedSymbols ) {
      generalSetSymbols.add( c );
    }
    for ( char c : JcrRepositoryFileUtils.getReservedChars() ) {
      generalSetSymbols.add( c );
    }

    for ( Character character : generalSetSymbols ) {
      testSymbol( character, true );
    }
  }

  private void testSymbol( char symbol, boolean isGood ) {
    DataNode goodNode = new DataNode( "node" );
    goodNode.setProperty( "property", "whatever" );
    NodeRepositoryFileData goodNodeData = new NodeRepositoryFileData( goodNode );

    DataNode badNode = new DataNode( "node" + symbol );
    badNode.setProperty( "property", "whatever" );
    NodeRepositoryFileData badNodeData = new NodeRepositoryFileData( badNode );

    DataNode goodNodeBadProp = new DataNode( "node" );
    goodNodeBadProp.setProperty( "property" + symbol, "whatever" );
    NodeRepositoryFileData goodNodeBadPropData = new NodeRepositoryFileData( goodNodeBadProp );

    final String parentFolderPath =
        ClientRepositoryPaths.getUserHomeFolderPath( PentahoSessionHolder.getSession().getName() );
    RepositoryFile parentFolder = repo.getFile( parentFolderPath );
    try {
      final String name = "folder" + symbol;
      final RepositoryFile folder =
          repo.createFolder( parentFolder.getId(), new RepositoryFile.Builder( name ).folder( true ).build(), null );
      failIfTrue( !isGood, symbol );
      assertEquals( name, folder.getName() );
    } catch ( UnifiedRepositoryMalformedNameException e ) {
      failIfTrue( isGood, symbol );
    }
    try {
      final String name = "file" + symbol;
      final RepositoryFile file =
          repo.createFile( parentFolder.getId(), new RepositoryFile.Builder( name ).build(), goodNodeData, null );
      failIfTrue( !isGood, symbol );
      assertEquals( name, file.getName() );
    } catch ( UnifiedRepositoryMalformedNameException e ) {
      failIfTrue( isGood, symbol );
    }
    try {
      final RepositoryFile file = repo.getFile( parentFolder.getPath() + RepositoryFile.SEPARATOR + "file" );
      if ( file != null ) {
        repo.deleteFile( file.getId(), null );
      }
      final RepositoryFile file1 =
          repo.createFile( parentFolder.getId(), new RepositoryFile.Builder( "file" ).build(), badNodeData, null );
      failIfTrue( !isGood, symbol );
      assertEquals( badNodeData.getNode().getName(), repo.getDataForRead( file1.getId(), NodeRepositoryFileData.class )
          .getNode().getName() );
    } catch ( UnifiedRepositoryMalformedNameException e ) {
      failIfTrue( isGood, symbol );
    }
    try {
      final RepositoryFile file = repo.getFile( parentFolder.getPath() + RepositoryFile.SEPARATOR + "file" );
      if ( file != null ) {
        repo.deleteFile( file.getId(), null );
      }
      final RepositoryFile file1 =
          repo.createFile( parentFolder.getId(), new RepositoryFile.Builder( "file" ).build(), goodNodeBadPropData,
              null );
      failIfTrue( !isGood, symbol );
      assertEquals( goodNodeBadPropData.getNode().getProperties().iterator().next().getName(), repo.getDataForRead(
          file1.getId(), NodeRepositoryFileData.class ).getNode().getProperties().iterator().next().getName() );
    } catch ( UnifiedRepositoryMalformedNameException e ) {
      failIfTrue( isGood, symbol );
    }
  }

  private void failIfTrue( boolean isGood, char symbol ) {
    if ( isGood ) {
      fail( String.format( "Symbol '%s' crashed the test", symbol ) );
    }
  }

  @Test( expected = UnifiedRepositoryException.class )
  public void testCreateFileUnrecognizedContentType() throws Exception {
    loginAsSysTenantAdmin();
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, PASSWORD, "", null );

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
    loginAsSysTenantAdmin();
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, PASSWORD, "", null );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    final String rootFolderID = String.valueOf( repo.getFile( ClientRepositoryPaths.getRootFolderPath() ).getId() );
    List<RepositoryFile> children = repo.getChildren( new RepositoryRequest( rootFolderID, true, -1, null ) );
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
    children = repo.getChildren( new RepositoryRequest( rootFolderID, true, -1, null ) );
    assertEquals( 3, children.size() );
    children = repo.getChildren( new RepositoryRequest( rootFolderID, true, -1, "*" ) );
    assertEquals( 3, children.size() );
    children = repo.getChildren( new RepositoryRequest( rootFolderID, true, -1, "*me" ) );
    assertEquals( 1, children.size() );
    children = repo.getChildren( new RepositoryRequest( rootFolderID, true, -1, "*Z*" ) );
    assertEquals( 0, children.size() );
  }

  /**
   * A user should only be able to see his home folder (unless your the admin).
   */
  // @Test
  public void testListHomeFolders() throws Exception {
    loginAsSysTenantAdmin();
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, PASSWORD, "", null );
    userRoleDao.createUser( tenantAcme, USERNAME_TIFFANY, PASSWORD, "", null );

    login( USERNAME_TIFFANY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    List<RepositoryFile> children =
        repo.getChildren( new RepositoryRequest( String.valueOf( repo.getFile(
            ClientRepositoryPaths.getHomeFolderPath() ).getId() ), true, -1, null ) );
    assertEquals( 1, children.size() );
  }

  @Test
  public void testUpdateFile() throws Exception {
    loginAsSysTenantAdmin();
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, PASSWORD, "", null );

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
    loginAsSysTenantAdmin();
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, PASSWORD, "", null );

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
    loginAsSysTenantAdmin();
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, PASSWORD, "", null );

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
      // ignore
    }
    assertFalse( SimpleJcrTestUtils.isCheckedOut( testJcrTemplate, expectedAbsolutePath ) );
  }

  @Test( expected = UnifiedRepositoryException.class )
  public void testCreateDuplicateFolder() throws Exception {
    loginAsSysTenantAdmin();
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, PASSWORD, "", null );

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
    loginAsSysTenantAdmin();
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );

    final String parentFolderPath = ClientRepositoryPaths.getPublicFolderPath();
    assertNotNull( createSampleFile( parentFolderPath, "helloworld.sample", "Hello World!", false, 500 ) );
  }

  @Test
  public void testLockFile() throws Exception {
    loginAsSysTenantAdmin();
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, PASSWORD, "", null );
    userRoleDao.createUser( tenantAcme, USERNAME_TIFFANY, PASSWORD, "", null );

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

    loginAsSysTenantAdmin();
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, PASSWORD, "", null );
    userRoleDao.createUser( tenantAcme, USERNAME_TIFFANY, PASSWORD, "", null );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    String parentFolderPath = ClientRepositoryPaths.getUserHomeFolderPath( PentahoSessionHolder.getSession().getName() );
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
    assertEquals( newFile.getId(), deletedFiles.get( 0 ).getId() );

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
      // ignore
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
   * This test exists to prove that the server wasn't the source of a problem. I'm leaving it in.
   */
  @Test
  public void testWeird1() throws Exception {
    loginAsSysTenantAdmin();
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, PASSWORD, "", null );

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
    loginAsSysTenantAdmin();
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, PASSWORD, "", null );

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
  public void testWriteOnFileToMove() throws Exception {
    loginAsSysTenantAdmin();
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, PASSWORD, "", null );
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
      // ignore
    }
  }

  @Test
  public void testMoveFile() throws Exception {
    loginAsSysTenantAdmin();
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, PASSWORD, "", null );

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
    repo.moveFile( testFolder.getId(), moveTest2Folder.getPath() + RepositoryFile.SEPARATOR + testFolder.getName(),
        null );
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
    loginAsSysTenantAdmin();
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, PASSWORD, "", null );

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
    loginAsSysTenantAdmin();
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, PASSWORD, "", null );

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
    loginAsSysTenantAdmin();
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, PASSWORD, "", null );

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
    repo.copyFile( testFolder1.getId(), testFolder2.getPath() + RepositoryFile.SEPARATOR + testFolder1.getName(), null );
    assertNotNull( repo.getFile( testFolder2.getPath() + RepositoryFile.SEPARATOR + "testfile2" ) );
    assertNotNull( repo.getFile( testFolder2.getPath() + RepositoryFile.SEPARATOR + "testfolder1" ) );
    assertNotNull( repo.getFile( testFolder2.getPath() + RepositoryFile.SEPARATOR + "testfolder1"
        + RepositoryFile.SEPARATOR + "testfile1" ) );
  }

  @Test
  public void testCopyFile() throws Exception {
    loginAsSysTenantAdmin();
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, PASSWORD, "", null );

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
    repo.copyFile( testFolder.getId(), copyTest2Folder.getPath() + RepositoryFile.SEPARATOR + testFolder.getName(),
        null );
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
    loginAsSysTenantAdmin();
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, PASSWORD, "", null );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    RepositoryFile rootFolder = repo.getFile( "/" );
    assertNotNull( rootFolder );
    assertEquals( "", rootFolder.getName() );
    assertNotNull( rootFolder.getId() );
    assertNotNull( repo.getChildren( new RepositoryRequest( String.valueOf( rootFolder.getId() ), true, -1, null ) ) );
    RepositoryFileAcl rootFolderAcl = repo.getAcl( rootFolder.getId() );
  }

  @Test
  public void testGetTree() throws Exception {
    loginAsSysTenantAdmin();
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, PASSWORD, "", null );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    RepositoryFileTree root =
        repo.getTree( new RepositoryRequest( ClientRepositoryPaths.getRootFolderPath(), true, 0, null ) );
    assertNotNull( root.getFile() );
    assertNull( root.getChildren() );

    root = repo.getTree( new RepositoryRequest( ClientRepositoryPaths.getRootFolderPath(), true, 1, null ) );
    assertNotNull( root.getFile() );
    assertNotNull( root.getChildren() );
    assertFalse( root.getChildren().isEmpty() );
    assertNull( root.getChildren().get( 0 ).getChildren() );

    root = repo.getTree( new RepositoryRequest( ClientRepositoryPaths.getHomeFolderPath(), true, -1, null ) );
    assertNotNull( root.getFile() );
    assertNotNull( root.getChildren() );
    assertFalse( root.getChildren().isEmpty() );
    assertTrue( root.getChildren().get( 0 ).getChildren().isEmpty() );

    root = repo.getTree( new RepositoryRequest( ClientRepositoryPaths.getHomeFolderPath(), true, -1, "*uz*" ) );
    assertEquals( 1, root.getChildren().size() );
  }

  @Test
  public void testGetTreeWithFileTypeFilter() throws Exception {
    RepositoryFileTree root = null;
    loginAsSysTenantAdmin();

    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, PASSWORD, "", null );

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

    root = repo.getTree( new RepositoryRequest( publicFolder.getPath(), true, 1, "*|FILES" ) );
    assertFalse( root.getChildren().isEmpty() );
    assertEquals( 1, root.getChildren().size() );
    assertEquals( "helloworld.xaction", root.getChildren().get( 0 ).getFile().getName() );

    root = repo.getTree( new RepositoryRequest( publicFolder.getPath(), true, 1, "*" ) );
    assertFalse( root.getChildren().isEmpty() );
    assertEquals( 2, root.getChildren().size() );

    root = repo.getTree( new RepositoryRequest( publicFolder.getPath(), true, 1, "*|FILES_FOLDERS" ) );
    assertFalse( root.getChildren().isEmpty() );
    assertEquals( 2, root.getChildren().size() );

    root = repo.getTree( new RepositoryRequest( publicFolder.getPath(), true, 1, "*|FOLDERS" ) );
    assertFalse( root.getChildren().isEmpty() );
    assertEquals( 1, root.getChildren().size() );
    assertEquals( "testFolder", root.getChildren().get( 0 ).getFile().getName() );

  }

  @Test
  public void testGetTreeWithShowHidden() throws Exception {
    RepositoryFileTree root = null;
    loginAsSysTenantAdmin();
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, PASSWORD, "", null );

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
    root = repo.getTree( new RepositoryRequest( publicFolder.getPath(), true, -1, null ) );
    assertFalse( root.getChildren().isEmpty() );
    root = repo.getTree( new RepositoryRequest( publicFolder.getPath(), false, -1, null ) );
    assertTrue( root.getChildren().isEmpty() );
  }

  @Test
  public void testGetDataForReadInBatch_unversioned() throws Exception {
    loginAsSysTenantAdmin();
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, PASSWORD, "", null );

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

    loginAsSysTenantAdmin();
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, PASSWORD, "", null );

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
    loginAsSysTenantAdmin();
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, PASSWORD, "", null );

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
  public void testGetReservedChars() throws Exception {
    assertFalse( repo.getReservedChars().isEmpty() );
  }

  @Test
  public void testMoveEncoded() throws Exception {
    loginAsSysTenantAdmin();
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, PASSWORD, "", null );
    defaultBackingRepositoryLifecycleManager.newTenant();
    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    RepositoryFile parentFolder =
        repo.getFile( ClientRepositoryPaths.getUserHomeFolderPath( PentahoSessionHolder.getSession().getName() ) );
    RepositoryFile srcFolder = new RepositoryFile.Builder( "src" ).folder( true ).build();
    RepositoryFile destFolder = new RepositoryFile.Builder( "dest" ).folder( true ).build();
    srcFolder = repo.createFolder( parentFolder.getId(), srcFolder, null );
    destFolder = repo.createFolder( parentFolder.getId(), destFolder, null );

    RepositoryFile newFile = createSampleFile( srcFolder.getPath(), "_x0039__x0020_rows.prpt", "ddfdf", false, 83 );
    repo.moveFile( newFile.getId(), destFolder.getPath(), null );
    assertEquals( "/home/suzy/dest/_x0039__x0020_rows.prpt", repo.getFileById( newFile.getId() ).getPath() );
  }

  private RepositoryFile createSimpleFile( final Serializable parentFolderId, final String fileName ) throws Exception {
    final String dataString = "Hello World!";
    final String encoding = "UTF-8";
    byte[] data = dataString.getBytes( encoding );
    ByteArrayInputStream dataStream = new ByteArrayInputStream( data );
    final SimpleRepositoryFileData content = new SimpleRepositoryFileData( dataStream, encoding, "text/plain" );
    return repo.createFile( parentFolderId, new RepositoryFile.Builder( fileName ).build(), content, null );
  }
}
