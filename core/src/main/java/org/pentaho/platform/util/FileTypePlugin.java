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


package org.pentaho.platform.util;

import org.pentaho.platform.api.ui.IFileTypePlugin;

/**
 * implementation of IFileTypePlugin
 * 
 * @author Will Gorman (wgorman@pentaho.com)
 */
public class FileTypePlugin implements IFileTypePlugin {

  private String fileExtension;
  private String enabledOptions;
  private String openUrlPattern;
  private String editUrlPattern;

  public FileTypePlugin( String fileExtension, String enabledOptions, String openUrlPattern, String editUrlPattern ) {
    this.fileExtension = fileExtension;
    this.enabledOptions = enabledOptions;
    this.openUrlPattern = openUrlPattern;
    this.editUrlPattern = editUrlPattern;
  }

  public String getFileExtension() {
    return fileExtension;
  }

  public String getEnabledOptions() {
    return enabledOptions;
  }

  public String getOpenUrlPattern() {
    return openUrlPattern;
  }

  public String getEditUrlPattern() {
    return editUrlPattern;
  }
}
