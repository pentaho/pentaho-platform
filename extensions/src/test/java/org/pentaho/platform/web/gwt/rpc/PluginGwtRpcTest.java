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
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.pentaho.platform.api.engine.IServiceConfig;
import org.pentaho.platform.api.engine.IServiceManager;
import org.pentaho.platform.api.engine.ServiceException;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.pluginmgr.PluginUtil;
import org.pentaho.platform.web.gwt.rpc.util.ThrowingSupplier;
import org.pentaho.platform.web.servlet.GwtRpcProxyException;

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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@RunWith( MockitoJUnitRunner.class )
public class PluginGwtRpcTest {

  private static Log loggerMock;
  private static Log sessionLogger;
  private static MockedStatic<LogFactory> logFactoryMock;
  private static MockedStatic<PentahoSessionHolder> pentahoSessionHolderMock;

  private MockedStatic<PentahoSystem> pentahoSystemMock;

  @BeforeClass
  public static void beforeAll() {
    logFactoryMock = mockStatic( LogFactory.class );

    loggerMock = mock( Log.class );
    sessionLogger = mock( Log.class );

    // Must use this lazy form of mocking `LogFactory.log` because the literal reference to
    // PentahoSessionHolder.class would trigger that class's initialization, which would request for the logger.

    logFactoryMock.when( () -> LogFactory.getLog( eq( PluginGwtRpc.class ) ) ).thenAnswer( invocationOnMock -> loggerMock );
    logFactoryMock.when( () -> LogFactory.getLog( eq( PentahoSessionHolder.class ) ) ).thenAnswer( invocationOnMock -> sessionLogger );

    // Static initialization of class requires LogFactory already configured, above.
    pentahoSessionHolderMock = mockStatic( PentahoSessionHolder.class );
    pentahoSessionHolderMock.when( PentahoSessionHolder::getSession ).thenReturn( null );
  }

  @After
  public void afterEach() {
    reset( loggerMock );
    if ( pentahoSystemMock != null && !pentahoSystemMock.isClosed() ) {
      pentahoSystemMock.close();
    }
  }

  @AfterClass
  public static void afterAll() {
    logFactoryMock.close();
    if ( pentahoSessionHolderMock != null && !pentahoSessionHolderMock.isClosed() ) {
      pentahoSessionHolderMock.close();
    }
  }

  private IServiceManager setupServiceManagerMock( String serviceKey, Object serviceBean ) {
    IServiceManager serviceManagerMock = mock( IServiceManager.class );

    IServiceConfig serviceConfigMock = mock( IServiceConfig.class );
    when( serviceManagerMock.getServiceConfig( "gwt", serviceKey ) ).thenReturn( serviceConfigMock );

    pentahoSystemMock = mockStatic( PentahoSystem.class );
    pentahoSystemMock.when( () -> PentahoSystem.get( eq( IServiceManager.class ), any() ) ).thenReturn( serviceManagerMock );

    try {
      when( serviceManagerMock.getServiceBean( "gwt", serviceKey ) ).thenReturn( serviceBean );
    } catch ( ServiceException e ) {
      // Does not happen.
    }

    return serviceManagerMock;
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
    try ( MockedStatic<PluginUtil> pluginUtilMock = mockStatic( PluginUtil.class ) ) {
      pluginUtilMock.when( () -> PluginUtil.getPluginIdFromPath( moduleContextPath ) ).thenReturn( pluginId );

      // ---
      SerializationPolicy pluginPolicyMock = mock( SerializationPolicy.class );

      pentahoSystemMock = mockStatic( PentahoSystem.class );
      pentahoSystemMock.when( () -> PentahoSystem.get( eq( SerializationPolicy.class ), eq( null ), anyMap() ) ).thenAnswer( invocationOnMock -> {
        Map<String, String> props = (Map<String, String>) invocationOnMock.getArguments()[2];
        if ( pluginId.equals( props.get( "plugin" ) ) ) {
          return pluginPolicyMock;
        }
        fail();
        return mock( SerializationPolicy.class );
      } );
      // ---

      HttpServletRequest httpRequestMock = setupHttpRequest( pathInfo );
      PluginGwtRpc gwtRpc = new PluginGwtRpc( httpRequestMock );

      SerializationPolicy result = gwtRpc.loadSerializationPolicy( moduleContextPath, strongName );

      assertEquals( pluginPolicyMock, result );
    }

  }
  @Test
  public void testLoadSerializationPolicyLogsErrorAndReturnsNullIfNoPluginClassLoader() {
    String pathInfo = "/serviceName";
    String pluginId = "data-access";
    String moduleContextPath = "/content/data-access/resources/gwt/";
    String strongName = "ABC";

    // ---

    try ( MockedStatic<PluginUtil> pluginUtilMock = mockStatic( PluginUtil.class ) ) {
      pluginUtilMock.when( () -> PluginUtil.getPluginIdFromPath( moduleContextPath ) ).thenReturn( pluginId );

      // ----

      // There is no configured SP bean.
      pentahoSystemMock = mockStatic( PentahoSystem.class );
      pentahoSystemMock.when( () -> PentahoSystem.get( eq( SerializationPolicy.class ), eq( null ), anyMap() ) ) .thenReturn( null );
      // ---

      when( PluginUtil.getClassLoaderForService( moduleContextPath ) ).thenReturn( null );

      // ---

      HttpServletRequest httpRequestMock = setupHttpRequest( pathInfo );
      PluginGwtRpc gwtRpc = new PluginGwtRpc( httpRequestMock );

      SerializationPolicy result = gwtRpc.loadSerializationPolicy( moduleContextPath, strongName );

      assertNull( result );

      verify( loggerMock, times( 1 ) ).error( nullable( String.class ) );
    }
  }

  @Test
  public void testLoadSerializationPolicyLogsErrorAndReturnsNullIfNoPluginResource() {
    String pathInfo = "/serviceName";
    String pluginId = "data-access";
    String moduleContextPath = "/content/data-access/resources/gwt/";
    String strongName = "ABC";
    String serializationPolicyFilename = strongName + ".gwt.rpc";

    // ---

    try ( MockedStatic<PluginUtil> pluginUtilMock = mockStatic( PluginUtil.class ) ) {
      pluginUtilMock.when( () -> PluginUtil.getPluginIdFromPath( moduleContextPath ) ).thenReturn( pluginId );

      // ----

      // There is no configured SP bean.
      pentahoSystemMock = mockStatic( PentahoSystem.class );
      pentahoSystemMock.when( () -> PentahoSystem.get( eq( SerializationPolicy.class ), eq( null ), anyMap() ) ) .thenReturn( null );

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

      verify( loggerMock, times( 1 ) ).error( nullable( String.class ) );
    }
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

    try ( MockedStatic<PluginUtil> pluginUtilMock = mockStatic( PluginUtil.class ) ) {
      pluginUtilMock.when( () -> PluginUtil.getPluginIdFromPath( moduleContextPath ) ).thenReturn( pluginId );

      // ----

      // There is no configured SP bean.
      pentahoSystemMock = mockStatic( PentahoSystem.class );
      pentahoSystemMock.when( () -> PentahoSystem.get( eq( SerializationPolicy.class ), eq( null ), anyMap() ) )
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
          @Override
          public InputStream get() {
            return pluginPolicyInputStreamMock;
          }
        } );

      // ---

      SerializationPolicy pluginPolicyMock = mock( SerializationPolicy.class );
      try ( MockedStatic<AbstractGwtRpc> abstractGwtRpcMock = mockStatic( AbstractGwtRpc.class ) ) {
        abstractGwtRpcMock.when( () -> AbstractGwtRpc.loadSerializationPolicyFromInputStream( pluginPolicyInputStreamSupplierSpy, policyFilename ) )
          .thenReturn( pluginPolicyMock );

        // ---

        HttpServletRequest httpRequestMock = setupHttpRequest( pathInfo );
        PluginGwtRpc gwtRpcSpy = spy( new PluginGwtRpc( httpRequestMock ) );

        doReturn( pluginPolicyInputStreamSupplierSpy ).when( gwtRpcSpy ).getInputStreamSupplier( policy1Url );

        // ---

        SerializationPolicy result = gwtRpcSpy.loadSerializationPolicy( moduleContextPath, strongName );

        // ---

        assertEquals( pluginPolicyMock, result );

        verify( loggerMock, times( 1 ) ).warn( nullable( String.class ) );
      }
    }
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

    try ( MockedStatic<PluginUtil> pluginUtilMock = mockStatic( PluginUtil.class ) ) {
      pluginUtilMock.when( () -> PluginUtil.getPluginIdFromPath( moduleContextPath ) ).thenReturn( pluginId );

      // ----

      // There is no configured SP bean.
      pentahoSystemMock = mockStatic( PentahoSystem.class );
      pentahoSystemMock.when( () -> PentahoSystem.get( eq( SerializationPolicy.class ), eq( null ), anyMap() ) )
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
          @Override
          public InputStream get() {
            return pluginPolicyInputStreamMock;
          }
        } );

      // ---

      SerializationPolicy pluginPolicyMock = mock( SerializationPolicy.class );
      try ( MockedStatic<AbstractGwtRpc> abstractGwtRpcMock = mockStatic( AbstractGwtRpc.class ) ) {
        abstractGwtRpcMock.when( () -> AbstractGwtRpc.loadSerializationPolicyFromInputStream( pluginPolicyInputStreamSupplierSpy, policyFilename ) )
          .thenReturn( pluginPolicyMock );
        // ---

        HttpServletRequest httpRequestMock = setupHttpRequest( pathInfo );
        PluginGwtRpc gwtRpcSpy = spy( new PluginGwtRpc( httpRequestMock ) );

        doReturn( pluginPolicyInputStreamSupplierSpy ).when( gwtRpcSpy ).getInputStreamSupplier( policy1Url );

        // ---

        SerializationPolicy result = gwtRpcSpy.loadSerializationPolicy( moduleContextPath, strongName );

        // ---

        assertEquals( pluginPolicyMock, result );

        verify( loggerMock, times( 0 ) ).error( nullable( String.class ) );
        verify( loggerMock, times( 0 ) ).warn( nullable( String.class ) );
      }
    }
  }

  // endregion

  private HttpServletRequest setupHttpRequest( String pathInfo ) {
    HttpServletRequest httpRequestMock = mock( HttpServletRequest.class );
    when( httpRequestMock.getPathInfo() ).thenReturn( pathInfo );

    return httpRequestMock;
  }
}
