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

package org.pentaho.platform.web.gwt.rpc.matcher;

import com.google.gwt.user.server.rpc.RPCRequest;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.junit.Test;
import org.pentaho.platform.web.gwt.rpc.AbstractGwtRpc;
import org.pentaho.platform.web.gwt.rpc.IGwtRpcSerializationPolicyCache;
import org.pentaho.platform.web.servlet.GwtRpcProxyException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.HttpMethod;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class AbstractGwtRpcRequestMatcherTest {

  private static final String TEST_PATH = "/ws/gwt/unifiedRepository";
  private static final String TEST_PATH_ALTERNATE_CASE = "/ws/gwt/UnifiedRepository";
  private static final String TEST_PATTERN = "^/ws/gwt/unifiedRepository\\b.*";
  private static final String TEST_RPC_METHOD_1 = "methodSave";
  private static final String TEST_RPC_METHOD_2 = "methodRead";
  private static final Collection<String> TEST_RPC_METHODS_SINGLE = Collections.singletonList( TEST_RPC_METHOD_1 );
  private static final Collection<String> TEST_RPC_METHODS_MULTIPLE =
    Arrays.asList( TEST_RPC_METHOD_1, TEST_RPC_METHOD_2 );

  // region Helpers
  static class TestRequestMatcher extends AbstractGwtRpcRequestMatcher {

    public TestRequestMatcher( @NonNull String pattern,
                               @Nullable Collection<String> rpcMethods,
                               @Nullable IGwtRpcSerializationPolicyCache serializationPolicyCache ) {
      super( pattern, rpcMethods, serializationPolicyCache );
    }

    public TestRequestMatcher( @NonNull String pattern,
                               boolean isCaseInsensitive,
                               @Nullable Collection<String> rpcMethods,
                               @Nullable IGwtRpcSerializationPolicyCache serializationPolicyCache ) {
      super( pattern, isCaseInsensitive, rpcMethods, serializationPolicyCache );
    }

    @NonNull @Override
    protected AbstractGwtRpc getGwtRpc( @NonNull HttpServletRequest httpRequest ) {
      // To be stubbed.
      throw new UnsupportedOperationException();
    }
  }

  @NonNull
  private HttpServletRequest createRequestMock( @NonNull String httpMethod, @NonNull String path ) {
    HttpServletRequest request = mock( HttpServletRequest.class );
    when( request.getMethod() ).thenReturn( httpMethod );
    when( request.getServletPath() ).thenReturn( "" );
    when( request.getPathInfo() ).thenReturn( path );
    when( request.getQueryString() ).thenReturn( null );

    return request;
  }

  // This method is used below to obtain a legitimate reflection Method instance.
  // ~ TEST_RPC_METHOD_1
  public void methodSave() {
  }

  // This method is used below to obtain a legitimate reflection Method instance.
  // ~ TEST_RPC_METHOD_2
  public void methodSaveSomething() {
  }

  @NonNull
  private AbstractGwtRpc createGwtRpcMock( @NonNull String serviceMethodName ) throws NoSuchMethodException {

    Method serviceMethodMock = this.getClass().getMethod( serviceMethodName );

    RPCRequest rpcRequestMock = new RPCRequest( serviceMethodMock, new Object[] {}, null, 0 );

    AbstractGwtRpc gwtRpcMock = mock( AbstractGwtRpc.class );
    when( gwtRpcMock.getRequest() ).thenReturn( rpcRequestMock );

    return gwtRpcMock;
  }
  // endregion

  // region RpcMethods
  @Test
  public void testRpcMethodsSingleMatch() {
    AbstractGwtRpcRequestMatcher matcher = spy( new TestRequestMatcher( TEST_PATTERN, TEST_RPC_METHODS_SINGLE, null ) );
    assertEquals( TEST_RPC_METHODS_SINGLE, matcher.getRpcMethodNames() );

    HttpServletRequest request = createRequestMock( HttpMethod.POST, TEST_PATH );

    // ---

    doReturn( TEST_RPC_METHOD_1 ).when( matcher ).getRpcMethodName( any() );
    assertTrue( matcher.test( request ) );

    // ---

    doReturn( TEST_RPC_METHOD_1 + "_SOMETHING" ).when( matcher ).getRpcMethodName( any() );
    assertFalse( matcher.test( request ) );
  }

  @Test
  public void testRpcMethodsMultipleMatch() {
    AbstractGwtRpcRequestMatcher matcher =
      spy( new TestRequestMatcher( TEST_PATTERN, TEST_RPC_METHODS_MULTIPLE, null ) );
    assertEquals( TEST_RPC_METHODS_MULTIPLE, matcher.getRpcMethodNames() );

    HttpServletRequest request = createRequestMock( HttpMethod.POST, TEST_PATH );

    // ---

    doReturn( TEST_RPC_METHOD_1 ).when( matcher ).getRpcMethodName( any() );
    assertTrue( matcher.test( request ) );

    // ---

    doReturn( TEST_RPC_METHOD_2 ).when( matcher ).getRpcMethodName( any() );
    assertTrue( matcher.test( request ) );

    // ---

    doReturn( TEST_RPC_METHOD_1 + "_SOMETHING" ).when( matcher ).getRpcMethodName( any() );
    assertFalse( matcher.test( request ) );
  }

  @Test
  public void testRpcMethodsAllMatch() {
    AbstractGwtRpcRequestMatcher matcher = spy( new TestRequestMatcher( TEST_PATTERN, null, null ) );
    assertNull( matcher.getRpcMethodNames() );

    HttpServletRequest request = createRequestMock( HttpMethod.POST, TEST_PATH );

    // ---

    doReturn( TEST_RPC_METHOD_1 ).when( matcher ).getRpcMethodName( any() );
    assertTrue( matcher.test( request ) );

    // ---

    doReturn( TEST_RPC_METHOD_1 + "_SOMETHING" ).when( matcher ).getRpcMethodName( any() );
    assertTrue( matcher.test( request ) );
  }
  // endregion

  // region Path Case Sensitivity
  @Test
  public void testPathCaseInsensitiveMatch() {

    AbstractGwtRpcRequestMatcher matcher = spy( new TestRequestMatcher(
      TEST_PATTERN,
      true,
      TEST_RPC_METHODS_SINGLE,
      null ) );

    doReturn( TEST_RPC_METHOD_1 ).when( matcher ).getRpcMethodName( any() );

    HttpServletRequest request = createRequestMock( HttpMethod.POST, TEST_PATH );
    assertTrue( matcher.test( request ) );

    request = createRequestMock( HttpMethod.POST, TEST_PATH_ALTERNATE_CASE );
    assertTrue( matcher.test( request ) );
  }

  @Test
  public void testPathCaseSensitiveMatch() {

    AbstractGwtRpcRequestMatcher matcher = spy( new TestRequestMatcher(
      TEST_PATTERN,
      false,
      TEST_RPC_METHODS_SINGLE,
      null ) );

    doReturn( TEST_RPC_METHOD_1 ).when( matcher ).getRpcMethodName( any() );

    HttpServletRequest request = createRequestMock( HttpMethod.POST, TEST_PATH );
    assertTrue( matcher.test( request ) );

    request = createRequestMock( HttpMethod.POST, TEST_PATH_ALTERNATE_CASE );
    assertFalse( matcher.test( request ) );

    // ---
    // Default is false

    matcher = spy( new TestRequestMatcher(
      TEST_PATTERN,
      TEST_RPC_METHODS_SINGLE,
      null ) );

    doReturn( TEST_RPC_METHOD_1 ).when( matcher ).getRpcMethodName( any() );

    request = createRequestMock( HttpMethod.POST, TEST_PATH );
    assertTrue( matcher.test( request ) );

    request = createRequestMock( HttpMethod.POST, TEST_PATH_ALTERNATE_CASE );
    assertFalse( matcher.test( request ) );
  }
  // endregion

  // region Http Method
  @Test
  public void testHttpMethodMustBePOSTMatch() {

    AbstractGwtRpcRequestMatcher matcher = spy( new TestRequestMatcher( TEST_PATTERN, TEST_RPC_METHODS_SINGLE, null ) );
    doReturn( TEST_RPC_METHOD_1 ).when( matcher ).getRpcMethodName( any() );

    HttpServletRequest request = createRequestMock( HttpMethod.POST, TEST_PATH );
    assertTrue( matcher.test( request ) );

    // ---

    request = createRequestMock( HttpMethod.GET, TEST_PATH );
    assertFalse( matcher.test( request ) );
  }
  // endregion

  // region getGwtRpc integration
  @Test
  public void testMatchWithMethodNameFromGwtRpc() throws NoSuchMethodException {
    HttpServletRequest request = createRequestMock( HttpMethod.POST, TEST_PATH );
    AbstractGwtRpcRequestMatcher matcher = spy( new TestRequestMatcher( TEST_PATTERN, TEST_RPC_METHODS_SINGLE, null ) );

    // ---

    AbstractGwtRpc gwtRpcMock = createGwtRpcMock( TEST_RPC_METHOD_1 );

    doReturn( gwtRpcMock ).when( matcher ).getGwtRpc( any() );

    assertTrue( matcher.test( request ) );

    // ---

    gwtRpcMock = createGwtRpcMock( TEST_RPC_METHOD_1 + "Something" );

    doReturn( gwtRpcMock ).when( matcher ).getGwtRpc( any() );

    assertFalse( matcher.test( request ) );
  }

  @Test
  public void testMatchFailsWhenGetGwtRpcThrows() {
    HttpServletRequest request = createRequestMock( HttpMethod.POST, TEST_PATH );
    AbstractGwtRpcRequestMatcher matcher = spy( new TestRequestMatcher( TEST_PATTERN, TEST_RPC_METHODS_SINGLE, null ) );

    // ---

    doThrow( GwtRpcProxyException.class ).when( matcher ).getGwtRpc( any() );

    assertFalse( matcher.test( request ) );
  }
  // endregion

  // region Serialization Policy Cache
  @Test
  public void testConstructWithSerializationPolicyCache() {
    IGwtRpcSerializationPolicyCache cache = mock( IGwtRpcSerializationPolicyCache.class );

    AbstractGwtRpcRequestMatcher matcher = new TestRequestMatcher( TEST_PATTERN, TEST_RPC_METHODS_SINGLE, cache );

    assertEquals( cache, matcher.getSerializationPolicyCache() );
  }
  // endregion
}
