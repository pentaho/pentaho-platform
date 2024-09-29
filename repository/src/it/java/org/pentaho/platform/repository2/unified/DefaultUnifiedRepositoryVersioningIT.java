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

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.repository2.unified.IRepositoryVersionManager;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.UnifiedRepositoryException;
import org.pentaho.platform.api.repository2.unified.VersionSummary;
import org.pentaho.platform.api.repository2.unified.data.sample.SampleRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.repository2.ClientRepositoryPaths;
import org.pentaho.platform.repository2.unified.jcr.JcrRepositoryFileUtils;
import org.pentaho.platform.repository2.unified.jcr.SimpleJcrTestUtils;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Integration test. Tests {@link DefaultUnifiedRepository} and {@link org.pentaho.platform.api.engine.IAuthorizationPolicy IAuthorizationPolicy} fully configured
 * behind Spring Security's method security and Spring's transaction interceptor.
 *
 * <p>
 * Note the RunWith annotation that uses a special runner that knows how to setup a Spring application context. The
 * application context config files are listed in the ContextConfiguration annotation. By implementing
 * {@link org.springframework.context.ApplicationContextAware ApplicationContextAware}, this unit test can access various beans defined in the application context,
 * including the bean under test.
 * </p>
 *
 * This is part of tests for Versioning check purposes
 *
 * @author mlowery
 * @author Aliaksei_Haidukou
 */
@RunWith( SpringJUnit4ClassRunner.class )
@SuppressWarnings( "nls" )
public class DefaultUnifiedRepositoryVersioningIT extends DefaultUnifiedRepositoryBase {
  @Before
  public void setup() {
    IRepositoryVersionManager mockRepositoryVersionManager = mock( IRepositoryVersionManager.class );
    when( mockRepositoryVersionManager.isVersioningEnabled( anyString() ) ).thenReturn( true );
    when( mockRepositoryVersionManager.isVersionCommentEnabled( anyString() ) ).thenReturn( false );
    JcrRepositoryFileUtils.setRepositoryVersionManager( mockRepositoryVersionManager );
  }
  
  @Test
  public void testCreateVersionedFolder() throws Exception {
    loginAsSysTenantAdmin();
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, PASSWORD, "", null );

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
    loginAsSysTenantAdmin();
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, PASSWORD, "", null );

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
  public void testDeleteFileAtVersion() throws Exception {
    // Startup and login to repository
    loginAsSysTenantAdmin();
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, PASSWORD, "", null );

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

    assertEquals( expectedDataString, IOUtils.toString( result.getInputStream(), expectedEncoding ) );
    assertEquals( expectedModDataString, IOUtils.toString( modResult.getInputStream(), expectedEncoding ) );

    // Remove first version
    repo.deleteFileAtVersion( foundFile.getId(), origVerList.get( 0 ).getId() );

    // Verify version removal
    List<VersionSummary> newVerList = repo.getVersionSummaries( foundFile.getId() );
    assertEquals( 1, newVerList.size() );

    SimpleRepositoryFileData newModResult =
        repo.getDataAtVersionForRead( foundFile.getId(), newVerList.get( 0 ).getId(), SimpleRepositoryFileData.class );

    assertEquals( expectedModDataString, IOUtils.toString( newModResult.getInputStream(), expectedEncoding ) );
  }

  @Test
  public void testRestoreFileAtVersion() throws Exception {
    // Startup and login to repository
    loginAsSysTenantAdmin();
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, PASSWORD, "", null );

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

    assertEquals( expectedDataString, IOUtils.toString( result.getInputStream(), expectedEncoding ) );
    assertEquals( expectedModDataString, IOUtils.toString( modResult.getInputStream(), expectedEncoding ) );

    // Restore first version
    repo.restoreFileAtVersion( foundFile.getId(), origVerList.get( 0 ).getId(), "restore version" );

    // Verify version restoration
    List<VersionSummary> newVerList = repo.getVersionSummaries( foundFile.getId() );
    assertEquals( 3, newVerList.size() );

    SimpleRepositoryFileData newOrigResult = repo.getDataForRead( foundFile.getId(), SimpleRepositoryFileData.class );

    assertEquals( expectedDataString, IOUtils.toString( newOrigResult.getInputStream(), expectedEncoding ) );
  }

  @Test
  public void testGetVersionSummaries() throws Exception {
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
    loginAsSysTenantAdmin();
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, PASSWORD, "", null );
    userRoleDao.createUser( tenantAcme, USERNAME_TIFFANY, PASSWORD, "", null );

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
  public void testGetDataForReadInBatch_versioned() throws Exception {
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
  public void testGetVersionSummaryInBatch() throws Exception {
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
}
