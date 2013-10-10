/*!
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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.scheduler2.quartz;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository.IClientRepositoryPathsStrategy;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.usersettings.IUserSettingService;
import org.pentaho.platform.api.usersettings.pojo.IUserSetting;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.security.SecurityHelper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author Rowell Belen
 */
public class SchedulerOutputPathResolver {

  final String DEFAULT_SETTING_KEY = "default-scheduler-output-path";
  public static final String SCHEDULER_ACTION_NAME = "org.pentaho.scheduler.manage";

  private static final Log logger = LogFactory.getLog( SchedulerOutputPathResolver.class );
  private static final List<RepositoryFilePermission> permissions = new ArrayList<RepositoryFilePermission>();

  private String jobName;
  private String outputDirectory;
  private String actionUser;

  static {
    // initialize permissions
    permissions.add( RepositoryFilePermission.READ );
    permissions.add( RepositoryFilePermission.WRITE );
  }

  public SchedulerOutputPathResolver( final String outputPathPattern, final String actionUser ) {
    this.jobName = FilenameUtils.getBaseName( outputPathPattern );
    this.outputDirectory = FilenameUtils.getPathNoEndSeparator( outputPathPattern );
    this.actionUser = actionUser;
  }

  public String resolveOutputFilePath() {

    final String fileNamePattern = "/" + this.jobName + ".*";
    final String outputFilePath = "/" + this.outputDirectory;

    // Enclose validation logic in the context of the job creator's session, not the current session
    final Callable<String> callable = new Callable<String>() {
      @Override
      public String call() throws Exception {

        if ( StringUtils.isNotBlank( outputFilePath ) && isValidOutputPath( outputFilePath )
            && isPermitted( outputFilePath ) ) {
          return outputFilePath + fileNamePattern; // return if valid
        }

        // evaluate fallback output paths
        String[] fallBackPaths = new String[] { getUserSettingOutputPath(), // user setting
          getSystemSettingOutputPath(), // system setting
          getUserHomeDirectoryPath() // home directory
        };

        for ( String path : fallBackPaths ) {
          if ( StringUtils.isNotBlank( path ) && isValidOutputPath( path ) ) {
            return path + fileNamePattern; // return the first valid path
          }
        }

        return null; // it should never reach here because the user directory is the ultimate fallback
      }
    };

    return runAsUser( callable );
  }

  private String runAsUser( Callable<String> callable ) {
    try {
      if ( callable != null ) {
        return SecurityHelper.getInstance().runAsUser( this.actionUser, callable );
      }
    } catch ( Exception e ) {
      logger.error( e.getMessage(), e );
    }

    return null;
  }

  private boolean isValidOutputPath( String path ) {
    try {
      RepositoryFile repoFile = getRepository().getFile( path );
      if ( repoFile != null && repoFile.isFolder() && isScheduleAllowed( repoFile.getId() ) ) {
        return true;
      }
    } catch ( Exception e ) {
      logger.warn( e.getMessage(), e );
    }
    return false;
  }

  private String getUserSettingOutputPath() {
    try {
      IUserSetting userSetting = getUserSettingService().getUserSetting( DEFAULT_SETTING_KEY, null );
      if ( userSetting != null && StringUtils.isNotBlank( userSetting.getSettingValue() ) ) {
        return userSetting.getSettingValue();
      }
    } catch ( Exception e ) {
      logger.warn( e.getMessage(), e );
    }
    return null;
  }

  private String getSystemSettingOutputPath() {
    try {
      return PentahoSystem.getSystemSettings().getSystemSetting( DEFAULT_SETTING_KEY, null );
    } catch ( Exception e ) {
      logger.warn( e.getMessage(), e );
    }
    return null;
  }

  private String getUserHomeDirectoryPath() {
    try {
      IClientRepositoryPathsStrategy pathsStrategy =
          PentahoSystem.get( IClientRepositoryPathsStrategy.class, getScheduleCreatorSession() );
      return pathsStrategy.getUserHomeFolderPath( getScheduleCreatorSession().getName() );
    } catch ( Exception e ) {
      logger.warn( e.getMessage(), e );
    }
    return null;
  }

  private IPentahoSession getScheduleCreatorSession() {
    return PentahoSessionHolder.getSession();
  }

  private IUnifiedRepository getRepository() {
    return PentahoSystem.get( IUnifiedRepository.class, getScheduleCreatorSession() );
  }

  private IUserSettingService getUserSettingService() {
    return PentahoSystem.get( IUserSettingService.class, getScheduleCreatorSession() );
  }

  private IAuthorizationPolicy getAuthorizationPolicy() {
    return PentahoSystem.get( IAuthorizationPolicy.class, getScheduleCreatorSession() );
  }

  private boolean isScheduleAllowed( final Serializable repositoryId ) {
    boolean canSchedule = false;
    canSchedule = getAuthorizationPolicy().isAllowed( SCHEDULER_ACTION_NAME );
    if ( canSchedule ) {
      Map<String, Serializable> metadata = getRepository().getFileMetadata( repositoryId );
      if ( metadata.containsKey( "_PERM_SCHEDULABLE" ) ) {
        canSchedule = Boolean.parseBoolean( (String) metadata.get( "_PERM_SCHEDULABLE" ) );
      }
    }

    return canSchedule;
  }

  private boolean isPermitted( final String path ) {
    try {
      return getRepository().hasAccess( path, EnumSet.copyOf( permissions ) );
    } catch ( Exception e ) {
      logger.warn( e.getMessage(), e );
    }
    return false;
  }
}
