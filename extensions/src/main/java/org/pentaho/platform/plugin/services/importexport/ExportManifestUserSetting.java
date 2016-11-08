package org.pentaho.platform.plugin.services.importexport;

import org.pentaho.platform.api.usersettings.pojo.IUserSetting;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

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
