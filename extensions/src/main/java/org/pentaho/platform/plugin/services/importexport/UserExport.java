/*!
 *
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
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

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
