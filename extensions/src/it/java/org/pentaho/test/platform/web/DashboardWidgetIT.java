/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.test.platform.web;

import org.pentaho.platform.engine.core.output.SimpleOutputHandler;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.services.BaseRequestHandler;
import org.pentaho.platform.uifoundation.chart.DashboardWidgetComponent;
import org.pentaho.platform.util.web.SimpleUrlFactory;
import org.pentaho.platform.web.http.request.HttpRequestParameterProvider;
import org.pentaho.platform.web.http.session.HttpSessionParameterProvider;
import org.pentaho.test.platform.engine.core.BaseTest;
import org.pentaho.test.platform.utils.TestResourceLocation;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;

@SuppressWarnings( "nls" )
public class DashboardWidgetIT extends BaseTest {

  private static final String SOLUTION_PATH = TestResourceLocation.TEST_RESOURCES + "/web-solution";

  public String getSolutionPath() {
    return SOLUTION_PATH;
  }

  public void testWidget2() {

    SimpleUrlFactory urlFactory = new SimpleUrlFactory( "" ); //$NON-NLS-1$
    ArrayList messages = new ArrayList();

    DashboardWidgetComponent widget =
        new DashboardWidgetComponent( DashboardWidgetComponent.TYPE_DIAL, getSolutionPath()
            + "/samples/charts/dashboardwidget1.dial.xml", 200, 200, urlFactory, messages ); //$NON-NLS-1$

    widget.setValue( 49 );
    widget.setTitle( "test widget 1" ); //$NON-NLS-1$
    widget.setUnits( "" ); //$NON-NLS-1$

    StandaloneSession session = new StandaloneSession( "BaseTest.DEBUG_JUNIT_SESSION" ); //$NON-NLS-1$
    widget.validate( session, null );

    SimpleParameterProvider requestParameters = new SimpleParameterProvider();
    SimpleParameterProvider sessionParameters = new SimpleParameterProvider();
    widget.setParameterProvider( HttpRequestParameterProvider.SCOPE_REQUEST, requestParameters );
    widget.setParameterProvider( HttpSessionParameterProvider.SCOPE_SESSION, sessionParameters );

    String content = widget.getContent( "text/html" ); //$NON-NLS-1$
    OutputStream outputStream = getOutputStream( "DashboardWidgetTest.testWidget1", ".html" ); //$NON-NLS-1$//$NON-NLS-2$
    try {
      outputStream.write( content.getBytes() );
    } catch ( Exception e ) {
      // content check will test this
    }
  }

  public void testWidget1() {
    startTest();

    SimpleUrlFactory urlFactory = new SimpleUrlFactory( "/testurl?" ); //$NON-NLS-1$
    ArrayList messages = new ArrayList();

    DashboardWidgetComponent widget =
        new DashboardWidgetComponent( DashboardWidgetComponent.TYPE_DIAL, getSolutionPath()
            + "/samples/charts/dashboardwidget1.dial.xml", 300, 300, urlFactory, messages ); //$NON-NLS-1$

    widget.setLoggingLevel( getLoggingLevel() );
    widget.setValue( 72.5 );
    widget.setTitle( "test widget 1" ); //$NON-NLS-1$
    widget.setUnits( "$" ); //$NON-NLS-1$

    OutputStream outputStream = getOutputStream( "DashboardWidgetTest.testWidget1", ".html" ); //$NON-NLS-1$//$NON-NLS-2$
    String contentType = "text/html"; //$NON-NLS-1$

    SimpleParameterProvider requestParameters = new SimpleParameterProvider();
    SimpleParameterProvider sessionParameters = new SimpleParameterProvider();

    HashMap parameterProviders = new HashMap();
    parameterProviders.put( HttpRequestParameterProvider.SCOPE_REQUEST, requestParameters );
    parameterProviders.put( HttpSessionParameterProvider.SCOPE_SESSION, sessionParameters );
    StandaloneSession session = new StandaloneSession( "BaseTest.DEBUG_JUNIT_SESSION" ); //$NON-NLS-1$

    SimpleOutputHandler outputHandler = new SimpleOutputHandler( outputStream, false );
    BaseRequestHandler requestHandler = new BaseRequestHandler( session, null, outputHandler, null, urlFactory );

    try {
      widget.validate( session, requestHandler );
      widget.handleRequest( outputStream, requestHandler, contentType, parameterProviders );
    } catch ( IOException e ) {
      e.printStackTrace();
    }
    finishTest();

  }

  public static void main( String[] args ) {
    DashboardWidgetIT test = new DashboardWidgetIT();
    test.setUp();
    try {
      test.testWidget1();
      test.testWidget2();
    } finally {
      test.tearDown();
      BaseTest.shutdown();
    }
  }

}
