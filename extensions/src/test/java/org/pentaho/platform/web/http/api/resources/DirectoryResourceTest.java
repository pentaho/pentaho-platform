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
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
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
  private static final String PATH = "/parentDir/dirName";
  private static final String ROOTLEVEL_PATH = "/dirName";

  private DirectoryResource directoryResource;

  @Before
  public void setUp() {
    directoryResource = spy( new DirectoryResource() );
    directoryResource.fileService = mock( FileService.class );
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

}
