/*
 * ******************************************************************************
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

import org.dom4j.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Created by rfellows on 11/5/15.
 */
@RunWith( MockitoJUnitRunner.class )
public class HttpWebServiceTest {

  HttpWebService httpWebService;

  @Mock HttpServletRequest request;
  @Mock HttpServletResponse response;
  @Mock ServletOutputStream responseOutputStream;
  @Mock IUserRoleListService userRoleListService;

  String payload;
  BufferedReader reader;

  @Before
  public void setUp() throws Exception {
    httpWebService = new HttpWebService();

    payload = "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">"
      + "<SOAP-ENV:Body><document><name>hello world</name></document></SOAP-ENV:Body></SOAP-ENV:Envelope>";
    reader = new BufferedReader( new StringReader( payload ) );
  }

  @After
  public void tearDown() throws Exception {
    PentahoSystem.clearObjectFactory();
  }

  @Test
  public void testGetPayloadAsString() throws Exception {
    when( request.getReader() ).thenReturn( reader );

    assertEquals( payload, httpWebService.getPayloadAsString( request ) );
  }

  @Test
  public void testGetPayloadAsString_nullReader() throws Exception {
    when( request.getReader() ).thenReturn( null );

    assertNull( httpWebService.getPayloadAsString( request ) );
  }

  @Test
  public void testGetParameterMapFromPayload() throws Exception {
    Map parameterMapFromPayload = httpWebService.getParameterMapFromPayload( payload );

    assertNotNull( parameterMapFromPayload );
    assertEquals( 1, parameterMapFromPayload.size() );
    assertEquals( "hello world", parameterMapFromPayload.get( "name" ) );
  }

  @Test
  public void testGetParameterMapFromPayload_invalidXml() throws Exception {
    Map parameterMapFromPayload = httpWebService.getParameterMapFromPayload( "<SOAP-ENV:Envelope "
      + "xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"><SOAP-ENV:Envelope>" );

    assertNotNull( parameterMapFromPayload );
    assertEquals( 0, parameterMapFromPayload.size() );
  }

  @Test
  public void testDoGetFixMe_dial() throws Exception {
    when( request.getReader() ).thenReturn( reader );
    when( request.getParameter( "path" ) ).thenReturn( "solutionName/actionName" );
    when( request.getParameter( "component" ) ).thenReturn( "dial" );

    HttpWebService service = spy( httpWebService );
    doNothing().when( service ).doDial(
      eq( "solutionName" ),
      eq( "solutionName/actionName" ),
      eq( "/actionName" ),
      any( IParameterProvider.class ),
      any( OutputStream.class ),
      any( IPentahoSession.class ) );

    service.doGetFixMe( request, response );
    verify( service ).doDial(
      eq( "solutionName" ),
      eq( "solutionName/actionName" ),
      eq( "/actionName" ),
      any( IParameterProvider.class ),
      any( OutputStream.class ),
      any( IPentahoSession.class ) );
  }

  @Test
  public void testDoGetFixMe_actionFromPayloadXml_chart() throws Exception {
    payload = "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"><SOAP-ENV:Body><document><name>hello world</name><action>/home/folder/chart.xyz</action><component>chart</component></document></SOAP-ENV:Body></SOAP-ENV:Envelope>";
    reader = new BufferedReader( new StringReader( payload ) );

    when( request.getReader() ).thenReturn( reader );
    when( request.getParameter( "path" ) ).thenReturn( "solutionName/actionName" );
    when( request.getParameter( "component" ) ).thenReturn( "" );

    HttpWebService service = spy( httpWebService );
    doNothing().when( service ).doChart(
      eq( "folder" ),
      any( IParameterProvider.class ),
      any( OutputStream.class ),
      any( IPentahoSession.class ) );

    service.doGetFixMe( request, response );
    verify( service ).doChart(
      eq( "folder" ),
      any( IParameterProvider.class ),
      any( OutputStream.class ),
      any( IPentahoSession.class ) );
  }

  @Test
  public void testDoGetFixMe_nullComponent() throws Exception {
    when( request.getReader() ).thenReturn( reader );
    when( request.getParameter( "path" ) ).thenReturn( "solutionName/actionName" );
    when( request.getParameter( "component" ) ).thenReturn( null );

    HttpWebService service = spy( httpWebService );

    service.doGetFixMe( request, response );
  }

  @Test
  public void testDoPost() throws Exception {
    HttpWebService service = spy( httpWebService );
    doNothing().when( service ).doGet( request, response );

    service.doPost( request, response );
    // this should just call through to the doGet method
    verify( service ).doGet( request, response );
  }

  @Test
  public void testDoGet_callThroughToDoGetFixMe() throws Exception {
    HttpWebService service = spy( httpWebService );
    doNothing().when( service ).doGetFixMe( request, response );
    service.doGet( request, response );
    verify( service ).doGetFixMe( request, response );
  }

  @Test
  public void testDoGet_users() throws Exception {
    HttpWebService service = spy( httpWebService );

    payload = "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"><SOAP-ENV:Body><document><details>users</details></document></SOAP-ENV:Body></SOAP-ENV:Envelope>";
    reader = new BufferedReader( new StringReader( payload ) );

    when( request.getReader() ).thenReturn( reader );
    doNothing().when( service ).doGetFixMe( request, response );
    doReturn( true ).when( service ).isSecurityDetailsRequest( request );
    when( response.getOutputStream() ).thenReturn( responseOutputStream );

    service.doGet( request, response );
    verify( service, never() ).doGetFixMe( request, response );
    verify( service, never() ).getRoles();
    verify( service ).getUsers();
    verify( service ).getACLs();
  }

  @Test
  public void testDoGet_roles() throws Exception {
    HttpWebService service = spy( httpWebService );

    payload = "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"><SOAP-ENV:Body><document><details>roles</details></document></SOAP-ENV:Body></SOAP-ENV:Envelope>";
    reader = new BufferedReader( new StringReader( payload ) );

    when( request.getReader() ).thenReturn( reader );
    doNothing().when( service ).doGetFixMe( request, response );
    doReturn( true ).when( service ).isSecurityDetailsRequest( request );
    when( response.getOutputStream() ).thenReturn( responseOutputStream );

    service.doGet( request, response );
    verify( service, never() ).doGetFixMe( request, response );
    verify( service, never() ).getUsers();
    verify( service ).getRoles();
    verify( service ).getACLs();
  }

  @Test
  public void testDoGet_acls() throws Exception {
    HttpWebService service = spy( httpWebService );

    payload = "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"><SOAP-ENV:Body><document><details>acls</details></document></SOAP-ENV:Body></SOAP-ENV:Envelope>";
    reader = new BufferedReader( new StringReader( payload ) );

    when( request.getReader() ).thenReturn( reader );
    doNothing().when( service ).doGetFixMe( request, response );
    doReturn( true ).when( service ).isSecurityDetailsRequest( request );
    when( response.getOutputStream() ).thenReturn( responseOutputStream );

    service.doGet( request, response );
    verify( service, never() ).doGetFixMe( request, response );
    verify( service, never() ).getUsers();
    verify( service, never() ).getRoles();
    verify( service ).getACLs();
  }

  @Test
  public void testDoGet() throws Exception {
    HttpWebService service = spy( httpWebService );
    doNothing().when( service ).doGetFixMe( request, response );
    doReturn( true ).when( service ).isSecurityDetailsRequest( request );
    when( response.getOutputStream() ).thenReturn( responseOutputStream );

    service.doGet( request, response );
    verify( service, never() ).doGetFixMe( request, response );
  }

  @Test
  public void testGetUsers() throws Exception {
    PentahoSystem.registerObject( userRoleListService );
    List<String> users = Arrays.asList( new String[] { "admin", "suzy" } );
    when( userRoleListService.getAllUsers() ).thenReturn( users );

    Document doc = httpWebService.getUsers();
    assertNotNull( doc );
    assertEquals( users.size(), doc.getRootElement().elements().size() );
  }

  @Test
  public void testGetRoles() throws Exception {
    PentahoSystem.registerObject( userRoleListService );
    List<String> roles = Arrays.asList( new String[] { "administrator", "power user" } );
    when( userRoleListService.getAllRoles() ).thenReturn( roles );

    Document doc = httpWebService.getRoles();
    assertNotNull( doc );
    assertEquals( roles.size(), doc.getRootElement().elements().size() );
  }

  @Test
  public void testHasActionInBody_errorReadingPayload() throws Exception {
    HttpWebService service = spy( httpWebService );
    doThrow( new IOException() ).when( service ).getPayloadAsString( request );

    assertFalse( service.hasActionInBody( request ) );
  }

  @Test
  public void testHasActionInBody() throws Exception {
    payload = "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"><SOAP-ENV:Body><document><action>securitydetails</action></document></SOAP-ENV:Body></SOAP-ENV:Envelope>";
    reader = new BufferedReader( new StringReader( payload ) );

    when( request.getReader() ).thenReturn( reader );

    assertTrue( httpWebService.hasActionInBody( request ) );
  }

  @Test
  public void testHasActionInQueryString() throws Exception {
    when( request.getParameter( "action" ) ).thenReturn( "securitydetails" );
    assertTrue( httpWebService.hasActionInQueryString( request ) );
  }

  @Test
  public void testGetDetailsParameter_fromRequestParam() throws Exception {
    when( request.getParameter( "details" ) ).thenReturn( "minutia" );
    assertEquals( "minutia", httpWebService.getDetailsParameter( request ) );
  }

  @Test
  public void testGetDetailsParameter_errorGettingPayloadString() throws Exception {
    HttpWebService service = spy( httpWebService );
    doThrow( new IOException() ).when( service ).getPayloadAsString( request );
    assertNull( service.getDetailsParameter( request ) );
  }

  @Test
  public void testGetDetailsParameter() throws Exception {
    payload = "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"><SOAP-ENV:Body><document><details>minutia</details></document></SOAP-ENV:Body></SOAP-ENV:Envelope>";
    HttpWebService service = spy( httpWebService );
    doReturn( payload ).when( service ).getPayloadAsString( request );

    assertEquals( "minutia", service.getDetailsParameter( request ) );
  }

}
