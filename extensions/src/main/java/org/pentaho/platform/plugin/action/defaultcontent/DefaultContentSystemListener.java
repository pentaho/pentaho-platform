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


package org.pentaho.platform.plugin.action.defaultcontent;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoSystemListener;
import org.pentaho.platform.api.engine.ISystemConfig;
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

    // By default we'll run in a separate thread. This checks to see if someone has disabled this.
    ISystemConfig systemSettings = PentahoSystem.get( ISystemConfig.class );
    Boolean enableAsyncLoading = true;
    if ( systemSettings != null ) {
      String disableLoadAsyncStr = systemSettings.getProperty( "system.enable-async-default-content-loading" );
      enableAsyncLoading = Boolean.valueOf( disableLoadAsyncStr );
    }

    Runnable runnable = new Runnable() {
      @Override public void run() {
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
      }
    };
    if ( enableAsyncLoading ) {
      Thread t = new Thread( runnable );
      t.setDaemon( true );
      t.setName( "Default Content Loader Thread" );
      t.start();
    } else {
      runnable.run();
    }
    return true;
  }
}
