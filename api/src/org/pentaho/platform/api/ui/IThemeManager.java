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

import java.util.List;

/**
 * Class responsible for collecting information about all available system module local themes. All implementations
 * should utilize a caching mechanism to prevent unnecessary overhead.
 * 
 * User: nbaker Date: 5/15/11
 */
public interface IThemeManager {

  /**
   * Set the collection of IThemeResolver objects used to find available themes
   * 
   * @param resolvers
   */
  void setThemeResolvers( List<IThemeResolver> resolvers );

  /**
   * Return a list of available system themes.
   * 
   * @return List of themes
   */
  public List<String> getSystemThemeIds();

  /**
   * Returns the named theme or null if not found.
   * 
   * @param themeName
   * @return Theme for the supplied name
   */
  Theme getSystemTheme( String themeName );

  /**
   * Returns the named local theme for the given named module or null if either is not found
   * 
   * @param moduleName
   * @param themeName
   * @return
   */
  Theme getModuleTheme( String moduleName, String themeName );

  /**
   * Returns the MoudleThemeInfo object containing all themes for the given module name or null if not found.
   * 
   * @param moduleName
   * @return
   */
  ModuleThemeInfo getModuleThemeInfo( String moduleName );

  /**
   * Force the Theme Manager to recalculate system and module themes
   */
  void refresh();
}
