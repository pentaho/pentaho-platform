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

package org.pentaho.platform.plugin.services.importer;

import org.pentaho.platform.plugin.services.importexport.ImportSession;
import org.pentaho.platform.util.logging.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.Date;

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
    bundleBuilder.path( "/" );
    bundleBuilder.overwriteFile( true );
    bundleBuilder.name( file.getName() );
    bundleBuilder.applyAclSettings( true );
    bundleBuilder.overwriteAclSettings( false );
    bundleBuilder.retainOwnership( true );
    return bundleBuilder.build();
  }

  FileInputStream createInputStream( File file ) throws FileNotFoundException {
    return new FileInputStream( file );
  }
}
