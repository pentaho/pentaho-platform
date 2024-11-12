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


package org.pentaho.platform.api.ui;

/**
 * This interface defines a file type plugin, which specifies the behavior for viewing and editing specific file
 * types
 * 
 * @author Will Gorman (wgorman@pentaho.com)
 */
public interface IFileTypePlugin {

  /**
   * return the file extension this plugin supports
   * 
   * @return file extension
   */
  String getFileExtension();

  /**
   * return a comma separated list of commands supported by this plugin
   * 
   * @return enabled options
   */
  String getEnabledOptions();

  /**
   * return the open url pattern. parameterized values include: {solution} {path} {name}
   * 
   * @return open url pattern.
   */
  String getOpenUrlPattern();

  /**
   * return the edit url pattern. parameterized values include: {solution} {path} {name}
   * 
   * @return edit url pattern.
   */
  String getEditUrlPattern();
}
