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

package org.pentaho.platform.plugin.services.importexport;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This export processor should be used when single file downloads are requested
 */
public class SimpleExportProcessor extends BaseExportProcessor {
  private static final Log log = LogFactory.getLog( SimpleExportProcessor.class );

  private String path;

  IUnifiedRepository unifiedRepository;

  /**
   * Encapsulates the logic of registering import handlers, generating the manifest, and performing the export
   */
  public SimpleExportProcessor( String path, IUnifiedRepository repository ) {
    // set a default path at root if missing
    if ( StringUtils.isEmpty( path ) ) {
      this.path = "/";
    } else {
      this.path = path;
    }

    this.unifiedRepository = repository;
  }

  /**
   * Performs the export process, returns a File object
   * 
   * @throws ExportException
   *           indicates an error in import processing
   */
  public File performExport( RepositoryFile exportRepositoryFile ) throws ExportException, IOException {
    OutputStream os;
    File exportFile = null;

    // create temp file
    exportFile = File.createTempFile( EXPORT_TEMP_FILENAME_PREFIX, EXPORT_TEMP_FILENAME_EXT );
    exportFile.deleteOnExit();

    // get the file path
    String filePath = new File( this.path ).getParent();

    // send a response right away if not found
    if ( exportRepositoryFile == null ) {
      // todo: add to messages.properties
      throw new FileNotFoundException( "JCR file not found: " + this.path );
    }

    os = new FileOutputStream( exportFile );

    try {
      exportFile( exportRepositoryFile, os, filePath );
    } catch ( Exception e ) {
      log.error( e.getMessage() );
      throw ( new ExportException() );
    } finally {
      // make sure to close output stream
      os.close();
    }

    // clean up
    os = null;

    return exportFile;
  }

  /**
   * @param repositoryDir
   * @param outputStream
   */
  @Override
  public void exportDirectory( RepositoryFile repositoryDir, OutputStream outputStream, String filePath )
    throws ExportException, IOException {
    throw new UnsupportedOperationException();
  }

  /**
   * 
   * @param repositoryFile
   * @param outputStream
   */
  @Override
  public void exportFile( RepositoryFile repositoryFile, OutputStream outputStream, String filePath )
    throws ExportException, IOException {
    // iterate through handlers to perform export
    for ( ExportHandler exportHandler : exportHandlerList ) {

      InputStream is = exportHandler.doExport( repositoryFile, filePath );

      // if we don't get a valid input stream back, skip it
      if ( is != null ) {

        IOUtils.copy( is, outputStream );

        is.close();
      }
    }
  }
}
