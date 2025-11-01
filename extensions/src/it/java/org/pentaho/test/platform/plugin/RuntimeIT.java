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

import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.engine.core.output.SimpleOutputHandler;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.test.platform.engine.core.BaseTest;
import org.pentaho.test.platform.utils.TestResourceLocation;

import java.io.OutputStream;

@SuppressWarnings( "nls" )
public class RuntimeIT extends BaseTest {

  private static final String SOLUTION_PATH = TestResourceLocation.TEST_RESOURCES + "/solution";

  public String getSolutionPath() {
    return SOLUTION_PATH;
  }

  public void testHelloWorld() {
    startTest();
    SimpleParameterProvider parameterProvider = new SimpleParameterProvider();
    parameterProvider.setParameter( "type", "html" ); //$NON-NLS-1$ //$NON-NLS-2$
    OutputStream outputStream = getOutputStream( "RuntimeTest.testHelloWorld", ".html" ); //$NON-NLS-1$ //$NON-NLS-2$
    SimpleOutputHandler outputHandler = new SimpleOutputHandler( outputStream, true );
    StandaloneSession session =
        new StandaloneSession( Messages.getInstance().getString( "BaseTest.DEBUG_JUNIT_SESSION" ) ); //$NON-NLS-1$

    IRuntimeContext context =
        run( "/test/platform/HelloWorld.xaction", null, false, parameterProvider, outputHandler, session ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals(
        Messages.getInstance().getString( "BaseTest.USER_RUNNING_ACTION_SEQUENCE" ), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus() ); //$NON-NLS-1$

    finishTest();
  }

  public void testForcePrompt() {
    startTest();
    SimpleParameterProvider parameterProvider = new SimpleParameterProvider();
    OutputStream outputStream = getOutputStream( "RuntimeTest.testForcePrompt", ".html" ); //$NON-NLS-1$ //$NON-NLS-2$
    SimpleOutputHandler outputHandler = new SimpleOutputHandler( outputStream, true );
    outputHandler.setOutputPreference( IOutputHandler.OUTPUT_TYPE_PARAMETERS );
    StandaloneSession session =
        new StandaloneSession( Messages.getInstance().getString( "BaseTest.DEBUG_JUNIT_SESSION" ) ); //$NON-NLS-1$

    ISolutionEngine solutionEngine = PentahoSystem.get( ISolutionEngine.class, session );
    solutionEngine.setLoggingLevel( getLoggingLevel() );
    solutionEngine.init( session );
    solutionEngine.setForcePrompt( true );
    IRuntimeContext context =
        run( solutionEngine,
            "/test/reporting/jfreereport-reports-test-param.xaction", null, false, parameterProvider, outputHandler ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals(
        Messages.getInstance().getString( "BaseTest.USER_RUNNING_ACTION_SEQUENCE" ), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus() ); //$NON-NLS-1$

    finishTest();
  }

  public void testMultiComponent() {
    startTest();
    IRuntimeContext context = run( "/test/platform/MultiComponentTest.xaction" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals(
        Messages.getInstance().getString( "BaseTest.USER_RUNNING_ACTION_SEQUENCE" ), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus() ); //$NON-NLS-1$
    finishTest();
  }

  /*
   * public void testMultiComponentLoop() { startTest(); IRuntimeContext context = run("test", "platform",
   * "MultiComponentTestLoop.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ assertEquals(
   * Messages.getInstance().getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS,
   * context.getStatus()); //$NON-NLS-1$ finishTest(); }
   */

  public void testLooping() {
    startTest();
    IRuntimeContext context = run( "/test/platform/LoopingTest.xaction" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals(
        Messages.getInstance().getString( "BaseTest.USER_RUNNING_ACTION_SEQUENCE" ), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus() ); //$NON-NLS-1$
    finishTest();
  }

  /*
   * public void testBursting() { startTest(); IRuntimeContext context = run("test", "platform",
   * "BurstingTest.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ assertEquals(
   * Messages.getInstance().getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS,
   * context.getStatus()); //$NON-NLS-1$ finishTest(); }
   * 
   * public void testHTMLBursting() { startTest(); IRuntimeContext context = run("test", "platform",
   * "BurstingHTMLTest.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ assertEquals(
   * Messages.getInstance().getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS,
   * context.getStatus()); //$NON-NLS-1$ finishTest(); }
   */

  public static void main( String[] args ) {
    RuntimeIT test = new RuntimeIT();
    test.setUp();
    try {
      test.testHelloWorld();
      test.testMultiComponent();
      // test.testMultiComponentLoop();
      test.testLooping();
      // test.testBursting();
      // test.testHTMLBursting();
      test.testForcePrompt();
    } finally {
      test.tearDown();
      BaseTest.shutdown();
    }
  }

}
