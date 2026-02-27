/*! ******************************************************************************
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

import jakarta.ws.rs.core.Response;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;

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
}
