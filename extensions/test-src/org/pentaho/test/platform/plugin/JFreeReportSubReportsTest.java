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

//import java.io.OutputStream;

import org.pentaho.test.platform.engine.core.BaseTest;

import java.util.Map;

//import org.pentaho.platform.api.engine.IRuntimeContext;
//import org.pentaho.platform.engine.core.output.SimpleOutputHandler;
//import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
//import org.pentaho.platform.engine.core.system.StandaloneSession;
//import org.pentaho.platform.plugin.services.messages.Messages;

/**
 * This class tests the BI Platform's subreport support in JFreeReport
 * 
 * @author Will Gorman
 * 
 */
@SuppressWarnings( "nls" )
public class JFreeReportSubReportsTest extends BaseTest {
  private static final String SOLUTION_PATH = "test-src/solution";

  public String getSolutionPath() {
    return SOLUTION_PATH;
  }

  public Map getRequiredListeners() {
    Map listeners = super.getRequiredListeners();
    listeners.put( "jfree-report", "jfree-report" ); //$NON-NLS-1$ //$NON-NLS-2$
    return listeners;
  }

  /*
   * public void testJFreeReportSubReport() { startTest(); SimpleParameterProvider parameterProvider = new
   * SimpleParameterProvider(); parameterProvider.setParameter("type", "html"); //$NON-NLS-1$ //$NON-NLS-2$ OutputStream
   * outputStream = getOutputStream("SubreportingTest.JFreeReportSubreport", ".html"); //$NON-NLS-1$ //$NON-NLS-2$
   * SimpleOutputHandler outputHandler = new SimpleOutputHandler(outputStream, true); StandaloneSession session = new
   * StandaloneSession(Messages.getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$ IRuntimeContext context = run(
   * "samples", "reporting", "subreport-example.xaction", null, false, parameterProvider, outputHandler, session);
   * //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ assertEquals(
   * Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS,
   * context.getStatus()); //$NON-NLS-1$ // TODO need some validation of success finishTest(); }
   */

  /*
   * public void testJFreeReportSubReportBasicHTML() { startTest(); SimpleParameterProvider parameterProvider = new
   * SimpleParameterProvider(); parameterProvider.setParameter("type", "html"); //$NON-NLS-1$ //$NON-NLS-2$ OutputStream
   * outputStream = getOutputStream("SubreportingTest.JFreeReportSubreportBasicHTML", ".html"); //$NON-NLS-1$
   * //$NON-NLS-2$ SimpleOutputHandler outputHandler = new SimpleOutputHandler(outputStream, true); StandaloneSession
   * session = new StandaloneSession(Messages.getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$ IRuntimeContext
   * context = run( "test", "reporting", "jfreereport-subreport-basic-test.xaction", null, false, parameterProvider,
   * outputHandler, session); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ assertEquals(
   * Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS,
   * context.getStatus()); //$NON-NLS-1$ // TODO need some validation of success finishTest(); }
   */

  /*
   * public void testJFreeReportSubReportBasicPDF() { startTest(); SimpleParameterProvider parameterProvider = new
   * SimpleParameterProvider(); parameterProvider.setParameter("type", "pdf"); //$NON-NLS-1$ //$NON-NLS-2$ OutputStream
   * outputStream = getOutputStream("SubreportingTest.JFreeReportSubreportBasicPDF", ".pdf"); //$NON-NLS-1$
   * //$NON-NLS-2$ SimpleOutputHandler outputHandler = new SimpleOutputHandler(outputStream, true); StandaloneSession
   * session = new StandaloneSession(Messages.getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$ IRuntimeContext
   * context = run( "test", "reporting", "jfreereport-subreport-basic-test.xaction", null, false, parameterProvider,
   * outputHandler, session); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ assertEquals(
   * Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS,
   * context.getStatus()); //$NON-NLS-1$ // TODO need some validation of success finishTest(); }
   */

  /*
   * public void testJFreeReportSubReportBasicXLS() { startTest(); SimpleParameterProvider parameterProvider = new
   * SimpleParameterProvider(); parameterProvider.setParameter("type", "xls"); //$NON-NLS-1$ //$NON-NLS-2$ OutputStream
   * outputStream = getOutputStream("SubreportingTest.JFreeReportSubreportBasicXLS", ".xls"); //$NON-NLS-1$
   * //$NON-NLS-2$ SimpleOutputHandler outputHandler = new SimpleOutputHandler(outputStream, true); StandaloneSession
   * session = new StandaloneSession(Messages.getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$ IRuntimeContext
   * context = run( "test", "reporting", "jfreereport-subreport-basic-test.xaction", null, false, parameterProvider,
   * outputHandler, session); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ assertEquals(
   * Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS,
   * context.getStatus()); //$NON-NLS-1$ // TODO need some validation of success finishTest(); }
   */

  /*
   * public void testJFreeReportSubReportIPreparedComponentHTML() { startTest(); SimpleParameterProvider
   * parameterProvider = new SimpleParameterProvider(); parameterProvider.setParameter("type", "html"); //$NON-NLS-1$
   * //$NON-NLS-2$ OutputStream outputStream =
   * getOutputStream("SubreportingTest.JFreeReportSubreportIPreparedComponentHTML", ".html"); //$NON-NLS-1$
   * //$NON-NLS-2$ SimpleOutputHandler outputHandler = new SimpleOutputHandler(outputStream, true); StandaloneSession
   * session = new StandaloneSession(Messages.getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$ IRuntimeContext
   * context = run( "test", "reporting", "jfreereport-subreport-ipreparedcomponent-test.xaction", null, false,
   * parameterProvider, outputHandler, session); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ assertEquals(
   * Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS,
   * context.getStatus()); //$NON-NLS-1$ // TODO need some validation of success finishTest(); }
   */

  /*
   * public void testJFreeReportSubReportIPreparedComponentPDF() { startTest(); SimpleParameterProvider
   * parameterProvider = new SimpleParameterProvider(); parameterProvider.setParameter("type", "pdf"); //$NON-NLS-1$
   * //$NON-NLS-2$ OutputStream outputStream =
   * getOutputStream("SubreportingTest.JFreeReportSubreportIPreparedComponentPDF", ".pdf"); //$NON-NLS-1$ //$NON-NLS-2$
   * SimpleOutputHandler outputHandler = new SimpleOutputHandler(outputStream, true); StandaloneSession session = new
   * StandaloneSession(Messages.getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$ IRuntimeContext context = run(
   * "test", "reporting", "jfreereport-subreport-ipreparedcomponent-test.xaction", null, false, parameterProvider,
   * outputHandler, session); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ assertEquals(
   * Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS,
   * context.getStatus()); //$NON-NLS-1$ // TODO need some validation of success finishTest(); }
   */

  /*
   * public void testJFreeReportSubReportIPreparedComponentXLS() { startTest(); SimpleParameterProvider
   * parameterProvider = new SimpleParameterProvider(); parameterProvider.setParameter("type", "xls"); //$NON-NLS-1$
   * //$NON-NLS-2$ OutputStream outputStream =
   * getOutputStream("SubreportingTest.JFreeReportSubreportIPreparedComponentXLS", ".xls"); //$NON-NLS-1$ //$NON-NLS-2$
   * SimpleOutputHandler outputHandler = new SimpleOutputHandler(outputStream, true); StandaloneSession session = new
   * StandaloneSession(Messages.getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$ IRuntimeContext context = run(
   * "test", "reporting", "jfreereport-subreport-ipreparedcomponent-test.xaction", null, false, parameterProvider,
   * outputHandler, session); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ assertEquals(
   * Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS,
   * context.getStatus()); //$NON-NLS-1$ // TODO need some validation of success finishTest(); }
   */

  public void testDummyTest() {
  }

  public static void main( String[] args ) {
    JFreeReportSubReportsTest test = new JFreeReportSubReportsTest();

    try {
      test.setUp();
      // test.testJFreeReportSubReportBasicHTML();
      // test.testJFreeReportSubReportBasicPDF();
      // test.testJFreeReportSubReportBasicXLS();
      // test.testJFreeReportSubReportIPreparedComponentHTML();
      // test.testJFreeReportSubReportIPreparedComponentPDF();
      // test.testJFreeReportSubReportIPreparedComponentXLS();

    } finally {
      test.tearDown();
      BaseTest.shutdown();
    }
  }
}
