/*
 * ******************************************************************************
 *
 * Copyright (C) 2017 by Hitachi Vantara : http://www.pentaho.com
 *
 * ******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.pentaho.platform.web.servlet;

import org.apache.commons.logging.Log;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ServletBaseTest {

  private ServletBase servletBase;
  private HttpServletRequest mockRequest;
  private HttpServletResponse mockResponse;

  @Before
  public void setup() {

    this.servletBase = spy( new ServletBase() {
      @Override
      public Log getLogger() {
        return null;
      }
    } );

    this.mockRequest = mock( HttpServletRequest.class );
    this.mockResponse = mock( HttpServletResponse.class );
  }

  @Test
  public void testSetCorsHeadersNormalRequest() {
    this.setupCorsTest();

    verify( this.mockResponse, never() ).setHeader( eq( ServletBase.CORS_ALLOW_ORIGIN_HEADER ), anyString() );
    verify( this.mockResponse, never() ).setHeader( eq( ServletBase.CORS_ALLOW_CREDENTIALS_HEADER ), anyString() );
  }

  @Test
  public void testSetCorsHeadersRequestsNotAllowed() {
    this.setupCorsTest( "false", null );
    when( this.mockRequest.getHeader( ServletBase.ORIGIN_HEADER ) ).thenReturn( "foobar.com" );

    verify( this.mockResponse, never() ).setHeader( eq( ServletBase.CORS_ALLOW_ORIGIN_HEADER ), anyString() );
    verify( this.mockResponse, never() ).setHeader( eq( ServletBase.CORS_ALLOW_CREDENTIALS_HEADER ), anyString() );
  }

  @Test
  public void testSetCorsHeadersDomainNotAllowed() {
    when( this.mockRequest.getHeader( ServletBase.ORIGIN_HEADER ) ).thenReturn( "foobar.com" );

    this.setupCorsTest( "true", null );

    verify( this.mockResponse, never() ).setHeader( eq( ServletBase.CORS_ALLOW_ORIGIN_HEADER ), anyString() );
    verify( this.mockResponse, never() ).setHeader( eq( ServletBase.CORS_ALLOW_CREDENTIALS_HEADER ), anyString() );
  }

  @Test
  public void testSetCorsHeadersRequestAndDomainAllowed() {
    String domain = "foobar.com";
    when( this.mockRequest.getHeader( ServletBase.ORIGIN_HEADER ) ).thenReturn( domain );

    this.setupCorsTest( "true", domain );

    verify( this.mockResponse ).setHeader( eq( ServletBase.CORS_ALLOW_ORIGIN_HEADER ), eq( domain ) );
    verify( this.mockResponse ).setHeader( eq( ServletBase.CORS_ALLOW_CREDENTIALS_HEADER ), eq( "true" ) );
  }

  @Test
  public void testSetCorsHeadersRequestAndDomainAllowedMultiple() {
    String domain = "localhost:1337";
    String allowedDomains = "foobar.com, " + domain;
    when( this.mockRequest.getHeader( ServletBase.ORIGIN_HEADER ) ).thenReturn( domain );

    this.setupCorsTest( "true", allowedDomains );

    verify( this.mockResponse ).setHeader( eq( ServletBase.CORS_ALLOW_ORIGIN_HEADER ), eq( domain ) );
    verify( this.mockResponse ).setHeader( eq( ServletBase.CORS_ALLOW_CREDENTIALS_HEADER ), eq( "true" ) );
  }

  private void setupCorsTest() {
    this.setupCorsTest( null, null );
  }

  private void setupCorsTest( String corsAllowed, String corsAllowedDomains ) {
    doReturn( corsAllowed ).when( this.servletBase ).getCorsRequestsAllowedSystemProperty();
    doReturn( corsAllowedDomains ).when( this.servletBase ).getCorsAllowedDomainsSystemProperty();

    this.servletBase.setCorsHeaders( this.mockRequest, this.mockResponse );
  }

}
