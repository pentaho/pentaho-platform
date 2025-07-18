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

  public static boolean canUpload( String uploadDir ) {
    IAuthorizationPolicy policy = PentahoSystem.get( IAuthorizationPolicy.class );
    // check if the user is admin or has a publish action role assigned
    if ( policy.isAllowed( RepositoryReadAction.NAME )
      && policy.isAllowed( RepositoryCreateAction.NAME )
      && ( policy.isAllowed( AdministerSecurityAction.NAME ) || policy.isAllowed( PublishAction.NAME ) ) ) {
      return true;
    }

    if ( StringUtil.isEmpty( uploadDir ) ) {
      return false;
    }

    IUnifiedRepository repo = PentahoSystem.get( IUnifiedRepository.class );
    // validate if the user has write permission
    if ( !repo.hasAccess( uploadDir, EnumSet.of( RepositoryFilePermission.WRITE ) ) ) {
      return false;
    }

    // check if the folder exists
    RepositoryFile file = repo.getFile( uploadDir );
    if ( file == null || !file.isFolder() ) {
      return false;
    }

    // the user does not have admin or publish action role assigned
    // check if the user uploads to their home folder
    return validateAccessToHomeFolder( uploadDir );
  }

  public static boolean canDownload( String path ) {
    IUserRoleListService userRoleListService = PentahoSystem.get( IUserRoleListService.class );
    String tenantedUserName = PentahoSessionHolder.getSession().getName();
    List<String> tenantedUserRoles = userRoleListService.getRolesForUser(
      JcrTenantUtils.getUserNameUtils().getTenant( tenantedUserName ),
      tenantedUserName
    );

    IAuthorizationPolicy policy = PentahoSystem.get( IAuthorizationPolicy.class );
    // check if the user is admin or has a download role assigned
    if ( policy.isAllowed( RepositoryReadAction.NAME )
      && policy.isAllowed( RepositoryCreateAction.NAME )
      && ( policy.isAllowed( AdministerSecurityAction.NAME )
        || !Collections.disjoint( tenantedUserRoles, PentahoSystem.getDownloadRolesList() ) ) ) {
      return true;
    }

    if ( StringUtil.isEmpty( path ) ) {
      return false;
    }

    IUnifiedRepository repo = PentahoSystem.get( IUnifiedRepository.class );
    // validate if the user has read permission
    if ( !repo.hasAccess( path, EnumSet.of( RepositoryFilePermission.READ ) ) ) {
      return false;
    }

    // the user does not have admin or a download role assigned
    // check if the user downloads from their home folder
    return validateAccessToHomeFolder( path );
  }

  public static boolean validateAccessToHomeFolder( String dir ) {
    if ( StringUtil.isEmpty( dir ) ) {
      return false;
    }

    IAuthorizationPolicy policy = PentahoSystem.get( IAuthorizationPolicy.class );
    if ( !( policy.isAllowed( RepositoryReadAction.NAME ) && policy.isAllowed( RepositoryCreateAction.NAME ) ) ) {
      return false;
    }

    String tenantedUserName = PentahoSessionHolder.getSession().getName();
    // get the user home folder path
    String userHomeFolderPath = ServerRepositoryPaths
      .getUserHomeFolderPath( JcrTenantUtils.getUserNameUtils().getTenant( tenantedUserName ),
        JcrTenantUtils.getUserNameUtils().getPrincipleName( tenantedUserName ) );

    if ( StringUtil.isEmpty( userHomeFolderPath ) ) {
      return false;
    }

    // dir is a relative path so prefix it with the tenant root folder path
    String dirFullPath = ServerRepositoryPaths.getTenantRootFolderPath() + dir;

    // check if dir full path is the user home folder path
    if ( dirFullPath.equals( userHomeFolderPath ) ) {
      return true;
    }

    // check if dir full path is a subpath of the user home folder path
    return dirFullPath.startsWith( userHomeFolderPath + RepositoryFile.SEPARATOR );
  }
}
