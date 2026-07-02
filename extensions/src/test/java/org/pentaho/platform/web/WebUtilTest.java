/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.platform.web;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )
public class WebUtilTest {

  @Mock
  private HttpServletRequest mockRequest;
  @Mock
  private HttpServletResponse mockResponse;

  // region WebUtil.setCorsResponseHeaders( request, response )
  @Test
  public void testSetCorsHeadersNormalRequest() {
    try ( MockedStatic<WebUtil> webUtils = Mockito.mockStatic( WebUtil.class ) ) {
      setupCorsTest( webUtils, null, null );

      WebUtil.setCorsResponseHeaders( this.mockRequest, this.mockResponse );

      verify( this.mockResponse, never() ).setHeader( eq( WebUtil.CORS_ALLOW_ORIGIN_HEADER ), nullable( String.class ) );
      verify( this.mockResponse, never() ).setHeader( eq( WebUtil.CORS_ALLOW_CREDENTIALS_HEADER ), nullable( String.class ) );
    }
  }

  @Test
  public void testSetCorsHeadersRequestsNotAllowed() {
    try ( MockedStatic<WebUtil> webUtils = Mockito.mockStatic( WebUtil.class ) ) {
      setupCorsTest( webUtils, "false", null );

      WebUtil.setCorsResponseHeaders( this.mockRequest, this.mockResponse );

      verify( this.mockResponse, never() ).setHeader( eq( WebUtil.CORS_ALLOW_ORIGIN_HEADER ), nullable( String.class ) );
      verify( this.mockResponse, never() ).setHeader( eq( WebUtil.CORS_ALLOW_CREDENTIALS_HEADER ), nullable( String.class ) );
    }
  }

  @Test
  public void testSetCorsHeadersDomainNotAllowed() {
    when( this.mockRequest.getHeader( WebUtil.ORIGIN_HEADER ) ).thenReturn( "foobar.com" );

    try ( MockedStatic<WebUtil> webUtils = Mockito.mockStatic( WebUtil.class ) ) {
      setupCorsTest( webUtils, "true", null );

      WebUtil.setCorsResponseHeaders( this.mockRequest, this.mockResponse );

      verify( this.mockResponse, never() ).setHeader( eq( WebUtil.CORS_ALLOW_ORIGIN_HEADER ), nullable( String.class ) );
      verify( this.mockResponse, never() ).setHeader( eq( WebUtil.CORS_ALLOW_CREDENTIALS_HEADER ), nullable( String.class ) );
    }
  }

  @Test
  public void testSetCorsHeadersRequestAndDomainAllowed() {
    String domain = "foobar.com";
    when( this.mockRequest.getHeader( WebUtil.ORIGIN_HEADER ) ).thenReturn( domain );

    try ( MockedStatic<WebUtil> webUtils = Mockito.mockStatic( WebUtil.class ) ) {
      setupCorsTest( webUtils, "true", domain );

      WebUtil.setCorsResponseHeaders( this.mockRequest, this.mockResponse );

      verify( this.mockResponse ).setHeader( eq( WebUtil.CORS_ALLOW_ORIGIN_HEADER ), eq( domain ) );
      verify( this.mockResponse ).setHeader( eq( WebUtil.CORS_ALLOW_CREDENTIALS_HEADER ), eq( "true" ) );
    }
  }

  @Test
  public void testSetCorsHeadersRequestAndDomainAllowedMultiple() {

    String domain = "localhost:1337";
    String allowedDomains = "foobar.com, " + domain;
    when( this.mockRequest.getHeader( WebUtil.ORIGIN_HEADER ) ).thenReturn( domain );

    try ( MockedStatic<WebUtil> webUtils = Mockito.mockStatic( WebUtil.class ) ) {
      setupCorsTest( webUtils, "true", allowedDomains );

      WebUtil.setCorsResponseHeaders( this.mockRequest, this.mockResponse );

      verify( this.mockResponse ).setHeader( eq( WebUtil.CORS_ALLOW_ORIGIN_HEADER ), eq( domain ) );
      verify( this.mockResponse ).setHeader( eq( WebUtil.CORS_ALLOW_CREDENTIALS_HEADER ), eq( "true" ) );
    }
  }

  @Test
  public void testSetCorsHeadersRequestAndDomainAllowedWithConfiguration() {
    String domain = "foobar.com";
    when( this.mockRequest.getHeader( WebUtil.ORIGIN_HEADER ) ).thenReturn( domain );

    Map<String, List<String>> corsHeadersConfiguration = new HashMap<>( 1 );

    final String extraHeaderName = "x-foo-bar";
    final String extraHeaderValue = "foo-value";
    corsHeadersConfiguration.put( extraHeaderName, Collections.singletonList( extraHeaderValue ) );

    try ( MockedStatic<WebUtil> webUtils = Mockito.mockStatic( WebUtil.class ) ) {
      setupCorsTest( webUtils, "true", domain );
      WebUtil.setCorsResponseHeaders( this.mockRequest, this.mockResponse, corsHeadersConfiguration );

      verify( this.mockResponse ).setHeader( eq( WebUtil.CORS_ALLOW_ORIGIN_HEADER ), eq( domain ) );
      verify( this.mockResponse ).setHeader( eq( WebUtil.CORS_ALLOW_CREDENTIALS_HEADER ), eq( "true" ) );
      verify( this.mockResponse ).setHeader( eq( extraHeaderName ), eq( extraHeaderValue ) );
    }
  }

  private static void setupCorsTest( MockedStatic<WebUtil> webUtilStatic, String corsAllowed, String corsAllowedDomains ) {
    spyStaticMethods( webUtilStatic );
    webUtilStatic.when( WebUtil::getCorsRequestsAllowedSystemProperty ).thenReturn( corsAllowed );
    webUtilStatic.when( WebUtil::getCorsAllowedOriginsSystemProperty ).thenReturn( corsAllowedDomains );
  }

  private static void spyStaticMethods( MockedStatic<WebUtil> webUtilStatic ) {
    webUtilStatic.when( WebUtil::isCorsRequestsAllowed ).thenCallRealMethod();
    webUtilStatic.when( () -> WebUtil.isCorsRequestOriginAllowed( nullable( String.class ) ) ).thenCallRealMethod();
    webUtilStatic.when( WebUtil::getCorsRequestsAllowedOrigins ).thenCallRealMethod();
    webUtilStatic.when( () -> WebUtil.setCorsResponseHeaders( any(), any() ) ).thenCallRealMethod();
    webUtilStatic.when( () -> WebUtil.setCorsResponseHeaders( any(), any(), any() ) ).thenCallRealMethod();
  }
  // endregion
}
