/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.platform.web.http.api.resources.services;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.IPentahoObjectReference;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ISystemConfig;
import org.pentaho.platform.api.engine.ISystemSettings;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.webservices.RepositoryFileDto;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.importexport.BaseExportProcessor;
import org.pentaho.platform.plugin.services.importexport.ExportHandler;
import org.pentaho.platform.repository2.ClientRepositoryPaths;
import org.pentaho.platform.repository2.unified.fileio.RepositoryFileOutputStream;
import org.pentaho.platform.repository2.unified.webservices.DefaultUnifiedRepositoryWebService;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryCreateAction;

import javax.ws.rs.core.StreamingOutput;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.channels.IllegalSelectorException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;
import java.util.LinkedList;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FileServiceTest {

  private static final String PATH_CONTROL_CHARACTER = "Create Control Character \u0017 File.xml";
  private static final String PATH_SPECIAL_CHARACTERS = "éÉèÈçÇºªüÜ@£§";
  private static final String PATH_JAPANESE_CHARACTERS = "キャラクター";
  public static final String UTF_8 = StandardCharsets.UTF_8.name();

  private FileService fileService;

  @BeforeClass
  public static void init() {
    PentahoSystem.init();
  }

  @Before
  public void setUp() throws Exception {
    fileService = spy( FileService.class );
    final ISystemSettings settingsService = mock( ISystemSettings.class );
    when( settingsService.getSystemSetting( eq( "anonymous-authentication/anonymous-user" ), eq( "anonymousUser" ) ) ).thenReturn( "anonymousUser" );
    PentahoSystem.setSystemSettingsService( settingsService );
    final ISystemConfig systemConfig = mock( ISystemConfig.class );

    final IPentahoObjectFactory objectFactory = mock( IPentahoObjectFactory.class );
    when( objectFactory.objectDefined( eq( ISystemConfig.class ) ) ).thenReturn( true );
    final IPentahoObjectReference pentahoObjectReference = mock( IPentahoObjectReference.class );
    when( pentahoObjectReference.getObject() ).thenReturn( systemConfig );
    try {
      when( objectFactory.getObjectReferences( eq( ISystemConfig.class ), nullable( IPentahoSession.class ),
          nullable( Map.class ) ) ).thenReturn( new LinkedList() { {
              add( pentahoObjectReference );
            } } );
    } catch ( ObjectFactoryException e ) {
      e.printStackTrace();
    }
    when( settingsService.getSystemSetting( nullable( String.class ), nullable( String.class ) ) ).thenReturn( "" );
    PentahoSystem.registerObjectFactory( objectFactory );
  }

  @Test( expected = FileService.InvalidNameException.class )
  public void testCreateFile_Forbidden_ControlCharactersFound() throws Exception {
    InputStream inputStream = mock( InputStream.class );

    fileService.createFile( UTF_8, PATH_CONTROL_CHARACTER, inputStream );
    verify( fileService, times( 0 ) ).idToPath( PATH_CONTROL_CHARACTER );
  }

  @Test
  public void testCreateFile_Special_Characters() throws Exception {
    InputStream inputStream = mock( InputStream.class );
    RepositoryFileOutputStream repositoryFileOutputStream = mock( RepositoryFileOutputStream.class );

    doReturn( repositoryFileOutputStream ).when( fileService ).getRepositoryFileOutputStream( nullable( String.class ) );
    doReturn( 1 ).when( fileService ).copy( inputStream, repositoryFileOutputStream );

    fileService.createFile( UTF_8, PATH_SPECIAL_CHARACTERS, inputStream );
    verify( inputStream ).close();
  }

  @Test
  public void testCreateFile_Japanese_Characters() throws Exception {
    InputStream inputStream = mock( InputStream.class );
    RepositoryFileOutputStream repositoryFileOutputStream = mock( RepositoryFileOutputStream.class );

    doReturn( repositoryFileOutputStream ).when( fileService ).getRepositoryFileOutputStream( nullable( String.class ) );
    doReturn( 1 ).when( fileService ).copy( inputStream, repositoryFileOutputStream );

    fileService.createFile( UTF_8, PATH_JAPANESE_CHARACTERS, inputStream );
    verify( inputStream ).close();
  }

  @Test( expected = FileService.InvalidNameException.class )
  public void testDoCreateDirSafe_ControlCharactersFound() throws Exception {
    DefaultUnifiedRepositoryWebService RepoWs = mock( DefaultUnifiedRepositoryWebService.class );
    doReturn( RepoWs ).when( fileService ).getRepoWs();

    fileService.doCreateDirSafe( PATH_CONTROL_CHARACTER );
  }

  @Test
  public void testDoCreateDirSafe_Special_Characters() throws Exception {
    DefaultUnifiedRepositoryWebService RepoWs = mock( DefaultUnifiedRepositoryWebService.class );
    doReturn( RepoWs ).when( fileService ).getRepoWs();
    doReturn( true ).when( fileService ).doCreateDirFor( nullable( String.class ) );

    assertTrue( fileService.doCreateDirSafe( PATH_SPECIAL_CHARACTERS ) );
  }

  @Test
  public void testDoCreateDirSafe_Japanese_Characters() throws Exception {
    DefaultUnifiedRepositoryWebService RepoWs = mock( DefaultUnifiedRepositoryWebService.class );
    doReturn( RepoWs ).when( fileService ).getRepoWs();
    doReturn( true ).when( fileService ).doCreateDirFor( nullable( String.class ) );

    assertTrue( fileService.doCreateDirSafe( PATH_JAPANESE_CHARACTERS ) );
  }

  // region doGetFileOrDirAsDownload

  private void setupRepositoryMock( String path, RepositoryFile file ) {
    org.pentaho.platform.api.repository2.unified.IUnifiedRepository mockRepo =
      mock( org.pentaho.platform.api.repository2.unified.IUnifiedRepository.class );
    when( mockRepo.getFile( path ) ).thenReturn( file );
    doReturn( mockRepo ).when( fileService ).getRepository();
  }

  @Test( expected = InvalidParameterException.class )
  public void testDoGetFileOrDirAsDownload_NullPathId() throws Throwable {
    fileService.doGetFileOrDirAsDownload( "userAgent", null, "true" );
  }

  @Test( expected = InvalidParameterException.class )
  public void testDoGetFileOrDirAsDownload_EmptyPathId() throws Throwable {
    fileService.doGetFileOrDirAsDownload( "userAgent", "", "false" );
  }

  @Test( expected = IllegalSelectorException.class )
  public void testDoGetFileOrDirAsDownload_InvalidPath() throws Throwable {
    String pathId = ":invalid:path";
    String path = fileService.idToPath( pathId );
    doReturn( false ).when( fileService ).isPathValid( path );

    fileService.doGetFileOrDirAsDownload( "userAgent", pathId, "true" );
  }

  @Test( expected = FileNotFoundException.class )
  public void testDoGetFileOrDirAsDownload_FileNotFound() throws Throwable {
    String pathId = ":home:user:file.txt";
    String path = fileService.idToPath( pathId );

    doReturn( true ).when( fileService ).isPathValid( path );
    setupRepositoryMock( path, null );

    fileService.doGetFileOrDirAsDownload( "userAgent", pathId, "true" );
  }

  @Test( expected = PentahoAccessControlException.class )
  public void testDoGetFileOrDirAsDownload_NoDownloadPermission() throws Throwable {
    String pathId = ":home:user:file.txt";
    String path = fileService.idToPath( pathId );
    RepositoryFile mockFile = mock( RepositoryFile.class );

    doReturn( true ).when( fileService ).isPathValid( path );
    setupRepositoryMock( path, mockFile );

    doThrow( new PentahoAccessControlException( "Access denied" ) )
      .when( fileService ).validateDownloadAccess( path );

    fileService.doGetFileOrDirAsDownload( "userAgent", pathId, "true" );
  }

  @Test
  public void testDoGetFileOrDirAsDownload_File_WithManifest() throws Throwable {
    String pathId = ":home:user:file.txt";
    String path = fileService.idToPath( pathId );
    RepositoryFile mockFile = mock( RepositoryFile.class );
    when( mockFile.isFolder() ).thenReturn( false );
    when( mockFile.getName() ).thenReturn( "file.txt" );

    BaseExportProcessor mockProcessor = mock( BaseExportProcessor.class );
    ExportHandler mockHandler = mock( ExportHandler.class );
    StreamingOutput mockStream = mock( StreamingOutput.class );

    doReturn( true ).when( fileService ).isPathValid( path );
    setupRepositoryMock( path, mockFile );
    doNothing().when( fileService ).validateDownloadAccess( path );
    doReturn( mockProcessor ).when( fileService ).getDownloadExportProcessor( path, true, true );
    doReturn( mockHandler ).when( fileService ).getDownloadExportHandler();
    doReturn( mockStream ).when( fileService ).getDownloadStream( mockFile, mockProcessor );

    FileService.DownloadFileWrapper result = fileService.doGetFileOrDirAsDownload( "userAgent", pathId, "true" );

    assertNotNull( result );
    assertEquals( mockStream, result.getOutputStream() );
    assertEquals( "file.txt.zip", result.getEncodedFileName() );
    verify( mockProcessor ).addExportHandler( mockHandler );
    verify( fileService ).getDownloadExportProcessor( path, true, true );
  }

  @Test
  public void testDoGetFileOrDirAsDownload_File_WithoutManifest() throws Throwable {
    String pathId = ":home:user:document.pdf";
    String path = fileService.idToPath( pathId );
    RepositoryFile mockFile = mock( RepositoryFile.class );
    when( mockFile.isFolder() ).thenReturn( false );
    when( mockFile.getName() ).thenReturn( "document.pdf" );

    BaseExportProcessor mockProcessor = mock( BaseExportProcessor.class );
    ExportHandler mockHandler = mock( ExportHandler.class );
    StreamingOutput mockStream = mock( StreamingOutput.class );

    doReturn( true ).when( fileService ).isPathValid( path );
    setupRepositoryMock( path, mockFile );
    doNothing().when( fileService ).validateDownloadAccess( path );
    doReturn( mockProcessor ).when( fileService ).getDownloadExportProcessor( path, false, false );
    doReturn( mockHandler ).when( fileService ).getDownloadExportHandler();
    doReturn( mockStream ).when( fileService ).getDownloadStream( mockFile, mockProcessor );

    FileService.DownloadFileWrapper result = fileService.doGetFileOrDirAsDownload( "userAgent", pathId, "false" );

    assertNotNull( result );
    assertEquals( mockStream, result.getOutputStream() );
    assertEquals( "document.pdf", result.getEncodedFileName() );
    verify( mockProcessor ).addExportHandler( mockHandler );
    verify( fileService ).getDownloadExportProcessor( path, false, false );
  }

  @Test
  public void testDoGetFileOrDirAsDownload_Folder_WithManifest() throws Throwable {
    String pathId = ":home:user:myFolder";
    String path = fileService.idToPath( pathId );
    RepositoryFile mockFolder = mock( RepositoryFile.class );
    when( mockFolder.isFolder() ).thenReturn( true );
    when( mockFolder.getName() ).thenReturn( "myFolder" );

    BaseExportProcessor mockProcessor = mock( BaseExportProcessor.class );
    ExportHandler mockHandler = mock( ExportHandler.class );
    StreamingOutput mockStream = mock( StreamingOutput.class );

    doReturn( true ).when( fileService ).isPathValid( path );
    setupRepositoryMock( path, mockFolder );
    doNothing().when( fileService ).validateDownloadAccess( path );
    doReturn( mockProcessor ).when( fileService ).getDownloadExportProcessor( path, true, true );
    doReturn( mockHandler ).when( fileService ).getDownloadExportHandler();
    doReturn( mockStream ).when( fileService ).getDownloadStream( mockFolder, mockProcessor );

    FileService.DownloadFileWrapper result = fileService.doGetFileOrDirAsDownload( "userAgent", pathId, "true" );

    assertNotNull( result );
    assertEquals( mockStream, result.getOutputStream() );
    assertEquals( "myFolder.zip", result.getEncodedFileName() );
    verify( mockProcessor ).addExportHandler( mockHandler );
    verify( fileService ).getDownloadExportProcessor( path, true, true );
  }

  @Test
  public void testDoGetFileOrDirAsDownload_Folder_WithoutManifest() throws Throwable {
    String pathId = ":public:reports";
    String path = fileService.idToPath( pathId );
    RepositoryFile mockFolder = mock( RepositoryFile.class );
    when( mockFolder.isFolder() ).thenReturn( true );
    when( mockFolder.getName() ).thenReturn( "reports" );

    BaseExportProcessor mockProcessor = mock( BaseExportProcessor.class );
    ExportHandler mockHandler = mock( ExportHandler.class );
    StreamingOutput mockStream = mock( StreamingOutput.class );

    doReturn( true ).when( fileService ).isPathValid( path );
    setupRepositoryMock( path, mockFolder );
    doNothing().when( fileService ).validateDownloadAccess( path );
    doReturn( mockProcessor ).when( fileService ).getDownloadExportProcessor( path, true, false );
    doReturn( mockHandler ).when( fileService ).getDownloadExportHandler();
    doReturn( mockStream ).when( fileService ).getDownloadStream( mockFolder, mockProcessor );

    FileService.DownloadFileWrapper result = fileService.doGetFileOrDirAsDownload( "userAgent", pathId, "false" );

    assertNotNull( result );
    assertEquals( mockStream, result.getOutputStream() );
    assertEquals( "reports.zip", result.getEncodedFileName() );
    verify( mockProcessor ).addExportHandler( mockHandler );
    verify( fileService ).getDownloadExportProcessor( path, true, false );
  }

  @Test
  public void testDoGetFileOrDirAsDownload_File_SpecialCharacters() throws Throwable {
    String fileName = "file name with spaces & special.txt";
    String pathId = ":home:user:" + fileName;
    String path = fileService.idToPath( pathId );
    RepositoryFile mockFile = mock( RepositoryFile.class );
    when( mockFile.isFolder() ).thenReturn( false );
    when( mockFile.getName() ).thenReturn( fileName );

    BaseExportProcessor mockProcessor = mock( BaseExportProcessor.class );
    ExportHandler mockHandler = mock( ExportHandler.class );
    StreamingOutput mockStream = mock( StreamingOutput.class );

    doReturn( true ).when( fileService ).isPathValid( path );
    setupRepositoryMock( path, mockFile );
    doNothing().when( fileService ).validateDownloadAccess( path );
    doReturn( mockProcessor ).when( fileService ).getDownloadExportProcessor( path, false, false );
    doReturn( mockHandler ).when( fileService ).getDownloadExportHandler();
    doReturn( mockStream ).when( fileService ).getDownloadStream( mockFile, mockProcessor );

    FileService.DownloadFileWrapper result = fileService.doGetFileOrDirAsDownload( "userAgent", pathId, "false" );

    assertNotNull( result );
    assertEquals( mockStream, result.getOutputStream() );

    // Derive expected encoding using the same logic as production (URLEncoder + replace + with %20)
    String expectedEncodedFileName = URLEncoder.encode( fileName, StandardCharsets.UTF_8 )
      .replaceAll( "\\+", "%20" );
    assertEquals( expectedEncodedFileName, result.getEncodedFileName() );

    // Validate round-trip: decode and compare back to original
    String decodedFileName = URLDecoder.decode( result.getEncodedFileName(), StandardCharsets.UTF_8 );
    assertEquals( fileName, decodedFileName );
  }

  @Test
  public void testDoGetFileOrDirAsDownload_File_NullStrWithManifest() throws Throwable {
    String pathId = ":home:user:file.txt";
    String path = fileService.idToPath( pathId );
    RepositoryFile mockFile = mock( RepositoryFile.class );
    when( mockFile.isFolder() ).thenReturn( false );
    when( mockFile.getName() ).thenReturn( "file.txt" );

    BaseExportProcessor mockProcessor = mock( BaseExportProcessor.class );
    ExportHandler mockHandler = mock( ExportHandler.class );
    StreamingOutput mockStream = mock( StreamingOutput.class );

    doReturn( true ).when( fileService ).isPathValid( path );
    setupRepositoryMock( path, mockFile );
    doNothing().when( fileService ).validateDownloadAccess( path );
    doReturn( mockProcessor ).when( fileService ).getDownloadExportProcessor( path, true, true );
    doReturn( mockHandler ).when( fileService ).getDownloadExportHandler();
    doReturn( mockStream ).when( fileService ).getDownloadStream( mockFile, mockProcessor );

    FileService.DownloadFileWrapper result = fileService.doGetFileOrDirAsDownload( "userAgent", pathId, null );

    assertNotNull( result );
    assertEquals( mockStream, result.getOutputStream() );
    assertEquals( "file.txt.zip", result.getEncodedFileName() );
    verify( fileService ).getDownloadExportProcessor( path, true, true );
  }

  @Test
  public void testDoGetFileOrDirAsDownload_File_EmptyStrWithManifest() throws Throwable {
    String pathId = ":home:user:test.xml";
    String path = fileService.idToPath( pathId );
    RepositoryFile mockFile = mock( RepositoryFile.class );
    when( mockFile.isFolder() ).thenReturn( false );
    when( mockFile.getName() ).thenReturn( "test.xml" );

    BaseExportProcessor mockProcessor = mock( BaseExportProcessor.class );
    ExportHandler mockHandler = mock( ExportHandler.class );
    StreamingOutput mockStream = mock( StreamingOutput.class );

    doReturn( true ).when( fileService ).isPathValid( path );
    setupRepositoryMock( path, mockFile );
    doNothing().when( fileService ).validateDownloadAccess( path );
    doReturn( mockProcessor ).when( fileService ).getDownloadExportProcessor( path, true, true );
    doReturn( mockHandler ).when( fileService ).getDownloadExportHandler();
    doReturn( mockStream ).when( fileService ).getDownloadStream( mockFile, mockProcessor );

    FileService.DownloadFileWrapper result = fileService.doGetFileOrDirAsDownload( "userAgent", pathId, "" );

    assertNotNull( result );
    assertEquals( mockStream, result.getOutputStream() );
    assertEquals( "test.xml.zip", result.getEncodedFileName() );
    verify( fileService ).getDownloadExportProcessor( path, true, true );
  }

  // endregion

  @Test
  public void testIsValidFolderName_DecodedControl_Characters() throws Exception {
    DefaultUnifiedRepositoryWebService RepoWs = mock( DefaultUnifiedRepositoryWebService.class );
    doReturn( RepoWs ).when( fileService ).getRepoWs();

    assertFalse( fileService.isValidFolderName( PATH_CONTROL_CHARACTER ) );
  }

  @Test
  public void testIsValidFolderName_EncodedControl_Characters() throws Exception {
    DefaultUnifiedRepositoryWebService RepoWs = mock( DefaultUnifiedRepositoryWebService.class );
    doReturn( RepoWs ).when( fileService ).getRepoWs();

    assertFalse(
      fileService.isValidFolderName( encode( PATH_CONTROL_CHARACTER ) ) );
  }

  @Test
  public void testIsValidFolderName_DecodedJapanese_Characters() throws Exception {
    DefaultUnifiedRepositoryWebService RepoWs = mock( DefaultUnifiedRepositoryWebService.class );
    doReturn( RepoWs ).when( fileService ).getRepoWs();

    assertTrue( fileService.isValidFolderName( PATH_JAPANESE_CHARACTERS ) );
  }

  @Test
  public void testIsValidFolderName_EncodedJapanese_Characters() throws Exception {
    DefaultUnifiedRepositoryWebService RepoWs = mock( DefaultUnifiedRepositoryWebService.class );
    doReturn( RepoWs ).when( fileService ).getRepoWs();

    assertTrue(
      fileService.isValidFolderName( encode( PATH_JAPANESE_CHARACTERS ) ) );
  }

  @Test
  public void testIsValidFolderName_DecodedSpecial_Characters() throws Exception {
    DefaultUnifiedRepositoryWebService RepoWs = mock( DefaultUnifiedRepositoryWebService.class );
    doReturn( RepoWs ).when( fileService ).getRepoWs();

    assertTrue( fileService.isValidFolderName( PATH_SPECIAL_CHARACTERS ) );
  }

  @Test
  public void testIsValidFolderName_EncodedSpecial_Characters() throws Exception {
    DefaultUnifiedRepositoryWebService RepoWs = mock( DefaultUnifiedRepositoryWebService.class );
    doReturn( RepoWs ).when( fileService ).getRepoWs();

    assertTrue(
      fileService.isValidFolderName( encode( PATH_SPECIAL_CHARACTERS ) ) );
  }

  @Test
  public void testIsValidFolderName_Decoded_InvalidCases() throws Exception {
    DefaultUnifiedRepositoryWebService RepoWs = mock( DefaultUnifiedRepositoryWebService.class );
    doReturn( RepoWs ).when( fileService ).getRepoWs();
    String[] invalidNames = { ".", ".." };
    for ( String invalidName : invalidNames ) {
      assertFalse( fileService.isValidFolderName( invalidName ) );
    }
  }

  @Test
  public void testIsValidFolderName_Encoded_InvalidCases() throws Exception {
    DefaultUnifiedRepositoryWebService RepoWs = mock( DefaultUnifiedRepositoryWebService.class );
    doReturn( RepoWs ).when( fileService ).getRepoWs();
    String[] invalidNames = { ".", ".." };
    for ( String invalidName : invalidNames ) {
      assertFalse(
        fileService.isValidFolderName( encode( invalidName ) ) );
    }
  }

  @Test
  public void testIsValidFileName_DecodedControl_Characters() throws Exception {
    DefaultUnifiedRepositoryWebService RepoWs = mock( DefaultUnifiedRepositoryWebService.class );
    doReturn( RepoWs ).when( fileService ).getRepoWs();

    assertFalse( fileService.isValidFileName( PATH_CONTROL_CHARACTER ) );
  }

  @Test
  public void testIsValidFileName_EncodedControl_Characters() throws Exception {
    DefaultUnifiedRepositoryWebService RepoWs = mock( DefaultUnifiedRepositoryWebService.class );
    doReturn( RepoWs ).when( fileService ).getRepoWs();
    String pathControlCharacter = PATH_CONTROL_CHARACTER;

    assertFalse(
      fileService.isValidFileName( encode( pathControlCharacter ) ) );
  }

  @Test
  public void testIsValidFileName_DecodedJapanese_Characters() throws Exception {
    DefaultUnifiedRepositoryWebService RepoWs = mock( DefaultUnifiedRepositoryWebService.class );
    doReturn( RepoWs ).when( fileService ).getRepoWs();

    assertTrue( fileService.isValidFileName( PATH_JAPANESE_CHARACTERS ) );
  }

  @Test
  public void testIsValidFileName_EncodedJapanese_Characters() throws Exception {
    DefaultUnifiedRepositoryWebService RepoWs = mock( DefaultUnifiedRepositoryWebService.class );
    doReturn( RepoWs ).when( fileService ).getRepoWs();

    assertTrue(
      fileService.isValidFileName( encode( PATH_JAPANESE_CHARACTERS ) ) );
  }

  @Test
  public void testIsValidFileName_DecodedSpecial_Characters() throws Exception {
    DefaultUnifiedRepositoryWebService RepoWs = mock( DefaultUnifiedRepositoryWebService.class );
    doReturn( RepoWs ).when( fileService ).getRepoWs();

    assertTrue( fileService.isValidFileName( PATH_SPECIAL_CHARACTERS ) );
  }

  @Test
  public void testIsValidFileName_EncodedSpecial_Characters() throws Exception {
    DefaultUnifiedRepositoryWebService RepoWs = mock( DefaultUnifiedRepositoryWebService.class );
    doReturn( RepoWs ).when( fileService ).getRepoWs();

    assertTrue(
      fileService.isValidFileName( encode( PATH_SPECIAL_CHARACTERS ) ) );
  }

  @Test
  public void testIsValidFileName_Decoded_InvalidCases() throws Exception {
    DefaultUnifiedRepositoryWebService RepoWs = mock( DefaultUnifiedRepositoryWebService.class );
    doReturn( RepoWs ).when( fileService ).getRepoWs();
    String[] invalidNames =
      { null, "", " ", " x", "x ", " x ", "\tx", "x\t", "\tx\t", "\rx", "x\r", "\rx\r", "\nx", "x\n", "\nx\n" };
    for ( String invalidName : invalidNames ) {
      assertFalse( fileService.isValidFileName( invalidName ) );
    }
  }

  @Test
  public void testIsValidFileName_Encoded_InvalidCases() throws Exception {
    DefaultUnifiedRepositoryWebService RepoWs = mock( DefaultUnifiedRepositoryWebService.class );
    doReturn( RepoWs ).when( fileService ).getRepoWs();
    String[] invalidNames =
      { "", " x", "x ", " x ", "\tx", "x\t", "\tx\t", "\rx", "x\r", "\rx\r", "\nx", "x\n", "\nx\n" };
    for ( String invalidName : invalidNames ) {
      assertFalse(
        fileService.isValidFileName( encode( invalidName ) ) );
    }
  }

  @Test
  public void testIsVisible() throws Exception {
    RepositoryFileDto hiddenDto = new RepositoryFileDto();
    hiddenDto.setFolder( true );
    hiddenDto.setHidden( true );
    hiddenDto.setId( RandomStringUtils.randomNumeric( 20 ) );
    hiddenDto.setName( "suzy" );
    RepositoryFileDto visibleDto = new RepositoryFileDto();
    visibleDto.setFolder( true );
    visibleDto.setHidden( false );
    visibleDto.setId( RandomStringUtils.randomNumeric( 20 ) );
    visibleDto.setName( "joe" );
    DefaultUnifiedRepositoryWebService repoWs = mock( DefaultUnifiedRepositoryWebService.class );

    doReturn( repoWs ).when( fileService ).getRepoWs();
    doReturn( hiddenDto ).when( repoWs ).getFile( "/home/suzy" );
    doReturn( visibleDto ).when( repoWs ).getFile( "/home/joe" );

    assertEquals( "false", fileService.doGetIsVisible( ":home:suzy" ) );
    assertEquals( "true", fileService.doGetIsVisible( ":home:joe" ) );
  }

  @Test
  public void testGetDefaultLocation_Scenario_ProvidedFolderIsNull() throws Exception {
    DefaultUnifiedRepositoryWebService repoWs = mock( DefaultUnifiedRepositoryWebService.class );
    doReturn( repoWs ).when( fileService ).getRepoWs();
    doReturn( null ).when( repoWs ).getFile( "/home/joe" );
    assertEquals( ClientRepositoryPaths.getRootFolderPath(), fileService.doGetDefaultLocation( ":home:joe" ) );
  }


  @Test
  public void testGetDefaultLocation_Scenario_ProvidedFolderIsVisible() throws Exception {
    RepositoryFileDto visibleDto = new RepositoryFileDto();
    visibleDto.setFolder( true );
    visibleDto.setHidden( false );
    visibleDto.setId( RandomStringUtils.randomNumeric( 20 ) );
    visibleDto.setName( "joe" );
    visibleDto.setPath( "/home/joe" );

    RepositoryFileDto homeFolderDto = new RepositoryFileDto();
    homeFolderDto.setFolder( true );
    homeFolderDto.setHidden( false );
    homeFolderDto.setId( RandomStringUtils.randomNumeric( 20 ) );
    homeFolderDto.setName( "home" );
    homeFolderDto.setPath( "/home" );

    DefaultUnifiedRepositoryWebService repoWs = mock( DefaultUnifiedRepositoryWebService.class );
    doReturn( repoWs ).when( fileService ).getRepoWs();
    doReturn( visibleDto ).when( repoWs ).getFile( "/home/joe" );
    doReturn( homeFolderDto ).when( repoWs ).getFile( "/home" );
    assertEquals( "/home/joe", fileService.doGetDefaultLocation( ":home:joe" ) );
  }

  @Test
  public void testGetDefaultLocation_Scenario__DefaultFolderIsNotProvidedPublicFolderIsVisible() throws Exception {
    RepositoryFileDto hiddenDto = new RepositoryFileDto();
    hiddenDto.setFolder( true );
    hiddenDto.setHidden( true );
    hiddenDto.setId( RandomStringUtils.randomNumeric( 20 ) );
    hiddenDto.setName( "suzy" );
    hiddenDto.setPath( "/home/suzy" );
    RepositoryFileDto publicDto = new RepositoryFileDto();
    publicDto.setFolder( true );
    publicDto.setHidden( false );
    publicDto.setId( RandomStringUtils.randomNumeric( 20 ) );
    publicDto.setName( "public" );
    publicDto.setPath( "/public" );
    DefaultUnifiedRepositoryWebService repoWs = mock( DefaultUnifiedRepositoryWebService.class );
    doReturn( repoWs ).when( fileService ).getRepoWs();
    doReturn( hiddenDto ).when( repoWs ).getFile( "/home/suzy" );
    when( PentahoSystem.get( ISystemConfig.class ).getProperty( eq( PentahoSystem.DEFAULT_FOLDER_WHEN_HOME_FOLDER_IS_HIDDEN_PROPERTY ) ) ).thenReturn( null );
    doReturn( publicDto ).when( repoWs ).getFile( ClientRepositoryPaths.getPublicFolderPath() );
    assertEquals( ClientRepositoryPaths.getPublicFolderPath(),  fileService.doGetDefaultLocation( ":home:suzy" ) );
  }

  @Test
  public void testGetDefaultLocation_Scenario_DefaultFolderIsNotProvidedPublicFolderIsHidden() throws Exception {
    RepositoryFileDto hiddenDto = new RepositoryFileDto();
    hiddenDto.setFolder( true );
    hiddenDto.setHidden( true );
    hiddenDto.setId( RandomStringUtils.randomNumeric( 20 ) );
    hiddenDto.setName( "suzy" );
    RepositoryFileDto publicDto = new RepositoryFileDto();
    publicDto.setFolder( true );
    publicDto.setHidden( true );
    publicDto.setId( RandomStringUtils.randomNumeric( 20 ) );
    publicDto.setName( "suzy" );
    DefaultUnifiedRepositoryWebService repoWs = mock( DefaultUnifiedRepositoryWebService.class );
    doReturn( repoWs ).when( fileService ).getRepoWs();
    doReturn( hiddenDto ).when( repoWs ).getFile( "/home/suzy" );
    when( PentahoSystem.get( ISystemConfig.class ).getProperty( eq( PentahoSystem.DEFAULT_FOLDER_WHEN_HOME_FOLDER_IS_HIDDEN_PROPERTY ) ) ).thenReturn( null );
    doReturn( publicDto ).when( repoWs ).getFile( ClientRepositoryPaths.getPublicFolderPath() );
    assertEquals( ClientRepositoryPaths.getRootFolderPath(), fileService.doGetDefaultLocation( ":home:suzy" ) );
  }

  @Test
  public void testGetDefaultLocation_Scenario_DefaultFolderIsVisible() throws Exception {
    RepositoryFileDto hiddenDto = new RepositoryFileDto();
    hiddenDto.setFolder( true );
    hiddenDto.setHidden( true );
    hiddenDto.setId( RandomStringUtils.randomNumeric( 20 ) );
    hiddenDto.setName( "suzy" );
    hiddenDto.setPath( "/home/suzy" );
    RepositoryFileDto defaultDto = new RepositoryFileDto();
    defaultDto.setFolder( true );
    defaultDto.setHidden( false );
    defaultDto.setId( RandomStringUtils.randomNumeric( 20 ) );
    defaultDto.setName( "default" );
    defaultDto.setPath( "/default-folder" );

    DefaultUnifiedRepositoryWebService repoWs = mock( DefaultUnifiedRepositoryWebService.class );
    doReturn( repoWs ).when( fileService ).getRepoWs();
    doReturn( hiddenDto ).when( repoWs ).getFile( "/home/suzy" );
    when( PentahoSystem.get( ISystemConfig.class ).getProperty( eq( PentahoSystem.DEFAULT_FOLDER_WHEN_HOME_FOLDER_IS_HIDDEN_PROPERTY ) ) ).thenReturn( "/default-folder" );
    doReturn( defaultDto ).when( repoWs ).getFile( "/default-folder" );
    assertEquals( "/default-folder",  fileService.doGetDefaultLocation( ":home:suzy" ) );
  }

  @Test
  public void testGetDefaultLocation_Scenario_DefaultFolderIsHidden() throws Exception {
    RepositoryFileDto hiddenDto = new RepositoryFileDto();
    hiddenDto.setFolder( true );
    hiddenDto.setHidden( true );
    hiddenDto.setId( RandomStringUtils.randomNumeric( 20 ) );
    hiddenDto.setName( "suzy" );
    hiddenDto.setPath( "/home/suzy" );

    RepositoryFileDto defaultDto = new RepositoryFileDto();
    defaultDto.setFolder( true );
    defaultDto.setHidden( true );
    defaultDto.setId( RandomStringUtils.randomNumeric( 20 ) );
    defaultDto.setName( "default" );
    defaultDto.setPath( "/default-folder" );

    RepositoryFileDto publicFolderDto = new RepositoryFileDto();
    publicFolderDto.setFolder( true );
    publicFolderDto.setHidden( false );
    publicFolderDto.setId( RandomStringUtils.randomNumeric( 20 ) );
    publicFolderDto.setName( "public" );
    publicFolderDto.setPath( "/public" );

    DefaultUnifiedRepositoryWebService repoWs = mock( DefaultUnifiedRepositoryWebService.class );
    doReturn( repoWs ).when( fileService ).getRepoWs();
    doReturn( hiddenDto ).when( repoWs ).getFile( "/home/suzy" );
    when( PentahoSystem.get( ISystemConfig.class ).getProperty( eq( PentahoSystem.DEFAULT_FOLDER_WHEN_HOME_FOLDER_IS_HIDDEN_PROPERTY ) ) ).thenReturn( "/default-folder" );
    doReturn( defaultDto ).when( repoWs ).getFile( "/default-folder" );
    doReturn( publicFolderDto ).when( repoWs ).getFile( "/public" );
    assertEquals( ClientRepositoryPaths.getPublicFolderPath(), fileService.doGetDefaultLocation( ":home:suzy" ) );
  }

  @Test
  public void testGetDefaultLocation_Scenario_DefaultFolderIsNotAccessible() throws Exception {
    RepositoryFileDto hiddenDto = new RepositoryFileDto();
    hiddenDto.setFolder( true );
    hiddenDto.setHidden( true );
    hiddenDto.setId( RandomStringUtils.randomNumeric( 20 ) );
    hiddenDto.setName( "suzy" );
    hiddenDto.setPath( "/home/suzy" );
    DefaultUnifiedRepositoryWebService repoWs = mock( DefaultUnifiedRepositoryWebService.class );
    doReturn( repoWs ).when( fileService ).getRepoWs();
    doReturn( hiddenDto ).when( repoWs ).getFile( "/home/suzy" );
    when( PentahoSystem.get( ISystemConfig.class ).getProperty( eq( PentahoSystem.DEFAULT_FOLDER_WHEN_HOME_FOLDER_IS_HIDDEN_PROPERTY ) ) ).thenReturn( "/default-folder" );
    doReturn( null ).when( repoWs ).getFile( "/default-folder" );
    assertEquals( ClientRepositoryPaths.getRootFolderPath(), fileService.doGetDefaultLocation( ":home:suzy" ) );
  }

  @Test
  public void testGetDefaultLocationWhenHomeDirectoryIsHiddenAndNoDefaultFolderProvided() {
    DefaultUnifiedRepositoryWebService repository = mock( DefaultUnifiedRepositoryWebService.class );

    RepositoryFileDto userHomeFolderDto = new RepositoryFileDto();
    userHomeFolderDto.setFolder( true );
    userHomeFolderDto.setHidden( false );
    userHomeFolderDto.setId( RandomStringUtils.randomNumeric( 20 ) );
    userHomeFolderDto.setName( "suzy" );
    userHomeFolderDto.setPath( "/home/suzy" );


    RepositoryFileDto homeFolderDto = new RepositoryFileDto();
    homeFolderDto.setFolder( true );
    homeFolderDto.setHidden( true );
    homeFolderDto.setId( RandomStringUtils.randomNumeric( 20 ) );
    homeFolderDto.setName( "home" );
    homeFolderDto.setPath( "/home" );

    RepositoryFileDto publicFolderDto = new RepositoryFileDto();
    publicFolderDto.setFolder( true );
    publicFolderDto.setHidden( false );
    publicFolderDto.setId( RandomStringUtils.randomNumeric( 20 ) );
    publicFolderDto.setName( "public" );
    publicFolderDto.setPath( "/public" );

    when( PentahoSystem.get( ISystemConfig.class ).getProperty( eq( PentahoSystem.DEFAULT_FOLDER_WHEN_HOME_FOLDER_IS_HIDDEN_PROPERTY ) ) ).thenReturn( null );

    doReturn( repository ).when( fileService ).getRepoWs();
    doReturn( userHomeFolderDto ).when( repository ).getFile( "/home/suzy" );
    doReturn( homeFolderDto ).when( repository ).getFile( "/home" );
    doReturn( publicFolderDto ).when( repository ).getFile( "/public" );

    assertEquals( ClientRepositoryPaths.getPublicFolderPath(), fileService.doGetDefaultLocation( ":home:suzy" ) );
  }

  @Test
  public void testDoCanEdit() throws Exception {
    ISystemSettings settingsService = mock( ISystemSettings.class );
    when( settingsService.getSystemSetting(  "edit-permission", "" ) ).thenReturn( RepositoryCreateAction.NAME );
    PentahoSystem.setSystemSettingsService( settingsService );

    // Test for user having a proper permission to edit
    IAuthorizationPolicy policy = mock( IAuthorizationPolicy.class );
    when( policy.isAllowed( RepositoryCreateAction.NAME ) ).thenReturn( true );
    PentahoSystem.registerObject( policy );
    assertEquals(fileService.doGetCanEdit(), "true" );

    // Test for user not having a proper permission to edit
    when( settingsService.getSystemSetting(  "edit-permission", "" ) ).thenReturn( RepositoryCreateAction.NAME );
    when( policy.isAllowed( any() ) ).thenReturn( false );
    assertEquals(fileService.doGetCanEdit(), "false" );

    // Test for configuration in the pentaho.xml is an empty string
    when( settingsService.getSystemSetting(  "edit-permission", "" ) ).thenReturn( "" );
    assertEquals(fileService.doGetCanEdit(), "true" );

    // Test for configuration in the pentaho.xml does not exist
    when( settingsService.getSystemSetting(  "edit-permission", "" ) ).thenReturn( null );
    assertEquals(fileService.doGetCanEdit(), "true" );
  }

  private static String encode( String pathControlCharacter ) throws UnsupportedEncodingException {
    return URLEncoder.encode( pathControlCharacter, UTF_8 );
  }
}
