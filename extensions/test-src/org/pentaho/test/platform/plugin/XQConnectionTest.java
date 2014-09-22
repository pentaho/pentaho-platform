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

import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.engine.core.output.SimpleOutputHandler;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.test.platform.engine.core.BaseTest;

import java.io.File;
import java.io.OutputStream;
import java.util.Map;

//import org.pentaho.test.platform.engine.core.BaseTestCase;

@SuppressWarnings( "nls" )
public class XQConnectionTest extends BaseTest {
  private static final String SOLUTION_PATH = "test-src/solution";
  private static final String ALT_SOLUTION_PATH = "test-src/solution";

  // private static final String PENTAHO_XML_PATH = "/system/pentaho.xml";

  public String getSolutionPath() {
    File file = new File( SOLUTION_PATH );
    if ( file.exists() ) {
      return SOLUTION_PATH;
    } else {
      return ALT_SOLUTION_PATH;
    }

  }

  public Map getRequiredListeners() {
    Map listeners = super.getRequiredListeners();
    listeners.put( "jfree-report", "jfree-report" ); //$NON-NLS-1$ //$NON-NLS-2$
    return listeners;
  }

  public void testXQConnection() {
    SimpleParameterProvider parameterProvider = new SimpleParameterProvider();
    StandaloneSession session =
        new StandaloneSession( Messages.getInstance().getString( "BaseTest.DEBUG_JUNIT_SESSION" ) ); //$NON-NLS-1$
    OutputStream outputStream = getOutputStream( "DatasourceTest.XQ_Datasource", ".html" ); //$NON-NLS-1$ //$NON-NLS-2$
    SimpleOutputHandler outputHandler = new SimpleOutputHandler( outputStream, true );
    IRuntimeContext context =
        run( "/test/datasource/XQ_Datasource.xaction", null, false, parameterProvider, outputHandler, session ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    assertEquals(
        Messages.getInstance().getString( "BaseTest.USER_RUNNING_ACTION_SEQUENCE" ), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus() ); //$NON-NLS-1$
    // TODO need some validation of success
  }

  /*
   * public void testXQConnection_JFreeXQuery() { SimpleParameterProvider parameterProvider = new
   * SimpleParameterProvider(); parameterProvider.setParameter("type", "html"); //$NON-NLS-1$ //$NON-NLS-2$
   * StandaloneSession session = new
   * StandaloneSession(Messages.getInstance().getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$ OutputStream
   * outputStream = getOutputStream("DatasourceTest.XQ_Datasource", ".html"); //$NON-NLS-1$ //$NON-NLS-2$
   * SimpleOutputHandler outputHandler = new SimpleOutputHandler(outputStream, true); IRuntimeContext context =
   * run("test", "reporting", "JFree_XQuery_report.xaction", null, false, parameterProvider, outputHandler, session);
   * //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ assertEquals(
   * Messages.getInstance().getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS,
   * context.getStatus()); //$NON-NLS-1$ // TODO need some validation of success }
   */

  public void testJFreeReportParameterPage2() {
    startTest();
    SimpleParameterProvider parameterProvider = new SimpleParameterProvider();
    parameterProvider.setParameter( "type", "html" ); //$NON-NLS-1$ //$NON-NLS-2$
    OutputStream outputStream = getOutputStream( "ReportingTest.testJFreeReportParameterPage2", ".html" ); //$NON-NLS-1$ //$NON-NLS-2$
    SimpleOutputHandler outputHandler = new SimpleOutputHandler( outputStream, true );
    StandaloneSession session =
        new StandaloneSession( Messages.getInstance().getString( "BaseTest.DEBUG_JUNIT_SESSION" ) ); //$NON-NLS-1$
    IRuntimeContext context =
        run(
            "/test/reporting/jfreereport-reports-test-param2.xaction", null, false, parameterProvider, outputHandler, session ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals(
        Messages.getInstance().getString( "BaseTest.USER_RUNNING_ACTION_SEQUENCE" ), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus() ); //$NON-NLS-1$
    // TODO need some validation of success
    finishTest();
  }

  public static void main( String[] args ) {
    XQConnectionTest test = new XQConnectionTest();
    try {
      test.testXQConnection();
      // test.testXQConnection_JFreeXQuery();
      test.testJFreeReportParameterPage2();
    } finally {
      BaseTest.shutdown();
    }
  }
}
