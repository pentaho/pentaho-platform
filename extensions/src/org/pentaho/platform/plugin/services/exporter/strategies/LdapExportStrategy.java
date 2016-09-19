package org.pentaho.platform.plugin.services.exporter.strategies;

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
 * Copyright (c) 2002-2016 Pentaho Corporation..  All rights reserved.
 */

import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.usersettings.IAnyUserSettingService;
import org.pentaho.platform.api.usersettings.IUserSettingService;
import org.pentaho.platform.api.usersettings.pojo.IUserSetting;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.TenantUtils;
import org.pentaho.platform.plugin.services.importexport.ExportManifestUserSetting;
import org.pentaho.platform.plugin.services.importexport.UserExport;
import org.pentaho.platform.plugin.services.importexport.exportManifest.ExportManifest;

import java.util.List;

/**
 * Strategy that exports users' roles from LDAP
 *
 * @author Andrei Abramov
 */
public class LdapExportStrategy implements IExportStrategy {
  @Override
  public void exportUsersAndRoles( ExportManifest exportManifest ) {

    IUserRoleListService userRoleListService = PentahoSystem.get( IUserRoleListService.class );
    ITenant tenant = TenantUtils.getCurrentTenant();

    //  get the user settings for this user
    IUserSettingService service = PentahoSystem.get( IUserSettingService.class, PentahoSessionHolder.getSession() );

    doUserExport( exportManifest, userRoleListService, tenant, service );
    doGlobalUserSettingsExport( exportManifest, service );
    doRoleExport( exportManifest, userRoleListService );
  }

  // export the global user settings
  private void doGlobalUserSettingsExport( ExportManifest exportManifest, IUserSettingService service ) {
    if ( service != null ) {
      List<IUserSetting> globalUserSettings = service.getGlobalUserSettings();
      if ( globalUserSettings != null ) {
        for ( IUserSetting setting : globalUserSettings ) {
          exportManifest.addGlobalUserSetting( new ExportManifestUserSetting( setting ) );
        }
      }
    }
  }

  //RoleExport
  private void doRoleExport( ExportManifest exportManifest, IUserRoleListService userRoleListService ) {
    List<String> roles = userRoleListService.getAllRoles();

    ExportStrategyUtil.exportRoles( exportManifest, roles );
  }

  //User Export
  private void doUserExport( ExportManifest exportManifest, IUserRoleListService userRoleListService, ITenant tenant,
                             IUserSettingService service ) {
    List<String> userList = userRoleListService.getAllUsers( tenant );
    for ( String user : userList ) {
      UserExport userExport = new UserExport();
      userExport.setUsername( user );

      for ( String role : userRoleListService.getRolesForUser( tenant, user ) ) {
        userExport.setRole( role );
      }

      if ( service != null && service instanceof IAnyUserSettingService ) {
        IAnyUserSettingService userSettings = (IAnyUserSettingService) service;
        List<IUserSetting> settings = userSettings.getUserSettings( user );
        if ( settings != null ) {
          for ( IUserSetting setting : settings ) {
            userExport.addUserSetting( new ExportManifestUserSetting( setting ) );
          }
        }
      }

      exportManifest.addUserExport( userExport );
    }
  }
}
