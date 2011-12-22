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
package org.pentaho.platform.repository.pmd;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.repository.messages.Messages;
import org.pentaho.platform.repository2.unified.IRepositoryFileAclDao;
import org.pentaho.platform.repository2.unified.IRepositoryFileDao;
import org.pentaho.platform.repository2.unified.ServerRepositoryPaths;
import org.pentaho.platform.repository2.unified.lifecycle.AbstractBackingRepositoryLifecycleManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;

/**
 * Initializes folders used by Pentaho Metadata
 *
 * @author dkincade
 */
public class PentahoMetadataRepositoryLifecycleManager extends AbstractBackingRepositoryLifecycleManager {

  private static final String FOLDER_METADATA = "metadata"; //$NON-NLS-1$

  public PentahoMetadataRepositoryLifecycleManager(final IRepositoryFileDao contentDao,
                                                   final IRepositoryFileAclDao repositoryFileAclDao,
                                                   final TransactionTemplate txnTemplate,
                                                   final String repositoryAdminUsername,
                                                   final String tenantAuthenticatedAuthorityNamePattern,
                                                   final String singleTenantAuthenticatedAuthorityName) {
    super(contentDao, repositoryFileAclDao, txnTemplate, repositoryAdminUsername,
        tenantAuthenticatedAuthorityNamePattern, singleTenantAuthenticatedAuthorityName);
  }

  public synchronized void doNewTenant(final String tenantId) {
    IPentahoSession origPentahoSession = PentahoSessionHolder.getSession();
    PentahoSessionHolder.setSession(createRepositoryAdminPentahoSession());
    try {
      txnTemplate.execute(new TransactionCallbackWithoutResult() {
        public void doInTransactionWithoutResult(final TransactionStatus status) {
          final RepositoryFileSid repositoryAdminUserSid = new RepositoryFileSid(repositoryAdminUsername);
          final String tenantEtcFolderPath = ServerRepositoryPaths.getTenantEtcFolderPath(tenantId);
          RepositoryFile tenantEtcFolder = repositoryFileDao.getFileByAbsolutePath(tenantEtcFolderPath);
          Assert.notNull(tenantEtcFolder);

          final String absMetadataPath = tenantEtcFolderPath + RepositoryFile.SEPARATOR + FOLDER_METADATA;
          if (repositoryFileDao.getFileByAbsolutePath(absMetadataPath) == null) {
            // create the metadata folder
            internalCreateFolder(tenantEtcFolder.getId(),
                new RepositoryFile.Builder(FOLDER_METADATA).folder(true).build(), false, repositoryAdminUserSid,
                Messages.getInstance().getString("PentahoMetadataRepositoryLifecycleManager.USER_0001_VER_COMMENT_METADATA"));
          }
        }
      });
    } finally {
      PentahoSessionHolder.setSession(origPentahoSession);
    }
  }

  public synchronized void doNewUser(final String tenantId, final String username) {
  }

  public synchronized void doShutdown() {
  }

  public synchronized void doStartup() {
  }
}
