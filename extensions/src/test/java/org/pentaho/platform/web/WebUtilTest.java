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

package org.pentaho.platform.web;

import org.dom4j.Element;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.ArgumentCaptor;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
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

  // region WebUtil.setCorsResponseHeaders( request, response )
  @Test
  public void testSetCorsHeadersNormalRequest() {

    this.setupCorsTest();
    this.doCorsTest();

    verify( this.mockResponse, never() ).setHeader( eq( WebUtil.CORS_ALLOW_ORIGIN_HEADER ), anyString() );
    verify( this.mockResponse, never() ).setHeader( eq( WebUtil.CORS_ALLOW_CREDENTIALS_HEADER ), anyString() );
  }

  @Test
  public void testSetCorsHeadersRequestsNotAllowed() {

    this.setupCorsTest();

    when( this.mockRequest.getHeader( WebUtil.ORIGIN_HEADER ) ).thenReturn( "foobar.com" );

    this.doCorsTest( "false", null );

    verify( this.mockResponse, never() ).setHeader( eq( WebUtil.CORS_ALLOW_ORIGIN_HEADER ), anyString() );
    verify( this.mockResponse, never() ).setHeader( eq( WebUtil.CORS_ALLOW_CREDENTIALS_HEADER ), anyString() );
  }

  @Test
  public void testSetCorsHeadersDomainNotAllowed() {

    this.setupCorsTest();

    when( this.mockRequest.getHeader( WebUtil.ORIGIN_HEADER ) ).thenReturn( "foobar.com" );

    this.doCorsTest( "true", null );

    verify( this.mockResponse, never() ).setHeader( eq( WebUtil.CORS_ALLOW_ORIGIN_HEADER ), anyString() );
    verify( this.mockResponse, never() ).setHeader( eq( WebUtil.CORS_ALLOW_CREDENTIALS_HEADER ), anyString() );
  }

  @Test
  public void testSetCorsHeadersRequestAndDomainAllowed() {

    this.setupCorsTest();

    String domain = "foobar.com";
    when( this.mockRequest.getHeader( WebUtil.ORIGIN_HEADER ) ).thenReturn( domain );

    this.doCorsTest( "true", domain );

    verify( this.mockResponse ).setHeader( eq( WebUtil.CORS_ALLOW_ORIGIN_HEADER ), eq( domain ) );
    verify( this.mockResponse ).setHeader( eq( WebUtil.CORS_ALLOW_CREDENTIALS_HEADER ), eq( "true" ) );
  }

  @Test
  public void testSetCorsHeadersRequestAndDomainAllowedMultiple() {

    this.setupCorsTest();

    String domain = "localhost:1337";
    String allowedDomains = "foobar.com, " + domain;
    when( this.mockRequest.getHeader( WebUtil.ORIGIN_HEADER ) ).thenReturn( domain );

    this.doCorsTest( "true", allowedDomains );

    verify( this.mockResponse ).setHeader( eq( WebUtil.CORS_ALLOW_ORIGIN_HEADER ), eq( domain ) );
    verify( this.mockResponse ).setHeader( eq( WebUtil.CORS_ALLOW_CREDENTIALS_HEADER ), eq( "true" ) );
  }

  @Test
  public void testSetCorsHeadersRequestAndDomainAllowedWithConfiguration() {

    this.setupCorsTest();

    String domain = "foobar.com";
    when( this.mockRequest.getHeader( WebUtil.ORIGIN_HEADER ) ).thenReturn( domain );

    Map<String, List<String>> corsHeadersConfiguration = new HashMap<>( 1 );

    final String extraHeaderName = "x-foo-bar";
    final String extraHeaderValue = "foo-value";
    corsHeadersConfiguration.put( extraHeaderName, Collections.singletonList( extraHeaderValue ) );

    this.doCorsTest( "true", domain, corsHeadersConfiguration );

    verify( this.mockResponse ).setHeader( eq( WebUtil.CORS_ALLOW_ORIGIN_HEADER ), eq( domain ) );
    verify( this.mockResponse ).setHeader( eq( WebUtil.CORS_ALLOW_CREDENTIALS_HEADER ), eq( "true" ) );

    verify( this.mockResponse ).setHeader( eq( extraHeaderName ), eq( extraHeaderValue ) );
  }

  private void setupCorsTest() {
    this.mockRequest = mock( HttpServletRequest.class );
    this.mockResponse = mock( HttpServletResponse.class );
  }

  private void doCorsTest() {
    this.doCorsTest( null, null, null );
  }

  private void doCorsTest( String corsAllowed, String corsAllowedDomains ) {
    this.doCorsTest( corsAllowed, corsAllowedDomains, null );
  }

  private void doCorsTest( String corsAllowed, String corsAllowedDomains,
                           Map<String, List<String>> corsHeadersConfiguration ) {

    spy( WebUtil.class );

    when( WebUtil.getCorsRequestsAllowedSystemProperty() ).thenReturn( corsAllowed );
    when( WebUtil.getCorsAllowedDomainsSystemProperty() ).thenReturn( corsAllowedDomains );

    if ( corsHeadersConfiguration == null ) {
      WebUtil.setCorsResponseHeaders( this.mockRequest, this.mockResponse );
    } else {
      WebUtil.setCorsResponseHeaders( this.mockRequest, this.mockResponse, corsHeadersConfiguration );
    }
  }
  // endregion
}
