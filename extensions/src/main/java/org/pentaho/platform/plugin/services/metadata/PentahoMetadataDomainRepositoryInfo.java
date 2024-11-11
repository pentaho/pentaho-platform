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
