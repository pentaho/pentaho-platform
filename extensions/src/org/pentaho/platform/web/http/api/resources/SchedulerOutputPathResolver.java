package org.pentaho.platform.web.http.api.resources;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.usersettings.IUserSettingService;
import org.pentaho.platform.api.usersettings.pojo.IUserSetting;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository.RepositoryFilenameUtils;
import org.pentaho.platform.repository2.ClientRepositoryPaths;

/**
 * @author Rowell Belen
 */
public class SchedulerOutputPathResolver {

  final String DEFAULT_SETTING_KEY = "default-scheduler-output-path";

  private static final Log logger = LogFactory.getLog(SchedulerOutputPathResolver.class);

  private IUnifiedRepository repository = PentahoSystem.get(IUnifiedRepository.class);
  private IPentahoSession pentahoSession = PentahoSessionHolder.getSession();

  private IUserSettingService getSettingsService() {
    if(settingsService == null) {
      settingsService = PentahoSystem.get(IUserSettingService.class, pentahoSession);
    }
    return settingsService;
  }

  private IUserSettingService settingsService;
  private JobScheduleRequest scheduleRequest;

  public SchedulerOutputPathResolver(JobScheduleRequest scheduleRequest){
    this.scheduleRequest = scheduleRequest;
  }

  public String resolveOutputFilePath(){

    String fileName = RepositoryFilenameUtils.getBaseName(scheduleRequest.getInputFile()); // default file name
    if (!StringUtils.isEmpty(scheduleRequest.getJobName())) {
      fileName = scheduleRequest.getJobName(); // use job name as file name if exists
    }
    String fileNamePattern = "/" + fileName + ".*";

    String outputFilePath = scheduleRequest.getOutputFile();
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
      IUserSetting userSetting = getSettingsService().getUserSetting(DEFAULT_SETTING_KEY, null);
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
      return ClientRepositoryPaths.getUserHomeFolderPath(pentahoSession.getName());
    }
    catch(Exception e){
      logger.warn(e.getMessage(), e);
    }
    return null;
  }

}
