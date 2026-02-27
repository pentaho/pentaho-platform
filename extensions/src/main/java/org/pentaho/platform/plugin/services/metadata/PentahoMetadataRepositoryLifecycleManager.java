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


package org.pentaho.platform.plugin.services.metadata;

import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.platform.repository2.unified.IRepositoryFileAclDao;
import org.pentaho.platform.repository2.unified.IRepositoryFileDao;
import org.pentaho.platform.repository2.unified.ServerRepositoryPaths;
import org.pentaho.platform.repository2.unified.jcr.IPathConversionHelper;
import org.pentaho.platform.repository2.unified.jcr.JcrTenantUtils;
import org.pentaho.platform.repository2.unified.lifecycle.AbstractBackingRepositoryLifecycleManager;
import org.springframework.extensions.jcr.JcrTemplate;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;

import java.io.Serializable;

/**
 * Initializes folders used by Pentaho Metadata
 * 
 * @author dkincade
 */
public class PentahoMetadataRepositoryLifecycleManager extends AbstractBackingRepositoryLifecycleManager {

  private static final String FOLDER_METADATA = "metadata"; //$NON-NLS-1$

  private ITenantedPrincipleNameResolver userNameUtils;

  protected String repositoryAdminUsername;

  protected String tenantAuthenticatedAuthorityNamePattern;

  protected String singleTenantAuthenticatedAuthorityName;

  protected IRepositoryFileDao repositoryFileDao;

  protected IRepositoryFileAclDao repositoryFileAclDao;

  public PentahoMetadataRepositoryLifecycleManager( final IRepositoryFileDao contentDao,
      final IRepositoryFileAclDao repositoryFileAclDao, final TransactionTemplate txnTemplate,
      final String repositoryAdminUsername, final String tenantAuthenticatedAuthorityNamePattern,
      final ITenantedPrincipleNameResolver userNameUtils, final JcrTemplate adminJcrTemplate,
      final IPathConversionHelper pathConversionHelper ) {
    super( txnTemplate, adminJcrTemplate, pathConversionHelper );
    Assert.notNull( contentDao, "Content DAO must not be null" );
    Assert.notNull( repositoryFileAclDao, "Repository File ACL DAO must not be null" );
    Assert.hasText( repositoryAdminUsername, "Repository admin username must not be null or empty" );
    Assert.hasText( tenantAuthenticatedAuthorityNamePattern, "Tenant authenticated authority name pattern must not be null or empty" );
    this.repositoryFileDao = contentDao;
    this.repositoryFileAclDao = repositoryFileAclDao;
    this.repositoryAdminUsername = repositoryAdminUsername;
    this.userNameUtils = userNameUtils;
    this.tenantAuthenticatedAuthorityNamePattern = tenantAuthenticatedAuthorityNamePattern;
  }

  public synchronized void doNewTenant( final String tenantPath ) {
  }

  @Override
  public void startup() {
    createEtcMetadaFolder( JcrTenantUtils.getDefaultTenant() );
  }

  @Override
  public void shutdown() {
    // TODO Auto-generated method stub

  }

  @Override
  public void newTenant( final ITenant tenant ) {
    if ( !tenant.equals( JcrTenantUtils.getDefaultTenant() ) ) {
      createEtcMetadaFolder( tenant );
    }
  }

  @Override
  public void newTenant() {
    newTenant( JcrTenantUtils.getTenant() );
  }

  @Override
  public void newUser( final ITenant tenant, String username ) {
    // TODO Auto-generated method stub

  }

  @Override
  public void newUser() {
    // TODO Auto-generated method stub

  }

  protected void createEtcMetadaFolder( final ITenant tenant ) {
    try {
      txnTemplate.execute( new TransactionCallbackWithoutResult() {
        @Override
        public void doInTransactionWithoutResult( final TransactionStatus status ) {
          final RepositoryFileSid repositoryAdminUserSid =
              new RepositoryFileSid( userNameUtils.getPrincipleId( tenant, repositoryAdminUsername ) );
          RepositoryFile tenantEtcFolder =
              repositoryFileDao.getFileByAbsolutePath( ServerRepositoryPaths.getTenantEtcFolderPath( tenant ) );
          Assert.notNull( tenantEtcFolder, "Tenant etc folder must not be null" );

          if ( repositoryFileDao.getFileByAbsolutePath( ServerRepositoryPaths.getTenantEtcFolderPath( tenant )
              + RepositoryFile.SEPARATOR + FOLDER_METADATA ) == null ) {
            // create the metadata folder
            internalCreateFolder( tenantEtcFolder.getId(), new RepositoryFile.Builder( FOLDER_METADATA ).folder( true )
                .build(), true, repositoryAdminUserSid, Messages.getInstance().getString(
                "PentahoMetadataRepositoryLifecycleManager.USER_0001_VER_COMMENT_METADATA" ) ); //$NON-NLS-1$
          }
        }
      } );
    } catch ( Exception e ) {
      //ignore
    }
  }

  protected RepositoryFile internalCreateFolder( final Serializable parentFolderId, final RepositoryFile file,
      final boolean inheritAces, final RepositoryFileSid ownerSid, final String versionMessage ) {
    Assert.notNull( file, "Repository file must not be null" );

    return repositoryFileDao.createFolder( parentFolderId, file, makeAcl( inheritAces, ownerSid ), versionMessage );
  }

  protected RepositoryFileAcl makeAcl( final boolean inheritAces, final RepositoryFileSid ownerSid ) {
    return new RepositoryFileAcl.Builder( ownerSid ).entriesInheriting( inheritAces ).build();
  }
}
