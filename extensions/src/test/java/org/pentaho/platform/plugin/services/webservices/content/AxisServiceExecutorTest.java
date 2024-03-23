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
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.plugin.services.webservices.content;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.junit.*;
import org.mockito.MockedStatic;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.engine.core.output.SimpleOutputHandler;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.plugin.services.pluginmgr.servicemgr.AxisWebServiceManager;
import org.pentaho.platform.util.web.SimpleUrlFactory;
import org.pentaho.test.platform.plugin.services.webservices.MimeTypeListener;
import org.pentaho.test.platform.plugin.services.webservices.StubService;
import org.pentaho.test.platform.plugin.services.webservices.StubServiceSetup;
import org.pentaho.test.platform.plugin.services.webservices.StubTransportListener;
import org.pentaho.test.platform.plugin.services.webservices.StubTransportSender;

import com.mockrunner.mock.web.MockHttpServletRequest;
import com.mockrunner.mock.web.MockHttpServletResponse;

public class AxisServiceExecutorTest {

  private static final String BASE_URL = "http://testhost:testport/testcontent";
  private static final String DEAFULT_TRANSPOT_PROTOCOL = "http";
  private static final String REMOTE_ADDRESS = "127.0.0.1";

  private ByteArrayOutputStream out;
  private AxisServiceExecutor contentGenerator;
  private static MockedStatic<PentahoSystem> pentahoSystem;


  @Before
  public void setUp() throws Exception {
    StandaloneSession session = new StandaloneSession( "test" );
    StubServiceSetup serviceSetup = new StubServiceSetup();
    serviceSetup.setSession( session );

    AxisConfiguration axisCfg = serviceSetup.getAxisConfiguration();
    ConfigurationContext configContext = new ConfigurationContext( axisCfg );

    serviceSetup.loadServices();

    TransportInDescription tIn = new TransportInDescription( DEAFULT_TRANSPOT_PROTOCOL );
    StubTransportListener receiver = new StubTransportListener();
    tIn.setReceiver( receiver );
    axisCfg.addTransportIn( tIn );

    TransportOutDescription tOut = new TransportOutDescription( DEAFULT_TRANSPOT_PROTOCOL );
    StubTransportSender sender = new StubTransportSender();
    tOut.setSender( sender );
    axisCfg.addTransportOut( tOut );

    AxisWebServiceManager.currentAxisConfiguration = axisCfg;
    AxisWebServiceManager.currentAxisConfigContext = configContext;

    out = new ByteArrayOutputStream();
    IOutputHandler outputHandler = new SimpleOutputHandler( out, false );
    outputHandler.setMimeTypeListener( new MimeTypeListener() );

    contentGenerator = new AxisServiceExecutor();

    contentGenerator.setOutputHandler( outputHandler );
    contentGenerator.setMessagesList( new ArrayList<String>() );

    contentGenerator.setSession( session );
    contentGenerator.setUrlFactory( new SimpleUrlFactory( BASE_URL + "?" ) );

    assertNotNull( "contentGenerator is null", contentGenerator );
    assertNotNull( "Logger is null", contentGenerator.getLogger() );

    pentahoSystem = mockStatic( PentahoSystem.class );
    IAuthorizationPolicy policy = mock( IAuthorizationPolicy.class );
    pentahoSystem.when( () -> PentahoSystem.get( eq( IAuthorizationPolicy.class ) ) ).thenReturn( policy );
    when( policy.isAllowed( anyString() ) ).thenReturn( true );
  }

  @After
  public void cleanUp() {
    pentahoSystem.close();
  }

  @Test
  public void testRunGet() throws Exception {
    Map<String, IParameterProvider> parameterProviders = new HashMap<String, IParameterProvider>();
    SimpleParameterProvider pathParams = new SimpleParameterProvider();
    pathParams.setParameter( "path", "/StubService/getString" );
    pathParams.setParameter( "remoteaddr", "http:test" );
    parameterProviders.put( "path", pathParams );
    contentGenerator.setParameterProviders( parameterProviders );

    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    request.setMethod( "GET" );
    request.setRequestURI( "/pentaho/content/ws-run/StubService/getString" );
    request.setRequestURL( "http://localhost:8080/pentaho/content/ws-run/StubService/getString" );
    request.setRemoteAddr( REMOTE_ADDRESS );

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
    } catch ( Exception e ) {
      assertTrue( "Exception occurred", false );
    }
  }

  @Test
  public void testRunGetWithParameter() throws Exception {
    Map<String, IParameterProvider> parameterProviders = new HashMap<String, IParameterProvider>();
    SimpleParameterProvider pathParams = new SimpleParameterProvider();
    pathParams.setParameter( "path", "/StubService/setString?str=testinput" );
    parameterProviders.put( "path", pathParams );
    contentGenerator.setParameterProviders( parameterProviders );

    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    request.setMethod( "GET" );
    request.setRequestURI( "/pentaho/content/ws-run/StubService/setString" );
    request.setRequestURL( "http://localhost:8080/pentaho/content/ws-run/StubService/getString" );
    request.setRemoteAddr( REMOTE_ADDRESS );
    request.setQueryString( "str=testinput" );

    pathParams.setParameter( "httprequest", request );
    pathParams.setParameter( "httpresponse", response );

    try {
      StubService.setStringCalled = false;
      StubTransportSender.transportOutStr = null;

      contentGenerator.createContent();

      assertTrue( StubService.setStringCalled );
      assertEquals( "testinput", StubService.str );
    } catch ( Exception e ) {
      assertTrue( "Exception occurred", false );
    }
  }

  @Test
  public void testRunGetThrowError() throws Exception {
    Map<String, IParameterProvider> parameterProviders = new HashMap<String, IParameterProvider>();
    SimpleParameterProvider pathParams = new SimpleParameterProvider();
    pathParams.setParameter( "path", "/StubService/throwsError1" );
    parameterProviders.put( "path", pathParams );
    contentGenerator.setParameterProviders( parameterProviders );

    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    request.setMethod( "GET" );
    request.setRequestURI( "/pentaho/content/ws-run/StubService/throwsError1" );
    request.setRequestURL( "http://localhost:8080/pentaho/content/ws-run/StubService/throwsError1" );
    request.setRemoteAddr( REMOTE_ADDRESS );

    pathParams.setParameter( "httprequest", request );
    pathParams.setParameter( "httpresponse", response );
    try {
      StubService.throwsError1Called = false;
      StubTransportSender.transportOutStr = null;

      contentGenerator.createContent();

      assertTrue( StubService.throwsError1Called );
      String content = StubTransportSender.transportOutStr;
      assertNull( "Content should be empty", content );
    } catch ( Exception e ) {
      assertTrue( "Exception occurred", false );
    }
  }

  @Test
  public void testRunGetErrorResponse() throws Exception {
    Map<String, IParameterProvider> parameterProviders = new HashMap<String, IParameterProvider>();
    SimpleParameterProvider pathParams = new SimpleParameterProvider();
    pathParams.setParameter( "path", "/StubService/throwsError2" );
    parameterProviders.put( "path", pathParams );
    contentGenerator.setParameterProviders( parameterProviders );

    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    request.setMethod( "GET" );
    request.setRequestURI( "/pentaho/content/ws-run/StubService/throwsError2" );
    request.setRequestURL( "http://localhost:8080/pentaho/content/ws-run/StubService/throwsError2" );
    request.setRemoteAddr( REMOTE_ADDRESS );

    pathParams.setParameter( "httprequest", request );
    pathParams.setParameter( "httpresponse", response );

    try {
      StubService.throwsError2Called = false;
      StubTransportSender.transportOutStr = null;

      contentGenerator.createContent();

      assertTrue( StubService.throwsError2Called );
      String content = StubTransportSender.transportOutStr;
      assertTrue( "results are wrong", content.contains( "soapenv:Fault" ) );
      assertTrue( "results are wrong", content.contains( "test error 2" ) );
    } catch ( Exception e ) {
      assertTrue( "Exception occurred", false );
    }
  }

  @Test
  public void testRunGetAxisServletHooks() throws Exception {
    Map<String, IParameterProvider> parameterProviders = new HashMap<String, IParameterProvider>();
    SimpleParameterProvider pathParams = new SimpleParameterProvider();
    pathParams.setParameter( "path", "/StubService/bogus" );
    parameterProviders.put( "path", pathParams );
    contentGenerator.setParameterProviders( parameterProviders );

    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    request.setMethod( "GET" );
    request.setRequestURI( "/pentaho/content/ws-run/StubService/bogus" );
    request.setRequestURL( "http://localhost:8080/pentaho/content/ws-run/StubService/bogus" );
    request.setRemoteAddr( REMOTE_ADDRESS );

    pathParams.setParameter( "httprequest", request );
    pathParams.setParameter( "httpresponse", response );

    try {
      StubTransportSender.transportOutStr = null;

      contentGenerator.createContent();

      String content = StubTransportSender.transportOutStr;
      assertTrue( "results are wrong", content.contains( "soapenv:Fault" ) );
      assertTrue( "results are wrong", content.contains( "AxisServletHooks" ) );
    } catch ( Exception e ) {
      assertTrue( "Exception occurred", false );
    }
  }

  @Test
  public void testRunPost() throws Exception {
    Map<String, IParameterProvider> parameterProviders = new HashMap<String, IParameterProvider>();
    SimpleParameterProvider pathParams = new SimpleParameterProvider();
    pathParams.setParameter( "path", "/StubService" );
    pathParams.setParameter( "remoteaddr", "http:test" );
    parameterProviders.put( "path", pathParams );
    contentGenerator.setParameterProviders( parameterProviders );

    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    request.setMethod( "POST" );
    request.setRequestURI( "/pentaho/content/ws-run/StubService" );
    request.setRequestURL( "http://localhost:8080/pentaho/content/ws-run/StubService" );
    request.setRemoteAddr( REMOTE_ADDRESS );
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
        "<?xml version='1.0' encoding='UTF-8'?><soapenv:Envelope xmlns:soapenv=\"http://www.w3"
          + ".org/2003/05/soap-envelope\"><soapenv:Body><ns:getStringResponse xmlns:ns=\""
          + "http://webservices.services" + ".plugin.platform.test.pentaho.org\"><return>test "
          + "result</return></ns:getStringResponse></soapenv:Body></soapenv:Envelope>", content );
    } catch ( Exception e ) {
      assertTrue( "Exception occurred", false );
    }
  }

  @Test
  public void testRunPut() throws Exception {
    Map<String, IParameterProvider> parameterProviders = new HashMap<String, IParameterProvider>();
    SimpleParameterProvider pathParams = new SimpleParameterProvider();
    pathParams.setParameter( "path", "/StubService" );
    pathParams.setParameter( "remoteaddr", "http:test" );
    parameterProviders.put( "path", pathParams );
    contentGenerator.setParameterProviders( parameterProviders );

    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    request.setMethod( "PUT" );
    request.setRequestURI( "/pentaho/content/ws-run/StubService" );
    request.setRequestURL( "http://localhost:8080/pentaho/content/ws-run/StubService" );
    request.setRemoteAddr( REMOTE_ADDRESS );
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
    } catch ( Exception e ) {
      assertTrue( "Exception occurred", false );
    }
  }

}
