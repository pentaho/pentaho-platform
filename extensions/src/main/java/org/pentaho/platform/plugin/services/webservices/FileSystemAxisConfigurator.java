/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.platform.plugin.services.webservices;

import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.messages.Messages;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * An Axis Configurator that uses PentahoSystem to get the axis configuration file
 * 
 * @author jamesdixon
 */
public class FileSystemAxisConfigurator extends SystemSolutionAxisConfigurator {

  private static final long serialVersionUID = 3018138384199613741L;

  @Override
  public InputStream getConfigXml() {

    try {

      File f = new File( PentahoSystem.getApplicationContext().getSolutionPath( getAxisConfigPath() ) );
      InputStream in = new FileInputStream( f );

      return in;
    } catch ( Exception e ) {
      getLogger().error(
          Messages.getInstance().getErrorString(
            "SystemSolutionAxisConfigurator.ERROR_0001_BAD_CONFIG_FILE", getAxisConfigPath() ), e ); //$NON-NLS-1$
    }
    return null;
  }
}
