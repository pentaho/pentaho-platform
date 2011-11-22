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
 * Copyright 2011 Pentaho Corporation. All rights reserved.
 */
package org.pentaho.platform.repository2.unified.metadata;

import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.repository2.ClientRepositoryPaths;

/**
 * Contains information about how and where metadata information is stored in the JCR repository
 * User: dkincade
 */
public class JcrMetadataInfo {
  /**
   * The name of the folder in which Pentaho Metadata should be stored (appended to the proper path for
   * each tenant) - the value is {@value}
   */
  protected static final String METADATA_FOLDER_NAME = "metadata";

  /**
   * The name of the file use for mapping Pentaho Metadata {@code domain IDs} into the sub-folders that
   * hold all the information for the Pentaho Metadata.
   */
  protected static final String METADATA_MAPPING_FILE_NAME = "metadata-mappings.properties";

  /**
   * The name of the file in each domain folder that will contain the metadata information
   */
  protected static final String METADATA_FILE_NAME = "metadata.xmi";

  /**
   * Returns the name of the metadata folder
   */
  public String getMetadataFolderName() {
    return METADATA_FOLDER_NAME;
  }

  /**
   * Returns the name of the metadata folder
   */
  public String getMetadataMappingFileName() {
    return METADATA_MAPPING_FILE_NAME;
  }

  /**
   * Returns the path location in which the Pentaho Metadata folder will be created
   */
  public String getMetadataParentPath() {
    return ClientRepositoryPaths.getEtcFolderPath();
  }

  /**
   * Generates the repository location for the Pentaho Metadata to be stored
   */
  public String getMetadataFolderPath() {
    return getMetadataParentPath() + RepositoryFile.SEPARATOR + METADATA_FOLDER_NAME;
  }

  /**
   * Returns the full-path to the Pentaho Metadata mappings file
   */
  public String getMetadataMappingFilePath() {
    return getMetadataFolderPath() + RepositoryFile.SEPARATOR + getMetadataMappingFileName();
  }

  public String getMetadataFilename() {
    return METADATA_FILE_NAME;
  }
}
