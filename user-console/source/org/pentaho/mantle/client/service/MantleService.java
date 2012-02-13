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
import java.util.Map;

import org.pentaho.mantle.client.objects.SimpleMessageException;
import org.pentaho.platform.api.engine.perspective.pojo.IPluginPerspective;
import org.pentaho.platform.api.usersettings.pojo.IUserSetting;
import org.pentaho.ui.xul.XulOverlay;
import com.google.gwt.user.client.rpc.RemoteService;

public interface MantleService extends RemoteService {
  public boolean isAuthenticated();
  public boolean isAdministrator();
  public boolean deleteContentItem(String contentId);
  
  public String getSoftwareUpdatesDocument();
  
  // admin
  public void executeGlobalActions();
  public String refreshMetadata();
  public void refreshSystemSettings();
  public void refreshRepository();
  public void flushMondrianSchemaCache();
  public void purgeReportingDataCache();

  // file api
  public ArrayList<String> getAllUsers();
  public ArrayList<String> getAllRoles();
  public boolean doesSolutionRepositorySupportPermissions();
  public boolean hasAccess(String path, String fileName, int actionOperation);
  
  // mantle settings
  public HashMap<String,String> getMantleSettings();

  // version information
  public String getVersion();
  
  // For New Analysis View
  public HashMap<String,ArrayList<String[]>> getMondrianCatalogs();  
  
  // user settings
  public ArrayList<IUserSetting> getUserSettings();
  public void setLocaleOverride(String locale);
  // generic user settings
  public void setUserSetting(String settingName, String settingValue) throws SimpleMessageException;
  public IUserSetting getUserSetting(String settingName) throws SimpleMessageException;
  public void setShowNavigator(boolean showNavigator);
  public void setShowLocalizedFileNames(boolean showLocalizedFileNames);
  public void setShowHiddenFiles(boolean showHiddenFiles);
  
  public boolean repositorySupportsACLS();
  
  public ArrayList<XulOverlay> getOverlays();
  public ArrayList<IPluginPerspective> getPluginPerpectives();
  
  public Map<String, String> getSystemThemes();
  public String getActiveTheme();
  public void setTheme(String theme) throws SimpleMessageException;

}
