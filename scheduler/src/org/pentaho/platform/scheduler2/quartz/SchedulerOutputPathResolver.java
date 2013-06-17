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
 * Copyright 2005 - 2008 Pentaho Corporation.  All rights reserved.
 *
 * @created Jun 23, 2005
 * @author Marc Batchelor
 *
 */

package org.pentaho.platform.scheduler2.quartz;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository.IClientRepositoryPathsStrategy;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.usersettings.IUserSettingService;
import org.pentaho.platform.api.usersettings.pojo.IUserSetting;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;

/**
 * @author Rowell Belen
 */
public class SchedulerOutputPathResolver {

  final String DEFAULT_SETTING_KEY = "default-scheduler-output-path";

  private static final Log logger = LogFactory.getLog(SchedulerOutputPathResolver.class);

  private IUnifiedRepository repository = PentahoSystem.get(IUnifiedRepository.class);
  private IPentahoSession pentahoSession = PentahoSessionHolder.getSession();
  private IUserSettingService settingsService = null;

  private String jobName;
  private String outputDirectory;

  public SchedulerOutputPathResolver(String outputPathPattern){
    this.jobName = FilenameUtils.getBaseName(outputPathPattern);
    this.outputDirectory = FilenameUtils.getPathNoEndSeparator(outputPathPattern);
    this.settingsService = PentahoSystem.get(IUserSettingService.class, pentahoSession);
  }

  public String resolveOutputFilePath(){

    final String fileNamePattern = "/" + this.jobName + ".*";

    final String outputFilePath = "/" + this.outputDirectory;
    if(StringUtils.isNotBlank(outputFilePath) && isValidOutputPath(outputFilePath)){
      return outputFilePath + fileNamePattern; // return if valid
    }

    // evaluate fallback output paths
    String[] fallBackPaths = new String[]{
       getUserSettingOutputPath(),    // user setting
       getSystemSettingOutputPath(),  // system setting
       getUserHomeDirectoryPath()     // home directory
    };

    for(String path : fallBackPaths){
      if(StringUtils.isNotBlank(path) && isValidOutputPath(path)){
        return path + fileNamePattern; // return the first valid path
      }
    }

    return null; // it should never reach here
  }

  private boolean isValidOutputPath(String path){
    try {
      RepositoryFile repoFile = repository.getFile(path);
      if(repoFile != null && repoFile.isFolder()){
        return true;
      }
    }
    catch (Exception e){
      logger.warn(e.getMessage(), e);
    }
    return false;
  }

  private String getUserSettingOutputPath(){
    try {
      IUserSetting userSetting = settingsService.getUserSetting(DEFAULT_SETTING_KEY, null);
      if(userSetting != null && StringUtils.isNotBlank(userSetting.getSettingValue())){
        return userSetting.getSettingValue();
      }
    }
    catch(Exception e){
      logger.warn(e.getMessage(), e);
    }
    return null;
  }

  private String getSystemSettingOutputPath(){
    try {
      return PentahoSystem.getSystemSettings().getSystemSetting(DEFAULT_SETTING_KEY, null);
    }
    catch(Exception e){
      logger.warn(e.getMessage(), e);
    }
    return null;
  }

  private String getUserHomeDirectoryPath(){
    try {
      IClientRepositoryPathsStrategy pathsStrategy = PentahoSystem.get(IClientRepositoryPathsStrategy.class, pentahoSession);
      return pathsStrategy.getUserHomeFolderPath(pentahoSession.getName());
    }
    catch(Exception e){
      logger.warn(e.getMessage(), e);
    }
    return null;
  }

}
