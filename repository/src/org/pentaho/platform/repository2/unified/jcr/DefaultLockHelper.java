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

package org.pentaho.platform.repository2.unified.jcr;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.repository.RepositoryFilenameUtils;
import org.pentaho.platform.repository2.unified.ServerRepositoryPaths;
import org.springframework.util.Assert;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.lock.Lock;
import javax.jcr.lock.LockManager;
import javax.jcr.security.AccessControlManager;
import javax.jcr.security.Privilege;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Default implementation of {@link ILockHelper}. If user {@code suzy} in tenant {@code acme} locks a file with
 * UUID {@code abc} then this implementation will store the lock token {@code xyz} as
 * {@code /pentaho/acme/home/suzy/.lockTokens/abc/xyz}. It is assumed that {@code /pentaho/acme/home/suzy} is never
 * versioned! Putting lock token storage beneath the user's home folder provides access control.
 * 
 * <p>
 * This implementation stores a lock owner, lock date, and lock message in the ownerInfo payload. See JCR 2.0
 * section 17.3. If implemented as custom properties, then a versioned node would require a checkout and checkin to
 * lock a file. There is one caveat: implementations of JCR are free to ignore the ownerInfo payload. In that case,
 * the implementation sets the value. If that happens, we simply return that value as the lock owner and date and
 * message are null.
 * </p>
 * 
 * @author mlowery
 */
public class DefaultLockHelper implements ILockHelper {

  // ~ Static fields/initializers
  // ======================================================================================

  private static final String FOLDER_NAME_LOCK_TOKENS = ".lockTokens"; //$NON-NLS-1$

  private static final char LOCK_OWNER_INFO_SEPARATOR = ':';

  private static final String LOCK_OWNER_INFO_SEPARATOR_REGEX = "\\" + LOCK_OWNER_INFO_SEPARATOR; //$NON-NLS-1$

  private static final List<Character> RESERVED_CHARS = Arrays.asList( new Character[] { LOCK_OWNER_INFO_SEPARATOR } );

  private static final Log logger = LogFactory.getLog( DefaultLockHelper.class );

  private static final int POSITION_LOCK_OWNER = 0;

  private static final int POSITION_LOCK_DATE = 1;

  private static final int POSITION_LOCK_MESSAGE = 2;

  ITenantedPrincipleNameResolver userNameUtils;

  // ~ Instance fields
  // =================================================================================================

  // ~ Constructors
  // ====================================================================================================

  public DefaultLockHelper( ITenantedPrincipleNameResolver userNameUtils ) {
    super();
    this.userNameUtils = userNameUtils;
  }

  // ~ Methods
  // =========================================================================================================

  /**
   * Stores a lock token associated with the session's user.
   */
  protected void addLockToken( final Session session, final PentahoJcrConstants pentahoJcrConstants, final Lock lock )
    throws RepositoryException {
    Node lockTokensNode = getOrCreateLockTokensNode( session, pentahoJcrConstants, lock );
    Node newLockTokenNode =
        lockTokensNode.addNode( lock.getNode().getIdentifier(), pentahoJcrConstants.getPHO_NT_LOCKTOKENSTORAGE() );
    newLockTokenNode.setProperty( pentahoJcrConstants.getPHO_LOCKEDNODEREF(), lock.getNode() );
    newLockTokenNode.setProperty( pentahoJcrConstants.getPHO_LOCKTOKEN(), lock.getLockToken() );
    session.save();
  }

  /**
   * Returns all lock tokens belonging to the session's user. Lock tokens can then be added to the session by
   * calling {@code Session.addLockToken(token)}.
   * 
   * <p>
   * Callers should call {#link {@link #canUnlock(Session, PentahoJcrConstants, Lock)} if the token is being
   * retrieved for the purpose of an unlock.
   * </p>
   */
  protected String getLockToken( final Session session, final PentahoJcrConstants pentahoJcrConstants, final Lock lock )
    throws RepositoryException {
    Node lockTokensNode = getOrCreateLockTokensNode( session, pentahoJcrConstants, lock );
    NodeIterator nodes = lockTokensNode.getNodes( lock.getNode().getIdentifier() );
    Assert.isTrue( nodes.hasNext() );
    return nodes.nextNode().getProperty( pentahoJcrConstants.getPHO_LOCKTOKEN() ).getString();
  }

  /**
   * Removes a lock token so that it can never be associated with anyone's session again. (To be called after the
   * file has been unlocked and therefore the token associated with the lock is unnecessary.)
   */
  public void removeLockToken( final Session session, final PentahoJcrConstants pentahoJcrConstants, final Lock lock )
    throws RepositoryException {
    Node lockTokensNode = getOrCreateLockTokensNode( session, pentahoJcrConstants, lock );
    NodeIterator nodes = lockTokensNode.getNodes( lock.getNode().getIdentifier() );
    if ( nodes.hasNext() ) {
      nodes.nextNode().remove();
    }
    session.save();
  }

  protected Node getOrCreateLockTokensNode( final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Lock lock ) throws RepositoryException {
    String absPath =
        ServerRepositoryPaths.getUserHomeFolderPath( userNameUtils.getTenant( getLockOwner( session,
            pentahoJcrConstants, lock ) ), userNameUtils.getPrincipleName( getLockOwner( session, pentahoJcrConstants,
            lock ) ) );
    Node userHomeFolderNode = (Node) session.getItem( absPath );
    if ( userHomeFolderNode.hasNode( FOLDER_NAME_LOCK_TOKENS ) ) {
      return userHomeFolderNode.getNode( FOLDER_NAME_LOCK_TOKENS );
    } else {
      Node lockTokensNode =
          userHomeFolderNode.addNode( FOLDER_NAME_LOCK_TOKENS, pentahoJcrConstants.getPHO_NT_INTERNALFOLDER() );
      session.save();
      return lockTokensNode;
    }
  }

  /**
   * {@inheritDoc}
   */
  public boolean canUnlock( final Session session, final PentahoJcrConstants pentahoJcrConstants, final Lock lock )
    throws RepositoryException {
    String absPath =
        ServerRepositoryPaths.getUserHomeFolderPath( userNameUtils.getTenant( getLockOwner( session,
            pentahoJcrConstants, lock ) ), userNameUtils.getPrincipleName( getLockOwner( session, pentahoJcrConstants,
            lock ) ) );
    AccessControlManager acMgr = session.getAccessControlManager();
    return acMgr.hasPrivileges( absPath, new Privilege[] {
      acMgr.privilegeFromName( "jcr:read" ), acMgr.privilegeFromName( "jcr:write" ), //$NON-NLS-1$ //$NON-NLS-2$
      acMgr.privilegeFromName( "jcr:lockManagement" ) } ); //$NON-NLS-1$
  }

  /**
   * {@inheritDoc}
   */
  public void addLockTokenToSessionIfNecessary( final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Serializable fileId ) throws RepositoryException {
    Node fileNode = session.getNodeByIdentifier( fileId.toString() );
    if ( fileNode.isLocked() ) {
      LockManager lockManager = session.getWorkspace().getLockManager();
      Lock lock = lockManager.getLock( fileNode.getPath() );
      String lockToken = getLockToken( session, pentahoJcrConstants, lock );
      lockManager.addLockToken( lockToken );
    }
  }

  /**
   * {@inheritDoc}
   */
  public void removeLockTokenFromSessionIfNecessary( final Session session,
      final PentahoJcrConstants pentahoJcrConstants, final Serializable fileId ) throws RepositoryException {
    Node fileNode = session.getNodeByIdentifier( fileId.toString() );
    if ( fileNode.isLocked() ) {
      LockManager lockManager = session.getWorkspace().getLockManager();
      Lock lock = lockManager.getLock( fileNode.getPath() );
      String lockToken = getLockToken( session, pentahoJcrConstants, lock );
      lockManager.removeLockToken( lockToken );
    }
  }

  /**
   * {@inheritDoc}
   */
  public void unlockFile( final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Serializable fileId ) throws RepositoryException {
    Node fileNode = session.getNodeByIdentifier( fileId.toString() );
    LockManager lockManager = session.getWorkspace().getLockManager();
    Lock lock = lockManager.getLock( fileNode.getPath() );
    String lockToken = getLockToken( session, pentahoJcrConstants, lock );
    lockManager.addLockToken( lockToken );
    // get the lock again so that it has a non-null lockToken
    lock = lockManager.getLock( fileNode.getPath() );
    // don't need lock token anymore
    removeLockToken( session, pentahoJcrConstants, lock );
    lockManager.unlock( fileNode.getPath() );
  }

  /**
   * {@inheritDoc}
   */
  public void lockFile( final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Serializable fileId, final String message ) throws RepositoryException {
    LockManager lockManager = session.getWorkspace().getLockManager();
    // locks are always deep in this impl
    final boolean isDeep = true;
    // locks are always open-scoped since a session is short-lived and all work occurs in a transaction
    // anyway; from spec, "if a lock is enabled and then disabled within the same transaction, its effect never
    // makes it to the persistent workspace and therefore it does nothing"
    final boolean isSessionScoped = false;
    final long timeoutHint = Long.MAX_VALUE;
    final String ownerInfo =
        makeOwnerInfo( JcrTenantUtils.getTenantedUser( PentahoSessionHolder.getSession().getName() ), Calendar
            .getInstance().getTime(), message );
    Node fileNode = session.getNodeByIdentifier( fileId.toString() );
    Assert.isTrue( fileNode.isNodeType( pentahoJcrConstants.getMIX_LOCKABLE() ) );
    Lock lock = lockManager.lock( fileNode.getPath(), isDeep, isSessionScoped, timeoutHint, ownerInfo );
    addLockToken( session, pentahoJcrConstants, lock );
  }

  private String makeOwnerInfo( final String lockOwner, final Date lockDate, final String lockMessage ) {
    return escape( lockOwner ) + LOCK_OWNER_INFO_SEPARATOR + lockDate.getTime() + LOCK_OWNER_INFO_SEPARATOR
        + escape( lockMessage );
  }

  @Override
  public Date getLockDate( final Session session, final PentahoJcrConstants pentahoJcrConstants, final Lock lock )
    throws RepositoryException {
    String[] tokens = tokenize( lock.getLockOwner() );
    if ( tokens != null ) {
      long date;
      try {
        date = Long.parseLong( tokens[POSITION_LOCK_DATE] );
        return new Date( date );
      } catch ( NumberFormatException e ) {
        logger.debug( "could not parse lock date; returning null", e ); //$NON-NLS-1$
      }
    }
    return null;
  }

  @Override
  public String getLockMessage( final Session session, final PentahoJcrConstants pentahoJcrConstants, final Lock lock )
    throws RepositoryException {
    String[] tokens = tokenize( lock.getLockOwner() );
    if ( tokens != null ) {
      return unescape( tokens[POSITION_LOCK_MESSAGE] );
    }
    return null;
  }

  @Override
  public String getLockOwner( final Session session, final PentahoJcrConstants pentahoJcrConstants, final Lock lock )
    throws RepositoryException {
    String[] tokens = tokenize( lock.getLockOwner() );
    if ( tokens != null ) {
      return unescape( tokens[POSITION_LOCK_OWNER] );
    }
    // return whatever the implementation stored in this property
    return lock.getLockOwner();
  }

  private String[] tokenize( final String ownerInfo ) {
    if ( ownerInfo != null ) {
      String[] tokens = ownerInfo.split( LOCK_OWNER_INFO_SEPARATOR_REGEX );
      if ( tokens.length == 3 ) {
        return tokens;
      }
    }
    return null;
  }

  private static String escape( final String in ) {
    if ( in == null || in.trim().equals( "" ) ) { //$NON-NLS-1$
      return ""; //$NON-NLS-1$
    }
    return RepositoryFilenameUtils.escape( in, RESERVED_CHARS );
  }

  private static String unescape( final String in ) {
    if ( in == null || in.trim().equals( "" ) ) { //$NON-NLS-1$
      return ""; //$NON-NLS-1$
    }
    return RepositoryFilenameUtils.unescape( in );
  }

  // public static void main(final String[] args) {
  // System.out.println("'" + escape(null) + "'");
  // System.out.println("'" + escape("") + "'");
  // System.out.println("'" + escape("hello") + "'");
  // System.out.println("'" + escape("hell:o") + "'");
  // System.out.println("'" + escape("hello:") + "'");
  // System.out.println("'" + escape(":hello") + "'");
  // System.out.println("'" + escape("hell::o") + "'");
  // System.out.println("'" + escape("hell\\::o") + "'");
  //
  // System.out.println("'" + unescape(null) + "'");
  // System.out.println("'" + unescape("") + "'");
  // System.out.println("'" + unescape("hello") + "'");
  // System.out.println("'" + unescape("hell\\:o") + "'");
  // System.out.println("'" + unescape("hello\\:") + "'");
  // System.out.println("'" + unescape("\\:hello") + "'");
  // System.out.println("'" + unescape("hell\\:\\:o") + "'");
  // System.out.println("'" + unescape("hell\\\\:\\:o") + "'");
  //
  // System.out.println(Arrays.toString("su%3Azy:1332272120111:lock within versioned folder"
  // .split(LOCK_OWNER_INFO_SEPARATOR_REGEX)));
  // }
}
