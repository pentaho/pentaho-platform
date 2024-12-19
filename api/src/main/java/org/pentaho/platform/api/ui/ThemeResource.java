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
