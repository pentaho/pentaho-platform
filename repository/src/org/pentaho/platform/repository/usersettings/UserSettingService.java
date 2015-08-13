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
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.repository.usersettings;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.usersettings.IUserSettingService;
import org.pentaho.platform.api.usersettings.pojo.IUserSetting;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.repository.usersettings.pojo.UserSetting;
import org.pentaho.platform.repository2.ClientRepositoryPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class UserSettingService implements IUserSettingService {

  public static final String SETTING_PREFIX = "_USERSETTING"; //$NON-NLS-1$
  IPentahoSession session = null;
  private static final byte[] lock = new byte[0];

  protected IUnifiedRepository repository;
  private Logger log = LoggerFactory.getLogger( getClass() );

  public UserSettingService( IUnifiedRepository repository ) {
    this.repository = repository;
  }

  public void init( IPentahoSession session ) {
    this.session = session;
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // GENERIC/ADMIN METHODS
  // ////////////////////////////////////////////////////////////////////////////////////////////////

  // delete all settings for a given user
  public void deleteUserSettings() {
    String homePath = ClientRepositoryPaths.getUserHomeFolderPath( PentahoSessionHolder.getSession().getName() );
    Serializable id = repository.getFile( homePath ).getId();

    Map<String, Serializable> fileMetadata = repository.getFileMetadata( id );
    Map<String, Serializable> finalMetadata = new HashMap<String, Serializable>( fileMetadata.size() );
    for ( Map.Entry<String, Serializable> entry : fileMetadata.entrySet() ) {
      String key = entry.getKey();
      if ( !key.startsWith( SETTING_PREFIX ) ) {
        finalMetadata.put( key, entry.getValue() );
      }
    }
    repository.setFileMetadata( id, finalMetadata );
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // USER SETTINGS METHODS
  // ////////////////////////////////////////////////////////////////////////////////////////////////

  private static UserSetting createSetting( String name, Serializable value ) {
    return createSetting( name, value.toString() );
  }

  private static UserSetting createSetting( String name, String value ) {
    UserSetting setting = new UserSetting();
    setting.setSettingName( name );
    setting.setSettingValue( value );
    return setting;
  }

  public List<IUserSetting> getUserSettings() {
    // get the global settings and the user settings
    // merge unseen global settings into the user settings list
    List<IUserSetting> userSettings = new ArrayList<IUserSetting>();

    String tentantHomePath = ClientRepositoryPaths.getEtcFolderPath();
    Serializable tenantHomeId = repository.getFile( tentantHomePath ).getId();
    Map<String, Serializable> tenantMetadata = repository.getFileMetadata( tenantHomeId );

    for ( Map.Entry<String, Serializable> entry : tenantMetadata.entrySet() ) {
      String key = entry.getKey();
      if ( key.startsWith( SETTING_PREFIX ) ) {
        UserSetting setting = createSetting( key.substring( SETTING_PREFIX.length() ), entry.getValue() );
        userSettings.add( setting );
      }
    }

    String homePath = ClientRepositoryPaths.getUserHomeFolderPath( PentahoSessionHolder.getSession().getName() );
    Serializable userHomeId = repository.getFile( homePath ).getId();
    Map<String, Serializable> userMetadata = repository.getFileMetadata( userHomeId );

    for ( Map.Entry<String, Serializable> entry : userMetadata.entrySet() ) {
      String key = entry.getKey();
      if ( key.startsWith( SETTING_PREFIX ) ) {
        UserSetting setting = createSetting( key.substring( SETTING_PREFIX.length() ), entry.getValue() );
        // see if a global setting exists which will be overridden
        if ( userSettings.contains( setting ) ) {
          userSettings.remove( setting );
        }
        userSettings.add( setting );
      }
    }
    return userSettings;
  }

  public IUserSetting getUserSetting( String settingName, String defaultValue ) {
    // if the user does not have the setting, check if a global setting exists
    boolean hasAuth = PentahoSessionHolder.getSession().getAttribute( "SPRING_SECURITY_CONTEXT" ) != null;
    if ( hasAuth ) {
      try {
        String homePath = ClientRepositoryPaths.getUserHomeFolderPath( PentahoSessionHolder.getSession().getName() );

        Serializable userHomeId = repository.getFile( homePath ).getId();
        Map<String, Serializable> userMetadata = repository.getFileMetadata( userHomeId );

        for ( Map.Entry<String, Serializable> entry : userMetadata.entrySet() ) {
          String key = entry.getKey();
          if ( key.startsWith( SETTING_PREFIX ) ) {
            String settingFromKey = key.substring( SETTING_PREFIX.length() );
            if ( settingFromKey.equals( settingName ) ) {
              return createSetting( settingFromKey, entry.getValue() );
            }
          }
        }

        String tentantHomePath = ClientRepositoryPaths.getEtcFolderPath();
        Serializable tenantHomeId = repository.getFile( tentantHomePath ).getId();
        Map<String, Serializable> tenantMetadata = repository.getFileMetadata( tenantHomeId );

        for ( Map.Entry<String, Serializable> entry : tenantMetadata.entrySet() ) {
          String key = entry.getKey();
          if ( key.startsWith( SETTING_PREFIX ) ) {
            String settingFromKey = key.substring( SETTING_PREFIX.length() );
            if ( settingFromKey.equals( settingName ) ) {
              return createSetting( settingFromKey, entry.getValue() );
            }
          }
        }
      } catch ( Throwable ignored ) {
        // if anything goes wrong with authentication (anonymous user) or permissions
        // just return the default value, if we continue to log these errors (like on before Login)
        // we'll see *many* errors in the logs which are not helpful
      }
    }
    return createSetting( settingName, defaultValue );
  }

  public void setUserSetting( String settingName, String settingValue ) {

    String name = PentahoSessionHolder.getSession().getName();
    String homePath = ClientRepositoryPaths.getUserHomeFolderPath( name );

    synchronized ( lock ) {

      final Serializable id = repository.getFile( homePath ).getId();

      final Map<String, Serializable> fileMetadata = repository.getFileMetadata( id );
      fileMetadata.put( SETTING_PREFIX + settingName, settingValue );
      try {
        SecurityHelper.getInstance().runAsSystem( new Callable<Void>() {
          @Override
          public Void call() throws Exception {
            repository.setFileMetadata( id, fileMetadata );
            return null;
          }
        } );
      } catch ( Exception e ) {
        if ( log.isDebugEnabled() ) {
          log.debug( "Error storing user setting for user: " + name + ", setting: " + settingName + ", value: "
            + settingValue, e );
        }
        log.error( "Error storing user setting", e );
      }
    }
  }

  // ////////////////////////////////////////////////////////////////////////////////////////////////
  // GLOBAL USER SETTINGS METHODS
  // ////////////////////////////////////////////////////////////////////////////////////////////////

  public IUserSetting getGlobalUserSetting( String settingName, String defaultValue ) {
    String tentantHomePath = ClientRepositoryPaths.getEtcFolderPath();
    Serializable tenantHomeId = repository.getFile( tentantHomePath ).getId();
    Map<String, Serializable> tenantMetadata = repository.getFileMetadata( tenantHomeId );

    String key = SETTING_PREFIX + settingName;
    Serializable value = tenantMetadata.get( key );
    if ( value != null ) {
      return createSetting( settingName, value.toString() );
    }

    return createSetting( settingName, defaultValue );
  }

  public List<IUserSetting> getGlobalUserSettings() {

    String tentantHomePath = ClientRepositoryPaths.getEtcFolderPath();
    Serializable tenantHomeId = repository.getFile( tentantHomePath ).getId();
    Map<String, Serializable> tenantMetadata = repository.getFileMetadata( tenantHomeId );

    List<IUserSetting> userSettings = new ArrayList<IUserSetting>( tenantMetadata.size() );
    for ( Map.Entry<String, Serializable> entry : tenantMetadata.entrySet() ) {
      String key = entry.getKey();
      if ( key.startsWith( SETTING_PREFIX ) ) {
        userSettings.add( createSetting( key.substring( SETTING_PREFIX.length() ), entry.getValue() ) );
      }
    }
    return userSettings;
  }

  public void setGlobalUserSetting( String settingName, String settingValue ) {
    if ( SecurityHelper.getInstance().isPentahoAdministrator( session ) ) {
      String tentantHomePath = ClientRepositoryPaths.getEtcFolderPath();
      Serializable tenantHomeId = repository.getFile( tentantHomePath ).getId();
      Map<String, Serializable> tenantMetadata = repository.getFileMetadata( tenantHomeId );
      tenantMetadata.put( SETTING_PREFIX + settingName, settingValue );
      repository.setFileMetadata( tenantHomeId, tenantMetadata );
    }
  }

}
