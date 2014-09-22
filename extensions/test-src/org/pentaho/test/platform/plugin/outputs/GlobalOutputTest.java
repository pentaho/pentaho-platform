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

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings( "nls" )
public class GlobalOutputTest extends BaseTest {

  private static final String SOLUTION_PATH = "test-src/outputs-solution";
  private static final String ALT_SOLUTION_PATH = "test-src/outputs-solution";
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
