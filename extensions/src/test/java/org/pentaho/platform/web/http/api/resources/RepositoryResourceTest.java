/*
 * ! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.platform.web.http.api.resources;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.UnifiedRepositoryAccessDeniedException;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.vfs.IKettleVFS;
import org.pentaho.di.core.vfs.KettleVFS;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;

import jakarta.ws.rs.core.Response;
import jakarta.servlet.http.HttpServletRequest;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )
public class RepositoryResourceTest {

  private IUnifiedRepository repository;
  private IPluginManager pluginManager;
  private PluginResource pluginResource;
  private RepositoryResource repositoryResource;

  @Before
  public void setUp() throws IOException {
    pluginManager = mock( IPluginManager.class );
    repository = mock( IUnifiedRepository.class );

    repositoryResource = spy( new RepositoryResource() );
    repositoryResource.pluginManager = pluginManager;
    repositoryResource.repository = repository;

    // Preconfigure `repositoryResource` with a PluginResource which successfully finds requested files.
    pluginResource = mock( PluginResource.class );

    when( pluginResource.readFile( any(), any(), eq( false ) ) )
      .thenReturn( Response.status( Response.Status.OK ).build() );

    doReturn( pluginResource ).when( repositoryResource ).createPluginResource();
  }

  private Response createNotFoundResponse() {
    return Response.status( Response.Status.NOT_FOUND ).build();
  }

  @Test
  public void doExecuteDefaultNotFound() throws Exception {
    try ( MockedStatic<FileResource> fileResource = Mockito.mockStatic( FileResource.class ) ) {
      fileResource.when( () -> FileResource.idToPath( nullable( String.class ) ) ).thenCallRealMethod();
      fileResource.when( FileResource::getRepository ).thenReturn( repository );

      doReturn( null ).when( repository ).getFile( "/home/admin/comments.wcdf" );
      Response response = new RepositoryResource().doExecuteDefault( ":home:admin:comments.wcdf" );

      assertEquals( Response.Status.NOT_FOUND.getStatusCode(), response.getStatus() );
    }
  }

  // region isStaticResource( request ) tests
  @Test
  public void testIsStaticResource_True_ContextIsContentTypeAndResourceIsPublicAndExisting() {
    HttpServletRequest request = mock( HttpServletRequest.class );
    when( request.getServletPath() ).thenReturn( "/api" );
    when( request.getPathInfo() ).thenReturn( "/repos/xcontent/resource/file.js" );
    when( pluginManager.getPluginIdForType( "xcontent" ) ).thenReturn( "pluginA" );

    when( pluginManager.isPublic( "pluginA", "resource/file.js" ) ).thenReturn( true );

    boolean result = repositoryResource.isStaticResource( request );
    assertTrue( result );
  }

  @Test
  public void testIsStaticResource_False_ContextIsContentTypeAndResourceIsPublicButNotExisting() throws IOException {
    HttpServletRequest request = mock( HttpServletRequest.class );
    when( request.getServletPath() ).thenReturn( "/api" );
    when( request.getPathInfo() ).thenReturn( "/repos/xcontent/resource/file.js" );
    when( pluginManager.getPluginIdForType( "xcontent" ) ).thenReturn( "pluginA" );

    when( pluginManager.isPublic( "pluginA", "resource/file.js" ) ).thenReturn( true );
    when( pluginResource.readFile( "pluginA", "resource/file.js", false ) ).thenReturn( createNotFoundResponse() );

    boolean result = repositoryResource.isStaticResource( request );
    assertFalse( result );
  }

  @Test
  public void testIsStaticResource_False_ContextIsContentTypeButResourceNotPublic() {
    HttpServletRequest request = mock( HttpServletRequest.class );
    when( request.getServletPath() ).thenReturn( "/api" );
    when( request.getPathInfo() ).thenReturn( "/repos/xcontent/resource/file.js" );
    when( pluginManager.getPluginIdForType( "xcontent" ) ).thenReturn( "pluginA" );

    when( pluginManager.isPublic( "pluginA", "resource/file.js" ) ).thenReturn( false );

    boolean result = repositoryResource.isStaticResource( request );
    assertFalse( result );
  }

  @Test
  public void testIsStaticResource_True_ContextIsPluginAndResourceIsPublicAndExisting() {
    HttpServletRequest request = mock( HttpServletRequest.class );
    when( request.getServletPath() ).thenReturn( "/api" );
    when( request.getPathInfo() ).thenReturn( "/repos/pluginA/resource/file.js" );
    when( pluginManager.getPluginIdForType( "pluginA" ) ).thenReturn( null );
    when( pluginManager.getRegisteredPlugins() ).thenReturn( List.of( "pluginA" ) );

    when( pluginManager.isPublic( "pluginA", "resource/file.js" ) ).thenReturn( true );

    boolean result = repositoryResource.isStaticResource( request );
    assertTrue( result );
  }

  @Test
  public void testIsStaticResource_False_ContextIsPluginAndResourceIsPublicButNotExisting() throws IOException {
    HttpServletRequest request = mock( HttpServletRequest.class );
    when( request.getServletPath() ).thenReturn( "/api" );
    when( request.getPathInfo() ).thenReturn( "/repos/pluginA/resource/file.js" );
    when( pluginManager.getPluginIdForType( "pluginA" ) ).thenReturn( null );
    when( pluginManager.getRegisteredPlugins() ).thenReturn( List.of( "pluginA" ) );

    when( pluginManager.isPublic( "pluginA", "resource/file.js" ) ).thenReturn( true );
    when( pluginResource.readFile( "pluginA", "resource/file.js", false ) ).thenReturn( createNotFoundResponse() );

    boolean result = repositoryResource.isStaticResource( request );
    assertFalse( result );
  }

  @Test
  public void testIsStaticResource_False_ContextIsPluginButResourceNotPublic() {
    HttpServletRequest request = mock( HttpServletRequest.class );
    when( request.getServletPath() ).thenReturn( "/api" );
    when( request.getPathInfo() ).thenReturn( "/repos/pluginA/resource/file.js" );
    when( pluginManager.getPluginIdForType( "pluginA" ) ).thenReturn( null );
    when( pluginManager.getRegisteredPlugins() ).thenReturn( List.of( "pluginA" ) );

    when( pluginManager.isPublic( "pluginA", "resource/file.js" ) ).thenReturn( false );

    boolean result = repositoryResource.isStaticResource( request );
    assertFalse( result );
  }

  @Test
  public void testIsStaticResource_True_ContextIsExistingRepositoryFileAndResourceIsPublicAndExisting() {
    HttpServletRequest request = mock( HttpServletRequest.class );
    when( request.getServletPath() ).thenReturn( "/api" );
    when( request.getPathInfo() ).thenReturn( "/repos/:home:admin:file.ext/resource/file.js" );

    RepositoryFile file = mock( RepositoryFile.class );
    when( file.getName() ).thenReturn( "file.ext" );
    when( repository.getFile( anyString() ) ).thenReturn( file );

    when( pluginManager.getPluginIdForType( "ext" ) ).thenReturn( "pluginExt" );
    when( pluginManager.isPublic( "pluginExt", "resource/file.js" ) ).thenReturn( true );

    boolean result = repositoryResource.isStaticResource( request );
    assertTrue( result );
  }

  @Test
  public void testIsStaticResource_False_ContextIsRepositoryFileButNoReadContentPermission() {
    HttpServletRequest request = mock( HttpServletRequest.class );
    when( request.getServletPath() ).thenReturn( "/api" );
    when( request.getPathInfo() ).thenReturn( "/repos/:home:admin:file.ext/resource/file.js" );
    when( repository.getFile( anyString() ) ).thenThrow( UnifiedRepositoryAccessDeniedException.class );

    boolean result = repositoryResource.isStaticResource( request );
    assertFalse( result );
  }

  @Test
  public void testIsStaticResource_False_ContextIsExistingRepositoryFileAndResourceIsPublicButNotExisting()
    throws IOException {
    HttpServletRequest request = mock( HttpServletRequest.class );
    when( request.getServletPath() ).thenReturn( "/api" );
    when( request.getPathInfo() ).thenReturn( "/repos/:home:admin:file.ext/resource/file.js" );

    RepositoryFile file = mock( RepositoryFile.class );
    when( file.getName() ).thenReturn( "file.ext" );
    when( repository.getFile( anyString() ) ).thenReturn( file );

    when( pluginManager.getPluginIdForType( "ext" ) ).thenReturn( "pluginExt" );
    when( pluginManager.isPublic( "pluginExt", "resource/file.js" ) ).thenReturn( true );
    when( pluginResource.readFile( "pluginExt", "resource/file.js", false ) ).thenReturn( createNotFoundResponse() );

    boolean result = repositoryResource.isStaticResource( request );
    assertFalse( result );
  }

  @Test
  public void testIsStaticResource_False_ContextIsExistingRepositoryFileButResourceNotPublic() {
    HttpServletRequest request = mock( HttpServletRequest.class );
    when( request.getServletPath() ).thenReturn( "/api" );
    when( request.getPathInfo() ).thenReturn( "/repos/:home:admin:file.ext/viewer.js" );

    RepositoryFile file = mock( RepositoryFile.class );
    when( file.getName() ).thenReturn( "file.ext" );
    when( repository.getFile( anyString() ) ).thenReturn( file );

    when( pluginManager.getPluginIdForType( "ext" ) ).thenReturn( "pluginExt" );
    when( pluginManager.isPublic( "pluginExt", "viewer.js" ) ).thenReturn( false );

    boolean result = repositoryResource.isStaticResource( request );
    assertFalse( result );
  }

  @Test
  public void testIsStaticResource_False_ContextIsUnexistentRepositoryFile() {
    HttpServletRequest request = mock( HttpServletRequest.class );
    when( request.getServletPath() ).thenReturn( "/api" );
    when( request.getPathInfo() ).thenReturn( "/repos/:home:admin:file.ext/resource/file.js" );

    boolean result = repositoryResource.isStaticResource( request );
    assertFalse( result );
  }

  @Test
  public void testIsStaticResource_False_ContextIsNotRepositoryFileAndNotContentTypeAndNotPlugin() {
    HttpServletRequest request = mock( HttpServletRequest.class );
    when( request.getServletPath() ).thenReturn( "/api" );
    when( request.getPathInfo() ).thenReturn( "/other/resource/file.js" );

    boolean result = repositoryResource.isStaticResource( request );
    assertFalse( result );
  }
  // endregion

  // region PVFS Path Tests
  @Test
  public void testDoService_PvfsPath_ExistingFile_Success() throws Exception {
    String contextId = "pvfs\t::file.prpt";
    String resourceId = "viewer";

    // Mock Bowl and KettleVFS
    Bowl bowl = mock( Bowl.class );
    IKettleVFS vfs = mock( IKettleVFS.class );
    FileObject fileObject = mock( FileObject.class );
    FileName fileName = mock( FileName.class );

    doReturn( bowl ).when( repositoryResource ).getBowl();

    try ( MockedStatic<KettleVFS> kettleVFS = Mockito.mockStatic( KettleVFS.class ) ) {
      kettleVFS.when( () -> KettleVFS.getInstance( bowl ) ).thenReturn( vfs );

      when( vfs.getFileObject( "pvfs://file.prpt" ) ).thenReturn( fileObject );
      when( fileObject.exists() ).thenReturn( true );
      when( fileObject.getName() ).thenReturn( fileName );
      when( fileName.getBaseName() ).thenReturn( "file.prpt" );

      when( pluginManager.getPluginIdForType( "prpt" ) ).thenReturn( "reporting" );

      Response mockResponse = Response.ok().build();
      doReturn( mockResponse ).when( repositoryResource ).getContentGeneratorResponse( any() );

      Response response = repositoryResource.doService( contextId, resourceId );

      assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
    }
  }

  @Test
  public void testDoService_PvfsPath_FileNotFound() throws Exception {
    String contextId = "pvfs\t::nonexistent.prpt";
    String resourceId = "viewer";

    // Mock Bowl and KettleVFS
    Bowl bowl = mock( Bowl.class );
    IKettleVFS vfs = mock( IKettleVFS.class );
    FileObject fileObject = mock( FileObject.class );

    doReturn( bowl ).when( repositoryResource ).getBowl();

    try ( MockedStatic<KettleVFS> kettleVFS = Mockito.mockStatic( KettleVFS.class ) ) {
      kettleVFS.when( () -> KettleVFS.getInstance( bowl ) ).thenReturn( vfs );

      when( vfs.getFileObject( "pvfs://nonexistent.prpt" ) ).thenReturn( fileObject );
      when( fileObject.exists() ).thenReturn( false );

      Response response = repositoryResource.doService( contextId, resourceId );

      assertEquals( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus() );
    }
  }

  @Test
  public void testDoService_PvfsPath_FileObjectNull() throws Exception {
    String contextId = "pvfs\t::nullfile.prpt";
    String resourceId = "viewer";

    // Mock Bowl and KettleVFS
    Bowl bowl = mock( Bowl.class );
    IKettleVFS vfs = mock( IKettleVFS.class );

    doReturn( bowl ).when( repositoryResource ).getBowl();

    try ( MockedStatic<KettleVFS> kettleVFS = Mockito.mockStatic( KettleVFS.class ) ) {
      kettleVFS.when( () -> KettleVFS.getInstance( bowl ) ).thenReturn( vfs );

      when( vfs.getFileObject( "pvfs://nullfile.prpt" ) ).thenReturn( null );

      Response response = repositoryResource.doService( contextId, resourceId );

      assertEquals( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus() );
    }
  }

  @Test
  public void testDoService_PvfsPath_ExceptionAccessingFile() throws Exception {
    String contextId = "pvfs\t::errorfile.prpt";
    String resourceId = "viewer";

    // Mock Bowl and KettleVFS
    Bowl bowl = mock( Bowl.class );
    IKettleVFS vfs = mock( IKettleVFS.class );

    doReturn( bowl ).when( repositoryResource ).getBowl();

    try ( MockedStatic<KettleVFS> kettleVFS = Mockito.mockStatic( KettleVFS.class ) ) {
      kettleVFS.when( () -> KettleVFS.getInstance( bowl ) ).thenReturn( vfs );

      when( vfs.getFileObject( "pvfs://errorfile.prpt" ) ).thenThrow( new RuntimeException( "VFS error" ) );

      Response response = repositoryResource.doService( contextId, resourceId );

      assertEquals( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus() );
    }
  }

  @Test
  public void testDoService_PvfsPath_PrptiFile_InvalidOutputFormat() throws Exception {
    String contextId = "pvfs\t::report.prpti";
    String resourceId = "viewer";

    // Mock Bowl and KettleVFS
    Bowl bowl = mock( Bowl.class );
    IKettleVFS vfs = mock( IKettleVFS.class );
    FileObject fileObject = mock( FileObject.class );
    HttpServletRequest request = mock( HttpServletRequest.class );

    repositoryResource.httpServletRequest = request;

    Map<String, String[]> paramMap = new HashMap<>();
    paramMap.put( "output-target", new String[] { "invalid-format" } );
    when( request.getParameterMap() ).thenReturn( paramMap );

    doReturn( bowl ).when( repositoryResource ).getBowl();

    try ( MockedStatic<KettleVFS> kettleVFS = Mockito.mockStatic( KettleVFS.class ) ) {
      kettleVFS.when( () -> KettleVFS.getInstance( bowl ) ).thenReturn( vfs );

      when( vfs.getFileObject( "pvfs://report.prpti" ) ).thenReturn( fileObject );
      when( fileObject.exists() ).thenReturn( true );

      Response response = repositoryResource.doService( contextId, resourceId );

      assertEquals( Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus() );
    }
  }

  @Test
  public void testDoService_PvfsPath_PrptiFile_ValidOutputFormat() throws Exception {
    String contextId = "pvfs\t::report.prpti";
    String resourceId = "viewer";

    // Mock Bowl and KettleVFS
    Bowl bowl = mock( Bowl.class );
    IKettleVFS vfs = mock( IKettleVFS.class );
    FileObject fileObject = mock( FileObject.class );
    FileName fileName = mock( FileName.class );
    HttpServletRequest request = mock( HttpServletRequest.class );

    repositoryResource.httpServletRequest = request;

    Map<String, String[]> paramMap = new HashMap<>();
    paramMap.put( "output-target", new String[] { "table/html;page-mode=page" } );
    when( request.getParameterMap() ).thenReturn( paramMap );

    doReturn( bowl ).when( repositoryResource ).getBowl();

    try ( MockedStatic<KettleVFS> kettleVFS = Mockito.mockStatic( KettleVFS.class ) ) {
      kettleVFS.when( () -> KettleVFS.getInstance( bowl ) ).thenReturn( vfs );

      when( vfs.getFileObject( "pvfs://report.prpti" ) ).thenReturn( fileObject );
      when( fileObject.exists() ).thenReturn( true );
      when( fileObject.getName() ).thenReturn( fileName );
      when( fileName.getBaseName() ).thenReturn( "report.prpti" );

      when( pluginManager.getPluginIdForType( "prpti" ) ).thenReturn( "reporting" );

      Response mockResponse = Response.ok().build();
      doReturn( mockResponse ).when( repositoryResource ).getContentGeneratorResponse( any() );

      Response response = repositoryResource.doService( contextId, resourceId );

      assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
    }
  }

  @Test
  public void testDoService_PvfsPath_NoPluginFoundForExtension() throws Exception {
    String contextId = "pvfs\t::file.unknown";
    String resourceId = "viewer";

    // Mock Bowl and KettleVFS
    Bowl bowl = mock( Bowl.class );
    IKettleVFS vfs = mock( IKettleVFS.class );
    FileObject fileObject = mock( FileObject.class );

    doReturn( bowl ).when( repositoryResource ).getBowl();

    try ( MockedStatic<KettleVFS> kettleVFS = Mockito.mockStatic( KettleVFS.class ) ) {
      kettleVFS.when( () -> KettleVFS.getInstance( bowl ) ).thenReturn( vfs );

      when( vfs.getFileObject( "pvfs://file.unknown" ) ).thenReturn( fileObject );
      when( fileObject.exists() ).thenReturn( true );

      when( pluginManager.getPluginIdForType( "unknown" ) ).thenReturn( null );

      Response response = repositoryResource.doService( contextId, resourceId );

      assertEquals( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus() );
    }
  }

  @Test
  public void testDoService_PvfsPath_WithSubdirectories() throws Exception {
    String contextId = "pvfs\t::folder/subfolder/report.prpt";
    String resourceId = "viewer";

    // Mock Bowl and KettleVFS
    Bowl bowl = mock( Bowl.class );
    IKettleVFS vfs = mock( IKettleVFS.class );
    FileObject fileObject = mock( FileObject.class );
    FileName fileName = mock( FileName.class );

    doReturn( bowl ).when( repositoryResource ).getBowl();

    try ( MockedStatic<KettleVFS> kettleVFS = Mockito.mockStatic( KettleVFS.class ) ) {
      kettleVFS.when( () -> KettleVFS.getInstance( bowl ) ).thenReturn( vfs );

      when( vfs.getFileObject( "pvfs://folder/subfolder/report.prpt" ) ).thenReturn( fileObject );
      when( fileObject.exists() ).thenReturn( true );
      when( fileObject.getName() ).thenReturn( fileName );
      when( fileName.getBaseName() ).thenReturn( "report.prpt" );

      when( pluginManager.getPluginIdForType( "prpt" ) ).thenReturn( "reporting" );

      Response mockResponse = Response.ok().build();
      doReturn( mockResponse ).when( repositoryResource ).getContentGeneratorResponse( any() );

      Response response = repositoryResource.doService( contextId, resourceId );

      assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
    }
  }
  // endregion
}
