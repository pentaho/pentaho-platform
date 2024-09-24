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

package org.pentaho.platform.web.gwt.rpc.support;

import com.google.gwt.user.server.rpc.SerializationPolicy;
import com.google.gwt.user.server.rpc.SerializationPolicyProvider;
import org.junit.Test;
import org.pentaho.platform.web.gwt.rpc.IGwtRpcSerializationPolicyCache;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GwtRpcSerializationPolicyCacheTest {

  // region Helpers
  private static void testSerializationPolicyIsNewAndCreated( IGwtRpcSerializationPolicyCache cache,
                                                              String moduleBaseURL,
                                                              String strongName,
                                                              SerializationPolicy policy ) {

    SerializationPolicyProvider sourceProviderMock = createProviderMock( moduleBaseURL, strongName, policy );

    SerializationPolicy result = cache.getSerializationPolicy( moduleBaseURL, strongName, sourceProviderMock );

    assertEquals( policy, result );

    verify( sourceProviderMock, times( 1 ) ).getSerializationPolicy( moduleBaseURL, strongName );
  }

  private static SerializationPolicyProvider createProviderMock( String moduleBaseURL,
                                                                 String strongName,
                                                                 SerializationPolicy policy ) {
    SerializationPolicyProvider sourceProviderMock = mock( SerializationPolicyProvider.class );
    when( sourceProviderMock.getSerializationPolicy( moduleBaseURL, strongName ) )
      .thenReturn( policy );

    return sourceProviderMock;
  }

  private static void testSerializationPolicyIsInCache( IGwtRpcSerializationPolicyCache cache,
                                                        String moduleBaseURL,
                                                        String strongName,
                                                        SerializationPolicy policy ) {

    SerializationPolicyProvider sourceProviderMock = createProviderMock( moduleBaseURL, strongName, policy );

    SerializationPolicy result = cache.getSerializationPolicy( moduleBaseURL, strongName, sourceProviderMock );

    assertEquals( policy, result );

    verify( sourceProviderMock, times( 0 ) ).getSerializationPolicy( any(), any() );
  }
  // endregion

  @Test
  public void testGetSerializationPolicyWithNullKeysIsSupported() {
    GwtRpcSerializationPolicyCache cache = new GwtRpcSerializationPolicyCache();
    SerializationPolicy policy = mock( SerializationPolicy.class );

    testSerializationPolicyIsNewAndCreated( cache, null, null, policy );
    testSerializationPolicyIsInCache( cache, null, null, policy );
  }

  @Test
  public void testGetSerializationPolicyWithNormalKeyIsSupported() {
    GwtRpcSerializationPolicyCache cache = new GwtRpcSerializationPolicyCache();
    SerializationPolicy policy = mock( SerializationPolicy.class );

    testSerializationPolicyIsNewAndCreated( cache, "url", "abc", policy );
    testSerializationPolicyIsInCache( cache, "url", "abc", policy );
  }

  @Test
  public void testGetSerializationPolicyWithMultipleKeysMaintainsCache() {
    GwtRpcSerializationPolicyCache cache = new GwtRpcSerializationPolicyCache();
    SerializationPolicy policy1 = mock( SerializationPolicy.class );
    String moduleBaseURL1 = "url1";
    String strongName1 = "abc1";

    SerializationPolicy policy2 = mock( SerializationPolicy.class );
    String moduleBaseURL2 = "url2";
    String strongName2 = "abc2";

    SerializationPolicy policy3 = mock( SerializationPolicy.class );
    String moduleBaseURL3 = "url3";
    String strongName3 = "abc3";

    testSerializationPolicyIsNewAndCreated( cache, moduleBaseURL1, strongName1, policy1 );
    testSerializationPolicyIsNewAndCreated( cache, moduleBaseURL2, strongName2, policy2 );
    testSerializationPolicyIsNewAndCreated( cache, moduleBaseURL3, strongName3, policy3 );

    testSerializationPolicyIsInCache( cache, moduleBaseURL1, strongName1, policy1 );
    testSerializationPolicyIsInCache( cache, moduleBaseURL2, strongName2, policy2 );
    testSerializationPolicyIsInCache( cache, moduleBaseURL3, strongName3, policy3 );
  }
}
