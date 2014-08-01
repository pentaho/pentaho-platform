package org.pentaho.platform.web.http.api.resources.services;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.InvalidPathException;
import java.security.GeneralSecurityException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.StreamingOutput;

import com.mockrunner.util.FileUtil;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.plugin.services.importexport.BaseExportProcessor;
import org.pentaho.platform.plugin.services.importexport.ExportHandler;
import org.pentaho.platform.repository.RepositoryDownloadWhitelist;
import org.pentaho.platform.repository2.locale.PentahoLocale;
import org.pentaho.platform.repository2.unified.fileio.RepositoryFileInputStream;
import org.pentaho.platform.repository2.unified.fileio.RepositoryFileOutputStream;
import org.pentaho.platform.repository2.unified.jcr.PentahoJcrConstants;
import org.pentaho.platform.repository2.unified.webservices.DefaultUnifiedRepositoryWebService;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileAclDto;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileDto;
import org.pentaho.platform.repository2.unified.webservices.StringKeyStringValueDto;
import org.pentaho.platform.web.http.api.resources.Setting;
import org.pentaho.platform.web.http.api.resources.StringListWrapper;
import org.pentaho.platform.web.http.api.resources.utils.FileUtils;

public class FileServiceTest {

  private static FileService fileService;

  @Before
  public void setUp() {
    fileService = spy( new FileService() );
    fileService.defaultUnifiedRepositoryWebService = mock( DefaultUnifiedRepositoryWebService.class );
    fileService.repository = mock( IUnifiedRepository.class );
    fileService.policy = mock( IAuthorizationPolicy.class );
  }

  @After
  public void cleanup() {
    fileService = null;
  }

  @Test
  public void testDoDeleteFiles() throws Exception {
    String params = "file1,file2";

    fileService.doDeleteFiles( params );

    verify( fileService.getRepoWs(), times( 1 ) ).deleteFile( "file1", null );
    verify( fileService.getRepoWs(), times( 1 ) ).deleteFile( "file2", null );
  }

  @Test
  public void testDoDeleteFilesException() {
    String params = "file1,file2";
    doThrow( new IllegalArgumentException() ).when(
      fileService.defaultUnifiedRepositoryWebService ).deleteFile( anyString(), anyString() );

    try {
      fileService.doDeleteFiles( params );
      fail(); //This line should never be reached
    } catch ( IllegalArgumentException e ) {
      //Expected exception
    } catch ( Exception e ) {
      fail();
    }
  }

  @Test
  public void testDoCopyFiles() throws Exception {

    String destinationPath = "/path/to/destination";
    String destinationPathColon = ":path:to:destination";

    String filePath1 = "/path/to/source/file1.ext";
    String fileId1 = "file1";

    String filePath2 = "/path/to/source/file2.ext";
    String fileId2 = "file2";

    when( fileService.policy.isAllowed( anyString() ) ).thenReturn( true );

    RepositoryFile repositoryFile = mock( RepositoryFile.class );
    when( fileService.repository.createFile( any( Serializable.class ), any( RepositoryFile.class ), any(
      IRepositoryFileData.class ), any( RepositoryFileAcl.class ), anyString() ) ).thenReturn( repositoryFile );

    RepositoryFile destDir = mock( RepositoryFile.class );
    when( destDir.isFolder() ).thenReturn( true );
    when( destDir.getPath() ).thenReturn( destinationPath );
    when( fileService.repository.getFile( destinationPath ) ).thenReturn( destDir );

    RepositoryFile repositoryFile1 = mock( RepositoryFile.class );
    when( repositoryFile1.isFolder() ).thenReturn( false );
    when( repositoryFile1.getPath() ).thenReturn( filePath1 );
    when( fileService.repository.getFileById( fileId1 ) ).thenReturn( repositoryFile1 );

    RepositoryFile repositoryFile2 = mock( RepositoryFile.class );
    when( repositoryFile2.isFolder() ).thenReturn( false );
    when( repositoryFile2.getPath() ).thenReturn( filePath2 );
    when( fileService.repository.getFileById( fileId2 ) ).thenReturn( repositoryFile2 );

    fileService.doCopyFiles( destinationPathColon, 1, fileId1 + "," + fileId2 );

    verify( fileService.repository, times( 2 ) )
      .createFile( any( Serializable.class ), any( RepositoryFile.class ), any(
        IRepositoryFileData.class ), any( RepositoryFileAcl.class ), anyString() );
    verify( fileService.repository ).getFile( destinationPath );
    verify( fileService.repository ).getFileById( fileId1 );
    verify( fileService.repository ).getFileById( fileId2 );
  }

  @Test
  public void doCopyFilesException() throws Exception {

    String destinationPath = "/path/to/destination";
    String destinationPathColon = ":path:to:destination";

    String filePath1 = "/path/to/source/file1.ext";
    String fileId1 = "file1";

    String filePath2 = "/path/to/source/file2.ext";
    String fileId2 = "file2";

    when( fileService.policy.isAllowed( anyString() ) ).thenReturn( false );

    try {
      fileService.doCopyFiles( destinationPathColon, 1, fileId1 + "," + fileId2 );
      fail();
    } catch ( IllegalArgumentException e ) {
      //Should catch the exception
    }
  }

  @Test
  public void testDoGetFileOrDir() throws Exception {
    RepositoryFile file = mock( RepositoryFile.class );
    doReturn( "file.txt" ).when( file ).getName();

    when( fileService.repository.getFile( anyString() ) ).thenReturn( file );

    RepositoryFileInputStream mockInputStream = mock( RepositoryFileInputStream.class );

    doReturn( 1 ).when( fileService ).copy( any( java.io.InputStream.class ), any( java.io.OutputStream.class ) );
    doReturn( mockInputStream ).when( fileService ).getRepositoryFileInputStream( any( RepositoryFile.class ) );

    String pathId = "/usr/folder/file.txt";
    FileService.RepositoryFileToStreamWrapper wrapper = fileService.doGetFileOrDir( pathId );

    assertEquals( "file.txt", wrapper.getRepositoryFile().getName() );
  }

  @Test
  public void testDoGetFileOrDirException() throws Exception {
    RepositoryFile file = mock( RepositoryFile.class );
    doReturn( "file.txt" ).when( file ).getName();

    RepositoryFileInputStream mockInputStream = mock( RepositoryFileInputStream.class );

    doReturn( 1 ).when( fileService ).copy( any( java.io.InputStream.class ), any( java.io.OutputStream.class ) );
    doReturn( mockInputStream ).when( fileService ).getRepositoryFileInputStream( any( RepositoryFile.class ) );

    String pathId = "/usr/folder/file.txt";
    try {
      fileService.doGetFileOrDir( pathId );
      fail(); //This line should never be reached
    } catch ( FileNotFoundException fileNotFound ) {
      //Expected exception
    }
  }

  @Test
  public void testSetFileAcls() throws Exception {
    RepositoryFileDto file = mock( RepositoryFileDto.class );
    doReturn( "file.txt" ).when( file ).getName();
    when( fileService.defaultUnifiedRepositoryWebService.getFile( anyString() ) ).thenReturn( file );

    String pathId = "/usr/folder/file.txt";
    RepositoryFileAclDto acl = mock( RepositoryFileAclDto.class );
    fileService.setFileAcls( pathId, acl );

    verify( acl, times( 1 ) ).setId( anyString() );
    verify( file, times( 1 ) ).getId();
    verify( fileService.defaultUnifiedRepositoryWebService, times( 1 ) ).updateAcl( acl );
  }

  @Test
  public void testSetFileAclsException() throws Exception {
    String pathId = "/usr/folder/file.txt";
    RepositoryFileAclDto acl = mock( RepositoryFileAclDto.class );
    try {
      fileService.setFileAcls( pathId, acl );
      fail();
    } catch ( FileNotFoundException e ) {
      //expected exception
    }
  }
  @Test
  public void testDoGetRootProperties() {
    fileService.doGetRootProperties();

    verify( fileService.defaultUnifiedRepositoryWebService, times( 1 ) ).
      getFile( FileUtils.PATH_SEPARATOR );
  }

  @Test
  public void testDoGetProperties() throws Exception {
    RepositoryFileDto file = mock( RepositoryFileDto.class );
    when( fileService.defaultUnifiedRepositoryWebService.getFile( anyString() ) ).thenReturn( file );

    String pathId = "/usr/folder/file.txt";
    fileService.doGetProperties( pathId );

    verify( fileService.defaultUnifiedRepositoryWebService, times( 1 ) ).getFile( anyString() );
  }

  @Test
  public void testDoGetPropertiesException() throws Exception {
    String pathId = "/usr/folder/file.txt";
    try {
      fileService.doGetProperties( pathId );
      fail();
    } catch ( FileNotFoundException fileNotFound ) {
      //expected exception
    }
  }

  @Ignore
  public void testDoCreateFile() throws Exception {
    RepositoryFileOutputStream mockOutputStream = mock( RepositoryFileOutputStream.class );
    doReturn( mockOutputStream ).when( fileService ).getRepositoryFileOutputStream( anyString() );

    HttpServletRequest mockRequest = mock( HttpServletRequest.class );
    InputStream mockInputStream = mock( InputStream.class );

    doReturn( 1 ).when( fileService ).copy( mockInputStream, mockOutputStream );

    fileService.createFile( mockRequest, "testString", mockInputStream );

    verify( mockOutputStream, times( 1 ) ).setCharsetName( anyString() );
    verify( mockOutputStream, times( 1 ) ).close();
    verify( mockInputStream, times( 1 ) ).close();
  }

  @Ignore
  public void testDoCreateFileException() {
    RepositoryFileOutputStream mockOutputStream = mock( RepositoryFileOutputStream.class );
    doThrow( new IllegalArgumentException() ).when( fileService ).idToPath( anyString() );

    try {
      fileService.createFile( null, null, null );
      fail();
    } catch ( IllegalArgumentException e ) {
      // expected
    } catch ( Exception e ) {
      fail();
    }
  }

  @Test
  public void testDoDeleteFilesPermanent() throws Exception {
    String params = "file1,file2";

    fileService.doDeleteFilesPermanent( params );

    verify( fileService.getRepoWs(), times( 1 ) ).deleteFileWithPermanentFlag( "file1", true, null );
    verify( fileService.getRepoWs(), times( 1 ) ).deleteFileWithPermanentFlag( "file2", true, null );
  }

  @Test
  public void testDoDeleteLocale() throws Exception {
    RepositoryFileDto file = mock( RepositoryFileDto.class );
    doReturn( file ).when( fileService.defaultUnifiedRepositoryWebService ).getFile( anyString() );
    doReturn( "file.txt" ).when( file ).getId();
    fileService.doDeleteLocale( file.getId(), "en_US" );
    verify( fileService.getRepoWs(), times( 1 ) ).deleteLocalePropertiesForFile( "file.txt", "en_US" );
  }
  
  @Test
  public void testDoGetCanAccessList() {
    String permissions = RepositoryFilePermission.READ.ordinal() + "|" + RepositoryFilePermission.WRITE.ordinal() + "|" + RepositoryFilePermission.DELETE.ordinal();
    doReturn( true ).when( fileService.repository ).hasAccess(  anyString() , any( EnumSet.class) );
    List<Setting> settings = fileService.doGetCanAccessList( "pathId", permissions );
    assertTrue( settings.size() > 0 );
  }  
  
  @Test
  public void testDoGetPathsAccessList() {
    List<String> paths = new ArrayList<String>();
    paths.add( "path1" );
    paths.add( "path2" );
    paths.add( "path3" );
    
    doReturn( true ).when( fileService.repository ).hasAccess(  anyString() , any( EnumSet.class) );
    List<Setting> settings = fileService.doGetPathsAccessList( new StringListWrapper(paths) );
    assertTrue( settings.size() > 0 );
    
    doReturn( false ).when( fileService.repository ).hasAccess(  anyString() , any( EnumSet.class) );
    settings = fileService.doGetPathsAccessList( new StringListWrapper(paths) );
    assertEquals(0, settings.size() );
  }  

  @Test
  public void testDoDeleteFilesPermanentException() {
    String params = "file1,file2";
    doThrow( new IllegalArgumentException() ).when(
      fileService.defaultUnifiedRepositoryWebService ).deleteFileWithPermanentFlag( anyString(), eq( true ),
      anyString() );

    try {
      fileService.doDeleteFilesPermanent( params );
      fail(); //This line should never be reached
    } catch ( Exception e ) {
      //Expected exception
    }
  }

  @Test
  public void testDoMoveFiles() throws Exception {
    String destPathId = "/test";
    String[] params = { "file1", "file2" };

    RepositoryFileDto repositoryFileDto = mock( RepositoryFileDto.class );
    doReturn( destPathId ).when( repositoryFileDto ).getPath();

    doReturn( repositoryFileDto ).when( fileService.defaultUnifiedRepositoryWebService ).getFile( destPathId );

    fileService.doMoveFiles( destPathId, StringUtils.join( params, "," ) );

    verify( fileService.getRepoWs(), times( 1 ) ).moveFile( params[ 0 ], destPathId, null );
    verify( fileService.getRepoWs(), times( 1 ) ).moveFile( params[ 1 ], destPathId, null );
  }

  @Test
  public void testDoMoveFilesForUnknownDestPath() throws Exception {
    String destPathId = "/test";
    String[] params = { "file1", "file2" };

    doReturn( null ).when( fileService.defaultUnifiedRepositoryWebService ).getFile( destPathId );

    try {
      fileService.doMoveFiles( destPathId, StringUtils.join( params, "," ) );
      Assert.assertTrue( false );
    } catch ( FileNotFoundException e ) {
      verify( fileService.getRepoWs(), times( 0 ) ).moveFile( params[ 0 ], destPathId, null );
      verify( fileService.getRepoWs(), times( 0 ) ).moveFile( params[ 1 ], destPathId, null );
    }
  }

  @Test
  public void testDoMoveFilesException() throws Exception {
    String destPathId = "/test";
    String[] params = { "file1", "file2" };

    RepositoryFileDto repositoryFileDto = mock( RepositoryFileDto.class );
    doReturn( destPathId ).when( repositoryFileDto ).getPath();

    doReturn( repositoryFileDto ).when( fileService.defaultUnifiedRepositoryWebService ).getFile( destPathId );
    doThrow( new InternalError() ).when( fileService.defaultUnifiedRepositoryWebService ).moveFile(
      params[ 0 ], destPathId, null );

    try {
      fileService.doMoveFiles( destPathId, StringUtils.join( params, "," ) );
      fail(); //This line should never be reached
    } catch ( Throwable e ) {
      verify( fileService.getRepoWs(), times( 1 ) ).moveFile( params[ 0 ], destPathId, null );
      verify( fileService.getRepoWs(), times( 0 ) ).moveFile( params[ 1 ], destPathId, null );
    }
  }

  @Test
  public void testDoRestoreFiles() throws Exception {
    String[] params = { "file1", "file2" };

    fileService.doRestoreFiles( StringUtils.join( params, "," ) );

    verify( fileService.getRepoWs(), times( 1 ) ).undeleteFile( params[ 0 ], null );
    verify( fileService.getRepoWs(), times( 1 ) ).undeleteFile( params[ 1 ], null );
  }

  @Test
  public void testDoRestoreFilesException() throws Exception {
    String[] params = { "file1", "file2" };

    doThrow( new InternalError() ).when( fileService.defaultUnifiedRepositoryWebService ).undeleteFile(
      params[ 0 ], null );

    try {
      fileService.doRestoreFiles( StringUtils.join( params, "," ) );
      fail(); //This line should never be reached
    } catch ( InternalError e ) {
      verify( fileService.getRepoWs(), times( 1 ) ).undeleteFile( params[ 0 ], null );
      verify( fileService.getRepoWs(), times( 0 ) ).undeleteFile( params[ 1 ], null );
    }
  }

  @Test
  public void testDoGetFileAsInline() throws FileNotFoundException {
    /*
     * TEST 1
     */
    doReturn( true ).when( fileService ).isPath( anyString() );
    doReturn( true ).when( fileService ).isPathValid( anyString() );

    RepositoryDownloadWhitelist mockWhiteList = mock( RepositoryDownloadWhitelist.class );
    doReturn( mockWhiteList ).when( fileService ).getWhitelist();
    doReturn( true ).when( mockWhiteList ).accept( anyString() );

    RepositoryFile mockRepoFile = mock( RepositoryFile.class );
    doReturn( mockRepoFile ).when( fileService.repository ).getFile( anyString() );

    SimpleRepositoryFileData mockData = mock( SimpleRepositoryFileData.class );
    doReturn( mockData ).when( fileService.repository ).getDataForRead( any( Serializable.class ), any( Class.class ) );

    InputStream mockInputStream = mock( InputStream.class );
    doReturn( mockInputStream ).when( mockData ).getInputStream();

    StreamingOutput mockStreamingOutput = mock( StreamingOutput.class );
    doReturn( mockStreamingOutput ).when( fileService ).getStreamingOutput( mockInputStream );

    FileService.RepositoryFileToStreamWrapper wrapper = fileService.doGetFileAsInline( "test" );

    verify( fileService.repository, times( 1 ) ).getFile( anyString() );
    verify( mockWhiteList, times( 1 ) ).accept( anyString() );
    verify( fileService, times( 2 ) ).getRepository();
    verify( fileService.repository, times( 1 ) ).getDataForRead( any( Serializable.class ), any( Class.class ) );
    verify( mockData, times( 1 ) ).getInputStream();

    assertEquals( mockRepoFile, wrapper.getRepositoryFile() );
    assertEquals( mockStreamingOutput, wrapper.getOutputStream() );

    /*
     * TEST 2
     */
    doReturn( false ).when( fileService ).isPath( anyString() );
    doReturn( mockRepoFile ).when( fileService.repository ).getFileById( anyString() );

    wrapper = fileService.doGetFileAsInline( "test" );

    verify( fileService.repository, times( 1 ) ).getFileById( anyString() );
    verify( fileService, times( 4 ) ).getRepository();

    assertEquals( mockRepoFile, wrapper.getRepositoryFile() );
    assertEquals( mockStreamingOutput, wrapper.getOutputStream() );
  }

  @Test
  public void testDoGetFileAsInlineException() {

    /*
     * TEST 1
     */
    doReturn( true ).when( fileService ).isPath( anyString() );
    doReturn( false ).when( fileService ).isPathValid( anyString() );

    try {
      fileService.doGetFileAsInline( "test" );
      fail();
    } catch ( IllegalArgumentException e ) {
      // Excpected
    } catch ( FileNotFoundException e ) {
      fail();
    }

    /*
     * TEST 2
     */
    doReturn( true ).when( fileService ).isPathValid( anyString() );
    doReturn( null ).when( fileService.repository ).getFile( anyString() );

    try {
      fileService.doGetFileAsInline( "test" );
      fail();
    } catch ( FileNotFoundException e ) {
      // Expected
    }

    /*
     * TEST 3
     */
    RepositoryFile mockFile = mock( RepositoryFile.class );
    doReturn( mockFile ).when( fileService.repository ).getFile( anyString() );

    RepositoryDownloadWhitelist mockWhiteList = mock( RepositoryDownloadWhitelist.class );
    doReturn( mockWhiteList ).when( fileService ).getWhitelist();
    doReturn( false ).when( mockWhiteList ).accept( anyString() );

    IAuthorizationPolicy mockPolicy = mock( IAuthorizationPolicy.class );
    doReturn( mockPolicy ).when( fileService ).getPolicy();
    doReturn( false ).when( mockPolicy ).isAllowed( anyString() );

    try {
      fileService.doGetFileAsInline( "test" );
      fail();
    } catch ( IllegalArgumentException e ) {
      // Excpected
    } catch ( FileNotFoundException e ) {
      fail();
    }

    /*
     * TEST 4
     */
    doReturn( true ).when( mockWhiteList ).accept( anyString() );
    doThrow( new InternalError() ).when( fileService.repository )
      .getDataForRead( any( Serializable.class ), any( Class.class ) );

    try {
      fileService.doGetFileAsInline( "test" );
      fail();
    } catch ( InternalError e ) {
      // Excpected
    } catch ( FileNotFoundException e ) {
      fail();
    }
  }

  @Test
  public void testDoGetLocalProperties() throws Exception {
    String pathId = "path:to:file:file1.ext";
    String fileId = "file1";
    String locale = "";

    doReturn( "/path/to/file/file1.ext" ).when( fileService ).idToPath( pathId );

    Set<String> propertiesList = new HashSet<String>();
    propertiesList.add( "prop1" );
    propertiesList.add( "prop2" );

    RepositoryFileDto repositoryFileDto = mock( RepositoryFileDto.class );
    doReturn( fileId ).when( repositoryFileDto ).getId();

    doReturn( repositoryFileDto ).when( fileService.defaultUnifiedRepositoryWebService ).getFile( anyString() );

    Properties properties = mock( Properties.class );
    doReturn( "value1" ).when( properties ).getProperty( "prop1" );
    doReturn( "value2" ).when( properties ).getProperty( "prop2" );
    doReturn( false ).when( properties ).isEmpty();
    doReturn( propertiesList ).when( properties ).stringPropertyNames();

    doReturn( properties ).when( fileService.defaultUnifiedRepositoryWebService )
      .getLocalePropertiesForFileById( anyString(), anyString() );

    List<StringKeyStringValueDto> keyValueList = fileService.doGetLocaleProperties( pathId, locale );

    verify( fileService.defaultUnifiedRepositoryWebService ).getFile( "/path/to/file/file1.ext" );
    verify( properties ).getProperty( "prop1" );
    verify( properties ).getProperty( "prop2" );
    verify( properties ).isEmpty();
    verify( properties ).stringPropertyNames();
    verify( fileService.defaultUnifiedRepositoryWebService ).getLocalePropertiesForFileById( anyString(), anyString() );

    assertEquals( 2, keyValueList.size() );
    assertEquals( "prop1", keyValueList.get( 1 ).getKey() );
    assertEquals( "prop2", keyValueList.get( 0 ).getKey() );
    assertEquals( "value1", keyValueList.get( 1 ).getValue() );
    assertEquals( "value2", keyValueList.get( 0 ).getValue() );
  }

  @Test
  public void testDoSetLocalProperties() throws Exception {
    String pathId = "path:to:file:file1.ext";
    String fileId = "file1";
    String locale = "";

    doReturn( "/path/to/file/file1.ext" ).when( fileService ).idToPath( pathId );

    RepositoryFileDto repositoryFileDto = mock( RepositoryFileDto.class );
    doReturn( fileId ).when( repositoryFileDto ).getId();

    doReturn( repositoryFileDto ).when( fileService.defaultUnifiedRepositoryWebService ).getFile( anyString() );

    Properties fileProperties = mock( Properties.class );
    doReturn( false ).when( fileProperties ).isEmpty();

    List<StringKeyStringValueDto> properties = new ArrayList<StringKeyStringValueDto>();
    properties.add( new StringKeyStringValueDto( "key1", "value1" ) );
    properties.add( new StringKeyStringValueDto( "key2", "value2" ) );

    fileService.doSetLocaleProperties( pathId, locale, properties );

    verify( fileService.defaultUnifiedRepositoryWebService ).getFile( "/path/to/file/file1.ext" );
    verify( fileService.defaultUnifiedRepositoryWebService )
      .setLocalePropertiesForFileByFileId( anyString(), anyString(), any( Properties.class ) );
  }

  @Test
  public void testDoGetCanAccess() throws Exception {
    String pathId = "path:to:file:file1.ext";
    String permissions = "0|1|2|3|4";

    List<Integer> permissionsList = new ArrayList<Integer>();
    permissionsList.add( 0 );
    permissionsList.add( 1 );
    permissionsList.add( 2 );
    permissionsList.add( 3 );
    permissionsList.add( 4 );

    doReturn( "/path/to/file/file1.ext" ).when( fileService ).idToPath( pathId );
    doReturn( true ).when( fileService.defaultUnifiedRepositoryWebService ).hasAccess( anyString(), anyList() );

    String hasAccess = fileService.doGetCanAccess( pathId, permissions );

    verify( fileService.defaultUnifiedRepositoryWebService ).hasAccess( "/path/to/file/file1.ext", permissionsList );

    assertEquals( "true", hasAccess );
  }

  public void testDoSetContentCreator() throws Exception {
    String param = "file1";
    RepositoryFileDto repositoryFileDto = mock( RepositoryFileDto.class );
    Map<String, Serializable> fileMetadata = mock( Map.class );

    doReturn( param ).when( repositoryFileDto ).getId();
    when( fileService.idToPath( param ) ).thenReturn( "/file1" );
    doReturn( repositoryFileDto ).when( fileService.defaultUnifiedRepositoryWebService )
      .getFile( fileService.idToPath( param ) );
    when( fileService.getRepository().getFileMetadata( repositoryFileDto.getId() ) ).thenReturn( fileMetadata );

    try {
      fileService.doSetContentCreator( param, repositoryFileDto );
      verify( fileService.getRepository(), times( 1 ) ).getFileMetadata( repositoryFileDto.getId() );
      verify( fileService.getRepository(), times( 1 ) ).setFileMetadata( param, fileMetadata );
    } catch ( FileNotFoundException e ) {
      fail();
    } catch ( InternalError e ) {
      fail();
    }
  }

  @Test
  public void testDoSetContentCreatorFileNotFoundException() throws Exception {
    String param = "file1";
    RepositoryFileDto mockFileDto = mock( RepositoryFileDto.class );
    Map<String, Serializable> fileMetadata = mock( Map.class );

    when( fileService.idToPath( param ) ).thenReturn( "/file1" );
    doReturn( null ).when( fileService.defaultUnifiedRepositoryWebService ).getFile( "/file1" );

    try {
      fileService.doSetContentCreator( param, mockFileDto );
      fail();
    } catch ( FileNotFoundException e ) {
    }
  }

  @Test
  public void testDoSetContentCreatorFileException() throws Exception {
    String param = "file1";
    RepositoryFileDto repositoryFileDto = mock( RepositoryFileDto.class );
    Map<String, Serializable> fileMetadata = mock( Map.class );

    doReturn( param ).when( repositoryFileDto ).getId();
    when( fileService.idToPath( param ) ).thenReturn( "/file1" );
    doReturn( repositoryFileDto ).when( fileService.defaultUnifiedRepositoryWebService ).getFile( "/file1" );
    when( fileService.getRepository().getFileMetadata( repositoryFileDto.getId() ) ).thenThrow( new InternalError() );

    //failing in get
    try {
      fileService.doSetContentCreator( param, repositoryFileDto );
    } catch ( FileNotFoundException e ) {
      fail();
    } catch ( InternalError e ) {
      verify( fileMetadata, times( 0 ) ).put( PentahoJcrConstants.PHO_CONTENTCREATOR, param );
      verify( fileService.repository, times( 0 ) ).setFileMetadata( param, fileMetadata );
    }
  }

  @Test
  public void testDoGetFileLocales() {
    String param = "file1";
    RepositoryFileDto repositoryFileDto = mock( RepositoryFileDto.class );
    List<PentahoLocale> locales = new ArrayList<PentahoLocale>();
    PentahoLocale mockedLocale = mock( PentahoLocale.class );
    locales.add( mockedLocale );

    doReturn( param ).when( repositoryFileDto ).getId();
    doReturn( repositoryFileDto ).when( fileService.defaultUnifiedRepositoryWebService ).getFile( "/" + param );
    when( fileService.defaultUnifiedRepositoryWebService.getAvailableLocalesForFileById( repositoryFileDto.getId() ) )
      .thenReturn( locales );

    try {
      fileService.doGetFileLocales( param );
      verify( fileService.getRepository(), times( 0 ) ).getAvailableLocalesForFileById( repositoryFileDto.getId() );
    } catch ( FileNotFoundException e ) {
      fail();
    } catch ( InternalError e ) {
      fail();
    }
  }

  @Test
  public void testDoGetFileLocalesFileNotFoundException() {
    String param = "file1";
    RepositoryFileDto repositoryFileDto = mock( RepositoryFileDto.class );

    doReturn( param ).when( repositoryFileDto ).getId();
    doReturn( null ).when( fileService.defaultUnifiedRepositoryWebService ).getFile( "/" + param );

    try {
      fileService.doGetFileLocales( param );
      fail();
    } catch ( FileNotFoundException e ) {
      verify( fileService.getRepository(), times( 0 ) ).getAvailableLocalesForFileById( repositoryFileDto.getId() );
    }
  }

  @Test
  public void testDoGetFileLocalesException() {
    String param = "file1";
    RepositoryFileDto repositoryFileDto = mock( RepositoryFileDto.class );

    doReturn( param ).when( repositoryFileDto ).getId();
    doReturn( repositoryFileDto ).when( fileService.defaultUnifiedRepositoryWebService ).getFile( "/" + param );
    when( fileService.defaultUnifiedRepositoryWebService.getAvailableLocalesForFileById( repositoryFileDto.getId() ) )
      .thenThrow( new InternalError() );

    try {
      fileService.doGetFileLocales( param );
    } catch ( FileNotFoundException e ) {
      fail();
    } catch ( InternalError e ) {
      verify( fileService.getRepository(), times( 0 ) ).getAvailableLocalesForFileById( repositoryFileDto.getId() );
    }
  }

  @Test
  public void testDoGetReservedChars() throws Exception {

    StringBuffer stringBuffer = new StringBuffer();
    stringBuffer.append( '/' );
    stringBuffer.append( '\\' );
    stringBuffer.append( '\t' );
    stringBuffer.append( '\r' );
    stringBuffer.append( '\n' );

    List<Character> characters = new ArrayList<Character>();
    characters.add( '/' );
    characters.add( '\\' );
    characters.add( '\t' );
    characters.add( '\r' );
    characters.add( '\n' );

    doReturn( characters ).when( fileService.defaultUnifiedRepositoryWebService ).getReservedChars();

    StringBuffer buffer = fileService.doGetReservedChars();

    verify( fileService.defaultUnifiedRepositoryWebService ).getReservedChars();

    assertEquals( stringBuffer.toString(), buffer.toString() );
  }
  
  
  @Test
  public void testDoGetFileOrDirAsDownload() throws Throwable {
    final String fileName = "mockFileName";
    
    IAuthorizationPolicy mockAuthPolicy = mock( IAuthorizationPolicy.class );
    doReturn(true).when( mockAuthPolicy ).isAllowed( anyString() );
    
    BaseExportProcessor mockExportProcessor = mock( BaseExportProcessor.class );
    File mockExportFile = mock( File.class );
    ExportHandler mockExportHandler = mock( ExportHandler.class );
    StreamingOutput mockStream = mock( StreamingOutput.class);
    
    RepositoryFile mockRepoFile = mock( RepositoryFile.class );
    doReturn( fileName ).when( mockRepoFile ).getName();
    doReturn( mockExportFile ).when( mockExportProcessor ).performExport( mockRepoFile );

    doReturn( mockRepoFile ).when( fileService.repository ).getFile( anyString() );
    doReturn( mockAuthPolicy ).when( fileService ).getPolicy();
    doReturn( mockExportProcessor ).when( fileService ).getDownloadExportProcessor( anyString(), anyBoolean(), anyBoolean() );
    doReturn( mockExportHandler ).when( fileService ).getDownloadExportHandler();
    doReturn( mockStream ).when( fileService ).getDownloadStream( mockRepoFile, mockExportProcessor );
    
    FileService.DownloadFileWrapper wrapper = fileService.doGetFileOrDirAsDownload( "", "mock:path:" + fileName, "true" );
    
    verify( fileService.repository, times( 1 ) ).getFile( anyString() );
    
    
    assertEquals( mockStream, wrapper.getOutputStream() );
    assertEquals( fileName+".zip", wrapper.getEncodedFileName() );
    assertEquals( true, wrapper.getAttachment().equals( "attachment; filename=\"mockFileName.zip\"" ));
  }

  @Test
  public void testDoGetFileOrDirAsDownloadException() {
    /*
     * Test 1
     */
    IAuthorizationPolicy mockAuthPolicy = mock( IAuthorizationPolicy.class );
    doReturn( false ).when( mockAuthPolicy ).isAllowed( anyString() );
    doReturn( mockAuthPolicy ).when( fileService ).getPolicy();
    
    try {
      FileService.DownloadFileWrapper wrapper = fileService.doGetFileOrDirAsDownload( "", "mock:path:fileName", "true" );
      fail();
    } catch (GeneralSecurityException e) {
      // Expected
    } catch (Throwable t) {
      fail();
    }
    
    /*
     * Test 2
     */
    doReturn( true ).when( mockAuthPolicy ).isAllowed( anyString() );
    try {
      FileService.DownloadFileWrapper wrapper = fileService.doGetFileOrDirAsDownload( "", "", "true" );
      fail();
    } catch (InvalidParameterException e) {
      // Expected
    } catch ( Throwable e ) {
      fail();
    }
    
    /*
     * Test 3
     */
    doReturn( false ).when( fileService ).isPathValid( anyString() );
    try {
      fileService.doGetFileOrDirAsDownload( "", "mock:path:fileName", "true" );
      fail();
    } catch (InvalidPathException e) {
      // Expected
    } catch (Throwable t) {
      fail();
    }

    /*
     * Test 4
     */
    doReturn( true ).when( fileService ).isPathValid( anyString() );
    doReturn( null ).when( fileService.repository ).getFile( anyString() );
    try {
      fileService.doGetFileOrDirAsDownload( "", "mock:path:fileName", "true" );
      fail();
    } catch (FileNotFoundException e) {
      // Expected
    } catch (Throwable t) {
      fail();
    }
  }
}
