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

import java.util.Map;

/**
 * Theme Resolvers provide information on available themes and their resources.
 * 
 * User: nbaker Date: 5/15/11
 */
public interface IThemeResolver {

  /**
   * Return a map of ModuleThemeInfo objects keyed by module name.
   * 
   * @return map of ModuleThemeInfo by module name
   */
  Map<String, ModuleThemeInfo> getModuleThemes();

  /**
   * Return a map of system themes keyed by theme name.
   * 
   * @return map of System themes
   */
  Map<String, Theme> getSystemThemes();

}
