package org.pentaho.platform.web.http.api.resources.services;

import org.apache.commons.lang.StringUtils;
import org.junit.After;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.Assert;
import org.pentaho.platform.repository2.unified.fileio.RepositoryFileOutputStream;
import org.pentaho.platform.repository2.unified.webservices.DefaultUnifiedRepositoryWebService;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileDto;

import javax.servlet.http.HttpServletRequest;

import java.io.InputStream;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class FileServiceTest {

  private static FileService fileService;

@Before
  public void setUp(){
    fileService = new FileService();
    fileService.defaultUnifiedRepositoryWebService =
            mock( DefaultUnifiedRepositoryWebService.class );
  }

  @After
  public void cleanup() {
    fileService = null;
  }

  @Test
  public void testDoDeleteFiles() throws Exception {
    String params = "file1,file2";

    fileService.doDeleteFiles( params );

    verify ( fileService.getDefaultUnifiedRepositoryWebService(), times(1)).deleteFile( "file1", null );
    verify ( fileService.getDefaultUnifiedRepositoryWebService(), times(1)).deleteFile( "file2", null );
  }

  @Test
  public void testDoDeleteFilesException() {
    String params = "file1,file2";
    doThrow(new RuntimeException()).when(
      fileService.getDefaultUnifiedRepositoryWebService() ).deleteFile( anyString(), anyString() );

    try{
      fileService.doDeleteFiles( params );
      fail(); //This line should never be reached
    } catch ( RuntimeException e ) {
      //Expected exception
    } catch ( Exception e ) {
      fail();
    }
  }

  //todo: refactor test to mock the call to copy in FileService before the test starts
  @Ignore
  public void testDoCreateFile() throws Exception {
    RepositoryFileOutputStream mockOutputStream = mock(RepositoryFileOutputStream.class);
    fileService.mockRepositoryFileOutputStream = mockOutputStream;

    HttpServletRequest mockRequest = mock(HttpServletRequest.class);
    InputStream mockInputStream = mock(InputStream.class);

    FileService mockFileServiceUtils = mock( FileService.class );

    fileService.createFile(mockRequest, "testString", mockInputStream);

    verify(mockOutputStream, times(1)).setCharsetName(anyString());
    verify(mockOutputStream, times(1)).close();
    verify(mockInputStream, times(1)).close();
  }

  //todo: refactor test to mock the call to copy in FileService before the test starts
  @Ignore
  public void testDoCreateFileException() {
    RepositoryFileOutputStream mockOutputStream = mock(RepositoryFileOutputStream.class);
    doThrow(new RuntimeException()).when(mockOutputStream).setCharsetName(anyString());
    fileService.mockRepositoryFileOutputStream = mockOutputStream;

    FileService mockFileServiceUtils = mock( FileService.class );
    fileService = mockFileServiceUtils;

    try {
      fileService.createFile(null, null, null);
      fail();
    } catch (RuntimeException e) {
      // expected
    } catch (Exception e) {
      fail();
    }
  }

  @Test
  public void testDoDeleteFilesPermanent() throws Exception {
    String params = "file1,file2";

    fileService.doDeleteFilesPermanent( params );

    verify ( fileService.getRepoWs(), times(1)).deleteFileWithPermanentFlag( "file1", true, null );
    verify ( fileService.getRepoWs(), times(1)).deleteFileWithPermanentFlag( "file2", true, null );
  }

  @Test
  public void testDoDeleteFilesPermanentException() {
    String params = "file1,file2";
    doThrow (new RuntimeException() ).when(
        fileService.getRepoWs()).deleteFileWithPermanentFlag( anyString(), eq(true), anyString() );

    try{
      fileService.doDeleteFilesPermanent( params );
      assert( false ); //This line should never be reached
    } catch ( Exception e ) {
      //Expected exception
    }
  }

  @Test
  public void testDoMoveFiles() throws Exception {
    String destPathId = "/test";
    String[] params = {"file1","file2"};

    RepositoryFileDto repositoryFileDto = mock( RepositoryFileDto.class );
    doReturn( destPathId ).when( repositoryFileDto ).getPath();

    doReturn( repositoryFileDto ).when( fileService.getRepoWs() ).getFile( destPathId );

    Assert.assertTrue( fileService.doMoveFiles( destPathId, StringUtils.join( params, "," ) ) );

    verify ( fileService.getRepoWs(), times(1)).moveFile( params[0], destPathId, null );
    verify ( fileService.getRepoWs(), times(1)).moveFile( params[1], destPathId, null );
  }

  @Test
  public void testDoMoveFilesForUnknownDestPath() throws Exception {
    String destPathId = "/test";
    String[] params = {"file1","file2"};

    doReturn( null ).when( fileService.getRepoWs() ).getFile( destPathId );

    Assert.assertFalse( fileService.doMoveFiles( destPathId, StringUtils.join( params, "," ) ) );
    verify ( fileService.getRepoWs(), times(0)).moveFile( params[0], destPathId, null );
    verify ( fileService.getRepoWs(), times(0)).moveFile( params[1], destPathId, null );
  }

  @Test
  public void testDoMoveFilesException() throws Exception {
    String destPathId = "/test";
    String[] params = {"file1","file2"};

    RepositoryFileDto repositoryFileDto = mock( RepositoryFileDto.class );
    doReturn( destPathId ).when( repositoryFileDto ).getPath();

    doReturn( repositoryFileDto ).when( fileService.getRepoWs() ).getFile( destPathId );
    doThrow( new RuntimeException() ).when( fileService.getRepoWs() ).moveFile( params[0], destPathId, null );

    try{
      fileService.doMoveFiles( destPathId, StringUtils.join( params, "," ) );
      assert( false ); //This line should never be reached
    } catch ( Exception e ) {
      verify ( fileService.getRepoWs(), times(1)).moveFile( params[0], destPathId, null );
      verify ( fileService.getRepoWs(), times(0)).moveFile( params[1], destPathId, null );
    }
  }

  @Test
  public void testDoRestoreFiles() throws Exception {
    String[] params = {"file1","file2"};

    fileService.doRestoreFiles( StringUtils.join( params, "," ) );

    verify ( fileService.getRepoWs(), times(1)).undeleteFile( params[0], null );
    verify ( fileService.getRepoWs(), times(1)).undeleteFile( params[1], null );
  }

  @Test
  public void testDoRestoreFilesException() throws Exception {
    String[] params = {"file1","file2"};

    doThrow( new RuntimeException() ).when( fileService.getRepoWs() ).undeleteFile( params[ 0 ], null );

    try {
      fileService.doRestoreFiles( StringUtils.join( params, "," ) );
      assert( false ); //This line should never be reached
    } catch ( Exception e ) {
      verify ( fileService.getRepoWs(), times(1)).undeleteFile( params[0], null );
      verify ( fileService.getRepoWs(), times(0)).undeleteFile( params[1], null );
    }
  }
}
