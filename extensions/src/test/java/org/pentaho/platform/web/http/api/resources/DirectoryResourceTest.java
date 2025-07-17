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

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Response;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.web.http.api.resources.services.FileService;

public class DirectoryResourceTest {
  private static final String PATH = "/home/dirName";
  private static final String PATH_CONTROL_CHARACTER = ":home:Create Control Character \u0017 Folder";
  private static final String PATH_SPECIAL_CHARACTERS = ":home:éÉèÈçÇºªüÜ@£§ folder";
  private static final String PATH_JAPANESE_CHARACTERS = ":home:キャラクター";
  private static final String ROOTLEVEL_PATH = "/dirName";

  private DirectoryResource directoryResource;

  @Before
  public void setUp() {
    directoryResource = spy( DirectoryResource.class );
    directoryResource.fileService = spy( FileService.class );
    directoryResource.httpServletRequest = mock( HttpServletRequest.class );
  }

  @After
  public void tearDown() {
    directoryResource = null;
  }

  @Test
  public void testCreateDirs_Ok() throws Exception {
    doReturn( true ).when( directoryResource.fileService ).doCreateDirSafe( PATH );

    Response testResponse = directoryResource.createDirs( PATH );

    assertEquals( Response.Status.OK.getStatusCode(), testResponse.getStatus() );
    verify( directoryResource.fileService, times( 1 ) ).doCreateDirSafe( PATH );
  }

  @Test
  public void testCreateDirs_Conflict() throws Exception {
    doReturn( false ).when( directoryResource.fileService ).doCreateDirSafe( PATH );

    Response testResponse = directoryResource.createDirs( PATH );

    assertEquals( Response.Status.CONFLICT.getStatusCode(), testResponse.getStatus() );
    verify( directoryResource.fileService, times( 1 ) ).doCreateDirSafe( PATH );
  }

  @Test
  public void testCreateDirs_ServerError_0() throws Exception {
    Response testResponse = directoryResource.createDirs( null );

    assertEquals( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), testResponse.getStatus() );
    verify( directoryResource.fileService, times( 0 ) ).doCreateDirSafe( nullable( String.class ) );
  }

  @Test
  public void testCreateDirs_ServerError_1() throws Exception {
    doThrow( new RuntimeException() ).when( directoryResource.fileService ).doCreateDirSafe( PATH );

    Response testResponse = directoryResource.createDirs( PATH );

    assertEquals( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), testResponse.getStatus() );
    verify( directoryResource.fileService, times( 1 ) ).doCreateDirSafe( PATH );
  }

  @Test
  public void testCreateDirs_Forbidden() throws Exception {
    doReturn( false ).when( directoryResource.fileService ).doCreateDirSafe( nullable( String.class ) );

    Response testResponse = directoryResource.createDirs( ROOTLEVEL_PATH );

    assertEquals( Response.Status.FORBIDDEN.getStatusCode(), testResponse.getStatus() );
    verify( directoryResource.fileService, times( 0 ) ).doCreateDirSafe( nullable( String.class ) );
  }

  @Test
  public void testCreateDirs_Forbidden_ControlCharactersFound() {
    doReturn( new StringBuffer() ).when( directoryResource.fileService ).doGetReservedChars();
    Response testResponse = directoryResource.createDirs( PATH_CONTROL_CHARACTER );

    assertEquals( Response.Status.FORBIDDEN.getStatusCode(), testResponse.getStatus() );
    assertEquals( testResponse.getEntity(), "containsIllegalCharacters" );
  }

  @Test
  public void testCreateDirs_Special_Characters() throws Exception {
    doReturn( true ).when( directoryResource.fileService ).doCreateDirSafe( PATH_SPECIAL_CHARACTERS );
    Response testResponse = directoryResource.createDirs( PATH_SPECIAL_CHARACTERS );

    assertEquals( Response.Status.OK.getStatusCode(), testResponse.getStatus() );
    verify( directoryResource.fileService, times( 1 ) ).doCreateDirSafe( PATH_SPECIAL_CHARACTERS );
  }

  @Test
  public void testCreateDirs_Forbidden_Japanese_Characters() throws Exception {
    doReturn( true ).when( directoryResource.fileService ).doCreateDirSafe( PATH_JAPANESE_CHARACTERS );
    Response testResponse = directoryResource.createDirs( PATH_JAPANESE_CHARACTERS );

    assertEquals( Response.Status.OK.getStatusCode(), testResponse.getStatus() );
    verify( directoryResource.fileService, times( 1 ) ).doCreateDirSafe( PATH_JAPANESE_CHARACTERS );
  }

  @Test
  public void testIsVisibleWhenFolderIsHidden() {
    doReturn( "false" ).when( directoryResource.fileService ).doGetIsVisible( "/home/suzy" );
    Response testResponse = directoryResource.isDirVisible( "/home/suzy" );

    assertEquals( Response.Status.OK.getStatusCode(), testResponse.getStatus() );
    assertEquals( "false", testResponse.getEntity() );
    verify( directoryResource.fileService, times( 1 ) ).doesExist( "/home/suzy" );
  }

  @Test
  public void testIsVisibleWhenFolderIsVisible() {
    doReturn( true ).when( directoryResource.fileService ).doesExist( "/home/joe" );
    doReturn( true ).when( directoryResource.fileService ).isFolder( "/home/joe" );
    doReturn( "true" ).when( directoryResource.fileService ).doGetIsVisible( "/home/joe" );
    Response response = directoryResource.isDirVisible( "/home/joe" );

    assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
    assertEquals( "true", response.getEntity() );
    verify( directoryResource.fileService, times( 1 ) ).doGetIsVisible( "/home/joe" );
    verify( directoryResource.fileService, times( 1 ) ).isFolder( "/home/joe" );
    verify( directoryResource.fileService, times( 1 ) ).doesExist( "/home/joe" );
  }

  @Test
  public void testGetDefaultLocation() {
    doReturn( "/public" ).when( directoryResource.fileService ).doGetDefaultLocation( "/home/suzy" );
    Response testResponse = directoryResource.getDefaultLocation( "/home/suzy" );

    assertEquals( Response.Status.OK.getStatusCode(), testResponse.getStatus() );
    assertEquals( "/public", testResponse.getEntity() );
    verify( directoryResource.fileService, times( 1 ) ).doGetDefaultLocation( "/home/suzy" );

    doReturn( "/home/joe" ).when( directoryResource.fileService ).doGetDefaultLocation( "/home/joe" );
    Response response = directoryResource.getDefaultLocation( "/home/joe" );

    assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
    assertEquals( "/home/joe", response.getEntity() );
    verify( directoryResource.fileService, times( 1 ) ).doGetDefaultLocation( "/home/joe" );
  }
}
