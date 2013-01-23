package org.pentaho.platform.plugin.services.importexport;/*
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
 * Time: 4:16 PM
 */

import org.pentaho.platform.api.repository2.unified.RepositoryFile;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipOutputStream;

public interface ExportProcessor {

  /**
   * Adds an {@link ExportHandler} to the end of the list of Export Handlers. The first ExportHandler added
   * will be the first to get a chance to process the data
   *
   * @param exportHandler
   */
  public void addExportHandler(final ExportHandler exportHandler);

  /**
   * Performs the export process
   *
   * @throws ExportException indicates an error in import processing
   */
  public File performExport(RepositoryFile repositoryFile) throws ExportException, IOException;

  /**
   *
   * @param repositoryFile
   * @param outputStream
   * @throws ExportException
   */
  public void exportFile(RepositoryFile repositoryFile, ZipOutputStream outputStream, String filePath) throws ExportException, IOException;

  /**
   *
   * @param repositoryFile
   * @param outputStream
   * @throws ExportException
   */
  public void exportDirectory(RepositoryFile repositoryFile, ZipOutputStream outputStream, String filePath) throws ExportException, IOException;

}
