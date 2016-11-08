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
import org.apache.axis2.engine.AxisConfiguration;
import org.junit.After;
import org.junit.Before;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class WsdlPageTest {

  private static final String BASE_URL = "http://testhost:testport/testcontent";

  private AxisConfiguration beforeTestCfg;
  private ConfigurationContext beforeTestCtx;

  private ByteArrayOutputStream out;
  private AxisServiceWsdlGenerator contentGenerator;

  @Before
  public void setUp() {
    beforeTestCfg = AxisWebServiceManager.currentAxisConfiguration;
    beforeTestCtx = AxisWebServiceManager.currentAxisConfigContext;

    AxisConfiguration axisCfg = new AxisConfiguration();
    AxisWebServiceManager.currentAxisConfiguration = axisCfg;
    AxisWebServiceManager.currentAxisConfigContext = new ConfigurationContext( axisCfg );


    out = new ByteArrayOutputStream();
    IOutputHandler outputHandler = new SimpleOutputHandler( out, false );
    outputHandler.setMimeTypeListener( new MimeTypeListener() );

    StandaloneSession session = new StandaloneSession( "test" );
    StubServiceSetup serviceSetup = new StubServiceSetup();
    serviceSetup.setSession( session );

    contentGenerator = new AxisServiceWsdlGenerator();
    contentGenerator.setOutputHandler( outputHandler );
    contentGenerator.setMessagesList( new ArrayList<String>() );
    contentGenerator.setSession( session );
    contentGenerator.setUrlFactory( new SimpleUrlFactory( BASE_URL + "?" ) );
  }

  @After
  public void tearDown() {
    AxisWebServiceManager.currentAxisConfiguration = beforeTestCfg;
    AxisWebServiceManager.currentAxisConfigContext = beforeTestCtx;
  }


  @Test
  public void testMissingPathParamProvider() throws Exception {
    assertNotNull( "Logger is null", contentGenerator.getLogger() ); //$NON-NLS-1$

    Map<String, IParameterProvider> parameterProviders = Collections.<String, IParameterProvider>singletonMap(
      IParameterProvider.SCOPE_REQUEST, new SimpleParameterProvider()
    );

    contentGenerator.setParameterProviders( parameterProviders );
    contentGenerator.createContent();
    String content = new String( out.toByteArray() );

    assertTrue( content, content.contains( Messages.getInstance().getErrorString(
      "WebServiceContentGenerator.ERROR_0004_PATH_PARAMS_IS_MISSING" ) ) ); //$NON-NLS-1$
  }

  @Test
  public void testMissingServiceName() throws Exception {
    assertNotNull( "Logger is null", contentGenerator.getLogger() ); //$NON-NLS-1$

    Map<String, IParameterProvider> parameterProviders = new HashMap<String, IParameterProvider>();
    parameterProviders.put( IParameterProvider.SCOPE_REQUEST, new SimpleParameterProvider() );
    parameterProviders.put( "path", new SimpleParameterProvider() ); //$NON-NLS-1$

    contentGenerator.setParameterProviders( parameterProviders );
    contentGenerator.createContent();
    String content = new String( out.toByteArray() );

    assertTrue( content, content.contains( Messages.getInstance().getErrorString(
      "WebServiceContentGenerator.ERROR_0005_SERVICE_NAME_IS_MISSING" ) ) ); //$NON-NLS-1$
  }

  @Test
  public void testBadServiceName() throws Exception {
    assertNotNull( "Logger is null", contentGenerator.getLogger() ); //$NON-NLS-1$

    Map<String, IParameterProvider> parameterProviders = new HashMap<String, IParameterProvider>();
    parameterProviders.put( IParameterProvider.SCOPE_REQUEST, new SimpleParameterProvider() );
    SimpleParameterProvider pathParams = new SimpleParameterProvider();
    pathParams.setParameter( "path", "/bogus" ); //$NON-NLS-1$//$NON-NLS-2$
    parameterProviders.put( "path", pathParams ); //$NON-NLS-1$

    contentGenerator.setParameterProviders( parameterProviders );
    contentGenerator.createContent();
    String content = new String( out.toByteArray() );

    assertTrue( content, content.contains( Messages.getInstance().getErrorString(
      "WebServiceContentGenerator.ERROR_0006_SERVICE_IS_INVALID", "bogus" ) ) ); //$NON-NLS-1$ //$NON-NLS-2$
  }

}
