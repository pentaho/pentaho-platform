/*!
 *
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
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

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
