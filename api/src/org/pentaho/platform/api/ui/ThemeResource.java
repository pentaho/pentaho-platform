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

/**
 * User: nbaker Date: 5/15/11
 */
public class ThemeResource implements Serializable {

  /**
   * for Serializable
   */
  private static final long serialVersionUID = -7755888490441339129L;

  Theme theme;
  String location;

  public ThemeResource( Theme theme, String resource ) {
    this.theme = theme;
    location = resource;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation( String location ) {
    this.location = location;
  }

  public Theme getTheme() {
    return theme;
  }

  public void setTheme( Theme theme ) {
    this.theme = theme;
  }
}
