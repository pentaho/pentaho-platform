package org.pentaho.platform.web.http.api.resources;

import com.sun.jersey.core.header.FormDataContentDisposition;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.plugin.services.importer.PlatformImportException;
import org.pentaho.platform.web.http.api.resources.services.RepositoryPublishService;

import javax.ws.rs.core.Response;
import java.io.InputStream;

import static javax.ws.rs.core.Response.Status.PRECONDITION_FAILED;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class RepositoryPublishResourceTest {

  RepositoryPublishResource repositoryPublishResource;

  @Before
  public void setUp() {
    repositoryPublishResource = spy( new RepositoryPublishResource() );
    repositoryPublishResource.repositoryPublishService = mock( RepositoryPublishService.class );
  }

  @After
  public void tearDown() {
    repositoryPublishResource = null;
  }

  @Test
  public void testWriteFile() throws Exception {
    String pathId = "pathId";
    InputStream fileContents = mock( InputStream.class );
    Boolean overwriteFile = Boolean.TRUE;
    FormDataContentDisposition mockFormDataContentDisposition = mock( FormDataContentDisposition.class );

    doNothing().when( repositoryPublishResource.repositoryPublishService )
      .writeFile( pathId, fileContents, overwriteFile );

    Response mockResponse = mock( Response.class );

    String okResponseText = "SUCCESS";
    doReturn( mockResponse ).when( repositoryPublishResource ).buildPlainTextOkResponse( okResponseText );

    Response testResponse =
      repositoryPublishResource.writeFile( pathId, fileContents, overwriteFile, mockFormDataContentDisposition );
    assertEquals( mockResponse, testResponse );

    verify( repositoryPublishResource.repositoryPublishService, times( 1 ) )
      .writeFile( pathId, fileContents, overwriteFile );
    verify( repositoryPublishResource, times( 1 ) ).buildPlainTextOkResponse( okResponseText );
  }

  @Test
  public void testWriteFileError() throws Exception {
    String pathId = "pathId";
    InputStream fileContents = mock( InputStream.class );
    Boolean overwriteFile = Boolean.TRUE;
    FormDataContentDisposition mockFormDataContentDisposition = mock( FormDataContentDisposition.class );

    Response mockUnauthorizedResponse = mock( Response.class );
    doReturn( mockUnauthorizedResponse ).when( repositoryPublishResource )
      .buildStatusResponse( UNAUTHORIZED, PlatformImportException.PUBLISH_USERNAME_PASSWORD_FAIL );

    int errorStatus = 0;

    Response mockPreconditionFailedResponse = mock( Response.class );
    doReturn( mockPreconditionFailedResponse ).when( repositoryPublishResource )
      .buildStatusResponse( PRECONDITION_FAILED, errorStatus );

    Response mockServerErrorResponse = mock( Response.class );
    doReturn( mockServerErrorResponse ).when( repositoryPublishResource )
      .buildServerErrorResponse( PlatformImportException.PUBLISH_GENERAL_ERROR );

    // Test 1
    Exception mockPentahoAccessControlException = mock( PentahoAccessControlException.class );
    doThrow( mockPentahoAccessControlException ).when( repositoryPublishResource.repositoryPublishService )
      .writeFile( pathId, fileContents, overwriteFile );

    Response testResponse =
      repositoryPublishResource.writeFile( pathId, fileContents, overwriteFile, mockFormDataContentDisposition );
    assertEquals( mockUnauthorizedResponse, testResponse );

    // Test 2
    PlatformImportException mockPlatformImportException = mock( PlatformImportException.class );
    doReturn( errorStatus ).when( mockPlatformImportException ).getErrorStatus();
    doThrow( mockPlatformImportException ).when( repositoryPublishResource.repositoryPublishService )
      .writeFile( pathId, fileContents, overwriteFile );

    testResponse =
      repositoryPublishResource.writeFile( pathId, fileContents, overwriteFile, mockFormDataContentDisposition );
    assertEquals( mockPreconditionFailedResponse, testResponse );

    // Test 3
    Exception mockException = mock( RuntimeException.class );
    doThrow( mockException ).when( repositoryPublishResource.repositoryPublishService )
      .writeFile( pathId, fileContents, overwriteFile );

    testResponse =
      repositoryPublishResource.writeFile( pathId, fileContents, overwriteFile, mockFormDataContentDisposition );
    assertEquals( mockServerErrorResponse, testResponse );

    verify( repositoryPublishResource.repositoryPublishService, times( 3 ) )
      .writeFile( pathId, fileContents, overwriteFile );
    verify( repositoryPublishResource, times( 1 ) ).buildStatusResponse( UNAUTHORIZED,
      PlatformImportException.PUBLISH_USERNAME_PASSWORD_FAIL );
    verify( repositoryPublishResource, times( 1 ) ).buildStatusResponse( PRECONDITION_FAILED, errorStatus );
    verify( repositoryPublishResource, times( 1 ) )
      .buildServerErrorResponse( PlatformImportException.PUBLISH_GENERAL_ERROR );
  }
}
