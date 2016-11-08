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
/*
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
 * Copyright 2013 Pentaho Corporation.  All rights reserved.
 *
 * User: pminutillo
 * Date: 1/16/13
 * Time: 4:41 PM
 */

import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * A BaseExportProcessor subclass should encapsulate all logic for creating and assembling export file. It should also
 * handle the desired output format. Examples:
 * <p/>
 * SimpleExportProcessor - Export single file "as-is" ZipExportProcessor - Export single file or directory as .zip
 */
public abstract class BaseExportProcessor {
  private String path;

  List<ExportHandler> exportHandlerList = new ArrayList();

  IUnifiedRepository unifiedRepository;

  protected static final String EXPORT_MANIFEST_FILENAME = "exportManifest.xml";
  protected static final String EXPORT_INFO_DATE_FORMAT = "dd-MM-yyyy";
  protected static final String EXPORT_INFO_TIME_FORMAT = "hh:mm:ss z";
  protected static final String EXPORT_TEMP_FILENAME_PREFIX = "repoExport";
  protected static final String EXPORT_TEMP_FILENAME_EXT = ".zip";
  protected static final String LOCALE_EXT = ".locale";

  /**
   *
   */
  public BaseExportProcessor() {
  }

  /**
   * Adds an {@link ExportHandler} to the end of the list of Export Handlers. The first ExportHandler added will be the
   * first to get a chance to process the data
   *
   * @param exportHandler
   */
  public void addExportHandler( ExportHandler exportHandler ) {
    exportHandlerList.add( exportHandler );
  }

  /**
   * @return
   */
  public String getPath() {
    return path;
  }

  /**
   * @param path
   */
  public void setPath( String path ) {
    this.path = path;
  }

  /**
   * @return
   */
  public IUnifiedRepository getUnifiedRepository() {
    return unifiedRepository;
  }

  /**
   * @param unifiedRepository
   */
  public void setUnifiedRepository( IUnifiedRepository unifiedRepository ) {
    this.unifiedRepository = unifiedRepository;
  }

  /**
   * Performs the export process, returns a zip File object
   *
   * @throws ExportException indicates an error in import processing
   */
  public abstract File performExport( RepositoryFile exportRepositoryFile ) throws ExportException, IOException;

  /**
   * @param repositoryDir
   * @param outputStream
   */

  public abstract void exportDirectory( RepositoryFile repositoryDir, OutputStream outputStream, String filePath )
    throws ExportException, IOException;

  /**
   * @param repositoryFile
   * @param outputStream
   */
  public abstract void exportFile( RepositoryFile repositoryFile, OutputStream outputStream, String filePath )
    throws ExportException, IOException;

}
