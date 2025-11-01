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


package org.pentaho.platform.engine.services;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoUrlFactory;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.boot.PlatformInitializationException;
import org.pentaho.platform.util.web.SimpleUrlFactory;
import org.pentaho.test.platform.engine.core.MicroPlatform;

@SuppressWarnings( "nls" )
public class ServiceLayerTest extends Assert {

  private static final String SYSTEM_FOLDER = "/system";

  private static final String DEFAULT_SPRING_CONFIG_FILE_NAME = "pentahoObjects.spring.xml";

  private static final String SOLUTION_PATH = "src/test/resources/solution";

  private static MicroPlatform mp;

  @BeforeClass
  public static void beforeClass() throws PlatformInitializationException {
    mp = new MicroPlatform( SOLUTION_PATH );
    mp.setSpringConfig( SOLUTION_PATH + SYSTEM_FOLDER + "/" + DEFAULT_SPRING_CONFIG_FILE_NAME );
    mp.start();
  }

  @Test
  public void testEmptyActionSequence() throws IOException {
    List<Object> messages = new ArrayList<Object>();
    String instanceId = null;
    IPentahoSession session = new StandaloneSession( "system" );
    ISolutionEngine solutionEngine = PentahoSystem.get( ISolutionEngine.class, session );
    solutionEngine.setLoggingLevel( ILogger.ERROR );
    solutionEngine.init( session );
    Map<Object, Object> parameterProviderMap = new HashMap<Object, Object>();
    IPentahoUrlFactory urlFactory = new SimpleUrlFactory( "" );
    Reader reader = null;
    try {
      File file = new File( SOLUTION_PATH + "/services_layer/test1.xaction" );
      StringBuilder str = new StringBuilder();
      reader = new FileReader( file );
      char[] buffer = new char[4096];
      int n = reader.read( buffer );
      while ( n != -1 ) {
        str.append( buffer, 0, n );
        n = reader.read( buffer );
      }
      String xactionStr = str.toString();

      solutionEngine.setSession( session );
      IRuntimeContext runtimeContext =
          solutionEngine.execute( xactionStr, "test1.xaction", "empty action sequence test", false, true, instanceId, //$NON-NLS-1$ //$NON-NLS-2$
              false, parameterProviderMap, null, null, urlFactory, messages );
      assertNotNull( "RuntimeContext is null", runtimeContext );
      assertEquals( "Action sequence execution failed", runtimeContext.getStatus(),
          IRuntimeContext.RUNTIME_STATUS_SUCCESS );
    } catch ( Exception e ) {
      // we should not get here
      e.printStackTrace();
      assertTrue( e.getMessage(), false );
    } finally {
      if ( reader != null ) {
        reader.close();
      }
    }
  }

  @AfterClass
  public static void afterClass() {
    mp.stop();
  }

}
