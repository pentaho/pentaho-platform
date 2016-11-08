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

package org.pentaho.platform.repository2.mt;

import org.pentaho.platform.api.engine.security.userroledao.IPentahoRole;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoUser;
import org.pentaho.platform.api.engine.security.userroledao.IUserRoleDao;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.mt.ITenantManager;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl.Builder;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid.Type;
import org.pentaho.platform.core.mt.Tenant;
import org.pentaho.platform.repository2.unified.IRepositoryFileAclDao;
import org.pentaho.platform.repository2.unified.IRepositoryFileDao;
import org.pentaho.platform.repository2.unified.ServerRepositoryPaths;
import org.pentaho.platform.repository2.unified.jcr.DefaultPathConversionHelper;
import org.pentaho.platform.repository2.unified.jcr.IPathConversionHelper;
import org.pentaho.platform.repository2.unified.jcr.JcrRepositoryFileUtils;
import org.pentaho.platform.repository2.unified.jcr.JcrTenantUtils;
import org.pentaho.platform.repository2.unified.jcr.PentahoJcrConstants;
import org.pentaho.platform.security.policy.rolebased.IRoleAuthorizationPolicyRoleBindingDao;
import org.springframework.util.Assert;

import javax.jcr.ItemNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

public abstract class AbstractRepositoryTenantManager implements ITenantManager {

  public static final String FOLDER_NAME_AUTHZ = ".authz"; //$NON-NLS-1$

  public static final String FOLDER_NAME_ROLEBASED = "roleBased"; //$NON-NLS-1$

  public static final String FOLDER_NAME_RUNTIMEROLES = "runtimeRoles"; //$NON-NLS-1$

  protected IRepositoryFileAclDao repositoryFileAclDao;

  protected IRoleAuthorizationPolicyRoleBindingDao roleBindingDao;

  protected IUserRoleDao userRoleDao;

  protected IRepositoryFileDao repositoryFileDao;

  protected ITenantedPrincipleNameResolver tenantedRoleNameResolver;

  protected ITenantedPrincipleNameResolver tenantedUserNameResolver;

  protected String repositoryAdminUsername;

  protected String tenantAdminRoleName;

  protected String tenantAuthenticatedRoleName;

  protected List<String> singleTenantAuthenticatedAuthorityRoleBindingList;

  protected IPathConversionHelper pathConversionHelper = new DefaultPathConversionHelper();

  protected AbstractRepositoryTenantManager( final IRepositoryFileDao contentDao, final IUserRoleDao userRoleDao,
      final IRepositoryFileAclDao repositoryFileAclDao, IRoleAuthorizationPolicyRoleBindingDao roleBindingDao,
      final String repositoryAdminUsername, final String tenantAuthenticatedAuthorityNamePattern,
      final ITenantedPrincipleNameResolver tenantedUserNameResolver,
      final ITenantedPrincipleNameResolver tenantedRoleNameResolver, final String tenantAdminRoleName,
      final List<String> singleTenantAuthenticatedAuthorityRoleBindingList ) {
    Assert.notNull( contentDao );
    Assert.notNull( repositoryFileAclDao );
    Assert.notNull( roleBindingDao );
    Assert.hasText( repositoryAdminUsername );
    Assert.hasText( tenantAuthenticatedAuthorityNamePattern );
    this.repositoryFileDao = contentDao;
    this.repositoryFileAclDao = repositoryFileAclDao;
    this.userRoleDao = userRoleDao;
    this.roleBindingDao = roleBindingDao;
    this.repositoryAdminUsername = repositoryAdminUsername;
    this.tenantAdminRoleName = tenantAdminRoleName;
    this.tenantAuthenticatedRoleName = tenantAuthenticatedAuthorityNamePattern;
    this.tenantedRoleNameResolver = tenantedRoleNameResolver;
    this.tenantedUserNameResolver = tenantedUserNameResolver;
    this.singleTenantAuthenticatedAuthorityRoleBindingList = singleTenantAuthenticatedAuthorityRoleBindingList;
  }

  public void deleteTenants( Session session, final List<ITenant> tenants ) throws RepositoryException {
    for ( ITenant tenant : tenants ) {
      deleteTenant( session, tenant );
    }
  }

  private void deleteUserRole( Session session, ITenant parentTenant, List<ITenant> tenants )
    throws RepositoryException {
    for ( ITenant tenant : tenants ) {
      deleteUserRole( session, tenant, getChildTenants( session, tenant ) );
    }
    for ( IPentahoRole role : userRoleDao.getRoles( parentTenant ) ) {
      userRoleDao.deleteRole( role );
    }
    for ( IPentahoUser user : userRoleDao.getUsers( parentTenant ) ) {
      userRoleDao.deleteUser( user );
    }
  }

  public void deleteTenant( Session jcrSession, final ITenant tenant ) throws RepositoryException {
    deleteUserRole( jcrSession, tenant, getChildTenants( jcrSession, tenant ) );
    repositoryFileDao.permanentlyDeleteFile( getTenantRootFolder( jcrSession, tenant ).getId(), "tenant delete" );
  }

  public void enableTenant( Session session, final ITenant tenant, final boolean enable ) throws ItemNotFoundException,
    RepositoryException {
    Map<String, Serializable> fileMeta =
        JcrRepositoryFileUtils.getFileMetadata( session, getTenantRootFolder( session, tenant ).getId() );
    fileMeta.put( ITenantManager.TENANT_ENABLED, enable );
    JcrRepositoryFileUtils.setFileMetadata( session, getTenantRootFolder( session, tenant ).getId(), fileMeta );
  }

  @Override
  public RepositoryFile getTenantRootFolder( ITenant tenant ) {
    RepositoryFile rootFolder = repositoryFileDao.getFileByAbsolutePath( tenant.getRootFolderAbsolutePath() );
    if ( rootFolder != null ) {
      Map<String, Serializable> metadata = repositoryFileDao.getFileMetadata( rootFolder.getId() );
      if ( !metadata.containsKey( ITenantManager.TENANT_ROOT ) || !(Boolean)
        metadata.get( ITenantManager.TENANT_ROOT ) ) {
        rootFolder = null;
      }
    }
    return rootFolder;
  }

  private RepositoryFile getTenantRootFolder( Session session, final ITenant tenant ) throws RepositoryException {
    RepositoryFile rootFolder =
        JcrRepositoryFileUtils.getFileByAbsolutePath( session, tenant.getRootFolderAbsolutePath(),
            pathConversionHelper, null, false, null );
    if ( rootFolder != null ) {
      Map<String, Serializable> metadata = JcrRepositoryFileUtils.getFileMetadata( session, rootFolder.getId() );
      if ( !metadata.containsKey( ITenantManager.TENANT_ROOT )
        || !(Boolean) metadata.get( ITenantManager.TENANT_ROOT ) ) {
        rootFolder = null;
      }
    }
    return rootFolder;

  }

  public void enableTenants( Session session, final List<ITenant> tenants, final boolean enable )
    throws ItemNotFoundException, RepositoryException {
    for ( ITenant tenant : tenants ) {
      enableTenant( session, tenant, enable );
    }
  }

  public List<ITenant> getChildTenants( Session session, final ITenant parentTenant,
      final boolean includeDisabledTenants ) throws RepositoryException {
    List<ITenant> children = new ArrayList<ITenant>();
    List<RepositoryFile> allChildren =
        JcrRepositoryFileUtils.getChildren( session, new PentahoJcrConstants( session ), pathConversionHelper, null,
            getTenantRootFolder( session, parentTenant ).getId(), null );
    for ( RepositoryFile repoFile : allChildren ) {
      Map<String, Serializable> metadata = JcrRepositoryFileUtils.getFileMetadata( session, repoFile.getId() );
      if ( metadata.containsKey( ITenantManager.TENANT_ROOT )
        && (Boolean) metadata.get( ITenantManager.TENANT_ROOT ) ) {
        Tenant tenant = new Tenant( repoFile.getPath(), isTenantEnabled( session, repoFile.getId() ) );
        if ( includeDisabledTenants || tenant.isEnabled() ) {
          children.add( new Tenant( pathConversionHelper.relToAbs(repoFile.getPath()), isTenantEnabled(session, repoFile.getId()) ) );
        }

      }
    }
    return children;
  }

  public List<ITenant> getChildTenants( Session session, final ITenant parentTenant ) throws RepositoryException {
    return getChildTenants( session, parentTenant, false );
  }

  public void updateTentant( Session jcrSession, String arg0, Map<String, Serializable> arg1 ) {
  }

  protected void createInitialTenantFolders( Session session, final RepositoryFile tenantRootFolder,
      final RepositoryFileSid fileOwnerSid, final RepositoryFileSid authenticatedRoleSid ) throws RepositoryException {
    // We create a tenant's home folder while creating a user
    repositoryFileDao.createFolder( tenantRootFolder.getId(), new RepositoryFile.Builder( ServerRepositoryPaths
        .getTenantPublicFolderName() ).folder( true ).build(), new RepositoryFileAcl.Builder( fileOwnerSid ).build(),
        null );
    repositoryFileDao
        .createFolder( tenantRootFolder.getId(), new RepositoryFile.Builder( ServerRepositoryPaths
            .getTenantEtcFolderName() ).folder( true ).build(), new RepositoryFileAcl.Builder( fileOwnerSid ).build(),
            null );
  }

  protected void setAsSystemFolder( Serializable fileId ) {
    Map<String, Serializable> fileMeta = repositoryFileDao.getFileMetadata( fileId );
    fileMeta.put( IUnifiedRepository.SYSTEM_FOLDER, true );
    repositoryFileDao.setFileMetadata( fileId, fileMeta );

  }

  private boolean isTenantEnabled( Session session, final Serializable tenantFolderId ) throws ItemNotFoundException,
    RepositoryException {
    Map<String, Serializable> metadata = JcrRepositoryFileUtils.getFileMetadata( session, tenantFolderId );

    return metadata.containsKey( ITenantManager.TENANT_ENABLED )
        && (Boolean) metadata.get( ITenantManager.TENANT_ENABLED );
  }

  public boolean isSubTenant( Session jcrSession, ITenant parentTenant, ITenant descendantTenant ) {
    return internalIsSubTenant( parentTenant, descendantTenant );
  }

  private boolean internalIsSubTenant( ITenant descendantTenant, List<ITenant> childTenants ) {
    for ( ITenant tenant : childTenants ) {
      if ( tenant != null ) {
        if ( tenant.equals( childTenants ) ) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean internalIsSubTenant( ITenant parentTenant, ITenant descendantTenant ) {
    if ( parentTenant.equals( descendantTenant ) ) {
      return true;
    } else {
      List<ITenant> childTenants = getChildTenants( parentTenant );
      if ( childTenants != null && childTenants.size() > 0 ) {
        if ( internalIsSubTenant( descendantTenant, childTenants ) ) {
          return true;
        } else {
          for ( ITenant childTenant : childTenants ) {
            boolean done = internalIsSubTenant( childTenant, descendantTenant );
            if ( done ) {
              return done;
            }
          }
        }
      } else {
        return false;
      }
    }
    return false;
  }

  public ITenant getTenant( Session session, String tenantId ) throws RepositoryException {
    ITenant tenant = null;
    RepositoryFile tenantRootFolder =
        JcrRepositoryFileUtils.getFileByAbsolutePath( session, tenantId, pathConversionHelper, null, false, null );
    if ( ( tenantRootFolder != null ) && isTenantRoot( session, tenantRootFolder.getId() ) ) {
      tenant = new Tenant( tenantId, isTenantEnabled( session, tenantRootFolder.getId() ) );
    }
    return tenant;
  }

  private boolean isTenantRoot( Session session, final Serializable tenantFolderId ) throws ItemNotFoundException,
    RepositoryException {
    Map<String, Serializable> metadata = JcrRepositoryFileUtils.getFileMetadata( session, tenantFolderId );

    return metadata.containsKey( ITenantManager.TENANT_ROOT ) && (Boolean) metadata.get( ITenantManager.TENANT_ROOT );
  }

  @Override
  public RepositoryFile createUserHomeFolder( ITenant theTenant, String username ) {
    Builder aclsForUserHomeFolder = null;
    Builder aclsForTenantHomeFolder = null;
    RepositoryFile userHomeFolder = null;
    RepositoryFile tenantHomeFolder = null;
    RepositoryFile tenantRootFolder = null;
    String userId = tenantedUserNameResolver.getPrincipleId( theTenant, username );
    final RepositoryFileSid userSid = new RepositoryFileSid( userId );
    username = JcrTenantUtils.getPrincipalName( username, true );
    if ( theTenant == null ) {
      theTenant = JcrTenantUtils.getTenant( username, true );
    }
    // Get the Tenant Root folder. If the Tenant Root folder does not exist then exit.
    tenantRootFolder =
        repositoryFileDao.getFileByAbsolutePath( ServerRepositoryPaths.getTenantRootFolderPath( theTenant ) );
    if ( tenantRootFolder != null ) {
      // Try to see if Tenant Home folder exist
      tenantHomeFolder =
          repositoryFileDao.getFileByAbsolutePath( ServerRepositoryPaths.getTenantHomeFolderPath( theTenant ) );
      if ( tenantHomeFolder == null ) {
        String ownerId = tenantedUserNameResolver.getPrincipleId( theTenant, username );
        RepositoryFileSid ownerSid = new RepositoryFileSid( ownerId, Type.USER );

        String tenantAuthenticatedRoleId =
            tenantedRoleNameResolver.getPrincipleId( theTenant, tenantAuthenticatedRoleName );
        RepositoryFileSid tenantAuthenticatedRoleSid = new RepositoryFileSid( tenantAuthenticatedRoleId, Type.ROLE );

        aclsForTenantHomeFolder =
            new RepositoryFileAcl.Builder( userSid ).ace( tenantAuthenticatedRoleSid, EnumSet
                .of( RepositoryFilePermission.READ ) );

        aclsForUserHomeFolder =
            new RepositoryFileAcl.Builder( userSid ).ace( ownerSid, EnumSet.of( RepositoryFilePermission.ALL ) );
        tenantHomeFolder =
            repositoryFileDao.createFolder( tenantRootFolder.getId(), new RepositoryFile.Builder( ServerRepositoryPaths
                .getTenantHomeFolderName() ).folder( true ).build(), aclsForTenantHomeFolder.build(),
                "tenant home folder" );
      } else {
        String ownerId = tenantedUserNameResolver.getPrincipleId( theTenant, username );
        RepositoryFileSid ownerSid = new RepositoryFileSid( ownerId, Type.USER );
        aclsForUserHomeFolder =
            new RepositoryFileAcl.Builder( userSid ).ace( ownerSid, EnumSet.of( RepositoryFilePermission.ALL ) );
      }

      // now check if user's home folder exist
      userHomeFolder =
          repositoryFileDao.getFileByAbsolutePath( ServerRepositoryPaths.getUserHomeFolderPath( theTenant, username ) );
      if ( userHomeFolder == null ) {
        userHomeFolder =
            repositoryFileDao.createFolder( tenantHomeFolder.getId(), new RepositoryFile.Builder( username ).folder(
                true ).build(), aclsForUserHomeFolder.build(), "user home folder" ); //$NON-NLS-1$
      }

    }
    return userHomeFolder;
  }

  @Override
  public RepositoryFile getUserHomeFolder( ITenant theTenant, String username ) {
    return repositoryFileDao.getFileByAbsolutePath( ServerRepositoryPaths
      .getUserHomeFolderPath( theTenant, username ) );
  }
}
