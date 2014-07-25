package org.pentaho.platform.web.http.api.resources.services;

import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;
import org.pentaho.platform.repository2.unified.webservices.DefaultUnifiedRepositoryWebService;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileDto;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class FileServiceTest {

  private static FileService fileService;

 @Before
  public void setUp(){
    fileService = new FileService();
    fileService.repoWs = mock( DefaultUnifiedRepositoryWebService.class );
  }

  @After
  public void cleanup() {
    fileService.repoWs = null;
  }

  @Test
  public void testDoDeleteFiles() throws Exception {
    String params = "file1,file2";

    fileService.doDeleteFiles( params );

    verify ( fileService.repoWs, times(1)).deleteFile( "file1", null );
    verify ( fileService.repoWs, times(1)).deleteFile( "file2", null );
  }

  @Test
  public void testDoDeleteFilesException() {
    String params = "file1,file2";
    doThrow (new RuntimeException() ).when(
      fileService.repoWs).deleteFile( anyString(), anyString() );

    try{
      fileService.doDeleteFiles( params );
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

    doReturn( repositoryFileDto ).when( fileService.repoWs ).getFile( destPathId );

    Assert.assertTrue( fileService.doMoveFiles( destPathId, StringUtils.join( params, "," ) ) );

    verify ( fileService.repoWs, times(1)).moveFile( params[0], destPathId, null );
    verify ( fileService.repoWs, times(1)).moveFile( params[1], destPathId, null );
  }

  @Test
  public void testDoMoveFilesForUnknownDestPath() throws Exception {
    String destPathId = "/test";
    String[] params = {"file1","file2"};

    doReturn( null ).when( fileService.repoWs ).getFile( destPathId );

    Assert.assertFalse( fileService.doMoveFiles( destPathId, StringUtils.join( params, "," ) ) );
    verify ( fileService.repoWs, times(0)).moveFile( params[0], destPathId, null );
    verify ( fileService.repoWs, times(0)).moveFile( params[1], destPathId, null );
  }

  @Test
  public void testDoMoveFilesException() throws Exception {
    String destPathId = "/test";
    String[] params = {"file1","file2"};

    RepositoryFileDto repositoryFileDto = mock( RepositoryFileDto.class );
    doReturn( destPathId ).when( repositoryFileDto ).getPath();

    doReturn( repositoryFileDto ).when( fileService.repoWs ).getFile( destPathId );
    doThrow (new RuntimeException() ).when( fileService.repoWs ).moveFile( params[0], destPathId, null );

    try{
      fileService.doMoveFiles( destPathId, StringUtils.join( params, "," ) );
      assert( false ); //This line should never be reached
    } catch ( Exception e ) {
      verify ( fileService.repoWs, times(1)).moveFile( params[0], destPathId, null );
      verify ( fileService.repoWs, times(0)).moveFile( params[1], destPathId, null );
    }
  }

  @Test
  public void testGetRepoWs() throws Exception {
    assert( fileService.getRepoWs() != null );
  }
}