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


package org.pentaho.platform.config;

import org.pentaho.platform.engine.core.system.PentahoSystem;

import java.io.File;

/**
 * User: nbaker Date: 4/6/13
 */
public class SolutionPropertiesFileConfiguration extends PropertiesFileConfiguration {

  public SolutionPropertiesFileConfiguration( String id, String propFile ) {
    super( id, new File( getSolutionPath() + File.separator + propFile ) );
  }

  protected static String getSolutionPath() {
    String solutionPath = null;
    if ( PentahoSystem.getApplicationContext() != null ) {
      solutionPath = PentahoSystem.getApplicationContext().getSolutionPath( "system" );
    } else {
      solutionPath = System.getProperty( "PentahoSystemPath" );
    }
    return solutionPath;
  }
}
