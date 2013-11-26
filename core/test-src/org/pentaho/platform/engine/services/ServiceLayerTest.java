/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.engine.services;

import junit.framework.TestCase;
import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoUrlFactory;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.engine.core.system.PathBasedSystemSettings;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneApplicationContext;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.objfac.StandaloneSpringPentahoObjectFactory;
import org.pentaho.platform.util.web.SimpleUrlFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings( "nls" )
public class ServiceLayerTest extends TestCase {

  final String SYSTEM_FOLDER = "/system";

  private static final String DEFAULT_SPRING_CONFIG_FILE_NAME = "pentahoObjects.spring.xml";

  private static final String SOLUTION_PATH = "test-res/solution";

  public String getSolutionPath() {
    return SOLUTION_PATH;
  }

  public void testEmptyActionSequence() throws IOException {
    StandaloneApplicationContext applicationContext = new StandaloneApplicationContext( getSolutionPath(), "" ); //$NON-NLS-1$
    String objectFactoryCreatorCfgFile = getSolutionPath() + SYSTEM_FOLDER + "/" + DEFAULT_SPRING_CONFIG_FILE_NAME; //$NON-NLS-1$

    PentahoSystem.setSystemSettingsService( new PathBasedSystemSettings() );
    IPentahoObjectFactory pentahoObjectFactory = new StandaloneSpringPentahoObjectFactory();
    pentahoObjectFactory.init( objectFactoryCreatorCfgFile, null );
    PentahoSystem.registerObjectFactory( pentahoObjectFactory );

    PentahoSystem.init( applicationContext );

    List messages = new ArrayList();
    String instanceId = null;
    IPentahoSession session = new StandaloneSession( "system" );
    ISolutionEngine solutionEngine = PentahoSystem.get( ISolutionEngine.class, session );
    solutionEngine.setLoggingLevel( ILogger.ERROR );
    solutionEngine.init( session );
    HashMap parameterProviderMap = new HashMap();
    IPentahoUrlFactory urlFactory = new SimpleUrlFactory( "" );
    Reader reader = null;
    try {
      File file = new File( getSolutionPath() + "/services_layer/test1.xaction" );
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
          solutionEngine
              .execute(
                  xactionStr,
                  "test1.xaction", "empty action sequence test", false, true, instanceId, false, parameterProviderMap, null, null, urlFactory, messages ); //$NON-NLS-1$ //$NON-NLS-2$
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

}
