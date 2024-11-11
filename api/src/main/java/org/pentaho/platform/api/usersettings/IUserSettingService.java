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


package org.pentaho.platform.api.usersettings;

import org.pentaho.platform.api.engine.IPentahoInitializer;
import org.pentaho.platform.api.usersettings.pojo.IUserSetting;

import java.util.List;

public interface IUserSettingService extends IPentahoInitializer {
  public void deleteUserSettings();

  // if a global setting exists, the user setting has priority
  public List<IUserSetting> getUserSettings();

  public IUserSetting getUserSetting( String settingName, String defaultValue );

  public void setUserSetting( String settingName, String settingValue );

  // the implementation should allow only an administrator to set global user settings
  public List<IUserSetting> getGlobalUserSettings();

  public IUserSetting getGlobalUserSetting( String settingName, String defaultValue );

  public void setGlobalUserSetting( String settingName, String settingValue );
}
