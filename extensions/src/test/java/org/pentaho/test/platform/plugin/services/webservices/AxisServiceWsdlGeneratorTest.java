/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.test.platform.plugin.services.webservices;

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

import static org.junit.Assert.*;

public class AxisServiceWsdlGeneratorTest {

  @Test
  public void testBadInit2() throws Exception {

    assertNull( AxisWebServiceManager.currentAxisConfiguration );

  }

  @Test
  public void testBadInit3() throws Exception {
    StandaloneSession session = new StandaloneSession( "test" ); //$NON-NLS-1$

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
    contentGenerator.createContent();
    String content = new String( out.toByteArray() );
    System.out.println( content );
    assertTrue( content.indexOf( Messages.getInstance().getErrorString(
        "WebServiceContentGenerator.ERROR_0001_AXIS_CONFIG_IS_NULL" ) ) != -1 ); //$NON-NLS-1$ 
  }

}
