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

import org.pentaho.platform.web.WebUtil;

import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockFilterConfig;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.mockito.Mockito;
import org.springframework.security.web.csrf.CsrfToken;

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

    this.mockRequest = Mockito.mock( HttpServletRequest.class );
    this.mockResponse = Mockito.mock( HttpServletResponse.class );
    this.filter = new CsrfTokenResponseHeaderFilter();
    this.filterChain = new MockFilterChain();
  }

  private static CsrfToken createToken() {

    CsrfToken token = Mockito.mock( CsrfToken.class );

    Mockito.when( token.getHeaderName() ).thenReturn( RESPONSE_HEADER_VALUE );
    Mockito.when( token.getParameterName() ).thenReturn( RESPONSE_PARAM_VALUE );
    Mockito.when( token.getToken() ).thenReturn( RESPONSE_TOKEN_VALUE );

    return token;
  }

  @Test( expected = ServletException.class )
  public void testWhenNoCsrfTokenThenThrow() throws Exception {

    MockFilterConfig cfg = new MockFilterConfig();

    filter.init( cfg );

    filter.doFilter( this.mockRequest, this.mockResponse, this.filterChain );

    Mockito.verify( mockResponse, Mockito.never() ).setHeader( Mockito.anyString(), Mockito.anyString() );
  }

  @Test
  public void testWhenCsrfTokenThenCsrfResponseHeaders() throws Exception {

    MockFilterConfig cfg = new MockFilterConfig();

    filter.init( cfg );

    CsrfToken token = createToken();

    Mockito.when( this.mockRequest.getAttribute( CsrfTokenResponseHeaderFilter.REQUEST_ATTRIBUTE_NAME ) )
        .thenReturn( token );

    filter.doFilter( this.mockRequest, this.mockResponse, this.filterChain );

    Mockito.verify( mockResponse, Mockito.times( 1 ))
            .setHeader( CsrfTokenResponseHeaderFilter.RESPONSE_HEADER_NAME, RESPONSE_HEADER_VALUE );

    Mockito.verify( mockResponse, Mockito.times( 1 ))
            .setHeader( CsrfTokenResponseHeaderFilter.RESPONSE_PARAM_NAME, RESPONSE_PARAM_VALUE );

    Mockito.verify( mockResponse, Mockito.times( 1 ))
            .setHeader( CsrfTokenResponseHeaderFilter.RESPONSE_TOKEN_NAME, RESPONSE_TOKEN_VALUE );
  }

  @Test
  public void TestWhenCsrfTokenThenCorsResponseHeaders() throws Exception {

    MockFilterConfig cfg = new MockFilterConfig();

    filter.init( cfg );

    CsrfToken token = createToken();

    Mockito.when( this.mockRequest.getAttribute( CsrfTokenResponseHeaderFilter.REQUEST_ATTRIBUTE_NAME ) )
        .thenReturn( token );

    PowerMockito.mockStatic( WebUtil.class );

    filter.doFilter( this.mockRequest, this.mockResponse, this.filterChain );

    PowerMockito.verifyStatic( Mockito.times( 1 ) );
    WebUtil.setCorsResponseHeaders( this.mockRequest, this.mockResponse );
  }

  @Test
  public void TestStatusCodeNoContent() throws Exception {

    MockFilterConfig cfg = new MockFilterConfig();

    filter.init( cfg );

    CsrfToken token = createToken();

    Mockito.when( this.mockRequest.getAttribute( CsrfTokenResponseHeaderFilter.REQUEST_ATTRIBUTE_NAME ) )
        .thenReturn( token );

    filter.doFilter( this.mockRequest, this.mockResponse, this.filterChain );

    Mockito.verify( mockResponse, Mockito.times( 1 ))
        .setStatus( HttpStatus.SC_NO_CONTENT );
  }
}
