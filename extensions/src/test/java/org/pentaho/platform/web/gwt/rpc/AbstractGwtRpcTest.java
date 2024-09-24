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

import com.google.gwt.user.client.rpc.IncompatibleRemoteServiceException;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.RPC;
import com.google.gwt.user.server.rpc.RPCRequest;
import com.google.gwt.user.server.rpc.RPCServletUtils;
import com.google.gwt.user.server.rpc.SerializationPolicy;
import com.google.gwt.user.server.rpc.SerializationPolicyLoader;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.pentaho.platform.web.gwt.rpc.impl.GwtRpcUtil;
import org.pentaho.platform.web.gwt.rpc.util.ThrowingSupplier;
import org.pentaho.platform.web.servlet.GwtRpcProxyException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@RunWith( MockitoJUnitRunner.class )
public class AbstractGwtRpcTest {

  private final String HTTP_GWT_RPC_ATTRIBUTE = AbstractGwtRpc.HTTP_GWT_RPC_ATTRIBUTE;

  private static Log loggerMock;
  private static MockedStatic<LogFactory> logFactoryMock;

  // region Helpers
  static class TestGwtRpc extends AbstractGwtRpc {

    public TestGwtRpc( @NonNull HttpServletRequest httpRequest ) {
      super( httpRequest );
    }

    @NonNull @Override
    protected Object resolveTarget() {
      throw new UnsupportedOperationException();
    }

    @Nullable @Override
    protected SerializationPolicy loadSerializationPolicy( @NonNull String moduleContextPath,
                                                           @Nullable String strongName ) {
      throw new UnsupportedOperationException();
    }
  }
  // endregion

  @BeforeClass
  public static void beforeAll() {
    logFactoryMock = Mockito.mockStatic( LogFactory.class );
    loggerMock = mock( Log.class );

    logFactoryMock.when( () -> LogFactory.getLog( eq( AbstractGwtRpc.class ) ) ).thenAnswer( invocationOnMock -> loggerMock );
  }

  @AfterClass
  public static void afterAll() {
    logFactoryMock.close();
  }

  @After
  public void afterEach() {
    reset( loggerMock );
  }

  // region Servlet
  @Test
  public void testGetServletRequest() {
    HttpServletRequest httpRequestMock = mock( HttpServletRequest.class );
    TestGwtRpc gwtRpc = new TestGwtRpc( httpRequestMock );

    assertEquals( httpRequestMock, gwtRpc.getServletRequest() );
  }

  @Test
  public void testGetServletContext() {
    ServletContext servletContextMock = mock( ServletContext.class );
    HttpServletRequest httpRequestMock = mock( HttpServletRequest.class );
    when( httpRequestMock.getServletContext() ).thenReturn( servletContextMock );

    TestGwtRpc gwtRpc = new TestGwtRpc( httpRequestMock );

    assertEquals( servletContextMock, gwtRpc.getServletContext() );
  }

  @Test
  public void testGetAppContextPath() {
    String appContextPath = "PATH";
    HttpServletRequest httpRequestMock = mock( HttpServletRequest.class );
    when( httpRequestMock.getContextPath() ).thenReturn( appContextPath );

    TestGwtRpc gwtRpc = new TestGwtRpc( httpRequestMock );

    assertEquals( appContextPath, gwtRpc.getAppContextPath() );
  }

  @Test
  public void testGetServletContextPath() {
    HttpServletRequest httpRequestMock = mock( HttpServletRequest.class );
    TestGwtRpc gwtRpc = new TestGwtRpc( httpRequestMock );

    when( httpRequestMock.getServletPath() ).thenReturn( "/A" );

    // ---

    when( httpRequestMock.getPathInfo() ).thenReturn( "/B" );
    assertEquals( "/A/B", gwtRpc.getServletContextPath() );

    when( httpRequestMock.getPathInfo() ).thenReturn( "B" );
    assertEquals( "/A/B", gwtRpc.getServletContextPath() );
  }
  // endregion

  // region SerializationPolicyCache
  @Test
  public void testDefaultSerializationPolicyCacheIsNull() {

    HttpServletRequest httpRequestMock = mock( HttpServletRequest.class );

    TestGwtRpc gwtRpc = new TestGwtRpc( httpRequestMock );

    assertNull( gwtRpc.getSerializationPolicyCache() );
  }

  @Test
  public void testSetAndGetSerializationPolicyCache() {

    IGwtRpcSerializationPolicyCache cache = mock( IGwtRpcSerializationPolicyCache.class );
    HttpServletRequest httpRequestMock = mock( HttpServletRequest.class );

    TestGwtRpc gwtRpc = new TestGwtRpc( httpRequestMock );

    gwtRpc.setSerializationPolicyCache( cache );

    assertEquals( cache, gwtRpc.getSerializationPolicyCache() );
  }
  // endregion

  // region Request Payload
  @Test
  public void testGetRequestPayloadFirstTime() {
    String requestPayload = "REQUEST_PAYLOAD";
    HttpServletRequest httpRequestMock = mock( HttpServletRequest.class );
    TestGwtRpc gwtRpc = new TestGwtRpc( httpRequestMock );

    try ( MockedStatic<RPCServletUtils> rpcServletUtils = Mockito.mockStatic( RPCServletUtils.class ) ) {
      rpcServletUtils.when( () -> RPCServletUtils.readContentAsGwtRpc( any() ) ).thenReturn( requestPayload );

      String result = gwtRpc.getRequestPayload();

      assertEquals( requestPayload, result );
    }
  }

  @Test
  public void testGetRequestPayloadSecondTimeIsCached() throws ServletException, IOException {
    String requestPayload = "REQUEST_PAYLOAD";
    HttpServletRequest httpRequestMock = mock( HttpServletRequest.class );
    TestGwtRpc gwtRpc = new TestGwtRpc( httpRequestMock );

    try ( MockedStatic<RPCServletUtils> rpcServletUtils = Mockito.mockStatic( RPCServletUtils.class ) ) {
      rpcServletUtils.when( () -> RPCServletUtils.readContentAsGwtRpc( any() ) ).thenReturn( requestPayload );

      gwtRpc.getRequestPayload();

      String result = gwtRpc.getRequestPayload();

      assertEquals( requestPayload, result );
      rpcServletUtils.verify( () -> RPCServletUtils.readContentAsGwtRpc( any() ) );
      RPCServletUtils.readContentAsGwtRpc( any() );
    }
  }

  @Test
  public void testGetRequestPayloadLogsAndWrapsThrownIOException() {
    IOException error = mock( IOException.class );
    HttpServletRequest httpRequestMock = mock( HttpServletRequest.class );
    TestGwtRpc gwtRpc = new TestGwtRpc( httpRequestMock );

    try ( MockedStatic<RPCServletUtils> rpcServletUtils = Mockito.mockStatic( RPCServletUtils.class ) ) {
      rpcServletUtils.when( () -> RPCServletUtils.readContentAsGwtRpc( any() ) ).thenThrow( error );

      try {
        gwtRpc.getRequestPayload();
        fail();
      } catch ( GwtRpcProxyException ex ) {
        assertEquals( error, ex.getCause() );
        verify( loggerMock ).error( nullable( String.class ), eq( error ) );
      }
    }
  }

  @Test
  public void testGetRequestPayloadLogsAndWrapsThrownServletException() {
    ServletException error = mock( ServletException.class );
    HttpServletRequest httpRequestMock = mock( HttpServletRequest.class );
    TestGwtRpc gwtRpc = new TestGwtRpc( httpRequestMock );

    try ( MockedStatic<RPCServletUtils> rpcServletUtils = Mockito.mockStatic( RPCServletUtils.class ) ) {
      rpcServletUtils.when( () -> RPCServletUtils.readContentAsGwtRpc( any() ) ).thenThrow( error );

      try {
        gwtRpc.getRequestPayload();

        fail();
      } catch ( GwtRpcProxyException ex ) {
        assertEquals( error, ex.getCause() );
        verify( loggerMock ).error( nullable( String.class ), eq( error ) );
      }
    }
  }
   //endregion

  // region Target
  @Test
  public void testGetTargetFirstTime() {
    Object target = new Object();

    HttpServletRequest httpRequestMock = mock( HttpServletRequest.class );

    TestGwtRpc gwtRpcSpy = spy( new TestGwtRpc( httpRequestMock ) );
    doReturn( target ).when( gwtRpcSpy ).resolveTarget();

    Object result = gwtRpcSpy.getTarget();

    assertEquals( target, result );
  }

  @Test
  public void testGetTargetSecondTimeIsCached() {
    Object target = new Object();

    HttpServletRequest httpRequestMock = mock( HttpServletRequest.class );

    TestGwtRpc gwtRpcSpy = spy( new TestGwtRpc( httpRequestMock ) );
    doReturn( target ).when( gwtRpcSpy ).resolveTarget();

    gwtRpcSpy.getTarget();

    Object result = gwtRpcSpy.getTarget();

    assertEquals( target, result );

    verify( gwtRpcSpy ).resolveTarget();
  }

  @Test
  public void testGetTargetLogsAndRethrowsGwtRpcException() {
    // Mocking the exception does not work with doThrow...
    // Spies require doThrow instead of thenThrow.
    // mock( GwtRpcProxyException.class );
    GwtRpcProxyException error = new GwtRpcProxyException();

    HttpServletRequest httpRequestMock = mock( HttpServletRequest.class );

    TestGwtRpc gwtRpcSpy = spy( new TestGwtRpc( httpRequestMock ) );
    doThrow( error ).when( gwtRpcSpy ).resolveTarget();
    try {
      gwtRpcSpy.getTarget();

      fail();
    } catch ( GwtRpcProxyException ex ) {
      assertEquals( error, ex );
      verify( loggerMock ).error( nullable( String.class ), eq( ex ) );
    }
  }

  @Test
  public void testGetTargetClassLoader() {
    // new Object() would have a null class loader...
    Object target = this;
    ClassLoader classLoader = this.getClass().getClassLoader();

    HttpServletRequest httpRequestMock = mock( HttpServletRequest.class );

    TestGwtRpc gwtRpcSpy = spy( new TestGwtRpc( httpRequestMock ) );
    doReturn( target ).when( gwtRpcSpy ).getTarget();

    Object result = gwtRpcSpy.getTargetClassLoader();

    assertEquals( classLoader, result );
  }
  // endregion

  // region Request
  @Test
  public void testGetRequestFirstTime() {
    String requestPayload = "REQUEST_PAYLOAD";
    // new Object would have a null class loader!
    Object target = this;
    RPCRequest rpcRequest = new RPCRequest( null, null, null, 0 );

    HttpServletRequest httpRequestMock = mock( HttpServletRequest.class );

    TestGwtRpc gwtRpcSpy = spy( new TestGwtRpc( httpRequestMock ) );
    doReturn( requestPayload ).when( gwtRpcSpy ).getRequestPayload();
    doReturn( target ).when( gwtRpcSpy ).resolveTarget();

    try ( MockedStatic<RPC> rpc = Mockito.mockStatic( RPC.class ) ) {
      rpc.when( () -> RPC.decodeRequest( eq( requestPayload ), eq( null ), any() ) ).thenReturn( rpcRequest );
      RPCRequest result = gwtRpcSpy.getRequest();
      assertEquals( rpcRequest, result );
    }
  }

  @Test
  public void testGetRequestSecondTimeIsCached() {
    String requestPayload = "REQUEST_PAYLOAD";
    // new Object would have a null class loader!
    Object target = this;
    RPCRequest rpcRequest = new RPCRequest( null, null, null, 0 );

    HttpServletRequest httpRequestMock = mock( HttpServletRequest.class );

    TestGwtRpc gwtRpcSpy = spy( new TestGwtRpc( httpRequestMock ) );
    doReturn( requestPayload ).when( gwtRpcSpy ).getRequestPayload();
    doReturn( target ).when( gwtRpcSpy ).resolveTarget();

    try ( MockedStatic<RPC> rpc = Mockito.mockStatic( RPC.class ) ) {
      rpc.when( () -> RPC.decodeRequest( eq( requestPayload ), eq( null ), any() ) ).thenReturn( rpcRequest );

      gwtRpcSpy.getRequest();

      verify( gwtRpcSpy ).getRequestPayload();

      RPCRequest result = gwtRpcSpy.getRequest();

      verify( gwtRpcSpy ).getRequestPayload();

      assertEquals( rpcRequest, result );
    }
  }

  @Test
  public void testGetRequestRunsWithTargetClassLoader() {
    String requestPayload = "REQUEST_PAYLOAD";
    ClassLoader targetClassLoader = mock( ClassLoader.class );
    RPCRequest rpcRequest = new RPCRequest( null, null, null, 0 );
    HttpServletRequest httpRequestMock = mock( HttpServletRequest.class );

    TestGwtRpc gwtRpcSpy = spy( new TestGwtRpc( httpRequestMock ) );
    doReturn( targetClassLoader ).when( gwtRpcSpy ).getTargetClassLoader();
    doReturn( requestPayload ).when( gwtRpcSpy ).getRequestPayload();

    // Stub getRequestCore with:
    doAnswer( (Answer<RPCRequest>) invocationOnMock -> {
      ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();

      assertEquals( targetClassLoader, currentClassLoader );

      return rpcRequest;
    } ).when( gwtRpcSpy ).getRequestCore( requestPayload );

    gwtRpcSpy.getRequest();

    verify( gwtRpcSpy ).getRequestCore( requestPayload );
  }

  @Test
  public void testGetRequestLogsAndWrapsThrownIllegalArgumentException() {
    String requestPayload = "REQUEST_PAYLOAD";
    // new Object would have a null class loader!
    Object target = this;
    IllegalArgumentException error = new IllegalArgumentException();
    HttpServletRequest httpRequestMock = mock( HttpServletRequest.class );

    TestGwtRpc gwtRpcSpy = spy( new TestGwtRpc( httpRequestMock ) );
    doReturn( requestPayload ).when( gwtRpcSpy ).getRequestPayload();
    doReturn( target ).when( gwtRpcSpy ).resolveTarget();

    try ( MockedStatic<RPC> rpc = Mockito.mockStatic( RPC.class ) ) {
      rpc.when( () -> RPC.decodeRequest( eq( requestPayload ), eq( null ), any() ) ).thenThrow( error );

      try {
        gwtRpcSpy.getRequest();
        fail();
      } catch ( GwtRpcProxyException ex ) {
        assertEquals( error, ex.getCause() );
        verify( loggerMock ).error( nullable( String.class ), eq( error ) );
      }
    }
  }

  @Test
  public void testGetRequestLogsAndWrapsThrownIncompatibleRemoteServiceException() {
    String requestPayload = "REQUEST_PAYLOAD";
    // new Object would have a null class loader!
    Object target = this;
    IncompatibleRemoteServiceException error = new IncompatibleRemoteServiceException();
    HttpServletRequest httpRequestMock = mock( HttpServletRequest.class );

    TestGwtRpc gwtRpcSpy = spy( new TestGwtRpc( httpRequestMock ) );
    doReturn( requestPayload ).when( gwtRpcSpy ).getRequestPayload();
    doReturn( target ).when( gwtRpcSpy ).resolveTarget();

    try ( MockedStatic<RPC> rpc = Mockito.mockStatic( RPC.class ) ) {
      rpc.when( () -> RPC.decodeRequest( eq( requestPayload ), eq( null ), any() ) ).thenThrow( error );


      try {
        gwtRpcSpy.getRequest();
        fail();
      } catch ( GwtRpcProxyException ex ) {
        assertEquals( error, ex.getCause() );
        verify( loggerMock ).error( nullable( String.class ), eq( error ) );
      }
    }
  }
  // endregion

  // region getSerializationPolicy
  @Test
  public void testGetSerializationPolicyUsesCacheIfSet() {

    String moduleBaseURL = "http://localhost:8080/pentaho/content/data-access/resources/gwt/";
    String strongName = "ABCDF12345";

    HttpServletRequest httpRequestMock = mock( HttpServletRequest.class );

    SerializationPolicy policyMock = mock( SerializationPolicy.class );

    IGwtRpcSerializationPolicyCache cache = mock( IGwtRpcSerializationPolicyCache.class );
    when( cache.getSerializationPolicy( eq( moduleBaseURL ), eq( strongName ), any() ) ).thenReturn( policyMock );

    TestGwtRpc gwtRpc = new TestGwtRpc( httpRequestMock );
    gwtRpc.setSerializationPolicyCache( cache );

    SerializationPolicy result = gwtRpc.getSerializationPolicy( moduleBaseURL, strongName );

    assertEquals( policyMock, result );
  }

  @Test
  public void testGetSerializationPolicyLogsAndReturnsDefaultIfModuleBaseURLIsNull() {

    String moduleBaseURL = null;
    String strongName = "ABCDF12345";

    HttpServletRequest httpRequestMock = mock( HttpServletRequest.class );

    SerializationPolicy defaultPolicyMock = mock( SerializationPolicy.class );

    try ( MockedStatic<AbstractGwtRpc> rpc = Mockito.mockStatic( AbstractGwtRpc.class ) ) {
      rpc.when( AbstractGwtRpc::getDefaultSerializationPolicy ).thenReturn( defaultPolicyMock );

      TestGwtRpc gwtRpc = new TestGwtRpc( httpRequestMock );

      SerializationPolicy result = gwtRpc.getSerializationPolicy( moduleBaseURL, strongName );

      assertEquals( defaultPolicyMock, result );

      verify( loggerMock ).error( anyString() );
    }
  }

  @Test
  public void testGetSerializationPolicyLogsAndReturnsDefaultIfModuleBaseURLIsNotValidURL() {

    String moduleBaseURL = "ht123_-tp:localhost:8080/pentaho/content/data-access/resources/gwt/";
    String strongName = "ABCDF12345";

    HttpServletRequest httpRequestMock = mock( HttpServletRequest.class );

    SerializationPolicy defaultPolicyMock = mock( SerializationPolicy.class );

    try ( MockedStatic<AbstractGwtRpc> rpc = Mockito.mockStatic( AbstractGwtRpc.class ) ) {
      rpc.when( AbstractGwtRpc::getDefaultSerializationPolicy ).thenReturn( defaultPolicyMock );

      TestGwtRpc gwtRpc = new TestGwtRpc( httpRequestMock );

      SerializationPolicy result = gwtRpc.getSerializationPolicy( moduleBaseURL, strongName );

      assertEquals( defaultPolicyMock, result );

      verify( loggerMock ).error( nullable( String.class ), any( MalformedURLException.class ) );
    }
  }

  @Test
  public void testGetSerializationPolicyReplacesWebAppRootToken() {

    String moduleBaseURL = "http://localhost:8080/WEBAPP_ROOT//content/data-access/resources/gwt/";
    String modulePath = "/WEBAPP_ROOT//content/data-access/resources/gwt/";
    String modulePathScrubbed = "/pentaho/content/data-access/resources/gwt/";
    String appContextPath = "/pentaho";
    String moduleContextPath = "/content/data-access/resources/gwt/";
    String strongName = "ABCDF12345";

    HttpServletRequest httpRequestMock = mock( HttpServletRequest.class );

    SerializationPolicy policyMock = mock( SerializationPolicy.class );

    TestGwtRpc gwtRpcSpy = spy( new TestGwtRpc( httpRequestMock ) );
    doReturn( appContextPath ).when( gwtRpcSpy ).getAppContextPath();
    doReturn( policyMock ).when( gwtRpcSpy ).loadSerializationPolicy( moduleContextPath, strongName );

    try ( MockedStatic<GwtRpcUtil> gwtRpcUtil = Mockito.mockStatic( GwtRpcUtil.class ) ) {
      gwtRpcUtil.when( () -> GwtRpcUtil.scrubWebAppRoot( modulePath, appContextPath ) ).thenReturn( modulePathScrubbed );

      gwtRpcSpy.getSerializationPolicy( moduleBaseURL, strongName );

      gwtRpcUtil.verify( () -> GwtRpcUtil.scrubWebAppRoot( modulePath, appContextPath ) );
      GwtRpcUtil.scrubWebAppRoot( modulePath, appContextPath );
    }
  }

  @Test
  public void testGetSerializationPolicyLogsAndReturnsDefaultIfModuleBaseURLIsNotPrefixByAppContextPath() {

    String moduleBaseURL = "http://localhost:8080/pentaho/content/data-access/resources/gwt/";
    String appContextPath = "/sixtaho";
    String strongName = "ABCDF12345";

    HttpServletRequest httpRequestMock = mock( HttpServletRequest.class );

    SerializationPolicy defaultPolicyMock = mock( SerializationPolicy.class );

    try ( MockedStatic<AbstractGwtRpc> rpc = Mockito.mockStatic( AbstractGwtRpc.class ) ) {
      rpc.when( AbstractGwtRpc::getDefaultSerializationPolicy ).thenReturn( defaultPolicyMock );

      TestGwtRpc gwtRpcSpy = spy( new TestGwtRpc( httpRequestMock ) );
      doReturn( appContextPath ).when( gwtRpcSpy ).getAppContextPath();

      SerializationPolicy result = gwtRpcSpy.getSerializationPolicy( moduleBaseURL, strongName );

      assertEquals( defaultPolicyMock, result );

      verify( loggerMock ).error( anyString() );
    }
  }

  @Test
  public void testGetSerializationPolicyCallsLoadSerializationPolicyWithModulePathAndStrongName() {

    String moduleBaseURL = "http://localhost:8080/pentaho/content/data-access/resources/gwt/";
    String appContextPath = "/pentaho";
    String moduleContextPath = "/content/data-access/resources/gwt/";
    String strongName = "ABCDF12345";

    HttpServletRequest httpRequestMock = mock( HttpServletRequest.class );

    SerializationPolicy policyMock = mock( SerializationPolicy.class );

    TestGwtRpc gwtRpcSpy = spy( new TestGwtRpc( httpRequestMock ) );
    doReturn( appContextPath ).when( gwtRpcSpy ).getAppContextPath();
    doReturn( policyMock ).when( gwtRpcSpy ).loadSerializationPolicy( moduleContextPath, strongName );

    SerializationPolicy result = gwtRpcSpy.getSerializationPolicy( moduleBaseURL, strongName );

    assertEquals( policyMock, result );
  }

  @Test
  public void testGetSerializationPolicyCallsLoadSerializationPolicyReturnsDefaultIfItReturnsNull() {

    String moduleBaseURL = "http://localhost:8080/pentaho/content/data-access/resources/gwt/";
    String appContextPath = "/pentaho";
    String moduleContextPath = "/content/data-access/resources/gwt/";
    String strongName = "ABCDF12345";

    HttpServletRequest httpRequestMock = mock( HttpServletRequest.class );

    SerializationPolicy defaultPolicyMock = mock( SerializationPolicy.class );

    try ( MockedStatic<AbstractGwtRpc> rpc = Mockito.mockStatic( AbstractGwtRpc.class ) ) {
      rpc.when( AbstractGwtRpc::getDefaultSerializationPolicy ).thenReturn( defaultPolicyMock );
      TestGwtRpc gwtRpcSpy = spy( new TestGwtRpc( httpRequestMock ) );
      doReturn( appContextPath ).when( gwtRpcSpy ).getAppContextPath();
      doReturn( null ).when( gwtRpcSpy ).loadSerializationPolicy( moduleContextPath, strongName );

      SerializationPolicy result = gwtRpcSpy.getSerializationPolicy( moduleBaseURL, strongName );

      assertEquals( defaultPolicyMock, result );
    }
  }
  // endregion

  // region static getDefaultSerializationPolicy
  @Test
  public void testGetDefaultSerializationPolicyDelegatesToRPCClass() {

    SerializationPolicy defaultPolicyMock = mock( SerializationPolicy.class );

    try ( MockedStatic<RPC> rpc = Mockito.mockStatic( RPC.class ) ) {
      rpc.when( RPC::getDefaultSerializationPolicy ).thenReturn( defaultPolicyMock );

      SerializationPolicy result = AbstractGwtRpc.getDefaultSerializationPolicy();

      assertEquals( defaultPolicyMock, result );
    }
  }
  // endregion

  // region static loadSerializationPolicyFromInputStream
  @Test
  public void testLoadSerializationPolicyFromInputStreamSuccessfully() {
    String serializationPolicyFileName = "ABCDF12345.gwt.rpc";
    InputStream inputStreamMock = mock( InputStream.class );
    ThrowingSupplier<InputStream, IOException> inputStreamSupplier = () -> inputStreamMock;
    SerializationPolicy policyMock = mock( SerializationPolicy.class );

    try ( MockedStatic<SerializationPolicyLoader> serializationPolicyLoader = Mockito.mockStatic( SerializationPolicyLoader.class ) ) {
      serializationPolicyLoader.when( () -> SerializationPolicyLoader.loadFromStream( inputStreamMock, null ) ).thenReturn( policyMock );

      SerializationPolicy result =
        AbstractGwtRpc.loadSerializationPolicyFromInputStream( inputStreamSupplier, serializationPolicyFileName );

      assertEquals( policyMock, result );
    }
  }

  @Test
  public void testLoadSerializationPolicyFromInputStreamLogsAndReturnsNullIfSupplierReturnsNull() {
    String serializationPolicyFileName = "ABCDF12345.gwt.rpc";
    ThrowingSupplier<InputStream, IOException> inputStreamSupplier = () -> null;

    SerializationPolicy result =
      AbstractGwtRpc.loadSerializationPolicyFromInputStream( inputStreamSupplier, serializationPolicyFileName );

    assertNull( result );
    verify( loggerMock ).error( nullable( String.class ) );
  }

  @Test
  public void testLoadSerializationPolicyFromInputStreamLogsAndReturnsNullIfSupplierThrowsIOException() {
    String serializationPolicyFileName = "ABCDF12345.gwt.rpc";
    IOException error = new IOException();
    ThrowingSupplier<InputStream, IOException> inputStreamSupplier = () -> {
      throw error;
    };

    SerializationPolicy result =
      AbstractGwtRpc.loadSerializationPolicyFromInputStream( inputStreamSupplier, serializationPolicyFileName );

    assertNull( result );
    verify( loggerMock ).error( nullable( String.class ), eq( error ) );
  }

  @Test
  public void testLoadSerializationPolicyFromInputStreamLogsAndReturnsNullAndClosesStreamIfLoadThrowsIOException()
    throws IOException {

    String serializationPolicyFileName = "ABCDF12345.gwt.rpc";
    InputStream inputStreamMock = mock( InputStream.class );
    ThrowingSupplier<InputStream, IOException> inputStreamSupplier = () -> inputStreamMock;
    IOException error = new IOException();

    try ( MockedStatic<SerializationPolicyLoader> serializationPolicyLoader = Mockito.mockStatic( SerializationPolicyLoader.class ) ) {
      serializationPolicyLoader.when( () -> SerializationPolicyLoader.loadFromStream( inputStreamMock, null ) ).thenThrow( error );

      SerializationPolicy result =
        AbstractGwtRpc.loadSerializationPolicyFromInputStream( inputStreamSupplier, serializationPolicyFileName );

      assertNull( result );
      verify( loggerMock ).error( nullable( String.class ), eq( error ) );
      verify( inputStreamMock ).close();
    }
  }

  @Test
  public void testLoadSerializationPolicyFromInputStreamLogsAndReturnsNullAndClosesStreamIfLoadThrowsParseException()
    throws IOException {

    String serializationPolicyFileName = "ABCDF12345.gwt.rpc";
    InputStream inputStreamMock = mock( InputStream.class );
    ThrowingSupplier<InputStream, IOException> inputStreamSupplier = () -> inputStreamMock;
    ParseException error = new ParseException( "ABC", 0 );

    try ( MockedStatic<SerializationPolicyLoader> serializationPolicyLoader = Mockito.mockStatic( SerializationPolicyLoader.class ) ) {
      serializationPolicyLoader.when( () -> SerializationPolicyLoader.loadFromStream( inputStreamMock, null ) ).thenThrow( error );

      SerializationPolicy result =
        AbstractGwtRpc.loadSerializationPolicyFromInputStream( inputStreamSupplier, serializationPolicyFileName );

      assertNull( result );
      verify( loggerMock ).error( nullable( String.class ), eq( error ) );
      verify( inputStreamMock ).close();
    }
  }

  @Test
  public void testLoadSerializationPolicyFromInputStreamDoesNotLogErrorAndClosesStreamWhenSuccessful() throws IOException {
    String serializationPolicyFileName = "ABCDF12345.gwt.rpc";
    InputStream inputStreamMock = mock( InputStream.class );
    ThrowingSupplier<InputStream, IOException> inputStreamSupplier = () -> inputStreamMock;
    SerializationPolicy policy = mock( SerializationPolicy.class );

    try ( MockedStatic<SerializationPolicyLoader> serializationPolicyLoader = Mockito.mockStatic( SerializationPolicyLoader.class ) ) {
      serializationPolicyLoader.when( () -> SerializationPolicyLoader.loadFromStream( inputStreamMock, null ) ).thenReturn( policy );

      AbstractGwtRpc.loadSerializationPolicyFromInputStream( inputStreamSupplier, serializationPolicyFileName );

      verify( loggerMock, never() ).error( nullable( String.class ), any() );
      verify( loggerMock, never() ).error( nullable( String.class ) );

      verify( inputStreamMock ).close();
    }
  }
  // endregion

  // region Invocation
  static class TestResponse {

  }

  interface ServiceInterface extends RemoteService {
    TestResponse method( String id );

    TestResponse methodSpecial( String id );
  }

  static class ServiceClassWhichDoesNotImplementInterface {
    public TestResponse method( String id ) {
      return new TestResponse();
    }
  }

  @Test
  public void testInvokeSuccessfully() throws NoSuchMethodException {
    ServiceClassWhichDoesNotImplementInterface target = new ServiceClassWhichDoesNotImplementInterface();
    Method targetMethod = target.getClass().getMethod( "method", String.class );
    ClassLoader targetClassLoader = mock( ClassLoader.class );

    Method serviceMethod = ServiceInterface.class.getMethod( "method", String.class );
    Object[] rpcParameters = new Object[] { "id1" };
    SerializationPolicy policy = mock( SerializationPolicy.class );
    RPCRequest rpcRequest = new RPCRequest( serviceMethod, rpcParameters, policy, 0 );

    HttpServletRequest httpRequestMock = mock( HttpServletRequest.class );

    String response = "rpc response";

    TestGwtRpc gwtRpcSpy = spy( new TestGwtRpc( httpRequestMock ) );
    doReturn( target ).when( gwtRpcSpy ).getTarget();
    doReturn( targetMethod ).when( gwtRpcSpy ).getTargetMethod( target.getClass(), rpcRequest );
    doReturn( targetClassLoader ).when( gwtRpcSpy ).getTargetClassLoader();
    doReturn( rpcRequest ).when( gwtRpcSpy ).getRequest();
    try ( MockedStatic<RPC> rpc = Mockito.mockStatic( RPC.class ) ) {
      rpc.when( () -> RPC.invokeAndEncodeResponse( target, targetMethod, rpcParameters, policy ) ).thenReturn( response );

      String result = gwtRpcSpy.invoke();

      assertEquals( response, result );

      rpc.verify( () -> RPC.invokeAndEncodeResponse( target, targetMethod, rpcParameters, policy ) );
    }
  }

  @Test
  public void testInvokeServiceClassDoesNotNeedToImplementServiceInterface()
    throws NoSuchMethodException, SerializationException {
    ServiceClassWhichDoesNotImplementInterface target = new ServiceClassWhichDoesNotImplementInterface();
    Method targetMethod = target.getClass().getMethod( "method", String.class );
    ClassLoader targetClassLoader = mock( ClassLoader.class );

    Method serviceMethod = ServiceInterface.class.getMethod( "method", String.class );
    Object[] rpcParameters = new Object[] { "id1" };
    SerializationPolicy policy = mock( SerializationPolicy.class );
    RPCRequest rpcRequest = new RPCRequest( serviceMethod, rpcParameters, policy, 0 );

    HttpServletRequest httpRequestMock = mock( HttpServletRequest.class );

    String response = "rpc response";

    TestGwtRpc gwtRpcSpy = spy( new TestGwtRpc( httpRequestMock ) );
    doReturn( target ).when( gwtRpcSpy ).getTarget();
    doReturn( targetClassLoader ).when( gwtRpcSpy ).getTargetClassLoader();
    doReturn( rpcRequest ).when( gwtRpcSpy ).getRequest();

    // Proven if the correct targetMethod is received at this call.
    try ( MockedStatic<RPC> rpc = Mockito.mockStatic( RPC.class ) ) {
      rpc.when( () -> RPC.invokeAndEncodeResponse( target, targetMethod, rpcParameters, policy ) ).thenReturn( response );

      String result = gwtRpcSpy.invoke();

      assertEquals( response, result );

      rpc.verify( () -> RPC.invokeAndEncodeResponse( target, targetMethod, rpcParameters, policy ) );
      RPC.invokeAndEncodeResponse( target, targetMethod, rpcParameters, policy );
    }
  }

  @Test
  public void testInvokeLogsAndThrowsIfServiceClassDoesNotHaveServiceInterfaceMethod() throws NoSuchMethodException {
    ServiceClassWhichDoesNotImplementInterface target = new ServiceClassWhichDoesNotImplementInterface();
    ClassLoader targetClassLoader = mock( ClassLoader.class );

    Method serviceSpecialMethod = ServiceInterface.class.getMethod( "methodSpecial", String.class );
    Object[] rpcParameters = new Object[] { "id1" };
    SerializationPolicy policy = mock( SerializationPolicy.class );
    RPCRequest rpcRequest = new RPCRequest( serviceSpecialMethod, rpcParameters, policy, 0 );

    HttpServletRequest httpRequestMock = mock( HttpServletRequest.class );

    TestGwtRpc gwtRpcSpy = spy( new TestGwtRpc( httpRequestMock ) );
    doReturn( target ).when( gwtRpcSpy ).getTarget();
    doReturn( rpcRequest ).when( gwtRpcSpy ).getRequest();

    try {
      gwtRpcSpy.invoke();
      fail();
    } catch ( GwtRpcProxyException ex ) {
      assertTrue( ex.getCause() instanceof NoSuchMethodException );
      verify( loggerMock ).error( nullable( String.class ), eq( ex.getCause() ) );
    }
  }

  @Test
  public void testInvokeServiceClassLogsAndThrowsIfRPCThrowsSerializationException()
    throws NoSuchMethodException {
    ServiceClassWhichDoesNotImplementInterface target = new ServiceClassWhichDoesNotImplementInterface();
    Method targetMethod = target.getClass().getMethod( "method", String.class );
    ClassLoader targetClassLoader = mock( ClassLoader.class );

    Method serviceSpecialMethod = ServiceInterface.class.getMethod( "method", String.class );
    Object[] rpcParameters = new Object[] { "id1" };
    SerializationPolicy policy = mock( SerializationPolicy.class );
    RPCRequest rpcRequest = new RPCRequest( serviceSpecialMethod, rpcParameters, policy, 0 );

    HttpServletRequest httpRequestMock = mock( HttpServletRequest.class );

    TestGwtRpc gwtRpcSpy = spy( new TestGwtRpc( httpRequestMock ) );
    doReturn( target ).when( gwtRpcSpy ).getTarget();
    doReturn( targetClassLoader ).when( gwtRpcSpy ).getTargetClassLoader();
    doReturn( rpcRequest ).when( gwtRpcSpy ).getRequest();

    SerializationException error = new SerializationException();

    try ( MockedStatic<RPC> rpc = Mockito.mockStatic( RPC.class ) ) {
      rpc.when( () -> RPC.invokeAndEncodeResponse( target, targetMethod, rpcParameters, policy ) ).thenThrow( error );

      try {
        gwtRpcSpy.invoke();
        fail();
      } catch ( GwtRpcProxyException ex ) {
        assertEquals( error, ex.getCause() );
        verify( loggerMock ).error( nullable( String.class ), eq( error ) );
      }
    }
  }

  @Test
  public void testInvokeRunsInTargetClassLoader() throws NoSuchMethodException, SerializationException {
    ServiceClassWhichDoesNotImplementInterface target = new ServiceClassWhichDoesNotImplementInterface();
    Method targetMethod = target.getClass().getMethod( "method", String.class );
    ClassLoader targetClassLoader = mock( ClassLoader.class );

    Method serviceMethod = ServiceInterface.class.getMethod( "method", String.class );
    Object[] rpcParameters = new Object[] { "id1" };
    SerializationPolicy policy = mock( SerializationPolicy.class );
    RPCRequest rpcRequest = new RPCRequest( serviceMethod, rpcParameters, policy, 0 );

    HttpServletRequest httpRequestMock = mock( HttpServletRequest.class );

    String response = "rpc response";

    TestGwtRpc gwtRpcSpy = spy( new TestGwtRpc( httpRequestMock ) );
    doReturn( target ).when( gwtRpcSpy ).getTarget();
    doReturn( targetMethod ).when( gwtRpcSpy ).getTargetMethod( target.getClass(), rpcRequest );
    doReturn( targetClassLoader ).when( gwtRpcSpy ).getTargetClassLoader();
    doReturn( rpcRequest ).when( gwtRpcSpy ).getRequest();

    // Stub invokeCore with:
    doAnswer( (Answer<String>) invocationOnMock -> {
      ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();

      assertEquals( targetClassLoader, currentClassLoader );

      return response;
    } ).when( gwtRpcSpy ).invokeCore( target, targetMethod, rpcRequest );

    gwtRpcSpy.invoke();

    verify( gwtRpcSpy ).invokeCore( target, targetMethod, rpcRequest );
  }

  // endregion

  // region getInstance
  @Test
  public void testGetInstanceFirstTimeCreatesSetsPolicyCacheAndStoresInRequest() {

    HttpServletRequest httpRequestMock = mock( HttpServletRequest.class );
    when( httpRequestMock.getAttribute( HTTP_GWT_RPC_ATTRIBUTE ) ).thenReturn( null );

    TestGwtRpc gwtRpcMock1 = mock( TestGwtRpc.class );
    Function<HttpServletRequest, TestGwtRpc> factory = request -> gwtRpcMock1;

    IGwtRpcSerializationPolicyCache cache = mock( IGwtRpcSerializationPolicyCache.class );

    TestGwtRpc result1 = AbstractGwtRpc.getInstance( httpRequestMock, factory, cache );

    assertEquals( gwtRpcMock1, result1 );

    verify( gwtRpcMock1 ).setSerializationPolicyCache( cache );

    verify( httpRequestMock ).getAttribute( HTTP_GWT_RPC_ATTRIBUTE );
    verify( httpRequestMock ).setAttribute( HTTP_GWT_RPC_ATTRIBUTE, gwtRpcMock1 );
  }

  @Test
  public void testGetInstanceSecondTimeGetsFromRequest() {

    HttpServletRequest httpRequestMock = mock( HttpServletRequest.class );
    TestGwtRpc gwtRpcMock1 = mock( TestGwtRpc.class );
    when( httpRequestMock.getAttribute( HTTP_GWT_RPC_ATTRIBUTE ) ).thenReturn( gwtRpcMock1 );

    Function<HttpServletRequest, TestGwtRpc> factory = httpServletRequest -> {
      fail();
      return null;
    };

    IGwtRpcSerializationPolicyCache cache = mock( IGwtRpcSerializationPolicyCache.class );

    TestGwtRpc result2 = AbstractGwtRpc.getInstance( httpRequestMock, factory, cache );

    assertEquals( gwtRpcMock1, result2 );

    verify( gwtRpcMock1, times( 0 ) ).setSerializationPolicyCache( cache );
    verify( httpRequestMock ).getAttribute( HTTP_GWT_RPC_ATTRIBUTE );
    verify( httpRequestMock, Mockito.times( 0 ) ).setAttribute( HTTP_GWT_RPC_ATTRIBUTE, gwtRpcMock1 );
  }
  // endregion
}
