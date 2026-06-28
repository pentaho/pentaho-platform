/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.platform.plugin.services.exporter.helper;

import org.castor.core.util.Assert;
import org.pentaho.platform.api.engine.ISystemConfig;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.importexport.ExportException;
import org.pentaho.platform.api.importexport.IExportHelper;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.usersettings.IAnyUserSettingService;
import org.pentaho.platform.api.usersettings.IUserSettingService;
import org.pentaho.platform.api.usersettings.pojo.IUserSetting;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.TenantUtils;
import org.pentaho.platform.plugin.services.exporter.PentahoPlatformExporter;
import org.pentaho.platform.plugin.services.importexport.ComponentConfig;
import org.pentaho.platform.plugin.services.importexport.ExportManifestUserSetting;
import org.pentaho.platform.plugin.services.importexport.ImportExportMetrics;
import org.pentaho.platform.plugin.services.importexport.RoleExport;
import org.pentaho.platform.plugin.services.importexport.UserExport;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.platform.security.policy.rolebased.IRoleAuthorizationPolicyRoleBindingDao;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Export helper for users and roles.
 * Contains all user and role export logic, including:
 * - User export with settings
 * - Role export with permissions
 * - Schedule owner selective export
 */
public class UsersAndRolesExportHelper implements IExportHelper {
  private IUserSettingService userSettingService;

  @Override
  public String getName() {
    return "UsersAndRolesExporter";
  }


  public boolean shouldExecute( Object config ) {
    if ( config instanceof ComponentConfig ) {
      return ( ( ComponentConfig ) config ).isIncludeUsers();
    }
    return false;
  }

  @Override
  public void doExport( Object exportArg ) throws ExportException {
    Assert.notNull( exportArg, "PentahoPlatformExporter is expected to be not null");
    PentahoPlatformExporter exporter = (PentahoPlatformExporter) exportArg;

    Object config = exporter.getComponentConfig();
    if ( !shouldExecute( config ) ) {
      return;
    }
    
    try {
      // Read property from system/security.properties
      ISystemConfig systemConfig = PentahoSystem.get( ISystemConfig.class );
      String provider = "jackrabbit";
      if ( systemConfig != null ) {
        provider = systemConfig.getProperty( "security.provider",  "jackrabbit");
      }
      if ( provider.equalsIgnoreCase( "jackrabbit" ) ) {
        exportUsersAndRoles( exporter );
      } else {
        // External authentication provider (jdbc/ldap): user and role objects are managed
        // externally and are not exported. However, the runtime-to-logical role bindings live in
        // the Pentaho repository, so we still export them so they can be restored.
        exporter.getRepositoryExportLogger().info(
            "Authentication is external - exporting role mappings (runtime-to-logical roles) only" );
        exportRoles( exporter );
      }

      if ( exporter.getExportMetrics() != null ) {
        exporter.getExportMetrics().recordSuccess( ImportExportMetrics.Category.USERS );
      }
    } catch ( Exception e ) {
      if ( exporter.getExportMetrics() != null ) {
        exporter.getExportMetrics().recordFailure( ImportExportMetrics.Category.USERS, "users", e );
      }
      throw new ExportException( "Failed to export users and roles: " + e.getMessage(), e );
    }
  }

  /**
   * Export all users and their roles
   */
  protected void exportUsersAndRoles( PentahoPlatformExporter exporter ) {
    exporter.getRepositoryExportLogger().info( Messages.getInstance().getString( "PentahoPlatformExporter.INFO_START_EXPORT_USER" ) );
    int successfulExportUsers = 0;
    int usersSize = 0;

    IUserRoleListService userRoleListService = PentahoSystem.get( IUserRoleListService.class );
    ITenant tenant = TenantUtils.getCurrentTenant();

    // User Export
    List<String> userList = userRoleListService.getAllUsers( tenant );
    if ( userList != null ) {
      usersSize = userList.size();
      exporter.getRepositoryExportLogger().info( Messages.getInstance().getString( "PentahoPlatformExporter.INFO_COUNT_USER_TO_EXPORT", usersSize ) );
      if ( exporter.getMetricsCollector() != null ) {
        exporter.getMetricsCollector().addUsers( usersSize );
      }
    }

    // Export each user and their roles
    for ( String user : userList ) {
      if ( exportUserAndRole( user, exporter ) ) {
        successfulExportUsers++;
      }
    }

    // export the global user settings
    IUserSettingService service = getUserSettingService();
    if ( service != null ) {
      exporter.getRepositoryExportLogger().debug( "Starting backup of global user settings" );
      List<IUserSetting> globalUserSettings = service.getGlobalUserSettings();
      if ( globalUserSettings != null ) {
        for ( IUserSetting setting : globalUserSettings ) {
          exporter.getExportManifest().addGlobalUserSetting( new ExportManifestUserSetting( setting ) );
        }
      }
      exporter.getRepositoryExportLogger().debug( "Finished backup of global user settings" );
    }
    exporter.getRepositoryExportLogger().info( Messages.getInstance().getString( "PentahoPlatformExporter.INFO_SUCCESSFUL_USER_EXPORT_COUNT", successfulExportUsers, usersSize ) );

    exporter.getRepositoryExportLogger().info( Messages.getInstance().getString( "PentahoPlatformExporter.INFO_END_EXPORT_USER" ) );

    // Export roles
    exportRoles( exporter );
  }

  /**
   * Export a single user and their roles.
   * Public method to allow external callers (e.g.,  IPentahoPlatformExporter stub)
   * to export individual users as dependencies.
   * 
   * @param username the username to export
   * @return true if the user was successfully exported, false otherwise
   */
  public boolean exportUserAndRole( String username, PentahoPlatformExporter exporter ) {
    if ( username == null || username.trim().isEmpty() ) {
      return false;
    }

    UserDetailsService userDetailsService = PentahoSystem.get( UserDetailsService.class );
    IUserRoleListService userRoleListService = PentahoSystem.get( IUserRoleListService.class );
    IRoleAuthorizationPolicyRoleBindingDao roleBindingDao = PentahoSystem.get(
        IRoleAuthorizationPolicyRoleBindingDao.class );
    ITenant tenant = TenantUtils.getCurrentTenant();
    IUserSettingService service = getUserSettingService();

    try {
      exporter.getRepositoryExportLogger().debug( "Starting backup of user [ " + username + " ] " );
      UserExport userExport = new UserExport();
      userExport.setUsername( username );
      userExport.setPassword( userDetailsService.loadUserByUsername( username ).getPassword() );

      // Track roles already in the manifest so we do not add duplicate RoleExport entries.
      Set<String> exportedRoleNames = collectExportedRoleNames( exporter );

      for ( String role : userRoleListService.getRolesForUser( tenant, username ) ) {
        exporter.getRepositoryExportLogger().trace( "user [ " + username + " ] has an associated role [ " + role + " ]" );
        userExport.setRole( role );
        // Also export the runtime-role -> logical-role binding for this role so that the schedule
        // owner's permissions are preserved. Schedules-only backups never run exportRoles(), so
        // without this the role-to-logical-role mapping would be lost on restore.
        exportRoleBinding( role, exporter, roleBindingDao, exportedRoleNames );
      }

      if ( service != null && service instanceof IAnyUserSettingService ) {
        exporter.getRepositoryExportLogger().debug( "Starting backup of user specific settings for user [ " + username + " ] " );
        IAnyUserSettingService userSettings = (IAnyUserSettingService) service;
        List<IUserSetting> settings = userSettings.getUserSettings( username );
        if ( settings != null ) {
          for ( IUserSetting setting : settings ) {
            try {
              exporter.getRepositoryExportLogger().debug( "Adding user specific setting [ "
                  + setting.getSettingName() + " ] with value [ " + setting.getSettingValue() + " ] to backup" );
              userExport.addUserSetting( new ExportManifestUserSetting( setting ) );
              exporter.getRepositoryExportLogger().debug( "Successfully added user specific setting [ "
                  + setting.getSettingName() + " ] with value [ " + setting.getSettingValue() + " ] to backup" );
            } catch ( Exception e ) {
              exporter.getRepositoryExportLogger().warn( "Failed to export user setting [ " + setting.getSettingName() + " ] for user [ " + username + " ]: " + e.getMessage() );
              // Continue with next setting
            }
          }
        }
        exporter.getRepositoryExportLogger().debug( "Finished backup of user specific settings for user [ " + username + " ] " );
      }

      exporter.getExportManifest().addUserExport( userExport );
      if ( exporter.getExportMetrics() != null ) {
        exporter.getExportMetrics().recordSuccess( ImportExportMetrics.Category.USERS );
      }
      exporter.getRepositoryExportLogger().debug( "Successfully perform backup of user [ " + username + " ] " );
      return true;
    } catch ( Exception e ) {
      exporter.getRepositoryExportLogger().error( "Failed to export user [ " + username + " ]: " + e.getMessage(), e );
      if ( exporter.getExportMetrics() != null ) {
        exporter.getExportMetrics().recordFailure( ImportExportMetrics.Category.USERS, username, e );
      }
      return false;
    }
  }

  /**
   * Export only the runtime-to-logical role bindings for the roles assigned to the given user,
   * without exporting the user object itself. Used when the platform is configured with an external
   * authentication provider (jdbc/ldap): user and role objects are managed externally, but their
   * logical-role bindings live in the Pentaho repository and must be backed up so they can be
   * restored.
   *
   * @param username the username whose role mappings should be exported
   * @return true if processed successfully, false otherwise
   */
  public boolean exportUserRoleBindings( String username, PentahoPlatformExporter exporter ) {
    if ( username == null || username.trim().isEmpty() ) {
      return false;
    }
    try {
      IUserRoleListService userRoleListService = PentahoSystem.get( IUserRoleListService.class );
      IRoleAuthorizationPolicyRoleBindingDao roleBindingDao = PentahoSystem.get(
          IRoleAuthorizationPolicyRoleBindingDao.class );
      ITenant tenant = TenantUtils.getCurrentTenant();
      Set<String> exportedRoleNames = collectExportedRoleNames( exporter );
      for ( String role : userRoleListService.getRolesForUser( tenant, username ) ) {
        exporter.getRepositoryExportLogger().debug( "Exporting runtime-to-logical role mapping for role [ "
            + role + " ] of external user [ " + username + " ]" );
        exportRoleBinding( role, exporter, roleBindingDao, exportedRoleNames );
      }
      return true;
    } catch ( Exception e ) {
      exporter.getRepositoryExportLogger().error( "Failed to export role mappings for user [ " + username + " ]: "
          + e.getMessage(), e );
      return false;
    }
  }

  /**
   * Export all roles in the system
   */
  protected void exportRoles( PentahoPlatformExporter exporter ) {
    exporter.getRepositoryExportLogger().info( Messages.getInstance().getString( "PentahoPlatformExporter.INFO_START_EXPORT_ROLE" ) );
    int successfulExportRoles = 0;
    int rolesSize = 0;

    IUserRoleListService userRoleListService = PentahoSystem.get( IUserRoleListService.class );
    IRoleAuthorizationPolicyRoleBindingDao roleBindingDao = PentahoSystem.get(
        IRoleAuthorizationPolicyRoleBindingDao.class );

    // RoleExport
    List<String> roles = userRoleListService.getAllRoles();
    if ( roles != null ) {
      rolesSize = roles.size();
      exporter.getRepositoryExportLogger().info( Messages.getInstance().getString( "PentahoPlatformExporter.INFO_COUNT_ROLE_TO_EXPORT", rolesSize ) );
      if ( exporter.getMetricsCollector() != null ) {
        exporter.getMetricsCollector().addRoles( rolesSize );
      }
    }
    Set<String> exportedRoleNames = collectExportedRoleNames( exporter );
    for ( String role : roles ) {
      try {
        exporter.getRepositoryExportLogger().debug( "Starting backup of role [ " + role + " ] " );
        exportRoleBinding( role, exporter, roleBindingDao, exportedRoleNames );
        successfulExportRoles++;
        if ( exporter.getExportMetrics() != null ) {
          exporter.getExportMetrics().recordSuccess( ImportExportMetrics.Category.ROLES );
        }
        exporter.getRepositoryExportLogger().debug( "Finished backup of role [ " + role + " ] " );
      } catch ( Exception e ) {
        exporter.getRepositoryExportLogger().error( "Failed to export role [ " + role + " ]: " + e.getMessage(), e );
        if ( exporter.getExportMetrics() != null ) {
          exporter.getExportMetrics().recordFailure( ImportExportMetrics.Category.ROLES, role, e );
        }
        // Continue with next role
      }
    }
    exporter.getRepositoryExportLogger().info( Messages.getInstance().getString( "PentahoPlatformExporter.INFO_SUCCESSFUL_ROLE_EXPORT_COUNT", successfulExportRoles, rolesSize ) );

    exporter.getRepositoryExportLogger().info( Messages.getInstance().getString( "PentahoPlatformExporter.INFO_END_EXPORT_ROLE" ) );
  }

  /**
   * Export the runtime-role to logical-role binding for a single role into the manifest, unless it
   * has already been exported. The {@code exportedRoleNames} set avoids adding duplicate
   * {@link RoleExport} entries when the same role is reached from multiple users (schedule-owner
   * dependency export) or from {@link #exportRoles(PentahoPlatformExporter)}.
   *
   * @return {@code true} if a new RoleExport was added, {@code false} if it was skipped
   */
  protected boolean exportRoleBinding( String role, PentahoPlatformExporter exporter,
      IRoleAuthorizationPolicyRoleBindingDao roleBindingDao, Set<String> exportedRoleNames ) {
    if ( role == null || roleBindingDao == null || exportedRoleNames.contains( role ) ) {
      return false;
    }
    RoleExport roleExport = new RoleExport();
    roleExport.setRolename( role );
    roleExport.setPermission( roleBindingDao.getRoleBindingStruct( null ).bindingMap.get( role ) );
    exporter.getExportManifest().addRoleExport( roleExport );
    exportedRoleNames.add( role );
    return true;
  }

  /**
   * Collect the names of roles already present in the export manifest so that role bindings are not
   * exported twice.
   */
  protected Set<String> collectExportedRoleNames( PentahoPlatformExporter exporter ) {
    Set<String> names = new HashSet<>();
    List<RoleExport> existing = exporter.getExportManifest().getRoleExports();
    if ( existing != null ) {
      for ( RoleExport roleExport : existing ) {
        names.add( roleExport.getRolename() );
      }
    }
    return names;
  }

  public IUserSettingService getUserSettingService() {
    if ( userSettingService == null ) {
      userSettingService = PentahoSystem.get( IUserSettingService.class, PentahoSessionHolder.getSession() );
    }
    return userSettingService;
  }

  public void setUserSettingService( IUserSettingService userSettingService ) {
    this.userSettingService = userSettingService;
  }
}
