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
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.api.engine.IPluginManager;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.pentaho.platform.web.servlet.GenericServlet.CACHE_FILE;

@RunWith( MockitoJUnitRunner.class )
public class GenericServletTest {
  private GenericServlet genericServlet;
  private HttpServletRequest request;
  private IPluginManager pluginManager;
  private ICacheManager cacheManager;

  @Before
  public void setUp() {
    genericServlet = spy( new GenericServlet() );
    request = mock( HttpServletRequest.class );
    pluginManager = mock( IPluginManager.class );
    cacheManager = mock( ICacheManager.class );

    doReturn( pluginManager ).when( genericServlet ).getPluginManager( request );
    doReturn( cacheManager ).when( genericServlet ).getCacheManager();
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
    when( pluginManager.getServicePlugin( "/my-plugin/resource.js" ) ).thenReturn( "my-plugin" );
    when( pluginManager.isStaticResource( "/my-plugin/resource.js" ) ).thenReturn( true );
    when( pluginManager.getStaticResource( "/my-plugin/resource.js" ) ).thenReturn( mock( InputStream.class ) );

    boolean result = genericServlet.isStaticResource( request );
    assertTrue( result );
  }

  @Test
  @SuppressWarnings( { "deprecation" } )
  public void testIsStaticResource_True_ReadFromCache() {
    when( request.getServletPath() ).thenReturn( "/content" );
    when( request.getPathInfo() ).thenReturn( "/my-plugin/resource.js" );
    when( pluginManager.getServicePlugin( "/my-plugin/resource.js" ) ).thenReturn( "my-plugin" );
    when( pluginManager.isStaticResource( "/my-plugin/resource.js" ) ).thenReturn( true );

    when( pluginManager.getPluginSetting( eq( "my-plugin" ), eq( "settings/cache" ), any() ) )
      .thenReturn( "true" );
    when( cacheManager.getFromRegionCache( CACHE_FILE, "/my-plugin/resource.js" ) ).thenReturn(
      mock( ByteArrayOutputStream.class ) );

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
    when( pluginManager.getServicePlugin( "/my-plugin/resource.js" ) ).thenReturn( null );

    boolean result = genericServlet.isStaticResource( request );
    assertFalse( result );
  }

  @Test
  @SuppressWarnings( {"deprecation"} )
  public void testIsStaticResource_False_NotAStaticResourceUrl() {
    when( request.getServletPath() ).thenReturn( "/content" );
    when( request.getPathInfo() ).thenReturn( "/my-plugin/resource.js" );
    when( pluginManager.getServicePlugin( "/my-plugin/resource.js" ) ).thenReturn( "my-plugin" );
    when( pluginManager.isStaticResource( "/my-plugin/resource.js" ) ).thenReturn( false );

    boolean result = genericServlet.isStaticResource( request );
    assertFalse( result );
  }

  @Test
  @SuppressWarnings( { "deprecation" } )
  public void testIsStaticResource_False_NotAnExistingStaticResource() {
    when( request.getServletPath() ).thenReturn( "/content" );
    when( request.getPathInfo() ).thenReturn( "/my-plugin/resource.js" );
    when( pluginManager.getServicePlugin( "/my-plugin/resource.js" ) ).thenReturn( "my-plugin" );
    when( pluginManager.isStaticResource( "/my-plugin/resource.js" ) ).thenReturn( true );
    when( pluginManager.getStaticResource( "/my-plugin/resource.js" ) ).thenReturn( null );

    boolean result = genericServlet.isStaticResource( request );
    assertFalse( result );
  }

  @Test
  @SuppressWarnings( { "deprecation" } )
  public void testIsStaticResource_False_NotAnExistingStaticResourceAndCacheEnabled() {
    when( request.getServletPath() ).thenReturn( "/content" );
    when( request.getPathInfo() ).thenReturn( "/my-plugin/resource.js" );
    when( pluginManager.getServicePlugin( "/my-plugin/resource.js" ) ).thenReturn( "my-plugin" );
    when( pluginManager.isStaticResource( "/my-plugin/resource.js" ) ).thenReturn( true );

    when( pluginManager.getPluginSetting( eq( "my-plugin" ), eq( "settings/cache" ), any() ) )
      .thenReturn( "true" );
    when( cacheManager.getFromRegionCache( CACHE_FILE, "/my-plugin/resource.js" ) ).thenReturn( null );

    when( pluginManager.getStaticResource( "/my-plugin/resource.js" ) ).thenReturn( null );

    boolean result = genericServlet.isStaticResource( request );
    assertFalse( result );
  }

  // endregion
}
