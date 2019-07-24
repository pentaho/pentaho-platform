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
 * Copyright (c) 2019 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.web.http.security;

import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.verification.VerificationMode;
import org.pentaho.platform.web.WebUtil;

import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockFilterConfig;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.pentaho.platform.web.http.security.CsrfTokenResponseHeaderFilter.REQUEST_ATTRIBUTE_NAME;
import static org.pentaho.platform.web.http.security.CsrfTokenResponseHeaderFilter.RESPONSE_HEADER_NAME;
import static org.pentaho.platform.web.http.security.CsrfTokenResponseHeaderFilter.RESPONSE_PARAM_NAME;
import static org.pentaho.platform.web.http.security.CsrfTokenResponseHeaderFilter.RESPONSE_TOKEN_NAME;

@RunWith( PowerMockRunner.class )
@PrepareForTest( WebUtil.class )
public class CsrfTokenResponseHeaderFilterTest {

  private HttpServletRequest mockRequest;
  private HttpServletResponse mockResponse;
  private CsrfTokenResponseHeaderFilter filter;
  private FilterChain filterChain;

  private static final String RESPONSE_HEADER_VALUE = "HEADER_NAME";
  private static final String RESPONSE_PARAM_VALUE = "PARAM_NAME";
  private static final String RESPONSE_TOKEN_VALUE = "TOKEN";

  @Before
  public void setUp() throws Exception {

    this.mockRequest = mock( HttpServletRequest.class );
    this.mockResponse = mock( HttpServletResponse.class );

    this.filter = spy( new CsrfTokenResponseHeaderFilter() );
    this.filterChain = new MockFilterChain();
  }

  private static CsrfToken createToken() {

    CsrfToken token = mock( CsrfToken.class );

    when( token.getHeaderName() ).thenReturn( RESPONSE_HEADER_VALUE );
    when( token.getParameterName() ).thenReturn( RESPONSE_PARAM_VALUE );
    when( token.getToken() ).thenReturn( RESPONSE_TOKEN_VALUE );

    return token;
  }

  public void testWhenNoCsrfTokenInRequest() throws Exception {

    MockFilterConfig cfg = new MockFilterConfig();

    filter.init( cfg );

    filter.doFilter( this.mockRequest, this.mockResponse, this.filterChain );

    verify( mockResponse, never() ).setHeader( anyString(), anyString() );
  }

  @Test
  public void testWhenCsrfTokenThenCsrfResponseHeaders() throws Exception {

    MockFilterConfig cfg = new MockFilterConfig();

    filter.init( cfg );

    CsrfToken token = createToken();

    when( this.mockRequest.getAttribute( REQUEST_ATTRIBUTE_NAME ) ).thenReturn( token );

    filter.doFilter( this.mockRequest, this.mockResponse, this.filterChain );

    verify( mockResponse, once() ).setHeader( RESPONSE_HEADER_NAME, RESPONSE_HEADER_VALUE );
    verify( mockResponse, once() ).setHeader( RESPONSE_PARAM_NAME, RESPONSE_PARAM_VALUE );
    verify( mockResponse, once() ).setHeader( RESPONSE_TOKEN_NAME, RESPONSE_TOKEN_VALUE );
  }

  @Test
  public void TestWhenCsrfTokenThenCorsResponseHeaders() throws Exception {
    MockFilterConfig cfg = new MockFilterConfig();

    filter.init( cfg );

    CsrfToken token = createToken();

    when( this.mockRequest.getAttribute( REQUEST_ATTRIBUTE_NAME ) ).thenReturn( token );

    Map<String, List<String>> configuration = Collections.emptyMap();
    when( this.filter.getCorsHeadersConfiguration() ).thenReturn( configuration );

    PowerMockito.mockStatic( WebUtil.class );

    filter.doFilter( this.mockRequest, this.mockResponse, this.filterChain );

    PowerMockito.verifyStatic( once() );
    WebUtil.setCorsResponseHeaders( this.mockRequest, this.mockResponse, configuration );
  }

  @Test
  public void TestStatusCodeNoContent() throws Exception {

    MockFilterConfig cfg = new MockFilterConfig();

    filter.init( cfg );

    CsrfToken token = createToken();

    when( this.mockRequest.getAttribute( REQUEST_ATTRIBUTE_NAME ) ).thenReturn( token );

    filter.doFilter( this.mockRequest, this.mockResponse, this.filterChain );

    verify( mockResponse, once() ).setStatus( HttpStatus.SC_NO_CONTENT );
  }

  private VerificationMode once() {
    return times( 1 );
  }
}
