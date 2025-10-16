/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.platform.web.http.api.resources.utils;

import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository2.unified.ServerRepositoryPaths;
import org.pentaho.platform.repository2.unified.jcr.JcrTenantUtils;
import org.pentaho.platform.security.policy.rolebased.actions.AdministerSecurityAction;
import org.pentaho.platform.security.policy.rolebased.actions.PublishAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryCreateAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryReadAction;
import org.pentaho.platform.util.StringUtil;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

public class SystemUtils {
  public static boolean canAdminister() {
    IAuthorizationPolicy policy = PentahoSystem.get( IAuthorizationPolicy.class );
    return policy.isAllowed( RepositoryReadAction.NAME )
      && policy.isAllowed( RepositoryCreateAction.NAME )
      && policy.isAllowed( AdministerSecurityAction.NAME );
  }

  /**
   * Checks if the current user can upload to the specified directory.<br>
   * Existence and write permission checks on the upload directory are performed.
   *
   * @param uploadDir The directory to check upload permissions for.
   * @return {@code true} if the user can upload to the specified directory, {@code false} otherwise.
   */
  public static boolean canUpload( String uploadDir ) {
    return canUpload( uploadDir, false );
  }

  /**
   * Checks if the current user can upload to the specified directory.<br>
   * If {@code ignoreUploadDirExistenceChecks} is {@code true}, the existence and write permission checks on
   * the upload directory are skipped.
   *
   * @param uploadDir The directory to check upload permissions for.
   * @param ignoreUploadDirExistenceChecks If {@code true}, skips existence and write permission checks
   *                                      on the upload directory.
   * @return {@code true} if the user can upload to the specified directory, {@code false} otherwise.
   */
  public static boolean canUpload( String uploadDir, boolean ignoreUploadDirExistenceChecks ) {
    // Repo and File Write Access checks
    IAuthorizationPolicy policy = PentahoSystem.get( IAuthorizationPolicy.class );
    if ( !( policy.isAllowed( RepositoryReadAction.NAME ) && policy.isAllowed( RepositoryCreateAction.NAME ) ) ) {
      return false;
    }

    if ( !ignoreUploadDirExistenceChecks && !StringUtil.isEmpty( uploadDir ) ) {
      IUnifiedRepository repo = PentahoSystem.get( IUnifiedRepository.class );
      // check if upload dir is a folder
      RepositoryFile file = repo.getFile( uploadDir );
      if ( file == null || !file.isFolder() ) {
        return false;
      }

      // validate if the user has write permission to the upload dir
      if ( !repo.hasAccess( uploadDir, EnumSet.of( RepositoryFilePermission.WRITE ) ) ) {
        return false;
      }
    }

    // Action-specific checks

    // check if the user is admin or has publish permission or the user is uploading to the home folder
    return policy.isAllowed( AdministerSecurityAction.NAME )
      || policy.isAllowed( PublishAction.NAME )
      || validateAccessToHomeFolder( uploadDir );
  }

  public static boolean canDownload( String path ) {
    // Repo and File Read Access checks

    // NOTE: RepositoryCreateAction should not really be required for reading or downloading files.
    IAuthorizationPolicy policy = PentahoSystem.get( IAuthorizationPolicy.class );
    if ( !( policy.isAllowed( RepositoryReadAction.NAME ) && policy.isAllowed( RepositoryCreateAction.NAME ) ) ) {
      return false;
    }

    if ( !StringUtil.isEmpty( path ) ) {
      IUnifiedRepository repo = PentahoSystem.get( IUnifiedRepository.class );
      // validate if the user has read permission to the path
      if ( !repo.hasAccess( path, EnumSet.of( RepositoryFilePermission.READ ) ) ) {
        return false;
      }
    }

    // Action-specific checks

    // check if the user is admin or has download role or is downloading from their home folder
    return policy.isAllowed( AdministerSecurityAction.NAME )
      || hasDownloadRole()
      || validateAccessToHomeFolder( path );
  }

  private static boolean hasDownloadRole() {
    IUserRoleListService userRoleListService = PentahoSystem.get( IUserRoleListService.class );
    String tenantedUserName = PentahoSessionHolder.getSession().getName();
    List<String> tenantedUserRoles = userRoleListService.getRolesForUser(
      JcrTenantUtils.getUserNameUtils().getTenant( tenantedUserName ),
      tenantedUserName
    );

    return !Collections.disjoint( tenantedUserRoles, PentahoSystem.getDownloadRolesList() );
  }

  public static boolean validateAccessToHomeFolder( String path ) {
    if ( StringUtil.isEmpty( path ) ) {
      return false;
    }

    IAuthorizationPolicy policy = PentahoSystem.get( IAuthorizationPolicy.class );
    if ( !( policy.isAllowed( RepositoryReadAction.NAME ) && policy.isAllowed( RepositoryCreateAction.NAME ) ) ) {
      return false;
    }

    String tenantedUserName = PentahoSessionHolder.getSession().getName();
    //get user home folder path
    String userHomeFolderPath = ServerRepositoryPaths
      .getUserHomeFolderPath( JcrTenantUtils.getUserNameUtils().getTenant( tenantedUserName ),
        JcrTenantUtils.getUserNameUtils().getPrincipleName( tenantedUserName ) );

    if ( StringUtil.isEmpty( userHomeFolderPath ) ) {
      return false;
    }

    // path is a relative path so prefix it with the tenant root folder path
    String fullPath = ServerRepositoryPaths.getTenantRootFolderPath() + path;

    // check if the full path is the user home folder path
    if ( fullPath.equals( userHomeFolderPath ) ) {
      return true;
    }

    // check if the full path is a subpath of the user home folder path
    return fullPath.startsWith( userHomeFolderPath + RepositoryFile.SEPARATOR );
  }
}
