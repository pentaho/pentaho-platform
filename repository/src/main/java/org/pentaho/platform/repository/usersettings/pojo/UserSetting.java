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


package org.pentaho.platform.repository.usersettings.pojo;

import com.google.gwt.core.client.EntryPoint;
import org.pentaho.platform.api.usersettings.pojo.IUserSetting;

import java.io.Serializable;

public class UserSetting implements EntryPoint, Serializable, IUserSetting {

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
