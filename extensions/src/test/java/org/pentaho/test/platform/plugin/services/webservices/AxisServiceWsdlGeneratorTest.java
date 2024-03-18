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

package org.pentaho.test.platform.plugin.services.webservices;

import org.junit.Test;
import org.mockito.MockedStatic;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.engine.core.output.SimpleOutputHandler;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.PentahoSystem;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public class AxisServiceWsdlGeneratorTest {

  @Test
  public void testBadInit2() throws Exception {

    assertNull( AxisWebServiceManager.currentAxisConfiguration );

  }

  @Test
  public void testBadInit3() throws Exception {

    MockedStatic<PentahoSystem>  pentahoSystem = mockStatic( PentahoSystem.class );
    IAuthorizationPolicy policy = mock( IAuthorizationPolicy.class );
    pentahoSystem.when( () -> PentahoSystem.get( eq( IAuthorizationPolicy.class ) ) ).thenReturn( policy );
    when( policy.isAllowed( anyString() ) ).thenReturn( true );

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
