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
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.platform.api.engine.IPluginManager;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )
public class GenericServletTest {
  private GenericServlet genericServlet;
  private HttpServletRequest request;
  private IPluginManager pluginManager;

  @Before
  public void setUp() {
    genericServlet = spy( new GenericServlet() );
    request = mock( HttpServletRequest.class );
    pluginManager = mock( IPluginManager.class );
    doReturn( pluginManager ).when( genericServlet ).getPluginManager( request );
  }

  /**
   * Created by dstepanov on 01/06/17.
   */
  @Test
  public void testDoGet() throws Exception {
    when( request.getPathInfo() ).thenReturn( "/somePath" );

    HttpServletResponse response = mock( HttpServletResponse.class );
    ServletOutputStream servletOutputStream = mock( ServletOutputStream.class );
    when( response.getOutputStream() ).thenReturn( servletOutputStream );
    doNothing().when( servletOutputStream ).write( byte[].class.cast( any() ) );

    doReturn( null ).when( genericServlet ).getPluginManager( request );

    InOrder orderInvocation = inOrder( request );
    genericServlet.doGet( request, response );
    orderInvocation.verify( request ).getServletPath();
    orderInvocation.verify( request ).getPathInfo();
    orderInvocation.verify( request ).getParameterMap();
    orderInvocation.verify( request ).getParameter( nullable( String.class ) );
    orderInvocation.verify( request ).getInputStream();
  }

  // region isStaticResource( request ) tests
  @Test
  @SuppressWarnings( {"deprecation"} )
  public void testIsStaticResource_True() {
    when( request.getServletPath() ).thenReturn( "/content" );
    when( request.getPathInfo() ).thenReturn( "/my-plugin/resource.js" );
    when( pluginManager.getServicePlugin( "my-plugin/resource.js" ) ).thenReturn( "plugin" );
    when( pluginManager.isStaticResource( "my-plugin/resource.js" ) ).thenReturn( true );

    boolean result = genericServlet.isStaticResource( request );
    assertTrue( result );
  }

  @Test
  public void testIsStaticResource_False_NotSameServletPath() {
    when( request.getServletPath() ).thenReturn( "/other" );
    when( request.getPathInfo() ).thenReturn( "/my-plugin/resource.js" );

    boolean result = genericServlet.isStaticResource( request );
    assertFalse( result );
  }

  @Test
  public void testIsStaticResource_False_NullPathInfo() {
    when( request.getPathInfo() ).thenReturn( null );

    boolean result = genericServlet.isStaticResource( request );
    assertFalse( result );
  }

  @Test
  public void testIsStaticResource_False_EmptyPathInfo() {
    when( request.getPathInfo() ).thenReturn( "" );

    boolean result = genericServlet.isStaticResource( request );
    assertFalse( result );
  }

  @Test
  public void testIsStaticResource_False_RootPathInfo() {
    when( request.getServletPath() ).thenReturn( "/content" );
    when( request.getPathInfo() ).thenReturn( "/" );

    boolean result = genericServlet.isStaticResource( request );
    assertFalse( result );
  }

  @Test
  public void testIsStaticResource_False_NoPluginManager() {
    doReturn( null ).when( genericServlet ).getPluginManager( request );
    when( request.getServletPath() ).thenReturn( "/content" );
    when( request.getPathInfo() ).thenReturn( "/my-plugin/resource.js" );

    boolean result = genericServlet.isStaticResource( request );
    assertFalse( result );
  }

  @Test
  @SuppressWarnings( {"deprecation"} )
  public void testIsStaticResource_False_NoAssociatedPlugin() {
    when( request.getServletPath() ).thenReturn( "/content" );
    when( request.getPathInfo() ).thenReturn( "/my-plugin/resource.js" );
    when( pluginManager.getServicePlugin( "my-plugin/resource.js" ) ).thenReturn( null );

    boolean result = genericServlet.isStaticResource( request );
    assertFalse( result );
  }

  @Test
  @SuppressWarnings( {"deprecation"} )
  public void testIsStaticResource_False_NotAStaticResource() {
    when( request.getServletPath() ).thenReturn( "/content" );
    when( request.getPathInfo() ).thenReturn( "/my-plugin/resource.js" );
    when( pluginManager.getServicePlugin( "my-plugin/resource.js" ) ).thenReturn( "my-plugin" );
    when( pluginManager.isStaticResource( "my-plugin/resource.js" ) ).thenReturn( false );

    boolean result = genericServlet.isStaticResource( request );
    assertFalse( result );
  }
  // endregion
}
