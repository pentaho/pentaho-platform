package org.pentaho.platform.web.http.api.resources.services;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.repository2.unified.webservices.DefaultUnifiedRepositoryWebService;

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
  public void testGetRepoWs() throws Exception {
    assert( fileService.getRepoWs() != null );
  }
}