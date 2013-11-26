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

package org.pentaho.platform.plugin.action.defaultcontent;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoSystemListener;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.plugin.services.importer.ArchiveLoader;
import org.pentaho.platform.plugin.services.importer.IPlatformImporter;
import org.pentaho.platform.util.logging.Logger;

import java.io.File;
import java.util.concurrent.Callable;

public class DefaultContentSystemListener implements IPentahoSystemListener {

  private static final String DEFAULT_CONTENT_FOLDER = "system/default-content";

  @Override
  public void shutdown() {
  }

  @Override
  public boolean startup( IPentahoSession arg0 ) {

    try {
      SecurityHelper.getInstance().runAsSystem( new Callable<Void>() {

        @Override
        public Void call() throws Exception {
          Logger.info( this.getClass().getName(), "Default content importer has started" );

          // get a File reference to the directory
          String solutionPath = PentahoSystem.getApplicationContext().getSolutionPath( DEFAULT_CONTENT_FOLDER );
          File directory = new File( solutionPath );

          // Instantiate the importer
          IPlatformImporter importer = PentahoSystem.get( IPlatformImporter.class );
          ArchiveLoader archiveLoader = new ArchiveLoader( importer );
          archiveLoader.loadAll( directory, ArchiveLoader.ZIPS_FILTER );
          return null;
        }
      } );
    } catch ( Exception e ) {
      Logger.error( this.getClass().getName(), e.getMessage() );
    }

    // we return true even of an exception is caught.
    // if we would return a false then the server will not start.
    return true;
  }
}
