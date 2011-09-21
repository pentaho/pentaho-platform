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
package org.pentaho.platform.repository2.unified.jcr.jackrabbit;

import java.io.Serializable;
import java.security.AccessControlException;
import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.lock.Lock;

import org.apache.jackrabbit.core.PentahoUnversionedPropertyHelper;
import org.pentaho.platform.engine.core.system.TenantUtils;
import org.pentaho.platform.repository2.unified.ServerRepositoryPaths;
import org.pentaho.platform.repository2.unified.jcr.ILockHelper;
import org.pentaho.platform.repository2.unified.jcr.PentahoJcrConstants;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Default implementation of {@link ILockHelper}. If user {@code suzy} in tenant {@code acme} locks a file with 
 * UUID {@code abc} then this implementation will store the lock token {@code xyz} as 
 * {@code /pentaho/acme/home/suzy/.lockTokens/abc/xyz}. It is assumed that {@code /pentaho/acme/home/suzy} is never 
 * versioned! Putting lock token storage beneath the user's home folder provides access control.
 * 
 * @author mlowery
 */
public class DefaultLockHelper implements ILockHelper {

  // ~ Static fields/initializers ======================================================================================

  private final String FOLDER_NAME_LOCK_TOKENS = ".lockTokens"; //$NON-NLS-1$

  // ~ Instance fields =================================================================================================

  private final PentahoUnversionedPropertyHelper unversionedPropertyHelper = new PentahoUnversionedPropertyHelper();

  // ~ Constructors ====================================================================================================

  public DefaultLockHelper() {
    super();
  }

  // ~ Methods =========================================================================================================

  /**
   * Stores a lock token associated with the session's user.
   */
  protected void addLockToken(final Session session, final PentahoJcrConstants pentahoJcrConstants, final Lock lock)
      throws RepositoryException {
    Node lockTokensNode = getOrCreateLockTokensNode(session, pentahoJcrConstants, lock);
    Node newLockTokenNode = lockTokensNode.addNode(lock.getNode().getUUID(), pentahoJcrConstants
        .getPHO_NT_LOCKTOKENSTORAGE());
    newLockTokenNode.setProperty(pentahoJcrConstants.getPHO_LOCKEDNODEREF(), lock.getNode());
    newLockTokenNode.setProperty(pentahoJcrConstants.getPHO_LOCKTOKEN(), lock.getLockToken());
  }

  /**
   * Returns all lock tokens belonging to the session's user. Lock tokens can then be added to the session by calling
   * {@code Session.addLockToken(token)}.
   * 
   * <p>Callers should call {#link {@link #canUnlock(Session, PentahoJcrConstants, Lock)} if the token is being 
   * retrieved for the purpose of an unlock.</p>
   */
  protected String getLockToken(final Session session, final PentahoJcrConstants pentahoJcrConstants, final Lock lock)
      throws RepositoryException {
    Node lockTokensNode = getOrCreateLockTokensNode(session, pentahoJcrConstants, lock);
    NodeIterator nodes = lockTokensNode.getNodes(lock.getNode().getUUID());
    Assert.isTrue(nodes.hasNext());
    return nodes.nextNode().getProperty(pentahoJcrConstants.getPHO_LOCKTOKEN()).getString();
  }

  /**
   * Removes a lock token so that it can never be associated with anyone's session again. (To be called after the file
   * has been unlocked and therefore the token associated with the lock is unnecessary.)
   */
  public void removeLockToken(final Session session, final PentahoJcrConstants pentahoJcrConstants, final Lock lock)
      throws RepositoryException {
    Node lockTokensNode = getOrCreateLockTokensNode(session, pentahoJcrConstants, lock);
    NodeIterator nodes = lockTokensNode.getNodes(lock.getNode().getUUID());
    if (nodes.hasNext()) {
      nodes.nextNode().remove();
    }
  }

  protected Node getOrCreateLockTokensNode(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Lock lock) throws RepositoryException {
    String absPath = ServerRepositoryPaths.getUserHomeFolderPath(ServerRepositoryPaths.getTenantId(lock.getNode().getPath()), lock
        .getLockOwner());
    Node userHomeFolderNode = (Node) session.getItem(absPath);
    if (userHomeFolderNode.hasNode(FOLDER_NAME_LOCK_TOKENS)) {
      return userHomeFolderNode.getNode(FOLDER_NAME_LOCK_TOKENS);
    } else {
      Node lockTokensNode = userHomeFolderNode.addNode(FOLDER_NAME_LOCK_TOKENS, pentahoJcrConstants
          .getPHO_NT_INTERNALFOLDER());
      return lockTokensNode;
    }
  }

  /**
   * {@inheritDoc}
   */
  public boolean canUnlock(final Session session, final PentahoJcrConstants pentahoJcrConstants, final Lock lock)
      throws RepositoryException {
    String absPath = ServerRepositoryPaths.getUserHomeFolderPath(TenantUtils.getTenantId(), lock.getLockOwner());
    try {
      // checked permissions needs to be the actual perms used during unlockFile
      session.checkPermission(absPath, "read,remove"); //$NON-NLS-1$
    } catch (AccessControlException e) {
      return false;
    }
    return true;
  }

  /**
   * {@inheritDoc}
   */
  public void addLockTokenToSessionIfNecessary(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Serializable fileId) throws RepositoryException {
    Node fileNode = session.getNodeByUUID(fileId.toString());
    if (fileNode.isLocked()) {
      Lock lock = fileNode.getLock();
      String lockToken = getLockToken(session, pentahoJcrConstants, lock);
      session.addLockToken(lockToken);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void removeLockTokenFromSessionIfNecessary(final Session session,
      final PentahoJcrConstants pentahoJcrConstants, final Serializable fileId) throws RepositoryException {
    Node fileNode = session.getNodeByUUID(fileId.toString());
    if (fileNode.isLocked()) {
      Lock lock = fileNode.getLock();
      String lockToken = getLockToken(session, pentahoJcrConstants, lock);
      session.removeLockToken(lockToken);
    }
  }

  /**
   * {@inheritDoc}
   * TODO mlowery see if Jackrabbit locking behavior can be overridden so that lock message and date can be "protected"
   * properties--which wouldn't require checkouts
   */
  public void unlockFile(final Session session, final PentahoJcrConstants pentahoJcrConstants, final Serializable fileId)
      throws RepositoryException {
    Node fileNode = session.getNodeByUUID(fileId.toString());
    Lock lock = fileNode.getLock();
    String lockToken = getLockToken(session, pentahoJcrConstants, lock);
    session.addLockToken(lockToken);
    // get the lock again so that it has a non-null lockToken
    lock = fileNode.getLock();
    // don't need lock token anymore
    removeLockToken(session, pentahoJcrConstants, lock);
    fileNode.unlock();
    // remove custom lock properties
    if (fileNode.hasProperty(pentahoJcrConstants.getPHO_LOCKMESSAGE())) {
      unversionedPropertyHelper.removeUnversionedItem(fileNode.getProperty(pentahoJcrConstants.getPHO_LOCKMESSAGE()));
    }
    unversionedPropertyHelper.removeUnversionedItem(fileNode.getProperty(pentahoJcrConstants.getPHO_LOCKDATE()));
    session.save();
  }

  /**
   * {@inheritDoc}
   */
  public void lockFile(final Session session, final PentahoJcrConstants pentahoJcrConstants, final Serializable fileId,
      final String message) throws RepositoryException {
    // locks are always deep in this impl
    final boolean isDeep = true;
    // locks are always open-scoped since a session is short-lived and all work occurs in a transaction
    // anyway; from spec, "if a lock is enabled and then disabled within the same transaction, its effect never 
    // makes it to the persistent workspace and therefore it does nothing"
    final boolean isSessionScoped = false;
    Node fileNode = session.getNodeByUUID(fileId.toString());
    Assert.isTrue(fileNode.isNodeType(pentahoJcrConstants.getPHO_MIX_LOCKABLE()));
    Lock lock = fileNode.lock(isDeep, isSessionScoped);

    addLockToken(session, pentahoJcrConstants, lock);

    // add custom lock properties
    if (StringUtils.hasText(message)) {
      unversionedPropertyHelper.setUnversionedProperty(fileNode, PentahoJcrConstants.PHO_NS, PentahoJcrConstants.PHO_LOCKMESSAGE, message);
    }
    unversionedPropertyHelper.setUnversionedProperty(fileNode, PentahoJcrConstants.PHO_NS, PentahoJcrConstants.PHO_LOCKDATE, Calendar.getInstance());
    session.save();

  }
}
