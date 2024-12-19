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


package org.pentaho.platform.plugin.action.mondrian;

import mondrian.olap.MondrianProperties;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoSystemListener;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.platform.util.logging.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class MondrianSystemListener implements IPentahoSystemListener {

  public boolean startup( final IPentahoSession session ) {
    loadMondrianProperties( session );
    return true;
  }

  /**
   * on pentaho system startup, load the mondrian.properties file from system/mondrian/mondrian.properties
   */
  public void loadMondrianProperties( final IPentahoSession session ) {
    /* Load the mondrian.properties file */
    String mondrianPropsFilename = "system" + File.separator + "mondrian" + File.separator + "mondrian.properties"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    File mondrianPropsFile = new File( PentahoSystem.getApplicationContext().getSolutionPath( mondrianPropsFilename ) );
    InputStream is = null;
    try {
      if ( mondrianPropsFile.exists() ) {
        is = new FileInputStream( mondrianPropsFile );
        MondrianProperties.instance().load( is );
        Logger.debug( MondrianSystemListener.class.getName(), Messages.getInstance().getString(
            "MondrianSystemListener.PROPERTY_FILE_LOADED", mondrianPropsFilename ) ); //$NON-NLS-1$
      } else {
        Logger.warn( MondrianSystemListener.class.getName(), Messages.getInstance().getString(
            "MondrianSystemListener.PROPERTY_FILE_NOT_FOUND", mondrianPropsFilename ) ); //$NON-NLS-1$
      }
    } catch ( IOException ioe ) {
      Logger.error( MondrianSystemListener.class.getName(), Messages.getInstance().getString(
          "MondrianSystemListener.ERROR_0002_PROPERTY_FILE_READ_FAILED", ioe.getMessage() ), ioe ); //$NON-NLS-1$
    } finally {
      try {
        if ( is != null ) {
          is.close();
        }
      } catch ( IOException e ) {
        // ignore
      }
    }
  }

  public void shutdown() {
    // Nothing required
  }
}
