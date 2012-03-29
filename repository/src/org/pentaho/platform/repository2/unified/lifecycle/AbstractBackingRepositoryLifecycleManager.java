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
import java.text.MessageFormat;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository2.unified.IBackingRepositoryLifecycleManager;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.TenantUtils;
import org.pentaho.platform.repository.messages.Messages;
import org.pentaho.platform.repository2.unified.IRepositoryFileAclDao;
import org.pentaho.platform.repository2.unified.IRepositoryFileDao;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;

/**
 * Contains some common functionality.
 * <p/>
 * <ul>
 * <li>Runs as the repository admin.</li>
 * <li>Uses {@code repositoryFileDao} and {@code repositoryFileAclDao} directly for the following reasons:
 * <ul>
 * <li>ability to call repositoryFileDao.getFileByAbsolutePath</li>
 * <li>ability to bypass Spring Security method interceptor</li>
 * </ul></li>
 * <li>As a consequence of above, uses programmatic transactions (because Spring's transaction proxy is also bypassed.
 * In addition, this keeps the amount of declarative transaction XML to a minimum.</li>
 * </ul>
 *
 * @author mlowery
 * @deprecated Implement IBackingRepositoryLifecycleManager instead
 */
@Deprecated
public abstract class AbstractBackingRepositoryLifecycleManager implements IBackingRepositoryLifecycleManager {

  // ~ Static fields/initializers ======================================================================================

  protected static final Log logger = LogFactory.getLog(DefaultBackingRepositoryLifecycleManager.class);

  // ~ Instance fields =================================================================================================

  /**
   * Repository super user.
   */
  protected String repositoryAdminUsername;

  /**
   * The role name pattern of role belonging to all authenticated users of a given tenant. {0} replaced with tenant ID.
   */
  protected String tenantAuthenticatedAuthorityNamePattern;

  /**
   * When not using multi-tenancy, this value is used as opposed to {@link tenantAuthenticatedAuthorityPattern}.
   */
  protected String singleTenantAuthenticatedAuthorityName;

  /**
   * When not using multi-tenancy, this value is used as opposed to {@link tenantAdminAuthorityPattern}.
   */
  protected String singleTenantAdminAuthorityName;

  protected TransactionTemplate txnTemplate;

  protected IRepositoryFileDao repositoryFileDao;

  protected IRepositoryFileAclDao repositoryFileAclDao;

  private AtomicBoolean startedUp = new AtomicBoolean(true);

  // ~ Constructors ====================================================================================================

  public AbstractBackingRepositoryLifecycleManager(final IRepositoryFileDao contentDao,
                                                   final IRepositoryFileAclDao repositoryFileAclDao, final TransactionTemplate txnTemplate,
                                                   final String repositoryAdminUsername, final String tenantAuthenticatedAuthorityNamePattern,
                                                   final String singleTenantAuthenticatedAuthorityName) {
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
    this.singleTenantAuthenticatedAuthorityName = singleTenantAuthenticatedAuthorityName;
    initTransactionTemplate();
  }

  public void newTenant() {
    newTenant(internalGetTenantId());
  }

  public void newUser() {
    newUser(internalGetTenantId(), internalGetUsername());
  }

  public void newTenant(final String tenantId) {
    assertStartedUp();
    doNewTenant(tenantId);
  }

  protected abstract void doNewTenant(final String tenantId);

  public void newUser(final String tenantId, final String username) {
    assertStartedUp();
    doNewUser(tenantId, username);
  }

  protected abstract void doNewUser(final String tenantId, final String username);

  public void shutdown() {
    assertStartedUp();
    doShutdown();
  }

  protected abstract void doShutdown();

  public void startup() {
    doStartup();
    startedUp.set(true);
  }

  protected abstract void doStartup();

  /**
   * Throws an {@code IllegalStateException} if not started up.  Should be called from all public methods (except
   * {@link #startup()}).
   */
  private void assertStartedUp() {
    Assert.state(startedUp.get(), Messages.getInstance().getString(
        "AbstractRepositoryLifecycleManager.ERROR_0001_STARTUP_NOT_CALLED")); //$NON-NLS-1$
  }

  protected void initTransactionTemplate() {
    // a new transaction must be created (in order to run with the correct user privileges)
    txnTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
  }

  protected RepositoryFile internalCreateFolder(final Serializable parentFolderId, final RepositoryFile file,
                                                final boolean inheritAces, final RepositoryFileSid ownerSid, final String versionMessage) {
    Assert.notNull(file);

    return repositoryFileDao.createFolder(parentFolderId, file, makeAcl(inheritAces, ownerSid), versionMessage);
  }

  protected RepositoryFileAcl makeAcl(final boolean inheritAces, final RepositoryFileSid ownerSid) {
    return new RepositoryFileAcl.Builder(ownerSid).entriesInheriting(inheritAces).build();
  }

  protected void internalSetFullControl(final Serializable fileId, final RepositoryFileSid sid) {
    Assert.notNull(fileId);
    Assert.notNull(sid);
    repositoryFileAclDao.setFullControl(fileId, sid, RepositoryFilePermission.ALL);
  }

  protected void internalAddPermission(final Serializable fileId, final RepositoryFileSid recipient,
                                       final EnumSet<RepositoryFilePermission> permissions) {
    Assert.notNull(fileId);
    Assert.notNull(recipient);
    Assert.notNull(permissions);
    Assert.notEmpty(permissions);

    repositoryFileAclDao.addAce(fileId, recipient, permissions);
  }

  protected IPentahoSession createRepositoryAdminPentahoSession() {
    StandaloneSession pentahoSession = new StandaloneSession(repositoryAdminUsername);
    pentahoSession.setAuthenticated(repositoryAdminUsername);
    return pentahoSession;
  }

  protected String internalGetTenantAuthenticatedAuthorityName(final String tenantId) {
    if (!TenantUtils.TENANTID_SINGLE_TENANT.equals(tenantId)) {
      return MessageFormat.format(tenantAuthenticatedAuthorityNamePattern, tenantId);
    } else {
      return singleTenantAuthenticatedAuthorityName;
    }
  }

  protected void internalSetOwner(final RepositoryFile file, final RepositoryFileSid owner) {
    Assert.notNull(file);
    Assert.notNull(owner);

    RepositoryFileAcl acl = repositoryFileAclDao.getAcl(file.getId());
    RepositoryFileAcl newAcl = new RepositoryFileAcl.Builder(acl).owner(owner).build();
    repositoryFileAclDao.updateAcl(newAcl);
  }

  /**
   * Returns the username of the current user.
   */
  protected String internalGetUsername() {
    IPentahoSession pentahoSession = PentahoSessionHolder.getSession();
    Assert.state(pentahoSession != null);
    return pentahoSession.getName();
  }

  /**
   * Returns the tenant ID of the current user.
   */
  protected String internalGetTenantId() {
    return TenantUtils.getTenantId();
  }

}
