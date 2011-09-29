package org.pentaho.mantle.client.usersettings;

import java.util.ArrayList;

import org.pentaho.platform.api.usersettings.pojo.IUserSetting;

public interface IUserSettingsListener {
  public void onFetchUserSettings(ArrayList<IUserSetting> settings);
}
