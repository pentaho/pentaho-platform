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

package org.pentaho.platform.web;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.spy;

@RunWith( PowerMockRunner.class )
@PrepareForTest( WebUtil.class )
public class WebUtilTest {

  private HttpServletRequest mockRequest;
  private HttpServletResponse mockResponse;

  @Before
  public void setup() {
    this.mockRequest = mock( HttpServletRequest.class );
    this.mockResponse = mock( HttpServletResponse.class );
  }

  @Test
  public void testSetCorsHeadersNormalRequest() {

    this.setupCorsTest();

    verify( this.mockResponse, never() ).setHeader( eq( WebUtil.CORS_ALLOW_ORIGIN_HEADER ), anyString() );
    verify( this.mockResponse, never() ).setHeader( eq( WebUtil.CORS_ALLOW_CREDENTIALS_HEADER ), anyString() );
  }

  @Test
  public void testSetCorsHeadersRequestsNotAllowed() {

    this.setupCorsTest( "false", null );

    when( this.mockRequest.getHeader( WebUtil.ORIGIN_HEADER ) ).thenReturn( "foobar.com" );

    verify( this.mockResponse, never() ).setHeader( eq( WebUtil.CORS_ALLOW_ORIGIN_HEADER ), anyString() );
    verify( this.mockResponse, never() ).setHeader( eq( WebUtil.CORS_ALLOW_CREDENTIALS_HEADER ), anyString() );
  }

  @Test
  public void testSetCorsHeadersDomainNotAllowed() {
    when( this.mockRequest.getHeader( WebUtil.ORIGIN_HEADER ) ).thenReturn( "foobar.com" );

    this.setupCorsTest( "true", null );

    verify( this.mockResponse, never() ).setHeader( eq( WebUtil.CORS_ALLOW_ORIGIN_HEADER ), anyString() );
    verify( this.mockResponse, never() ).setHeader( eq( WebUtil.CORS_ALLOW_CREDENTIALS_HEADER ), anyString() );
  }

  @Test
  public void testSetCorsHeadersRequestAndDomainAllowed() {
    String domain = "foobar.com";
    when( this.mockRequest.getHeader( WebUtil.ORIGIN_HEADER ) ).thenReturn( domain );

    this.setupCorsTest( "true", domain );

    verify( this.mockResponse ).setHeader( eq( WebUtil.CORS_ALLOW_ORIGIN_HEADER ), eq( domain ) );
    verify( this.mockResponse ).setHeader( eq( WebUtil.CORS_ALLOW_CREDENTIALS_HEADER ), eq( "true" ) );
  }

  @Test
  public void testSetCorsHeadersRequestAndDomainAllowedMultiple() {
    String domain = "localhost:1337";
    String allowedDomains = "foobar.com, " + domain;
    when( this.mockRequest.getHeader( WebUtil.ORIGIN_HEADER ) ).thenReturn( domain );

    this.setupCorsTest( "true", allowedDomains );

    verify( this.mockResponse ).setHeader( eq( WebUtil.CORS_ALLOW_ORIGIN_HEADER ), eq( domain ) );
    verify( this.mockResponse ).setHeader( eq( WebUtil.CORS_ALLOW_CREDENTIALS_HEADER ), eq( "true" ) );
  }

  private void setupCorsTest() {
    this.setupCorsTest( null, null );
  }

  private void setupCorsTest( String corsAllowed, String corsAllowedDomains ) {
    spy( WebUtil.class );

    when( WebUtil.getCorsRequestsAllowedSystemProperty() ).thenReturn( corsAllowed );
    when( WebUtil.getCorsAllowedDomainsSystemProperty() ).thenReturn( corsAllowedDomains );

    WebUtil.setCorsResponseHeaders( this.mockRequest, this.mockResponse );
  }
}
