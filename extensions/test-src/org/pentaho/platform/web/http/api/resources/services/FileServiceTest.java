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
 * Copyright (c) 2002-2016 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.web.http.api.resources.services;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.channels.IllegalSelectorException;
import java.security.GeneralSecurityException;
import java.security.InvalidParameterException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.RepositoryRequest;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.plugin.services.importexport.BaseExportProcessor;
import org.pentaho.platform.plugin.services.importexport.ExportHandler;
import org.pentaho.platform.repository.RepositoryDownloadWhitelist;
import org.pentaho.platform.repository2.locale.PentahoLocale;
import org.pentaho.platform.repository2.unified.fileio.RepositoryFileInputStream;
import org.pentaho.platform.repository2.unified.fileio.RepositoryFileOutputStream;
import org.pentaho.platform.repository2.unified.jcr.PentahoJcrConstants;
import org.pentaho.platform.repository2.unified.webservices.DefaultUnifiedRepositoryWebService;
import org.pentaho.platform.repository2.unified.webservices.PropertiesWrapper;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileAclAceDto;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileAclDto;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileDto;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileTreeDto;
import org.pentaho.platform.repository2.unified.webservices.StringKeyStringValueDto;
import org.pentaho.platform.scheduler2.quartz.QuartzScheduler;
import org.pentaho.platform.security.policy.rolebased.actions.AdministerSecurityAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryCreateAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryReadAction;
import org.pentaho.platform.web.http.api.resources.SessionResource;
import org.pentaho.platform.web.http.api.resources.Setting;
import org.pentaho.platform.web.http.api.resources.StringListWrapper;
import org.pentaho.platform.web.http.api.resources.utils.FileUtils;

public class FileServiceTest {

  private static FileService fileService;
  private static final String COMMA = ",";
  private static final String FILE_1 = "file1";
  private static final String FILE_2 = "file2";
  private static final String FILE_3 = "file3";
  private static final String PARAMS = FILE_1 + COMMA + FILE_2;
  private static String USER_NAME = "user";
  private static String FOLDER_HOME = "home";
  private static String SEPARATOR = "/";
  private static String PATH_USER_HOME_FOLDER = SEPARATOR + FOLDER_HOME + SEPARATOR + USER_NAME;

  @Before
  public void setUp() {
    fileService = Mockito.spy( new FileService() );
    fileService.defaultUnifiedRepositoryWebService = Mockito.mock( DefaultUnifiedRepositoryWebService.class );
    fileService.repository = Mockito.mock( IUnifiedRepository.class );
    fileService.policy = Mockito.mock( IAuthorizationPolicy.class );
  }

  @After
  public void cleanup() {
    fileService = null;
  }

  @Test
  public void testDoDeleteFiles() throws Exception {

    fileService.doDeleteFiles( PARAMS );

    Mockito.verify( fileService.getRepoWs(), Mockito.times( 1 ) ).deleteFile( "file1", null );
    Mockito.verify( fileService.getRepoWs(), Mockito.times( 1 ) ).deleteFile( "file2", null );
  }

  @Test
  public void restoredFilesInTrashDeletedAfterRestoringInHomeDir_renameMode() throws Exception {
    FileService fileService = Mockito.mock( FileService.class );
    mockSession( fileService, USER_NAME );
    Mockito.when( fileService.doRestoreFilesInHomeDir( PARAMS, FileService.MODE_RENAME ) ).thenCallRealMethod();

    boolean restored = fileService.doRestoreFilesInHomeDir( PARAMS, FileService.MODE_RENAME );

    Mockito.verify( fileService ).doDeleteFilesPermanent( PARAMS );
    Assert.assertEquals( restored, true );
  }

  @Test
  public void onlyNonConflictFilesDeletedAfterRestoringInHomeDir_noOverwriteMode() throws Exception {
    FileService fileService = Mockito.mock( FileService.class );
    mockSession( fileService, USER_NAME );
    mockDoRestoreInHomeDir( fileService );

    final String filesToRestore = PARAMS;

    Mockito.when( fileService.getSourceFileIdsThatNotConflictWithFolderFiles( PATH_USER_HOME_FOLDER, PARAMS ) )
      .thenCallRealMethod();
    Mockito.when( fileService.getCommaSeparatedFileIds( Mockito.anyListOf( String.class ) ) ).thenCallRealMethod();

    boolean result = fileService.doRestoreFilesInHomeDir( filesToRestore, FileService.MODE_NO_OVERWRITE );

    Mockito.verify( fileService ).doDeleteFilesPermanent( FILE_2 );
    Assert.assertEquals( result, true );
  }

  @Test
  public void filesOverwrittenWhenConflict_overwriteMode() throws Exception {
    FileService fileService = Mockito.mock( FileService.class );
    mockSession( fileService, USER_NAME );
    mockDoRestoreInHomeDir( fileService );
    final String filesToRestore = PARAMS;

    Mockito.when( fileService.getFolderFileIdsThatConflictWithSource( PATH_USER_HOME_FOLDER, filesToRestore ) )
      .thenCallRealMethod();
    Mockito.when( fileService.getCommaSeparatedFileIds( Mockito.anyListOf( String.class ) ) ).thenCallRealMethod();

    boolean result = fileService.doRestoreFilesInHomeDir( filesToRestore, FileService.MODE_OVERWRITE );

    Mockito.verify( fileService ).doMoveFiles( PATH_USER_HOME_FOLDER, filesToRestore );
    Mockito.verify( fileService ).doDeleteFilesPermanent( FILE_1 );

    Assert.assertEquals( result, true );
  }


  @Test
  public void filesOverwrittenWhenNoConflict_overwriteMode() throws Exception {
    FileService fileService = Mockito.mock( FileService.class );
    mockSession( fileService, USER_NAME );
    final String filesToRestore = PARAMS;

    Mockito.when( fileService.doRestoreFilesInHomeDir( filesToRestore, FileService.MODE_OVERWRITE ) ).thenCallRealMethod();
    Mockito.when( fileService.getFolderFileIdsThatConflictWithSource( PATH_USER_HOME_FOLDER, filesToRestore ) )
      .thenReturn( "" );


    boolean result = fileService.doRestoreFilesInHomeDir( filesToRestore, FileService.MODE_OVERWRITE );

    Mockito.verify( fileService ).doMoveFiles( PATH_USER_HOME_FOLDER, filesToRestore );

    Assert.assertEquals( result, true );
  }


  @Test
  public void noFilesOverwrittenWhenDeletingOfConflictFilesFailed_overwriteMode() throws Exception {
    FileService fileService = Mockito.mock( FileService.class );
    mockSession( fileService, USER_NAME );
    Mockito.when( fileService.doRestoreFilesInHomeDir( PARAMS, FileService.MODE_OVERWRITE ) ).thenCallRealMethod();
    Mockito.when( fileService.getFolderFileIdsThatConflictWithSource( PATH_USER_HOME_FOLDER, PARAMS ) ).thenReturn( PARAMS );
    Mockito.doThrow( new Exception() ).when( fileService ).doDeleteFilesPermanent( Mockito.anyString() );

    boolean result = fileService.doRestoreFilesInHomeDir( PARAMS, FileService.MODE_OVERWRITE );

    Mockito.verify( fileService, Mockito.never() ).doMoveFiles( PATH_USER_HOME_FOLDER, PARAMS );
    Assert.assertEquals( result, false );
  }

  @Test
  public void conflictWhenRestoreFileNameEqFolderFileName() throws Exception {
    FileService fileService = Mockito.mock( FileService.class );
    mockDoRestoreInHomeDir( fileService );
    Mockito.when( fileService.canRestoreToFolderWithNoConflicts( PATH_USER_HOME_FOLDER, PARAMS ) ).thenCallRealMethod();

    boolean result = fileService.canRestoreToFolderWithNoConflicts( PATH_USER_HOME_FOLDER, PARAMS );
    Assert.assertEquals( result, false );
  }

  public List<RepositoryFileDto> getMockedRepositoryFileDtoList( String[] fileNames ) {
    List<RepositoryFileDto> repoFileDtoList = new ArrayList<>();

    for ( String fileName : fileNames ) {
      RepositoryFileDto repoFileDto = Mockito.mock( RepositoryFileDto.class );
      Mockito.when( repoFileDto.getName() ).thenReturn( fileName );
      Mockito.when( repoFileDto.getId() ).thenReturn( fileName );
      repoFileDtoList.add( repoFileDto );
    }

    return repoFileDtoList;
  }

  public RepositoryFile getMockedRepoFile( String fileName ) {
    RepositoryFile repoFileDto = Mockito.mock( RepositoryFile.class );
    Mockito.when( repoFileDto.getName() ).thenReturn( fileName );

    return repoFileDto;
  }

  public void mockDoRestoreInHomeDir( FileService fileService ) {
    IUnifiedRepository iUnifiedRepository = Mockito.mock( IUnifiedRepository.class );

    List<RepositoryFileDto> homeFolderFiles = getMockedRepositoryFileDtoList( new String[] { FILE_1, FILE_3 } );
    Mockito.when( fileService.doGetChildren( Mockito.eq( PATH_USER_HOME_FOLDER ), Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyBoolean() ) )
      .thenReturn( homeFolderFiles );

    Mockito.when( fileService.getRepository() ).thenReturn( iUnifiedRepository );
    Mockito.when( fileService.doRestoreFilesInHomeDir( Mockito.eq( PARAMS ), Mockito.anyInt() ) ).thenCallRealMethod();
    RepositoryFile mockRepoFile1 = getMockedRepoFile( FILE_1 );
    RepositoryFile mockRepoFile2 = getMockedRepoFile( FILE_2 );
    Mockito.when( iUnifiedRepository.getFileById( Mockito.eq( FILE_1 ) ) ).thenReturn( mockRepoFile1 );
    Mockito.when( iUnifiedRepository.getFileById( Mockito.eq( FILE_2 ) ) ).thenReturn( mockRepoFile2 );
  }


  public void mockSession( FileService fileService, String userName ) {
    IPentahoSession mockSession = Mockito.mock( IPentahoSession.class );
    Mockito.when( mockSession.getName() ).thenReturn( userName );
    Mockito.when( fileService.getSession() ).thenReturn( mockSession );
  }

  @Test
  public void testDoDeleteFilesException() {

    Mockito.doThrow( new IllegalArgumentException() ).when(
      fileService.defaultUnifiedRepositoryWebService ).deleteFile( Mockito.anyString(), Mockito.anyString() );

    try {
      fileService.doDeleteFiles( PARAMS );
      Assert.fail(); //This line should never be reached
    } catch ( IllegalArgumentException e ) {
      //Expected exception
    } catch ( Exception e ) {
      Assert.fail();
    }
  }

  @Test
  public void doCopyFilesException() throws Exception {

    String destinationPath = "/path/to/destination";
    String destinationPathColon = ":path:to:destination";

    String filePath1 = "/path/to/source/file1.ext";
    String fileId1 = "file1";

    String filePath2 = "/path/to/source/file2.ext";
    String fileId2 = "file2";

    Mockito.when( fileService.policy.isAllowed( Mockito.anyString() ) ).thenReturn( false );

    try {
      fileService.doCopyFiles( destinationPathColon, 1, fileId1 + "," + fileId2 );
      Assert.fail();
    } catch ( IllegalArgumentException e ) {
      //Should catch the exception
    }
  }

  @Test
  public void testDoGetFileOrDir() throws Exception {
    RepositoryFile file = Mockito.mock( RepositoryFile.class );
    Mockito.doReturn( "file.txt" ).when( file ).getName();

    Mockito.when( fileService.repository.getFile( Mockito.anyString() ) ).thenReturn( file );

    RepositoryFileInputStream mockInputStream = Mockito.mock( RepositoryFileInputStream.class );

    Mockito.doReturn( 1 ).when( fileService ).copy( Mockito.any( java.io.InputStream.class ), Mockito.any( java.io.OutputStream.class ) );
    Mockito.doReturn( mockInputStream ).when( fileService ).getRepositoryFileInputStream( Mockito.any( RepositoryFile.class ) );

    String pathId = "/usr/folder/file.txt";
    FileService.RepositoryFileToStreamWrapper wrapper = fileService.doGetFileOrDir( pathId );

    Assert.assertEquals( "file.txt", wrapper.getRepositoryFile().getName() );
  }

  @Test
  public void testDoGetFileOrDirException() throws Exception {
    RepositoryFile file = Mockito.mock( RepositoryFile.class );
    Mockito.doReturn( "file.txt" ).when( file ).getName();

    RepositoryFileInputStream mockInputStream = Mockito.mock( RepositoryFileInputStream.class );

    Mockito.doReturn( 1 ).when( fileService ).copy( Mockito.any( java.io.InputStream.class ), Mockito.any( java.io.OutputStream.class ) );
    Mockito.doReturn( mockInputStream ).when( fileService ).getRepositoryFileInputStream( Mockito.any( RepositoryFile.class ) );

    String pathId = "/usr/folder/file.txt";
    try {
      fileService.doGetFileOrDir( pathId );
      Assert.fail(); //This line should never be reached
    } catch ( FileNotFoundException fileNotFound ) {
      //Expected exception
    }
  }

  @Test
  public void testSetFileAcls() throws Exception {
    RepositoryFileDto file = Mockito.mock( RepositoryFileDto.class );
    Mockito.doReturn( "file.txt" ).when( file ).getName();
    Mockito.when( fileService.defaultUnifiedRepositoryWebService.getFile( Mockito.anyString() ) ).thenReturn( file );

    String pathId = "/usr/folder/file.txt";
    RepositoryFileAclDto acl = Mockito.mock( RepositoryFileAclDto.class );
    fileService.setFileAcls( pathId, acl );

    Mockito.verify( acl, Mockito.times( 1 ) ).setId( Mockito.anyString() );
    Mockito.verify( file, Mockito.times( 1 ) ).getId();
    Mockito.verify( fileService.defaultUnifiedRepositoryWebService, Mockito.times( 1 ) ).updateAcl( acl );
  }

  @Test
  public void testSetFileAclsException() throws Exception {
    String pathId = "/usr/folder/file.txt";
    RepositoryFileAclDto acl = Mockito.mock( RepositoryFileAclDto.class );
    try {
      fileService.setFileAcls( pathId, acl );
      Assert.fail();
    } catch ( FileNotFoundException e ) {
      //expected exception
    }
  }

  @Test
  public void testDoGetRootProperties() {
    fileService.doGetRootProperties();

    Mockito.verify( fileService.defaultUnifiedRepositoryWebService, Mockito.times( 1 ) ).
      getFile( FileUtils.PATH_SEPARATOR );
  }

  @Test
  public void testDoGetProperties() throws Exception {
    RepositoryFileDto file = Mockito.mock( RepositoryFileDto.class );
    Mockito.when( fileService.defaultUnifiedRepositoryWebService.getFile( Mockito.anyString() ) ).thenReturn( file );

    String pathId = "/usr/folder/file.txt";
    fileService.doGetProperties( pathId );

    Mockito.verify( fileService.defaultUnifiedRepositoryWebService, Mockito.times( 1 ) ).getFile( Mockito.anyString() );
  }

  @Test
  public void testDoGetPropertiesException() throws Exception {
    String pathId = "/usr/folder/file.txt";
    try {
      fileService.doGetProperties( pathId );
      Assert.fail();
    } catch ( FileNotFoundException fileNotFound ) {
      //expected exception
    }
  }

  @Test
  public void testDoCreateFile() throws Exception {
    RepositoryFileOutputStream mockOutputStream = Mockito.mock( RepositoryFileOutputStream.class );
    Mockito.doReturn( mockOutputStream ).when( fileService ).getRepositoryFileOutputStream( Mockito.anyString() );

    HttpServletRequest mockRequest = Mockito.mock( HttpServletRequest.class );
    InputStream mockInputStream = Mockito.mock( InputStream.class );

    Mockito.doReturn( 1 ).when( fileService ).copy( mockInputStream, mockOutputStream );

    String charsetName = "test";
    fileService.createFile( charsetName, "testString", mockInputStream );

    Mockito.verify( mockOutputStream, Mockito.times( 1 ) ).setCharsetName( Mockito.eq( charsetName ) );
    Mockito.verify( mockOutputStream, Mockito.times( 1 ) ).close();
    Mockito.verify( mockInputStream, Mockito.times( 1 ) ).close();
  }

  @Test
  public void testDoCreateFileException() {
    RepositoryFileOutputStream mockOutputStream = Mockito.mock( RepositoryFileOutputStream.class );
    Mockito.doThrow( new IllegalArgumentException() ).when( fileService ).idToPath( Mockito.anyString() );

    try {
      fileService.createFile( null, null, null );
      Assert.fail();
    } catch ( IllegalArgumentException e ) {
      // expected
    } catch ( Exception e ) {
      Assert.fail();
    }
  }

  @Test
  public void testDoDeleteFilesPermanent() throws Exception {

    fileService.doDeleteFilesPermanent( PARAMS );

    Mockito.verify( fileService.getRepoWs(), Mockito.times( 1 ) ).deleteFileWithPermanentFlag( "file1", true, null );
    Mockito.verify( fileService.getRepoWs(), Mockito.times( 1 ) ).deleteFileWithPermanentFlag( "file2", true, null );
  }

  @Test
  public void testDoDeleteLocale() throws Exception {
    RepositoryFileDto file = Mockito.mock( RepositoryFileDto.class );
    Mockito.doReturn( file ).when( fileService.defaultUnifiedRepositoryWebService ).getFile( Mockito.anyString() );
    Mockito.doReturn( "file.txt" ).when( file ).getId();
    fileService.doDeleteLocale( file.getId(), "en_US" );
    Mockito.verify( fileService.getRepoWs(), Mockito.times( 1 ) ).deleteLocalePropertiesForFile( "file.txt", "en_US" );
  }

  @Test
  public void testDoGetCanAccessList() {
    String permissions = RepositoryFilePermission.READ.ordinal() + "|" + RepositoryFilePermission.WRITE.ordinal() + "|"
      + RepositoryFilePermission.DELETE.ordinal();
    Mockito.doReturn( true ).when( fileService.repository ).hasAccess( Mockito.anyString(), Mockito.any( EnumSet.class ) );
    List<Setting> settings = fileService.doGetCanAccessList( "pathId", permissions );
    Assert.assertTrue( settings.size() > 0 );
  }

  @Test
  public void testDoGetPathsAccessList() {
    List<String> paths = new ArrayList<String>();
    paths.add( "path1" );
    paths.add( "path2" );
    paths.add( "path3" );

    Mockito.doReturn( true ).when( fileService.repository ).hasAccess( Mockito.anyString(), Mockito.any( EnumSet.class ) );
    List<Setting> settings = fileService.doGetPathsAccessList( new StringListWrapper( paths ) );
    Assert.assertTrue( settings.size() > 0 );

    Mockito.doReturn( false ).when( fileService.repository ).hasAccess( Mockito.anyString(), Mockito.any( EnumSet.class ) );
    settings = fileService.doGetPathsAccessList( new StringListWrapper( paths ) );
    Assert.assertEquals( 0, settings.size() );
  }

  @Test
  public void testDoDeleteFilesPermanentException() {

    Mockito.doThrow( new IllegalArgumentException() ).when(
      fileService.defaultUnifiedRepositoryWebService ).deleteFileWithPermanentFlag( Mockito.anyString(), Mockito.eq( true ),
      Mockito.anyString() );

    try {
      fileService.doDeleteFilesPermanent( PARAMS );
      Assert.fail(); //This line should never be reached
    } catch ( Exception e ) {
      //Expected exception
    }
  }

  @Test
  public void testDoMoveFiles() throws Exception {
    String destPathId = "/test";
    String[] params = { "file1", "file2" };

    RepositoryFileDto repositoryFileDto = Mockito.mock( RepositoryFileDto.class );
    Mockito.doReturn( destPathId ).when( repositoryFileDto ).getPath();

    Mockito.doReturn( repositoryFileDto ).when( fileService.defaultUnifiedRepositoryWebService ).getFile( destPathId );

    fileService.doMoveFiles( destPathId, StringUtils.join( params, "," ) );

    Mockito.verify( fileService.getRepoWs(), Mockito.times( 1 ) ).moveFile( params[ 0 ], destPathId, null );
    Mockito.verify( fileService.getRepoWs(), Mockito.times( 1 ) ).moveFile( params[ 1 ], destPathId, null );
  }

  @Test
  public void testDoMoveFilesForUnknownDestPath() throws Exception {
    String destPathId = "/test";
    String[] params = { "file1", "file2" };

    Mockito.doReturn( null ).when( fileService.defaultUnifiedRepositoryWebService ).getFile( destPathId );

    try {
      fileService.doMoveFiles( destPathId, StringUtils.join( params, "," ) );
      Assert.assertTrue( false );
    } catch ( FileNotFoundException e ) {
      Mockito.verify( fileService.getRepoWs(), Mockito.times( 0 ) ).moveFile( params[ 0 ], destPathId, null );
      Mockito.verify( fileService.getRepoWs(), Mockito.times( 0 ) ).moveFile( params[ 1 ], destPathId, null );
    }
  }

  @Test
  public void testDoMoveFilesException() throws Exception {
    String destPathId = "/test";
    String[] params = { "file1", "file2" };

    RepositoryFileDto repositoryFileDto = Mockito.mock( RepositoryFileDto.class );
    Mockito.doReturn( destPathId ).when( repositoryFileDto ).getPath();

    Mockito.doReturn( repositoryFileDto ).when( fileService.defaultUnifiedRepositoryWebService ).getFile( destPathId );
    Mockito.doThrow( new InternalError() ).when( fileService.defaultUnifiedRepositoryWebService ).moveFile(
      params[ 0 ], destPathId, null );

    try {
      fileService.doMoveFiles( destPathId, StringUtils.join( params, "," ) );
      Assert.fail(); //This line should never be reached
    } catch ( Throwable e ) {
      Mockito.verify( fileService.getRepoWs(), Mockito.times( 1 ) ).moveFile( params[ 0 ], destPathId, null );
      Mockito.verify( fileService.getRepoWs(), Mockito.times( 0 ) ).moveFile( params[ 1 ], destPathId, null );
    }
  }

  @Test
  public void testDoRestoreFiles() throws Exception {
    String[] params = { "file1", "file2" };

    fileService.doRestoreFiles( StringUtils.join( params, "," ) );

    Mockito.verify( fileService.getRepoWs(), Mockito.times( 1 ) ).undeleteFile( params[ 0 ], null );
    Mockito.verify( fileService.getRepoWs(), Mockito.times( 1 ) ).undeleteFile( params[ 1 ], null );
  }

  @Test
  public void testDoRestoreFilesException() throws Exception {
    String[] params = { "file1", "file2" };

    Mockito.doThrow( new InternalError() ).when( fileService.defaultUnifiedRepositoryWebService ).undeleteFile(
      params[ 0 ], null );

    try {
      fileService.doRestoreFiles( StringUtils.join( params, "," ) );
      Assert.fail(); //This line should never be reached
    } catch ( InternalError e ) {
      Mockito.verify( fileService.getRepoWs(), Mockito.times( 1 ) ).undeleteFile( params[ 0 ], null );
      Mockito.verify( fileService.getRepoWs(), Mockito.times( 0 ) ).undeleteFile( params[ 1 ], null );
    }
  }

  @Test
  public void testDoGetFileAsInline() throws FileNotFoundException {
    /*
     * TEST 1
     */
    Mockito.doReturn( true ).when( fileService ).isPath( Mockito.anyString() );
    Mockito.doReturn( true ).when( fileService ).isPathValid( Mockito.anyString() );

    RepositoryDownloadWhitelist mockWhiteList = Mockito.mock( RepositoryDownloadWhitelist.class );
    Mockito.doReturn( mockWhiteList ).when( fileService ).getWhitelist();
    Mockito.doReturn( true ).when( mockWhiteList ).accept( Mockito.anyString() );

    RepositoryFile mockRepoFile = Mockito.mock( RepositoryFile.class );
    Mockito.doReturn( mockRepoFile ).when( fileService.repository ).getFile( Mockito.anyString() );

    SimpleRepositoryFileData mockData = Mockito.mock( SimpleRepositoryFileData.class );
    Mockito.doReturn( mockData ).when( fileService.repository ).getDataForRead( Mockito.any( Serializable.class ), Mockito.any( Class.class ) );

    InputStream mockInputStream = Mockito.mock( InputStream.class );
    Mockito.doReturn( mockInputStream ).when( mockData ).getInputStream();

    StreamingOutput mockStreamingOutput = Mockito.mock( StreamingOutput.class );
    Mockito.doReturn( mockStreamingOutput ).when( fileService ).getStreamingOutput( mockInputStream );

    FileService.RepositoryFileToStreamWrapper wrapper = fileService.doGetFileAsInline( "test" );

    Mockito.verify( fileService.repository, Mockito.times( 1 ) ).getFile( Mockito.anyString() );
    Mockito.verify( mockWhiteList, Mockito.times( 1 ) ).accept( Mockito.anyString() );
    Mockito.verify( fileService, Mockito.times( 2 ) ).getRepository();
    Mockito.verify( fileService.repository, Mockito.times( 1 ) ).getDataForRead( Mockito.any( Serializable.class ), Mockito.any( Class.class ) );
    Mockito.verify( mockData, Mockito.times( 1 ) ).getInputStream();

    Assert.assertEquals( mockRepoFile, wrapper.getRepositoryFile() );
    Assert.assertEquals( mockStreamingOutput, wrapper.getOutputStream() );

    /*
     * TEST 2
     */
    Mockito.doReturn( false ).when( fileService ).isPath( Mockito.anyString() );
    Mockito.doReturn( mockRepoFile ).when( fileService.repository ).getFileById( Mockito.anyString() );

    wrapper = fileService.doGetFileAsInline( "test" );

    Mockito.verify( fileService.repository, Mockito.times( 1 ) ).getFileById( Mockito.anyString() );
    Mockito.verify( fileService, Mockito.times( 4 ) ).getRepository();

    Assert.assertEquals( mockRepoFile, wrapper.getRepositoryFile() );
    Assert.assertEquals( mockStreamingOutput, wrapper.getOutputStream() );
  }

  @Test
  public void testDoGetFileAsInlineException() {

    /*
     * TEST 1
     */
    Mockito.doReturn( true ).when( fileService ).isPath( Mockito.anyString() );
    Mockito.doReturn( false ).when( fileService ).isPathValid( Mockito.anyString() );

    try {
      fileService.doGetFileAsInline( "test" );
      Assert.fail();
    } catch ( IllegalArgumentException e ) {
      // Excpected
    } catch ( FileNotFoundException e ) {
      Assert.fail();
    }

    /*
     * TEST 2
     */
    Mockito.doReturn( true ).when( fileService ).isPathValid( Mockito.anyString() );
    Mockito.doReturn( null ).when( fileService.repository ).getFile( Mockito.anyString() );

    try {
      fileService.doGetFileAsInline( "test" );
      Assert.fail();
    } catch ( FileNotFoundException e ) {
      // Expected
    }

    /*
     * TEST 3
     */
    RepositoryFile mockFile = Mockito.mock( RepositoryFile.class );
    Mockito.doReturn( mockFile ).when( fileService.repository ).getFile( Mockito.anyString() );

    RepositoryDownloadWhitelist mockWhiteList = Mockito.mock( RepositoryDownloadWhitelist.class );
    Mockito.doReturn( mockWhiteList ).when( fileService ).getWhitelist();
    Mockito.doReturn( false ).when( mockWhiteList ).accept( Mockito.anyString() );

    IAuthorizationPolicy mockPolicy = Mockito.mock( IAuthorizationPolicy.class );
    Mockito.doReturn( mockPolicy ).when( fileService ).getPolicy();
    Mockito.doReturn( false ).when( mockPolicy ).isAllowed( Mockito.anyString() );

    try {
      fileService.doGetFileAsInline( "test" );
      Assert.fail();
    } catch ( IllegalArgumentException e ) {
      // Excpected
    } catch ( FileNotFoundException e ) {
      Assert.fail();
    }

    /*
     * TEST 4
     */
    Mockito.doReturn( true ).when( mockWhiteList ).accept( Mockito.anyString() );
    Mockito.doThrow( new InternalError() ).when( fileService.repository )
      .getDataForRead( Mockito.any( Serializable.class ), Mockito.any( Class.class ) );

    try {
      fileService.doGetFileAsInline( "test" );
      Assert.fail();
    } catch ( InternalError e ) {
      // Excpected
    } catch ( FileNotFoundException e ) {
      Assert.fail();
    }
  }

  @Test
  public void testDoGetLocalProperties() throws Exception {
    String pathId = "path:to:file:file1.ext";
    String fileId = "file1";
    String locale = "";

    Mockito.doReturn( "/path/to/file/file1.ext" ).when( fileService ).idToPath( pathId );

    Set<String> propertiesList = new HashSet<String>();
    propertiesList.add( "prop1" );
    propertiesList.add( "prop2" );

    RepositoryFileDto repositoryFileDto = Mockito.mock( RepositoryFileDto.class );
    Mockito.doReturn( fileId ).when( repositoryFileDto ).getId();

    Mockito.doReturn( repositoryFileDto ).when( fileService.defaultUnifiedRepositoryWebService ).getFile( Mockito.anyString() );

    Properties properties = Mockito.mock( Properties.class );
    Mockito.doReturn( "value1" ).when( properties ).getProperty( "prop1" );
    Mockito.doReturn( "value2" ).when( properties ).getProperty( "prop2" );
    Mockito.doReturn( false ).when( properties ).isEmpty();
    Mockito.doReturn( propertiesList ).when( properties ).stringPropertyNames();

    PropertiesWrapper propertiesWrapper = Mockito.mock( PropertiesWrapper.class );
    Mockito.when( propertiesWrapper.getProperties() ).thenReturn( properties );

    Mockito.doReturn( propertiesWrapper ).when( fileService.defaultUnifiedRepositoryWebService )
      .getLocalePropertiesForFileById( Mockito.anyString(), Mockito.anyString() );

    List<StringKeyStringValueDto> keyValueList = fileService.doGetLocaleProperties( pathId, locale );

    Mockito.verify( fileService.defaultUnifiedRepositoryWebService ).getFile( "/path/to/file/file1.ext" );
    Mockito.verify( properties ).getProperty( "prop1" );
    Mockito.verify( properties ).getProperty( "prop2" );
    Mockito.verify( properties ).isEmpty();
    Mockito.verify( properties ).stringPropertyNames();
    Mockito.verify( fileService.defaultUnifiedRepositoryWebService ).getLocalePropertiesForFileById( Mockito.anyString(), Mockito.anyString() );

    Assert.assertEquals( 2, keyValueList.size() );
    Assert.assertEquals( "prop1", keyValueList.get( 1 ).getKey() );
    Assert.assertEquals( "prop2", keyValueList.get( 0 ).getKey() );
    Assert.assertEquals( "value1", keyValueList.get( 1 ).getValue() );
    Assert.assertEquals( "value2", keyValueList.get( 0 ).getValue() );
  }

  @Test
  public void testDoSetLocalProperties() throws Exception {
    String pathId = "path:to:file:file1.ext";
    String fileId = "file1";
    String locale = "";

    Mockito.doReturn( "/path/to/file/file1.ext" ).when( fileService ).idToPath( pathId );

    RepositoryFileDto repositoryFileDto = Mockito.mock( RepositoryFileDto.class );
    Mockito.doReturn( fileId ).when( repositoryFileDto ).getId();

    Mockito.doReturn( repositoryFileDto ).when( fileService.defaultUnifiedRepositoryWebService ).getFile( Mockito.anyString() );

    Properties fileProperties = Mockito.mock( Properties.class );
    Mockito.doReturn( false ).when( fileProperties ).isEmpty();

    List<StringKeyStringValueDto> properties = new ArrayList<StringKeyStringValueDto>();
    properties.add( new StringKeyStringValueDto( "key1", "value1" ) );
    properties.add( new StringKeyStringValueDto( "key2", "value2" ) );

    fileService.doSetLocaleProperties( pathId, locale, properties );

    Mockito.verify( fileService.defaultUnifiedRepositoryWebService ).getFile( "/path/to/file/file1.ext" );
    Mockito.verify( fileService.defaultUnifiedRepositoryWebService )
      .setLocalePropertiesForFileByFileId( Mockito.anyString(), Mockito.anyString(), Mockito.any( Properties.class ) );
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

    Mockito.doReturn( "/path/to/file/file1.ext" ).when( fileService ).idToPath( pathId );
    Mockito.doReturn( true ).when( fileService.defaultUnifiedRepositoryWebService ).hasAccess( Mockito.anyString(), Mockito.anyList() );

    String hasAccess = fileService.doGetCanAccess( pathId, permissions );

    Mockito.verify( fileService.defaultUnifiedRepositoryWebService ).hasAccess( "/path/to/file/file1.ext", permissionsList );

    Assert.assertEquals( "true", hasAccess );
  }

  @Test
  public void testDoSetContentCreator() throws Exception {
    String param = "file1";
    RepositoryFileDto repositoryFileDto = Mockito.mock( RepositoryFileDto.class );
    Map<String, Serializable> fileMetadata = Mockito.mock( Map.class );

    String idToPathResult = "/file1";
    Mockito.doReturn( param ).when( repositoryFileDto ).getId();
    Mockito.when( fileService.idToPath( param ) ).thenReturn( idToPathResult );
    Mockito.doReturn( repositoryFileDto ).when( fileService.defaultUnifiedRepositoryWebService )
      .getFile( Mockito.eq( idToPathResult ) );
    Mockito.when( fileService.getRepository().getFileMetadata( repositoryFileDto.getId() ) ).thenReturn( fileMetadata );

    try {
      fileService.doSetContentCreator( param, repositoryFileDto );
      Mockito.verify( fileService.getRepository(), Mockito.times( 1 ) ).getFileMetadata( repositoryFileDto.getId() );
      Mockito.verify( fileService.getRepository(), Mockito.times( 1 ) ).setFileMetadata( param, fileMetadata );
    } catch ( FileNotFoundException e ) {
      Assert.fail();
    } catch ( InternalError e ) {
      Assert.fail();
    }
  }

  @Test
  public void testDoSetContentCreatorFileNotFoundException() throws Exception {
    String param = "file1";
    RepositoryFileDto mockFileDto = Mockito.mock( RepositoryFileDto.class );
    Map<String, Serializable> fileMetadata = Mockito.mock( Map.class );

    Mockito.when( fileService.idToPath( param ) ).thenReturn( "/file1" );
    Mockito.doReturn( null ).when( fileService.defaultUnifiedRepositoryWebService ).getFile( "/file1" );

    try {
      fileService.doSetContentCreator( param, mockFileDto );
      Assert.fail();
    } catch ( FileNotFoundException e ) {
    }
  }

  @Test
  public void testDoSetContentCreatorFileException() throws Exception {
    String param = "file1";
    RepositoryFileDto repositoryFileDto = Mockito.mock( RepositoryFileDto.class );
    Map<String, Serializable> fileMetadata = Mockito.mock( Map.class );

    Mockito.doReturn( param ).when( repositoryFileDto ).getId();
    Mockito.when( fileService.idToPath( param ) ).thenReturn( "/file1" );
    Mockito.doReturn( repositoryFileDto ).when( fileService.defaultUnifiedRepositoryWebService ).getFile( "/file1" );
    Mockito.when( fileService.getRepository().getFileMetadata( repositoryFileDto.getId() ) ).thenThrow( new InternalError() );

    //failing in get
    try {
      fileService.doSetContentCreator( param, repositoryFileDto );
    } catch ( FileNotFoundException e ) {
      Assert.fail();
    } catch ( InternalError e ) {
      Mockito.verify( fileMetadata, Mockito.times( 0 ) ).put( PentahoJcrConstants.PHO_CONTENTCREATOR, param );
      Mockito.verify( fileService.repository, Mockito.times( 0 ) ).setFileMetadata( param, fileMetadata );
    }
  }

  @Test
  public void testDoGetFileLocales() {
    String param = "file1";
    RepositoryFileDto repositoryFileDto = Mockito.mock( RepositoryFileDto.class );
    List<PentahoLocale> locales = new ArrayList<PentahoLocale>();
    PentahoLocale mockedLocale = Mockito.mock( PentahoLocale.class );
    locales.add( mockedLocale );

    Mockito.doReturn( param ).when( repositoryFileDto ).getId();
    Mockito.doReturn( repositoryFileDto ).when( fileService.defaultUnifiedRepositoryWebService ).getFile( "/" + param );
    Mockito.when( fileService.defaultUnifiedRepositoryWebService.getAvailableLocalesForFileById( repositoryFileDto.getId() ) )
      .thenReturn( locales );

    try {
      fileService.doGetFileLocales( param );
      Mockito.verify( fileService.getRepository(), Mockito.times( 0 ) ).getAvailableLocalesForFileById( repositoryFileDto.getId() );
    } catch ( FileNotFoundException e ) {
      Assert.fail();
    } catch ( InternalError e ) {
      Assert.fail();
    }
  }

  @Test
  public void testDoGetFileLocalesFileNotFoundException() {
    String param = "file1";
    RepositoryFileDto repositoryFileDto = Mockito.mock( RepositoryFileDto.class );

    Mockito.doReturn( param ).when( repositoryFileDto ).getId();
    Mockito.doReturn( null ).when( fileService.defaultUnifiedRepositoryWebService ).getFile( "/" + param );

    try {
      fileService.doGetFileLocales( param );
      Assert.fail();
    } catch ( FileNotFoundException e ) {
      Mockito.verify( fileService.getRepository(), Mockito.times( 0 ) ).getAvailableLocalesForFileById( repositoryFileDto.getId() );
    }
  }

  @Test
  public void testDoGetFileLocalesException() {
    String param = "file1";
    RepositoryFileDto repositoryFileDto = Mockito.mock( RepositoryFileDto.class );

    Mockito.doReturn( param ).when( repositoryFileDto ).getId();
    Mockito.doReturn( repositoryFileDto ).when( fileService.defaultUnifiedRepositoryWebService ).getFile( "/" + param );
    Mockito.when( fileService.defaultUnifiedRepositoryWebService.getAvailableLocalesForFileById( repositoryFileDto.getId() ) )
      .thenThrow( new InternalError() );

    try {
      fileService.doGetFileLocales( param );
    } catch ( FileNotFoundException e ) {
      Assert.fail();
    } catch ( InternalError e ) {
      Mockito.verify( fileService.getRepository(), Mockito.times( 0 ) ).getAvailableLocalesForFileById( repositoryFileDto.getId() );
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

    Mockito.doReturn( characters ).when( fileService.defaultUnifiedRepositoryWebService ).getReservedChars();

    StringBuffer buffer = fileService.doGetReservedChars();

    Mockito.verify( fileService.defaultUnifiedRepositoryWebService ).getReservedChars();

    Assert.assertEquals( stringBuffer.toString(), buffer.toString() );
  }

  @Test
  public void testDoGetReservedCharactersDisplay() throws Exception {
    String stringBuffer = "/,\\,\\t,\\r,\\n";

    List<Character> characters = new ArrayList<Character>();
    characters.add( '/' );
    characters.add( '\\' );
    characters.add( '\t' );
    characters.add( '\r' );
    characters.add( '\n' );

    Mockito.doReturn( "\\t" ).when( fileService ).escapeJava( "" + characters.get( 2 ) );
    Mockito.doReturn( "\\r" ).when( fileService ).escapeJava( "" + characters.get( 3 ) );
    Mockito.doReturn( "\\n" ).when( fileService ).escapeJava( "" + characters.get( 4 ) );

    Mockito.doReturn( characters ).when( fileService.defaultUnifiedRepositoryWebService ).getReservedChars();
    StringBuffer buffer = fileService.doGetReservedCharactersDisplay();
    Assert.assertEquals( buffer.toString(), stringBuffer.toString() );

    Mockito.verify( fileService, Mockito.times( 3 ) ).escapeJava( Mockito.anyString() );
  }

  @Test
  public void testDoGetFileOrDirAsDownload0() throws Throwable {
    assertDoGetFileOrDirAsDownload( "mockFileName.prpt", "true", "mockFileName.prpt.zip", "mockFileName.prpt.zip" );
  }

  @Test
  public void testDoGetFileOrDirAsDownload1() throws Throwable {
    assertDoGetFileOrDirAsDownload( "mockFileName.prpt", "false", "mockFileName.prpt", "mockFileName.prpt" );
  }

  @Test
  public void testDoGetFileOrDirAsDownload2() throws Throwable {
    assertDoGetFileOrDirAsDownload( "mock File+Name(%25).prpt", "true", "mock%20File%2BName%28%2525%29.prpt.zip",
      "mock File+Name(%25).prpt.zip" );
  }

  @Test
  public void testDoGetFileOrDirAsDownload3() throws Throwable {
    assertDoGetFileOrDirAsDownload( "mock File+Name(%25).prpt", "false", "mock%20File%2BName%28%2525%29.prpt",
      "mock File+Name(%25).prpt" );
  }

  /**
   * @param fileName
   * @param withManifest
   * @param expectedEncodedFileName
   * @param expectedFileName
   * @throws Throwable
   */
  public void assertDoGetFileOrDirAsDownload( final String fileName, final String withManifest,
                                              final String expectedEncodedFileName, final String expectedFileName )
    throws Throwable {

    IAuthorizationPolicy mockAuthPolicy = Mockito.mock( IAuthorizationPolicy.class );
    Mockito.doReturn( true ).when( mockAuthPolicy ).isAllowed( Mockito.anyString() );

    BaseExportProcessor mockExportProcessor = Mockito.mock( BaseExportProcessor.class );
    File mockExportFile = Mockito.mock( File.class );
    ExportHandler mockExportHandler = Mockito.mock( ExportHandler.class );
    StreamingOutput mockStream = Mockito.mock( StreamingOutput.class );

    RepositoryFile mockRepoFile = Mockito.mock( RepositoryFile.class );
    Mockito.doReturn( fileName ).when( mockRepoFile ).getName();
    Mockito.doReturn( mockExportFile ).when( mockExportProcessor ).performExport( mockRepoFile );

    Mockito.doReturn( mockRepoFile ).when( fileService.repository ).getFile( Mockito.anyString() );
    Mockito.doReturn( mockAuthPolicy ).when( fileService ).getPolicy();
    Mockito.doReturn( mockExportProcessor ).when( fileService ).getDownloadExportProcessor( Mockito.anyString(), Mockito.anyBoolean(),
      Mockito.anyBoolean() );
    Mockito.doReturn( mockExportHandler ).when( fileService ).getDownloadExportHandler();
    Mockito.doReturn( mockStream ).when( fileService ).getDownloadStream( mockRepoFile, mockExportProcessor );

    FileService.DownloadFileWrapper wrapper =
      fileService.doGetFileOrDirAsDownload( "", "mock:path:" + fileName, withManifest );

    Mockito.verify( fileService.repository, Mockito.times( 1 ) ).getFile( Mockito.anyString() );

    Assert.assertEquals( mockStream, wrapper.getOutputStream() );
    Assert.assertEquals( expectedEncodedFileName, wrapper.getEncodedFileName() );
    Assert.assertEquals( "attachment; filename=\"" + expectedFileName + "\"", wrapper.getAttachment() );
  }

  @Test
  public void testDoGetFileOrDirAsDownloadException() {
    // Test 1
    IAuthorizationPolicy mockAuthPolicy = Mockito.mock( IAuthorizationPolicy.class );
    Mockito.doReturn( false ).when( mockAuthPolicy ).isAllowed( Mockito.anyString() );
    Mockito.doReturn( mockAuthPolicy ).when( fileService ).getPolicy();

    try {
      FileService.DownloadFileWrapper wrapper =
        fileService.doGetFileOrDirAsDownload( "", "mock:path:fileName", "true" );
      Assert.fail();
    } catch ( GeneralSecurityException e ) {
      // Expected
    } catch ( Throwable t ) {
      Assert.fail();
    }

    // Test 2
    Mockito.doReturn( true ).when( mockAuthPolicy ).isAllowed( Mockito.anyString() );
    try {
      fileService.doGetFileOrDirAsDownload( "", "", "true" );
      Assert.fail();
    } catch ( InvalidParameterException e ) {
      // Expected
    } catch ( Throwable e ) {
      Assert.fail();
    }

    // Test 3
    Mockito.doReturn( false ).when( fileService ).isPathValid( Mockito.anyString() );
    try {
      fileService.doGetFileOrDirAsDownload( "", "mock:path:fileName", "true" );
      Assert.fail();
    } catch ( IllegalSelectorException e ) {
      // Expected
    } catch ( Throwable t ) {
      Assert.fail();
    }

    /*
     * Test 4
     */
    Mockito.doReturn( true ).when( fileService ).isPathValid( Mockito.anyString() );
    Mockito.doReturn( null ).when( fileService.repository ).getFile( Mockito.anyString() );
    try {
      fileService.doGetFileOrDirAsDownload( "", "mock:path:fileName", "true" );
      Assert.fail();
    } catch ( FileNotFoundException e ) {
      // Expected
    } catch ( Throwable t ) {
    }
  }

  public void testDoCanAdminister() throws Exception {
    IAuthorizationPolicy authorizationPolicy = Mockito.mock( IAuthorizationPolicy.class );
    Mockito.doReturn( authorizationPolicy ).when( fileService ).getPolicy();

    Mockito.doReturn( true ).when( authorizationPolicy ).isAllowed( RepositoryReadAction.NAME );
    Mockito.doReturn( true ).when( authorizationPolicy ).isAllowed( RepositoryCreateAction.NAME );
    Mockito.doReturn( true ).when( authorizationPolicy ).isAllowed( AdministerSecurityAction.NAME );
    Assert.assertTrue( fileService.doCanAdminister() );

    Mockito.doReturn( false ).when( authorizationPolicy ).isAllowed( RepositoryReadAction.NAME );
    Mockito.doReturn( true ).when( authorizationPolicy ).isAllowed( RepositoryCreateAction.NAME );
    Mockito.doReturn( true ).when( authorizationPolicy ).isAllowed( AdministerSecurityAction.NAME );
    Assert.assertFalse( fileService.doCanAdminister() );

    Mockito.doReturn( true ).when( authorizationPolicy ).isAllowed( RepositoryReadAction.NAME );
    Mockito.doReturn( false ).when( authorizationPolicy ).isAllowed( RepositoryCreateAction.NAME );
    Mockito.doReturn( true ).when( authorizationPolicy ).isAllowed( AdministerSecurityAction.NAME );
    Assert.assertFalse( fileService.doCanAdminister() );

    Mockito.doReturn( true ).when( authorizationPolicy ).isAllowed( RepositoryReadAction.NAME );
    Mockito.doReturn( true ).when( authorizationPolicy ).isAllowed( RepositoryCreateAction.NAME );
    Mockito.doReturn( false ).when( authorizationPolicy ).isAllowed( AdministerSecurityAction.NAME );
    Assert.assertFalse( fileService.doCanAdminister() );

    Mockito.doReturn( false ).when( authorizationPolicy ).isAllowed( RepositoryReadAction.NAME );
    Mockito.doReturn( false ).when( authorizationPolicy ).isAllowed( RepositoryCreateAction.NAME );
    Mockito.doReturn( false ).when( authorizationPolicy ).isAllowed( AdministerSecurityAction.NAME );
    Assert.assertFalse( fileService.doCanAdminister() );
  }

  @Test
  public void testDoCanAdministerException() throws Exception {
    IAuthorizationPolicy authorizationPolicy = Mockito.mock( IAuthorizationPolicy.class );
    Mockito.doReturn( authorizationPolicy ).when( fileService ).getPolicy();

    Mockito.doThrow( new InternalError() ).when( authorizationPolicy ).isAllowed( RepositoryReadAction.NAME );
    Mockito.doReturn( true ).when( authorizationPolicy ).isAllowed( RepositoryCreateAction.NAME );
    Mockito.doReturn( false ).when( authorizationPolicy ).isAllowed( AdministerSecurityAction.NAME );

    try {
      Assert.assertFalse( fileService.doCanAdminister() );
      Assert.fail();
    } catch ( InternalError e ) {
    }

    Mockito.doReturn( false ).when( authorizationPolicy ).isAllowed( RepositoryReadAction.NAME );
    Mockito.doThrow( new InternalError() ).when( authorizationPolicy ).isAllowed( RepositoryCreateAction.NAME );
    Mockito.doReturn( true ).when( authorizationPolicy ).isAllowed( AdministerSecurityAction.NAME );

    try {
      Assert.assertFalse( fileService.doCanAdminister() );
    } catch ( InternalError e ) { //the first comparison fail and the result should be false and no exception returned
      Assert.fail();
    }

    Mockito.doReturn( true ).when( authorizationPolicy ).isAllowed( RepositoryReadAction.NAME );
    Mockito.doReturn( false ).when( authorizationPolicy ).isAllowed( RepositoryCreateAction.NAME );
    Mockito.doThrow( new InternalError() ).when( authorizationPolicy ).isAllowed( AdministerSecurityAction.NAME );

    try {
      Assert.assertFalse( fileService.doCanAdminister() );
    } catch ( InternalError e ) { //the second comparison fail and the result should be false and no exception returned
      Assert.fail();
    }
  }

  @Test
  public void testDoGetCanCreate() {
    Mockito.doReturn( true ).when( fileService.policy ).isAllowed( Mockito.anyString() );

    String canCreate = fileService.doGetCanCreate();

    Mockito.verify( fileService.policy ).isAllowed( Mockito.anyString() );

    Assert.assertEquals( "true", canCreate );
  }

  @Test
  public void doGetContentCreator() {
    String pathId = "path:to:file:file1.ext";
    String fileId = "file1";
    String creatorId = "creatorId";

    Map<String, Serializable> fileMetadata = Mockito.mock( HashMap.class );
    Mockito.doReturn( creatorId ).when( fileMetadata ).get( "contentCreator" );

    Mockito.doReturn( fileMetadata ).when( fileService.repository ).getFileMetadata( fileId );

    Mockito.doReturn( "/path/to/file/file1.ext" ).when( fileService ).idToPath( pathId );

    RepositoryFileDto repositoryFileDto = Mockito.mock( RepositoryFileDto.class );
    Mockito.doReturn( fileId ).when( repositoryFileDto ).getId();

    Mockito.doReturn( repositoryFileDto ).when( fileService.defaultUnifiedRepositoryWebService ).getFile( Mockito.anyString() );

    RepositoryFileDto repositoryFileDto1 = Mockito.mock( RepositoryFileDto.class );

    Mockito.doReturn( repositoryFileDto1 ).when( fileService.defaultUnifiedRepositoryWebService ).getFileById( creatorId );


    // Test 1
    RepositoryFileDto repositoryFileDto2 = null;
    try {
      repositoryFileDto2 = fileService.doGetContentCreator( pathId );
    } catch ( Exception e ) {
      Assert.fail();
    }

    Assert.assertEquals( repositoryFileDto1, repositoryFileDto2 );

    // Test 2
    Mockito.doReturn( null ).when( fileMetadata ).get( "contentCreator" );

    try {
      repositoryFileDto2 = fileService.doGetContentCreator( pathId );
      Assert.assertEquals( null, repositoryFileDto2 );
    } catch ( Exception e ) {
      Assert.fail();
    }

    // Test 3
    Mockito.doReturn( "" ).when( fileMetadata ).get( "contentCreator" );

    try {
      repositoryFileDto2 = fileService.doGetContentCreator( pathId );
      Assert.assertEquals( null, repositoryFileDto2 );
    } catch ( Exception e ) {
      Assert.fail();
    }

    Mockito.verify( fileService, Mockito.times( 3 ) ).idToPath( pathId );
    Mockito.verify( fileService.repository, Mockito.times( 3 ) ).getFileMetadata( fileId );
    Mockito.verify( fileService.defaultUnifiedRepositoryWebService, Mockito.times( 3 ) ).getFile( Mockito.anyString() );
    Mockito.verify( fileService.defaultUnifiedRepositoryWebService ).getFileById( Mockito.anyString() );
  }

  @Test
  public void doGetContentCreatorException() {
    String pathId = "path:to:file:file1.ext";
    String fileId = "file1";
    String creatorId = "creatorId";

    Map<String, Serializable> fileMetadata = Mockito.mock( HashMap.class );
    Mockito.doReturn( creatorId ).when( fileMetadata ).get( "contentCreator" );

    Mockito.doReturn( fileMetadata ).when( fileService.repository ).getFileMetadata( fileId );

    Mockito.doReturn( "/path/to/file/file1.ext" ).when( fileService ).idToPath( pathId );

    RepositoryFileDto repositoryFileDto = Mockito.mock( RepositoryFileDto.class );
    Mockito.doReturn( fileId ).when( repositoryFileDto ).getId();

    Mockito.doReturn( null ).when( fileService.defaultUnifiedRepositoryWebService ).getFile( Mockito.anyString() );

    RepositoryFileDto repositoryFileDto1 = Mockito.mock( RepositoryFileDto.class );

    Mockito.doReturn( repositoryFileDto1 ).when( fileService.defaultUnifiedRepositoryWebService ).getFileById( creatorId );

    try {
      fileService.doGetContentCreator( pathId );
      Assert.fail();
    } catch ( FileNotFoundException e ) {
      //Should catch the exception
    }
  }

  @Test
  public void testDoGetGeneratedContent() {
    String pathId = "test.prpt",
      user = "admin",
      userFolder = "public/admin";

    RepositoryFileDto fileDetailsMock = Mockito.mock( RepositoryFileDto.class );
    RepositoryFile workspaceFolder = Mockito.mock( RepositoryFile.class );
    Mockito.doReturn( userFolder ).when( workspaceFolder ).getId();
    SessionResource sessionResource = Mockito.mock( SessionResource.class );

    List<RepositoryFile> children = new ArrayList<RepositoryFile>();
    RepositoryFile mockedChild = Mockito.mock( RepositoryFile.class );
    Mockito.doReturn( false ).when( mockedChild ).isFolder();
    children.add( mockedChild );

    Map<String, Serializable> mockedFileMetadata = Mockito.mock( Map.class );
    Mockito.doReturn( pathId ).when( mockedFileMetadata ).get( PentahoJcrConstants.PHO_CONTENTCREATOR );
    Mockito.when( fileService.repository.getFileMetadata( mockedChild.getId() ) ).thenReturn( mockedFileMetadata );

    Mockito.doReturn( pathId ).when( fileDetailsMock ).getId();
    Mockito.doReturn( userFolder ).when( sessionResource ).doGetCurrentUserDir();
    Mockito.doReturn( workspaceFolder ).when( fileService.repository ).getFile( userFolder );
    Mockito.doReturn( sessionResource ).when( fileService ).getSessionResource();
    Mockito.doReturn( children ).when( fileService.repository ).getChildren( userFolder );

    RepositoryFileDto mockedRepositoryFileDto = Mockito.mock( RepositoryFileDto.class );
    Mockito.doReturn( mockedRepositoryFileDto ).when( fileService ).toFileDto( mockedChild, null, false );

    try {
      Mockito.doReturn( fileDetailsMock ).when( fileService ).doGetProperties( pathId );
      List<RepositoryFileDto> list = fileService.doGetGeneratedContent( pathId );
      Assert.assertEquals( list.size(), 1 );
    } catch ( FileNotFoundException e ) {
      e.printStackTrace();
      Assert.fail();
    } catch ( Throwable t ) {
      Assert.fail();
    }
  }

  @Test
  public void testDoGetGeneratedContentFileNotFound() {
    String pathId = "test.prpt",
      userFolder = "public/admin";

    SessionResource sessionResource = Mockito.mock( SessionResource.class );

    Mockito.doReturn( userFolder ).when( sessionResource ).doGetCurrentUserDir();
    Mockito.doReturn( sessionResource ).when( fileService ).getSessionResource();

    try {
      Mockito.doReturn( null ).when( fileService ).doGetProperties( pathId );
      fileService.doGetGeneratedContent( pathId );
      Assert.fail();
    } catch ( FileNotFoundException e ) {
    }
  }

  @Test
  public void testDoGetGeneratedContentForUser() {
    String pathId = "test.prpt",
      user = "admin",
      userFolder = "public/admin";

    RepositoryFileDto fileDetailsMock = Mockito.mock( RepositoryFileDto.class );
    RepositoryFile workspaceFolder = Mockito.mock( RepositoryFile.class );
    Mockito.doReturn( userFolder ).when( workspaceFolder ).getId();
    SessionResource sessionResource = Mockito.mock( SessionResource.class );

    List<RepositoryFile> children = new ArrayList<RepositoryFile>();
    RepositoryFile mockedChild = Mockito.mock( RepositoryFile.class );
    Mockito.doReturn( false ).when( mockedChild ).isFolder();
    children.add( mockedChild );

    Map<String, Serializable> mockedFileMetadata = Mockito.mock( Map.class );
    Mockito.doReturn( pathId ).when( mockedFileMetadata ).get( PentahoJcrConstants.PHO_CONTENTCREATOR );
    Mockito.when( fileService.repository.getFileMetadata( mockedChild.getId() ) ).thenReturn( mockedFileMetadata );

    Mockito.doReturn( pathId ).when( fileDetailsMock ).getId();
    Mockito.doReturn( userFolder ).when( sessionResource ).doGetUserDir( user );
    Mockito.doReturn( workspaceFolder ).when( fileService.repository ).getFile( userFolder );
    Mockito.doReturn( sessionResource ).when( fileService ).getSessionResource();
    Mockito.doReturn( children ).when( fileService.repository ).getChildren( userFolder );

    RepositoryFileDto mockedRepositoryFileDto = Mockito.mock( RepositoryFileDto.class );
    Mockito.doReturn( mockedRepositoryFileDto ).when( fileService ).toFileDto( mockedChild, null, false );

    try {
      Mockito.doReturn( fileDetailsMock ).when( fileService ).doGetProperties( pathId );
      List<RepositoryFileDto> list = fileService.doGetGeneratedContent( pathId, user );
      Assert.assertEquals( list.size(), 1 );
    } catch ( FileNotFoundException e ) {
      e.printStackTrace();
      Assert.fail();
    } catch ( Throwable t ) {
      Assert.fail();
    }
  }

  @Test
  public void testDoGetGeneratedContentForUserFileNotFound() {
    String pathId = "test.prpt",
      user = "admin",
      userFolder = "public/admin";

    SessionResource sessionResource = Mockito.mock( SessionResource.class );

    Mockito.doReturn( userFolder ).when( sessionResource ).doGetUserDir( user );
    Mockito.doReturn( sessionResource ).when( fileService ).getSessionResource();

    try {
      Mockito.doReturn( null ).when( fileService ).doGetProperties( pathId );
      fileService.doGetGeneratedContent( pathId, user );
      Assert.fail();
    } catch ( FileNotFoundException e ) {
    }
  }

  @Test
  public void testSearchGeneratedContent() {
    String lineageId = "test.prpt",
      pathId = "test.prpt",
      userFolder = "public/admin";

    RepositoryFileDto fileDetailsMock = Mockito.mock( RepositoryFileDto.class );
    RepositoryFile workspaceFolder = Mockito.mock( RepositoryFile.class );
    Mockito.doReturn( userFolder ).when( workspaceFolder ).getId();
    SessionResource sessionResource = Mockito.mock( SessionResource.class );

    List<RepositoryFile> children = new ArrayList<RepositoryFile>();
    RepositoryFile mockedChild = Mockito.mock( RepositoryFile.class );
    Mockito.doReturn( false ).when( mockedChild ).isFolder();
    children.add( mockedChild );

    Map<String, Serializable> mockedFileMetadata = Mockito.mock( Map.class );
    Mockito.doReturn( lineageId ).when( mockedFileMetadata ).get( QuartzScheduler.RESERVEDMAPKEY_LINEAGE_ID );
    Mockito.when( fileService.repository.getFileMetadata( mockedChild.getId() ) ).thenReturn( mockedFileMetadata );

    Mockito.doReturn( pathId ).when( fileDetailsMock ).getId();
    Mockito.doReturn( userFolder ).when( sessionResource ).doGetCurrentUserDir();
    Mockito.doReturn( workspaceFolder ).when( fileService.repository ).getFile( userFolder );
    Mockito.doReturn( sessionResource ).when( fileService ).getSessionResource();
    Mockito.doReturn( children ).when( fileService.repository ).getChildren( userFolder );

    RepositoryFileDto mockedRepositoryFileDto = Mockito.mock( RepositoryFileDto.class );
    Mockito.doReturn( mockedRepositoryFileDto ).when( fileService ).toFileDto( mockedChild, null, false );

    try {
      Mockito.doReturn( fileDetailsMock ).when( fileService ).doGetProperties( pathId );
      List<RepositoryFileDto> list =
        fileService.searchGeneratedContent( userFolder, lineageId, QuartzScheduler.RESERVEDMAPKEY_LINEAGE_ID );
      Assert.assertEquals( list.size(), 1 );
    } catch ( FileNotFoundException e ) {
      e.printStackTrace();
      Assert.fail();
    } catch ( Throwable t ) {
      Assert.fail();
    }
  }

  @Test
  public void testSearchGeneratedContentFileNotFound() {
    String lineageId = "test.prpt",
      pathId = "test.prpt",
      userFolder = "public/admin";

    SessionResource sessionResource = Mockito.mock( SessionResource.class );

    Mockito.doReturn( userFolder ).when( sessionResource ).doGetCurrentUserDir();
    Mockito.doReturn( sessionResource ).when( fileService ).getSessionResource();

    try {
      Mockito.doReturn( null ).when( fileService ).doGetProperties( pathId );
      fileService.searchGeneratedContent( userFolder, lineageId, QuartzScheduler.RESERVEDMAPKEY_LINEAGE_ID );
      Assert.fail();
    } catch ( FileNotFoundException e ) {
    }
  }

  @Test
  public void doGetDeletedFiles() {
    RepositoryFileDto repositoryFileDto = Mockito.mock( RepositoryFileDto.class );
    RepositoryFileDto repositoryFileDto1 = Mockito.mock( RepositoryFileDto.class );

    List<RepositoryFileDto> fileDtos = new ArrayList<RepositoryFileDto>();
    fileDtos.add( repositoryFileDto );
    fileDtos.add( repositoryFileDto1 );

    //Test 1
    Mockito.doReturn( fileDtos ).when( fileService.defaultUnifiedRepositoryWebService ).getDeletedFiles();

    List<RepositoryFileDto> repositoryFiles = fileService.doGetDeletedFiles();
    Assert.assertEquals( 2, repositoryFiles.size() );

    //Test 2
    Mockito.doReturn( null ).when( fileService.defaultUnifiedRepositoryWebService ).getDeletedFiles();

    repositoryFiles = fileService.doGetDeletedFiles();
    Assert.assertEquals( null, repositoryFiles );

    Mockito.verify( fileService.defaultUnifiedRepositoryWebService, Mockito.times( 2 ) ).getDeletedFiles();
  }

  @Test
  public void doGetMetadata() {
    String pathId = "path:to:file:file1.ext";

    List<StringKeyStringValueDto> stringKeyStringValueDtos = new ArrayList<StringKeyStringValueDto>();
    StringKeyStringValueDto stringKeyStringValueDto1 = Mockito.mock( StringKeyStringValueDto.class );
    Mockito.doReturn( "key1" ).when( stringKeyStringValueDto1 ).getKey();
    Mockito.doReturn( "value1" ).when( stringKeyStringValueDto1 ).getValue();

    StringKeyStringValueDto stringKeyStringValueDto2 = Mockito.mock( StringKeyStringValueDto.class );
    Mockito.doReturn( "key2" ).when( stringKeyStringValueDto2 ).getKey();
    Mockito.doReturn( "value2" ).when( stringKeyStringValueDto2 ).getValue();

    stringKeyStringValueDtos.add( stringKeyStringValueDto1 );
    stringKeyStringValueDtos.add( stringKeyStringValueDto2 );

    Mockito.doReturn( "/path/to/file/file1.ext" ).when( fileService ).idToPath( pathId );

    RepositoryFileDto repositoryFileDto = Mockito.mock( RepositoryFileDto.class );
    Mockito.doReturn( repositoryFileDto ).when( fileService.defaultUnifiedRepositoryWebService ).getFile( Mockito.anyString() );
    Mockito.doReturn( true ).when( repositoryFileDto ).isHidden();

    Mockito.doReturn( stringKeyStringValueDtos ).when( fileService.defaultUnifiedRepositoryWebService )
      .getFileMetadata( Mockito.anyString() );

    // Test 1
    try {
      List<StringKeyStringValueDto> list = fileService.doGetMetadata( pathId );
      Assert.assertEquals( 4, list.size() );
      Boolean hasIsHidden = false;
      Boolean hasScheduable = false;
      for ( StringKeyStringValueDto item : list ) {
        if ( item.getKey().equals( "_PERM_HIDDEN" ) ) {
          hasIsHidden = true;
        }
        if ( item.getKey().equals( RepositoryFile.SCHEDULABLE_KEY ) ) {
          hasScheduable = true;
        }
      }
      Assert.assertTrue( hasIsHidden );
      Assert.assertTrue( hasScheduable );
    } catch ( FileNotFoundException e ) {
      Assert.fail();
    }

    stringKeyStringValueDtos = new ArrayList<StringKeyStringValueDto>();
    stringKeyStringValueDtos.add( stringKeyStringValueDto1 );
    stringKeyStringValueDtos.add( stringKeyStringValueDto2 );

    StringKeyStringValueDto stringKeyStringValueDto3 = Mockito.mock( StringKeyStringValueDto.class );
    Mockito.doReturn( RepositoryFile.SCHEDULABLE_KEY ).when( stringKeyStringValueDto3 ).getKey();
    Mockito.doReturn( "value3" ).when( stringKeyStringValueDto3 ).getValue();

    stringKeyStringValueDtos.add( stringKeyStringValueDto3 );

    Mockito.doReturn( stringKeyStringValueDtos ).when( fileService.defaultUnifiedRepositoryWebService )
      .getFileMetadata( Mockito.anyString() );

    // Test 2
    try {
      List<StringKeyStringValueDto> list = fileService.doGetMetadata( pathId );
      Assert.assertEquals( 4, list.size() );
      Boolean hasIsHidden = false;
      Boolean hasScheduable = false;
      for ( StringKeyStringValueDto item : list ) {
        if ( item.getKey().equals( "_PERM_HIDDEN" ) ) {
          hasIsHidden = true;
        }
        if ( item.getKey().equals( RepositoryFile.SCHEDULABLE_KEY ) ) {
          hasScheduable = true;
        }
      }
      Assert.assertTrue( hasIsHidden );
      Assert.assertTrue( hasScheduable );
    } catch ( FileNotFoundException e ) {
      Assert.fail();
    }

    Mockito.doReturn( null ).when( fileService.defaultUnifiedRepositoryWebService ).getFileMetadata( Mockito.anyString() );

    // Test 3
    try {
      List<StringKeyStringValueDto> list = fileService.doGetMetadata( null );
      Assert.assertEquals( null, list );
    } catch ( FileNotFoundException e ) {
      Assert.fail();
    }

    Mockito.verify( fileService, Mockito.times( 2 ) ).idToPath( pathId );
    Mockito.verify( fileService.defaultUnifiedRepositoryWebService, Mockito.times( 3 ) ).getFile( Mockito.anyString() );
    Mockito.verify( fileService.defaultUnifiedRepositoryWebService, Mockito.times( 3 ) ).getFileMetadata( Mockito.anyString() );
  }

  @Test
  public void doGetMetadataException() {
    String pathId = "path:to:file:file1.ext";

    Mockito.doReturn( null ).when( fileService.defaultUnifiedRepositoryWebService ).getFile( Mockito.anyString() );

    try {
      List<StringKeyStringValueDto> list = fileService.doGetMetadata( pathId );
      Assert.fail();
    } catch ( FileNotFoundException e ) {
      // Should catch exception
    }

    Mockito.verify( fileService.defaultUnifiedRepositoryWebService ).getFile( Mockito.anyString() );
  }

  @Test
  public void testDoGetChildren() {
    RepositoryFileDto mockRepositoryFileDto = Mockito.mock( RepositoryFileDto.class );
    Collator mockCollator = Mockito.mock( Collator.class );
    List<RepositoryFileDto> mockRepositoryFileDtos = new ArrayList<RepositoryFileDto>();
    mockRepositoryFileDtos.add( mockRepositoryFileDto );
    RepositoryRequest mockRepositoryRequest = Mockito.mock( RepositoryRequest.class );

    Mockito.doReturn( true ).when( fileService ).isPathValid( Mockito.anyString() );
    Mockito.doReturn( mockRepositoryFileDto ).when( fileService.defaultUnifiedRepositoryWebService ).getFile( Mockito.anyString() );
    Mockito.doReturn( mockCollator ).when( fileService ).getCollator( Mockito.anyInt() );
    Mockito.doReturn( mockRepositoryRequest ).when( fileService )
      .getRepositoryRequest( (RepositoryFileDto) Mockito.anyObject(), Mockito.anyBoolean(), Mockito.anyString(), Mockito.anyBoolean() );
    Mockito.doReturn( mockRepositoryFileDtos ).when( fileService.defaultUnifiedRepositoryWebService )
      .getChildrenFromRequest( mockRepositoryRequest );
    Mockito.doReturn( true ).when( fileService ).isShowingTitle( mockRepositoryRequest );

    List<RepositoryFileDto> repositoryFileDtos = fileService.doGetChildren( "mock:path:fileName", null, true, true );

    Mockito.verify( fileService, Mockito.times( 1 ) ).isPathValid( Mockito.anyString() );
    Mockito.verify( fileService.defaultUnifiedRepositoryWebService, Mockito.times( 1 ) ).getFile( Mockito.anyString() );
    Mockito.verify( fileService, Mockito.times( 1 ) ).getCollator( Mockito.anyInt() );
    Mockito.verify( fileService, Mockito.times( 1 ) )
      .getRepositoryRequest( (RepositoryFileDto) Mockito.anyObject(), Mockito.anyBoolean(), Mockito.anyString(), Mockito.anyBoolean() );
    Mockito.verify( fileService.defaultUnifiedRepositoryWebService, Mockito.times( 1 ) )
      .getChildrenFromRequest( mockRepositoryRequest );
    Mockito.verify( fileService, Mockito.times( 1 ) ).isShowingTitle( mockRepositoryRequest );

    Assert.assertEquals( mockRepositoryFileDtos, repositoryFileDtos );
    Assert.assertEquals( 1, repositoryFileDtos.size() );
    Assert.assertEquals( mockRepositoryFileDto, repositoryFileDtos.get( 0 ) );
  }

  @Test
  public void doSetMetadata() {
    String pathId = "path:to:file:file1.ext";

    List<StringKeyStringValueDto> stringKeyStringValueDtos = new ArrayList<StringKeyStringValueDto>();
    StringKeyStringValueDto stringKeyStringValueDto1 = Mockito.mock( StringKeyStringValueDto.class );
    Mockito.doReturn( "key1" ).when( stringKeyStringValueDto1 ).getKey();
    Mockito.doReturn( "value1" ).when( stringKeyStringValueDto1 ).getValue();

    StringKeyStringValueDto stringKeyStringValueDto2 = Mockito.mock( StringKeyStringValueDto.class );
    Mockito.doReturn( "key2" ).when( stringKeyStringValueDto2 ).getKey();
    Mockito.doReturn( "value2" ).when( stringKeyStringValueDto2 ).getValue();

    stringKeyStringValueDtos.add( stringKeyStringValueDto1 );
    stringKeyStringValueDtos.add( stringKeyStringValueDto2 );

    Mockito.doReturn( "/path/to/file/file1.ext" ).when( fileService ).idToPath( pathId );

    Mockito.doReturn( true ).when( fileService.policy ).isAllowed( RepositoryReadAction.NAME );
    Mockito.doReturn( true ).when( fileService.policy ).isAllowed( RepositoryCreateAction.NAME );
    Mockito.doReturn( true ).when( fileService.policy ).isAllowed( AdministerSecurityAction.NAME );

    RepositoryFileDto file = Mockito.mock( RepositoryFileDto.class );
    Mockito.doReturn( false ).when( file ).isFolder();
    Mockito.doReturn( true ).when( file ).isHidden();

    Mockito.doReturn( file ).when( fileService.defaultUnifiedRepositoryWebService ).getFile( Mockito.anyString() );

    List<RepositoryFileAclAceDto> repositoryFileAclAceDtos = new ArrayList<RepositoryFileAclAceDto>();

    RepositoryFileAclDto repositoryFileAclDto = Mockito.mock( RepositoryFileAclDto.class );
    Mockito.doReturn( "sessionName" ).when( repositoryFileAclDto ).getOwner();
    Mockito.doReturn( true ).when( repositoryFileAclDto ).isEntriesInheriting();
    Mockito.doReturn( repositoryFileAclAceDtos ).when( repositoryFileAclDto ).getAces();

    Mockito.doReturn( repositoryFileAclDto ).when( fileService.defaultUnifiedRepositoryWebService ).getAcl( Mockito.anyString() );

    IPentahoSession pentahoSession = Mockito.mock( IPentahoSession.class );
    Mockito.doReturn( pentahoSession ).when( fileService ).getSession();
    Mockito.doReturn( "sessionName" ).when( pentahoSession ).getName();

    RepositoryFileAclAceDto repositoryFileAclAceDto = Mockito.mock( RepositoryFileAclAceDto.class );
    List<Integer> permissions = new ArrayList<Integer>();
    permissions.add( RepositoryFilePermission.ACL_MANAGEMENT.ordinal() );
    Mockito.doReturn( permissions ).when( repositoryFileAclAceDto ).getPermissions();
    Mockito.doReturn( "sessionName" ).when( repositoryFileAclAceDto ).getRecipient();

    repositoryFileAclAceDtos.add( repositoryFileAclAceDto );

    Mockito.doReturn( repositoryFileAclAceDtos ).when( fileService.defaultUnifiedRepositoryWebService )
      .getEffectiveAces( Mockito.anyString() );

    Map<String, Serializable> metadata = new HashMap<String, Serializable>();
    Mockito.doReturn( metadata ).when( fileService.repository ).getFileMetadata( Mockito.anyString() );

    RepositoryFile sourceFile = Mockito.mock( RepositoryFile.class );
    Mockito.doReturn( sourceFile ).when( fileService.repository ).getFileById( Mockito.anyString() );

    RepositoryFileDto destFileDto = Mockito.mock( RepositoryFileDto.class );
    Mockito.doReturn( destFileDto ).when( fileService ).toFileDto( sourceFile, null, false );

    RepositoryFile destFile = Mockito.mock( RepositoryFile.class );
    Mockito.doReturn( destFile ).when( fileService ).toFile( destFileDto );

    RepositoryFileAcl acl = Mockito.mock( RepositoryFileAcl.class );
    Mockito.doReturn( acl ).when( fileService.repository ).getAcl( acl );

    // Test 1 - canManage should be true at start
    try {
      fileService.doSetMetadata( pathId, stringKeyStringValueDtos );
    } catch ( GeneralSecurityException e ) {
      Assert.fail();
    }

    // Test 2 - canManage should be false at start
    Mockito.doReturn( false ).when( fileService.policy ).isAllowed( RepositoryReadAction.NAME );
    Mockito.doReturn( false ).when( fileService.policy ).isAllowed( RepositoryCreateAction.NAME );
    Mockito.doReturn( false ).when( fileService.policy ).isAllowed( AdministerSecurityAction.NAME );
    Mockito.doReturn( "sessionName1" ).when( repositoryFileAclDto ).getOwner();

    try {
      fileService.doSetMetadata( pathId, stringKeyStringValueDtos );
    } catch ( GeneralSecurityException e ) {
      Assert.fail();
    }

    // Test 3 - canManage should be false at start
    Mockito.doReturn( true ).when( fileService.policy ).isAllowed( RepositoryReadAction.NAME );
    Mockito.doReturn( false ).when( fileService.policy ).isAllowed( RepositoryCreateAction.NAME );
    Mockito.doReturn( false ).when( fileService.policy ).isAllowed( AdministerSecurityAction.NAME );
    Mockito.doReturn( "sessionName1" ).when( repositoryFileAclDto ).getOwner();

    try {
      fileService.doSetMetadata( pathId, stringKeyStringValueDtos );
    } catch ( GeneralSecurityException e ) {
      Assert.fail();
    }

    // Test 4 - canManage should be false at start
    Mockito.doReturn( false ).when( fileService.policy ).isAllowed( RepositoryReadAction.NAME );
    Mockito.doReturn( true ).when( fileService.policy ).isAllowed( RepositoryCreateAction.NAME );
    Mockito.doReturn( false ).when( fileService.policy ).isAllowed( AdministerSecurityAction.NAME );
    Mockito.doReturn( "sessionName1" ).when( repositoryFileAclDto ).getOwner();

    try {
      fileService.doSetMetadata( pathId, stringKeyStringValueDtos );
    } catch ( GeneralSecurityException e ) {
      Assert.fail();
    }

    // Test 5 - canManage should be false at start
    Mockito.doReturn( false ).when( fileService.policy ).isAllowed( RepositoryReadAction.NAME );
    Mockito.doReturn( false ).when( fileService.policy ).isAllowed( RepositoryCreateAction.NAME );
    Mockito.doReturn( true ).when( fileService.policy ).isAllowed( AdministerSecurityAction.NAME );
    Mockito.doReturn( "sessionName1" ).when( repositoryFileAclDto ).getOwner();

    try {
      fileService.doSetMetadata( pathId, stringKeyStringValueDtos );
    } catch ( GeneralSecurityException e ) {
      Assert.fail();
    }

    // Test 6 - canManage should be false at start
    Mockito.doReturn( true ).when( fileService.policy ).isAllowed( RepositoryReadAction.NAME );
    Mockito.doReturn( true ).when( fileService.policy ).isAllowed( RepositoryCreateAction.NAME );
    Mockito.doReturn( false ).when( fileService.policy ).isAllowed( AdministerSecurityAction.NAME );
    Mockito.doReturn( "sessionName1" ).when( repositoryFileAclDto ).getOwner();

    try {
      fileService.doSetMetadata( pathId, stringKeyStringValueDtos );
    } catch ( GeneralSecurityException e ) {
      Assert.fail();
    }

    // Test 7 - canManage should be false at start
    Mockito.doReturn( false ).when( fileService.policy ).isAllowed( RepositoryReadAction.NAME );
    Mockito.doReturn( true ).when( fileService.policy ).isAllowed( RepositoryCreateAction.NAME );
    Mockito.doReturn( true ).when( fileService.policy ).isAllowed( AdministerSecurityAction.NAME );
    Mockito.doReturn( "sessionName1" ).when( repositoryFileAclDto ).getOwner();

    try {
      fileService.doSetMetadata( pathId, stringKeyStringValueDtos );
    } catch ( GeneralSecurityException e ) {
      Assert.fail();
    }

    // Test 8 - canManage should be false at start
    Mockito.doReturn( true ).when( file ).isFolder();
    Mockito.doReturn( true ).when( file ).isHidden();

    try {
      fileService.doSetMetadata( pathId, stringKeyStringValueDtos );
    } catch ( GeneralSecurityException e ) {
      Assert.fail();
    }

    // Test 9
    StringKeyStringValueDto stringKeyStringValueDto3 = Mockito.mock( StringKeyStringValueDto.class );
    Mockito.doReturn( "_PERM_HIDDEN" ).when( stringKeyStringValueDto3 ).getKey();
    Mockito.doReturn( "true" ).when( stringKeyStringValueDto3 ).getValue();
    stringKeyStringValueDtos.add( stringKeyStringValueDto3 );

    try {
      fileService.doSetMetadata( pathId, stringKeyStringValueDtos );
    } catch ( GeneralSecurityException e ) {
      Assert.fail();
    }

    Mockito.verify( fileService.defaultUnifiedRepositoryWebService, Mockito.times( 9 ) ).getFile( Mockito.anyString() );
    Mockito.verify( fileService.defaultUnifiedRepositoryWebService, Mockito.times( 9 ) ).getAcl( Mockito.anyString() );
    Mockito.verify( repositoryFileAclDto, Mockito.times( 9 ) ).getOwner();
    Mockito.verify( fileService.policy, Mockito.times( 11 ) ).isAllowed( Mockito.anyString() );
    Mockito.verify( fileService.repository, Mockito.times( 9 ) ).getFileMetadata( Mockito.anyString() );
    Mockito.verify( fileService.repository, Mockito.times( 7 ) ).setFileMetadata( Mockito.anyString(), Mockito.any( Map.class ) );
    Mockito.verify( file, Mockito.times( 8 ) ).setHidden( Mockito.anyBoolean() );
    Mockito.verify( fileService.repository, Mockito.times( 8 ) ).getFileById( Mockito.anyString() );
    Mockito.verify( fileService, Mockito.times( 8 ) ).toFileDto( Mockito.any( RepositoryFile.class ), Mockito.anySet(), Mockito.anyBoolean() );
    Mockito.verify( fileService, Mockito.times( 8 ) ).toFile( Mockito.any( RepositoryFileDto.class ) );
    Mockito.verify( destFileDto, Mockito.times( 8 ) ).setHidden( Mockito.anyBoolean() );
    Mockito.verify( fileService.repository, Mockito.times( 8 ) ).getAcl( Mockito.anyString() );
    Mockito.verify( fileService.repository, Mockito.times( 7 ) )
      .updateFile( Mockito.any( RepositoryFile.class ), Mockito.any( IRepositoryFileData.class ),
        Mockito.anyString() );
    Mockito.verify( fileService.repository, Mockito.times( 7 ) ).updateAcl( Mockito.any( RepositoryFileAcl.class ) );
    Mockito.verify( fileService.repository ).updateFolder( Mockito.any( RepositoryFile.class ), Mockito.anyString() );
  }

  @Test
  public void doSetMetadataException() {

    String pathId = "path:to:file:file1.ext";

    List<StringKeyStringValueDto> stringKeyStringValueDtos = new ArrayList<StringKeyStringValueDto>();

    Mockito.doReturn( "/path/to/file/file1.ext" ).when( fileService ).idToPath( pathId );

    Mockito.doReturn( false ).when( fileService.policy ).isAllowed( RepositoryReadAction.NAME );
    Mockito.doReturn( false ).when( fileService.policy ).isAllowed( RepositoryCreateAction.NAME );
    Mockito.doReturn( false ).when( fileService.policy ).isAllowed( AdministerSecurityAction.NAME );

    RepositoryFileDto file = Mockito.mock( RepositoryFileDto.class );

    Mockito.doReturn( file ).when( fileService.defaultUnifiedRepositoryWebService ).getFile( Mockito.anyString() );

    RepositoryFileAclDto repositoryFileAclDto = Mockito.mock( RepositoryFileAclDto.class );
    Mockito.doReturn( "sessionName" ).when( repositoryFileAclDto ).getOwner();

    Mockito.doReturn( repositoryFileAclDto ).when( fileService.defaultUnifiedRepositoryWebService ).getAcl( Mockito.anyString() );

    IPentahoSession pentahoSession = Mockito.mock( IPentahoSession.class );
    Mockito.doReturn( pentahoSession ).when( fileService ).getSession();
    Mockito.doReturn( "sessionName1" ).when( pentahoSession ).getName();

    try {
      fileService.doSetMetadata( pathId, stringKeyStringValueDtos );
      Assert.fail();
    } catch ( GeneralSecurityException e ) {
      //Should catch the exception
    }

    Mockito.verify( fileService.defaultUnifiedRepositoryWebService ).getFile( Mockito.anyString() );
    Mockito.verify( fileService.defaultUnifiedRepositoryWebService ).getAcl( Mockito.anyString() );
    Mockito.verify( repositoryFileAclDto ).getOwner();
    Mockito.verify( fileService.policy ).isAllowed( Mockito.anyString() );
  }

  @Test
  public void testDoGetFileAcl() {
    RepositoryFileDto file = Mockito.mock( RepositoryFileDto.class );

    RepositoryFileAclDto fileAcl = Mockito.mock( RepositoryFileAclDto.class );
    Mockito.when( fileAcl.isEntriesInheriting() ).thenReturn( false );

    Mockito.when( fileService.defaultUnifiedRepositoryWebService.getFile( Mockito.anyString() ) ).thenReturn( file );
    Mockito.when( fileService.defaultUnifiedRepositoryWebService.getAcl( Mockito.anyString() ) ).thenReturn( fileAcl );

    Mockito.doNothing().when( fileService ).addAdminRole( fileAcl );

    String pathId = "/usr/dir/file.txt";
    fileService.doGetFileAcl( pathId );

    Mockito.verify( fileService ).addAdminRole( fileAcl );
  }

  public void testDoGetTree() {
    String pathId = ":path:to:file:file1.ext";
    int depth = 1;
    String filter = "*|FOLDERS";
    boolean showHidden = true;
    boolean includeAcls = true;

    // Test 1
    Mockito.doReturn( "test" ).when( fileService ).idToPath( Mockito.anyString() );

    RepositoryRequest mockRequest = Mockito.mock( RepositoryRequest.class );
    Mockito.doReturn( mockRequest ).when( fileService )
      .getRepositoryRequest( Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyInt(), Mockito.anyString() );

    RepositoryFileDto mockChildFile = Mockito.mock( RepositoryFileDto.class );
    Mockito.doReturn( "test" ).when( mockChildFile ).getId();

    RepositoryFileTreeDto mockChildDto = Mockito.mock( RepositoryFileTreeDto.class );
    Mockito.doReturn( mockChildFile ).when( mockChildDto ).getFile();

    List<RepositoryFileTreeDto> mockChildrenDto = new ArrayList<RepositoryFileTreeDto>();
    mockChildrenDto.add( mockChildDto );

    RepositoryFileTreeDto mockTreeDto = Mockito.mock( RepositoryFileTreeDto.class );
    Mockito.doReturn( mockChildrenDto ).when( mockTreeDto ).getChildren();
    Mockito.doReturn( mockTreeDto ).when( fileService.defaultUnifiedRepositoryWebService ).getTreeFromRequest( mockRequest );

    Mockito.doReturn( true ).when( fileService ).isShowingTitle( mockRequest );

    Collator mockCollator = Mockito.mock( Collator.class );
    Mockito.doReturn( mockCollator ).when( fileService ).getCollatorInstance();
    Mockito.doNothing().when( fileService ).sortByLocaleTitle( mockCollator, mockTreeDto );

    Map<String, Serializable> fileMeta = new HashMap<String, Serializable>();
    fileMeta.put( IUnifiedRepository.SYSTEM_FOLDER, new Boolean( false ) );

    Mockito.doReturn( fileMeta ).when( fileService.repository ).getFileMetadata( Mockito.anyString() );

    fileService.doGetTree( pathId, depth, filter, showHidden, includeAcls );

    Mockito.verify( fileService, Mockito.times( 1 ) ).idToPath( Mockito.anyString() );
    Mockito.verify( mockRequest, Mockito.times( 1 ) ).setIncludeAcls( Mockito.anyBoolean() );
    Mockito.verify( mockCollator, Mockito.times( 1 ) ).setStrength( Collator.PRIMARY );
    Mockito.verify( fileService, Mockito.times( 1 ) ).sortByLocaleTitle( mockCollator, mockTreeDto );
    Mockito.verify( mockTreeDto ).setChildren( mockChildrenDto );

    // Test 2 - path id is null
    pathId = null;
    fileService.doGetTree( pathId, depth, filter, showHidden, includeAcls );

    Mockito.verify( fileService, Mockito.times( 1 ) )
      .getRepositoryRequest( Mockito.eq( FileUtils.PATH_SEPARATOR ), Mockito.anyBoolean(), Mockito.anyInt(), Mockito.anyString() );

    // Test 3 - path id is set to the file utils path separator
    pathId = FileUtils.PATH_SEPARATOR;
    fileService.doGetTree( pathId, depth, filter, showHidden, includeAcls );

    Mockito.verify( fileService, Mockito.times( 2 ) )
      .getRepositoryRequest( Mockito.eq( FileUtils.PATH_SEPARATOR ), Mockito.anyBoolean(), Mockito.anyInt(), Mockito.anyString() );
  }

  @Test
  public void testDoRename() throws Exception {
    RepositoryFile repositoryFile = Mockito.mock( RepositoryFile.class );
    Mockito.when( repositoryFile.getPath() ).thenReturn( "/dir/file.txt" );
    Mockito.when( repositoryFile.getName() ).thenReturn( "file.txt" );

    Mockito.when( fileService.repository.getFile( Mockito.anyString() ) ).thenReturn( repositoryFile );
    Mockito.when( fileService.repository.getFileById( Mockito.anyString() ) ).thenReturn( repositoryFile );
    String pathId = ":dir:file.txt";
    String newName = "file1.txt";

    boolean success = fileService.doRename( pathId, newName );
    Assert.assertTrue( success );
  }

  @Test
  public void testDoRenameNegative() throws Exception {
    RepositoryFile repositoryFile = Mockito.mock( RepositoryFile.class );
    Mockito.when( repositoryFile.getPath() ).thenReturn( "/dir/file.txt" );
    Mockito.when( repositoryFile.getName() ).thenReturn( "file.txt" );

    Mockito.when( fileService.repository.getFile( Mockito.anyString() ) ).thenReturn( repositoryFile );
    String pathId = ":dir:file.txt";
    String newName = "file1.txt";

    boolean success = fileService.doRename( pathId, newName );
    Assert.assertFalse( success );
  }

  @Test
  public void testDoCreateDirs() throws Exception {
    String pathId = "path:to:file:file1.ext";

    Mockito.doReturn( "/path/to/file/file1.ext" ).when( fileService ).idToPath( pathId );

    RepositoryFileDto parentDir = Mockito.mock( RepositoryFileDto.class );
    Mockito.doReturn( "" ).when( parentDir ).getPath();
    Mockito.doReturn( FileUtils.PATH_SEPARATOR ).when( parentDir ).getId();
    Mockito.when( fileService.getRepoWs().getFile( FileUtils.PATH_SEPARATOR ) ).thenReturn( parentDir );

    Mockito.when( fileService.getRepoWs().getFile( "/path" ) ).thenReturn( null );
    Mockito.when( fileService.getRepoWs().getFile( "/to" ) ).thenReturn( null );
    Mockito.when( fileService.getRepoWs().getFile( "/file" ) ).thenReturn( null );
    Mockito.when( fileService.getRepoWs().getFile( "/file1.ext" ) ).thenReturn( null );

    RepositoryFileDto filePath = Mockito.mock( RepositoryFileDto.class );
    Mockito.doReturn( "/path" ).when( filePath ).getPath();
    Mockito.doReturn( "/path" ).when( filePath ).getId();
    RepositoryFileDto fileTo = Mockito.mock( RepositoryFileDto.class );
    Mockito.doReturn( "/path/to" ).when( fileTo ).getPath();
    Mockito.doReturn( "/path/to" ).when( fileTo ).getId();
    RepositoryFileDto fileFile = Mockito.mock( RepositoryFileDto.class );
    Mockito.doReturn( "/path/to/file" ).when( fileFile ).getPath();
    Mockito.doReturn( "/path/to/file" ).when( fileFile ).getId();
    RepositoryFileDto fileFileExt = Mockito.mock( RepositoryFileDto.class );
    Mockito.doReturn( "/path/to/file/file1" ).when( fileFileExt ).getPath();
    Mockito.doReturn( "/path/to/file/file1" ).when( fileFileExt ).getId();

    Mockito.when( fileService.getRepoWs().createFolder( Mockito.eq( "/" ), Mockito.any( RepositoryFileDto.class ), Mockito.eq( "/path" ) ) ).thenReturn(
      filePath );
    Mockito.when( fileService.getRepoWs().createFolder( Mockito.eq( "/path" ), Mockito.any( RepositoryFileDto.class ), Mockito.eq( "/path/to" ) ) )
      .thenReturn( fileTo );
    Mockito.when(
      fileService.getRepoWs().createFolder( Mockito.eq( "/path/to" ), Mockito.any( RepositoryFileDto.class ), Mockito.eq( "/path/to/file" ) ) )
      .thenReturn( fileFile );
    Mockito.when(
      fileService.getRepoWs()
        .createFolder( Mockito.eq( "/path/to/file" ), Mockito.any( RepositoryFileDto.class ), Mockito.eq( "/path/to/file/file1.ext" ) ) )
      .thenReturn( fileFileExt );

    Assert.assertTrue( fileService.doCreateDir( pathId ) );

    Mockito.verify( fileService.getRepoWs(), Mockito.times( 4 ) )
      .createFolder( Mockito.anyString(), Mockito.any( RepositoryFileDto.class ), Mockito.anyString() );
  }

  @Test
  public void testDoCreateDirsNegative() throws Exception {
    String pathId = "path:to:file:file1.ext";

    Mockito.doReturn( "/path/to/file/file1.ext" ).when( fileService ).idToPath( pathId );

    RepositoryFileDto parentDir = Mockito.mock( RepositoryFileDto.class );
    Mockito.doReturn( "" ).when( parentDir ).getPath();
    Mockito.doReturn( FileUtils.PATH_SEPARATOR ).when( parentDir ).getId();
    Mockito.when( fileService.getRepoWs().getFile( FileUtils.PATH_SEPARATOR ) ).thenReturn( parentDir );

    RepositoryFileDto filePath = Mockito.mock( RepositoryFileDto.class );
    Mockito.doReturn( "/path" ).when( filePath ).getPath();
    Mockito.doReturn( "/path" ).when( filePath ).getId();
    RepositoryFileDto fileTo = Mockito.mock( RepositoryFileDto.class );
    Mockito.doReturn( "/path/to" ).when( fileTo ).getPath();
    Mockito.doReturn( "/path/to" ).when( fileTo ).getId();
    RepositoryFileDto fileFile = Mockito.mock( RepositoryFileDto.class );
    Mockito.doReturn( "/path/to/file" ).when( fileFile ).getPath();
    Mockito.doReturn( "/path/to/file" ).when( fileFile ).getId();
    RepositoryFileDto fileFileExt = Mockito.mock( RepositoryFileDto.class );
    Mockito.doReturn( "/path/to/file/file1" ).when( fileFileExt ).getPath();
    Mockito.doReturn( "/path/to/file/file1" ).when( fileFileExt ).getId();

    Mockito.when( fileService.getRepoWs().getFile( "/path" ) ).thenReturn( filePath );
    Mockito.when( fileService.getRepoWs().getFile( "/path/to" ) ).thenReturn( fileTo );
    Mockito.when( fileService.getRepoWs().getFile( "/path/to/file" ) ).thenReturn( fileFile );
    Mockito.when( fileService.getRepoWs().getFile( "/path/to/file/file1.ext" ) ).thenReturn( fileFileExt );

    Assert.assertFalse( fileService.doCreateDir( pathId ) );

    Mockito.verify( fileService.getRepoWs(), Mockito.times( 0 ) )
      .createFolder( Mockito.anyString(), Mockito.any( RepositoryFileDto.class ), Mockito.anyString() );


    Mockito.when( fileService.getRepoWs().getFile( "/path" ) ).thenReturn( null );
    Mockito.when( fileService.getRepoWs().createFolder( Mockito.eq( "/" ), Mockito.any( RepositoryFileDto.class ), Mockito.eq( "/path" ) ) ).
      thenThrow( new InternalError( "negativetest" ) );

    try {
      fileService.doCreateDir( pathId );
    } catch ( InternalError e ) {
      Assert.assertEquals( e.getMessage(), "negativetest" );
    }
  }
}
