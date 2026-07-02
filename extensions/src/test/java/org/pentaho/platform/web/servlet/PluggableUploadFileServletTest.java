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

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.pentaho.di.core.util.Assert.assertTrue;


public class PluggableUploadFileServletTest {

  PluggableUploadFileServlet pluggableUploadFileServlet;
  HttpServletRequest httpServletRequest;
  HttpServletResponse httpServletResponse;

  @Before
  public void setUp() throws Exception {
    pluggableUploadFileServlet = spy( new PluggableUploadFileServlet() );
    httpServletRequest = mock( HttpServletRequest.class );
    httpServletResponse = mock( HttpServletResponse.class );
  }

  @Test
  public void testXSSUsingPathInfo() throws Exception {
    when( httpServletRequest.getPathInfo() ).thenReturn( "<script>alert('XSS')</script>" );
    IPluginManager pluginManager = mock( IPluginManager.class );
    when( pluginManager.isBeanRegistered( nullable( String.class ) ) ).thenReturn( false );
    PentahoSystem.registerObject( pluginManager );
    try ( StringWriter stringWriter = new StringWriter();
          PrintWriter printWriter = new PrintWriter( stringWriter ) ) {
      when( httpServletResponse.getWriter() ).thenReturn( printWriter );
      pluggableUploadFileServlet.doPost( httpServletRequest, httpServletResponse );
      assertTrue( stringWriter.toString().contains( "&lt;script&gt;alert(&#39;XSS&#39;)&lt;/script&gt;" ) );
    }
  }
}
