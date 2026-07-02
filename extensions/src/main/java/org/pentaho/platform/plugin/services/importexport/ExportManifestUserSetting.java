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


package org.pentaho.platform.plugin.services.importexport;

import org.pentaho.platform.api.usersettings.pojo.IUserSetting;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "ExportManifestUserSetting", propOrder = { "name", "value" } )
public class ExportManifestUserSetting {
  @XmlElement( name = "name" )
  String name;
  @XmlElement( name = "value" )
  String value;

  public ExportManifestUserSetting() {
  }

  public ExportManifestUserSetting( String name, String value ) {
    this.name = name;
    this.value = value;
  }

  public ExportManifestUserSetting( IUserSetting userSetting ) {
    this.setName( userSetting.getSettingName() );
    this.setValue( userSetting.getSettingValue() );
  }

  public String getName() {
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public String getValue() {
    return value;
  }

  public void setValue( String value ) {
    this.value = value;
  }
}
