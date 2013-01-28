package org.pentaho.mantle.client.usersettings;

import com.google.gwt.core.client.JsArray;

public interface IUserSettingsListener {
  public void onFetchUserSettings(JsArray<JsSetting> settings);
}
