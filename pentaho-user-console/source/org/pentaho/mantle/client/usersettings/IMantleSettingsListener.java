package org.pentaho.mantle.client.usersettings;

import java.util.HashMap;

public interface IMantleSettingsListener {
  public void onFetchMantleSettings(HashMap<String,String> settings);
}
