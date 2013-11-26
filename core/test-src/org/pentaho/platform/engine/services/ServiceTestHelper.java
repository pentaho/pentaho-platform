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

import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneApplicationContext;
import org.pentaho.platform.engine.core.system.StandaloneSession;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;

@SuppressWarnings( "nls" )
public class ServiceTestHelper {

  public static String getXAction( String solutionRoot, String actionPath ) {
    String xactionStr = null;
    Reader reader = null;
    try {
      File file = new File( solutionRoot + "/" + actionPath );
      StringBuilder str = new StringBuilder();
      reader = new FileReader( file );
      char[] buffer = new char[4096];
      int n = reader.read( buffer );
      while ( n != -1 ) {
        str.append( buffer, 0, n );
        n = reader.read( buffer );
      }
      xactionStr = str.toString();
    } catch ( Exception e ) {
      // we should not get here
      e.printStackTrace();
    } finally {
      if ( reader != null ) {
        try {
          reader.close();
        } catch ( Exception ex ) {
          ex.printStackTrace(); // shouldn't really fail the close....
        }
      }
    }
    return xactionStr;
  }

  public static void init( String solutionRoot ) {
    if ( !PentahoSystem.getInitializedOK() ) {
      StandaloneApplicationContext applicationContext = new StandaloneApplicationContext( solutionRoot, "" ); //$NON-NLS-1$
      PentahoSystem.init( applicationContext );
    }
  }

  public static ISolutionEngine getSolutionEngine() {
    IPentahoSession session = new StandaloneSession( "system" );
    return getSolutionEngine( session );

  }

  public static ISolutionEngine getSolutionEngine( IPentahoSession session ) {
    ISolutionEngine solutionEngine = PentahoSystem.get( ISolutionEngine.class, session );
    solutionEngine.setLoggingLevel( ILogger.ERROR );
    solutionEngine.init( session );
    return solutionEngine;
  }
}
