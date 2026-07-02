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

package org.pentaho.platform.web.http.api.resources;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.List;

@XmlRootElement( name = "themes" )
@XmlAccessorType( XmlAccessType.FIELD )
public class ThemeWrapper {

  @XmlElement( name = "theme" )
  private List<Theme> themes;

  public ThemeWrapper() {

  }

  public ThemeWrapper( List<Theme> themes ) {
    this.themes = themes;
  }

  public List<Theme> getThemes() {
    return themes;
  }

  public void setThemes( List<Theme> themes ) {
    this.themes = themes;
  }
}
