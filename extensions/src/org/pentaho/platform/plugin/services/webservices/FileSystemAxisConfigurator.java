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
