/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.platform.plugin.services.importexport;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
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
