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
 * Copyright (c) 2019 Hitachi Vantara. All rights reserved.
 */
package org.pentaho.platform.web.http.security;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.junit.Before;
import org.junit.Test;

import com.sun.jersey.api.client.Client;

import javax.ws.rs.core.MultivaluedMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CsrfUtilTest {

  private Client mockClient;
  private WebResource mockResourcePre;
  private WebResource mockResource;
  private WebResource.Builder mockRequestBuilder;
  private ClientResponse mockClientReponse;
  private MultivaluedMap<String, String> mockResponseHeaders;

  private final static String TEST_CONTEXT_URL = "/pentaho";
  private final static String TEST_PROTECTED_SERVICE_URL = "http://mydomain.com:8080/pentaho/abcProtected";
  private final static String TEST_CSRF_TOKEN = "test-token-value";
  private final static String TEST_CSRF_HEADER = "test-token-header";
  private final static String TEST_CSRF_PARAMETER = "test-token-param";

  @Before
  public void setUp() throws Exception {
    mockClient = mock( Client.class );
    mockResourcePre = mock( WebResource.class );
    mockResource = mock( WebResource.class );
    mockRequestBuilder = mock( WebResource.Builder.class );
    mockClientReponse = mock( ClientResponse.class );
    mockResponseHeaders = (MultivaluedMap<String, String>) mock( MultivaluedMap.class );
  }

  private void setupGetCsrfToken() {
    when( this.mockClient.resource( anyString() ) ).thenReturn( this.mockResourcePre );
    when( this.mockResourcePre.queryParam( anyString(), anyString() ) ).thenReturn( this.mockResource );
    when( this.mockResource.getRequestBuilder() ).thenReturn( this.mockRequestBuilder );
    when( this.mockRequestBuilder.get( eq( ClientResponse.class ) ) ).thenReturn( this.mockClientReponse );
  }

  @Test
  public void testGetCsrfTokenWhenServiceResponseIsNull() {

    setupGetCsrfToken();

    when( this.mockRequestBuilder.get( eq( ClientResponse.class ) ) ).thenReturn( null );

    CsrfToken token = CsrfUtil.getCsrfToken( this.mockClient, TEST_CONTEXT_URL, TEST_PROTECTED_SERVICE_URL);

    assertNull( token );
  }

  @Test
  public void testGetCsrfTokenCallsCsrfService() {

    setupGetCsrfToken();

    when( this.mockRequestBuilder.get( eq( ClientResponse.class ) ) ).thenReturn( null );

    CsrfToken token = CsrfUtil.getCsrfToken( this.mockClient, TEST_CONTEXT_URL, TEST_PROTECTED_SERVICE_URL);

    assertNull( token );

    final String csrfServiceUrl = TEST_CONTEXT_URL + CsrfUtil.API_SYSTEM_CSRF;

    verify( this.mockClient, times( 1 ) ).resource( eq( csrfServiceUrl ) );
    verify( this.mockResourcePre, times( 1 ) )
        .queryParam( eq( CsrfUtil.API_SYSTEM_CSRF_PARAM_URL ), eq( TEST_PROTECTED_SERVICE_URL) );

    verify( this.mockResource, times( 1 ) ).getRequestBuilder();
    verify( this.mockRequestBuilder, times( 1 ) ).get( eq( ClientResponse.class ) );
  }

  @Test
  public void testGetCsrfTokenWhenServiceResponseIsNot200Or204() {

    setupGetCsrfToken();

    CsrfToken token = CsrfUtil.getCsrfToken( this.mockClient, TEST_CONTEXT_URL, TEST_PROTECTED_SERVICE_URL );

    when( this.mockClientReponse.getStatus() ).thenReturn( 210 );

    assertNull( token );
  }

  @Test
  public void testGetCsrfTokenWhenServiceResponseIs200AndTokenIsPresent() {

    setupGetCsrfToken();

    when( this.mockClientReponse.getStatus() ).thenReturn( 200 );
    when( this.mockClientReponse.getHeaders() ).thenReturn( this.mockResponseHeaders );
    when( this.mockResponseHeaders.getFirst( eq( CsrfUtil.API_SYSTEM_CSRF_RESPONSE_HEADER_TOKEN ) ) )
        .thenReturn( TEST_CSRF_TOKEN );
    when( this.mockResponseHeaders.getFirst( eq( CsrfUtil.API_SYSTEM_CSRF_RESPONSE_HEADER_HEADER ) ) )
        .thenReturn( TEST_CSRF_HEADER );
    when( this.mockResponseHeaders.getFirst( eq( CsrfUtil.API_SYSTEM_CSRF_RESPONSE_HEADER_PARAM ) ) )
        .thenReturn( TEST_CSRF_PARAMETER );

    CsrfToken token = CsrfUtil.getCsrfToken( this.mockClient, TEST_CONTEXT_URL, TEST_PROTECTED_SERVICE_URL);

    assertNotNull( token );

    assertEquals( token.getHeader(), TEST_CSRF_HEADER );
    assertEquals( token.getParameter(), TEST_CSRF_PARAMETER );
    assertEquals( token.getToken(), TEST_CSRF_TOKEN );
  }

  @Test
  public void testGetCsrfTokenWhenServiceResponseIs204AndTokenIsPresent() {

    setupGetCsrfToken();

    when( this.mockClientReponse.getStatus() ).thenReturn( 204 );
    when( this.mockClientReponse.getHeaders() ).thenReturn( this.mockResponseHeaders );
    when( this.mockResponseHeaders.getFirst( CsrfUtil.API_SYSTEM_CSRF_RESPONSE_HEADER_TOKEN ) )
        .thenReturn( TEST_CSRF_TOKEN );
    when( this.mockResponseHeaders.getFirst( CsrfUtil.API_SYSTEM_CSRF_RESPONSE_HEADER_HEADER ) )
        .thenReturn( TEST_CSRF_HEADER );
    when( this.mockResponseHeaders.getFirst( CsrfUtil.API_SYSTEM_CSRF_RESPONSE_HEADER_PARAM ) )
        .thenReturn( TEST_CSRF_PARAMETER );

    CsrfToken token = CsrfUtil.getCsrfToken( this.mockClient, TEST_CONTEXT_URL, TEST_PROTECTED_SERVICE_URL);

    assertNotNull( token );

    assertEquals( token.getHeader(), TEST_CSRF_HEADER );
    assertEquals( token.getParameter(), TEST_CSRF_PARAMETER );
    assertEquals( token.getToken(), TEST_CSRF_TOKEN );
  }
}

