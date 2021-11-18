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

import edu.umd.cs.findbugs.annotations.NonNull;
import org.junit.Test;
import org.pentaho.platform.web.gwt.rpc.AbstractGwtRpc;
import org.pentaho.platform.web.gwt.rpc.IGwtRpcSerializationPolicyCache;
import org.pentaho.platform.web.gwt.rpc.SystemGwtRpc;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.HttpMethod;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SystemGwtRpcRequestMatcherTest {
  private static final String HTTP_GWT_RPC_ATTRIBUTE = AbstractGwtRpc.class.getSimpleName();

  private static final String TEST_PATH = "/ws/gwt/unifiedRepository";
  private static final String TEST_PATH_ALTERNATE_CASE = "/ws/gwt/UnifiedRepository";
  private static final String TEST_PATTERN = "^/ws/gwt/unifiedRepository\\b.*";
  private static final String TEST_RPC_METHOD_1 = "methodSave";
  private static final String TEST_RPC_METHOD_2 = "methodRead";
  private static final Collection<String> TEST_RPC_METHODS_SINGLE = Collections.singletonList( TEST_RPC_METHOD_1 );
  private static final Collection<String> TEST_RPC_METHODS_MULTIPLE =
    Arrays.asList( TEST_RPC_METHOD_1, TEST_RPC_METHOD_2 );

  // region Helpers

  @NonNull
  private HttpServletRequest createRequestMock( @NonNull String httpMethod, @NonNull String path ) {
    HttpServletRequest request = mock( HttpServletRequest.class );
    when( request.getMethod() ).thenReturn( httpMethod );
    when( request.getServletPath() ).thenReturn( "" );
    when( request.getPathInfo() ).thenReturn( path );
    when( request.getQueryString() ).thenReturn( null );

    return request;
  }
  // endregion

  // region RpcMethods
  @Test
  public void testRpcMethodsSingleMatch() {
    SystemGwtRpcRequestMatcher matcher =
      spy( new SystemGwtRpcRequestMatcher( TEST_PATTERN, TEST_RPC_METHODS_SINGLE, null ) );
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
    SystemGwtRpcRequestMatcher matcher =
      spy( new SystemGwtRpcRequestMatcher( TEST_PATTERN, TEST_RPC_METHODS_MULTIPLE, null ) );
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
    SystemGwtRpcRequestMatcher matcher = spy( new SystemGwtRpcRequestMatcher( TEST_PATTERN, null, null ) );
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

    SystemGwtRpcRequestMatcher matcher = spy( new SystemGwtRpcRequestMatcher(
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

    SystemGwtRpcRequestMatcher matcher = spy( new SystemGwtRpcRequestMatcher(
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

    matcher = spy( new SystemGwtRpcRequestMatcher(
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

  // region getGwtRpc integration
  @Test
  public void testGetGwtRpcReturnsASystemGwtRpc() {

    HttpServletRequest requestMock = createRequestMock( HttpMethod.POST, TEST_PATH );
    IGwtRpcSerializationPolicyCache serializationPolicyCache = ( moduleBaseURL, strongName, sourceProvider ) -> {
      throw new UnsupportedOperationException();
    };

    SystemGwtRpcRequestMatcher matcher =
      spy( new SystemGwtRpcRequestMatcher( TEST_PATTERN, TEST_RPC_METHODS_SINGLE, serializationPolicyCache ) );

    // ---

    AbstractGwtRpc gwtRpc = matcher.getGwtRpc( requestMock );

    assertNotNull( gwtRpc );
    assertTrue( gwtRpc instanceof SystemGwtRpc );
    assertEquals( requestMock, gwtRpc.getServletRequest() );
    assertEquals( serializationPolicyCache, gwtRpc.getSerializationPolicyCache() );
  }

  @Test
  public void testGetGwtRpcCachesSystemGwtRpcOnRequest() {

    HttpServletRequest requestMock = createRequestMock( HttpMethod.POST, TEST_PATH );

    SystemGwtRpcRequestMatcher matcher =
      spy( new SystemGwtRpcRequestMatcher( TEST_PATTERN, TEST_RPC_METHODS_SINGLE, null ) );

    // ---

    AbstractGwtRpc gwtRpc1 = matcher.getGwtRpc( requestMock );

    verify( requestMock, times( 1 ) ).getAttribute( HTTP_GWT_RPC_ATTRIBUTE );
    verify( requestMock, times( 1 ) ).setAttribute( HTTP_GWT_RPC_ATTRIBUTE, gwtRpc1 );

    // ---

    when( requestMock.getAttribute( HTTP_GWT_RPC_ATTRIBUTE ) ).thenReturn( gwtRpc1 );

    AbstractGwtRpc gwtRpc2 = matcher.getGwtRpc( requestMock );

    assertEquals( gwtRpc1, gwtRpc2 );
  }
  // endregion
}
