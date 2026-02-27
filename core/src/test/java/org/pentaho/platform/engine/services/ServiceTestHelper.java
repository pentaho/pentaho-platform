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

import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.engine.core.system.PentahoSystem;
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
