package org.pentaho.platform.web.http.api.resources;

import org.apache.commons.lang.StringUtils;
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

  private IUnifiedRepository repository = PentahoSystem.get(IUnifiedRepository.class);
  private IPentahoSession pentahoSession = PentahoSessionHolder.getSession();
  private IUserSettingService settingsService = PentahoSystem.get(IUserSettingService.class, pentahoSession);
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
    RepositoryFile repoFile = repository.getFile(path);
    if(repoFile != null && repoFile.isFolder()){
      return true;
    }

    return false;
  }

  private String getUserSettingOutputPath(){
    IUserSetting userSetting = settingsService.getUserSetting(DEFAULT_SETTING_KEY, null);
    if(userSetting != null && StringUtils.isNotBlank(userSetting.getSettingValue())){
      return userSetting.getSettingValue();
    }

    return null;
  }

  private String getSystemSettingOutputPath(){
    return PentahoSystem.getSystemSettings().getSystemSetting(DEFAULT_SETTING_KEY, null);
  }

  private String getUserHomeDirectoryPath(){
    return ClientRepositoryPaths.getUserHomeFolderPath(pentahoSession.getName());
  }

}
