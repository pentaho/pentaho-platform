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

import java.io.Serializable;
import java.util.EnumSet;
import java.util.Map;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository2.unified.IBackingRepositoryLifecycleManager;
import org.pentaho.platform.api.repository2.unified.ITenantManager;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.repository2.messages.Messages;
import org.pentaho.platform.repository2.unified.IRepositoryFileAclDao;
import org.pentaho.platform.repository2.unified.IRepositoryFileDao;
import org.pentaho.platform.repository2.unified.ServerRepositoryPaths;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;

/**
 * Default {@link IBackingRepositoryLifecycleManager} implementation.
 * <p/>
 * <p>
 * <strong>
 * Note: You must be careful when changing, overriding, or substituting this class. The configuration of
 * {@code DefaultPentahoJackrabbitAccessControlHelper} depends on the behavior of this class.
 * </strong>
 * </p>
 *
 * @author mlowery
 */
public class DefaultBackingRepositoryLifecycleManager extends AbstractBackingRepositoryLifecycleManager {

  // ~ Static fields/initializers ======================================================================================

  // ~ Instance fields =================================================================================================

  // ~ Constructors ====================================================================================================

  public DefaultBackingRepositoryLifecycleManager(final IRepositoryFileDao contentDao,
                                                  final IRepositoryFileAclDao repositoryFileAclDao, final TransactionTemplate txnTemplate,
                                                  final String repositoryAdminUsername, final String tenantAuthenticatedAuthorityNamePattern,
                                                  final String singleTenantAuthenticatedAuthorityName) {
    super(contentDao, repositoryFileAclDao, txnTemplate, repositoryAdminUsername,
        tenantAuthenticatedAuthorityNamePattern, singleTenantAuthenticatedAuthorityName);
  }

  // ~ Methods =========================================================================================================

  public synchronized void doNewTenant(final String tenantId) {
    createTenantRootFolder(tenantId);
    createInitialTenantFolders(tenantId);
  }

  public synchronized void doNewUser(final String tenantId, final String username) {
    createUserHomeFolder(tenantId, username);
  }

  public synchronized void doShutdown() {
  }

  public synchronized void doStartup() {
    createPentahoRootFolder();
  }

  protected void createPentahoRootFolder() {
    IPentahoSession origPentahoSession = PentahoSessionHolder.getSession();
    PentahoSessionHolder.setSession(createRepositoryAdminPentahoSession());
    try {
      txnTemplate.execute(new TransactionCallbackWithoutResult() {
        public void doInTransactionWithoutResult(final TransactionStatus status) {
          final RepositoryFileSid repositoryAdminUserSid = new RepositoryFileSid(repositoryAdminUsername);
          RepositoryFile rootFolder = repositoryFileDao.getFileByAbsolutePath(ServerRepositoryPaths
              .getPentahoRootFolderPath());
          if (rootFolder == null) {
            // because this is running as the repo admin, the owner of this folder is the repo admin who also has full
            // control (no need to do a setOwner call); also, inherit from parent to let everyone see this folder
            rootFolder = internalCreateFolder(null, new RepositoryFile.Builder(ServerRepositoryPaths
                .getPentahoRootFolderName()).folder(true).build(), true, repositoryAdminUserSid, Messages.getInstance()
                .getString("DefaultRepositoryLifecycleManager.USER_0001_VER_COMMENT_PENTAHO_ROOT")); //$NON-NLS-1$
          }
        }
      });
    } finally {
      PentahoSessionHolder.setSession(origPentahoSession);
    }
  }

  protected void createTenantRootFolder(final String tenantId) {
    IPentahoSession origPentahoSession = PentahoSessionHolder.getSession();
    PentahoSessionHolder.setSession(createRepositoryAdminPentahoSession());
    try {
      txnTemplate.execute(new TransactionCallbackWithoutResult() {
        public void doInTransactionWithoutResult(final TransactionStatus status) {
          final RepositoryFileSid repositoryAdminUserSid = new RepositoryFileSid(repositoryAdminUsername);
          RepositoryFile rootFolder = repositoryFileDao.getFileByAbsolutePath(ServerRepositoryPaths
              .getPentahoRootFolderPath());
          RepositoryFile tenantRootFolder = repositoryFileDao.getFileByAbsolutePath(ServerRepositoryPaths
              .getTenantRootFolderPath(tenantId));
          if (tenantRootFolder == null) {
            tenantRootFolder = internalCreateFolder(rootFolder.getId(), new RepositoryFile.Builder(tenantId).folder(
                true).build(), false, repositoryAdminUserSid, Messages.getInstance().getString(
                "DefaultRepositoryLifecycleManager.USER_0002_VER_COMMENT_TENANT_ROOT")); //$NON-NLS-1$
            // no aces added here; access to tenant root is governed by DefaultPentahoJackrabbitAccessControlHelper
            // Here is where we tell the system that we're a tenant
            Map<String, Serializable> fileMeta = repositoryFileDao.getFileMetadata(tenantRootFolder.getId());
            fileMeta.put(ITenantManager.TENANT_ROOT, "true");
            fileMeta.put(ITenantManager.TENANT_ENABLED, "true");
            repositoryFileDao.setFileMetadata(tenantRootFolder.getId(), fileMeta);
          }
        }
      });
    } finally {
      PentahoSessionHolder.setSession(origPentahoSession);
    }
  }

  protected void createInitialTenantFolders(final String tenantId) {
    final String tenantAuthenticatedAuthorityName = internalGetTenantAuthenticatedAuthorityName(tenantId);
    IPentahoSession origPentahoSession = PentahoSessionHolder.getSession();
    PentahoSessionHolder.setSession(createRepositoryAdminPentahoSession());
    try {
      txnTemplate.execute(new TransactionCallbackWithoutResult() {
        public void doInTransactionWithoutResult(final TransactionStatus status) {
          final RepositoryFileSid repositoryAdminUserSid = new RepositoryFileSid(repositoryAdminUsername);
          RepositoryFile tenantRootFolder = repositoryFileDao.getFileByAbsolutePath(ServerRepositoryPaths
              .getTenantRootFolderPath(tenantId));
          Assert.notNull(tenantRootFolder);
          if (repositoryFileDao.getFileByAbsolutePath(ServerRepositoryPaths.getTenantPublicFolderPath(tenantId)) == null) {
            RepositoryFile tenantPublicFolder = internalCreateFolder(tenantRootFolder.getId(),
                new RepositoryFile.Builder(ServerRepositoryPaths.getTenantPublicFolderName()).folder(true).build(),
                false, repositoryAdminUserSid, Messages.getInstance().getString(
                "DefaultRepositoryLifecycleManager.USER_0003_TENANT_PUBLIC")); //$NON-NLS-1$
            internalAddPermission(tenantPublicFolder.getId(), new RepositoryFileSid(tenantAuthenticatedAuthorityName,
                RepositoryFileSid.Type.ROLE), EnumSet.of(RepositoryFilePermission.READ,
                RepositoryFilePermission.READ_ACL, RepositoryFilePermission.WRITE, RepositoryFilePermission.WRITE_ACL));

            // home folder used to inherit ACEs from parent ACL but instead now defines non-inherited ACEs since the 
            // user has a UI to modify it if it needs changing; if ACEs were inherited, the ACEs list would be empty in 
            // the UI; this is not desirable UI behavior
            RepositoryFile tenantHomeFolder = internalCreateFolder(tenantRootFolder.getId(),
                new RepositoryFile.Builder(ServerRepositoryPaths.getTenantHomeFolderName()).folder(true).build(),
                false, repositoryAdminUserSid, Messages.getInstance().getString(
                "DefaultRepositoryLifecycleManager.USER_0004_TENANT_HOME")); //$NON-NLS-1$
            internalAddPermission(tenantHomeFolder.getId(), new RepositoryFileSid(tenantAuthenticatedAuthorityName,
                RepositoryFileSid.Type.ROLE), EnumSet.of(RepositoryFilePermission.READ,
                RepositoryFilePermission.READ_ACL));

            // etc folder inherits ACEs from parent ACL
            internalCreateFolder(tenantRootFolder.getId(), new RepositoryFile.Builder(ServerRepositoryPaths
                .getTenantEtcFolderName()).folder(true).build(), true, repositoryAdminUserSid, Messages.getInstance()
                .getString("DefaultRepositoryLifecycleManager.USER_0005_TENANT_ETC")); //$NON-NLS-1$
          }
        }
      });
    } finally {
      PentahoSessionHolder.setSession(origPentahoSession);
    }
  }

  protected void createUserHomeFolder(final String tenantId, final String username) {
    IPentahoSession origPentahoSession = PentahoSessionHolder.getSession();
    PentahoSessionHolder.setSession(createRepositoryAdminPentahoSession());
    try {
      txnTemplate.execute(new TransactionCallbackWithoutResult() {
        public void doInTransactionWithoutResult(final TransactionStatus status) {
          final RepositoryFileSid userSid = new RepositoryFileSid(username);
          RepositoryFile userHomeFolder = repositoryFileDao.getFileByAbsolutePath(ServerRepositoryPaths
              .getUserHomeFolderPath(tenantId, username));
          if (userHomeFolder == null) {
            RepositoryFile tenantHomeFolder = repositoryFileDao.getFileByAbsolutePath(ServerRepositoryPaths
                .getTenantHomeFolderPath(tenantId));
            userHomeFolder = internalCreateFolder(tenantHomeFolder.getId(), new RepositoryFile.Builder(username)
                .folder(true).build(), false, userSid, Messages.getInstance().getString(
                "DefaultRepositoryLifecycleManager.USER_0006_USER_HOME")); //$NON-NLS-1$
            internalSetOwner(userHomeFolder, userSid);
            internalSetFullControl(userHomeFolder.getId(), userSid);
          }
        }
      });
    } finally {
      PentahoSessionHolder.setSession(origPentahoSession);
    }
  }
}
