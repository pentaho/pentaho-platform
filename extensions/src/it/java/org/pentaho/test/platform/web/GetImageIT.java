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
 * Copyright (c) 2002-2021 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.test.platform.web;

import org.apache.commons.lang3.mutable.MutableInt;
import org.junit.Before;
import org.junit.Test;
import org.mockito.stubbing.Answer;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.boot.PlatformInitializationException;
import org.pentaho.platform.web.servlet.GetImage;
import org.pentaho.test.platform.engine.core.MicroPlatform;
import org.pentaho.test.platform.utils.TestResourceLocation;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.matches;

/**
 * Tests for <code>org.pentaho.platform.web.servlet.GetImage</code>.
 * 
 * @author mlowery
 */
@SuppressWarnings( "nls" )
public class GetImageIT {
  private static final String TEST_MIME_TYPE = "test-mime-type";
  private static final String RESOURCE_PARAM = "image";

  private final MicroPlatform mp = new MicroPlatform( TestResourceLocation.TEST_RESOURCES + "/web-servlet-solution" );
  private HttpServletRequest request;
  private HttpServletResponse response;
  private GetImage servlet;

  @Before
  public void setUp() throws PlatformInitializationException, ServletException {
    request = mock( HttpServletRequest.class );
    when( request.getMethod() ).thenReturn( "GET" );

    response = mock( HttpServletResponse.class );

    servlet = spy( new GetImage() );
    final ServletConfig servletConfig = mock( ServletConfig.class );
    final ServletContext servletContext = mock( ServletContext.class );
    when( servletContext.getMimeType( nullable( String.class ) ) ).thenReturn( TEST_MIME_TYPE );
    when( servletConfig.getServletContext() ).thenReturn( servletContext );
    servlet.init( servletConfig );

    mp.start();
  }

  @Test
  public void testGetImage() throws ServletException, IOException {
    final String resource = "picture.png";
    when( request.getParameter( RESOURCE_PARAM ) ).thenReturn( resource );

    final ServletOutputStream outputStream = mock( ServletOutputStream.class );
    final MutableInt fileLength = new MutableInt( 0 );
    doAnswer( (Answer<Void>) invocation -> {
      fileLength.add( (Integer) invocation.getArguments()[2] );
      return null;
    } ).when( outputStream ).write( any( byte[].class ), anyInt(), anyInt() );
    when( response.getOutputStream() ).thenReturn( outputStream );

    servlet.service( request, response );

    verify( response ).setContentType( eq( TEST_MIME_TYPE ) );

    final int expected = new Long( new File( PentahoSystem.getApplicationContext()
      .getSolutionPath( "system/tmp/" + resource ) ).length() ).intValue();
    assertEquals( expected, fileLength.intValue() );
    verify( response ).setContentLength( eq( expected ) );
  }

  @Test
  public void testParentPath() throws ServletException, IOException {
    when( request.getParameter( RESOURCE_PARAM ) ).thenReturn( "../picture.png" );

    servlet.service( request, response );

    verify( response ).sendError( HttpServletResponse.SC_NOT_FOUND );
    verify( servlet ).error( matches( ".*ERROR_0002.*" ) );
  }

  @Test
  public void testParameterEmpty() throws ServletException, IOException {
    when( request.getParameter( RESOURCE_PARAM ) ).thenReturn( null );

    servlet.service( request, response );

    verify( response ).sendError( HttpServletResponse.SC_SERVICE_UNAVAILABLE );
    verify( servlet ).error( matches( ".*ERROR_0001.*" ) );
  }

  @Test
  public void testFileNotExist() throws ServletException, IOException {
    when( request.getParameter( RESOURCE_PARAM ) ).thenReturn( "not_exist.png" );

    servlet.service( request, response );

    verify( response ).sendError( HttpServletResponse.SC_NOT_FOUND );
    verify( servlet ).error( matches( ".*ERROR_0002.*" ) );
  }

}
