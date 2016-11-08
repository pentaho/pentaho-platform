/*
 * Copyright 2002 - 2016 Pentaho Corporation.  All rights reserved.
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
