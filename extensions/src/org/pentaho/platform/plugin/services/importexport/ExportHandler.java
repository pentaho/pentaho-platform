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
 * Time: 4:18 PM
 */

import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.repository2.unified.exportManifest.ExportManifest;

import java.io.IOException;
import java.util.zip.ZipOutputStream;

/**
 * Contract with ExportProcessor to know how to perform export given the parameters
 */
public interface ExportHandler {

  /**
   * Perform export with registered handlers
   */
  public void doExport(RepositoryFile repositoryFile, ZipOutputStream outputStream, String filePath, ExportManifest exportManifest) throws ExportException, IOException;

}
