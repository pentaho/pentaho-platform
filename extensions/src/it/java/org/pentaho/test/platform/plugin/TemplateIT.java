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

import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.engine.core.output.SimpleOutputHandler;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.test.platform.engine.core.BaseTest;
import org.pentaho.test.platform.utils.TestResourceLocation;

import java.io.OutputStream;

@SuppressWarnings( "nls" )
public class TemplateIT extends BaseTest {
  private static final String SOLUTION_PATH = TestResourceLocation.TEST_RESOURCES + "/solution";

  public String getSolutionPath() {
    return SOLUTION_PATH;
  }

  public void testTemplate1() {
    startTest();
    SimpleParameterProvider parameterProvider = new SimpleParameterProvider();
    parameterProvider.setParameter( "customer", "Acme" ); //$NON-NLS-1$ //$NON-NLS-2$

    IRuntimeContext context = run( "/test/template/template1.xaction", parameterProvider ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals(
        Messages.getInstance().getString( "BaseTest.USER_RUNNING_ACTION_SEQUENCE" ), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus() ); //$NON-NLS-1$
    assertNotNull( "", context.getOutputParameter( "output" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    assertEquals( "", "Customer 'Acme' is in region West", context.getOutputParameter( "output" ).getStringValue() ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    finishTest();
  }

  public void testTemplate2() {
    startTest();
    SimpleParameterProvider parameterProvider = new SimpleParameterProvider();

    IRuntimeContext context = run( "/test/template/template2.xaction", parameterProvider ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals(
        Messages.getInstance().getString( "BaseTest.USER_RUNNING_ACTION_SEQUENCE" ), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus() ); //$NON-NLS-1$
    assertNotNull( "", context.getOutputParameter( "output" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    assertEquals(
        "", "Eastern is 120000 with a color of #00ff00, Western is 90000 with a color of #ffff00, Southern is 75000 with a color of #ff0000", context.getOutputParameter( "output" ).getStringValue() ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    finishTest();
  }

  public void testTemplate3() {
    startTest();

    SimpleParameterProvider parameterProvider = new SimpleParameterProvider();
    parameterProvider.setParameter( "type", "html" ); //$NON-NLS-1$ //$NON-NLS-2$
    OutputStream outputStream = getOutputStream( "TemplateTest.testTemplate3", ".svg" ); //$NON-NLS-1$ //$NON-NLS-2$
    SimpleOutputHandler outputHandler = new SimpleOutputHandler( outputStream, true );
    StandaloneSession session =
        new StandaloneSession( Messages.getInstance().getString( "BaseTest.DEBUG_JUNIT_SESSION" ) ); //$NON-NLS-1$
    IRuntimeContext context =
        run( "/test/template/europemap.xaction", null, false, parameterProvider, outputHandler, session ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals(
        Messages.getInstance().getString( "BaseTest.USER_RUNNING_ACTION_SEQUENCE" ), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus() ); //$NON-NLS-1$

    assertNotNull( "", context.getOutputParameter( "svg" ) ); //$NON-NLS-1$ //$NON-NLS-2$

    finishTest();
  }

  public static void main( String[] args ) {
    TemplateIT test = new TemplateIT();
    test.setUp();
    try {
      test.testTemplate1();
      test.testTemplate2();
      test.testTemplate3();
    } finally {
      test.tearDown();
      BaseTest.shutdown();
    }
  }

}
