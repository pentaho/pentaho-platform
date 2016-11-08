/*
 * Copyright 2002 - 2013 Pentaho Corporation.  All rights reserved.
 *
 * This software was developed by Pentaho Corporation and is provided under the terms
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. TThe Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */

package org.pentaho.platform.web.http.api.resources;

import com.sun.jersey.core.header.FormDataContentDisposition;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.plugin.services.importer.PlatformImportException;
import org.pentaho.platform.web.http.api.resources.services.RepositoryPublishService;

import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;

import static javax.ws.rs.core.Response.Status.*;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import static org.pentaho.platform.plugin.services.importer.PlatformImportException.PUBLISH_GENERAL_ERROR;
import static org.pentaho.platform.plugin.services.importer.PlatformImportException.PUBLISH_USERNAME_PASSWORD_FAIL;

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
    InputStream fileContents = emptyStream();
    FormDataContentDisposition mockFormDataContentDisposition = mock( FormDataContentDisposition.class );

    doNothing().when( repositoryPublishResource.repositoryPublishService )
      .writeFile( pathId, fileContents, true );

    String okResponseText = "SUCCESS";

    Response testResponse =
      repositoryPublishResource.writeFile( pathId, fileContents, true, mockFormDataContentDisposition );
    assertResponse( testResponse, OK, okResponseText );

    verify( repositoryPublishResource.repositoryPublishService, times( 1 ) )
      .writeFile( pathId, fileContents, true );
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
      .buildServerErrorResponse( PUBLISH_GENERAL_ERROR );

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
      .buildServerErrorResponse( PUBLISH_GENERAL_ERROR );
  }


  @Test
  public void writeFileWithEncodedName_Returns200_OnSuccess() throws Exception {
    final String originalFile = "my-ktr.ktr";
    final String originalPath = "/public/" + originalFile;

    doReturn( false ).when( repositoryPublishResource ).invalidPath( originalPath );

    Response shouldBeOk = repositoryPublishResource
      .writeFileWithEncodedName( encode( originalPath ), emptyStream(), true, dummyInfo( originalFile ) );

    assertResponse( shouldBeOk, OK, "SUCCESS" );
    verify( repositoryPublishResource.repositoryPublishService, times( 1 ) )
      .publishFile( eq( originalPath ), any( InputStream.class ), eq( true ) );
  }

  @Test
  public void writeFileWithEncodedName_Returns422_OnPathWithReservedChars() throws Exception {
    final String originalFile = "my\nktr.ktr";
    final String originalPath = "/public/" + originalFile;

    doReturn( true ).when( repositoryPublishResource ).invalidPath( originalPath );

    Response shouldBeUnprocessableEntity = repositoryPublishResource
      .writeFileWithEncodedName( encode( originalPath ), emptyStream(), true, dummyInfo( originalFile ) );

    assertResponse( shouldBeUnprocessableEntity, 422,
      "Cannot publish [" + originalPath + "] because it contains reserved character(s)" );
    verify( repositoryPublishResource.repositoryPublishService, never() )
      .writeFile( eq( originalPath ), any( InputStream.class ), eq( true ) );
  }

  @Test
  public void writeFileWithEncodedName_Returns500_OnUnexpectedError() throws Exception {
    testWriteFileWithEncodedName_OnError(
      new RuntimeException( "unexpected" ),
      INTERNAL_SERVER_ERROR, PUBLISH_GENERAL_ERROR );
  }

  @Test
  public void writeFileWithEncodedName_Returns401_OnUnauthorizedAccess() throws Exception {
    testWriteFileWithEncodedName_OnError(
      new PentahoAccessControlException( "unauthorized" ),
      UNAUTHORIZED, PUBLISH_USERNAME_PASSWORD_FAIL );
  }

  @Test
  public void writeFileWithEncodedName_Returns412_OnPlatformImportException() throws Exception {
    final int someCode = 50;
    testWriteFileWithEncodedName_OnError(
      new PlatformImportException( "import exception", someCode ),
      PRECONDITION_FAILED, someCode );
  }

  private void testWriteFileWithEncodedName_OnError( Exception thrownException, Response.Status expectedStatus,
                                                     int expectedErrorCode ) throws Exception {
    final String originalFile = "my-ktr.ktr";
    final String originalPath = "/public/" + originalFile;

    doReturn( false ).when( repositoryPublishResource ).invalidPath( originalPath );
    doThrow( thrownException ).when( repositoryPublishResource.repositoryPublishService )
      .publishFile( eq( originalPath ), any( InputStream.class ), eq( true ) );

    Response response = repositoryPublishResource
      .writeFileWithEncodedName( encode( originalPath ), emptyStream(), true, dummyInfo( originalFile ) );
    assertResponse( response, expectedStatus, String.valueOf( expectedErrorCode ) );
  }

  @Test
  public void writeFileWithEncodedName_DecodesPathAndName() throws Exception {
    final String originalFile = "my-\"quoted\".ktr";
    final String originalPath = "/public/" + originalFile;

    doReturn( false ).when( repositoryPublishResource ).invalidPath( originalPath );

    repositoryPublishResource.writeFileWithEncodedName(
      encode( originalPath ), emptyStream(), true, dummyInfo( originalFile ) );

    // decodes path
    verify( repositoryPublishResource.repositoryPublishService, times( 1 ) )
      .publishFile( eq( originalPath ), any( InputStream.class ), eq( true ) );
  }


  private static String encode( String originalPath ) throws UnsupportedEncodingException {
    return URLEncoder.encode( originalPath, "UTF-8" );
  }

  private static InputStream emptyStream() {
    return new ByteArrayInputStream( new byte[ 0 ] );
  }

  private static FormDataContentDisposition dummyInfo( String fileName ) throws Exception {
    return FormDataContentDisposition
      .name( "name" ).size( 1 ).fileName( encode( fileName ) )
      .creationDate( new Date( 0 ) )
      .modificationDate( new Date( 1 ) )
      .readDate( new Date( 2 ) )
      .build();
  }


  private static void assertResponse( Response actual, Response.Status expectedStatus, Object expectedEntity ) {
    assertResponse( actual, expectedStatus.getStatusCode(), expectedEntity );
  }

  private static void assertResponse( Response actual, int expectedCode, Object expectedEntity ) {
    assertNotNull( "Response should not be null", actual );
    assertEquals( expectedCode, actual.getStatus() );
    assertEquals( expectedEntity, actual.getEntity() );
  }
}
