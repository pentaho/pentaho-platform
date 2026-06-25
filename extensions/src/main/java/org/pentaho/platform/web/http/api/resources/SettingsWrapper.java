/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/


package org.pentaho.platform.web.http.api.resources;

import jakarta.xml.bind.annotation.*;

import java.util.List;

@XmlRootElement( name = "settings" )
@XmlAccessorType( XmlAccessType.FIELD )
public class SettingsWrapper {

  @XmlElement( name = "setting" )
  private List<Setting> settings;

  public SettingsWrapper() {
  }

  public SettingsWrapper( List<Setting> settings ) {
    this.settings = settings;
  }

  public List<Setting> getSettings() {
    return settings;
  }

  public void setSettings( List<Setting> settings ) {
    this.settings = settings;
  }
}
