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

package org.pentaho.platform.web.servlet;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
