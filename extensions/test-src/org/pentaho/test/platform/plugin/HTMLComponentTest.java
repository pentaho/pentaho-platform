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

package org.pentaho.test.platform.plugin;

import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.engine.core.output.SimpleOutputHandler;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.services.BaseRequestHandler;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.platform.uifoundation.component.HtmlComponent;
import org.pentaho.platform.util.web.SimpleUrlFactory;
import org.pentaho.test.platform.engine.core.BaseTest;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;

@SuppressWarnings( "nls" )
public class HTMLComponentTest extends BaseTest {
  private static final String SOLUTION_PATH = "test-src/solution";

  public String getSolutionPath() {
    return SOLUTION_PATH;
  }

  public void testComponent1() {
    startTest();
    // This should succeed
    String url = "http://www.pentaho.org/demo/news.html"; //$NON-NLS-1$
    SimpleUrlFactory urlFactory = new SimpleUrlFactory( "/testurl?" ); //$NON-NLS-1$
    ArrayList messages = new ArrayList();
    HtmlComponent component = new HtmlComponent( HtmlComponent.TYPE_URL, url, "", urlFactory, messages ); //$NON-NLS-1$
    StandaloneSession session =
        new StandaloneSession( Messages.getInstance().getString( "BaseTest.DEBUG_JUNIT_SESSION" ) ); //$NON-NLS-1$
    try {
      OutputStream outputStream = getOutputStream( "HTMLComponentTest.testComponent1", ".html" ); //$NON-NLS-1$//$NON-NLS-2$
      component.validate( session, null );
      String content = component.getContent( "text/html" ); //$NON-NLS-1$
      outputStream.write( content.getBytes() );
    } catch ( IOException e ) {
      e.printStackTrace();
    }

    finishTest();
  }

  public void testComponent2() {
    startTest();
    info( Messages.getInstance().getString( "HTMLComponentTest.USER_ERRORS_EXPECTED_CONTENT_TYPE_INVALID" ) ); //$NON-NLS-1$
    // this should fail because the requested content type is not supported
    String url = "http://www.pentaho.org/demo/news.html"; //$NON-NLS-1$
    SimpleUrlFactory urlFactory = new SimpleUrlFactory( "/testurl?" ); //$NON-NLS-1$
    ArrayList messages = new ArrayList();
    HtmlComponent component = new HtmlComponent( HtmlComponent.TYPE_URL, url, "", urlFactory, messages ); //$NON-NLS-1$
    component.setLoggingLevel( getLoggingLevel() );
    OutputStream outputStream = getOutputStream( "HTMLComponentTest.testComponent2", ".html" ); //$NON-NLS-1$//$NON-NLS-2$
    String contentType = "text/xml"; //$NON-NLS-1$

    SimpleParameterProvider requestParameters = new SimpleParameterProvider();
    SimpleParameterProvider sessionParameters = new SimpleParameterProvider();

    HashMap parameterProviders = new HashMap();
    parameterProviders.put( IParameterProvider.SCOPE_REQUEST, requestParameters );
    parameterProviders.put( IParameterProvider.SCOPE_SESSION, sessionParameters );
    StandaloneSession session =
        new StandaloneSession( Messages.getInstance().getString( "BaseTest.DEBUG_JUNIT_SESSION" ) ); //$NON-NLS-1$

    SimpleOutputHandler outputHandler = new SimpleOutputHandler( outputStream, false );
    BaseRequestHandler requestHandler = new BaseRequestHandler( session, null, outputHandler, null, urlFactory );

    try {
      component.validate( session, requestHandler );
      component.handleRequest( outputStream, requestHandler, contentType, parameterProviders );
    } catch ( IOException e ) {
      e.printStackTrace();
    }

    finishTest();
  }

  public void testComponent3() {
    startTest();
    info( Messages.getInstance().getString( "HTMLComponentTest.USER_ERRORS_EXPECTED_URL_INVALID" ) ); //$NON-NLS-1$
    // this should fail because the url is bad
    String url = "xttp://a"; //$NON-NLS-1$
    SimpleUrlFactory urlFactory = new SimpleUrlFactory( "/testurl?" ); //$NON-NLS-1$
    ArrayList messages = new ArrayList();
    HtmlComponent component =
        new HtmlComponent( HtmlComponent.TYPE_URL, url, Messages.getInstance().getString(
          "HTML.ERROR_0001_NOT_AVAILABLE" ), urlFactory, messages ); //$NON-NLS-1$
    component.setLoggingLevel( getLoggingLevel() );
    OutputStream outputStream = getOutputStream( "HTMLComponentTest.testComponent3", ".html" ); //$NON-NLS-1$//$NON-NLS-2$
    String contentType = "text/html"; //$NON-NLS-1$

    SimpleParameterProvider requestParameters = new SimpleParameterProvider();
    SimpleParameterProvider sessionParameters = new SimpleParameterProvider();

    HashMap parameterProviders = new HashMap();
    parameterProviders.put( IParameterProvider.SCOPE_REQUEST, requestParameters );
    parameterProviders.put( IParameterProvider.SCOPE_SESSION, sessionParameters );
    StandaloneSession session =
        new StandaloneSession( Messages.getInstance().getString( "BaseTest.DEBUG_JUNIT_SESSION" ) ); //$NON-NLS-1$

    SimpleOutputHandler outputHandler = new SimpleOutputHandler( outputStream, false );
    BaseRequestHandler requestHandler = new BaseRequestHandler( session, null, outputHandler, null, urlFactory );

    try {
      component.validate( session, requestHandler );
      component.handleRequest( outputStream, requestHandler, contentType, parameterProviders );
    } catch ( IOException e ) {
      e.printStackTrace();
    }

    finishTest();
  }

  public static void main( String[] args ) {
    HTMLComponentTest test = new HTMLComponentTest();
    test.setUp();
    try {
      test.testComponent1();
      // test.testComponent2();
      // test.testComponent3();
    } finally {
      test.tearDown();
      BaseTest.shutdown();
    }
  }

}
