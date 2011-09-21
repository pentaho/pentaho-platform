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
 * Copyright 2008 Pentaho Corporation.  All rights reserved. 
 * 
 */
package org.pentaho.platform.repository.usersettings;

import java.util.List;

import org.hibernate.Session;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.usersettings.IUserSettingService;
import org.pentaho.platform.api.usersettings.pojo.IUserSetting;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.repository.hibernate.HibernateUtil;
import org.pentaho.platform.repository.usersettings.pojo.UserSetting;
import org.pentaho.platform.repository.messages.Messages;

public class UserSettingService implements IUserSettingService {

  public static final String GLOBAL_SETTING = "_GLOBAL"; //$NON-NLS-1$
  IPentahoSession            session        = null;

  public UserSettingService() {
  }

  public void init(IPentahoSession session) {
    this.session = session;
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // GENERIC/ADMIN METHODS
  // ////////////////////////////////////////////////////////////////////////////////////////////////

  // delete all settings for a given user
  public void deleteUserSettings() {
    Session hibsession = HibernateUtil.getSession();
    try {
    HibernateUtil.beginTransaction();
      try {
    List<IUserSetting> settings = UserSettingDAO.getUserSettings(hibsession, session.getName());
    if (settings != null) {
      for (IUserSetting setting : settings) {
        hibsession.delete(setting);
      }
    }
      } finally {
    HibernateUtil.commitTransaction();
      }
    } finally {
    HibernateUtil.closeSession();
  }
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // USER SETTINGS METHODS
  // ////////////////////////////////////////////////////////////////////////////////////////////////

  public List<IUserSetting> getUserSettings() {
    // get the global settings and the user settings
    // merge unseen global settings into the user settings list
    List<IUserSetting> userSettings = null;
    Session hibsession = HibernateUtil.getSession();
    try {
      HibernateUtil.beginTransaction();
      try {
        userSettings = UserSettingDAO.getUserSettings(hibsession, session.getName());
    List<IUserSetting> globalSettings = UserSettingDAO.getUserSettings(hibsession, GLOBAL_SETTING);
    // merge the two lists (add unseen global settings)
    for (IUserSetting globalSetting : globalSettings) {
      if (!userSettings.contains(globalSetting)) {
        userSettings.add(globalSetting);
      }
    }
      } finally {
        HibernateUtil.commitTransaction();
      }
    } finally {
    HibernateUtil.closeSession();
    }
    return userSettings;
  }

  public IUserSetting getUserSetting(String settingName, String defaultValue) {
    // if the user does not have the setting, check if a global setting exists
    IUserSetting userSetting = null; 
    Session hibsession = HibernateUtil.getSession();
    try {
      HibernateUtil.beginTransaction();
      try {
        userSetting = UserSettingDAO.getUserSetting(hibsession, session.getName(), settingName);
    if (userSetting == null) {
      userSetting = getGlobalUserSetting(settingName, defaultValue);
    }
      } finally {
        HibernateUtil.commitTransaction();
      }
    } finally {
    HibernateUtil.closeSession();
    }
    return userSetting;
  }

  public void setUserSetting(String settingName, String settingValue) {
    Session hibsession = HibernateUtil.getSession();
    try {
    HibernateUtil.beginTransaction();
      try {
    UserSettingDAO.setUserSetting(hibsession, session.getName(), settingName, settingValue);
      } finally {
    HibernateUtil.commitTransaction();
      }
    } finally {
    HibernateUtil.closeSession();
  }
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // GLOBAL USER SETTINGS METHODS
  // ////////////////////////////////////////////////////////////////////////////////////////////////

  public IUserSetting getGlobalUserSetting(String settingName, String defaultValue) {
    IUserSetting userSetting = null;
    Session hibsession = HibernateUtil.getSession();
    try {
      HibernateUtil.beginTransaction();
      try {
        userSetting = UserSettingDAO.getUserSetting(hibsession, GLOBAL_SETTING, settingName);
    if (userSetting == null && defaultValue != null) {
      // pass out default value
      userSetting = new UserSetting();
      userSetting.setUsername(session.getName());
      userSetting.setSettingName(settingName);
      userSetting.setSettingValue(defaultValue);
    }
      } finally {
        HibernateUtil.commitTransaction();
      }
    } finally {
    HibernateUtil.closeSession();
    }
    return userSetting;
  }

  public List<IUserSetting> getGlobalUserSettings() {
    List<IUserSetting> settings = null;
    Session hibsession = HibernateUtil.getSession();
    try {
      HibernateUtil.beginTransaction();
      try {
        settings = UserSettingDAO.getUserSettings(hibsession, GLOBAL_SETTING);
      } finally {
        HibernateUtil.commitTransaction();
      }
    } finally {
    HibernateUtil.closeSession();
    }
    return settings;
  }

  public void setGlobalUserSetting(String settingName, String settingValue) {
    if (SecurityHelper.isPentahoAdministrator(session)) {
      Session hibsession = HibernateUtil.getSession();
      try {
      HibernateUtil.beginTransaction();
        try {
      UserSettingDAO.setUserSetting(hibsession, GLOBAL_SETTING, settingName, settingValue);
        } finally {
      HibernateUtil.commitTransaction();
        }
      } finally {
      HibernateUtil.closeSession();
      }
    } else {
      throw new UnsupportedOperationException(Messages.getInstance().getErrorString("UserSettingService.ERROR_0001_INSUFFICIENT_PRIVILEGES")); //$NON-NLS-1$
    }
  }

}
