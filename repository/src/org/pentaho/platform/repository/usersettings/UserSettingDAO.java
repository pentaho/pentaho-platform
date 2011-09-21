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

import org.hibernate.Query;
import org.hibernate.Session;
import org.pentaho.platform.api.usersettings.pojo.IUserSetting;
import org.pentaho.platform.repository.usersettings.pojo.UserSetting;

public class UserSettingDAO {
  // only the org.pentaho.platform.repository.usersettings package can execute these methods!
  // we do not want to generically expose these methods
  static List<IUserSetting> getUserSettings(Session session, String username) {
    Query namedQuery = session.getNamedQuery("org.pentaho.platform.repository.usersettings.pojo.UserSetting.getUserSettings");
    Query username1 = namedQuery.setParameter("username", username);
    Query username11 = username1;
    Query qry = username11 .setCacheable(true);
    return qry.list();
  }

  static IUserSetting getUserSetting(Session session, String username, String settingName) {
    Query namedQuery = session.getNamedQuery("org.pentaho.platform.repository.usersettings.pojo.UserSetting.getUserSetting"); //$NON-NLS-1$
    Query namedQuery2 = namedQuery.setParameter("username", username); //$NON-NLS-1$
    Query namedQuery3 = namedQuery2.setParameter("settingName", settingName); //$NON-NLS-1$
    Query namedQuery4 = namedQuery3.setCacheable(true);
    List list = namedQuery4.list();
    if (list != null && list.size() > 0) {
      return (IUserSetting)list.get(0);
    }
    return null;
  }

  static void setUserSetting(Session session, String username, String settingName, String settingValue) {
    // make sure any old values are removed
    removeUserSetting(session, username, settingName);
    IUserSetting setting = new UserSetting();
    setting.setUsername(username);
    setting.setSettingName(settingName);
    setting.setSettingValue(settingValue);
    session.saveOrUpdate(setting);
  }

  // remove all settings for a given settingName
  static void removeUserSetting(Session session, String username, String settingName) {
    Query qry = session.getNamedQuery("org.pentaho.platform.repository.usersettings.pojo.UserSetting.getUserSetting").setParameter("username", username) //$NON-NLS-1$ //$NON-NLS-2$
        .setParameter("settingName", settingName).setCacheable(true); //$NON-NLS-1$
    List settings = qry.list();
    for (int i = 0; i < settings.size(); i++) {
      IUserSetting setting = (IUserSetting) settings.get(i);
      session.delete(setting);
    }
  }

}
