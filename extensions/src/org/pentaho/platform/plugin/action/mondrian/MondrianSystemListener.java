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
