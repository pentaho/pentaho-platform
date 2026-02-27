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


package org.pentaho.platform.plugin.services.importer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.pentaho.platform.api.repository2.unified.IPlatformImportBundle;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.plugin.services.importexport.ImportSession;
import org.pentaho.platform.util.logging.Logger;

/**
 * Will import all the zip files in a given directory using the supplied IPlatformImporter
 * 
 * User: kwalker Date: 6/20/13
 */
public class ArchiveLoader {
  public static final FilenameFilter ZIPS_FILTER = new FilenameFilter() {
    @Override
    public boolean accept( File dir, String name ) {
      return name.endsWith( ".zip" );
    }
  };
  static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat( ".yyyyMMddHHmm" );

  private IPlatformImporter importer;
  private Date loadStamp;

  public ArchiveLoader( IPlatformImporter importer ) {
    this( importer, new Date() );
  }

  ArchiveLoader( IPlatformImporter importer, Date loadStamp ) {
    this.importer = importer;
    ImportSession.getSession().setAclProperties( true, true, false );
    this.loadStamp = loadStamp;
  }

  public void loadAll( File directory, FilenameFilter filenameFilter ) {
    File[] files = directory.listFiles( filenameFilter );
    if ( files != null ) {
      for ( File file : files ) {
        try {
          Logger.debug( this.getClass().getName(), this.getClass().getName() + ": importing " + file.getName() );
          importer.importFile( createBundle( file ) );
        } catch ( Exception e ) {
          Logger.error( this.getClass().getName(), e.getMessage(), e );
        } finally {
          ImportSession.clearSession();
          file.renameTo( new File( file.getPath() + DATE_FORMAT.format( loadStamp ) ) );
        }
      }
    }
  }

  private IPlatformImportBundle createBundle( File file ) throws FileNotFoundException {

    RepositoryFileImportBundle.Builder bundleBuilder = new RepositoryFileImportBundle.Builder();
    bundleBuilder.input( createInputStream( file ) );
    bundleBuilder.charSet( "UTF-8" );
    bundleBuilder.hidden( true );
    bundleBuilder.schedulable( RepositoryFile.SCHEDULABLE_BY_DEFAULT );
    bundleBuilder.path( "/" );
    bundleBuilder.overwriteFile( true );
    bundleBuilder.name( file.getName() );
    bundleBuilder.applyAclSettings( true );
    bundleBuilder.overwriteAclSettings( false );
    bundleBuilder.retainOwnership( true );
    bundleBuilder.preserveDsw( true );
    return bundleBuilder.build();
  }

  FileInputStream createInputStream( File file ) throws FileNotFoundException {
    return new FileInputStream( file );
  }
}
