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


package org.pentaho.test.platform.plugin.outputs;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.test.platform.engine.core.BaseTest;
import org.pentaho.test.platform.utils.TestResourceLocation;

import java.io.File;

@SuppressWarnings ( "nls" )
public class VFSOutputIT extends BaseTest {

  private static final String SOLUTION_PATH = TestResourceLocation.TEST_RESOURCES + "/solution";
  private static final String ALT_SOLUTION_PATH = TestResourceLocation.TEST_RESOURCES + "/outputs-solution";
  private static final String PENTAHO_XML_PATH = "/system/pentahoObjects.spring.xml";

  @Override
  public String getSolutionPath() {
    File file = new File( SOLUTION_PATH + PENTAHO_XML_PATH );
    if ( file.exists() ) {
      return SOLUTION_PATH;
    } else {
      return ALT_SOLUTION_PATH;
    }
  }

  public void testFileOutput() {
    startTest();
    SimpleParameterProvider parameterProvider = new SimpleParameterProvider();
    // create a user session
    IPentahoSession session = new StandaloneSession( "test" );
    PentahoSessionHolder.setSession( session );

    System.out.println( "solution.root.dir = " + System.getProperty( "solution.root.dir" ) );

    String xaction = "/test/platform/VFSOutputTest_file.xaction";
    if ( System.getProperty( "os.name" ).startsWith( "Windows" ) ) {
      xaction = "/test/platform/VFSOutputTestWindows_file.xaction";
    }

    IRuntimeContext context =
        run( xaction, parameterProvider, "VFSOutputTest.testFileOutput", ".txt" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
    assertEquals(
        Messages.getInstance().getString( "BaseTest.USER_RUNNING_ACTION_SEQUENCE" ), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus() ); //$NON-NLS-1$

    finishTest();
  }

  public static void main( String[] args ) {
    VFSOutputIT test = new VFSOutputIT();
    try {
      test.setUp();
      // test.testFileOutput();
    } finally {
      test.tearDown();
      BaseTest.shutdown();
    }
  }
}
