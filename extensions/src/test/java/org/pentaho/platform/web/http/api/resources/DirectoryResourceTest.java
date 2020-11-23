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
 * Copyright (c) 2002-2020 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.web.http.api.resources;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

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
    verify( directoryResource.fileService, times( 0 ) ).doCreateDirSafe( anyString() );
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
    doReturn( false ).when( directoryResource.fileService ).doCreateDirSafe( anyString() );

    Response testResponse = directoryResource.createDirs( ROOTLEVEL_PATH );

    assertEquals( Response.Status.FORBIDDEN.getStatusCode(), testResponse.getStatus() );
    verify( directoryResource.fileService, times( 0 ) ).doCreateDirSafe( anyString() );
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
  public void testIsVisibleWhenFolderIsHidden() throws Exception {
    doReturn( "false" ).when( directoryResource.fileService ).doGetIsVisible( "/home/suzy" );
    Response testResponse = directoryResource.isDirVisible( "/home/suzy");

    assertEquals( Response.Status.OK.getStatusCode(), testResponse.getStatus() );
    assertEquals( "false", testResponse.getEntity() );
    verify( directoryResource.fileService, times( 1 ) ).doesExist( "/home/suzy" );
  }

  @Test
  public void testIsVisibleWhenFolderIsVisible() throws Exception {
    doReturn( true ).when( directoryResource.fileService ).doesExist( "/home/joe" );
    doReturn( true ).when( directoryResource.fileService ).isFolder( "/home/joe" );
    doReturn( "true" ).when( directoryResource.fileService ).doGetIsVisible( "/home/joe" );
    Response response = directoryResource.isDirVisible( "/home/joe");

    assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
    assertEquals( "true", response.getEntity() );
    verify( directoryResource.fileService, times( 1 ) ).doGetIsVisible( "/home/joe" );
    verify( directoryResource.fileService, times( 1 ) ).isFolder( "/home/joe" );
    verify( directoryResource.fileService, times( 1 ) ).doesExist( "/home/joe" );
  }

  @Test
  public void testGetDefaultLocation() throws Exception {
    doReturn( "/public" ).when( directoryResource.fileService ).doGetDefaultLocation( "/home/suzy" );
    Response testResponse = directoryResource.getDefaultLocation( "/home/suzy");

    assertEquals( Response.Status.OK.getStatusCode(), testResponse.getStatus() );
    assertEquals( "/public", testResponse.getEntity() );
    verify( directoryResource.fileService, times( 1 ) ).doGetDefaultLocation( "/home/suzy" );

    doReturn( "/home/joe" ).when( directoryResource.fileService ).doGetDefaultLocation( "/home/joe" );
    Response response = directoryResource.getDefaultLocation( "/home/joe");

    assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
    assertEquals( "/home/joe", response.getEntity() );
    verify( directoryResource.fileService, times( 1 ) ).doGetDefaultLocation( "/home/joe" );
  }
}
