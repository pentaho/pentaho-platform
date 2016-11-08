/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.repository.usersettings.pojo;

import org.pentaho.platform.api.usersettings.pojo.IUserSetting;

import java.io.Serializable;

public class UserSetting implements Serializable, IUserSetting {

  private static final long serialVersionUID = 823604019645900631L;

  private long id; // Required

  private String username;

  private String settingName;

  private String settingValue;

  public UserSetting() {
  }

  public UserSetting( long id, int revision, String username, String settingName, String settingValue ) {
    super();
    this.id = id;
    this.username = username;
    this.settingName = settingName;
    this.settingValue = settingValue;
  }

  public long getId() {
    return id;
  }

  public void setId( long id ) {
    this.id = id;
  }

  public String getSettingName() {
    return settingName;
  }

  public void setSettingName( String settingName ) {
    this.settingName = settingName;
  }

  public String getSettingValue() {
    return settingValue;
  }

  public void setSettingValue( String settingValue ) {
    this.settingValue = settingValue;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername( String username ) {
    this.username = username;
  }

  public void onModuleLoad() {
    // no-up
  }

  public boolean equals( Object anotherSetting ) {
    // we define equality to mean that the settingName's are the same, not the values
    if ( anotherSetting == null || !( anotherSetting instanceof IUserSetting ) ) {
      return false;
    }
    if ( settingName.equals( ( (IUserSetting) anotherSetting ).getSettingName() ) ) {
      return true;
    }
    return false;
  }

}
