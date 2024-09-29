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
 * Copyright (c) 2024 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.web.servlet;

import com.mockrunner.mock.web.MockHttpServletRequest;
import com.mockrunner.mock.web.MockHttpServletResponse;
import com.mockrunner.mock.web.MockServletConfig;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.util.HttpClientManager;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.util.messages.LocaleHelper;

import javax.servlet.ServletException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )
public class ProxyServletTest {

  @Test
  public void testInitMalformedURLException() throws ServletException {
    MockServletConfig config = new MockServletConfig();
    config.setInitParameter( "ProxyURL", "pentaho" );

    ProxyServlet servlet = spy( new ProxyServlet() );
    servlet.init( config );

    assertNull( servlet.getProxyURL() );

    verify( servlet, times( 1 ) ).error( any( String.class ) );
  }

  @Test
  public void testInitParameterProxyURL() throws ServletException {
    MockServletConfig config = new MockServletConfig();
    config.setInitParameter( "ProxyURL", "https://www.pentaho.org" );

    ProxyServlet servlet = spy( new ProxyServlet() );
    servlet.init( config );

    assertEquals( "https://www.pentaho.org", servlet.getProxyURL() );
    assertNull( servlet.getErrorURL() );

    verify( servlet, times( 1 ) ).info( any( String.class ) );
  }

  @Test
  public void testInitParameterErrorURL() throws ServletException {
    MockServletConfig config = new MockServletConfig();
    config.setInitParameter( "ErrorURL", "https://www.pentaho.org" );

    ProxyServlet servlet = spy( new ProxyServlet() );
    servlet.init( config );

    assertNull( servlet.getProxyURL() );
    assertEquals( "https://www.pentaho.org", servlet.getErrorURL() );

    verify( servlet, never() ).info( any( String.class ) );
    verify( servlet, times( 1 ) ).error( any( String.class ) );
  }

  @Test
  public void testInitParameterLocaleOverrideEnabledFalse() throws ServletException {
    MockServletConfig config = new MockServletConfig();
    config.setInitParameter( "ProxyURL", "https://www.pentaho.org" );
    config.setInitParameter( "LocaleOverrideEnabled", "false" );

    ProxyServlet servlet = spy( new ProxyServlet() );
    servlet.init( config );

    assertFalse( servlet.isLocaleOverrideEnabled() );
  }

  @Test
  public void testInitParameterLocaleOverrideEnabledTrue() throws ServletException {
    MockServletConfig config = new MockServletConfig();
    config.setInitParameter( "ProxyURL", "https://www.pentaho.org" );
    config.setInitParameter( "LocaleOverrideEnabled", "true" );

    ProxyServlet servlet = spy( new ProxyServlet() );
    servlet.init( config );

    assertTrue( servlet.isLocaleOverrideEnabled() );
  }

  @Test
  public void testInitParameterLocaleOverrideEnabledDefault() throws ServletException {
    MockServletConfig config = new MockServletConfig();
    config.setInitParameter( "ProxyURL", "https://www.pentaho.org" );

    ProxyServlet servlet = spy( new ProxyServlet() );
    servlet.init( config );

    assertTrue( servlet.isLocaleOverrideEnabled() );
  }

  @Test
  public void testDoProxyEmptyProxyURL() throws MalformedURLException, URISyntaxException {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    ProxyServlet servlet = spy( new ProxyServlet() );

    servlet.doProxy( request, response );

    verify( servlet, never() ).buildProxiedUri( any(), any() );
    verify( servlet, never() ).doProxyCore( any(), any(), any() );
  }

  @Test
  public void testDoProxyEmptySessionWithErrorURL() throws IOException, URISyntaxException {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    ProxyServlet servlet = spy( new ProxyServlet() );

    when( servlet.getProxyURL() ).thenReturn( "https://www.pentaho.org" );
    when( servlet.getErrorURL() ).thenReturn( "https://www.pentaho.org/error" );
    when( servlet.getPentahoSession( request ) ).thenReturn( null );

    servlet.doProxy( request, response );

    verify( servlet, never() ).buildProxiedUri( any(), any() );
    verify( servlet, never() ).doProxyCore( any(), any(), any() );
  }

  @Test
  public void testDoProxyEmptyUsernameWithErrorURL() throws IOException, URISyntaxException {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    IPentahoSession session = mock( IPentahoSession.class );
    when( session.getName() ).thenReturn( null );

    ProxyServlet servlet = spy( new ProxyServlet() );

    when( servlet.getProxyURL() ).thenReturn( "https://www.pentaho.org" );
    when( servlet.getErrorURL() ).thenReturn( "https://www.pentaho.org/error" );
    when( servlet.getPentahoSession( request ) ).thenReturn( session );

    servlet.doProxy( request, response );

    verify( servlet, never() ).buildProxiedUri( any(), any() );
    verify( servlet, never() ).doProxyCore( any(), any(), any() );
  }

  @Test
  public void testDoProxy() throws IOException, URISyntaxException {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    URI mockURI = mock( URI.class );
    final String userName = "admin";

    request.setServletPath( "/pentaho" );

    IPentahoSession session = mock( IPentahoSession.class );
    when( session.getName() ).thenReturn( userName );

    ProxyServlet servlet = spy( new ProxyServlet() );

    when( servlet.getProxyURL() ).thenReturn( "https://www.pentaho.org" );
    when( servlet.getPentahoSession( request ) ).thenReturn( session );

    doReturn( mockURI ).when( servlet ).buildProxiedUri( request, userName );
    doNothing().when( servlet ).doProxyCore( mockURI, request, response );

    servlet.doProxy( request, response );

    verify( servlet, times( 1 ) ).buildProxiedUri( request, userName );
    verify( servlet, times( 1 ) ).doProxyCore( mockURI, request, response );
  }

  @Test
  public void testDoProxyURISyntaxException() throws IOException, URISyntaxException {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    final String userName = "admin";

    request.setServletPath( "/pentaho" );

    IPentahoSession session = mock( IPentahoSession.class );
    when( session.getName() ).thenReturn( userName );

    ProxyServlet servlet = spy( new ProxyServlet() );

    when( servlet.getProxyURL() ).thenReturn( "https://www.pentaho.org" );
    when( servlet.getPentahoSession( request ) ).thenReturn( session );

    doThrow( URISyntaxException.class ).when( servlet ).buildProxiedUri( request, userName );

    servlet.doProxy( request, response );

    verify( servlet, times( 1 ) ).buildProxiedUri( request, userName );
    verify( servlet, never() ).doProxyCore( any(), any(), any() );
    verify( servlet, times( 1 ) ).error( any( String.class ) );
  }

  @Test
  public void testDoProxyMalformedURLException() throws IOException, URISyntaxException {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    final String userName = "admin";

    request.setServletPath( "/pentaho" );

    IPentahoSession session = mock( IPentahoSession.class );
    when( session.getName() ).thenReturn( userName );

    ProxyServlet servlet = spy( new ProxyServlet() );

    when( servlet.getProxyURL() ).thenReturn( "https://www.pentaho.org" );
    when( servlet.getPentahoSession( request ) ).thenReturn( session );

    doThrow( MalformedURLException.class ).when( servlet ).buildProxiedUri( request, userName );

    servlet.doProxy( request, response );

    verify( servlet, times( 1 ) ).buildProxiedUri( request, userName );
    verify( servlet, never() ).doProxyCore( any(), any(), any() );
    verify( servlet, times( 1 ) ).error( any( String.class ), any( IOException.class ) );
  }

  @Test
  public void testBuildProxiedUri() throws IOException, URISyntaxException {
    MockHttpServletRequest request = new MockHttpServletRequest();
    final String userName = "admin";
    Locale locale = new Locale( "en" );

    request.setServletPath( "/pentaho" );
    request.setupAddParameter( "_TRUST_USER_", "suzy" );
    request.setupAddParameter( "foo", "bar" );

    ProxyServlet servlet = spy( new ProxyServlet() );

    when( servlet.getProxyURL() ).thenReturn( "https://www.pentaho.org?_TRUST_LOCALE_OVERRIDE_=en_PT" );
    doNothing().when( servlet ).debug( any( String.class ) );

    try ( MockedStatic<LocaleHelper> manager = Mockito.mockStatic( LocaleHelper.class ) ) {
      manager.when( LocaleHelper::getLocale ).thenReturn( locale );


      assertEquals(
        "https://www.pentaho.org/pentaho?foo=bar&_TRUST_USER_=admin&_TRUST_LOCALE_OVERRIDE_=en",
        servlet.buildProxiedUri( request, userName ).toString() );
    }

    verify( servlet, times( 1 ) ).debug( any( String.class ) );
  }

  @Test
  public void testBuildProxiedUriProxyURLEmptyQuery() throws IOException, URISyntaxException {
    MockHttpServletRequest request = new MockHttpServletRequest();
    final String userName = "admin";
    Locale locale = new Locale( "en" );

    request.setServletPath( "/pentaho" );

    ProxyServlet servlet = spy( new ProxyServlet() );

    when( servlet.getProxyURL() ).thenReturn( "https://www.pentaho.org" );
    doNothing().when( servlet ).debug( any( String.class ) );

    try ( MockedStatic<LocaleHelper> manager = Mockito.mockStatic( LocaleHelper.class ) ) {
      manager.when( LocaleHelper::getLocale ).thenReturn( locale );


      assertEquals(
        "https://www.pentaho.org/pentaho?_TRUST_USER_=admin&_TRUST_LOCALE_OVERRIDE_=en",
        servlet.buildProxiedUri( request, userName ).toString() );
    }

    verify( servlet, times( 1 ) ).debug( any( String.class ) );
  }

  @Test
  public void testDoProxyCore() throws IOException, URISyntaxException {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    final URI uri = new URL( "https://www.pentaho.org/pentaho?_TRUST_USER_=admin&_TRUST_LOCALE_OVERRIDE_=en_PT" )
      .toURI();
    HttpClientManager mockManager = mock( HttpClientManager.class );
    CloseableHttpClient client = mock( CloseableHttpClient.class );
    CloseableHttpResponse proxyResponse = mock( CloseableHttpResponse.class );
    StatusLine statusLine = mock( StatusLine.class );
    HttpEntity entity = mock( HttpEntity.class );
    Header header = mock( Header.class );

    when( statusLine.getStatusCode() ).thenReturn( HttpStatus.SC_OK );
    when( proxyResponse.getStatusLine() ).thenReturn( statusLine );
    when( header.getValue() ).thenReturn( ContentType.TEXT_XML.toString() );
    when( entity.getContentType() ).thenReturn( header );
    when( proxyResponse.getEntity() ).thenReturn( entity );
    when( client.execute( any() ) ).thenReturn( proxyResponse );
    when( mockManager.createDefaultClient() ).thenReturn( client );

    ProxyServlet servlet = spy( new ProxyServlet() );

    try ( MockedStatic<HttpClientManager> manager = Mockito.mockStatic( HttpClientManager.class ) ) {
      manager.when( HttpClientManager::getInstance ).thenReturn( mockManager );

      servlet.doProxyCore( uri, request, response );
    }

    verify( client, times( 1 ) ).execute( any( HttpUriRequest.class ) );

    assertEquals( ContentType.TEXT_XML.toString(), response.getContentType() );
  }

  @Test
  public void testDoProxyCoreInnerErrorResponse() throws IOException, URISyntaxException {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    final URI uri = new URL( "https://www.pentaho.org/pentaho?_TRUST_USER_=admin&_TRUST_LOCALE_OVERRIDE_=en_PT" )
      .toURI();
    HttpClientManager mockManager = mock( HttpClientManager.class );
    CloseableHttpClient client = mock( CloseableHttpClient.class );
    CloseableHttpResponse proxyResponse = mock( CloseableHttpResponse.class );
    StatusLine statusLine = mock( StatusLine.class );

    when( statusLine.getStatusCode() ).thenReturn( HttpStatus.SC_INTERNAL_SERVER_ERROR );
    when( proxyResponse.getStatusLine() ).thenReturn( statusLine );
    when( client.execute( any() ) ).thenReturn( proxyResponse );
    when( mockManager.createDefaultClient() ).thenReturn( client );

    ProxyServlet servlet = spy( new ProxyServlet() );

    try ( MockedStatic<HttpClientManager> manager = Mockito.mockStatic( HttpClientManager.class ) ) {
      manager.when( HttpClientManager::getInstance ).thenReturn( mockManager );

      servlet.doProxyCore( uri, request, response );
    }

    verify( client, times( 1 ) ).execute( any( HttpUriRequest.class ) );
    verify( servlet, times( 1 ) ).error( any( String.class ) );
  }

  @Test
  public void testDoProxyCoreIOException() throws IOException, URISyntaxException {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    final URI uri = new URL( "https://www.pentaho.org/pentaho?_TRUST_USER_=admin&_TRUST_LOCALE_OVERRIDE_=en_PT" )
      .toURI();
    HttpClientManager mockManager = mock( HttpClientManager.class );
    CloseableHttpClient client = mock( CloseableHttpClient.class );

    when( client.execute( any() ) ).thenThrow( IOException.class );
    when( mockManager.createDefaultClient() ).thenReturn( client );

    ProxyServlet servlet = spy( new ProxyServlet() );

    try ( MockedStatic<HttpClientManager> manager = Mockito.mockStatic( HttpClientManager.class ) ) {
      manager.when( HttpClientManager::getInstance ).thenReturn( mockManager );

      servlet.doProxyCore( uri, request, response );
    }

    verify( client, times( 1 ) ).execute( any( HttpUriRequest.class ) );
    verify( servlet, times( 1 ) ).error( any( String.class ), any( IOException.class ) );
  }

  @Test
  public void testDoProxyCoreUnsupportedOperationException() throws IOException, URISyntaxException {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    final URI uri = new URL( "https://www.pentaho.org/pentaho?_TRUST_USER_=admin&_TRUST_LOCALE_OVERRIDE_=en_PT" )
      .toURI();
    HttpClientManager mockManager = mock( HttpClientManager.class );
    CloseableHttpClient client = mock( CloseableHttpClient.class );
    CloseableHttpResponse proxyResponse = mock( CloseableHttpResponse.class );
    StatusLine statusLine = mock( StatusLine.class );
    HttpEntity entity = mock( HttpEntity.class );
    Header header = mock( Header.class );

    when( statusLine.getStatusCode() ).thenReturn( HttpStatus.SC_OK );
    when( proxyResponse.getStatusLine() ).thenReturn( statusLine );
    when( header.getValue() ).thenReturn( ContentType.TEXT_XML.toString() );
    when( entity.getContentType() ).thenReturn( header );
    when( entity.getContent() ).thenThrow( UnsupportedOperationException.class );
    when( proxyResponse.getEntity() ).thenReturn( entity );
    when( client.execute( any() ) ).thenReturn( proxyResponse );
    when( mockManager.createDefaultClient() ).thenReturn( client );

    ProxyServlet servlet = spy( new ProxyServlet() );

    try ( MockedStatic<HttpClientManager> manager = Mockito.mockStatic( HttpClientManager.class ) ) {
      manager.when( HttpClientManager::getInstance ).thenReturn( mockManager );

      servlet.doProxyCore( uri, request, response );
    }

    verify( client, times( 1 ) ).execute( any( HttpUriRequest.class ) );
    verify( servlet, times( 1 ) )
      .error( any( String.class ), any( UnsupportedOperationException.class ) );
  }

  @Test
  public void testDoProxyCorePOSTWithContent() throws IOException, URISyntaxException {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    final String content = "some content to get copied";
    final URI uri = new URL( "https://www.pentaho.org/pentaho?_TRUST_USER_=admin&_TRUST_LOCALE_OVERRIDE_=en_PT" )
      .toURI();
    HttpClientManager mockManager = mock( HttpClientManager.class );
    CloseableHttpClient client = mock( CloseableHttpClient.class );
    CloseableHttpResponse proxyResponse = mock( CloseableHttpResponse.class );
    StatusLine statusLine = mock( StatusLine.class );
    HttpEntity entity = mock( HttpEntity.class );
    Header header = mock( Header.class );

    request.setMethod( HttpPost.METHOD_NAME );
    request.setBodyContent( content );
    request.setContentType( ContentType.TEXT_XML.toString() );
    when( statusLine.getStatusCode() ).thenReturn( HttpStatus.SC_OK );
    when( proxyResponse.getStatusLine() ).thenReturn( statusLine );
    when( header.getValue() ).thenReturn( ContentType.TEXT_XML.toString() );
    when( entity.getContentType() ).thenReturn( header );
    when( proxyResponse.getEntity() ).thenReturn( entity );
    when( client.execute( any() ) ).thenReturn( proxyResponse );
    when( mockManager.createDefaultClient() ).thenReturn( client );

    request.setServletPath( "/pentaho" );

    ProxyServlet servlet = spy( new ProxyServlet() );

    doNothing().when( servlet ).copyContent( any(), any() );

    try ( MockedStatic<HttpClientManager> manager = Mockito.mockStatic( HttpClientManager.class ) ) {
      manager.when( HttpClientManager::getInstance ).thenReturn( mockManager );

      servlet.doProxyCore( uri, request, response );
    }

    verify( client, times( 1 ) ).execute( any( HttpUriRequest.class ) );
    verify( servlet, times( 2 ) ).copyContent( any(), any() );

    assertEquals( ContentType.TEXT_XML.toString(), response.getContentType() );
  }

  @Test
  public void testCopyContent() throws IOException {
    final String content = "some content to get copied";
    final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream( content.getBytes() );
    final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

    ProxyServlet servlet = spy( new ProxyServlet() );

    servlet.copyContent( byteArrayInputStream, byteArrayOutputStream );

    assertEquals( content, byteArrayOutputStream.toString() );
  }

  @Test
  public void testCopyContentNullInputStream() throws IOException {
    final ByteArrayOutputStream byteArrayOutputStream = mock( ByteArrayOutputStream.class );

    ProxyServlet servlet = spy( new ProxyServlet() );

    servlet.copyContent( null, byteArrayOutputStream );

    verify( byteArrayOutputStream, never() ).write( any( byte[].class ), anyInt(), anyInt() );
  }

  @Test
  public void testCopyContentNullOutputStream() throws IOException {
    final ByteArrayInputStream byteArrayInputStream = mock( ByteArrayInputStream.class );

    ProxyServlet servlet = spy( new ProxyServlet() );

    servlet.copyContent( byteArrayInputStream, null );

    verify( byteArrayInputStream, never() ).read( any( byte[].class ) );
  }

  @Test
  public void testDoPost() throws ServletException, IOException {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    ProxyServlet servlet = spy( new ProxyServlet() );

    servlet.doPost( request, response );

    verify( servlet, times( 1 ) ).doProxy( request, response );
  }

  @Test
  public void testDoGet() throws ServletException, IOException {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    ProxyServlet servlet = spy( new ProxyServlet() );

    servlet.doGet( request, response );

    verify( servlet, times( 1 ) ).doProxy( request, response );
  }
}
