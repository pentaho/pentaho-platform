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

import java.util.*;
import org.pentaho.mantle.client.MantleXulOverlay;
import org.pentaho.mantle.client.objects.SimpleMessageException;
import org.pentaho.mantle.client.objects.SolutionFileInfo;
import org.pentaho.mantle.client.objects.SubscriptionSchedule;
import org.pentaho.mantle.client.objects.SubscriptionState;
import org.pentaho.mantle.client.objects.WorkspaceContent;
import org.pentaho.platform.api.usersettings.pojo.IUserSetting;

import com.google.gwt.user.client.rpc.RemoteService;

public interface MantleService extends RemoteService {
  public boolean isAuthenticated();
  public boolean isAdministrator();
  public boolean cancelBackgroundJob(String jobName, String jobGroup);
  public boolean deleteContentItem(String contentId);
  
  public String getSoftwareUpdatesDocument();
  
  // admin
  public void executeGlobalActions();
  public String refreshMetadata();
  public void refreshSystemSettings();
  public void refreshRepository();
  public int cleanContentRepository(int daysBack);
  public void flushMondrianSchemaCache();
  public void purgeReportingDataCache();

  //schedule API
  public void createCronJob(String solutionName, String path, String actionName, String triggerName, String triggerGroup, String description, String cronExpression) throws SimpleMessageException;
  public void createSimpleTriggerJob(String triggerName, String triggerGroup, String description, Date strStartDate, Date strEndDate, int repeatCount, int strRepeatInterval,
      String solutionName, String path, String actionName) throws SimpleMessageException;
  public void suspendJob(String jobName, String jobGroup);
  public void resumeJob(String jobName, String jobGroup);
  public void deleteJob(String jobName, String jobGroup);
  public void runJob(String jobName, String jobGroup);
  
  //subscriptions API
  public Boolean isSubscriptionContent(String actionRef);
  public ArrayList<SubscriptionSchedule> getAvailableSubscriptionSchedules(String actionRef);
  public ArrayList<SubscriptionSchedule> getAppliedSubscriptionSchedules(String actionRef);
  public void setSubscriptions(String solutionName, String solutionPath, String fileName, boolean enabled, ArrayList<SubscriptionSchedule> currentSchedules);
  public SubscriptionState getSubscriptionState(String solutionName, String solutionPath, String fileName);
  public String deleteSubscriptionArchive(String subscriptionName, String fileId);
  public String deletePublicScheduleAndContents(String currSubscr, ArrayList<String> fileItemArrayList);    
  public String runAndArchivePublicSchedule(String publicScheduleName) throws SimpleMessageException;
  public WorkspaceContent getWorkspaceContent();
  
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
  
  public ArrayList<MantleXulOverlay> getOverlays();
  
  public Map<String, String> getSystemThemes();
  public String getActiveTheme();
  public void setTheme(String theme) throws SimpleMessageException;

}
