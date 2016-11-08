package org.pentaho.platform.api.usersettings;

import org.pentaho.platform.api.usersettings.pojo.IUserSetting;

import java.util.List;

@Deprecated
/**
 * This interface only exists to extend the IUserSettingService to support getting settings for any user by username
 * not just for the current session's user. It should be folded back into the IUserSettingService
 * at the next major release
 *
 * @deprecated This is only here to extend the IUserSettingService capabilities until these methods can be folded in.
 */
public interface IAnyUserSettingService extends IUserSettingService {
  /**
   * Deletes all user settings for a specified username
   * @param username username to remove the setting from
   * @throws SecurityException if the active user does not have the appropriate credentials to perform this operation
   */
  void deleteUserSettings( String username ) throws SecurityException;

  /**
   * Gets a list of all user settings for a specified username
   * @param username username to get the setting for
   * @return
   * @throws SecurityException if the active user does not have the appropriate credentials to perform this operation
   */
  List<IUserSetting> getUserSettings( String username ) throws SecurityException;

  /**
   * Gets a particular user setting for a specified username
   * @param username      username to get the setting for
   * @param settingName   name of the interested setting
   * @param defaultValue  default value if none found
   * @return
   * @throws SecurityException if the active user does not have the appropriate credentials to perform this operation
   */
  IUserSetting getUserSetting( String username, String settingName, String defaultValue ) throws SecurityException;

  /**
   * Sets a user setting for a specified username
   * @param username      username to get the setting for
   * @param settingName   name of the setting
   * @param settingValue  value of the setting
   * @throws SecurityException if the active user does not have the appropriate credentials to perform this operation
   */
  void setUserSetting( String username, String settingName, String settingValue ) throws SecurityException;
}
