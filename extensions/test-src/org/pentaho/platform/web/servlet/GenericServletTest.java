/*
 * ******************************************************************************
 *
 * Copyright (C) 2002-2021 by Hitachi Vantara : http://www.pentaho.com
 *
 * ******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.pentaho.platform.web.servlet;

import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import javax.servlet.ServletConfig;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
