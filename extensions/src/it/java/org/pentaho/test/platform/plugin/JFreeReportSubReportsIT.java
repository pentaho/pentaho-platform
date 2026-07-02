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


package org.pentaho.test.platform.plugin;

//import java.io.OutputStream;

import org.pentaho.test.platform.engine.core.BaseTest;
import org.pentaho.test.platform.utils.TestResourceLocation;

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
public class JFreeReportSubReportsIT extends BaseTest {
  private static final String SOLUTION_PATH = TestResourceLocation.TEST_RESOURCES + "/solution";

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
    JFreeReportSubReportsIT test = new JFreeReportSubReportsIT();

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
