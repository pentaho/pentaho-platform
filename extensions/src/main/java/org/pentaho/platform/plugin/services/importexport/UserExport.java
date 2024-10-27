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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "UserExport", propOrder = { "username", "roles", "password", "userSettings" } )
public class UserExport {
  @XmlElement( name = "username" )
  String username;
  @XmlElement( name = "roles" )
  List<String> roles = new ArrayList<String>();
  @XmlElement( name = "password" )
  String password;
  @XmlElement( name = "userSettings" )
  List<ExportManifestUserSetting> userSettings = new ArrayList<>();

  public String getUsername() {
    return username;
  }

  public void setUsername( String username ) {
    this.username = username;
  }

  public List<String> getRoles() {
    return roles;
  }

  public void setRole( String role ) {
    this.roles.add( role );
  }

  public String getPassword() {
    return password;
  }

  public void setPassword( String password ) {
    this.password = password;
  }

  public List<ExportManifestUserSetting> getUserSettings() {
    return userSettings;
  }

  public void addUserSetting( ExportManifestUserSetting userSetting ) {
    userSettings.add( userSetting );
  }
}
