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

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
