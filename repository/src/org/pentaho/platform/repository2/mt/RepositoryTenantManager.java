/*
 * Copyright 2002 - 2013 Pentaho Corporation.  All rights reserved.
 * 
 * This software was developed by Pentaho Corporation and is provided under the terms
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. TThe Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */

package org.pentaho.platform.repository2.mt;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.security.userroledao.IUserRoleDao;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.mt.ITenantManager;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl.Builder;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid.Type;
import org.pentaho.platform.core.mt.Tenant;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.repository2.unified.IRepositoryFileAclDao;
import org.pentaho.platform.repository2.unified.IRepositoryFileDao;
import org.pentaho.platform.repository2.unified.ServerRepositoryPaths;
import org.pentaho.platform.repository2.unified.jcr.JcrRepositoryFileUtils;
import org.pentaho.platform.repository2.unified.jcr.PentahoJcrConstants;
import org.pentaho.platform.security.policy.rolebased.IRoleAuthorizationPolicyRoleBindingDao;
import org.pentaho.platform.security.policy.rolebased.messages.Messages;
import org.springframework.extensions.jcr.JcrCallback;
import org.springframework.extensions.jcr.JcrTemplate;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.security.userdetails.User;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.util.Assert;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

/**
 * @author wseyler
 * 
 */
public class RepositoryTenantManager extends AbstractRepositoryTenantManager {
  // ~ Static fields/initializers
  // ======================================================================================

  /**
   * @author wseyler
   * 
   *         This class ensures that the DefaultTenantManager is ONLY working with absolute paths
   */

  protected static final Log logger = LogFactory.getLog( RepositoryTenantManager.class );

  // ~ Instance fields
  // =================================================================================================

  /**
   * Repository super user.
   */

  /**
   * When not using multi-tenancy, this value is used as opposed to {@link tenantAuthenticatedAuthorityPattern}.
   */
  // protected String singleTenantAuthenticatedAuthorityName;

  protected JcrTemplate jcrTemplate;

  public RepositoryTenantManager( final IRepositoryFileDao contentDao, final IUserRoleDao userRoleDao,
      final IRepositoryFileAclDao repositoryFileAclDao, IRoleAuthorizationPolicyRoleBindingDao roleBindingDao,
      final JcrTemplate jcrTemplate, final String repositoryAdminUsername,
      final String tenantAuthenticatedAuthorityNamePattern,
      final ITenantedPrincipleNameResolver tenantedUserNameResolver,
      final ITenantedPrincipleNameResolver tenantedRoleNameResolver, final String tenantAdminRoleName,
      final List<String> singleTenantAuthenticatedAuthorityRoleBindingList ) {
    super( contentDao, userRoleDao, repositoryFileAclDao, roleBindingDao, repositoryAdminUsername,
        tenantAuthenticatedAuthorityNamePattern, tenantedUserNameResolver, tenantedRoleNameResolver,
        tenantAdminRoleName, singleTenantAuthenticatedAuthorityRoleBindingList );
    this.jcrTemplate = jcrTemplate;
  }

  private RepositoryFile createTenantFolder( final ITenant parentTenant, final String tenantName,
      final String tenantCreatorId ) {
    return (RepositoryFile) jcrTemplate.execute( new JcrCallback() {
      @Override
      public Object doInJcr( final Session session ) throws RepositoryException {
        Tenant tenant = null;
        RepositoryFile parentFolder = null;
        if ( parentTenant == null ) {
          tenant = new Tenant( "/" + tenantName, true );
        } else {
          tenant = new Tenant( parentTenant.getRootFolderAbsolutePath() + "/" + tenantName, true );
          String folderPath = parentTenant.getRootFolderAbsolutePath();
          parentFolder = repositoryFileDao.getFileByAbsolutePath( folderPath );
        }

        RepositoryFileAcl acl = new RepositoryFileAcl.Builder( tenantCreatorId ).entriesInheriting( false ).build();
        RepositoryFile systemTenantFolder =
            repositoryFileDao.createFolder( parentFolder != null ? parentFolder.getId() : null,
                new RepositoryFile.Builder( tenant.getName() ).folder( true ).build(), acl, "" );
        repositoryFileDao.getFileByAbsolutePath( tenant.getId() );

        Map<String, Serializable> fileMeta = repositoryFileDao.getFileMetadata( systemTenantFolder.getId() );
        fileMeta.put( ITenantManager.TENANT_ROOT, true );
        fileMeta.put( ITenantManager.TENANT_ENABLED, true );
        JcrRepositoryFileUtils.setFileMetadata( session, systemTenantFolder.getId(), fileMeta );

        createRuntimeRolesFolderNode( session, new PentahoJcrConstants( session ), tenant );
        return systemTenantFolder;
      }
    } );
  }

  public Node createAuthzFolderNode( final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final ITenant tenant ) throws RepositoryException {
    Node tenantRootFolderNode = null;
    try {
      tenantRootFolderNode = (Node) session.getItem( ServerRepositoryPaths.getTenantRootFolderPath( tenant ) );
    } catch ( PathNotFoundException e ) {
      Assert.state( false, Messages.getInstance().getString(
          "JcrRoleAuthorizationPolicyRoleBindingDao.ERROR_0002_REPO_NOT_INITIALIZED" ) ); //$NON-NLS-1$
    }
    Node authzFolderNode =
        tenantRootFolderNode.addNode( FOLDER_NAME_AUTHZ, pentahoJcrConstants.getPHO_NT_INTERNALFOLDER() );
    session.save();
    return authzFolderNode;
  }

  public Node createRoleBasedFolderNode( final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final ITenant tenant ) throws RepositoryException {
    Node authzFolderNode = createAuthzFolderNode( session, pentahoJcrConstants, tenant );
    Node node = authzFolderNode.addNode( FOLDER_NAME_ROLEBASED, pentahoJcrConstants.getPHO_NT_INTERNALFOLDER() );
    authzFolderNode.save();
    session.save();
    return node;
  }

  public Node createRuntimeRolesFolderNode( final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final ITenant tenant ) throws RepositoryException {
    Node roleBasedFolderNode = createRoleBasedFolderNode( session, pentahoJcrConstants, tenant );
    Node node = roleBasedFolderNode.addNode( FOLDER_NAME_RUNTIMEROLES, pentahoJcrConstants.getPHO_NT_INTERNALFOLDER() );
    roleBasedFolderNode.save();
    session.save();
    return node;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.platform.api.repository2.unified.ITenantManager#createTenant(java.lang.String,
   * java.lang.String)
   */
  @Override
  public ITenant createTenant( final ITenant parentTenant, final String tenantName, final String tenantAdminRoleName,
      final String authenticatedRoleName, final String anonymousRoleName ) {
    Tenant newTenant;
    String parentTenantFolder;
    if ( parentTenant == null ) {
      if ( repositoryFileDao.getFileByAbsolutePath( "/" + tenantName ) != null ) {
        return null;
      }
    } else {
      if ( repositoryFileDao.getFileByAbsolutePath( parentTenant.getRootFolderAbsolutePath() + "/" + tenantName )
        != null ) {
        return null;
      }
    }
    if ( parentTenant == null ) {
      newTenant = new Tenant( RepositoryFile.SEPARATOR + tenantName, true );
      parentTenantFolder = "/";
    } else {
      newTenant = new Tenant( parentTenant.getRootFolderAbsolutePath() + RepositoryFile.SEPARATOR + tenantName, true );
      parentTenantFolder = parentTenant.getRootFolderAbsolutePath();
    }

    String tenantCreatorId = PentahoSessionHolder.getSession().getName();
    RepositoryFile tenantRootFolder = createTenantFolder( parentTenant, tenantName, tenantCreatorId );

    userRoleDao.createRole( newTenant, tenantAdminRoleName, "", new String[0] );
    userRoleDao.createRole( newTenant, authenticatedRoleName, "", new String[0] );
    userRoleDao.createRole( newTenant, anonymousRoleName, "", new String[0] );
    roleBindingDao
        .setRoleBindings( newTenant, authenticatedRoleName, singleTenantAuthenticatedAuthorityRoleBindingList );

    String tenantAdminRoleId = tenantedRoleNameResolver.getPrincipleId( newTenant, tenantAdminRoleName );
    RepositoryFileSid tenantAdminRoleSid = new RepositoryFileSid( tenantAdminRoleId, Type.ROLE );

    this.jcrTemplate.save();
    // If parent tenant is null then we assume we're creating the system tenant. In which case we'll give the
    // system
    // tenant admin permissions on the root folder.
    if ( parentTenant == null ) {
      repositoryFileAclDao.addAce( tenantRootFolder.getId(), tenantAdminRoleSid, EnumSet
          .of( RepositoryFilePermission.ALL ) );
    } else {

      RepositoryFileAcl acl = repositoryFileAclDao.getAcl( tenantRootFolder.getId() );
      Builder aclBuilder =
          new RepositoryFileAcl.Builder( acl ).ace( tenantAdminRoleSid, EnumSet.of( RepositoryFilePermission.ALL ) );

      IPentahoSession origPentahoSession = PentahoSessionHolder.getSession();
      Authentication origAuthentication = SecurityContextHolder.getContext().getAuthentication();
      login( repositoryAdminUsername, tenantAdminRoleId );
      try {
        // Give all to Tenant Admin of all ancestors
        while ( !parentTenantFolder.equals( "/" ) ) {
          ITenant tenant = new Tenant( parentTenantFolder, true );
          String parentTenantAdminRoleId = tenantedRoleNameResolver.getPrincipleId( tenant, tenantAdminRoleName );
          RepositoryFileSid parentTenantAdminSid = new RepositoryFileSid( parentTenantAdminRoleId, Type.ROLE );
          aclBuilder.ace( parentTenantAdminSid, EnumSet.of( RepositoryFilePermission.ALL ) );
          parentTenantFolder = FilenameUtils.getFullPathNoEndSeparator( parentTenantFolder );
        }
        repositoryFileAclDao.updateAcl( aclBuilder.build() );
      } catch ( Throwable th ) {
        th.printStackTrace();
      } finally {
        PentahoSessionHolder.setSession( origPentahoSession );
        SecurityContextHolder.getContext().setAuthentication( origAuthentication );
      }
    }

    try {
      RepositoryFileSid fileOwnerSid = new RepositoryFileSid( tenantCreatorId );
      createInitialTenantFolders( newTenant, tenantRootFolder, fileOwnerSid );
    } catch ( Exception ex ) {
      throw new RuntimeException( "Error creating initial tenant folders", ex );
    }
    return newTenant;
  }

  protected IPentahoSession createAuthenticatedPentahoSession( String tenantId, String userName ) {
    StandaloneSession pentahoSession = new StandaloneSession( userName );
    pentahoSession.setAuthenticated( tenantId, userName );
    return pentahoSession;
  }

  protected void login( final String username, String tenantAdminRoleId ) {
    StandaloneSession pentahoSession = new StandaloneSession( username );
    pentahoSession.setAuthenticated( null, username );
    PentahoSessionHolder.setSession( pentahoSession );
    final String password = "ignored";

    List<GrantedAuthority> authList = new ArrayList<GrantedAuthority>();
    authList.add( new GrantedAuthorityImpl( tenantAdminRoleId ) );
    GrantedAuthority[] authorities = authList.toArray( new GrantedAuthority[0] );
    UserDetails userDetails = new User( username, password, true, true, true, true, authorities );
    Authentication auth = new UsernamePasswordAuthenticationToken( userDetails, password, authorities );
    PentahoSessionHolder.setSession( pentahoSession );
    // this line necessary for Spring Security's MethodSecurityInterceptor
    SecurityContextHolder.getContext().setAuthentication( auth );
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.platform.api.repository2.unified.ITenantManager#deleteTenants(java.util.List)
   */
  @Override
  public void deleteTenants( final List<ITenant> tenants ) {
    jcrTemplate.execute( new JcrCallback() {
      @Override
      public Object doInJcr( final Session session ) {
        try {
          deleteTenants( session, tenants );
        } catch ( RepositoryException e ) {
          e.printStackTrace();
        }
        return null;
      }
    } );
  }

  @Override
  public void deleteTenant( final ITenant tenant ) {
    jcrTemplate.execute( new JcrCallback() {
      @Override
      public Object doInJcr( final Session session ) {
        try {
          deleteTenant( session, tenant );
        } catch ( RepositoryException e ) {
          e.printStackTrace();
        }
        return null;
      }
    } );
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.platform.api.repository2.unified.ITenantManager#disableTenant(java.io.Serializable)
   */
  @Override
  public void enableTenant( final ITenant tenant, final boolean enable ) {
    jcrTemplate.execute( new JcrCallback() {
      @Override
      public Object doInJcr( final Session session ) {
        try {
          enableTenant( session, tenant, enable );
        } catch ( RepositoryException e ) {
          e.printStackTrace();
        }
        return null;
      }
    } );
  }

  @Override
  public RepositoryFile getTenantRootFolder( ITenant tenant ) {
    RepositoryFile rootFolder = repositoryFileDao.getFileByAbsolutePath( tenant.getRootFolderAbsolutePath() );
    if ( rootFolder != null ) {
      Map<String, Serializable> metadata = repositoryFileDao.getFileMetadata( rootFolder.getId() );
      if ( !metadata.containsKey( ITenantManager.TENANT_ROOT ) || !(Boolean) metadata.get(
        ITenantManager.TENANT_ROOT ) ) {
        rootFolder = null;
      }
    }
    return rootFolder;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.platform.api.repository2.unified.ITenantManager#enableTenants(java.util.List)
   */
  @Override
  public void enableTenants( final List<ITenant> tenants, final boolean enable ) {
    jcrTemplate.execute( new JcrCallback() {
      @Override
      public Object doInJcr( final Session session ) {
        try {
          enableTenants( session, tenants, enable );
        } catch ( RepositoryException e ) {
          e.printStackTrace();
        }
        return null;
      }
    } );
  }

  @Override
  public List<ITenant> getChildTenants( final ITenant parentTenant, final boolean includeDisabledTenants ) {
    return (List<ITenant>) jcrTemplate.execute( new JcrCallback() {
      @Override
      public Object doInJcr( final Session session ) {
        List<ITenant> childTenants = null;
        try {
          childTenants = getChildTenants( session, parentTenant, includeDisabledTenants );
        } catch ( RepositoryException e ) {
          childTenants = new ArrayList<ITenant>();
          e.printStackTrace();
        }
        return childTenants;
      }
    } );
  }

  @Override
  public List<ITenant> getChildTenants( final ITenant parentTenant ) {
    return (List<ITenant>) jcrTemplate.execute( new JcrCallback() {
      @Override
      public Object doInJcr( final Session session ) {
        List<ITenant> childTenants = null;
        try {
          childTenants = getChildTenants( session, parentTenant );
        } catch ( RepositoryException e ) {
          childTenants = new ArrayList<ITenant>();
          e.printStackTrace();
        }
        return childTenants;
      }
    } );
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.platform.api.repository2.unified.ITenantManager#updateTentant(java.lang.String,
   * java.util.Map)
   */
  @Override
  public void updateTentant( String tenantPath, Map<String, Serializable> tenantInfo ) {
  }

  String getParentPath( String parentPath ) {
    if ( parentPath != null && parentPath.length() > 0 ) {
      return ServerRepositoryPaths.getPentahoRootFolderPath() + RepositoryFile.SEPARATOR + parentPath
          + RepositoryFile.SEPARATOR;
    } else {
      return ServerRepositoryPaths.getPentahoRootFolderPath() + RepositoryFile.SEPARATOR;
    }
  }

  String getTenantPath( String parentPath, String tenantName ) {
    return getParentPath( parentPath ) + tenantName;
  }

  @Override
  public ITenant getTenant( final String tenantId ) {
    return (ITenant) jcrTemplate.execute( new JcrCallback() {
      @Override
      public Object doInJcr( Session session ) throws IOException, RepositoryException {
        return getTenant( session, tenantId );
      }
    } );
  }

  @Override
  public ITenant getTenantByRootFolderPath( final String tenantRootFolderPath ) {
    return (ITenant) jcrTemplate.execute( new JcrCallback() {
      @Override
      public Object doInJcr( Session session ) throws IOException, RepositoryException {
        return getTenant( session, tenantRootFolderPath );
      }
    } );
  }

  @Override
  public boolean isSubTenant( final ITenant parentTenant, final ITenant descendantTenant ) {
    return (Boolean) jcrTemplate.execute( new JcrCallback() {
      @Override
      public Object doInJcr( Session session ) throws IOException, RepositoryException {
        return isSubTenant( session, parentTenant, descendantTenant );
      }
    } );
  }

  public IUserRoleDao getUserRoleDao() {
    return userRoleDao;
  }

  private RepositoryFile createPublicFolder( ITenant tenant, RepositoryFile tenantRootFolder,
      RepositoryFileSid fileOwnerSid ) {
    String tenantAdminRoleId = tenantedRoleNameResolver.getPrincipleId( tenant, tenantAdminRoleName );
    RepositoryFileSid tenantAdminRoleSid = new RepositoryFileSid( tenantAdminRoleId, Type.ROLE );

    String tenantAuthenticatedRoleId = tenantedRoleNameResolver.getPrincipleId( tenant, tenantAuthenticatedRoleName );
    RepositoryFileSid tenantAuthenticatedRoleSid = new RepositoryFileSid( tenantAuthenticatedRoleId, Type.ROLE );

    RepositoryFile publicFolder =
        repositoryFileDao.createFolder( tenantRootFolder.getId(), new RepositoryFile.Builder( ServerRepositoryPaths
            .getTenantPublicFolderName() ).folder( true ).title(
            Messages.getInstance().getString( "RepositoryTenantManager.publicFolderDisplayName" ) ).build(),
            new RepositoryFileAcl.Builder( fileOwnerSid ).ace( tenantAdminRoleSid,
                EnumSet.of( RepositoryFilePermission.ALL ) ).ace( tenantAuthenticatedRoleSid,
                EnumSet.of( RepositoryFilePermission.READ ) ).build(), null );
    return publicFolder;
  }

  private RepositoryFile createHomeFolder( ITenant tenant, RepositoryFile tenantRootFolder,
      RepositoryFileSid fileOwnerSid ) {
    String tenantAdminRoleId = tenantedRoleNameResolver.getPrincipleId( tenant, tenantAdminRoleName );
    RepositoryFileSid tenantAdminRoleSid = new RepositoryFileSid( tenantAdminRoleId, Type.ROLE );

    String tenantAuthenticatedRoleId = tenantedRoleNameResolver.getPrincipleId( tenant, tenantAuthenticatedRoleName );
    RepositoryFileSid tenantAuthenticatedRoleSid = new RepositoryFileSid( tenantAuthenticatedRoleId, Type.ROLE );

    RepositoryFile homeFolder =
        repositoryFileDao.createFolder( tenantRootFolder.getId(), new RepositoryFile.Builder( ServerRepositoryPaths
            .getTenantHomeFolderName() ).folder( true ).title(
            Messages.getInstance().getString( "RepositoryTenantManager.usersFolderDisplayName" ) ).build(),
            new RepositoryFileAcl.Builder( fileOwnerSid ).ace( tenantAdminRoleSid,
                EnumSet.of( RepositoryFilePermission.ALL ) ).ace( tenantAuthenticatedRoleSid,
                EnumSet.of( RepositoryFilePermission.READ ) ).build(), null );
    return homeFolder;
  }

  private RepositoryFile createEtcFolder( ITenant tenant, RepositoryFile tenantRootFolder,
      RepositoryFileSid fileOwnerSid ) {

    String tenantAuthenticatedRoleId = tenantedRoleNameResolver.getPrincipleId( tenant, tenantAuthenticatedRoleName );
    RepositoryFileSid tenantAuthenticatedRoleSid = new RepositoryFileSid( tenantAuthenticatedRoleId, Type.ROLE );

    String tenantAdminRoleId = tenantedRoleNameResolver.getPrincipleId( tenant, tenantAdminRoleName );
    RepositoryFileSid tenantAdminRoleSid = new RepositoryFileSid( tenantAdminRoleId, Type.ROLE );

    RepositoryFile etcFolder =
        repositoryFileDao.createFolder( tenantRootFolder.getId(), new RepositoryFile.Builder( ServerRepositoryPaths
            .getTenantEtcFolderName() ).folder( true ).build(), new RepositoryFileAcl.Builder( fileOwnerSid )
              .entriesInheriting( true ).ace( tenantAuthenticatedRoleSid, EnumSet.of( RepositoryFilePermission.READ ) )
              .ace( tenantAdminRoleSid, EnumSet.of( RepositoryFilePermission.ALL ) ).build(), null );

    RepositoryFile pdiFolder =
        repositoryFileDao.createFolder( etcFolder.getId(), new RepositoryFile.Builder( "pdi" ).folder( true ).build(),
            new RepositoryFileAcl.Builder( fileOwnerSid ).entriesInheriting( true ).build(), null );
    repositoryFileDao.createFolder( pdiFolder.getId(),
        new RepositoryFile.Builder( "databases" ).folder( true ).build(), new RepositoryFileAcl.Builder( fileOwnerSid )
            .entriesInheriting( true ).build(), null );
    repositoryFileDao.createFolder( pdiFolder.getId(), new RepositoryFile.Builder( "slaveServers" ).folder( true )
        .build(), new RepositoryFileAcl.Builder( fileOwnerSid ).entriesInheriting( true ).build(), null );
    repositoryFileDao.createFolder( pdiFolder.getId(), new RepositoryFile.Builder( "clusterSchemas" ).folder( true )
        .build(), new RepositoryFileAcl.Builder( fileOwnerSid ).entriesInheriting( true ).build(), null );
    repositoryFileDao.createFolder( pdiFolder.getId(), new RepositoryFile.Builder( "partitionSchemas" ).folder( true )
        .build(), new RepositoryFileAcl.Builder( fileOwnerSid ).entriesInheriting( true ).build(), null );

    repositoryFileDao.createFolder( etcFolder.getId(),
        new RepositoryFile.Builder( "metastore" ).folder( true ).build(), new RepositoryFileAcl.Builder( fileOwnerSid )
            .entriesInheriting( true ).build(), null );

    return etcFolder;
  }

  protected void createInitialTenantFolders( ITenant tenant, final RepositoryFile tenantRootFolder,
      final RepositoryFileSid fileOwnerSid ) throws RepositoryException {
    // We create a tenant's home folder while creating a user

    createPublicFolder( tenant, tenantRootFolder, fileOwnerSid );
    RepositoryFile etcFolder = createEtcFolder( tenant, tenantRootFolder, fileOwnerSid );
    createHomeFolder( tenant, tenantRootFolder, fileOwnerSid );
    setAsSystemFolder( etcFolder.getId() );
  }
}
