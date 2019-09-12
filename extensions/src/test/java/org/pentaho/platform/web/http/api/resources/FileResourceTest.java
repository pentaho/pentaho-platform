/*!
 *
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
 *
 * Copyright (c) 2002-2019 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.web.http.api.resources;

import org.dom4j.Document;
import org.dom4j.Element;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IContentGenerator;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoUser;
import org.pentaho.platform.api.engine.security.userroledao.IUserRoleDao;
import org.pentaho.platform.api.repository2.unified.IRepositoryContentConverterHandler;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.UnifiedRepositoryAccessDeniedException;
import org.pentaho.platform.core.mt.Tenant;
import org.pentaho.platform.engine.core.output.SimpleOutputHandler;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.TenantUtils;
import org.pentaho.platform.plugin.services.importexport.Exporter;
import org.pentaho.platform.plugin.services.importexport.StreamConverter;
import org.pentaho.platform.repository2.unified.webservices.DefaultUnifiedRepositoryWebService;
import org.pentaho.platform.api.repository2.unified.webservices.LocaleMapDto;
import org.pentaho.platform.api.repository2.unified.webservices.RepositoryFileAclDto;
import org.pentaho.platform.api.repository2.unified.webservices.RepositoryFileDto;
import org.pentaho.platform.api.repository2.unified.webservices.RepositoryFileTreeDto;
import org.pentaho.platform.api.repository2.unified.webservices.StringKeyStringValueDto;
import org.pentaho.platform.security.policy.rolebased.actions.PublishAction;
import org.pentaho.platform.web.http.api.resources.services.FileService;
import org.pentaho.platform.web.http.api.resources.utils.FileUtils;
import org.pentaho.platform.web.http.messages.Messages;
import org.pentaho.test.mock.MockPentahoUser;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.IllegalSelectorException;
import java.security.GeneralSecurityException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.OK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith( PowerMockRunner.class )
@PrepareForTest( TenantUtils.class )
public class FileResourceTest {
  private static final String ACL_OWNER = "ACL Owner";
  private static final String XML_EXTENSION = "xml";
  private static final String PATH_ID = "pathId.xml";
  private static final String PATH_ID_WITHOUTH_EXTENSION = "pathId";
  private static final String PATH_ID_INCORRECT_EXTENSION = "pathId.wrong";
  private static final String NAME_NEW_FILE = "nameNewFile.xml";
  private static final String NAME_NEW_FILE_WITHOUTH_EXTENSION = "nameNewFile";
  private static final String NAME_NEW_FILE_WRONG_EXTENSION = "nameNewFile.wrong";

  private static final String FILE_ID = "444324fd54ghad";
  private FileResource fileResource;

  @Before
  public void setUp() {
    mockStatic( TenantUtils.class );

    fileResource = spy( new FileResource() );
    fileResource.fileService = mock( FileService.class );
    fileResource.httpServletRequest = mock( HttpServletRequest.class );
    fileResource.policy = mock( IAuthorizationPolicy.class );
    fileResource.repository = mock( IUnifiedRepository.class );
    fileResource.repoWs = mock( DefaultUnifiedRepositoryWebService.class );
  }

  @After
  public void tearDown() {
    fileResource = null;
  }

  @BeforeClass
  public static void initTest() {
    IRepositoryContentConverterHandler handler = mock( IRepositoryContentConverterHandler.class );
    when( handler.getConverter( XML_EXTENSION ) ).thenReturn( new StreamConverter() );
    PentahoSystem.registerObject( handler );
  }

  @Test
  public void doDeleteFiles() throws Exception {
    String params = "params";

    doNothing().when( fileResource.fileService ).doDeleteFiles( eq( params ) );

    Response mockResponse = mock( Response.class );
    doReturn( mockResponse ).when( fileResource ).buildOkResponse();

    Response testResponse = fileResource.doDeleteFiles( params );

    verify( fileResource, times( 1 ) ).buildOkResponse();
    verify( fileResource.fileService, times( 1 ) ).doDeleteFiles( params );
    assertEquals( testResponse, mockResponse );
  }

  @Test
  public void testDoDeleteFilesError() throws Exception {
    Throwable mockException = mock( RuntimeException.class );
    String params = "params";

    doThrow( mockException ).when( fileResource.fileService ).doDeleteFiles( params );

    Response mockResponse = mock( Response.class );
    doReturn( mockResponse ).when( fileResource ).buildServerErrorResponse( mockException );

    Response testResponse = fileResource.doDeleteFiles( params );

    assertEquals( mockResponse, testResponse );
    verify( fileResource.fileService, times( 1 ) ).doDeleteFiles( params );
  }

  @Test
  public void testDoDeleteFilesPermanent() throws Exception {
    String params = "params";

    doNothing().when( fileResource.fileService ).doDeleteFilesPermanent( params );

    Response mockResponse = mock( Response.class );
    doReturn( mockResponse ).when( fileResource ).buildOkResponse();

    Response testResponse = fileResource.doDeleteFilesPermanent( params );

    assertEquals( mockResponse, testResponse );
    verify( fileResource, times( 1 ) ).buildOkResponse();
    verify( fileResource.fileService, times( 1 ) ).doDeleteFilesPermanent( params );
  }

  @Test
  public void testDoDeleteFilesPermanentError() throws Exception {
    Throwable mockException = mock( RuntimeException.class );
    String params = "params";

    doThrow( mockException ).when( fileResource.fileService ).doDeleteFilesPermanent( params );

    Response mockResponse = mock( Response.class );
    doReturn( mockResponse ).when( fileResource ).buildServerErrorResponse( mockException );

    Response testResponse = fileResource.doDeleteFilesPermanent( params );

    assertEquals( mockResponse, testResponse );
    verify( fileResource.fileService, times( 1 ) ).doDeleteFilesPermanent( params );
    verify( mockException, times( 1 ) ).printStackTrace();
  }

  @Test
  public void testDoMove() throws Exception {
    String params = "params";
    doNothing().when( fileResource.fileService ).doMoveFiles( PATH_ID, params );

    Response mockResponse = mock( Response.class );
    doReturn( mockResponse ).when( fileResource ).buildOkResponse();

    Response testResponse = fileResource.doMove( PATH_ID, params );

    assertEquals( mockResponse, testResponse );
    verify( fileResource.fileService, times( 1 ) ).doMoveFiles( PATH_ID, params );
    verify( fileResource, times( 1 ) ).buildOkResponse();
  }

  @Test
  public void testDoMoveError() throws Exception {
    // Test 1
    String params = "params";

    doThrow( mock( FileNotFoundException.class ) ).when( fileResource.fileService ).doMoveFiles( PATH_ID, params );

    Response mockNotFoundResponse = mock( Response.class );
    doReturn( mockNotFoundResponse ).when( fileResource ).buildStatusResponse( Response.Status.NOT_FOUND );

    Response mockInternalServerErrorResponse = mock( Response.class );
    doReturn( mockInternalServerErrorResponse ).when( fileResource )
      .buildStatusResponse( INTERNAL_SERVER_ERROR );

    Response testResponse = fileResource.doMove( PATH_ID, params );

    assertEquals( mockNotFoundResponse, testResponse );

    // Test 2
    doThrow( mock( RuntimeException.class ) ).when( fileResource.fileService ).doMoveFiles( PATH_ID, params );

    testResponse = fileResource.doMove( PATH_ID, params );

    assertEquals( mockInternalServerErrorResponse, testResponse );
  }

  @Test
  public void testDoRestore() throws Exception {
    String params = "params";

    doNothing().when( fileResource.fileService ).doRestoreFiles( eq( params ) );

    Response mockResponse = mock( Response.class );
    doReturn( mockResponse ).when( fileResource ).buildOkResponse();

    Response testResponse = fileResource.doRestore( params, null );

    verify( fileResource, times( 1 ) ).buildOkResponse();
    verify( fileResource.fileService, times( 1 ) ).doRestoreFiles( params );
    assertEquals( testResponse, mockResponse );
  }

  @Test
  public void testDoRestoreError() {
    String params = "params";

    doThrow( mock( InternalError.class ) ).when( fileResource.fileService ).doRestoreFiles( params );

    Response mockInternalErrorResponse = mock( Response.class );
    doReturn( mockInternalErrorResponse ).when( fileResource ).buildStatusResponse( INTERNAL_SERVER_ERROR );

    Response testResponse = fileResource.doRestore( params, null );

    assertEquals( mockInternalErrorResponse, testResponse );
    verify( fileResource.fileService, times( 1 ) ).doRestoreFiles( params );
    verify( fileResource ).buildStatusResponse( INTERNAL_SERVER_ERROR );
  }

  @Test
  public void testNoServerErrorCodeReturnedWhenUserHasNoWritePermissionsToFolder() {
    UnifiedRepositoryAccessDeniedException mockedException = mock( UnifiedRepositoryAccessDeniedException.class );
    doThrow( mockedException ).when( fileResource.fileService ).doRestoreFiles( FILE_ID );
    doReturn( "user/home" ).when( fileResource ).getUserHomeFolder();
    doReturn( false ).when( fileResource.fileService ).canRestoreToFolderWithNoConflicts( anyString(), eq( FILE_ID ) );

    Response response = fileResource.doRestore( FILE_ID, null );
    assertNotEquals( response.getStatus(), INTERNAL_SERVER_ERROR.getStatusCode() );
  }


  @Test
  public void testCreateFile() throws Exception {
    String charsetName = "charsetName";
    InputStream mockInputStream = mock( InputStream.class );

    doNothing().when( fileResource.fileService ).createFile( charsetName, PATH_ID, mockInputStream );

    Response mockResponse = mock( Response.class );
    doReturn( mockResponse ).when( fileResource ).buildOkResponse();

    doReturn( charsetName ).when( fileResource.httpServletRequest ).getCharacterEncoding();

    Response testResponse = fileResource.createFile( PATH_ID, mockInputStream );

    assertEquals( mockResponse, testResponse );
    verify( fileResource.fileService, times( 1 ) ).createFile( charsetName, PATH_ID, mockInputStream );
    verify( fileResource, times( 1 ) ).buildOkResponse();
  }

  @Test
  public void testCreateFileWithoutExtension() throws Exception {
    InputStream mockInputStream = mock( InputStream.class );
    Response testResponse = fileResource.createFile( PATH_ID_WITHOUTH_EXTENSION, mockInputStream );
    assertEquals( INTERNAL_SERVER_ERROR.getStatusCode(), testResponse.getStatus() );

    verify( fileResource, times( 1 ) ).buildServerErrorResponse( anyString() );
  }

  @Test
  public void testCreateFileIncorrectExtension() throws Exception {
    InputStream mockInputStream = mock( InputStream.class );
    Response testResponse = fileResource.createFile( PATH_ID_INCORRECT_EXTENSION, mockInputStream );
    assertEquals( INTERNAL_SERVER_ERROR.getStatusCode(), testResponse.getStatus() );

    verify( fileResource, times( 1 ) ).buildServerErrorResponse( anyString() );
  }

  @Test
  public void testCreateFileCorrectExtension() throws Exception {
    InputStream mockInputStream = mock( InputStream.class );
    Response testResponse = fileResource.createFile( PATH_ID, mockInputStream );
    assertEquals( OK.getStatusCode(), testResponse.getStatus() );

    verify( fileResource, times( 1 ) ).buildOkResponse();
  }

  @Test
  public void testCreateFileError() throws Exception {
    String charsetName = "charsetName";
    InputStream mockInputStream = mock( InputStream.class );

    Exception mockException = mock( RuntimeException.class );

    doThrow( mockException ).when( fileResource.fileService )
      .createFile( charsetName, PATH_ID, mockInputStream );

    doReturn( charsetName ).when( fileResource.httpServletRequest ).getCharacterEncoding();

    Response mockResponse = mock( Response.class );
    doReturn( mockResponse ).when( fileResource ).buildServerErrorResponse( mockException );

    Response testResponse = fileResource.createFile( PATH_ID, mockInputStream );

    assertEquals( mockResponse, testResponse );
    verify( fileResource, times( 1 ) ).buildServerErrorResponse( mockException );
    verify( fileResource.httpServletRequest, times( 1 ) ).getCharacterEncoding();
    verify( fileResource.fileService ).createFile( charsetName, PATH_ID, mockInputStream );
  }

  @Test
  public void testDoCopyFiles() {
    Integer mode = 1;
    String params = "params";

    doNothing().when( fileResource.fileService ).doCopyFiles( PATH_ID, mode, params );

    Response mockResponse = mock( Response.class );
    doReturn( mockResponse ).when( fileResource ).buildOkResponse();

    Response testResponse = fileResource.doCopyFiles( PATH_ID, mode, params );

    assertEquals( mockResponse, testResponse );
    verify( fileResource.fileService, times( 1 ) ).doCopyFiles( PATH_ID, mode, params );
  }

  @Test
  public void testDoCopyFilesError() {
    Integer mode = 1;
    String params = "params";

    Exception mockException = mock( RuntimeException.class );
    doThrow( mockException ).when( fileResource.fileService ).doCopyFiles( PATH_ID, mode, params );

    Response mockResponse = mock( Response.class );
    doReturn( mockResponse ).when( fileResource ).buildSafeHtmlServerErrorResponse( mockException );

    Response testResponse = fileResource.doCopyFiles( PATH_ID, mode, params );

    assertEquals( mockResponse, testResponse );
    verify( fileResource, times( 1 ) ).buildSafeHtmlServerErrorResponse( mockException );
    verify( fileResource.fileService ).doCopyFiles( PATH_ID, mode, params );
  }

  @Test
  public void testCopyReturnsForbiddenOnIllegalArgument() {
    Integer mode = 1;
    String params = "params";

    Exception illegalArgument = new IllegalArgumentException();
    doThrow( illegalArgument ).when( fileResource.fileService ).doCopyFiles( PATH_ID, mode, params );

    Response testResponse = fileResource.doCopyFiles( PATH_ID, mode, params );

    assertEquals( FORBIDDEN.getStatusCode(), testResponse.getStatus() );
    verify( fileResource, times( 0 ) ).buildSafeHtmlServerErrorResponse( illegalArgument );
    verify( fileResource.fileService ).doCopyFiles( PATH_ID, mode, params );
  }

  @Test
  public void testDoGetFileOrDir() throws Exception {
    FileService.RepositoryFileToStreamWrapper mockWrapper = mock( FileService.RepositoryFileToStreamWrapper.class );
    doReturn( mockWrapper ).when( fileResource.fileService ).doGetFileOrDir( PATH_ID );

    Response mockResponse = mock( Response.class );
    doReturn( mockResponse ).when( fileResource ).buildOkResponse( mockWrapper );

    Response testResponse = fileResource.doGetFileOrDir( PATH_ID );

    assertEquals( mockResponse, testResponse );
    verify( fileResource.fileService, times( 1 ) ).doGetFileOrDir( PATH_ID );
    verify( fileResource, times( 1 ) ).buildOkResponse( mockWrapper );
  }

  @Test
  public void testDoGetFileOrDirError() throws Exception {
    Exception mockFileNotFoundException = mock( FileNotFoundException.class );
    doThrow( mockFileNotFoundException ).when( fileResource.fileService ).doGetFileOrDir( PATH_ID );

    Response mockNotFoundResponse = mock( Response.class );
    doReturn( mockNotFoundResponse ).when( fileResource ).buildStatusResponse( Response.Status.NOT_FOUND );

    Response testResponse = fileResource.doGetFileOrDir( PATH_ID );

    assertEquals( mockNotFoundResponse, testResponse );
    verify( fileResource, times( 1 ) ).buildStatusResponse( Response.Status.NOT_FOUND );
    verify( fileResource.fileService, times( 1 ) ).doGetFileOrDir( PATH_ID );

    // Test 2
    Exception mockIllegalArgumentException = mock( IllegalArgumentException.class );
    doThrow( mockIllegalArgumentException ).when( fileResource.fileService ).doGetFileOrDir( PATH_ID );

    Response mockForbiddenResponse = mock( Response.class );
    doReturn( mockForbiddenResponse ).when( fileResource ).buildStatusResponse( FORBIDDEN );

    testResponse = fileResource.doGetFileOrDir( PATH_ID );

    assertEquals( mockForbiddenResponse, testResponse );
    verify( fileResource, times( 1 ) ).buildStatusResponse( Response.Status.NOT_FOUND );
    verify( fileResource.fileService, times( 2 ) ).doGetFileOrDir( PATH_ID );
  }

  @Test
  public void testDoGetDirAsZipWithPathId() {
    String path = "path";
    doReturn( path ).when( fileResource.fileService ).idToPath( PATH_ID );

    doReturn( true ).when( fileResource ).isPathValid( path );

    doReturn( true ).when( fileResource.policy ).isAllowed( PublishAction.NAME );

    RepositoryFile mockFile = mock( RepositoryFile.class );
    doReturn( mockFile ).when( fileResource.repository ).getFile( path );

    Response mockResponse = mock( Response.class );
    doReturn( mockResponse ).when( fileResource ).doGetDirAsZip( mockFile );

    Response testResponse = fileResource.doGetDirAsZip( PATH_ID );

    assertEquals( mockResponse, testResponse );
    verify( fileResource.repository, times( 1 ) ).getFile( path );
    verify( fileResource.policy, times( 1 ) ).isAllowed( PublishAction.NAME );
    verify( fileResource, times( 1 ) ).isPathValid( path );
    verify( fileResource.fileService, times( 1 ) ).idToPath( PATH_ID );
  }

  @Test
  public void testDoGetDirAsZipWithPathIdError() {
    String path = "path";

    doReturn( path ).when( fileResource.fileService ).idToPath( PATH_ID );

    doReturn( false ).when( fileResource ).isPathValid( path );

    Response mockForbiddenResponse = mock( Response.class );
    doReturn( mockForbiddenResponse ).when( fileResource ).buildStatusResponse( FORBIDDEN );

    Response testResponse = fileResource.doGetDirAsZip( PATH_ID );

    assertEquals( mockForbiddenResponse, testResponse );
    verify( fileResource, times( 1 ) ).buildStatusResponse( FORBIDDEN );
    verify( fileResource, times( 1 ) ).isPathValid( path );

    // Test 2
    doReturn( true ).when( fileResource ).isPathValid( path );
    doReturn( false ).when( fileResource.policy ).isAllowed( PublishAction.NAME );

    testResponse = fileResource.doGetDirAsZip( PATH_ID );

    assertEquals( mockForbiddenResponse, testResponse );
    verify( fileResource, times( 2 ) ).buildStatusResponse( FORBIDDEN );
    verify( fileResource, times( 2 ) ).isPathValid( path );
    verify( fileResource.policy, times( 1 ) ).isAllowed( PublishAction.NAME );

    // Test 3
    doReturn( true ).when( fileResource.policy ).isAllowed( PublishAction.NAME );
    doReturn( null ).when( fileResource.repository ).getFile( path );

    Response mockNotFoundResponse = mock( Response.class );
    doReturn( mockNotFoundResponse ).when( fileResource ).buildStatusResponse( Response.Status.NOT_FOUND );

    testResponse = fileResource.doGetDirAsZip( PATH_ID );

    assertEquals( mockNotFoundResponse, testResponse );
    verify( fileResource, times( 1 ) ).buildStatusResponse( Response.Status.NOT_FOUND );
    verify( fileResource, times( 3 ) ).isPathValid( path );
    verify( fileResource.policy, times( 2 ) ).isAllowed( PublishAction.NAME );
  }

  @Test
  public void testDoGetDirAsZipWithFile() throws Exception {
    RepositoryFile mockRepositoryFile = mock( RepositoryFile.class );

    String path = "path";
    doReturn( path ).when( mockRepositoryFile ).getPath();

    Exporter mockExporter = mock( Exporter.class );
    doReturn( mockExporter ).when( fileResource ).getExporter();

    File mockFile = mock( File.class );
    doReturn( mockFile ).when( mockExporter ).doExportAsZip( mockRepositoryFile );

    InputStream mockInputStream = mock( FileInputStream.class );
    doReturn( mockInputStream ).when( fileResource ).getFileInputStream( mockFile );

    StreamingOutput mockOutput = mock( StreamingOutput.class );
    doReturn( mockOutput ).when( fileResource ).getStreamingOutput( mockInputStream );

    Response mockResponse = mock( Response.class );
    doReturn( mockResponse ).when( fileResource ).buildOkResponse( mockOutput, FileResource.APPLICATION_ZIP );

    Response testResponse = fileResource.doGetDirAsZip( mockRepositoryFile );

    assertEquals( mockResponse, testResponse );
    verify( mockRepositoryFile, times( 1 ) ).getPath();
    verify( fileResource, times( 1 ) ).getExporter();
    verify( mockExporter, times( 1 ) ).setRepoPath( path );
    verify( mockExporter, times( 1 ) ).setRepoWs( fileResource.repoWs );
    verify( mockExporter, times( 1 ) ).doExportAsZip( mockRepositoryFile );
    verify( fileResource, times( 1 ) ).getFileInputStream( mockFile );
    verify( fileResource, times( 1 ) ).getStreamingOutput( mockInputStream );
    verify( fileResource, times( 1 ) ).buildOkResponse( mockOutput, FileResource.APPLICATION_ZIP );
  }

  @Test
  public void testDoGetDirAsZipWithFileError() {
    RepositoryFile mockRepositoryFile = mock( RepositoryFile.class );

    String path = "path";
    doReturn( path ).when( mockRepositoryFile ).getPath();

    Exception mockException = mock( RuntimeException.class );
    doThrow( mockException ).when( fileResource ).getExporter();

    String exceptionToString = "exception";
    doReturn( exceptionToString ).when( mockException ).toString();

    Response mockServerErrorResponse = mock( Response.class );
    doReturn( mockServerErrorResponse ).when( fileResource ).buildServerErrorResponse( exceptionToString );
  }

  @Test
  public void testDoIsParameterizable() throws Exception {


    String path = "path";
    doReturn( path ).when( fileResource.fileService ).idToPath( PATH_ID );

    RepositoryFile mockRepositoryFile = mock( RepositoryFile.class );
    doReturn( mockRepositoryFile ).when( fileResource.repository ).getFile( path );

    doReturn( true ).when( fileResource ).hasParameterUi( mockRepositoryFile );

    IContentGenerator mockContentGenerator = mock( IContentGenerator.class );
    doReturn( mockContentGenerator ).when( fileResource ).getContentGenerator( mockRepositoryFile );

    SimpleParameterProvider mockSimpleParameterProvider = mock( SimpleParameterProvider.class );
    doReturn( mockSimpleParameterProvider ).when( fileResource ).getSimpleParameterProvider();

    String repositoryPath = "repositoryPath";
    doReturn( repositoryPath ).when( mockRepositoryFile ).getPath();

    String encodedPath = "encodedPath";
    doReturn( encodedPath ).when( fileResource ).encode( repositoryPath );

    IPentahoSession mockPentahoSession = mock( IPentahoSession.class );
    doReturn( mockPentahoSession ).when( fileResource ).getSession();

    ByteArrayOutputStream mockByteArrayOutputStream = mock( ByteArrayOutputStream.class );
    doReturn( mockByteArrayOutputStream ).when( fileResource ).getByteArrayOutputStream();

    doReturn( 1 ).when( mockByteArrayOutputStream ).size();

    String outputStreamToString = "outputStreamToString";
    doReturn( outputStreamToString ).when( mockByteArrayOutputStream ).toString();

    Document mockDocument = mock( Document.class );
    doReturn( mockDocument ).when( fileResource ).parseText( outputStreamToString );

    String selectNodesParam = "parameters/parameter";
    List<Element> elements = new ArrayList<Element>();
    doReturn( elements ).when( mockDocument ).selectNodes( selectNodesParam );

    Element mockElement = mock( Element.class );
    doReturn( "output-target" ).when( mockElement ).attributeValue( "name" );
    doReturn( "true" ).when( mockElement ).attributeValue( "is-mandatory" );

    // Test 1
    String testString = fileResource.doIsParameterizable( PATH_ID );
    assertEquals( Boolean.FALSE.toString(), testString );

    // Test 2
    elements.add( mockElement );

    testString = fileResource.doIsParameterizable( PATH_ID );
    assertEquals( Boolean.TRUE.toString(), testString );

    // Test 3
    doReturn( "false" ).when( mockElement ).attributeValue( "is-mandatory" );

    testString = fileResource.doIsParameterizable( PATH_ID );
    assertEquals( Boolean.TRUE.toString(), testString );

    // Test 4
    Element mockAttribElement = mock( Element.class );
    doReturn( mockAttribElement ).when( mockElement ).selectSingleNode( "attribute[@namespace='http://reporting.pentaho"
      + ".org/namespaces/engine/parameter-attributes/core' and @name='role']" );

    testString = fileResource.doIsParameterizable( PATH_ID );
    assertEquals( Boolean.TRUE.toString(), testString );

    verify( fileResource.fileService, times( 4 ) ).idToPath( PATH_ID );
    verify( fileResource.repository, times( 4 ) ).getFile( path );
    verify( fileResource, times( 4 ) ).hasParameterUi( mockRepositoryFile );
    verify( fileResource, times( 4 ) ).getContentGenerator( mockRepositoryFile );
    verify( mockContentGenerator, times( 4 ) ).setOutputHandler( any( SimpleOutputHandler.class ) );
    verify( mockContentGenerator, times( 4 ) ).setMessagesList( anyList() );
    verify( fileResource, times( 4 ) ).getSimpleParameterProvider();
    verify( mockRepositoryFile, times( 4 ) ).getPath();
    verify( fileResource, times( 4 ) ).encode( repositoryPath );
    verify( mockSimpleParameterProvider, times( 4 ) ).setParameter( "path", encodedPath );
    verify( mockSimpleParameterProvider, times( 4 ) ).setParameter( "renderMode", "PARAMETER" );
    verify( mockContentGenerator, times( 4 ) ).setParameterProviders( anyMap() );
    verify( fileResource, times( 4 ) ).getSession();
    verify( mockContentGenerator, times( 4 ) ).setSession( mockPentahoSession );
    verify( mockContentGenerator, times( 4 ) ).createContent();
    verify( fileResource, times( 4 ) ).getByteArrayOutputStream();
    verify( mockDocument, times( 4 ) ).selectNodes( selectNodesParam );
    verify( mockElement, times( 3 ) ).attributeValue( "name" );
    verify( mockElement, times( 3 ) ).attributeValue( "is-mandatory" );
    verify( mockAttribElement, times( 1 ) ).attributeValue( "value" );
  }

  @Test
  public void testDoIsParameterizableError() throws Exception {


    String path = "path";
    doReturn( path ).when( fileResource.fileService ).idToPath( PATH_ID );

    RepositoryFile mockRepositoryFile = mock( RepositoryFile.class );
    doReturn( mockRepositoryFile ).when( fileResource.repository ).getFile( path );

    Exception mockNoSuchBeanDefinitionException = mock( NoSuchBeanDefinitionException.class );
    doThrow( mockNoSuchBeanDefinitionException ).when( fileResource ).hasParameterUi( mockRepositoryFile );

    String exceptionMessage = "exceptionMessage";
    doReturn( exceptionMessage ).when( mockNoSuchBeanDefinitionException ).getMessage();

    Messages mockMessages = mock( Messages.class );
    doReturn( mockMessages ).when( fileResource ).getMessagesInstance();

    String message = "message";
    String key = "FileResource.PARAM_FAILURE";
    doReturn( message ).when( mockMessages ).getString( key, exceptionMessage );

    // Test 1
    String testResult = fileResource.doIsParameterizable( PATH_ID );
    assertEquals( Boolean.FALSE.toString(), testResult );

    // Test 2
    doReturn( true ).when( fileResource ).hasParameterUi( mockRepositoryFile );
    doThrow( mockNoSuchBeanDefinitionException ).when( fileResource ).getContentGenerator( mockRepositoryFile );

    testResult = fileResource.doIsParameterizable( PATH_ID );
    assertEquals( Boolean.FALSE.toString(), testResult );

    verify( fileResource.fileService, times( 2 ) ).idToPath( PATH_ID );
    verify( fileResource.repository, times( 2 ) ).getFile( path );
    verify( fileResource, times( 2 ) ).hasParameterUi( mockRepositoryFile );
    verify( mockNoSuchBeanDefinitionException, times( 1 ) ).getMessage();
    verify( mockMessages, times( 1 ) ).getString( key, exceptionMessage );
  }

  @Test
  public void testDoGetFileOrDirAsDownload() throws Throwable {
    String userAgent = "userAgent";

    String strWithManifest = "strWithManifest";

    FileService.DownloadFileWrapper mockDownloadFileWrapper = mock( FileService.DownloadFileWrapper.class );
    doReturn( mockDownloadFileWrapper ).when( fileResource.fileService )
      .doGetFileOrDirAsDownload( userAgent, PATH_ID, strWithManifest );

    Response mockResponse = mock( Response.class );
    doReturn( mockResponse ).when( fileResource ).buildZipOkResponse( mockDownloadFileWrapper );

    Response testResponse = fileResource.doGetFileOrDirAsDownload( userAgent, PATH_ID, strWithManifest );
    assertEquals( mockResponse, testResponse );

    verify( fileResource.fileService, times( 1 ) ).doGetFileOrDirAsDownload( userAgent, PATH_ID, strWithManifest );
    verify( fileResource ).buildZipOkResponse( mockDownloadFileWrapper );
  }

  @Test
  public void testDoGetFileOrDirAsDownloadError() throws Throwable {
    String userAgent = "userAgent";

    String strWithManifest = "strWithManifest";

    Messages mockMessages = mock( Messages.class );
    doReturn( mockMessages ).when( fileResource ).getMessagesInstance();

    Response mockBadRequestResponse = mock( Response.class );
    doReturn( mockBadRequestResponse ).when( fileResource ).buildStatusResponse( Response.Status.BAD_REQUEST );

    Response mockForbiddenResponse = mock( Response.class );
    doReturn( mockForbiddenResponse ).when( fileResource ).buildStatusResponse( FORBIDDEN );

    Response mockNotFoundResponse = mock( Response.class );
    doReturn( mockNotFoundResponse ).when( fileResource ).buildStatusResponse( Response.Status.NOT_FOUND );

    Response mockInternalServerErrorResponse = mock( Response.class );
    doReturn( mockInternalServerErrorResponse ).when( fileResource ).buildStatusResponse( INTERNAL_SERVER_ERROR );

    String exceptionMessage = "exception";

    // Test 1
    Exception mockInvalidParameterException = mock( InvalidParameterException.class );
    doThrow( mockInvalidParameterException ).when( fileResource.fileService )
      .doGetFileOrDirAsDownload( userAgent, PATH_ID, strWithManifest );
    doReturn( exceptionMessage ).when( mockInvalidParameterException ).getMessage();

    Response testResponse = fileResource.doGetFileOrDirAsDownload( userAgent, PATH_ID, strWithManifest );
    assertEquals( mockBadRequestResponse, testResponse );

    // Test 2
    Exception mockIllegalSelectorException = mock( IllegalSelectorException.class );
    doThrow( mockIllegalSelectorException ).when( fileResource.fileService ).doGetFileOrDirAsDownload( userAgent,
      PATH_ID, strWithManifest );
    doReturn( exceptionMessage ).when( mockIllegalSelectorException ).getMessage();

    testResponse = fileResource.doGetFileOrDirAsDownload( userAgent, PATH_ID, strWithManifest );
    assertEquals( mockForbiddenResponse, testResponse );

    // Test 3
    Exception mockPentahoAccessControlException = mock( PentahoAccessControlException.class );
    doThrow( mockPentahoAccessControlException ).when( fileResource.fileService ).doGetFileOrDirAsDownload( userAgent,
      PATH_ID, strWithManifest );
    doReturn( exceptionMessage ).when( mockPentahoAccessControlException ).getMessage();

    testResponse = fileResource.doGetFileOrDirAsDownload( userAgent, PATH_ID, strWithManifest );
    assertEquals( mockForbiddenResponse, testResponse );

    // Test 4
    Exception mockFileNotFoundException = mock( FileNotFoundException.class );
    doThrow( mockFileNotFoundException ).when( fileResource.fileService ).doGetFileOrDirAsDownload( userAgent, PATH_ID,
      strWithManifest );
    doReturn( exceptionMessage ).when( mockFileNotFoundException ).getMessage();

    testResponse = fileResource.doGetFileOrDirAsDownload( userAgent, PATH_ID, strWithManifest );
    assertEquals( mockNotFoundResponse, testResponse );

    // Test 5
    Throwable mockThrowable = mock( Throwable.class );
    doThrow( mockThrowable ).when( fileResource.fileService ).doGetFileOrDirAsDownload( userAgent, PATH_ID,
      strWithManifest );
    doReturn( exceptionMessage ).when( mockThrowable ).getMessage();

    testResponse = fileResource.doGetFileOrDirAsDownload( userAgent, PATH_ID, strWithManifest );
    assertEquals( mockInternalServerErrorResponse, testResponse );

    verify( mockMessages, times( 4 ) ).getString( "FileResource.EXPORT_FAILED", exceptionMessage );
    verify( mockMessages, times( 1 ) ).getString( "FileResource.EXPORT_FAILED", PATH_ID + " " + exceptionMessage );
  }

  @Test
  public void testDoGetFileAsInline() throws Exception {


    FileService.RepositoryFileToStreamWrapper mockWrapper = mock( FileService.RepositoryFileToStreamWrapper.class );
    doReturn( mockWrapper ).when( fileResource.fileService ).doGetFileAsInline( PATH_ID );

    Response mockResponse = mock( Response.class );
    doReturn( mockResponse ).when( fileResource ).buildOkResponse( mockWrapper );

    Response testResponse = fileResource.doGetFileAsInline( PATH_ID );

    assertEquals( mockResponse, testResponse );
    verify( fileResource.fileService, times( 1 ) ).doGetFileAsInline( PATH_ID );
    verify( fileResource, times( 1 ) ).buildOkResponse( mockWrapper );
  }

  @Test
  public void testDoGetFileAsInlineError() throws Exception {


    Response mockForbiddenResponse = mock( Response.class );
    doReturn( mockForbiddenResponse ).when( fileResource ).buildStatusResponse( FORBIDDEN );

    Response mockNotFoundResponse = mock( Response.class );
    doReturn( mockNotFoundResponse ).when( fileResource ).buildStatusResponse( Response.Status.NOT_FOUND );

    Response mockInternalServereErrorResponse = mock( Response.class );
    doReturn( mockInternalServereErrorResponse ).when( fileResource ).buildStatusResponse( INTERNAL_SERVER_ERROR );

    Messages mockMessages = mock( Messages.class );
    doReturn( mockMessages ).when( fileResource ).getMessagesInstance();

    // Test 1
    Exception mockIllegalArgumentException = mock( IllegalArgumentException.class );
    doThrow( mockIllegalArgumentException ).when( fileResource.fileService ).doGetFileAsInline( PATH_ID );

    Response testResponse = fileResource.doGetFileAsInline( PATH_ID );
    assertEquals( mockForbiddenResponse, testResponse );

    // Test 2
    Exception mockFileNotFoundException = mock( FileNotFoundException.class );
    doThrow( mockFileNotFoundException ).when( fileResource.fileService ).doGetFileAsInline( PATH_ID );

    testResponse = fileResource.doGetFileAsInline( PATH_ID );
    assertEquals( mockNotFoundResponse, testResponse );

    // Test 3
    Error mockInternalError = mock( InternalError.class );
    doThrow( mockInternalError ).when( fileResource.fileService ).doGetFileAsInline( PATH_ID );

    testResponse = fileResource.doGetFileAsInline( PATH_ID );
    assertEquals( mockInternalServereErrorResponse, testResponse );

    verify( mockMessages, times( 3 ) ).getString( "SystemResource.GENERAL_ERROR" );
  }

  @Test
  public void testSetFileAcls() throws Exception {
    RepositoryFileAclDto repository = mock( RepositoryFileAclDto.class );
    doReturn( ACL_OWNER ).when( repository ).getOwner();

    doReturn( true ).when( fileResource ).userExists( ACL_OWNER );

    assertEquals( OK.getStatusCode(), fileResource.setFileAcls( PATH_ID, repository ).getStatus() );
  }

  @Test
  public void testSetFileAclsError() throws Exception {
    RepositoryFileAclDto repository = mock( RepositoryFileAclDto.class );
    doReturn( ACL_OWNER ).when( repository ).getOwner();

    doReturn( mock( Messages.class ) ).when( fileResource ).getMessagesInstance();
    doReturn( true ).when( fileResource ).userExists( ACL_OWNER );
    doThrow( mock( RuntimeException.class ) ).when( fileResource.fileService ).setFileAcls( PATH_ID, repository );

    assertEquals( INTERNAL_SERVER_ERROR.getStatusCode(), fileResource.setFileAcls( PATH_ID, repository ).getStatus() );
  }

  /*
   * [BISERVER-14294] Validating the ACL empty owner is tested against.
   */
  @Test
  public void testSetFileAclsErrorNoOwner() throws Exception {
    RepositoryFileAclDto repository = mock( RepositoryFileAclDto.class );
    doReturn( "" ).when( repository ).getOwner();

    doReturn( mock( Messages.class ) ).when( fileResource ).getMessagesInstance();

    assertEquals( FORBIDDEN.getStatusCode(), fileResource.setFileAcls( PATH_ID, repository ).getStatus() );
  }

  /*
   * [BISERVER-14294] Validating the ACL unknown owner is tested against.
   */
  @Test
  public void testSetFileAclsWrongOwnerError() throws Exception {
    RepositoryFileAclDto repository = mock( RepositoryFileAclDto.class );
    doReturn( "Wrong Owner" ).when( repository ).getOwner();

    doReturn( mock( Messages.class ) ).when( fileResource ).getMessagesInstance();
    doReturn( false ).when( fileResource ).userExists( anyString() );

    assertEquals( FORBIDDEN.getStatusCode(), fileResource.setFileAcls( PATH_ID, repository ).getStatus() );
  }

  @Test
  public void testSetContentCreator() throws Exception {

    RepositoryFileDto mockRepositoryFileDto = mock( RepositoryFileDto.class );

    doNothing().when( fileResource.fileService ).doSetContentCreator( PATH_ID, mockRepositoryFileDto );

    Response mockOkResponse = mock( Response.class );
    doReturn( mockOkResponse ).when( fileResource ).buildOkResponse();

    Response testResponse = fileResource.doSetContentCreator( PATH_ID, mockRepositoryFileDto );
    assertEquals( mockOkResponse, testResponse );

    verify( fileResource.fileService, times( 1 ) ).doSetContentCreator( PATH_ID, mockRepositoryFileDto );
    verify( fileResource, times( 1 ) ).buildOkResponse();
  }

  @Test
  public void testSetContentCreatorError() throws Exception {

    RepositoryFileDto mockRepositoryFileDto = mock( RepositoryFileDto.class );

    Response mockNotFoundResponse = mock( Response.class );
    doReturn( mockNotFoundResponse ).when( fileResource ).buildStatusResponse( Response.Status.NOT_FOUND );

    Response mockInternalServerErrorResponse = mock( Response.class );
    doReturn( mockInternalServerErrorResponse ).when( fileResource ).buildStatusResponse( INTERNAL_SERVER_ERROR );

    Messages mockMessages = mock( Messages.class );
    doReturn( mockMessages ).when( fileResource ).getMessagesInstance();

    // Test 1
    Exception mockFileNotFoundException = mock( FileNotFoundException.class );
    doThrow( mockFileNotFoundException ).when( fileResource.fileService )
      .doSetContentCreator( PATH_ID, mockRepositoryFileDto );

    Response testResponse = fileResource.doSetContentCreator( PATH_ID, mockRepositoryFileDto );
    assertEquals( mockNotFoundResponse, testResponse );

    // Test 2
    Throwable mockThrowable = mock( RuntimeException.class );
    doThrow( mockThrowable ).when( fileResource.fileService ).doSetContentCreator( PATH_ID, mockRepositoryFileDto );

    testResponse = fileResource.doSetContentCreator( PATH_ID, mockRepositoryFileDto );
    assertEquals( mockInternalServerErrorResponse, testResponse );

    verify( fileResource, times( 1 ) ).buildStatusResponse( Response.Status.NOT_FOUND );
    verify( fileResource, times( 1 ) ).buildStatusResponse( INTERNAL_SERVER_ERROR );
    verify( fileResource.fileService, times( 2 ) ).doSetContentCreator( PATH_ID, mockRepositoryFileDto );
    verify( mockMessages, times( 1 ) ).getErrorString( "FileResource.FILE_NOT_FOUND", PATH_ID );
    verify( mockMessages, times( 1 ) ).getString( "SystemResource.GENERAL_ERROR" );
  }

  @Test
  public void testDoGetFileLocales() throws Exception {


    List<LocaleMapDto> mockList = mock( List.class );
    doReturn( mockList ).when( fileResource.fileService ).doGetFileLocales( PATH_ID );

    List<LocaleMapDto> testLocales = fileResource.doGetFileLocales( PATH_ID );
    assertEquals( mockList, testLocales );

    verify( fileResource.fileService, times( 1 ) ).doGetFileLocales( PATH_ID );
  }

  @Test
  public void testDoGetFileLocalesError() throws Exception {


    Messages mockMessages = mock( Messages.class );
    doReturn( mockMessages ).when( fileResource ).getMessagesInstance();

    // Test 1
    Exception mockFileNotFoundException = mock( FileNotFoundException.class );
    doThrow( mockFileNotFoundException ).when( fileResource.fileService ).doGetFileLocales( PATH_ID );

    List<LocaleMapDto> testLocales = fileResource.doGetFileLocales( PATH_ID );
    assertEquals( 0, testLocales.size() );

    // Test 2
    Throwable mockThrowable = mock( RuntimeException.class );
    doThrow( mockThrowable ).when( fileResource.fileService ).doGetFileLocales( PATH_ID );

    testLocales = fileResource.doGetFileLocales( PATH_ID );
    assertEquals( 0, testLocales.size() );

    verify( fileResource, times( 2 ) ).getMessagesInstance();
    verify( mockMessages, times( 1 ) ).getErrorString( "FileResource.FILE_NOT_FOUND", PATH_ID );
    verify( mockMessages, times( 1 ) ).getString( "SystemResource.GENERAL_ERROR" );
  }

  @Test
  public void testDoGetLocaleProperties() {

    String locale = "locale";

    List<StringKeyStringValueDto> mockList = mock( List.class );
    doReturn( mockList ).when( fileResource.fileService ).doGetLocaleProperties( PATH_ID, locale );

    List<StringKeyStringValueDto> testList = fileResource.doGetLocaleProperties( PATH_ID, locale );
    assertEquals( testList, mockList );
  }

  @Test
  public void testDoDeleteLocale() throws Exception {

    String locale = "locale";

    doNothing().when( fileResource.fileService ).doDeleteLocale( PATH_ID, locale );

    Response mockResponse = mock( Response.class );
    doReturn( mockResponse ).when( fileResource ).buildOkResponse();

    Response testResponse = fileResource.doDeleteLocale( PATH_ID, locale );
    assertEquals( mockResponse, testResponse );

    verify( fileResource.fileService, times( 1 ) ).doDeleteLocale( PATH_ID, locale );
    verify( fileResource, times( 1 ) ).buildOkResponse();
  }

  @Test
  public void testDoDeleteLocaleError() throws Exception {

    String locale = "locale";

    Throwable mockThrowable = mock( RuntimeException.class );
    doThrow( mockThrowable ).when( fileResource.fileService ).doDeleteLocale( PATH_ID, locale );

    Response mockResponse = mock( Response.class );
    doReturn( mockResponse ).when( fileResource ).buildServerErrorResponse( mockThrowable );

    Response testResponse = fileResource.doDeleteLocale( PATH_ID, locale );
    assertEquals( mockResponse, testResponse );

    verify( fileResource.fileService, times( 1 ) ).doDeleteLocale( PATH_ID, locale );
    verify( fileResource, times( 1 ) ).buildServerErrorResponse( mockThrowable );
  }

  @Test
  public void testDoGetRootProperties() {
    RepositoryFileDto mockRepositoryFileDto = mock( RepositoryFileDto.class );
    doReturn( mockRepositoryFileDto ).when( fileResource.fileService ).doGetRootProperties();

    RepositoryFileDto testRepositoryFileDto = fileResource.doGetRootProperties();
    assertEquals( mockRepositoryFileDto, testRepositoryFileDto );

    verify( fileResource.fileService, times( 1 ) ).doGetRootProperties();
  }

  @Test
  public void testDoGetPathsAccessList() {
    StringListWrapper pathsWrapper = mock( StringListWrapper.class );

    List<Setting> mockList = mock( List.class );
    doReturn( mockList ).when( fileResource.fileService ).doGetPathsAccessList( pathsWrapper );

    List<Setting> testList = fileResource.doGetPathsAccessList( pathsWrapper );
    assertEquals( mockList, testList );

    verify( fileResource.fileService, times( 1 ) ).doGetPathsAccessList( pathsWrapper );
  }

  @Test
  public void testDoGetCanAccessList() {

    String permissions = "permissions";

    List<Setting> mockList = mock( List.class );
    doReturn( mockList ).when( fileResource.fileService ).doGetCanAccessList( PATH_ID, permissions );

    List<Setting> testList = fileResource.doGetCanAccessList( PATH_ID, permissions );
    assertEquals( mockList, testList );

    verify( fileResource.fileService, times( 1 ) ).doGetCanAccessList( PATH_ID, permissions );
  }

  @Test
  public void testDoGetCanAccess() {

    String permissions = "permissions";

    String canAccess = "canAccess";
    doReturn( canAccess ).when( fileResource.fileService ).doGetCanAccess( PATH_ID, permissions );

    String testString = fileResource.doGetCanAccess( PATH_ID, permissions );
    assertEquals( canAccess, testString );

    verify( fileResource.fileService, times( 1 ) ).doGetCanAccess( PATH_ID, permissions );
  }

  @Test
  public void testDoGetCanAdminister() throws Exception {
    // Test 1
    doReturn( true ).when( fileResource.fileService ).doCanAdminister();

    String result = fileResource.doGetCanAdminister();
    assertEquals( Boolean.TRUE.toString(), result );

    // Test 2
    doReturn( false ).when( fileResource.fileService ).doCanAdminister();

    result = fileResource.doGetCanAdminister();
    assertEquals( Boolean.FALSE.toString(), result );
  }

  @Test
  public void testDoGetReservedChars() {
    StringBuffer sb = new StringBuffer();
    doReturn( sb ).when( fileResource.fileService ).doGetReservedChars();

    Response mockResponse = mock( Response.class );
    doReturn( mockResponse ).when( fileResource ).buildPlainTextOkResponse( sb.toString() );

    Response testResponse = fileResource.doGetReservedChars();
    assertEquals( mockResponse, testResponse );

    verify( fileResource.fileService, times( 1 ) ).doGetReservedChars();
    verify( fileResource, times( 1 ) ).buildPlainTextOkResponse( sb.toString() );
  }

  @Test
  public void testDoGetReservedCharactersDisplay() {
    StringBuffer sb = new StringBuffer();
    doReturn( sb ).when( fileResource.fileService ).doGetReservedCharactersDisplay();

    Response mockResponse = mock( Response.class );
    doReturn( mockResponse ).when( fileResource ).buildPlainTextOkResponse( sb.toString() );

    Response testResponse = fileResource.doGetReservedCharactersDisplay();
    assertEquals( mockResponse, testResponse );

    verify( fileResource.fileService, times( 1 ) ).doGetReservedCharactersDisplay();
    verify( fileResource, times( 1 ) ).buildPlainTextOkResponse( sb.toString() );
  }

  @Test
  public void testDoGetCanCreate() {
    String canCreate = "canCreate";
    doReturn( canCreate ).when( fileResource.fileService ).doGetCanCreate();

    String result = fileResource.doGetCanCreate();
    assertEquals( canCreate, result );

    verify( fileResource.fileService, times( 1 ) ).doGetCanCreate();
  }

  @Test
  public void testDoGetFileAcl() {


    RepositoryFileAclDto mockRepositoryFileAclDto = mock( RepositoryFileAclDto.class );
    doReturn( mockRepositoryFileAclDto ).when( fileResource.fileService ).doGetFileAcl( PATH_ID );

    RepositoryFileAclDto testResult = fileResource.doGetFileAcl( PATH_ID );
    assertEquals( mockRepositoryFileAclDto, testResult );
  }

  @Test
  public void testDoGetProperties() throws Exception {


    RepositoryFileDto mockRepositoryFileDto = mock( RepositoryFileDto.class );
    doReturn( mockRepositoryFileDto ).when( fileResource.fileService ).doGetProperties( PATH_ID );

    RepositoryFileDto testDto = fileResource.doGetProperties( PATH_ID );
    assertEquals( mockRepositoryFileDto, testDto );

    verify( fileResource.fileService, times( 1 ) ).doGetProperties( PATH_ID );
  }

  @Test
  public void testDoGetPropertiesError() throws Exception {


    Exception mockFileNotFoundException = mock( FileNotFoundException.class );
    doThrow( mockFileNotFoundException ).when( fileResource.fileService ).doGetProperties( PATH_ID );

    Messages mockMessages = mock( Messages.class );
    doReturn( mockMessages ).when( fileResource ).getMessagesInstance();

    RepositoryFileDto testDto = fileResource.doGetProperties( PATH_ID );
    assertNull( testDto );

    verify( fileResource.fileService, times( 1 ) ).doGetProperties( PATH_ID );
    verify( fileResource, times( 1 ) ).getMessagesInstance();
    verify( mockMessages, times( 1 ) ).getString( "SystemResource.GENERAL_ERROR" );
  }

  @Test
  public void testDoGetContentCreator() throws Exception {


    RepositoryFileDto mockRepositoryFileDto = mock( RepositoryFileDto.class );
    doReturn( mockRepositoryFileDto ).when( fileResource.fileService ).doGetContentCreator( PATH_ID );

    RepositoryFileDto testDto = fileResource.doGetContentCreator( PATH_ID );
    assertEquals( mockRepositoryFileDto, testDto );

    verify( fileResource.fileService, times( 1 ) ).doGetContentCreator( PATH_ID );
  }

  @Test
  public void testDoGetContentCreatorError() throws Exception {


    Throwable mockThrowable = mock( RuntimeException.class );
    doThrow( mockThrowable ).when( fileResource.fileService ).doGetContentCreator( PATH_ID );

    RepositoryFileDto testDto = fileResource.doGetContentCreator( PATH_ID );
    assertNull( testDto );

    verify( fileResource.fileService, times( 1 ) ).doGetContentCreator( PATH_ID );
  }

  @Test
  public void testDoGetGeneratedContent() throws Exception {


    List<RepositoryFileDto> mockList = mock( List.class );
    doReturn( mockList ).when( fileResource.fileService ).doGetGeneratedContent( PATH_ID );

    List<RepositoryFileDto> testList = fileResource.doGetGeneratedContent( PATH_ID );
    assertEquals( mockList, testList );

    verify( fileResource.fileService, times( 1 ) ).doGetGeneratedContent( PATH_ID );
  }

  @Test
  public void testDoGetGeneratedContentError() throws Exception {


    Exception mockFileNotFoundException = mock( FileNotFoundException.class );
    doThrow( mockFileNotFoundException ).when( fileResource.fileService ).doGetGeneratedContent( PATH_ID );

    Messages mockMessages = mock( Messages.class );
    doReturn( mockMessages ).when( fileResource ).getMessagesInstance();

    // Test 1
    List<RepositoryFileDto> testList = fileResource.doGetGeneratedContent( PATH_ID );
    assertEquals( 0, testList.size() );

    // Test 2
    Throwable mockThrowable = mock( RuntimeException.class );
    doThrow( mockThrowable ).when( fileResource.fileService ).doGetGeneratedContent( PATH_ID );

    testList = fileResource.doGetGeneratedContent( PATH_ID );
    assertEquals( 0, testList.size() );

    verify( fileResource.fileService, times( 2 ) ).doGetGeneratedContent( PATH_ID );
    verify( fileResource, times( 1 ) ).getMessagesInstance();
    verify( mockMessages, times( 1 ) ).getString( "FileResource.GENERATED_CONTENT_FAILED", PATH_ID );
  }

  @Test
  public void testDoGetGeneratedContentForUser() throws Exception {

    String user = "user";

    List<RepositoryFileDto> mockList = mock( List.class );
    doReturn( mockList ).when( fileResource.fileService ).doGetGeneratedContent( PATH_ID, user );

    List<RepositoryFileDto> testList = fileResource.doGetGeneratedContentForUser( PATH_ID, user );
    assertEquals( mockList, testList );

    verify( fileResource.fileService, times( 1 ) ).doGetGeneratedContent( PATH_ID, user );
  }

  @Test
  public void testDoGetGeneratedContentForUserError() throws Exception {

    String user = "user";

    Exception mockFileNotFoundException = mock( FileNotFoundException.class );
    doThrow( mockFileNotFoundException ).when( fileResource.fileService ).doGetGeneratedContent( PATH_ID, user );

    Messages mockMessages = mock( Messages.class );
    doReturn( mockMessages ).when( fileResource ).getMessagesInstance();

    // Test 1
    List<RepositoryFileDto> testList = fileResource.doGetGeneratedContentForUser( PATH_ID, user );
    assertEquals( 0, testList.size() );

    // Test 2
    Throwable mockThrowable = mock( RuntimeException.class );
    doThrow( mockThrowable ).when( fileResource.fileService ).doGetGeneratedContent( PATH_ID, user );

    testList = fileResource.doGetGeneratedContentForUser( PATH_ID, user );
    assertEquals( 0, testList.size() );

    verify( fileResource.fileService, times( 2 ) ).doGetGeneratedContent( PATH_ID, user );
    verify( fileResource, times( 1 ) ).getMessagesInstance();
    verify( mockMessages, times( 1 ) ).getString( "FileResource.GENERATED_CONTENT_FOR_USER_FAILED", PATH_ID, user );
  }

  @Test
  public void testDoGetRootTree() {
    Integer depth = 0;
    String filter = "filter";
    Boolean showHidden = Boolean.TRUE;
    Boolean includeAcls = Boolean.TRUE;

    RepositoryFileTreeDto mockRepositoryFileTreeDto = mock( RepositoryFileTreeDto.class );
    doReturn( mockRepositoryFileTreeDto ).when( fileResource.fileService )
      .doGetTree( FileUtils.PATH_SEPARATOR, depth, filter, showHidden, includeAcls );

    RepositoryFileTreeDto testDto = fileResource.doGetRootTree( depth, filter, showHidden, includeAcls );
    assertEquals( mockRepositoryFileTreeDto, testDto );

    verify( fileResource.fileService, times( 1 ) )
      .doGetTree( FileUtils.PATH_SEPARATOR, depth, filter, showHidden, includeAcls );
  }

  @Test
  public void testDoGetRootChildren() {
    String filter = "filter";
    Boolean showHidden = Boolean.TRUE;
    Boolean includeAcls = Boolean.TRUE;

    List<RepositoryFileDto> mockList = mock( List.class );
    doReturn( mockList ).when( fileResource.fileService )
      .doGetChildren( FileUtils.PATH_SEPARATOR, filter, showHidden, includeAcls );

    List<RepositoryFileDto> testList = fileResource.doGetRootChildren( filter, showHidden, includeAcls );
    assertEquals( mockList, testList );

    verify( fileResource.fileService, times( 1 ) ).doGetChildren( FileUtils.PATH_SEPARATOR, filter, showHidden,
      includeAcls );
  }

  @Test
  public void testDoGetTree() {

    Integer depth = 0;
    String filter = "filter";
    Boolean showHidden = Boolean.TRUE;
    Boolean includeAcls = Boolean.TRUE;
    Boolean includeSysDirs = Boolean.FALSE;

    RepositoryFileTreeDto mockRepositoryFileTreeDto = mock( RepositoryFileTreeDto.class );
    doReturn( mockRepositoryFileTreeDto ).when( fileResource.fileService )
      .doGetTree( PATH_ID, depth, filter, showHidden,
        includeAcls, includeSysDirs );

    RepositoryFileTreeDto testDto = fileResource.doGetTree( PATH_ID, depth, filter, showHidden, includeAcls, includeSysDirs );
    assertEquals( mockRepositoryFileTreeDto, testDto );

    verify( fileResource.fileService ).doGetTree( PATH_ID, depth, filter, showHidden, includeAcls, includeSysDirs );
  }

  @Test
  public void testDoGetChildren() {

    String filter = "filter";
    Boolean showHidden = Boolean.TRUE;
    Boolean includeAcls = Boolean.TRUE;

    List<RepositoryFileDto> mockList = mock( List.class );
    doReturn( mockList ).when( fileResource.fileService )
      .doGetChildren( PATH_ID, filter, showHidden, includeAcls );

    List<RepositoryFileDto> testList = fileResource.doGetChildren( PATH_ID, filter, showHidden, includeAcls );
    assertEquals( mockList, testList );

    verify( fileResource.fileService, times( 1 ) ).doGetChildren( PATH_ID, filter, showHidden,
      includeAcls );
  }

  @Test
  public void testDoGetDeletedFiles() {
    List<RepositoryFileDto> mockList = mock( List.class );
    doReturn( mockList ).when( fileResource.fileService ).doGetDeletedFiles();

    List<RepositoryFileDto> testList = fileResource.doGetDeletedFiles();
    assertEquals( mockList, testList );

    verify( fileResource.fileService, times( 1 ) ).doGetDeletedFiles();
  }

  @Test
  public void testDoGetMetadata() throws Exception {


    List<StringKeyStringValueDto> mockList = mock( List.class );
    doReturn( mockList ).when( fileResource.fileService ).doGetMetadata( PATH_ID );

    List<StringKeyStringValueDto> testList = fileResource.doGetMetadata( PATH_ID );
    assertEquals( mockList, testList );

    verify( fileResource.fileService, times( 1 ) ).doGetMetadata( PATH_ID );
  }

  @Test
  public void testDoGetMetadataError() throws Exception {


    Exception mockFileNotFoundException = mock( FileNotFoundException.class );
    doThrow( mockFileNotFoundException ).when( fileResource.fileService ).doGetMetadata( PATH_ID );

    Messages mockMessages = mock( Messages.class );
    doReturn( mockMessages ).when( fileResource ).getMessagesInstance();

    List<StringKeyStringValueDto> testList = fileResource.doGetMetadata( PATH_ID );
    assertNull( testList );

    verify( fileResource.fileService, times( 1 ) ).doGetMetadata( PATH_ID );
    verify( fileResource, times( 1 ) ).getMessagesInstance();
    verify( mockMessages, times( 1 ) ).getErrorString( "FileResource.FILE_UNKNOWN", PATH_ID );
  }

  @Test
  public void testDoRename() throws Exception {
    Response mockOkResponse = mock( Response.class );
    doReturn( mockOkResponse ).when( fileResource ).buildOkResponse();

    String errMsg = "File to be renamed does not exist";
    Response mockOkMsgResponse = mock( Response.class );
    doReturn( mockOkMsgResponse ).when( fileResource ).buildOkResponse( errMsg );

    // Test 1
    doReturn( true ).when( fileResource.fileService ).doRename( PATH_ID, NAME_NEW_FILE_WITHOUTH_EXTENSION );

    Response testResponse = fileResource.doRename( PATH_ID, NAME_NEW_FILE_WITHOUTH_EXTENSION );
    assertEquals( mockOkResponse, testResponse );

    // Test 2
    doReturn( false ).when( fileResource.fileService ).doRename( PATH_ID, NAME_NEW_FILE_WITHOUTH_EXTENSION );

    testResponse = fileResource.doRename( PATH_ID, NAME_NEW_FILE_WITHOUTH_EXTENSION );
    assertEquals( mockOkMsgResponse, testResponse );

    verify( fileResource.fileService, times( 2 ) ).doRename( PATH_ID, NAME_NEW_FILE_WITHOUTH_EXTENSION );
    verify( fileResource, times( 1 ) ).buildOkResponse();
    verify( fileResource, times( 1 ) ).buildOkResponse( errMsg );
  }

  @Test
  public void testDoRenameCorrectExtension() throws Exception {
    Response testResponse = fileResource.doRename( PATH_ID, NAME_NEW_FILE_WITHOUTH_EXTENSION );
    assertEquals( OK.getStatusCode(), testResponse.getStatus() );

    verify( fileResource, times( 1 ) ).buildOkResponse( anyString() );
  }

  @Test
  public void testDoRenameError() throws Exception {
    Throwable mockThrowable = mock( RuntimeException.class );
    doThrow( mockThrowable ).when( fileResource.fileService ).doRename( PATH_ID, NAME_NEW_FILE_WITHOUTH_EXTENSION );

    String msg = "msg";
    doReturn( msg ).when( mockThrowable ).getMessage();

    Response mockResponse = mock( Response.class );
    doReturn( mockResponse ).when( fileResource ).buildServerErrorResponse( msg );

    Response testResponse = fileResource.doRename( PATH_ID, NAME_NEW_FILE_WITHOUTH_EXTENSION );
    assertEquals( mockResponse, testResponse );

    verify( fileResource.fileService, times( 1 ) ).doRename( PATH_ID, NAME_NEW_FILE_WITHOUTH_EXTENSION );
    verify( mockThrowable, times( 1 ) ).getMessage();
    verify( fileResource, times( 1 ) ).buildServerErrorResponse( msg );
  }

  @Test
  public void testDoSetMetadata() throws Exception {
    List<StringKeyStringValueDto> metadata = mock( List.class );

    doNothing().when( fileResource.fileService ).doSetMetadata( PATH_ID, metadata );

    Response mockResponse = mock( Response.class );
    doReturn( mockResponse ).when( fileResource ).buildOkResponse();

    Response testResponse = fileResource.doSetMetadata( PATH_ID, metadata );
    assertEquals( mockResponse, testResponse );

    verify( fileResource.fileService, times( 1 ) ).doSetMetadata( PATH_ID, metadata );
    verify( fileResource, times( 1 ) ).buildOkResponse();
  }

  @Test
  public void testDoSetMetadataError() throws Exception {
    List<StringKeyStringValueDto> metadata = mock( List.class );

    Response mockUnauthorizedResponse = mock( Response.class );
    doReturn( mockUnauthorizedResponse ).when( fileResource ).buildStatusResponse( Response.Status.UNAUTHORIZED );

    Throwable mockThrowable = mock( RuntimeException.class );

    String errMsg = "errMsg";
    doReturn( errMsg ).when( mockThrowable ).getMessage();

    Response mockThrowableResponse = mock( Response.class );
    doReturn( mockThrowableResponse ).when( fileResource ).buildServerErrorResponse( errMsg );

    // Test 1
    Exception mockGeneralSecurityException = mock( GeneralSecurityException.class );
    doThrow( mockGeneralSecurityException ).when( fileResource.fileService ).doSetMetadata( PATH_ID, metadata );

    Response testResponse = fileResource.doSetMetadata( PATH_ID, metadata );
    assertEquals( mockUnauthorizedResponse, testResponse );

    // Test 2
    doThrow( mockThrowable ).when( fileResource.fileService ).doSetMetadata( PATH_ID, metadata );

    testResponse = fileResource.doSetMetadata( PATH_ID, metadata );
    assertEquals( mockThrowableResponse, testResponse );

    verify( fileResource.fileService, times( 2 ) ).doSetMetadata( PATH_ID, metadata );
    verify( fileResource, times( 1 ) ).buildStatusResponse( Response.Status.UNAUTHORIZED );
    verify( mockThrowable, times( 1 ) ).getMessage();
    verify( fileResource, times( 1 ) ).buildServerErrorResponse( errMsg );
  }


  @Test
  public void testCustomMime() {
    FileResource fileResource = new FileResource();
    final FileService.RepositoryFileToStreamWrapper mock = mock( FileService.RepositoryFileToStreamWrapper.class );
    when( mock.getOutputStream() ).thenReturn( new StreamingOutput() {
      @Override public void write( OutputStream outputStream ) throws IOException, WebApplicationException {

      }
    } );
    when( mock.getMimetype() ).thenReturn( "mime-message/text/html" );
    final RepositoryFile repositoryFile = mock( RepositoryFile.class );
    when( repositoryFile.getName() ).thenReturn( "test" );
    when( mock.getRepositoryFile() ).thenReturn( repositoryFile );
    final Response response = fileResource.buildOkResponse( mock );
    final MultivaluedMap<String, Object> metadata = response.getMetadata();
  }

  @Test
  public void testGenerateDocumentFromXMLString() throws Exception {
    String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<element>" + "true" + "</element>";
    Document document = fileResource.parseText( xml );
    assertNotNull( document );
    assertTrue( document.getRootElement().getStringValue().equals( "true" ) );
  }

  @Test
  public void userExists() {
    Tenant tenant = mock( Tenant.class );
    when( TenantUtils.getCurrentTenant() ).thenReturn( tenant );

    IUserRoleDao userRoleDao = mock( IUserRoleDao.class );
    PentahoSystem.registerObject( userRoleDao );

    IPentahoUser user = new MockPentahoUser( new Tenant(), ACL_OWNER, null, null, false );
    doReturn( user ).when( userRoleDao ).getUser( tenant, "admin" );

    FileResource fileResource = new FileResource();
    assertTrue( fileResource.userExists( "admin" ) );
  }

  @Test
  public void userDoesntExist() {
    Tenant tenant = mock( Tenant.class );
    when( TenantUtils.getCurrentTenant() ).thenReturn( tenant );

    IUserRoleDao userRoleDao = mock( IUserRoleDao.class );
    PentahoSystem.registerObject( userRoleDao );

    doReturn( null ).when( userRoleDao ).getUser( tenant, "joe" );

    FileResource fileResource = new FileResource();
    assertFalse( fileResource.userExists( "joe" ) );
  }
}
