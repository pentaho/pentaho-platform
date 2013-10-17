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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.test.platform.plugin.services.webservices;

import com.mockrunner.mock.web.MockHttpServletRequest;
import com.mockrunner.mock.web.MockHttpServletResponse;
import com.mockrunner.mock.web.MockServletConfig;
import com.mockrunner.mock.web.MockServletContext;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.transport.local.LocalTransportReceiver;
import org.junit.Ignore;
import org.junit.Test;
import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.engine.core.output.SimpleOutputHandler;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.plugin.services.pluginmgr.servicemgr.AxisWebServiceManager;
import org.pentaho.platform.plugin.services.webservices.content.AxisServiceExecutor;
import org.pentaho.platform.util.web.SimpleUrlFactory;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

@SuppressWarnings( "nls" )
public class AxisServiceExecutorTest {

  private void setupAxis() throws AxisFault {
    StandaloneSession session = new StandaloneSession( "test" );

    StubServiceSetup setup = new StubServiceSetup();
    setup.setSession( session );

    /*
     * // create a test transport so we can catch the output config.setTransportOut( "http" );
     * 
     * assertEquals( "Transport is wrong", "http", config.getTransportOut() );
     */
    AxisConfiguration axisConfig = AxisWebServiceManager.currentAxisConfiguration;

    TransportInDescription tIn = new TransportInDescription( "http" );
    StubTransportListener receiver = new StubTransportListener();
    tIn.setReceiver( receiver );
    axisConfig.addTransportIn( tIn );

    TransportOutDescription tOut = new TransportOutDescription( "http" );
    StubTransportSender sender = new StubTransportSender();
    tOut.setSender( sender );
    axisConfig.addTransportOut( tOut );

    LocalTransportReceiver.CONFIG_CONTEXT = new ConfigurationContext( axisConfig );
  }

  @Test
  @Ignore
  public void testRunGet1() throws Exception {
    setupAxis();

    AxisServiceExecutor contentGenerator = new AxisServiceExecutor();

    assertNotNull( "contentGenerator is null", contentGenerator );
    assertNotNull( "Logger is null", contentGenerator.getLogger() );

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    IOutputHandler outputHandler = new SimpleOutputHandler( out, false );

    String baseUrl = "http://testhost:testport/testcontent";
    Map<String, IParameterProvider> parameterProviders = new HashMap<String, IParameterProvider>();
    SimpleParameterProvider requestParams = new SimpleParameterProvider();
    parameterProviders.put( IParameterProvider.SCOPE_REQUEST, requestParams );
    SimpleParameterProvider pathParams = new SimpleParameterProvider();
    pathParams.setParameter( "path", "/StubService/getString" );
    pathParams.setParameter( "remoteaddr", "http:test" );
    parameterProviders.put( "path", pathParams );
    SimpleUrlFactory urlFactory = new SimpleUrlFactory( baseUrl + "?" );
    List<String> messages = new ArrayList<String>();
    contentGenerator.setOutputHandler( outputHandler );
    MimeTypeListener mimeTypeListener = new MimeTypeListener();
    outputHandler.setMimeTypeListener( mimeTypeListener );
    contentGenerator.setMessagesList( messages );
    contentGenerator.setParameterProviders( parameterProviders );
    contentGenerator.setSession( PentahoSessionHolder.getSession() );
    contentGenerator.setUrlFactory( urlFactory );

    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    MockServletConfig servletConfig = new MockServletConfig();
    MockServletContext servletContext = new MockServletContext();
    servletConfig.setServletContext( servletContext );

    pathParams.setParameter( "servletconfig", servletConfig );

    request.setMethod( "GET" );
    request.setRequestURI( "/pentaho/content/ws-run/StubService/getString" );
    request.setRequestURL( "http://localhost:8080/pentaho/content/ws-run/StubService/getString" );
    request.setRemoteAddr( "127.0.0.1" );

    pathParams.setParameter( "httprequest", request );
    pathParams.setParameter( "httpresponse", response );

    try {

      IContentItem contentItem = outputHandler.getOutputContentItem( "response", "content", null, null );
      assertEquals( "content type is wrong", null, contentItem.getMimeType() );
      contentGenerator.setContentType( "text/xml" );
      contentItem = outputHandler.getOutputContentItem( "response", "content", null, null );
      assertEquals( "content type is wrong", "text/xml", contentItem.getMimeType() );

      StubTransportSender.transportOutStr = null;
      StubService.getStringCalled = false;
      contentGenerator.createContent();

      assertTrue( StubService.getStringCalled );
      String content = StubTransportSender.transportOutStr;
      assertEquals( "result are wrong",
          "<ns:getStringResponse xmlns:ns=\"http://webservices.services.plugin.platform.test.pentaho.org\"><return>test"
              + " result</return></ns:getStringResponse>", content );
      System.out.println( content );

    } catch ( Exception e ) {
      e.printStackTrace();
      assertTrue( "Exception occurred", false );
    }
  }

  @Test
  @Ignore
  public void testRunGet2() throws Exception {

    StandaloneSession session = new StandaloneSession( "test" );

    AxisServiceExecutor contentGenerator = new AxisServiceExecutor();

    assertNotNull( "contentGenerator is null", contentGenerator );
    assertNotNull( "Logger is null", contentGenerator.getLogger() );

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    IOutputHandler outputHandler = new SimpleOutputHandler( out, false );

    String baseUrl = "http://testhost:testport/testcontent";
    Map<String, IParameterProvider> parameterProviders = new HashMap<String, IParameterProvider>();
    SimpleParameterProvider requestParams = new SimpleParameterProvider();
    parameterProviders.put( IParameterProvider.SCOPE_REQUEST, requestParams );
    SimpleParameterProvider pathParams = new SimpleParameterProvider();
    pathParams.setParameter( "path", "/StubService/setString?str=testinput" );
    parameterProviders.put( "path", pathParams );
    SimpleUrlFactory urlFactory = new SimpleUrlFactory( baseUrl + "?" );
    List<String> messages = new ArrayList<String>();
    contentGenerator.setOutputHandler( outputHandler );
    MimeTypeListener mimeTypeListener = new MimeTypeListener();
    outputHandler.setMimeTypeListener( mimeTypeListener );
    contentGenerator.setMessagesList( messages );
    contentGenerator.setParameterProviders( parameterProviders );
    contentGenerator.setSession( session );
    contentGenerator.setUrlFactory( urlFactory );

    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    MockServletConfig servletConfig = new MockServletConfig();
    MockServletContext servletContext = new MockServletContext();
    servletConfig.setServletContext( servletContext );

    pathParams.setParameter( "servletconfig", servletConfig );

    request.setMethod( "GET" );
    request.setRequestURI( "/pentaho/content/ws-run/StubService/setString" );
    request.setRequestURL( "http://localhost:8080/pentaho/content/ws-run/StubService/getString" );
    request.setRemoteAddr( "127.0.0.1" );
    request.setQueryString( "str=testinput" );

    pathParams.setParameter( "httprequest", request );
    pathParams.setParameter( "httpresponse", response );

    try {
      StubService.setStringCalled = false;
      StubTransportSender.transportOutStr = null;
      contentGenerator.createContent();
      assertTrue( StubService.setStringCalled );
      assertEquals( "testinput", StubService.str );
      String content = StubTransportSender.transportOutStr;
      System.out.println( content );
    } catch ( Exception e ) {
      e.printStackTrace();
      assertTrue( "Exception occurred", false );
    }

  }

  @Test
  @Ignore
  public void testRunGet3() throws Exception {

    StandaloneSession session = new StandaloneSession( "test" );

    AxisServiceExecutor contentGenerator = new AxisServiceExecutor();

    assertNotNull( "contentGenerator is null", contentGenerator );
    assertNotNull( "Logger is null", contentGenerator.getLogger() );

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    IOutputHandler outputHandler = new SimpleOutputHandler( out, false );

    String baseUrl = "http://testhost:testport/testcontent";
    Map<String, IParameterProvider> parameterProviders = new HashMap<String, IParameterProvider>();
    SimpleParameterProvider requestParams = new SimpleParameterProvider();
    parameterProviders.put( IParameterProvider.SCOPE_REQUEST, requestParams );
    SimpleParameterProvider pathParams = new SimpleParameterProvider();
    pathParams.setParameter( "path", "/StubService/throwsError1" );
    parameterProviders.put( "path", pathParams );
    SimpleUrlFactory urlFactory = new SimpleUrlFactory( baseUrl + "?" );
    List<String> messages = new ArrayList<String>();
    contentGenerator.setOutputHandler( outputHandler );
    MimeTypeListener mimeTypeListener = new MimeTypeListener();
    outputHandler.setMimeTypeListener( mimeTypeListener );
    contentGenerator.setMessagesList( messages );
    contentGenerator.setParameterProviders( parameterProviders );
    contentGenerator.setSession( session );
    contentGenerator.setUrlFactory( urlFactory );

    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    MockServletConfig servletConfig = new MockServletConfig();
    MockServletContext servletContext = new MockServletContext();
    servletConfig.setServletContext( servletContext );

    pathParams.setParameter( "servletconfig", servletConfig );

    request.setMethod( "GET" );
    request.setRequestURI( "/pentaho/content/ws-run/StubService/throwsError1" );
    request.setRequestURL( "http://localhost:8080/pentaho/content/ws-run/StubService/throwsError1" );
    request.setRemoteAddr( "127.0.0.1" );

    pathParams.setParameter( "httprequest", request );
    pathParams.setParameter( "httpresponse", response );
    try {
      StubService.throwsError1Called = false;
      StubTransportSender.transportOutStr = null;
      contentGenerator.createContent();
      assertTrue( StubService.throwsError1Called );
      String content = StubTransportSender.transportOutStr;
      assertEquals( "Content should be empty", null, content );
      System.out.println( content );
    } catch ( Exception e ) {
      e.printStackTrace();
      assertTrue( "Exception occurred", false );
    }

  }

  @Test
  @Ignore
  public void testRunGet4() throws Exception {

    StandaloneSession session = new StandaloneSession( "test" );

    AxisServiceExecutor contentGenerator = new AxisServiceExecutor();

    assertNotNull( "contentGenerator is null", contentGenerator );
    assertNotNull( "Logger is null", contentGenerator.getLogger() );

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    IOutputHandler outputHandler = new SimpleOutputHandler( out, false );

    String baseUrl = "http://testhost:testport/testcontent";
    Map<String, IParameterProvider> parameterProviders = new HashMap<String, IParameterProvider>();
    SimpleParameterProvider requestParams = new SimpleParameterProvider();
    parameterProviders.put( IParameterProvider.SCOPE_REQUEST, requestParams );
    SimpleParameterProvider pathParams = new SimpleParameterProvider();
    pathParams.setParameter( "path", "/StubService/throwsError2" );
    parameterProviders.put( "path", pathParams );
    SimpleUrlFactory urlFactory = new SimpleUrlFactory( baseUrl + "?" );
    List<String> messages = new ArrayList<String>();
    contentGenerator.setOutputHandler( outputHandler );
    MimeTypeListener mimeTypeListener = new MimeTypeListener();
    outputHandler.setMimeTypeListener( mimeTypeListener );
    contentGenerator.setMessagesList( messages );
    contentGenerator.setParameterProviders( parameterProviders );
    contentGenerator.setSession( session );
    contentGenerator.setUrlFactory( urlFactory );

    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    MockServletConfig servletConfig = new MockServletConfig();
    MockServletContext servletContext = new MockServletContext();
    servletConfig.setServletContext( servletContext );

    pathParams.setParameter( "servletconfig", servletConfig );

    request.setMethod( "GET" );
    request.setRequestURI( "/pentaho/content/ws-run/StubService/throwsError2" );
    request.setRequestURL( "http://localhost:8080/pentaho/content/ws-run/StubService/throwsError2" );
    request.setRemoteAddr( "127.0.0.1" );

    pathParams.setParameter( "httprequest", request );
    pathParams.setParameter( "httpresponse", response );

    try {
      StubService.throwsError2Called = false;
      StubTransportSender.transportOutStr = null;
      contentGenerator.createContent();
      assertTrue( StubService.throwsError2Called );
      String content = StubTransportSender.transportOutStr;

      assertTrue( "results are wrong", content.indexOf( "soapenv:Fault" ) > 0 );
      assertTrue( "results are wrong", content.indexOf( "test error 2" ) > 0 );

      System.out.println( content );
    } catch ( Exception e ) {
      e.printStackTrace();
      assertTrue( "Exception occurred", false );
    }
  }

  @Test
  @Ignore
  public void testRunGet5() throws Exception {

    StandaloneSession session = new StandaloneSession( "test" );

    AxisServiceExecutor contentGenerator = new AxisServiceExecutor();

    assertNotNull( "contentGenerator is null", contentGenerator );
    assertNotNull( "Logger is null", contentGenerator.getLogger() );

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    IOutputHandler outputHandler = new SimpleOutputHandler( out, false );

    String baseUrl = "http://testhost:testport/testcontent";
    Map<String, IParameterProvider> parameterProviders = new HashMap<String, IParameterProvider>();
    SimpleParameterProvider requestParams = new SimpleParameterProvider();
    parameterProviders.put( IParameterProvider.SCOPE_REQUEST, requestParams );
    SimpleParameterProvider pathParams = new SimpleParameterProvider();
    pathParams.setParameter( "path", "/StubService/bogus" );
    parameterProviders.put( "path", pathParams );
    SimpleUrlFactory urlFactory = new SimpleUrlFactory( baseUrl + "?" );
    List<String> messages = new ArrayList<String>();
    contentGenerator.setOutputHandler( outputHandler );
    MimeTypeListener mimeTypeListener = new MimeTypeListener();
    outputHandler.setMimeTypeListener( mimeTypeListener );
    contentGenerator.setMessagesList( messages );
    contentGenerator.setParameterProviders( parameterProviders );
    contentGenerator.setSession( session );
    contentGenerator.setUrlFactory( urlFactory );

    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    MockServletConfig servletConfig = new MockServletConfig();
    MockServletContext servletContext = new MockServletContext();
    servletConfig.setServletContext( servletContext );

    pathParams.setParameter( "servletconfig", servletConfig );

    request.setMethod( "GET" );
    request.setRequestURI( "/pentaho/content/ws-run/StubService/bogus" );
    request.setRequestURL( "http://localhost:8080/pentaho/content/ws-run/StubService/bogus" );
    request.setRemoteAddr( "127.0.0.1" );

    pathParams.setParameter( "httprequest", request );
    pathParams.setParameter( "httpresponse", response );

    try {
      StubTransportSender.transportOutStr = null;
      contentGenerator.createContent();
      String content = StubTransportSender.transportOutStr;
      System.out.println( content );

      assertTrue( "results are wrong", content.indexOf( "soapenv:Fault" ) > 0 );
      assertTrue( "results are wrong", content.indexOf( "AxisServletHooks" ) > 0 );

    } catch ( Exception e ) {
      e.printStackTrace();
      assertTrue( "Exception occurred", false );
    }
  }

  @Test
  @Ignore
  public void testRunPost1() throws Exception {

    StandaloneSession session = new StandaloneSession( "test" );

    AxisServiceExecutor contentGenerator = new AxisServiceExecutor();

    assertNotNull( "contentGenerator is null", contentGenerator );
    assertNotNull( "Logger is null", contentGenerator.getLogger() );

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    IOutputHandler outputHandler = new SimpleOutputHandler( out, false );

    String baseUrl = "http://testhost:testport/testcontent";
    Map<String, IParameterProvider> parameterProviders = new HashMap<String, IParameterProvider>();
    SimpleParameterProvider requestParams = new SimpleParameterProvider();
    parameterProviders.put( IParameterProvider.SCOPE_REQUEST, requestParams );
    SimpleParameterProvider pathParams = new SimpleParameterProvider();
    pathParams.setParameter( "path", "/StubService" );
    pathParams.setParameter( "remoteaddr", "http:test" );
    parameterProviders.put( "path", pathParams );
    SimpleUrlFactory urlFactory = new SimpleUrlFactory( baseUrl + "?" );
    List<String> messages = new ArrayList<String>();
    contentGenerator.setOutputHandler( outputHandler );
    MimeTypeListener mimeTypeListener = new MimeTypeListener();
    outputHandler.setMimeTypeListener( mimeTypeListener );
    contentGenerator.setMessagesList( messages );
    contentGenerator.setParameterProviders( parameterProviders );
    contentGenerator.setSession( session );
    contentGenerator.setUrlFactory( urlFactory );

    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    MockServletConfig servletConfig = new MockServletConfig();
    MockServletContext servletContext = new MockServletContext();
    servletConfig.setServletContext( servletContext );

    pathParams.setParameter( "servletconfig", servletConfig );

    request.setMethod( "POST" );
    request.setRequestURI( "/pentaho/content/ws-run/StubService" );
    request.setRequestURL( "http://localhost:8080/pentaho/content/ws-run/StubService" );
    request.setRemoteAddr( "127.0.0.1" );
    request.setContentType( "application/soap+xml; charset=UTF-8; action=\"urn:getString\"" );
    String xml =
        "<?xml version='1.0' encoding='UTF-8'?><soapenv:Envelope xmlns:soapenv=\"http://www.w3"
            + ".org/2003/05/soap-envelope\"><soapenv:Body><ns2:getString xmlns:ns2=\"http://webservice.pentaho"
            + ".com\"></ns2:getString></soapenv:Body></soapenv:Envelope>";
    request.setBodyContent( xml );

    pathParams.setParameter( "httprequest", request );
    pathParams.setParameter( "httpresponse", response );

    try {
      StubTransportSender.transportOutStr = null;
      StubService.getStringCalled = false;
      contentGenerator.createContent();

      assertTrue( StubService.getStringCalled );
      String content = StubTransportSender.transportOutStr;
      assertEquals(
          "result are wrong",
          "<?xml version='1.0' encoding='UTF-8'?><soapenv:Envelope xmlns:soapenv=\"http://www.w3"
              + ".org/2003/05/soap-envelope\"><soapenv:Body><ns:getStringResponse xmlns:ns=\""
              + "http://webservices.services"
              + ".plugin.platform.test.pentaho.org\"><return>test "
              + "result</return></ns:getStringResponse></soapenv:Body></soapenv:Envelope>", content );
      System.out.println( content );

    } catch ( Exception e ) {
      e.printStackTrace();
      assertTrue( "Exception occurred", false );
    }
  }

  @Test
  @Ignore
  public void testRunPut1() throws Exception {

    StandaloneSession session = new StandaloneSession( "test" );

    AxisServiceExecutor contentGenerator = new AxisServiceExecutor();

    assertNotNull( "contentGenerator is null", contentGenerator );
    assertNotNull( "Logger is null", contentGenerator.getLogger() );

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    IOutputHandler outputHandler = new SimpleOutputHandler( out, false );

    String baseUrl = "http://testhost:testport/testcontent";
    Map<String, IParameterProvider> parameterProviders = new HashMap<String, IParameterProvider>();
    SimpleParameterProvider requestParams = new SimpleParameterProvider();
    parameterProviders.put( IParameterProvider.SCOPE_REQUEST, requestParams );
    SimpleParameterProvider pathParams = new SimpleParameterProvider();
    pathParams.setParameter( "path", "/StubService" );
    pathParams.setParameter( "remoteaddr", "http:test" );
    parameterProviders.put( "path", pathParams );
    SimpleUrlFactory urlFactory = new SimpleUrlFactory( baseUrl + "?" );
    List<String> messages = new ArrayList<String>();
    contentGenerator.setOutputHandler( outputHandler );
    MimeTypeListener mimeTypeListener = new MimeTypeListener();
    outputHandler.setMimeTypeListener( mimeTypeListener );
    contentGenerator.setMessagesList( messages );
    contentGenerator.setParameterProviders( parameterProviders );
    contentGenerator.setSession( session );
    contentGenerator.setUrlFactory( urlFactory );

    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    MockServletConfig servletConfig = new MockServletConfig();
    MockServletContext servletContext = new MockServletContext();
    servletConfig.setServletContext( servletContext );

    pathParams.setParameter( "servletconfig", servletConfig );

    request.setMethod( "PUT" );
    request.setRequestURI( "/pentaho/content/ws-run/StubService" );
    request.setRequestURL( "http://localhost:8080/pentaho/content/ws-run/StubService" );
    request.setRemoteAddr( "127.0.0.1" );
    request.setContentType( "application/soap+xml; charset=UTF-8; action=\"urn:getString\"" );
    String xml =
        "<?xml version='1.0' encoding='UTF-8'?><soapenv:Envelope xmlns:soapenv=\"http://www.w3"
            + ".org/2003/05/soap-envelope\"><soapenv:Body><ns2:getString xmlns:ns2=\"http://webservice.pentaho"
            + ".com\"></ns2:getString></soapenv:Body></soapenv:Envelope>";
    request.setBodyContent( xml );

    pathParams.setParameter( "httprequest", request );
    pathParams.setParameter( "httpresponse", response );

    try {
      StubTransportSender.transportOutStr = null;
      StubService.getStringCalled = false;
      contentGenerator.createContent();

      assertTrue( StubService.getStringCalled );
      String content = StubTransportSender.transportOutStr;
      assertEquals( "result are wrong",
          "<ns:getStringResponse xmlns:ns=\"http://webservices.services.plugin.platform.test.pentaho.org\"><return>test"
              + " result</return></ns:getStringResponse>", content );
      System.out.println( content );

    } catch ( Exception e ) {
      e.printStackTrace();
      assertTrue( "Exception occurred", false );
    }
  }

}
