/*!
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
 * Copyright (c) 2021 Hitachi Vantara. All rights reserved.
 */

package org.pentaho.platform.web.gwt.rpc;

import com.google.gwt.user.server.rpc.SerializationPolicy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.stubbing.Answer;
import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.pentaho.platform.api.engine.IServiceConfig;
import org.pentaho.platform.api.engine.IServiceManager;
import org.pentaho.platform.api.engine.ServiceException;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.pluginmgr.PluginUtil;
import org.pentaho.platform.web.gwt.rpc.util.ThrowingSupplier;
import org.pentaho.platform.web.servlet.GwtRpcProxyException;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.powermock.api.mockito.PowerMockito.doAnswer;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith( PowerMockRunner.class )
@PrepareForTest( {
  PentahoSystem.class,
  PluginUtil.class,
  PentahoSessionHolder.class,
  LogFactory.class,
  AbstractGwtRpc.class
} )
public class PluginGwtRpcTest {

  private static Log loggerMock;

  private IServiceManager setupServiceManagerMock( String serviceKey, Object serviceBean ) {
    IServiceManager serviceManagerMock = mock( IServiceManager.class );

    IServiceConfig serviceConfigMock = mock( IServiceConfig.class );
    when( serviceManagerMock.getServiceConfig( "gwt", serviceKey ) ).thenReturn( serviceConfigMock );

    mockStatic( PentahoSystem.class );
    when( PentahoSystem.get( eq( IServiceManager.class ), any() ) ).thenReturn( serviceManagerMock );

    try {
      when( serviceManagerMock.getServiceBean( "gwt", serviceKey ) ).thenReturn( serviceBean );
    } catch ( ServiceException e ) {
      // Does not happen.
    }

    return serviceManagerMock;
  }

  private HttpServletRequest setupHttpRequest( String pathInfo ) {
    HttpServletRequest httpRequestMock = mock( HttpServletRequest.class );
    when( httpRequestMock.getPathInfo() ).thenReturn( pathInfo );

    return httpRequestMock;
  }

  @BeforeClass
  public static void beforeAll() {

    mockStatic( LogFactory.class );

    // Must use this lazy form of mocking `LogFactory.log` because the literal reference to
    // PentahoSessionHolder.class would trigger that class's initialization, which would request for the logger.

    loggerMock = mock( Log.class );
    Log sessionLogger = mock( Log.class );

    doAnswer( (Answer<Log>) invocationOnMock -> {

      Class<?> logClass = invocationOnMock.getArgumentAt( 0, Class.class );

      if ( logClass.equals( PluginGwtRpc.class ) ) {
        return loggerMock;
      }

      if ( logClass.equals( PentahoSessionHolder.class ) ) {
        return sessionLogger;
      }

      return mock( Log.class );
    } ).when( LogFactory.class );
    LogFactory.getLog( any( Class.class ) );

    // ---

    // Static initialization of class requires LogFactory already configured, above.
    mockStatic( PentahoSessionHolder.class );
    when( PentahoSessionHolder.getSession() ).thenReturn( null );
  }

  @After
  public void afterEach() {
    reset( loggerMock );
  }

  // region HttpRequest
  @Test
  public void testGetServletRequest() {
    String pathInfo = "/serviceName";

    HttpServletRequest httpRequestMock = setupHttpRequest( pathInfo );

    PluginGwtRpc gwtRpc = new PluginGwtRpc( httpRequestMock );

    HttpServletRequest result = gwtRpc.getServletRequest();

    assertEquals( httpRequestMock, result );
  }
  // endregion

  // region Target
  @Test
  public void testResolveTargetSuccessfully() {
    String serviceKey = "serviceName";
    String pathInfo = "/serviceName";
    Object serviceTarget = new Object();

    setupServiceManagerMock( serviceKey, serviceTarget );

    HttpServletRequest httpRequestMock = setupHttpRequest( pathInfo );

    PluginGwtRpc gwtRpc = new PluginGwtRpc( httpRequestMock );

    Object result = gwtRpc.resolveTarget();

    assertEquals( serviceTarget, result );
  }

  @Test( expected = GwtRpcProxyException.class )
  public void testResolveTargetThrowsWhenNotRegisteredServiceKey() {
    String serviceKey = "serviceName";
    String pathInfo = "/serviceName";
    Object serviceTarget = null;

    @SuppressWarnings( "ConstantConditions" )
    IServiceManager serviceManagerMock = setupServiceManagerMock( serviceKey, serviceTarget );

    // No Configuration
    when( serviceManagerMock.getServiceConfig( "gwt", serviceKey ) ).thenReturn( null );

    HttpServletRequest httpRequestMock = setupHttpRequest( pathInfo );

    PluginGwtRpc gwtRpc = new PluginGwtRpc( httpRequestMock );
    gwtRpc.resolveTarget();
  }

  @Test( expected = GwtRpcProxyException.class )
  public void testResolveTargetThrowsWhenServiceResolveThrowsServiceException() {
    String serviceKey = "serviceName";
    String pathInfo = "/serviceName";
    Object serviceTarget = null;

    @SuppressWarnings( "ConstantConditions" )
    IServiceManager serviceManagerMock = setupServiceManagerMock( serviceKey, serviceTarget );
    ServiceException error = new ServiceException();
    try {
      when( serviceManagerMock.getServiceBean( "gwt", serviceKey ) ).thenThrow( error );
    } catch ( ServiceException ex ) {
      // noop
    }

    HttpServletRequest httpRequestMock = setupHttpRequest( pathInfo );

    PluginGwtRpc gwtRpc = new PluginGwtRpc( httpRequestMock );
    gwtRpc.resolveTarget();
  }

  @Test
  public void testResolveTargetWithCompositeServiceNameUsesLastSegmentAsKey() {
    String serviceKey = "Name";
    String pathInfo = "/service/Name";
    Object serviceTarget = new Object();

    setupServiceManagerMock( serviceKey, serviceTarget );

    HttpServletRequest httpRequestMock = setupHttpRequest( pathInfo );

    PluginGwtRpc gwtRpc = new PluginGwtRpc( httpRequestMock );

    Object result = gwtRpc.resolveTarget();

    assertEquals( serviceTarget, result );
  }
  // endregion

  // region Serialization Policy
  @Test
  public void testLoadSerializationPolicySupportsPluginBean() {
    String pathInfo = "/serviceName";
    String pluginId = "data-access";
    String moduleContextPath = "/content/data-access/resources/gwt/";
    String strongName = "ABC";

    // ---

    mockStatic( PluginUtil.class );
    when( PluginUtil.getPluginIdFromPath( moduleContextPath ) ).thenReturn( pluginId );

    // ---

    SerializationPolicy pluginPolicyMock = mock( SerializationPolicy.class );

    mockStatic( PentahoSystem.class );
    doAnswer( invocationOnMock -> {
      @SuppressWarnings( "unchecked" )
      Map<String, String> props = (Map<String, String>) invocationOnMock.getArguments()[ 2 ];
      if ( pluginId.equals( props.get( "plugin" ) ) ) {
        return pluginPolicyMock;
      }

      fail();
      return mock( SerializationPolicy.class );
    } ).when( PentahoSystem.class );
    PentahoSystem.get( eq( SerializationPolicy.class ), eq( null ), anyMapOf( String.class, String.class ) );

    // ---

    HttpServletRequest httpRequestMock = setupHttpRequest( pathInfo );
    PluginGwtRpc gwtRpc = new PluginGwtRpc( httpRequestMock );

    SerializationPolicy result = gwtRpc.loadSerializationPolicy( moduleContextPath, strongName );

    assertEquals( pluginPolicyMock, result );
  }

  @Test
  public void testLoadSerializationPolicyLogsErrorAndReturnsNullIfNoPluginClassLoader() {
    String pathInfo = "/serviceName";
    String pluginId = "data-access";
    String moduleContextPath = "/content/data-access/resources/gwt/";
    String strongName = "ABC";

    // ---

    mockStatic( PluginUtil.class );
    when( PluginUtil.getPluginIdFromPath( moduleContextPath ) ).thenReturn( pluginId );

    // ----

    // There is no configured SP bean.
    mockStatic( PentahoSystem.class );
    when( PentahoSystem.get( eq( SerializationPolicy.class ), eq( null ), anyMapOf( String.class, String.class ) ) )
      .thenReturn( null );

    // ---

    when( PluginUtil.getClassLoaderForService( moduleContextPath ) ).thenReturn( null );

    // ---

    HttpServletRequest httpRequestMock = setupHttpRequest( pathInfo );
    PluginGwtRpc gwtRpc = new PluginGwtRpc( httpRequestMock );

    SerializationPolicy result = gwtRpc.loadSerializationPolicy( moduleContextPath, strongName );

    assertNull( result );

    verify( loggerMock, times( 1 ) ).error( anyString() );
  }

  @Test
  public void testLoadSerializationPolicyLogsErrorAndReturnsNullIfNoPluginResource() {
    String pathInfo = "/serviceName";
    String pluginId = "data-access";
    String moduleContextPath = "/content/data-access/resources/gwt/";
    String strongName = "ABC";
    String serializationPolicyFilename = strongName + ".gwt.rpc";

    // ---

    mockStatic( PluginUtil.class );
    when( PluginUtil.getPluginIdFromPath( moduleContextPath ) ).thenReturn( pluginId );

    // ----

    // There is no configured SP bean.
    mockStatic( PentahoSystem.class );
    when( PentahoSystem.get( eq( SerializationPolicy.class ), eq( null ), anyMapOf( String.class, String.class ) ) )
      .thenReturn( null );

    // ---

    ClassLoader pluginClassLoader = mock( ClassLoader.class );
    when( PluginUtil.getClassLoaderForService( moduleContextPath ) ).thenReturn( pluginClassLoader );

    // ---

    // No serialization policy resource found.
    IPluginResourceLoader resLoaderMock = mock( IPluginResourceLoader.class );
    when( resLoaderMock.findResources( pluginClassLoader, serializationPolicyFilename ) )
      .thenReturn( Collections.emptyList() );

    when( PentahoSystem.get( eq( IPluginResourceLoader.class ), eq( null ) ) ).thenReturn( resLoaderMock );

    // ---

    HttpServletRequest httpRequestMock = setupHttpRequest( pathInfo );
    PluginGwtRpc gwtRpc = new PluginGwtRpc( httpRequestMock );

    SerializationPolicy result = gwtRpc.loadSerializationPolicy( moduleContextPath, strongName );

    assertNull( result );

    verify( loggerMock, times( 1 ) ).error( anyString() );
  }

  @Test
  public void testLoadSerializationPolicyLogsWarningAndReturnsFirstIfMoreThanOnePluginResource()
    throws MalformedURLException {
    String pathInfo = "/serviceName";
    String pluginId = "data-access";
    String moduleContextPath = "/content/data-access/resources/gwt/";
    String strongName = "ABC";
    String policyFilename = strongName + ".gwt.rpc";
    URL policy1Url = new URL( "file:///resources/gwt/a/" + policyFilename );
    URL policy2Url = new URL( "file:///resources/gwt/b/" + policyFilename );

    // ---

    mockStatic( PluginUtil.class );
    when( PluginUtil.getPluginIdFromPath( moduleContextPath ) ).thenReturn( pluginId );

    // ----

    // There is no configured SP bean.
    mockStatic( PentahoSystem.class );
    when( PentahoSystem.get( eq( SerializationPolicy.class ), eq( null ), anyMapOf( String.class, String.class ) ) )
      .thenReturn( null );

    // ---

    ClassLoader pluginClassLoader = mock( ClassLoader.class );
    when( PluginUtil.getClassLoaderForService( moduleContextPath ) ).thenReturn( pluginClassLoader );

    // ---

    // More than one serialization policy resource found.
    IPluginResourceLoader resLoaderMock = mock( IPluginResourceLoader.class );
    when( resLoaderMock.findResources( pluginClassLoader, policyFilename ) )
      .thenReturn( Arrays.asList( policy1Url, policy2Url ) );

    when( PentahoSystem.get( eq( IPluginResourceLoader.class ), eq( null ) ) ).thenReturn( resLoaderMock );

    // ---

    InputStream pluginPolicyInputStreamMock = mock( InputStream.class );

    // Spying requires a non-final class. Lambda expression is not suitable.
    @SuppressWarnings( "Convert2Lambda" )
    ThrowingSupplier<InputStream, IOException> pluginPolicyInputStreamSupplierSpy =
      spy( new ThrowingSupplier<InputStream, IOException>() {
        @Override public InputStream get() {
          return pluginPolicyInputStreamMock;
        }
      } );

    // ---

    SerializationPolicy pluginPolicyMock = mock( SerializationPolicy.class );
    mockStatic( AbstractGwtRpc.class );
    when( AbstractGwtRpc.loadSerializationPolicyFromInputStream( pluginPolicyInputStreamSupplierSpy, policyFilename ) )
      .thenReturn( pluginPolicyMock );

    // ---

    HttpServletRequest httpRequestMock = setupHttpRequest( pathInfo );
    PluginGwtRpc gwtRpcSpy = spy( new PluginGwtRpc( httpRequestMock ) );

    doReturn( pluginPolicyInputStreamSupplierSpy ).when( gwtRpcSpy ).getInputStreamSupplier( policy1Url );

    // ---

    SerializationPolicy result = gwtRpcSpy.loadSerializationPolicy( moduleContextPath, strongName );

    // ---

    assertEquals( pluginPolicyMock, result );

    verify( loggerMock, times( 1 ) ).warn( anyString() );
  }

  @Test
  public void testLoadSerializationPolicyWhenPluginResource() throws MalformedURLException {
    String pathInfo = "/serviceName";
    String pluginId = "data-access";
    String moduleContextPath = "/content/data-access/resources/gwt/";
    String strongName = "ABC";
    String policyFilename = strongName + ".gwt.rpc";
    URL policy1Url = new URL( "file:///resources/gwt/a/" + policyFilename );

    // ---

    mockStatic( PluginUtil.class );
    when( PluginUtil.getPluginIdFromPath( moduleContextPath ) ).thenReturn( pluginId );

    // ----

    // There is no configured SP bean.
    mockStatic( PentahoSystem.class );
    when( PentahoSystem.get( eq( SerializationPolicy.class ), eq( null ), anyMapOf( String.class, String.class ) ) )
      .thenReturn( null );

    // ---

    ClassLoader pluginClassLoader = mock( ClassLoader.class );
    when( PluginUtil.getClassLoaderForService( moduleContextPath ) ).thenReturn( pluginClassLoader );

    // ---

    // More than one serialization policy resource found.
    IPluginResourceLoader resLoaderMock = mock( IPluginResourceLoader.class );
    when( resLoaderMock.findResources( pluginClassLoader, policyFilename ) )
      .thenReturn( Collections.singletonList( policy1Url ) );

    when( PentahoSystem.get( eq( IPluginResourceLoader.class ), eq( null ) ) ).thenReturn( resLoaderMock );

    // ---

    InputStream pluginPolicyInputStreamMock = mock( InputStream.class );

    // Spying requires a non-final class. Lambda expression is not suitable.
    @SuppressWarnings( "Convert2Lambda" )
    ThrowingSupplier<InputStream, IOException> pluginPolicyInputStreamSupplierSpy =
      spy( new ThrowingSupplier<InputStream, IOException>() {
        @Override public InputStream get() {
          return pluginPolicyInputStreamMock;
        }
      } );

    // ---

    SerializationPolicy pluginPolicyMock = mock( SerializationPolicy.class );
    mockStatic( AbstractGwtRpc.class );
    when( AbstractGwtRpc.loadSerializationPolicyFromInputStream( pluginPolicyInputStreamSupplierSpy, policyFilename ) )
      .thenReturn( pluginPolicyMock );

    // ---

    HttpServletRequest httpRequestMock = setupHttpRequest( pathInfo );
    PluginGwtRpc gwtRpcSpy = spy( new PluginGwtRpc( httpRequestMock ) );

    doReturn( pluginPolicyInputStreamSupplierSpy ).when( gwtRpcSpy ).getInputStreamSupplier( policy1Url );

    // ---

    SerializationPolicy result = gwtRpcSpy.loadSerializationPolicy( moduleContextPath, strongName );

    // ---

    assertEquals( pluginPolicyMock, result );

    verify( loggerMock, times( 0 ) ).error( anyString() );
    verify( loggerMock, times( 0 ) ).warn( anyString() );
  }

  // endregion
}
