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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * ModuleThemeInfo holds theme information for a platform module such as a plugin or other area.
 * 
 * User: nbaker Date: 5/15/11
 */
public class ModuleThemeInfo implements Serializable {

  /**
   * for Serializable
   */
  private static final long serialVersionUID = 7878856976816551482L;

  private List<Theme> moduleThemes = new ArrayList<Theme>();
  private List<Theme> systemThemes = new ArrayList<Theme>();
  private String module;

  /**
   * Constructs a new ModuleThemeInfo object parented to the given ThemeModule
   * 
   * @param module
   *          name
   */
  public ModuleThemeInfo( String module ) {
    this.module = module;
  }

  public List<Theme> getModuleThemes() {
    return moduleThemes;
  }

  public String getModule() {
    return module;
  }

  public List<Theme> getSystemThemes() {
    return systemThemes;
  }

}
