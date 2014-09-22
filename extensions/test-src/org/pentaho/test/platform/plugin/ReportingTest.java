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

import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoRequestContext;
import org.pentaho.platform.api.engine.IPentahoUrlFactory;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.PentahoRequestContextHolder;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.platform.uifoundation.component.ActionComponent;
import org.pentaho.platform.util.web.SimpleUrlFactory;
import org.pentaho.test.platform.engine.core.BaseTest;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Map;

@SuppressWarnings( "nls" )
public class ReportingTest extends BaseTest {
  private static final String SOLUTION_PATH = "test-src/solution";

  public String getSolutionPath() {
    return SOLUTION_PATH;
  }

  public Map getRequiredListeners() {
    Map listeners = super.getRequiredListeners();
    listeners.put( "birt", "birt" ); //$NON-NLS-1$ //$NON-NLS-2$
    return listeners;
  }

  /*
   * public void testBIRTReport1() { startTest(); SimpleParameterProvider parameterProvider = new
   * SimpleParameterProvider(); parameterProvider.setParameter("type", "html"); //$NON-NLS-1$ //$NON-NLS-2$ OutputStream
   * outputStream = getOutputStream("ReportingTest.testBIRTReport1-a", ".html"); //$NON-NLS-1$ //$NON-NLS-2$
   * SimpleOutputHandler outputHandler = new SimpleOutputHandler(outputStream, true); StandaloneSession session = new
   * StandaloneSession(Messages.getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$ IRuntimeContext context = run(
   * "test", "reporting", "quadrant-budget-hsql.xaction", null, false, parameterProvider, outputHandler, session);
   * //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ assertEquals(
   * Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS,
   * context.getStatus()); //$NON-NLS-1$ outputStream = getOutputStream("ReportingTest.testBIRTReport1-b", ".pdf");
   * //$NON-NLS-1$ //$NON-NLS-2$ outputHandler = new SimpleOutputHandler(outputStream, true);
   * parameterProvider.setParameter("type", "pdf"); //$NON-NLS-1$ //$NON-NLS-2$ context = run( "test", "reporting",
   * "quadrant-budget-hsql.xaction", null, false, parameterProvider, outputHandler, session); //$NON-NLS-1$
   * //$NON-NLS-2$ //$NON-NLS-3$ assertEquals( Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"),
   * IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus()); //$NON-NLS-1$ finishTest(); }
   * 
   * public void testBIRTReport4() { startTest(); SimpleParameterProvider parameterProvider = new
   * SimpleParameterProvider(); parameterProvider.setParameter("type", "html"); //$NON-NLS-1$ //$NON-NLS-2$
   * parameterProvider.setParameter("REGION", "Eastern"); //$NON-NLS-1$ //$NON-NLS-2$
   * parameterProvider.setParameter("DEPARTMENT", "Finance"); //$NON-NLS-1$ //$NON-NLS-2$ OutputStream outputStream =
   * getOutputStream("ReportingTest.testBIRTReport4", ".html"); //$NON-NLS-1$ //$NON-NLS-2$ SimpleOutputHandler
   * outputHandler = new SimpleOutputHandler(outputStream, true); StandaloneSession session = new
   * StandaloneSession(Messages.getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$ IRuntimeContext context = run(
   * "test", "reporting", "quadrant-budget-for-region-and-dept-to-repository.xaction", null, false, parameterProvider,
   * outputHandler, session); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ assertEquals(
   * Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS,
   * context.getStatus()); //$NON-NLS-1$ // TODO need some validation of success finishTest(); }
   * 
   * public void testBIRTIntparm() { startTest(); SimpleParameterProvider parameterProvider = new
   * SimpleParameterProvider(); parameterProvider.setParameter("type", "html"); //$NON-NLS-1$ //$NON-NLS-2$
   * parameterProvider.setParameter("intparm", "300000"); //$NON-NLS-1$ //$NON-NLS-2$ OutputStream outputStream =
   * getOutputStream("ReportingTest.testIntParm", ".html"); //$NON-NLS-1$ //$NON-NLS-2$ SimpleOutputHandler
   * outputHandler = new SimpleOutputHandler(outputStream, true); StandaloneSession session = new
   * StandaloneSession(Messages.getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$ IRuntimeContext context = run(
   * "test", "reporting", "BIRT-quadrant-budget-hsql-intparm.xaction", null, false, parameterProvider, outputHandler,
   * session); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ assertEquals(
   * Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS,
   * context.getStatus()); //$NON-NLS-1$ // TODO need some validation of success finishTest(); }
   */

  /*
   * public void testJasperReports1() { startTest(); SimpleParameterProvider parameterProvider = new
   * SimpleParameterProvider(); parameterProvider.setParameter("REGION", "Eastern"); //$NON-NLS-1$ //$NON-NLS-2$
   * OutputStream outputStream = getOutputStream("ReportingTest.testJasperReports1", ".html"); //$NON-NLS-1$
   * //$NON-NLS-2$ SimpleOutputHandler outputHandler = new SimpleOutputHandler(outputStream, true); StandaloneSession
   * session = new StandaloneSession(Messages.getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$ IRuntimeContext
   * context = run( "test", "reporting", "jasper-reports-test-1.xaction", null, false, parameterProvider, outputHandler,
   * session); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ assertEquals(
   * Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS,
   * context.getStatus()); //$NON-NLS-1$ // TODO need some validation of success finishTest(); }
   * 
   * public void testJasperReports2() { startTest(); SimpleParameterProvider parameterProvider = new
   * SimpleParameterProvider(); parameterProvider.setParameter("REGION", "Eastern"); //$NON-NLS-1$ //$NON-NLS-2$
   * parameterProvider.setParameter("type", "pdf"); //$NON-NLS-1$ //$NON-NLS-2$ OutputStream outputStream =
   * getOutputStream("ReportingTest.testJasperReports2", ".pdf"); //$NON-NLS-1$ //$NON-NLS-2$ SimpleOutputHandler
   * outputHandler = new SimpleOutputHandler(outputStream, true); StandaloneSession session = new
   * StandaloneSession(Messages.getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$ IRuntimeContext context = run(
   * "test", "reporting", "jasper-reports-test-2.xaction", null, false, parameterProvider, outputHandler, session);
   * //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ assertEquals(
   * Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS,
   * context.getStatus()); //$NON-NLS-1$ // TODO need some validation of success finishTest(); }
   */

  /*
   * public void testBIRTReport3() { startTest(); SimpleParameterProvider parameterProvider = new
   * SimpleParameterProvider(); parameterProvider.setParameter("type", "pdf"); //$NON-NLS-1$ //$NON-NLS-2$ OutputStream
   * outputStream = getOutputStream("ReportingTest.testBIRTReport3", ".pdf"); //$NON-NLS-1$ //$NON-NLS-2$
   * SimpleOutputHandler outputHandler = new SimpleOutputHandler(outputStream, true); StandaloneSession session = new
   * StandaloneSession(Messages.getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$ IRuntimeContext context = run(
   * "test", "reporting", "quadrant-budget-for-region-hsql.xaction", null, false, parameterProvider, outputHandler,
   * session); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ assertEquals(
   * Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS,
   * context.getStatus()); //$NON-NLS-1$ finishTest(); }
   */
  public void testActionComponent() {
    startTest();
    IPentahoRequestContext requestContext = PentahoRequestContextHolder.getRequestContext();
    IPentahoUrlFactory urlFactory = new SimpleUrlFactory( requestContext.getContextPath() );
    ArrayList messages = new ArrayList();
    SimpleParameterProvider parameterProvider = new SimpleParameterProvider();
    parameterProvider.setParameter( "solution", "test" ); //$NON-NLS-1$ //$NON-NLS-2$
    parameterProvider.setParameter( "path", "reporting" ); //$NON-NLS-1$ //$NON-NLS-2$
    parameterProvider.setParameter( "action", "custom-parameter-page-example.xaction" ); //$NON-NLS-1$ //$NON-NLS-2$
    ActionComponent component =
        new ActionComponent(
            "test/reporting/custom-parameter-page-example.xaction", null, IOutputHandler.OUTPUT_TYPE_DEFAULT, urlFactory, messages ); //$NON-NLS-1$
    component.setParameterProvider( IParameterProvider.SCOPE_REQUEST, parameterProvider );
    StandaloneSession session =
        new StandaloneSession( Messages.getInstance().getString( "BaseTest.DEBUG_JUNIT_SESSION" ) ); //$NON-NLS-1$
    component.validate( session, null );
    OutputStream outputStream = getOutputStream( "ReportingTest.testActionComponent", ".html" ); //$NON-NLS-1$ //$NON-NLS-2$
    String content = component.getContent( "text/html" ); //$NON-NLS-1$
    try {
      outputStream.write( content.getBytes() );
    } catch ( Exception e ) {
      //ignore
    }
    finishTest();
  }

  public static void main( String[] args ) {
    ReportingTest test = new ReportingTest();
    test.setUp();
    try {
      /*
       * test.testBIRTReport1(); test.testBIRTReport3(); test.testBIRTReport4(); test.testBIRTIntparm();
       */
      // test.testJasperReports1();
      // test.testJasperReports2();
      test.testActionComponent();
    } finally {
      test.tearDown();
      BaseTest.shutdown();
    }
  }
}
