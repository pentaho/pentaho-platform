/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.plugin.services.metadata;

import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.repository2.ClientRepositoryPaths;

/**
 * Contains information about how and where metadata information is stored in the {@code }IUnifiedRepository}
 * 
 * @author <a href="mailto:dkincade@pentaho.com">David M. Kincade</a>
 */
public class PentahoMetadataDomainRepositoryInfo {
  /**
   * The name of the folder in which Pentaho Metadata should be stored (appended to the proper path for each tenant) -
   * the value is {@value}
   */
  private static final String METADATA_FOLDER_NAME = "metadata";

  /**
   * The file extension to be used with Pentaho Metadata Files
   */
  private static final String FILE_EXTENSION = ".xmi";

  /**
   * Returns the name of the metadata folder
   */
  public static String getMetadataFolderName() {
    return METADATA_FOLDER_NAME;
  }

  /**
   * Returns the path location in which the Pentaho Metadata folder will be created
   */
  public static String getMetadataParentPath() {
    return ClientRepositoryPaths.getEtcFolderPath();
  }

  /**
   * Generates the repository location for the Pentaho Metadata to be stored
   */
  public static String getMetadataFolderPath() {
    return getMetadataParentPath() + RepositoryFile.SEPARATOR + getMetadataFolderName();
  }

  /**
   * Returns the file extension to be used with Pentaho Metadata files
   */
  public static String getFileExtension() {
    return FILE_EXTENSION;
  }
}
