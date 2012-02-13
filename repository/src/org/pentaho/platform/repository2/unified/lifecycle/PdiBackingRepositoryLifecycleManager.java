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
package org.pentaho.platform.repository2.unified.lifecycle;

import java.util.EnumSet;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.repository.messages.Messages;
import org.pentaho.platform.repository2.unified.IRepositoryFileAclDao;
import org.pentaho.platform.repository2.unified.IRepositoryFileDao;
import org.pentaho.platform.repository2.unified.ServerRepositoryPaths;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;

/**
 * Initializes folders used by Pentaho Data Integration.
 *
 * @author mlowery
 */
public class PdiBackingRepositoryLifecycleManager extends AbstractBackingRepositoryLifecycleManager {

  // ~ Static fields/initializers ======================================================================================

  // ~ Instance fields =================================================================================================

  // ~ Constructors ====================================================================================================

  private static final String FOLDER_PDI = "pdi"; //$NON-NLS-1$

  private static final String FOLDER_PARTITION_SCHEMAS = "partitionSchemas"; //$NON-NLS-1$

  private static final String FOLDER_CLUSTER_SCHEMAS = "clusterSchemas"; //$NON-NLS-1$

  private static final String FOLDER_SLAVE_SERVERS = "slaveServers"; //$NON-NLS-1$

  private static final String FOLDER_DATABASES = "databases"; //$NON-NLS-1$

  public PdiBackingRepositoryLifecycleManager(final IRepositoryFileDao contentDao,
                                              final IRepositoryFileAclDao repositoryFileAclDao, final TransactionTemplate txnTemplate,
                                              final String repositoryAdminUsername, final String tenantAuthenticatedAuthorityNamePattern,
                                              final String singleTenantAuthenticatedAuthorityName) {
    super(contentDao, repositoryFileAclDao, txnTemplate, repositoryAdminUsername,
        tenantAuthenticatedAuthorityNamePattern, singleTenantAuthenticatedAuthorityName);
  }

  // ~ Methods =========================================================================================================

  public synchronized void doNewTenant(String tenantId) {
    createEtcPdiFolders(tenantId);
  }

  public synchronized void doNewUser(String tenantId, String username) {
  }

  public synchronized void doShutdown() {
  }

  public synchronized void doStartup() {
  }

  protected void createEtcPdiFolders(final String tenantId) {
    IPentahoSession origPentahoSession = PentahoSessionHolder.getSession();
    PentahoSessionHolder.setSession(createRepositoryAdminPentahoSession());
    try {
      txnTemplate.execute(new TransactionCallbackWithoutResult() {
        public void doInTransactionWithoutResult(final TransactionStatus status) {
          final RepositoryFileSid repositoryAdminUserSid = new RepositoryFileSid(repositoryAdminUsername);
          RepositoryFile tenantEtcFolder = repositoryFileDao.getFileByAbsolutePath(ServerRepositoryPaths
              .getTenantEtcFolderPath(tenantId));
          Assert.notNull(tenantEtcFolder);

          if (repositoryFileDao.getFileByAbsolutePath(ServerRepositoryPaths.getTenantEtcFolderPath(tenantId)
              + RepositoryFile.SEPARATOR + FOLDER_PDI) == null) {
            // pdi folder
            RepositoryFile tenantEtcPdiFolder = internalCreateFolder(tenantEtcFolder.getId(),
                new RepositoryFile.Builder(FOLDER_PDI).folder(true).build(), false, repositoryAdminUserSid, Messages
                .getInstance().getString("PdiRepositoryLifecycleManager.USER_0001_VER_COMMENT_PDI")); //$NON-NLS-1$
            // databases folder
            internalCreateFolder(tenantEtcPdiFolder.getId(), new RepositoryFile.Builder(FOLDER_DATABASES).folder(true)
                .build(), true, repositoryAdminUserSid, Messages.getInstance().getString(
                "PdiRepositoryLifecycleManager.USER_0002_VER_COMMENT_DATABASES")); //$NON-NLS-1$
            // slave servers folder
            internalCreateFolder(tenantEtcPdiFolder.getId(), new RepositoryFile.Builder(FOLDER_SLAVE_SERVERS).folder(
                true).build(), true, repositoryAdminUserSid, Messages.getInstance().getString(
                "PdiRepositoryLifecycleManager.USER_0003_VER_COMMENT_SLAVESERVERS")); //$NON-NLS-1$
            // cluster schemas folder
            internalCreateFolder(tenantEtcPdiFolder.getId(), new RepositoryFile.Builder(FOLDER_CLUSTER_SCHEMAS).folder(
                true).build(), true, repositoryAdminUserSid, Messages.getInstance().getString(
                "PdiRepositoryLifecycleManager.USER_0004_CLUSTERSCHEMAS")); //$NON-NLS-1$
            // partition schemas folder
            internalCreateFolder(tenantEtcPdiFolder.getId(), new RepositoryFile.Builder(FOLDER_PARTITION_SCHEMAS)
                .folder(true).build(), true, repositoryAdminUserSid, Messages.getInstance().getString(
                "PdiRepositoryLifecycleManager.USER_0005_PARTITIONSCHEMAS")); //$NON-NLS-1$
          }
        }
      });
    } finally {
      PentahoSessionHolder.setSession(origPentahoSession);
    }
  }

  protected void addTenantAuthenticatedPermissions(final RepositoryFile folder,
                                                   final RepositoryFileSid tenantAuthenticatedAuthoritySid) {
    internalAddPermission(folder.getId(), tenantAuthenticatedAuthoritySid, EnumSet.of(RepositoryFilePermission.READ,
        RepositoryFilePermission.READ_ACL, RepositoryFilePermission.WRITE, RepositoryFilePermission.WRITE_ACL));
  }
}
