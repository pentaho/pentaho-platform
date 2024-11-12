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

import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoUrlFactory;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.util.web.SimpleUrlFactory;
import org.pentaho.test.platform.engine.core.BaseTest;
import org.pentaho.test.platform.utils.TestResourceLocation;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings( "nls" )
public class GlobalOutputIT extends BaseTest {

  private static final String SOLUTION_PATH = TestResourceLocation.TEST_RESOURCES + "/outputs-solution";
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

  public void testEmptyActionSequence() {
    startTest();

    List messages = new ArrayList();
    String instanceId = null;
    IPentahoSession session = new StandaloneSession( "system" );
    PentahoSessionHolder.setSession( session );
    ISolutionEngine solutionEngine = PentahoSystem.get( ISolutionEngine.class );
    solutionEngine.setLoggingLevel( ILogger.ERROR );
    solutionEngine.init( session );
    String baseUrl = PentahoSystem.getApplicationContext().getBaseUrl();
    HashMap parameterProviderMap = new HashMap();
    IPentahoUrlFactory urlFactory = new SimpleUrlFactory( baseUrl );

    try {
      File file = new File( getSolutionPath() + "/samples/platform/SetGlobalOutputTest.xaction" );
      StringBuilder str = new StringBuilder();
      Reader reader = new FileReader( file );
      char[] buffer = new char[4096];
      int n = reader.read( buffer );
      while ( n != -1 ) {
        str.append( buffer, 0, n );
        n = reader.read( buffer );
      }
      String xactionStr = str.toString();

      solutionEngine.setSession( session );
      IRuntimeContext runtimeContext =
          solutionEngine
              .execute(
                xactionStr,
                "SetGlobalOutputTest.xaction", "empty action sequence test", false, true, instanceId, false,
                parameterProviderMap, null, null, urlFactory, messages ); //$NON-NLS-1$ //$NON-NLS-2$
      assertNotNull( "RuntimeContext is null", runtimeContext );
      assertEquals( "Action sequence execution failed", runtimeContext.getStatus(),
          IRuntimeContext.RUNTIME_STATUS_SUCCESS );
      IParameterProvider provider = PentahoSystem.getGlobalParameters();
      String parameter = provider.getStringParameter( "GLOBAL_TEST", null ); //$NON-NLS-1$
      assertNotNull( parameter );
      assertEquals( "This is a test", parameter ); //$NON-NLS-1$

    } catch ( Exception e ) {
      // we should not get here
      e.printStackTrace();
      assertTrue( e.getMessage(), false );
    }

  }

}
