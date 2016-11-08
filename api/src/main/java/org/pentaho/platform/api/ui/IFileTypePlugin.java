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
