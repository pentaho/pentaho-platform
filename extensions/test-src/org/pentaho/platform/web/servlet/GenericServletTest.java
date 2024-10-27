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


package org.pentaho.platform.web.servlet;

import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Created by dstepanov on 01/06/17.
 */
public class GenericServletTest {
  @Test
  public void doGetTest() throws Exception {
    GenericServlet gs = Mockito.spy( new GenericServlet() );
    ServletConfig sc = Mockito.mock( ServletConfig.class );
    Mockito.doReturn( sc ).when( gs ).getServletConfig();
    Mockito.when( sc.getInitParameter( "showDeprecationMessage" ) ).thenReturn( "false" );
    HttpServletRequest req = Mockito.mock( HttpServletRequest.class );
    Mockito.when( req.getPathInfo() ).thenReturn( "/somePath" );
    HttpServletResponse resp = Mockito.mock( HttpServletResponse.class );
    ServletOutputStream sout = Mockito.mock( ServletOutputStream.class );
    Mockito.when( resp.getOutputStream() ).thenReturn( sout );
    Mockito.doNothing().when( sout ).write( byte[].class.cast( Mockito.any() ) );

    InOrder orderInvocation = Mockito.inOrder( req );
    gs.doGet( req, resp );
    orderInvocation.verify( req ).getServletPath();
    orderInvocation.verify( req ).getPathInfo();
    orderInvocation.verify( req ).getParameterMap();
    orderInvocation.verify( req ).getParameter( Mockito.nullable( String.class ) );
    orderInvocation.verify( req ).getInputStream();
  }

}
