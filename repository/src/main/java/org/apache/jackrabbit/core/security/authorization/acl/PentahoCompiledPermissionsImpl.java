/*!
 *
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
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.apache.jackrabbit.core.security.authorization.acl;

import org.apache.jackrabbit.api.JackrabbitWorkspace;
import org.apache.jackrabbit.core.ItemManager;
import org.apache.jackrabbit.core.NodeImpl;
import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.cache.GrowingLRUMap;
import org.apache.jackrabbit.core.id.ItemId;
import org.apache.jackrabbit.core.id.NodeId;
import org.apache.jackrabbit.core.id.PropertyId;
import org.apache.jackrabbit.core.security.authorization.AbstractCompiledPermissions;
import org.apache.jackrabbit.core.security.authorization.AccessControlConstants;
import org.apache.jackrabbit.core.security.authorization.AccessControlListener;
import org.apache.jackrabbit.core.security.authorization.AccessControlModifications;
import org.apache.jackrabbit.core.security.authorization.AccessControlUtils;
import org.apache.jackrabbit.core.security.authorization.Permission;
import org.apache.jackrabbit.core.security.authorization.PrivilegeBits;
import org.apache.jackrabbit.core.security.authorization.PrivilegeManagerImpl;
import org.apache.jackrabbit.core.security.authorization.PrivilegeRegistry;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.Path;
import org.apache.jackrabbit.util.Text;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.repository2.unified.ServerRepositoryPaths;
import org.pentaho.platform.repository2.unified.jcr.JcrRepositoryFileAclUtils;
import org.pentaho.platform.repository2.unified.jcr.JcrRepositoryFileUtils;
import org.pentaho.platform.repository2.unified.jcr.PentahoJcrConstants;

import javax.jcr.ItemNotFoundException;
import javax.jcr.RepositoryException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A copy-paste of {@code CompiledPermissionsImpl} modified with more lenient locking on cache to prevent deadlocks
 * between {@link #clearCache} and {@link #canRead} as detailed in <a
 * href="http://jira.pentaho.com/browse/BISERVER-8382">BISERVER-8382</a><br/>
 * This shouldn't differ from {@code CompiledPermissionsImpl} except for the extra lock and the
 * <code>synchronized</code> changes within {@link #canRead(Path, ItemId)}
 *
 * @see CompiledPermissionsImpl
 */
public class PentahoCompiledPermissionsImpl extends AbstractCompiledPermissions implements AccessControlListener {

  private final List<String> principalNames;
  private final SessionImpl session;
  private final EntryCollector entryCollector;
  private final AccessControlUtils util;

  /*
   * Start with initial map size of 1024 and grow up to 5000 before removing LRU items.
   */
  @SuppressWarnings( "unchecked" )
  private final Map<ItemId, Boolean> readCache = new GrowingLRUMap( 1024, 5000 );
  private final Object monitor = new Object();
  private final Object readMonitor = new Object();

  PentahoCompiledPermissionsImpl( Set<Principal> principals, SessionImpl session, EntryCollector entryCollector,
                                  AccessControlUtils util, boolean listenToEvents ) throws RepositoryException {
    this.session = session;
    this.entryCollector = entryCollector;
    this.util = util;

    principalNames = new ArrayList<String>( principals.size() );
    for ( Principal princ : principals ) {
      principalNames.add( princ.getName() );
    }

    if ( listenToEvents ) {
      /*
       * Make sure this AclPermission recalculates the permissions if any ACL concerning it is modified.
       */
      entryCollector.addListener( this );
    }
  }

  private Result buildResult( NodeImpl node, boolean isExistingNode, boolean isAcItem, EntryFilterImpl filter )
    throws RepositoryException {
    // retrieve all ACEs at path or at the direct ancestor of path that
    // apply for the principal names.
    NodeImpl n = ACLProvider.getNode( node, isAcItem );
    Iterator entries = entryCollector.collectEntries( n, filter ).iterator();

    /*
     * Calculate privileges and permissions: Since the ACEs only define privileges on a node and do not allow to
     * add additional restrictions, the permissions can be determined without taking the given target name or
     * target item into account.
     */
    int allows = Permission.NONE;
    int denies = Permission.NONE;

    PrivilegeBits allowBits = PrivilegeBits.getInstance();
    PrivilegeBits denyBits = PrivilegeBits.getInstance();
    PrivilegeBits parentAllowBits = PrivilegeBits.getInstance();
    PrivilegeBits parentDenyBits = PrivilegeBits.getInstance();

    String parentPath = Text.getRelativeParent( filter.getPath(), 1 );
    NodeId nodeId = ( node == null ) ? null : node.getNodeId();

    while ( entries.hasNext() ) {

      Object ace = entries.next();

      /*
       * Determine if the ACE also takes effect on the parent: Some permissions (e.g. add-node or removal) must be
       * determined from privileges defined for the parent. A 'local' entry defined on the target node never
       * effects the parent. For inherited ACEs determine if the ACE matches the parent path.
       */

      PrivilegeBits entryBits = null;
      boolean isLocal = false;
      boolean matchesParent = false;
      boolean isAllow = false;

      if ( ace instanceof PentahoEntry ) {

        entryBits = ( ( (PentahoEntry) ace ).getPrivilegeBits() );
        isLocal = isExistingNode && ( (PentahoEntry) ace ).isLocal( nodeId );
        matchesParent = ( !isLocal && ( (PentahoEntry) ace ).matches( parentPath ) );
        isAllow = ( (PentahoEntry) ace ).isAllow();

      } else {

        entryBits = ( (Entry) ace ).getPrivilegeBits();
        isLocal = isExistingNode && ( (Entry) ace ).isLocal( nodeId );
        matchesParent = ( !isLocal && ( (Entry) ace ).matches( parentPath ) );
        isAllow = ( (Entry) ace ).isAllow();
      }

      // check specific case: "Inherit permissions" may have been unchecked, and node operation permissions may
      // have been granted directly to the item ( thus not requiring having those permissions defined for the parent )
      boolean isLocalAndDoesNotInheritPermissions =
        isLocal && isValidPentahoNode( node ) && !isEntriesInheriting( node );
      if ( matchesParent || isLocalAndDoesNotInheritPermissions ) {
        if ( isAllow ) {
          parentAllowBits.addDifference( entryBits, parentDenyBits );
        } else {
          parentDenyBits.addDifference( entryBits, parentAllowBits );
        }
      }
      if ( isAllow ) {
        allowBits.addDifference( entryBits, denyBits );
        int permissions = PrivilegeRegistry.calculatePermissions( allowBits, parentAllowBits, true, isAcItem );
        allows |= Permission.diff( permissions, denies );
      } else {
        denyBits.addDifference( entryBits, allowBits );
        int permissions = PrivilegeRegistry.calculatePermissions( denyBits, parentDenyBits, false, isAcItem );
        denies |= Permission.diff( permissions, allows );
      }
    }

    return new Result( allows, denies, allowBits, denyBits );
  }

  // ------------------------------------< AbstractCompiledPermissions >---

  /**
   * @see AbstractCompiledPermissions#buildResult(org.apache.jackrabbit.spi.Path)
   */
  @Override
  protected Result buildResult( Path absPath ) throws RepositoryException {
    boolean existingNode = false;
    NodeImpl node;

    ItemManager itemMgr = session.getItemManager();
    try {
      node = itemMgr.getNode( absPath );
      existingNode = true;
    } catch ( RepositoryException e ) {
      // path points to a non-persisted item.
      // -> find the nearest persisted node starting from the root.
      Path.Element[] elems = absPath.getElements();
      NodeImpl parent = (NodeImpl) session.getRootNode();
      for ( int i = 1; i < elems.length - 1; i++ ) {
        Name name = elems[ i ].getName();
        int index = elems[ i ].getIndex();
        if ( !parent.hasNode( name, index ) ) {
          // last persisted node reached
          break;
        }
        parent = parent.getNode( name, index );

      }
      node = parent;
    }

    if ( node == null ) {
      // should never get here
      throw new ItemNotFoundException( "Item out of hierarchy." );
    }

    boolean isAcItem = util.isAcItem( absPath );
    return buildResult( node, existingNode, isAcItem, new PentahoEntryFilterImpl( principalNames, absPath, session ) );
  }

  @Override
  protected Result buildRepositoryResult() throws RepositoryException {
    return buildResult( null, true, false, new EntryFilterImpl( principalNames, session.getQPath( "/" ), session ) );
  }

  /**
   * @see AbstractCompiledPermissions#getPrivilegeManagerImpl()
   */
  @Override
  protected PrivilegeManagerImpl getPrivilegeManagerImpl() throws RepositoryException {
    return (PrivilegeManagerImpl) ( (JackrabbitWorkspace) session.getWorkspace() ).getPrivilegeManager();
  }

  /**
   * @see AbstractCompiledPermissions#clearCache()
   */
  @Override
  protected void clearCache() {
    synchronized ( monitor ) {
      readCache.clear();
    }
    super.clearCache();
  }

  // --------------------------------------------< CompiledPermissions >---

  /**
   * @see org.apache.jackrabbit.core.security.authorization.CompiledPermissions#close()
   */
  @Override
  public void close() {
    entryCollector.removeListener( this );
    // NOTE: do not logout shared session.
    super.close();
  }

  /**
   * Changed so that access to entryCollector is done outside of a <code>monitor</code> synchronized block.<br/>
   * Should be functionally equivalent to {@link CompiledPermissions#canRead(Path, ItemId)}
   *
   * @see org.apache.jackrabbit.core.security.authorization.CompiledPermissions#canRead(Path, ItemId)
   */
  public boolean canRead( Path path, ItemId itemId ) throws RepositoryException {
    ItemId id = ( itemId == null ) ? session.getHierarchyManager().resolvePath( path ) : itemId;
    // no extra check for existence as method may only be called for existing items.
    boolean isExistingNode = id.denotesNode();
    boolean canRead = false;
    // emulates behavior of CompiledPermissionsImpl with two locks
    // synchronized (readMonitor) {
    synchronized ( monitor ) {
      if ( readCache.containsKey( id ) ) {
        return readCache.get( id );
      }
    }

    ItemManager itemMgr = session.getItemManager();
    NodeId nodeId = ( isExistingNode ) ? (NodeId) id : ( (PropertyId) id ).getParentId();
    NodeImpl node = (NodeImpl) itemMgr.getItem( nodeId );

    boolean isAcItem = util.isAcItem( node );
    EntryFilterImpl filter;
    if ( path == null ) {
      filter = new PentahoEntryFilterImpl( principalNames, id, session );
    } else {
      filter = new PentahoEntryFilterImpl( principalNames, path, session );
    }

    if ( isAcItem ) {
      /* item defines ac content -> regular evaluation */
      Result result = buildResult( node, isExistingNode, isAcItem, filter );
      canRead = result.grants( Permission.READ );
    } else {
      /*
       * simplified evaluation focusing on READ permission. this allows to omit evaluation of parent node
       * permissions that are required when calculating the complete set of permissions (see special treatment of
       * remove, create or ac-specific permissions).
       */

      for ( Object entry : entryCollector.collectEntries( node, filter ) ) {

        PrivilegeBits privilegeBits = null;
        boolean isAllow = false;

        if ( entry instanceof PentahoEntry ) {

          privilegeBits = ( (PentahoEntry) entry ).getPrivilegeBits();
          isAllow = ( (PentahoEntry) entry ).isAllow();

        } else {

          privilegeBits = ( (Entry) entry ).getPrivilegeBits();
          isAllow = ( (Entry) entry ).isAllow();

        }

        if ( privilegeBits.includesRead() ) {
          canRead = isAllow;
          break;
        }
      }
    }
    synchronized ( monitor ) {
      readCache.put( id, canRead );
    }
    // } // readMonitor
    return canRead;
  }

  // ----------------------------------------< ACLModificationListener >---

  /**
   * @see org.apache.jackrabbit.core.security.authorization.AccessControlListener# acModified(org.apache.jackrabbit
   * .core.security.authorization.AccessControlModifications)
   */
  public void acModified( AccessControlModifications modifications ) {
    // ignore the details of the modifications and clear all caches.
    clearCache();
  }

  /**
   * Returns stored entriesInheriting flag for given node
   */
  private boolean isEntriesInheriting( final NodeImpl node ) throws RepositoryException {
    NodeImpl aclNode = node.getNode( AccessControlConstants.N_POLICY );
    String path = aclNode != null ? aclNode.getParent().getPath() : null;
    return JcrRepositoryFileAclUtils.getAclMetadata( session, node.getPath(),
      new ACLTemplate( aclNode, path, false /* allowUnknownPrincipals */ ) ).isEntriesInheriting();
  }

  private boolean isBelowRootFolder( final NodeImpl node ) throws RepositoryException {

    final String tenantRootFolderPath = ServerRepositoryPaths.getTenantRootFolderPath() + RepositoryFile.SEPARATOR;

    return node != null && node.getPath().startsWith( tenantRootFolderPath )
      && node.getPath().replace( tenantRootFolderPath, "" ).length() > 0;
  }

  private boolean isValidPentahoNode( final NodeImpl node ) throws RepositoryException {
    return node != null && isBelowRootFolder( node )
      && JcrRepositoryFileUtils.isPentahoHierarchyNode( session, new PentahoJcrConstants( session ), node );
  }

  /**
   * We override this method as the superclass implementation caches permissions in an LRU cache.
   * Other than not caching the result the functionality is identical to {@link AbstractCompiledPermissions#getResult }
   *
   * @param absPath
   * @return CompiledPermissions
   * @throws RepositoryException
   */
  @Override
  public Result getResult( Path absPath ) throws RepositoryException {
    Result result;
    synchronized ( monitor ) {
      if ( absPath == null ) {
        result = buildRepositoryResult();
      } else {
        result = buildResult( absPath );
      }
    }
    return result;
  }
}
