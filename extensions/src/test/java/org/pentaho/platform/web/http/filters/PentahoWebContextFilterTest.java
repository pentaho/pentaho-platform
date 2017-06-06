/*
 * ******************************************************************************
 *
 * Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
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

package org.pentaho.platform.web.http.filters;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.platform.api.engine.IApplicationContext;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.api.engine.IPentahoRequestContext;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.anyObject;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PentahoWebContextFilterTest {
  private String contextRoot;
  private String fullyQualifiedServerURL;
  private String serverAddress;

  private HttpServletRequest mockRequest;
  private HttpServletResponse mockResponse;
  private java.io.ByteArrayOutputStream mockResponseOutputStream;

  private PentahoWebContextFilter pentahoWebContextFilter;

  @Before
  public void setup() throws IOException, ServletException {
    String scheme = "https";
    String serverName = "di.pentaho.local";
    int port = 9055;

    this.serverAddress = scheme + "://" + serverName + ":" + port;

    this.contextRoot = "/the/context/root/";
    this.fullyQualifiedServerURL = this.serverAddress + this.contextRoot;

    this.mockRequest = mock( HttpServletRequest.class );

    when( this.mockRequest.getRequestURI() ).thenReturn( "/somewhere/" + PentahoWebContextFilter.WEB_CONTEXT_JS );

    when( this.mockRequest.getScheme() ).thenReturn( scheme );
    when( this.mockRequest.getServerName() ).thenReturn( serverName );
    when( this.mockRequest.getServerPort() ).thenReturn( port );

    when( this.mockRequest.getHeader( "referer" ) ).thenReturn( this.serverAddress + "/some/app" );

    this.mockResponse = mock( HttpServletResponse.class );
    this.mockResponseOutputStream = new java.io.ByteArrayOutputStream();
    when( this.mockResponse.getOutputStream() ).thenReturn( new ServletOutputStream() {
      @Override
      public void write( int b ) throws IOException {
        PentahoWebContextFilterTest.this.mockResponseOutputStream.write( b );
      }
    } );

    FilterConfig mockFilterConfig = mock( FilterConfig.class );

    this.pentahoWebContextFilter = spy( new PentahoWebContextFilter() );

    IApplicationContext mockApplicationContext = mock( IApplicationContext.class );
    when( mockApplicationContext.getFullyQualifiedServerURL() ).thenReturn( this.fullyQualifiedServerURL );
    doReturn( mockApplicationContext ).when( this.pentahoWebContextFilter ).getApplicationContext();

    IPentahoRequestContext mockRequestContext = mock( IPentahoRequestContext.class );
    when( mockRequestContext.getContextPath() ).thenReturn( this.contextRoot );
    doReturn( mockRequestContext ).when( this.pentahoWebContextFilter ).getRequestContext();

    IPentahoSession mockSession = mock( IPentahoSession.class );
    doReturn( mockSession ).when( this.pentahoWebContextFilter ).getSession();

    IPluginManager mockPluginManager = mock( IPluginManager.class );
    doReturn( mockPluginManager ).when( this.pentahoWebContextFilter ).getPluginManager();

    this.pentahoWebContextFilter.init( mockFilterConfig );
  }

  @Test
  public void testWebContextCachedWaitSecondVariable() throws Exception {

    ICacheManager cacheManager = Mockito.mock( ICacheManager.class );

    PentahoWebContextFilter filter = new PentahoWebContextFilter();

    PentahoWebContextFilter.cache = cacheManager;

    when( cacheManager.getFromGlobalCache( PentahoSystem.WAIT_SECONDS ) ).thenReturn( null )
      .thenReturn( new Integer( 30 ) );

    filter.printRequireJsCfgStart( new ByteArrayOutputStream() );
    filter.printRequireJsCfgStart( new ByteArrayOutputStream() );

    verify( cacheManager, times( 2 ) ).getFromGlobalCache( eq( PentahoSystem.WAIT_SECONDS ) );
    verify( cacheManager, times( 1 ) ).putInGlobalCache( eq( PentahoSystem.WAIT_SECONDS ), anyObject() );
  }

  @Test
  public void testDoGetWithNoOsgiRequireConfigTrue() throws ServletException, IOException {
    when( this.mockRequest.getParameter( "noOsgiRequireConfig" ) ).thenReturn( "true" );

    this.pentahoWebContextFilter.doFilter( this.mockRequest, this.mockResponse, null );

    final String response = this.mockResponseOutputStream.toString( "UTF-8" );

    assertFalse( this.requirejsManagerInitIsCalled( response, null ) );
  }

  @Test
  public void testDoGetWithNoOsgiRequireConfigFalse() throws ServletException, IOException {
    when( this.mockRequest.getParameter( "noOsgiRequireConfig" ) ).thenReturn( "false" );

    this.pentahoWebContextFilter.doFilter( this.mockRequest, this.mockResponse, null );

    final String response = this.mockResponseOutputStream.toString( "UTF-8" );

    assertTrue( this.requirejsManagerInitIsCalled( response, null ) );
  }

  @Test
  public void testDoGetWithNoOsgiRequireConfigDefault() throws ServletException, IOException {
    this.pentahoWebContextFilter.doFilter( this.mockRequest, this.mockResponse, null );

    final String response = this.mockResponseOutputStream.toString( "UTF-8" );

    assertTrue( this.requirejsManagerInitIsCalled( response, null ) );
  }

  @Test
  public void testDoGetWithFullyQualifiedUrlTrue() throws ServletException, IOException {
    when( this.mockRequest.getParameter( "fullyQualifiedUrl" ) ).thenReturn( "true" );

    this.pentahoWebContextFilter.doFilter( this.mockRequest, this.mockResponse, null );

    final String response = this.mockResponseOutputStream.toString( "UTF-8" );

    assertTrue( this.responseSetsContextPathGlobal( response, this.fullyQualifiedServerURL ) );
    assertTrue( this.requirejsManagerInitIsCalled( response, "true" ) );
  }

  @Test
  public void testDoGetWithFullyQualifiedUrlFalse() throws ServletException, IOException {
    when( this.mockRequest.getParameter( "fullyQualifiedUrl" ) ).thenReturn( "false" );

    this.pentahoWebContextFilter.doFilter( this.mockRequest, this.mockResponse, null );

    final String response = this.mockResponseOutputStream.toString( "UTF-8" );

    assertTrue( this.responseSetsContextPathGlobal( response, this.contextRoot ) );
    assertTrue( this.requirejsManagerInitIsCalled( response, "false" ) );
  }

  @Test
  public void testDoGetWithFullyQualifiedUrlDefaultOutsideReferer() throws ServletException, IOException {
    when( this.mockRequest.getHeader( "referer" ) ).thenReturn( "http://dashboard.somewhere.com/other/app" );

    this.pentahoWebContextFilter.doFilter( this.mockRequest, this.mockResponse, null );

    final String response = this.mockResponseOutputStream.toString( "UTF-8" );

    assertTrue( this.responseSetsContextPathGlobal( response, this.contextRoot ) );
    assertTrue( this.requirejsManagerInitIsCalled( response, null ) );
  }

  @Test
  public void testDoGetWithFullyQualifiedUrlDefaultLocalReferer() throws ServletException, IOException {
    when( this.mockRequest.getHeader( "referer" ) ).thenReturn( this.serverAddress + "/other/app" );

    this.pentahoWebContextFilter.doFilter( this.mockRequest, this.mockResponse, null );

    final String response = this.mockResponseOutputStream.toString( "UTF-8" );

    assertTrue( this.responseSetsContextPathGlobal( response, this.contextRoot ) );
    assertTrue( this.requirejsManagerInitIsCalled( response, null ) );
  }

  private boolean responseSetsContextPathGlobal( String response, String contextRoot ) {
    return response.contains( "var CONTEXT_PATH = '" + contextRoot + "';" );
  }

  private boolean requirejsManagerInitIsCalled( String response, String useFullyQualifiedUrlParameter ) {
    final boolean containsScript = response.contains( "requirejs-manager/js/require-init.js?requirejs=false" );

    final boolean containsFullyQualifiedUrlParameter = response.contains(
      "&fullyQualifiedUrl=" + ( useFullyQualifiedUrlParameter != null ? useFullyQualifiedUrlParameter : "" )
    );

    return containsScript && containsFullyQualifiedUrlParameter == ( useFullyQualifiedUrlParameter != null );
  }
}
