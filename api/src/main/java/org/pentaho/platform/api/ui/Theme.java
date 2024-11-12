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
  private boolean responsive;

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

  /**
   * Gets a value that indicates whether the theme is responsive.
   * @return `true` if responsive; `false`, otherwise.
   */
  public boolean getResponsive() {
    return responsive;
  }

  /**
   * Sets whether the theme is responsive.
   * <p>
   * Responsive themes enable certain CSS classes, e.g. `responsive`, to behave in a responsive manner.
   * <p>
   * When the current theme is responsive, the CSS class `responsive-theme` is added to the HTML document's root
   * element, `html`.
   *
   * @param responsive `true` if responsive; `false`, otherwise.
   */
  public void setResponsive( boolean responsive ) {
    this.responsive = responsive;
  }
}
