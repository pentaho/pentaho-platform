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


package org.pentaho.platform.repository2.unified.lifecycle;

import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid;
import org.pentaho.platform.repository.messages.Messages;
import org.pentaho.platform.repository2.unified.IRepositoryFileAclDao;
import org.pentaho.platform.repository2.unified.IRepositoryFileDao;
import org.pentaho.platform.repository2.unified.ServerRepositoryPaths;
import org.pentaho.platform.repository2.unified.jcr.IPathConversionHelper;
import org.pentaho.platform.repository2.unified.jcr.JcrTenantUtils;
import org.springframework.extensions.jcr.JcrTemplate;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;

import java.io.Serializable;

/**
 * Initializes folders used by Hitachi Vantara Mondrian.
 * 
 * @author Ezequiel Cuellar
 */
public class MondrianBackingRepositoryLifecycleManager extends AbstractBackingRepositoryLifecycleManager {

  // ~ Static fields/initializers
  // ======================================================================================

  // ~ Instance fields
  // =================================================================================================
  protected String repositoryAdminUsername;

  protected String tenantAuthenticatedAuthorityNamePattern;

  protected String singleTenantAuthenticatedAuthorityName;

  protected IRepositoryFileDao repositoryFileDao;

  protected IRepositoryFileAclDao repositoryFileAclDao;
  // ~ Constructors
  // ====================================================================================================

  private static final String FOLDER_MONDRIAN = "mondrian"; //$NON-NLS-1$

  private ITenantedPrincipleNameResolver userNameUtils;

  public MondrianBackingRepositoryLifecycleManager( final IRepositoryFileDao contentDao,
      final IRepositoryFileAclDao repositoryFileAclDao, final TransactionTemplate txnTemplate,
      final String repositoryAdminUsername, final String tenantAuthenticatedAuthorityNamePattern,
      final ITenantedPrincipleNameResolver userNameUtils, final JcrTemplate adminJcrTemplate,
      final IPathConversionHelper pathConversionHelper ) {
    super( txnTemplate, adminJcrTemplate, pathConversionHelper );
    Assert.notNull( contentDao, "The content DAO must not be null. Ensure a valid content DAO is provided." );
    Assert.notNull( repositoryFileAclDao, "The repository file ACL DAO must not be null. Ensure a valid ACL DAO is provided." );
    Assert.hasText( repositoryAdminUsername, "The repository admin username must not be null or empty. Ensure a valid username is provided." );
    Assert.hasText( tenantAuthenticatedAuthorityNamePattern, "The tenant authenticated authority name pattern must not be null or empty. Ensure a valid pattern is provided." );
    this.repositoryFileDao = contentDao;
    this.repositoryFileAclDao = repositoryFileAclDao;
    this.repositoryAdminUsername = repositoryAdminUsername;
    this.tenantAuthenticatedAuthorityNamePattern = tenantAuthenticatedAuthorityNamePattern;
    this.userNameUtils = userNameUtils;
  }

  // ~ Methods
  // =========================================================================================================

  protected void createEtcMondrianFolder( final ITenant tenant ) {
    txnTemplate.execute( new TransactionCallbackWithoutResult() {
      @Override
      public void doInTransactionWithoutResult( final TransactionStatus status ) {
        final RepositoryFileSid repositoryAdminUserSid =
            new RepositoryFileSid( userNameUtils.getPrincipleId( tenant, repositoryAdminUsername ) );
        RepositoryFile tenantEtcFolder =
            repositoryFileDao.getFileByAbsolutePath( ServerRepositoryPaths.getTenantEtcFolderPath( tenant ) );
        Assert.notNull( tenantEtcFolder, "The tenant etc folder must not be null. Ensure the tenant's etc folder exists in the repository." );

        if ( repositoryFileDao.getFileByAbsolutePath( ServerRepositoryPaths.getTenantEtcFolderPath( tenant )
            + RepositoryFile.SEPARATOR + FOLDER_MONDRIAN ) == null ) {
          // mondrian folder
          internalCreateFolder( tenantEtcFolder.getId(), new RepositoryFile.Builder( FOLDER_MONDRIAN ).folder( true )
              .build(), true, repositoryAdminUserSid, Messages.getInstance().getString(
              "MondrianRepositoryLifecycleManager.USER_0001_VER_COMMENT_MONDRIAN" ) ); //$NON-NLS-1$
        }
      }
    } );
  }

  @Override
  public void startup() {
    // Create the /etc/mondrian folder for a default tenant
    createEtcMondrianFolder( JcrTenantUtils.getDefaultTenant() );
  }

  @Override
  public void shutdown() {
    // TODO Auto-generated method stub

  }

  @Override
  public void newTenant( final ITenant tenant ) {
    // Create the /etc/mondrian folder if the tenant is not a default tenant
    if ( !tenant.equals( JcrTenantUtils.getDefaultTenant() ) ) {
      createEtcMondrianFolder( tenant );
    }
  }

  @Override
  public void newTenant() {
    // TODO Auto-generated method stub

  }

  @Override
  public void newUser( final ITenant tenant, String username ) {
    // TODO Auto-generated method stub

  }

  @Override
  public void newUser() {
    // TODO Auto-generated method stub

  }

  protected RepositoryFile internalCreateFolder( final Serializable parentFolderId, final RepositoryFile file,
      final boolean inheritAces, final RepositoryFileSid ownerSid, final String versionMessage ) {
    Assert.notNull( file, "The file must not be null. Ensure a valid RepositoryFile object is provided." );

    return repositoryFileDao.createFolder( parentFolderId, file, makeAcl( inheritAces, ownerSid ), versionMessage );
  }

  protected RepositoryFileAcl makeAcl( final boolean inheritAces, final RepositoryFileSid ownerSid ) {
    return new RepositoryFileAcl.Builder( ownerSid ).entriesInheriting( inheritAces ).build();
  }
}
