/*
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
 * Copyright 2008 Pentaho Corporation.  All rights reserved.
 *
 * Created Mar 25, 2008
 * @author Michael D'Amour
 */
package org.pentaho.mantle.client.service;

import java.util.ArrayList;
import java.util.HashMap;

import org.pentaho.platform.api.engine.perspective.pojo.IPluginPerspective;
import org.pentaho.platform.api.usersettings.pojo.IUserSetting;
import org.pentaho.ui.xul.XulOverlay;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface MantleServiceAsync {
  public void isAuthenticated(AsyncCallback<Boolean> callback);
  public void isAdministrator(AsyncCallback<Boolean> callback);
  public void deleteContentItem(String contentId, AsyncCallback<Boolean> callback);

  public void getSoftwareUpdatesDocument(AsyncCallback<String> callback);

  // file api
  public void getAllUsers(AsyncCallback<ArrayList<String>> callback);
  public void getAllRoles(AsyncCallback<ArrayList<String>> callback);
  public void doesSolutionRepositorySupportPermissions(AsyncCallback<Boolean> callback);
  public void hasAccess(String path, String fileName, int actionOperation, AsyncCallback<Boolean> callback);
  
  // mantle settings
  public void getMantleSettings(AsyncCallback<HashMap<String,String>> callback);

  // version information
  public void getVersion(AsyncCallback<String> callback);

  // For New Analysis View
  public void getMondrianCatalogs(AsyncCallback<HashMap<String,ArrayList<String[]>>> callback);
  
  // user settings
  public void getUserSettings(AsyncCallback<ArrayList<IUserSetting>> callback);
  public void setLocaleOverride(String locale, AsyncCallback<Void> callback);
  // generic user setting getter/setters
  public void setUserSetting(String settingName, String settingValue, AsyncCallback<Void> callback);
  public void getUserSetting(String settingName, AsyncCallback<IUserSetting> callback);
  public void setShowNavigator(boolean showNavigator, AsyncCallback<Void> callback);
  public void setShowLocalizedFileNames(boolean showLocalizedFileNames, AsyncCallback<Void> callback);
  public void setShowHiddenFiles(boolean showHiddenFiles, AsyncCallback<Void> callback);
  public void repositorySupportsACLS(AsyncCallback<Boolean> callback);
  public void getOverlays(AsyncCallback<ArrayList<XulOverlay>> callback);
  public void getPluginPerpectives(AsyncCallback<ArrayList<IPluginPerspective>> callback);
}
