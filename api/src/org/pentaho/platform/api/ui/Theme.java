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
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Theme encapsulates a collection of ThemeResources and a root directory to access them from.
 * 
 * User: nbaker Date: 5/15/11
 */
public class Theme implements Serializable {

  /**
   * for Serializable
   */
  private static final long serialVersionUID = 1941655513749815162L;

  private Set<ThemeResource> resources = new LinkedHashSet<ThemeResource>();

  private String name;
  private String themeRootDir;
  private boolean hidden;
  private String id;

  public Theme( String id, String name, String rootDir ) {
    this.id = id;
    this.name = name;
    this.themeRootDir = rootDir;
  }

  public String getName() {
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public Set<ThemeResource> getResources() {
    return resources;
  }

  public void setResources( Set<ThemeResource> resources ) {
    this.resources = resources;
  }

  public void addResource( ThemeResource themeResource ) {
    resources.add( themeResource );
  }

  public String getThemeRootDir() {
    return themeRootDir;
  }

  @Override
  public boolean equals( Object o ) {
    if ( this == o ) {
      return true;
    }
    if ( o == null || getClass() != o.getClass() ) {
      return false;
    }

    Theme theme = (Theme) o;

    if ( name != null ? !name.equals( theme.name ) : theme.name != null ) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return name != null ? name.hashCode() : 0;
  }

  public boolean isHidden() {
    return hidden;
  }

  public void setHidden( boolean hidden ) {
    this.hidden = hidden;
  }

  public String getId() {
    return id;
  }

  public void setId( String id ) {
    this.id = id;
  }
}
