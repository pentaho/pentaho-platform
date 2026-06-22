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
 */

package org.pentaho.platform.plugin.services.importer.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.pentaho.platform.api.engine.ISystemConfig;
import org.pentaho.platform.api.importexport.IImportHelper;
import org.pentaho.platform.api.importexport.ImportException;
import org.pentaho.platform.api.engine.security.userroledao.IUserRoleDao;
import org.pentaho.platform.api.engine.security.userroledao.AlreadyExistsException;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.core.mt.Tenant;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.TenantUtils;
import org.pentaho.platform.plugin.services.importexport.ComponentConfig;
import org.pentaho.platform.plugin.services.importexport.ExportManifestUserSetting;
import org.pentaho.platform.plugin.services.importexport.ImportExportMetrics;
import org.pentaho.platform.plugin.services.importexport.RoleExport;
import org.pentaho.platform.plugin.services.importexport.UserExport;
import org.pentaho.platform.plugin.services.importexport.exportManifest.ExportManifest;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.platform.plugin.services.importer.SolutionImportHandler;
import org.pentaho.platform.security.policy.rolebased.IRoleAuthorizationPolicyRoleBindingDao;
import org.pentaho.platform.api.usersettings.IAnyUserSettingService;
import org.pentaho.platform.api.usersettings.pojo.IUserSetting;
import org.pentaho.platform.api.usersettings.IUserSettingService;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoRole;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoUser;

/**
 * Import helper for users and roles restoration.
 * Handles importing user accounts, roles, and role-to-user mappings from backup.
 *
 * Profile: USERS
 * Filters: isIncludeUsers()
 */
public class UsersAndRolesImportHelper implements IImportHelper {

  private SolutionImportHandler solutionImportHandler;

  @Override
  public String getName() {
    return "Users and Roles Import Helper";
  }

  @Override
  public boolean shouldExecute( Object config ) {
    if ( config == null ) {
      return true; // Full restore - include all content
    }
    if ( config instanceof ComponentConfig ) {
      return ( ( ComponentConfig ) config ).isIncludeUsers();
    }
    return true; // Unknown type - default to include
  }

  @Override
  public void doImport( Object importArg ) throws ImportException {
    solutionImportHandler = (SolutionImportHandler) importArg;
    if ( !shouldExecute( solutionImportHandler.getImportSession().getComponentOverrides() ) ) {
      return;
    }
    try {
      // Read property from system/security.properties
      ISystemConfig systemConfig = PentahoSystem.get( ISystemConfig.class );
      String provider = "jackrabbit";
      if ( systemConfig != null ) {
        provider = systemConfig.getProperty( "security.provider",  "jackrabbit");
      }

      if ( !provider.equalsIgnoreCase( "jackrabbit" ) ) {
        solutionImportHandler.getLogger().info( "Nothing to import from users and roles as the authentication is external");
        return;
      }

      ExportManifest manifest = solutionImportHandler.getImportSession().getManifest();

      if ( manifest == null ) {
        if ( solutionImportHandler.isPerformingRestore() ) {
          solutionImportHandler.getLogger().debug( "Manifest is null - skipping users and roles import" );
        }
        return;
      }

      if ( solutionImportHandler.isPerformingRestore() ) {
        solutionImportHandler.getLogger().info( "Starting users and roles import..." );
      }

      try {
        Map<String, List<String>> roleToUserMap = importUsers( manifest.getUserExports(), solutionImportHandler );
        // Import the roles
        importRoles( manifest.getRoleExports(), roleToUserMap, solutionImportHandler );
        
        // Import global user settings
        importGlobalUserSettings( manifest.getGlobalUserSettings(), solutionImportHandler );

        if ( solutionImportHandler.isPerformingRestore() ) {
          solutionImportHandler.getLogger().info( "Successfully completed users and roles import" );
        }
      } catch ( Exception e ) {
        if ( solutionImportHandler.isPerformingRestore() ) {
          solutionImportHandler.getLogger().error( "Failed to import users and roles: " + e.getMessage() );
          solutionImportHandler.getLogger().debug( "Users and roles import error", e );
        }
        throw new ImportException( "Failed to import users and roles: " + e.getMessage(), e );
      }
    } catch ( Exception e ) {
      if ( solutionImportHandler.isPerformingRestore() ) {
        solutionImportHandler.getLogger().error( "Users and roles import helper error: " + e.getMessage() );
      }
      throw new ImportException( "Users and roles import helper failed: " + e.getMessage(), e );
    }
  }

  /**
   * Import users into the platform.
   * Internal implementation extracted from SolutionImportHandler.
   */
  public Map<String, List<String>> importUsers( List<UserExport> users, SolutionImportHandler handler ) {
    Map<String, List<String>> roleToUserMap = new HashMap<>();
    int successFullUserImportCount = 0;
    int newUsersCreated = 0;
    int existingUsersSkipped = 0;
    int userFailedCount = 0;
    
    if ( handler.isPerformingRestore() ) {
      handler.getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_START_IMPORT_USER" ) );
    }
    if ( users != null ) {
      if ( handler.isPerformingRestore() ) {
        handler.getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_COUNT_USER", users.size() ) );
      }
      for ( UserExport user : users ) {
        int importResult = importUserAndRoleWithTracking( user.getUsername(), user, roleToUserMap, this.solutionImportHandler );
        if ( importResult > 0 ) {
          successFullUserImportCount++;
          if ( importResult == 1 ) {
            // User was newly created
            newUsersCreated++;
          } else if ( importResult == 2 ) {
            // User already existed and was skipped
            existingUsersSkipped++;
          }
        } else {
          // User import failed
          userFailedCount++;
        }
      }
    }
    
    if ( handler.isPerformingRestore() ) {
      handler.getLogger().info( "User import summary - Total: " + (users != null ? users.size() : 0) + 
        ", Created: " + newUsersCreated + ", Existing (skipped): " + existingUsersSkipped + ", Failed: " + userFailedCount );
      
      // Track user imports in metrics with detailed breakdown
      ImportExportMetrics metrics = handler.getMetrics();
      if ( metrics != null ) {
        for ( int i = 0; i < newUsersCreated; i++ ) {
          metrics.recordSuccess( ImportExportMetrics.Category.USERS );
        }
        for ( int i = 0; i < existingUsersSkipped; i++ ) {
          metrics.recordSuccess( ImportExportMetrics.Category.USERS );
        }
        for ( int i = 0; i < userFailedCount; i++ ) {
          metrics.recordFailure( ImportExportMetrics.Category.USERS, "user", "Import failed" );
        }
      }
      
      handler.getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_SUCCESSFUL_USER_COUNT", successFullUserImportCount, users != null ? users.size() : 0 ) );
      handler.getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_END_IMPORT_USER" ) );
    }
    return roleToUserMap;
  }

  /**
   * Import a single user with tracking of whether it was newly created or already existed.
   */
  public int importUserAndRoleWithTracking( String username, UserExport user, Map<String, List<String>> roleToUserMap, SolutionImportHandler handler ) {
    // Check if user exists BEFORE import attempt
    boolean userExistedBeforeImport = false;
    IUserRoleDao roleDao = PentahoSystem.get( IUserRoleDao.class );
    if ( roleDao != null ) {
      try {
        ITenant tenant = new Tenant( "/pentaho/" + TenantUtils.getDefaultTenant(), true );
        IPentahoUser existingUser = roleDao.getUser( tenant, username );
        if ( existingUser != null ) {
          userExistedBeforeImport = true;
        }
      } catch ( Exception e ) {
        // If we can't check, assume user doesn't exist
        if ( ( handler != null ? handler : solutionImportHandler ).isPerformingRestore() ) {
          ( handler != null ? handler : solutionImportHandler ).getLogger().debug( "Could not check if user [ " + username + " ] existed before import: " + e.getMessage() );
        }
      }
    }
    
    // Attempt import
    boolean result = importUserAndRole( username, user, roleToUserMap, handler );
    
    if ( result ) {
      if ( userExistedBeforeImport ) {
        // User already existed, so it was skipped
        if ( ( handler != null ? handler : solutionImportHandler ).isPerformingRestore() ) {
          ( handler != null ? handler : solutionImportHandler ).getLogger().debug( "User [ " + username + " ] already existed, skipped import" );
        }
        return 2; // Existing (skipped)
      } else {
        // User did not exist before, so it was newly created
        if ( ( handler != null ? handler : solutionImportHandler ).isPerformingRestore() ) {
          ( handler != null ? handler : solutionImportHandler ).getLogger().debug( "User [ " + username + " ] was newly created" );
        }
        return 1; // Newly created
      }
    } else {
      // Import failed
      if ( ( handler != null ? handler : solutionImportHandler ).isPerformingRestore() ) {
        ( handler != null ? handler : solutionImportHandler ).getLogger().debug( "User [ " + username + " ] import failed" );
      }
      return 0; // Failed
    }
  }

  /**
   * Import a single user with their roles and settings.
   */
  private boolean importUserAndRole( String username, UserExport user, Map<String, List<String>> roleToUserMap, SolutionImportHandler handler ) {
    // Handler is required - cannot proceed without it
    if ( handler == null ) {
      return false;
    }
    
    IUserRoleDao roleDao = PentahoSystem.get( IUserRoleDao.class );
    if ( roleDao == null ) {
      handler.getLogger().warn( "Unable to import user [ " + username + " ] - IUserRoleDao not available" );
      return false;
    }
    
    ITenant tenant = new Tenant( "/pentaho/" + TenantUtils.getDefaultTenant(), true );
    
    // Check if user already exists
    try {
      IPentahoUser existingUser = roleDao.getUser( tenant, username );
      if ( existingUser != null ) {
        if ( handler.isPerformingRestore() ) {
          handler.getLogger().debug( "User [ " + username + " ] already exists, skipping import" );
        }
        
        // Still need to map the user to their roles for role binding later
        for ( String role : user.getRoles() ) {
          List<String> userList;
          if ( !roleToUserMap.containsKey( role ) ) {
            userList = new ArrayList<>();
            roleToUserMap.put( role, userList );
          } else {
            userList = roleToUserMap.get( role );
          }
          userList.add( username );
        }
        return true; // User exists, treat as success
      }
    } catch ( Exception e ) {
      // User doesn't exist, proceed with import
      if ( handler.isPerformingRestore() ) {
        handler.getLogger().debug( "User [ " + username + " ] does not exist or error checking existence: " + e.getMessage() );
      }
    }
    
    // User doesn't exist, import it
    String password = user.getPassword();
    handler.getLogger().debug( Messages.getInstance().getString( "USER.importing", username ) );

    // map the user to the roles he/she is in
    for ( String role : user.getRoles() ) {
      List<String> userList;
      if ( !roleToUserMap.containsKey( role ) ) {
        userList = new ArrayList<>();
        roleToUserMap.put( role, userList );
      } else {
        userList = roleToUserMap.get( role );
      }
      userList.add( username );
    }

    String[] userRoles = user.getRoles().toArray( new String[] {} );
    try {
      if ( handler.isPerformingRestore() ) {
        handler.getLogger().debug( "Restoring user [ " + username + " ] " );
      }
      roleDao.createUser( tenant, username, password, null, userRoles );
      if ( handler.isPerformingRestore() ) {
        handler.getLogger().debug( "Successfully restored user [ " + username + " ]" );
      }
    } catch ( AlreadyExistsException e ) {
      // it's ok if the user already exists, it is probably a default user
      handler.getLogger().debug( Messages.getInstance().getString( "USER.Already.Exists", username ) );
      // User was just created but this exception thrown anyway - still treat as success
      return true;
    } catch ( Exception e ) {
      handler.getLogger().debug( Messages.getInstance()
          .getString( "ERROR.OverridingExistingUser", username ), e );
      handler.getLogger().error( Messages.getInstance()
          .getString( "ERROR.OverridingExistingUser", username ) );
      return false;
    }
    if ( handler.isPerformingRestore() ) {
      handler.getLogger().debug( "Restoring user [ " + username + " ] specific settings" );
    }
    importUserSettings( user, handler );
    if ( handler.isPerformingRestore() ) {
      handler.getLogger().debug( "Successfully restored user [ " + username + " ] specific settings" );
    }
    return true;
  }

  /**
   * Import user-specific settings for a user.
   * Internal implementation extracted from SolutionImportHandler.
   */
  public void importUserSettings( UserExport user, SolutionImportHandler handler ) {
    if ( handler == null ) {
      return;  // Cannot import settings without handler context
    }
    IUserSettingService settingService = PentahoSystem.get( IUserSettingService.class );
    IAnyUserSettingService userSettingService = null;
    int userSettingsListSize = 0;
    int successfulUserSettingsImportCount = 0;
    if ( settingService != null && settingService instanceof IAnyUserSettingService ) {
      userSettingService = (IAnyUserSettingService) settingService;
    }
    if ( handler.isPerformingRestore() ) {
      handler.getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_START_IMPORT_USER_SETTING" ) );
    }
    if ( userSettingService != null ) {
      List<ExportManifestUserSetting> exportedSettings = user.getUserSettings();
      userSettingsListSize = user.getUserSettings().size();
      if ( handler.isPerformingRestore() ) {
        handler.getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_COUNT_USER_SETTING", userSettingsListSize, user.getUsername() ) );
      }
      for ( ExportManifestUserSetting exportedSetting : exportedSettings ) {
        try {
          if ( handler.isPerformingRestore() ) {
            handler.getLogger().debug( "Restore user specific setting  [ " + exportedSetting.getName() + " ]" );
          }
          if ( handler.isOverwriteFile() ) {
            if ( handler.isPerformingRestore() ) {
              handler.getLogger().debug( "Overwrite is set to true. So restoring setting  [ " + exportedSetting.getName() + " ]" );
            }
            userSettingService.setUserSetting( user.getUsername(),
                exportedSetting.getName(), exportedSetting.getValue() );
            if ( handler.isPerformingRestore() ) {
              handler.getLogger().debug( "Finished restore of user specific setting with name [ " + exportedSetting.getName() + " ]" );
            }
            successfulUserSettingsImportCount++;
          } else {
            // see if it's there first before we set this setting
            if ( handler.isPerformingRestore() ) {
              handler.getLogger().debug( "Overwrite is set to false. Only restore setting  [ " + exportedSetting.getName() + " ] if is does not exist" );
            }
            IUserSetting userSetting =
                userSettingService.getUserSetting( user.getUsername(), exportedSetting.getName(), null );
            if ( userSetting == null ) {
              // only set it if we didn't find that it exists already
              userSettingService.setUserSetting( user.getUsername(), exportedSetting.getName(), exportedSetting.getValue() );
              if ( handler.isPerformingRestore() ) {
                handler.getLogger().debug( "Finished restore of user specific setting with name [ " + exportedSetting.getName() + " ]" );
              }
              successfulUserSettingsImportCount++;
            }
          }
          if ( handler.isPerformingRestore() ) {
            handler.getLogger().debug( "Successfully restored setting  [ " + exportedSetting.getName() + " ]" );
          }
        } catch ( Exception e ) {
          handler.getLogger().warn( "Failed to import user setting [ " + exportedSetting.getName() + " ] for user [ " + user.getUsername() + " ]: " + e.getMessage() );
          handler.getLogger().debug( "User setting error", e );
          // Continue with next setting even if this one fails
        }
      }
      if ( handler.isPerformingRestore() ) {
        handler.getLogger().info( Messages.getInstance()
            .getString( "SolutionImportHandler.INFO_SUCCESSFUL_USER_SETTING_IMPORT_COUNT", successfulUserSettingsImportCount, userSettingsListSize ) );
        handler.getLogger().info( Messages.getInstance()
            .getString( "SolutionImportHandler.INFO_END_IMPORT_USER_SETTING" ) );
      }
    }
  }

  /**
   * Import roles into the platform.
   * Internal implementation extracted from SolutionImportHandler.
   */
  public void importRoles( List<RoleExport> roles, Map<String, List<String>> roleToUserMap, SolutionImportHandler handler ) {
    if ( handler.isPerformingRestore() ) {
      handler.getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_START_IMPORT_ROLE" ) );
    }
    if ( roles != null ) {
      IUserRoleDao roleDao = PentahoSystem.get( IUserRoleDao.class );
      ITenant tenant = new Tenant( "/pentaho/" + TenantUtils.getDefaultTenant(), true );
      IRoleAuthorizationPolicyRoleBindingDao roleBindingDao = PentahoSystem.get(
          IRoleAuthorizationPolicyRoleBindingDao.class );

      Set<String> existingRoles = new HashSet<>();
      int newRolesCreated = 0;
      int existingRolesSkipped = 0;
      int rolesWithPermissionsUpdated = 0;
      int roleFailedCount = 0;
      
      if ( handler.isPerformingRestore() ) {
        handler.getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_COUNT_ROLE", roles.size() ) );
      }
      int successFullRoleImportCount = 0;
      for ( RoleExport role : roles ) {
        handler.getLogger().debug( Messages.getInstance().getString( "ROLE.importing", role.getRolename() ) );
        
        // Check if role already exists before attempting to create
        boolean roleExists = false;
        try {
          IPentahoRole existingRole = roleDao.getRole( tenant, role.getRolename() );
          if ( existingRole != null ) {
            roleExists = true;
            existingRoles.add( role.getRolename() );
            existingRolesSkipped++;
            if ( handler.isPerformingRestore() ) {
              handler.getLogger().debug( "Role [ " + role.getRolename() + " ] already exists (will skip creation)" );
            }
          }
        } catch ( Exception e ) {
          // Role doesn't exist, proceed with creation
          if ( handler.isPerformingRestore() ) {
            handler.getLogger().debug( "Role [ " + role.getRolename() + " ] does not exist or error checking existence: " + e.getMessage() );
          }
        }
        
        // Only create role if it doesn't already exist
        if ( !roleExists ) {
          try {
            List<String> users = roleToUserMap.get( role.getRolename() );
            String[] userarray = users == null ? new String[] {} : users.toArray( new String[] {} );
            IPentahoRole role1 = roleDao.createRole( tenant, role.getRolename(), null, userarray );
            newRolesCreated++;
            successFullRoleImportCount++;
            if ( handler.isPerformingRestore() ) {
              handler.getLogger().debug( "Role [ " + role.getRolename() + " ] created successfully" );
            }
          } catch ( AlreadyExistsException e ) {
            existingRoles.add( role.getRolename() );
            existingRolesSkipped++;
            successFullRoleImportCount++; // Treat existing role as successful
            if ( handler.isPerformingRestore() ) {
              handler.getLogger().debug( "Role [ " + role.getRolename() + " ] already exists (caught as AlreadyExistsException)" );
            }
          } catch ( Exception e ) {
            roleFailedCount++;
            handler.getLogger().error( "Failed to create role [ " + role.getRolename() + " ]: " + e.getMessage(), e );
            // Continue with next role even if creation fails
            continue;
          }
        } else {
          // Role already exists, count it as processed
          successFullRoleImportCount++;
        }
        try {
          if ( existingRoles.contains( role.getRolename() ) ) {
            //Only update an existing role if the overwrite flag is set
            if ( handler.isOverwriteFile() ) {
              if ( handler.isPerformingRestore() ) {
                handler.getLogger().debug( "Overwrite is set to true. Updating permissions for role [ " + role.getRolename() + "]" );
              }
              roleBindingDao.setRoleBindings( tenant, role.getRolename(), role.getPermissions() );
              rolesWithPermissionsUpdated++;
              if ( handler.isPerformingRestore() ) {
                handler.getLogger().debug( "Permissions updated for role [ " + role.getRolename() + "]" );
              }
            } else {
              if ( handler.isPerformingRestore() ) {
                handler.getLogger().debug( "Overwrite is false. Skipping permission update for existing role [ " + role.getRolename() + "]" );
              }
            }
          } else {
            if ( handler.isPerformingRestore() ) {
              handler.getLogger().debug( "Updating role mapping from runtime roles to logical roles for [ " + role.getRolename() + "]" );
            }
            //Always write a roles permissions that were not previously existing
            roleBindingDao.setRoleBindings( tenant, role.getRolename(), role.getPermissions() );
            if ( handler.isPerformingRestore() ) {
              handler.getLogger().debug( "Permissions set for new role [ " + role.getRolename() + "]" );
            }
          }
        } catch ( Exception e ) {
          handler.getLogger().error( Messages.getInstance()
              .getString( "ERROR.SettingRolePermissions", role.getRolename() ), e );
          // Continue with next role even if permission setting fails
        }
      }
      if ( handler.isPerformingRestore() ) {
        handler.getLogger().info( "Role import summary - Total: " + roles.size() + 
          ", Created: " + newRolesCreated + ", Existing (skipped): " + existingRolesSkipped + 
          ", Permissions Updated: " + rolesWithPermissionsUpdated + ", Failed: " + roleFailedCount );
        
        // Track role imports in metrics with detailed breakdown
        ImportExportMetrics metrics = handler.getMetrics();
        if ( metrics != null ) {
          for ( int i = 0; i < newRolesCreated; i++ ) {
            metrics.recordSuccess( ImportExportMetrics.Category.ROLES );
          }
          for ( int i = 0; i < existingRolesSkipped; i++ ) {
            metrics.recordSuccess( ImportExportMetrics.Category.ROLES );
          }
          for ( int i = 0; i < roleFailedCount; i++ ) {
            metrics.recordFailure( ImportExportMetrics.Category.ROLES, "role", "Import failed" );
          }
        }
        
        handler.getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_SUCCESSFUL_ROLE_COUNT", successFullRoleImportCount, roles.size() ) );
      }
    }
    if ( handler.isPerformingRestore() ) {
      handler.getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_END_IMPORT_ROLE" ) );
    }
  }

  /**
   * Import global user settings into the platform.
   * Handles importing settings that apply to all users.
   */
  public void importGlobalUserSettings( List<ExportManifestUserSetting> globalSettings, SolutionImportHandler handler ) {
    if ( solutionImportHandler == null ) {
      solutionImportHandler = handler;
    }

    if ( solutionImportHandler.isPerformingRestore() ) {
      solutionImportHandler.getLogger().debug( "[Start: Restore global user settings]" );
    }
    
    IUserSettingService settingService = PentahoSystem.get( IUserSettingService.class );
    int successfulGlobalSettingsCount = 0;
    int totalGlobalSettingsCount = globalSettings != null ? globalSettings.size() : 0;
    
    if ( settingService != null && globalSettings != null ) {
      for ( ExportManifestUserSetting globalSetting : globalSettings ) {
        try {
          if ( solutionImportHandler.isOverwriteFile() ) {
            if ( solutionImportHandler.isPerformingRestore() ) {
              solutionImportHandler.getLogger().trace( "Overwrite flag is set to true. Setting global user setting [ " + globalSetting.getName() + " ]" );
            }
            settingService.setGlobalUserSetting( globalSetting.getName(), globalSetting.getValue() );
            successfulGlobalSettingsCount++;
            if ( solutionImportHandler.isPerformingRestore() ) {
              solutionImportHandler.getLogger().debug( "Successfully set global user setting [ " + globalSetting.getName() + " ]" );
            }
          } else {
            if ( solutionImportHandler.isPerformingRestore() ) {
              solutionImportHandler.getLogger().trace( "Overwrite flag is set to false. Only setting [ " + globalSetting.getName() + " ] if does not exist" );
            }
            IUserSetting userSetting = settingService.getGlobalUserSetting( globalSetting.getName(), null );
            if ( userSetting == null ) {
              settingService.setGlobalUserSetting( globalSetting.getName(), globalSetting.getValue() );
              successfulGlobalSettingsCount++;
              if ( solutionImportHandler.isPerformingRestore() ) {
                solutionImportHandler.getLogger().debug( "Successfully set global user setting [ " + globalSetting.getName() + " ]" );
              }
            }
          }
        } catch ( Exception e ) {
          solutionImportHandler.getLogger().warn( "Failed to set global user setting [ " + globalSetting.getName() + " ]: " + e.getMessage() );
          solutionImportHandler.getLogger().debug( "Global setting error", e );
          // Continue with next setting even if this one fails
        }
      }
    }
    
    if ( solutionImportHandler.isPerformingRestore() ) {
      if ( totalGlobalSettingsCount > 0 ) {
        solutionImportHandler.getLogger().debug( "Completed restore of global user settings: " + successfulGlobalSettingsCount + "/" + totalGlobalSettingsCount + " successful" );
      }
      solutionImportHandler.getLogger().debug( "[End: Restore global user settings]" );
    }
  }

  /**
   * Import a user by username from the manifest.
   * This method finds the user in the export manifest, imports the user with their roles.
   *
   * @param username the username of the schedule owner to import
   * @param manifest the export manifest containing user and role information
   * @return true if user was imported successfully or already exists, false if user not found or import failed
   */
  public boolean importUserAndRole( String username, ExportManifest manifest, SolutionImportHandler handler ) {
    if ( username == null || username.trim().isEmpty() || manifest == null || handler == null ) {
      return false;
    }

    try {
      // Find the user in the manifest
      UserExport scheduleOwnerUser = null;
      List<UserExport> users = manifest.getUserExports();
      if ( users != null ) {
        for ( UserExport user : users ) {
          if ( username.equals( user.getUsername() ) ) {
            scheduleOwnerUser = user;
            break;
          }
        }
      }

      if ( scheduleOwnerUser == null ) {
        if ( handler.isPerformingRestore() ) {
          handler.getLogger().debug( "User [ " + username + " ] not found in export manifest" );
        }
        return false;
      }

      if ( handler.isPerformingRestore() ) {
        handler.getLogger().debug( "Importing user [ " + username + " ] from manifest" );
      }

      // Create a map to track role-to-user mappings
      Map<String, List<String>> roleToUserMap = new HashMap<>();

      // Import the user (creates home folder if needed)
      boolean userImported = importUserAndRole( username, scheduleOwnerUser, roleToUserMap, handler );

      if ( !userImported ) {
        if ( handler.isPerformingRestore() ) {
          handler.getLogger().warn( "Failed to import user [ " + username + " ]" );
        }
        return false;
      }

      if ( handler.isPerformingRestore() ) {
        handler.getLogger().debug( "Successfully user [ " + username + " ]" );
      }

      // Find and import the user's roles
      List<RoleExport> rolesToImport = new ArrayList<>();
      List<RoleExport> allRoles = manifest.getRoleExports();
      if ( allRoles != null && !roleToUserMap.isEmpty() ) {
        for ( RoleExport role : allRoles ) {
          if ( roleToUserMap.containsKey( role.getRolename() ) ) {
            rolesToImport.add( role );
          }
        }
      }

      // Import the roles
      if ( !rolesToImport.isEmpty() ) {
        if ( handler.isPerformingRestore() ) {
          handler.getLogger().debug( "Importing [ " + rolesToImport.size() + " ] roles for user [ " + username + " ]" );
        }
        importRoles( rolesToImport, roleToUserMap, handler );
      }

      if ( handler.isPerformingRestore() ) {
        handler.getLogger().info( "Successfully completed import of user [ " + username + " ] and their roles" );
      }

      return true;
    } catch ( Exception e ) {
      if ( handler.isPerformingRestore() ) {
        handler.getLogger().error( "Error importing user [ " + username + " ]: " + e.getMessage(), e );
      }
      return false;
    }
  }
}
