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
import org.pentaho.platform.api.engine.CsrfProtectionDefinition;
import org.pentaho.platform.api.engine.RequestMatcherDefinition;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.security.web.util.matcher.RequestMatcher;

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

  // region WebUtil.parseXmlCsrfProtectionDefinition ( csrfProtectionDefinitionElem )
  @Test
  public void testParseXmlCsrfNoChildRequestMatchers() {

    Element csrfProtectionDefinitionElem = mock( Element.class );
    when( csrfProtectionDefinitionElem.selectNodes( eq( "request-matcher" ) ) )
        .thenReturn( Collections.emptyList() );

    CsrfProtectionDefinition result = WebUtil.parseXmlCsrfProtectionDefinition( csrfProtectionDefinitionElem );

    assertNull( result );
  }

  @Test
  public void testParseXmlCsrfOneChildRequestMatchers() {

    Element csrfRequestMatcherElem = mock( Element.class );
    when( csrfRequestMatcherElem.attributeValue( eq( "type" ), anyString() ) ).thenReturn( "regex" );
    when( csrfRequestMatcherElem.attributeValue( eq( "pattern" ), anyString() ) ).thenReturn( "abc" );
    when( csrfRequestMatcherElem.attributeValue( eq( "methods" ), anyString() ) ).thenReturn( "GET,POST" );

    Element csrfProtectionDefinitionElem = mock( Element.class );
    when( csrfProtectionDefinitionElem.selectNodes( eq( "request-matcher" ) ) )
        .thenReturn( Collections.singletonList( csrfRequestMatcherElem ) );

    CsrfProtectionDefinition result = WebUtil.parseXmlCsrfProtectionDefinition( csrfProtectionDefinitionElem );

    assertNotNull( result );

    RequestMatcherDefinition[] requestMatchers = result.getProtectedRequestMatchers()
        .toArray( new RequestMatcherDefinition[0] );
    assertNotNull( requestMatchers );
    assertEquals( 1, requestMatchers.length );

    assertEquals( "regex", requestMatchers[0].getType() );
    assertEquals( "abc", requestMatchers[0].getPattern() );
    assertEquals( 2, requestMatchers[0].getMethods().size() );
    Assert.assertArrayEquals( new String[] { "GET", "POST" }, requestMatchers[0].getMethods().toArray() );
  }

  @Test
  public void testParseXmlCsrfTwoChildRequestMatchers() {

    Element csrfRequestMatcher1Elem = mock( Element.class );
    when( csrfRequestMatcher1Elem.attributeValue( eq( "type" ), anyString() ) ).thenReturn( "regex" );
    when( csrfRequestMatcher1Elem.attributeValue( eq( "pattern" ), anyString() ) ).thenReturn( "abc" );
    when( csrfRequestMatcher1Elem.attributeValue( eq( "methods" ), anyString() ) ).thenReturn( "GET,POST" );

    Element csrfRequestMatcher2Elem = mock( Element.class );
    when( csrfRequestMatcher2Elem.attributeValue( eq( "type" ), anyString() ) ).thenReturn( "regex" );
    when( csrfRequestMatcher2Elem.attributeValue( eq( "pattern" ), anyString() ) ).thenReturn( "abc2" );
    when( csrfRequestMatcher2Elem.attributeValue( eq( "methods" ), anyString() ) ).thenReturn( "POST" );

    Element csrfProtectionDefinitionElem = mock( Element.class );
    when( csrfProtectionDefinitionElem.selectNodes( eq( "request-matcher" ) ) )
        .thenReturn( Arrays.asList( csrfRequestMatcher1Elem, csrfRequestMatcher2Elem ) );

    CsrfProtectionDefinition result = WebUtil.parseXmlCsrfProtectionDefinition( csrfProtectionDefinitionElem );

    assertNotNull( result );

    RequestMatcherDefinition[] requestMatchers = result.getProtectedRequestMatchers()
        .toArray( new RequestMatcherDefinition[ 0 ] );

    assertNotNull( requestMatchers );
    assertEquals( 2, requestMatchers.length );

    assertEquals( "regex", requestMatchers[0].getType() );
    assertEquals( "abc", requestMatchers[0].getPattern() );
    assertEquals( 2, requestMatchers[0].getMethods().size() );
    Assert.assertArrayEquals( new String[] { "GET", "POST" }, requestMatchers[0].getMethods().toArray() );

    assertEquals( "regex", requestMatchers[1].getType() );
    assertEquals( "abc2", requestMatchers[1].getPattern() );
    assertEquals( 1, requestMatchers[1].getMethods().size() );
    Assert.assertArrayEquals( new String[] { "POST" }, requestMatchers[1].getMethods().toArray() );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testParseXmlCsrfInvalidType() {

    Element csrfRequestMatcherElem = mock( Element.class );
    when( csrfRequestMatcherElem.attributeValue( eq( "type" ), anyString() ) ).thenReturn( "foo" );
    when( csrfRequestMatcherElem.attributeValue( eq( "pattern" ), anyString() ) ).thenReturn( "abc" );
    when( csrfRequestMatcherElem.attributeValue( eq( "methods" ), anyString() ) ).thenReturn( "GET,POST" );

    Element csrfProtectionDefinitionElem = mock( Element.class );
    when( csrfProtectionDefinitionElem.selectNodes( eq( "request-matcher" ) ) )
        .thenReturn( Collections.singletonList( csrfRequestMatcherElem ) );

    WebUtil.parseXmlCsrfProtectionDefinition( csrfProtectionDefinitionElem );
  }

  @Test
  public void testParseXmlCsrfEmptyValues() {

    ArgumentCaptor<String> defaultTypeCapture = ArgumentCaptor.forClass( String.class );
    ArgumentCaptor<String> defaultMethodsCapture = ArgumentCaptor.forClass( String.class );

    Element csrfRequestMatcherElem = mock( Element.class );
    when( csrfRequestMatcherElem.attributeValue( eq( "type" ), defaultTypeCapture.capture() ) )
        .thenReturn( "regex" );
    when( csrfRequestMatcherElem.attributeValue( eq( "pattern" ), anyString() ) )
        .thenReturn( "abc" );
    when( csrfRequestMatcherElem.attributeValue( eq( "methods" ), defaultMethodsCapture.capture() ) )
        .thenReturn( "GET,POST" );

    Element csrfProtectionDefinitionElem = mock( Element.class );
    when( csrfProtectionDefinitionElem.selectNodes( eq( "request-matcher" ) ) )
        .thenReturn( Collections.singletonList( csrfRequestMatcherElem ) );

    WebUtil.parseXmlCsrfProtectionDefinition( csrfProtectionDefinitionElem );

    assertEquals( "regex", defaultTypeCapture.getValue() );
    assertEquals( "GET,POST", defaultMethodsCapture.getValue() );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testParseXmlCsrfEmptyPattern() {

    Element csrfRequestMatcherElem = mock( Element.class );
    when( csrfRequestMatcherElem.attributeValue( eq( "type" ), anyString() ) ).thenReturn( "regex" );
    when( csrfRequestMatcherElem.attributeValue( eq( "pattern" ), anyString() ) ).thenReturn( "" );
    when( csrfRequestMatcherElem.attributeValue( eq( "methods" ), anyString() ) ).thenReturn( "GET,POST" );

    Element csrfProtectionDefinitionElem = mock( Element.class );
    when( csrfProtectionDefinitionElem.selectNodes( eq( "request-matcher" ) ) )
        .thenReturn( Collections.singletonList( csrfRequestMatcherElem ) );

    WebUtil.parseXmlCsrfProtectionDefinition( csrfProtectionDefinitionElem );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testParseXmlCsrfInvalidMethods() {

    Element csrfRequestMatcherElem = mock( Element.class );
    when( csrfRequestMatcherElem.attributeValue( eq( "type" ), anyString() ) ).thenReturn( "regex" );
    when( csrfRequestMatcherElem.attributeValue( eq( "pattern" ), anyString() ) ).thenReturn( "abc" );
    when( csrfRequestMatcherElem.attributeValue( eq( "methods" ), anyString() ) ).thenReturn( "GET,FOO" );

    Element csrfProtectionDefinitionElem = mock( Element.class );
    when( csrfProtectionDefinitionElem.selectNodes( eq( "request-matcher" ) ) )
        .thenReturn( Collections.singletonList( csrfRequestMatcherElem ) );

    WebUtil.parseXmlCsrfProtectionDefinition( csrfProtectionDefinitionElem );
  }
  // endregion

  // region WebUtil.buildCsrfRequestMatcher( List csrfProtectionDefinitions )
  @Test
  public void testBuildCsrfRequestMatcherEmptyList() {
    CsrfProtectionDefinition protectionDefinition = new CsrfProtectionDefinition();
    Collection<CsrfProtectionDefinition> protectionDefinitions = Collections.emptyList();

    RequestMatcher result = WebUtil.buildCsrfRequestMatcher( protectionDefinitions );

    assertNull( result );
  }

  @Test
  public void testBuildCsrfRequestMatcherOneDefinitionWithNullRequestMatchers() {
    CsrfProtectionDefinition protectionDefinition = new CsrfProtectionDefinition();
    Collection<CsrfProtectionDefinition> protectionDefinitions = Collections.singletonList( protectionDefinition );

    RequestMatcher result = WebUtil.buildCsrfRequestMatcher( protectionDefinitions );

    assertNull( result );
  }

  @Test
  public void testBuildCsrfRequestMatcherOneDefinitionWithEmptyRequestMatchers() {
    CsrfProtectionDefinition protectionDefinition = new CsrfProtectionDefinition();
    protectionDefinition.setProtectedRequestMatchers( Collections.emptyList() );

    Collection<CsrfProtectionDefinition> protectionDefinitions = Collections.singletonList( protectionDefinition );


    RequestMatcher result = WebUtil.buildCsrfRequestMatcher( protectionDefinitions );

    assertNull( result );
  }

  @Test
  public void testBuildCsrfRequestMatcherOneDefinitionWithOneRequestMatcher() {
    doTestBuildCsrfRequestMatcher( "^/abc", "/abc" );
    doTestBuildCsrfRequestMatcher( "^/abc\\b.*", "/abc/def" );
  }

  private void doTestBuildCsrfRequestMatcher( String pattern, String url ) {
    RequestMatcherDefinition requestMatcherDefinition = new RequestMatcherDefinition( "regex", pattern );
    CsrfProtectionDefinition protectionDefinition = new CsrfProtectionDefinition();
    protectionDefinition.setProtectedRequestMatchers( Collections.singletonList( requestMatcherDefinition ) );
    Collection<CsrfProtectionDefinition> protectionDefinitions = Collections.singletonList( protectionDefinition );

    // POST pentaho/abc
    HttpServletRequest mockRequest = mock( HttpServletRequest.class );
    when( mockRequest.getServletPath() ).thenReturn( "" );
    when( mockRequest.getMethod() ).thenReturn( "POST" );
    when( mockRequest.getPathInfo() ).thenReturn( url );

    RequestMatcher requestMatcher = WebUtil.buildCsrfRequestMatcher( protectionDefinitions );
    assertNotNull( requestMatcher );
    assertTrue( requestMatcher.matches( mockRequest ) );
  }
  // endregion
}
