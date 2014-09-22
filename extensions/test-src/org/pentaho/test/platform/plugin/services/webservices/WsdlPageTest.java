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

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.transport.local.LocalTransportReceiver;
import org.junit.Ignore;
import org.junit.Test;
import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.engine.core.output.SimpleOutputHandler;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.plugin.services.pluginmgr.servicemgr.AxisWebServiceManager;
import org.pentaho.platform.plugin.services.webservices.content.AxisServiceWsdlGenerator;
import org.pentaho.platform.plugin.services.webservices.messages.Messages;
import org.pentaho.platform.util.web.SimpleUrlFactory;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class WsdlPageTest {

  @Test
  @Ignore
  public void testRender() throws Exception {

    StandaloneSession session = new StandaloneSession( "test" ); //$NON-NLS-1$

    StubServiceSetup setup = new StubServiceSetup();
    setup.setSession( session );
    AxisConfiguration axisConfig = AxisWebServiceManager.currentAxisConfiguration;

    TransportInDescription tIn = new TransportInDescription( "http" ); //$NON-NLS-1$
    StubTransportListener receiver = new StubTransportListener();
    tIn.setReceiver( receiver );
    axisConfig.addTransportIn( tIn );

    TransportOutDescription tOut = new TransportOutDescription( "http" ); //$NON-NLS-1$
    StubTransportSender sender = new StubTransportSender();
    tOut.setSender( sender );
    axisConfig.addTransportOut( tOut );

    LocalTransportReceiver.CONFIG_CONTEXT = new ConfigurationContext( axisConfig );

    AxisServiceWsdlGenerator contentGenerator = new AxisServiceWsdlGenerator();

    assertNotNull( "contentGenerator is null", contentGenerator ); //$NON-NLS-1$
    assertNotNull( "Logger is null", contentGenerator.getLogger() ); //$NON-NLS-1$

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    IOutputHandler outputHandler = new SimpleOutputHandler( out, false );

    String baseUrl = "http://testhost:testport/testcontent"; //$NON-NLS-1$
    Map<String, IParameterProvider> parameterProviders = new HashMap<String, IParameterProvider>();
    SimpleParameterProvider requestParams = new SimpleParameterProvider();
    parameterProviders.put( IParameterProvider.SCOPE_REQUEST, requestParams );
    SimpleParameterProvider pathParams = new SimpleParameterProvider();
    pathParams.setParameter( "path", "/StubService" ); //$NON-NLS-1$//$NON-NLS-2$
    parameterProviders.put( "path", pathParams ); //$NON-NLS-1$
    SimpleUrlFactory urlFactory = new SimpleUrlFactory( baseUrl + "?" ); //$NON-NLS-1$
    List<String> messages = new ArrayList<String>();
    contentGenerator.setOutputHandler( outputHandler );
    MimeTypeListener mimeTypeListener = new MimeTypeListener();
    outputHandler.setMimeTypeListener( mimeTypeListener );
    contentGenerator.setMessagesList( messages );
    contentGenerator.setParameterProviders( parameterProviders );
    contentGenerator.setSession( session );
    contentGenerator.setUrlFactory( urlFactory );
    try {
      contentGenerator.createContent();
      String content = new String( out.toByteArray() );
      System.out.println( content );

      assertTrue( "wsdl:definitions is missing", content.indexOf( "wsdl:definitions" ) != -1 ); //$NON-NLS-1$ //$NON-NLS-2$
      assertTrue(
          "targetNamespace is missing", content.indexOf( "targetNamespace=\"http://webservice.pentaho.com\"" ) != -1 ); //$NON-NLS-1$ //$NON-NLS-2$

      assertTrue(
          "<xs:complexType name=\"ComplexType\">", content.indexOf( "<xs:complexType name=\"ComplexType\">" ) != -1 ); //$NON-NLS-1$ //$NON-NLS-2$
      assertTrue( "name=\"address\"", content.indexOf( "name=\"address\"" ) != -1 ); //$NON-NLS-1$ //$NON-NLS-2$
      assertTrue( "name=\"age\"", content.indexOf( "name=\"age\"" ) != -1 ); //$NON-NLS-1$ //$NON-NLS-2$
      assertTrue( "name=\"name\"", content.indexOf( "name=\"name\"" ) != -1 ); //$NON-NLS-1$ //$NON-NLS-2$

      assertTrue( "setStringRequest", content.indexOf( "setStringRequest" ) != -1 ); //$NON-NLS-1$ //$NON-NLS-2$
      assertTrue( "getStringResponse", content.indexOf( "getStringResponse" ) != -1 ); //$NON-NLS-1$ //$NON-NLS-2$
      assertTrue( "urn:setString", content.indexOf( "urn:setString" ) != -1 ); //$NON-NLS-1$ //$NON-NLS-2$
      assertTrue( "urn:getString", content.indexOf( "urn:getString" ) != -1 ); //$NON-NLS-1$ //$NON-NLS-2$
      assertTrue( "urn:getStringResponse", content.indexOf( "urn:getStringResponse" ) != -1 ); //$NON-NLS-1$ //$NON-NLS-2$
      assertTrue( "StubServiceSoap11Binding", content.indexOf( "StubServiceSoap11Binding" ) != -1 ); //$NON-NLS-1$ //$NON-NLS-2$
      assertTrue( "<wsdl:operation name=\"setString\">", content.indexOf( "<wsdl:operation name=\"setString\">" ) != -1 ); //$NON-NLS-1$ //$NON-NLS-2$
      assertTrue(
          "<wsdl:binding name=\"StubServiceHttpBinding\"", content.indexOf( "<wsdl:binding name=\"StubServiceHttpBinding\"" ) != -1 ); //$NON-NLS-1$ //$NON-NLS-2$
      assertTrue( "<wsdl:operation name=\"getString\">", content.indexOf( "<wsdl:operation name=\"getString\">" ) != -1 ); //$NON-NLS-1$ //$NON-NLS-2$
      assertTrue( "StubServiceSoap12Binding", content.indexOf( "StubServiceSoap12Binding" ) != -1 ); //$NON-NLS-1$ //$NON-NLS-2$
      assertTrue( "<wsdl:service name=\"StubService\">", content.indexOf( "<wsdl:service name=\"StubService\">" ) != -1 ); //$NON-NLS-1$ //$NON-NLS-2$
      assertTrue(
          "http://testhost:8080/testcontext/content/ws-run/StubService", content.indexOf( "http://testhost:8080/testcontext/content/ws-run/StubService" ) != -1 ); //$NON-NLS-1$ //$NON-NLS-2$

    } catch ( Exception e ) {
      assertTrue( "Exception occurred", false ); //$NON-NLS-1$
    }
  }

  @Test
  @Ignore
  public void testMissingPathParamProvider() throws Exception {

    StandaloneSession session = new StandaloneSession( "test" ); //$NON-NLS-1$

    StubServiceSetup setup = new StubServiceSetup();
    setup.setSession( session );

    AxisServiceWsdlGenerator contentGenerator = new AxisServiceWsdlGenerator();

    assertNotNull( "contentGenerator is null", contentGenerator ); //$NON-NLS-1$
    assertNotNull( "Logger is null", contentGenerator.getLogger() ); //$NON-NLS-1$

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    IOutputHandler outputHandler = new SimpleOutputHandler( out, false );

    String baseUrl = "http://testhost:testport/testcontent"; //$NON-NLS-1$
    Map<String, IParameterProvider> parameterProviders = new HashMap<String, IParameterProvider>();
    SimpleParameterProvider requestParams = new SimpleParameterProvider();
    parameterProviders.put( IParameterProvider.SCOPE_REQUEST, requestParams );
    SimpleUrlFactory urlFactory = new SimpleUrlFactory( baseUrl + "?" ); //$NON-NLS-1$
    List<String> messages = new ArrayList<String>();
    contentGenerator.setOutputHandler( outputHandler );
    MimeTypeListener mimeTypeListener = new MimeTypeListener();
    outputHandler.setMimeTypeListener( mimeTypeListener );
    contentGenerator.setMessagesList( messages );
    contentGenerator.setParameterProviders( parameterProviders );
    contentGenerator.setSession( session );
    contentGenerator.setUrlFactory( urlFactory );
    try {
      contentGenerator.createContent();
      String content = new String( out.toByteArray() );
      assertTrue( content.indexOf( Messages.getInstance().getErrorString(
          "WebServiceContentGenerator.ERROR_0004_PATH_PARAMS_IS_MISSING" ) ) != -1 ); //$NON-NLS-1$
      System.out.println( content );

    } catch ( Exception e ) {
      assertTrue( "Exception occurred", false ); //$NON-NLS-1$
    }
  }

  @Test
  @Ignore
  public void testMissingServiceName() throws Exception {

    StandaloneSession session = new StandaloneSession( "test" ); //$NON-NLS-1$

    StubServiceSetup setup = new StubServiceSetup();
    setup.setSession( session );

    AxisServiceWsdlGenerator contentGenerator = new AxisServiceWsdlGenerator();

    assertNotNull( "contentGenerator is null", contentGenerator ); //$NON-NLS-1$
    assertNotNull( "Logger is null", contentGenerator.getLogger() ); //$NON-NLS-1$

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    IOutputHandler outputHandler = new SimpleOutputHandler( out, false );

    String baseUrl = "http://testhost:testport/testcontent"; //$NON-NLS-1$
    Map<String, IParameterProvider> parameterProviders = new HashMap<String, IParameterProvider>();
    SimpleParameterProvider requestParams = new SimpleParameterProvider();
    parameterProviders.put( IParameterProvider.SCOPE_REQUEST, requestParams );
    SimpleParameterProvider pathParams = new SimpleParameterProvider();
    parameterProviders.put( "path", pathParams ); //$NON-NLS-1$
    SimpleUrlFactory urlFactory = new SimpleUrlFactory( baseUrl + "?" ); //$NON-NLS-1$
    List<String> messages = new ArrayList<String>();
    contentGenerator.setOutputHandler( outputHandler );
    MimeTypeListener mimeTypeListener = new MimeTypeListener();
    outputHandler.setMimeTypeListener( mimeTypeListener );
    contentGenerator.setMessagesList( messages );
    contentGenerator.setParameterProviders( parameterProviders );
    contentGenerator.setSession( session );
    contentGenerator.setUrlFactory( urlFactory );
    try {
      contentGenerator.createContent();
      String content = new String( out.toByteArray() );
      System.out.println( content );
      assertTrue( content.indexOf( Messages.getInstance().getErrorString(
          "WebServiceContentGenerator.ERROR_0005_SERVICE_NAME_IS_MISSING" ) ) != -1 ); //$NON-NLS-1$

    } catch ( Exception e ) {
      assertTrue( "Exception occurred", false ); //$NON-NLS-1$
    }
  }

  @Test
  @Ignore
  public void testBadServiceName() throws Exception {

    StandaloneSession session = new StandaloneSession( "test" ); //$NON-NLS-1$

    StubServiceSetup setup = new StubServiceSetup();
    setup.setSession( session );

    AxisServiceWsdlGenerator contentGenerator = new AxisServiceWsdlGenerator();

    assertNotNull( "contentGenerator is null", contentGenerator ); //$NON-NLS-1$
    assertNotNull( "Logger is null", contentGenerator.getLogger() ); //$NON-NLS-1$

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    IOutputHandler outputHandler = new SimpleOutputHandler( out, false );

    String baseUrl = "http://testhost:testport/testcontent"; //$NON-NLS-1$
    Map<String, IParameterProvider> parameterProviders = new HashMap<String, IParameterProvider>();
    SimpleParameterProvider requestParams = new SimpleParameterProvider();
    parameterProviders.put( IParameterProvider.SCOPE_REQUEST, requestParams );
    SimpleParameterProvider pathParams = new SimpleParameterProvider();
    pathParams.setParameter( "path", "/bogus" ); //$NON-NLS-1$//$NON-NLS-2$
    parameterProviders.put( "path", pathParams ); //$NON-NLS-1$
    SimpleUrlFactory urlFactory = new SimpleUrlFactory( baseUrl + "?" ); //$NON-NLS-1$
    List<String> messages = new ArrayList<String>();
    contentGenerator.setOutputHandler( outputHandler );
    MimeTypeListener mimeTypeListener = new MimeTypeListener();
    outputHandler.setMimeTypeListener( mimeTypeListener );
    contentGenerator.setMessagesList( messages );
    contentGenerator.setParameterProviders( parameterProviders );
    contentGenerator.setSession( session );
    contentGenerator.setUrlFactory( urlFactory );
    try {
      contentGenerator.createContent();
      String content = new String( out.toByteArray() );
      System.out.println( content );
      assertTrue( content.indexOf( Messages.getInstance().getErrorString(
          "WebServiceContentGenerator.ERROR_0006_SERVICE_IS_INVALID", "bogus" ) ) != -1 ); //$NON-NLS-1$ //$NON-NLS-2$
    } catch ( Exception e ) {
      assertTrue( "Exception occurred", false ); //$NON-NLS-1$
    }
  }

}
