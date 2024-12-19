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

import org.pentaho.platform.api.engine.IActionParameter;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.plugin.action.builtin.SubActionComponent;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.test.platform.engine.core.BaseTest;
import org.pentaho.test.platform.utils.TestResourceLocation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

//import org.pentaho.commons.connection.IPentahoConnection;
//import org.pentaho.commons.connection.IPentahoResultSet;
//import org.pentaho.platform.api.data.IPreparedComponent;

@SuppressWarnings ( "nls" )
public class SubActionComponentIT extends BaseTest {

  private static final String CO_TEST_NAME = "MultipleComponentTest_ContentOutput_"; //$NON-NLS-1$

  private static final String CO_TEST_EXTN = ".txt"; //$NON-NLS-1$

  private ByteArrayOutputStream lastStream;

  private static final String SOLUTION_NAME = "test"; //$NON-NLS-1$

  private static final String TEST_XACTION = "SubActionTest.xaction"; //$NON-NLS-1$

  private static final String SUBACTION = "SubActionTestTarget.xaction"; //$NON-NLS-1$

  private static final String SOLUTION_NAME_PARAM = "solution"; //$NON-NLS-1$

  private static final String SOLUTION_PATH_PARAM = "path"; //$NON-NLS-1$

  private static final String SUBACTION_PARAM = "subaction"; //$NON-NLS-1$

  private static final String TEST_STRING = "Hello World"; //$NON-NLS-1$

  private static final String TEST_OUTPUT_PARAM = "outputString"; //$NON-NLS-1$

  private static final String TEST_INPUT_PARAM = "inputString"; //$NON-NLS-1$

  private static final String SOLUTION_PATH = TestResourceLocation.TEST_RESOURCES + "/solution";

  public String getSolutionPath() {
    return SOLUTION_PATH;
  }

  public void testSuccessPaths() {
    startTest();
    String testName = CO_TEST_NAME + "string_" + System.currentTimeMillis(); //$NON-NLS-1$
    SimpleParameterProvider parameterProvider = new SimpleParameterProvider();
    parameterProvider.setParameter( SOLUTION_NAME_PARAM, SOLUTION_NAME );
    parameterProvider.setParameter( SOLUTION_PATH_PARAM, "platform" );
    parameterProvider.setParameter( SUBACTION_PARAM, SUBACTION );
    parameterProvider.setParameter( TEST_INPUT_PARAM, TEST_STRING );

    IRuntimeContext context =
        run( "/" + SOLUTION_NAME + "/platform/" + TEST_XACTION, parameterProvider, testName, CO_TEST_EXTN );
    assertEquals(
        Messages.getInstance().getString( "BaseTest.USER_RUNNING_ACTION_SEQUENCE" ), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus() ); //$NON-NLS-1$

    IActionParameter rtn = context.getOutputParameter( TEST_OUTPUT_PARAM );
    Object value = rtn.getValue();
    assertEquals( TEST_STRING, value );

    finishTest();
  }

  public void testJcrPaths() {
    startTest();

    SubActionComponent c = new SubActionComponent();

    final String PATH = "/bi-developers/reporting/myaction.xaction";

    String value = c.buildActionPath( "", "bi-developers/reporting/myaction.xaction", "" );
    assertEquals( PATH, value );

    value = c.buildActionPath( null, "bi-developers/reporting/myaction.xaction", "" );
    assertEquals( PATH, value );

    value = c.buildActionPath( "", "bi-developers/reporting/myaction.xaction", null );
    assertEquals( PATH, value );

    value = c.buildActionPath( null, "bi-developers/reporting/myaction.xaction", null );
    assertEquals( PATH, value );

    value = c.buildActionPath( null, "/bi-developers/reporting/myaction.xaction", null );
    assertEquals( PATH, value );

    value = c.buildActionPath( "bi-developers", "/reporting/myaction.xaction", null );
    assertEquals( PATH, value );

    value = c.buildActionPath( "bi-developers", "reporting", "myaction.xaction" );
    assertEquals( PATH, value );

    value = c.buildActionPath( "/bi-developers", "/reporting", "/myaction.xaction" );
    assertEquals( PATH, value );

    value = c.buildActionPath( "bi-developers", "/reporting", "/myaction.xaction" );
    assertEquals( PATH, value );

    value = c.buildActionPath( "bi-developers", "reporting", "/myaction.xaction" );
    assertEquals( PATH, value );

    value = c.buildActionPath( "", "bi-developers/reporting", "myaction.xaction" );
    assertEquals( PATH, value );

    value = c.buildActionPath( "\\bi-developers", "reporting", "myaction.xaction" );
    assertEquals( PATH, value );

    value = c.buildActionPath( "\\\\//bi-developers", "\\reporting", "\\myaction.xaction" );
    System.out.println( value );
    assertEquals( PATH, value );

    finishTest();
  }

  /**
   * parent action creates a connection, used by sub-action parent then re-uses connection
   */
  /*
   * public void testParentConnectionSharing() {
   * 
   * startTest(); info("Expected: Successful execution with object available"); //$NON-NLS-1$ IRuntimeContext context =
   * run("test", "platform", "SubActionConnectionTest4.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
   * assertEquals( Messages.getInstance().getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"),
   * IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus()); //$NON-NLS-1$
   * 
   * IActionParameter rtn = context.getOutputParameter("query-results"); //$NON-NLS-1$ assertNotNull(rtn);
   * IPentahoResultSet resultSet = (IPentahoResultSet) rtn.getValue(); assertNotNull(resultSet);
   * 
   * try { assertEquals(5, resultSet.getRowCount()); } catch (Exception e) { e.printStackTrace(); fail(); }
   * 
   * finishTest(); }
   */

  /**
   * sub-action creates connection, passes result set on to parent action
   *
   */
  /*
   * public void testChildResultSetSharing() {
   * 
   * startTest(); info("Expected: Successful execution with object available"); //$NON-NLS-1$ IRuntimeContext context =
   * run("test", "platform", "SubActionConnectionTest3.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
   * assertEquals( Messages.getInstance().getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"),
   * IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus()); //$NON-NLS-1$
   * 
   * IActionParameter rtn = context.getOutputParameter("query-results"); //$NON-NLS-1$ assertNotNull(rtn);
   * IPentahoResultSet resultSet = (IPentahoResultSet) rtn.getValue(); assertNotNull(resultSet);
   * 
   * try { // if row count is zero, that means the native connection has been closed. assertEquals(5,
   * resultSet.getRowCount()); } catch (Exception e) { e.printStackTrace(); fail(); }
   * 
   * finishTest(); }
   */

  /**
   * sub-action creates result set, uses, and passes nothing to parent action
   */
  public void testChildConnectionNotSharing() {
    startTest();
    info( "Expected: Failed execution with object not available" ); //$NON-NLS-1$
    IRuntimeContext context = run( "/test/platform/SubActionConnectionTest2.xaction" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    // xaction should fail, because connection isn't available
    assertEquals(
        Messages.getInstance().getString( "BaseTest.USER_RUNNING_ACTION_SEQUENCE" ), IRuntimeContext.RUNTIME_STATUS_FAILURE, context.getStatus() ); //$NON-NLS-1$

    finishTest();
  }

  /**
   * sub-action creates connection, passes connection on to the parent action
   */
  /*
   * public void testChildConnectionSharing() { startTest();
   * info("Expected: Successful execution with object available"); //$NON-NLS-1$ IRuntimeContext context = run("test",
   * "platform", "SubActionConnectionTest1.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ assertEquals(
   * Messages.getInstance().getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS,
   * context.getStatus()); //$NON-NLS-1$
   * 
   * IActionParameter rtn = context.getOutputParameter("prepared_component"); //$NON-NLS-1$ assertNotNull(rtn);
   * IPreparedComponent preparedComponent = (IPreparedComponent) rtn.getValue();
   * 
   * // connection in this case should be available IPentahoConnection conn = preparedComponent.shareConnection();
   * assertNotNull(conn); try { IPentahoResultSet results = conn
   * .executeQuery("SELECT BUDGET FROM QUADRANT_ACTUALS WHERE REGION='Western' AND DEPARTMENT='Sales'"); //$NON-NLS-1$
   * assertEquals(5, results.getRowCount()); } catch (Exception e) { e.printStackTrace(); fail(); }
   * 
   * finishTest(); }
   */
  protected OutputStream getOutputStream( String testName, String extension ) {
    if ( testName.indexOf( "BAD_OUTPUTSTREAM_" ) > 0 ) { //$NON-NLS-1$ 
      ByteArrayOutputStream exceptionStream = new ByteArrayOutputStream() {
        public static final String ERROR_MSG = "Cannot write to this stream."; //$NON-NLS-1$

        public synchronized void write( int b ) {
          throw new RuntimeException( ERROR_MSG );
        }

        public synchronized void write( byte[] b, int off, int len ) {
          throw new RuntimeException( ERROR_MSG );
        }
      };
      return exceptionStream;

    } else {
      lastStream = new ByteArrayOutputStream();
      return lastStream;
    }
  }

  protected InputStream getInputStreamFromOutput( String testName, String extension ) {
    return new ByteArrayInputStream( lastStream.toByteArray() );
  }

  public static void main( String[] args ) {
    SubActionComponentIT test = new SubActionComponentIT();
    try {
      test.setUp();
      test.testSuccessPaths();
    } finally {
      test.tearDown();
      BaseTest.shutdown();
    }
  }
}
