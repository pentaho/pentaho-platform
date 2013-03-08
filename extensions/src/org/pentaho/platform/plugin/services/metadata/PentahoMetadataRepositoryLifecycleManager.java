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
package org.pentaho.platform.plugin.services.metadata;

import java.io.Serializable;

import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.repository2.unified.IBackingRepositoryLifecycleManager;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.platform.repository2.ClientRepositoryPaths;
import org.pentaho.platform.repository2.unified.IRepositoryFileAclDao;
import org.pentaho.platform.repository2.unified.IRepositoryFileDao;
import org.pentaho.platform.repository2.unified.jcr.JcrTenantUtils;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;

/**
 * Initializes folders used by Pentaho Metadata
 *
 * @author dkincade
 */
public class PentahoMetadataRepositoryLifecycleManager implements IBackingRepositoryLifecycleManager {

  private static final String FOLDER_METADATA = "metadata"; //$NON-NLS-1$

  protected String repositoryAdminUsername;

  protected String tenantAuthenticatedAuthorityNamePattern;

  protected String singleTenantAuthenticatedAuthorityName;

  protected TransactionTemplate txnTemplate;

  protected IRepositoryFileDao repositoryFileDao;

  protected IRepositoryFileAclDao repositoryFileAclDao;

  public PentahoMetadataRepositoryLifecycleManager(final IRepositoryFileDao contentDao,
      final IRepositoryFileAclDao repositoryFileAclDao, final TransactionTemplate txnTemplate,
      final String repositoryAdminUsername, final String tenantAuthenticatedAuthorityNamePattern) {

    Assert.notNull(contentDao);
    Assert.notNull(repositoryFileAclDao);
    Assert.notNull(txnTemplate);
    Assert.hasText(repositoryAdminUsername);
    Assert.hasText(tenantAuthenticatedAuthorityNamePattern);
    this.repositoryFileDao = contentDao;
    this.repositoryFileAclDao = repositoryFileAclDao;
    this.txnTemplate = txnTemplate;
    this.repositoryAdminUsername = repositoryAdminUsername;
    this.tenantAuthenticatedAuthorityNamePattern = tenantAuthenticatedAuthorityNamePattern;
    initTransactionTemplate();
  }

  protected void initTransactionTemplate() {
    // a new transaction must be created (in order to run with the correct user privileges)
    txnTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
  }

  public synchronized void doNewTenant(final String tenantPath) {
  }

  @Override
  public void startup() {
    // TODO Auto-generated method stub

  }

  @Override
  public void shutdown() {
    // TODO Auto-generated method stub

  }

  @Override
  public void newTenant(final ITenant tenant) {
    try {
      txnTemplate.execute(new TransactionCallbackWithoutResult() {
        @Override
        public void doInTransactionWithoutResult(final TransactionStatus status) {
          final RepositoryFileSid repositoryAdminUserSid = new RepositoryFileSid(repositoryAdminUsername);
          RepositoryFile etcFolder = repositoryFileDao.getFile(ClientRepositoryPaths.getEtcFolderPath());
          Assert.notNull(etcFolder);

          final String metadataPath = ClientRepositoryPaths.getEtcFolderPath() + RepositoryFile.SEPARATOR
              + FOLDER_METADATA;
          if (repositoryFileDao.getFile(metadataPath) == null) {
            // create the metadata folder
            internalCreateFolder(
                etcFolder.getId(),
                new RepositoryFile.Builder(FOLDER_METADATA).folder(true).build(),
                true,
                repositoryAdminUserSid,
                Messages.getInstance().getString(
                    "PentahoMetadataRepositoryLifecycleManager.USER_0001_VER_COMMENT_METADATA"));
          }
        }
      });
    } finally {

    }
  }

  @Override
  public void newTenant() {
    newTenant(JcrTenantUtils.getTenant());
  }

  @Override
  public void newUser(final ITenant tenant, String username) {
    // TODO Auto-generated method stub

  }

  @Override
  public void newUser() {
    // TODO Auto-generated method stub

  }

  protected RepositoryFile internalCreateFolder(final Serializable parentFolderId, final RepositoryFile file,
      final boolean inheritAces, final RepositoryFileSid ownerSid, final String versionMessage) {
    Assert.notNull(file);

    return repositoryFileDao.createFolder(parentFolderId, file, makeAcl(inheritAces, ownerSid), versionMessage);
  }

  protected RepositoryFileAcl makeAcl(final boolean inheritAces, final RepositoryFileSid ownerSid) {
    return new RepositoryFileAcl.Builder(ownerSid).entriesInheriting(inheritAces).build();
  }
}
