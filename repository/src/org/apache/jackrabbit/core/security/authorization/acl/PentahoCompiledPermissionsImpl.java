/*!
 * Copyright 2010 - 2013 Pentaho Corporation.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.jackrabbit.core.security.authorization.acl;

import org.apache.jackrabbit.api.JackrabbitWorkspace;
import org.apache.jackrabbit.core.ItemImpl;
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
import javax.jcr.security.AccessControlEntry;
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
    Iterator<AccessControlEntry> entries = entryCollector.collectEntries( n, filter ).iterator();

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
      ACLTemplate.Entry ace = (ACLTemplate.Entry) entries.next();
      /*
       * Determine if the ACE also takes effect on the parent: Some permissions (e.g. add-node or removal) must be
       * determined from privileges defined for the parent. A 'local' entry defined on the target node never
       * effects the parent. For inherited ACEs determine if the ACE matches the parent path.
       */
      PrivilegeBits entryBits = ace.getPrivilegeBits();
      boolean isLocal = isExistingNode && ace.isLocal( nodeId );
      boolean matchesParent = ( !isLocal && ace.matches( parentPath ) );

      // check specific case: "Inherit permissions" may have been unchecked, and node operation permissions may
      // have been granted directly to the item ( thus not requiring having those permissions defined for the parent )
      boolean isLocalAndDoesNotInheritPermissions = isLocal && isValidPentahoNode( node ) && !isEntriesInheriting( node );
      if ( matchesParent || isLocalAndDoesNotInheritPermissions ) {
        if ( ace.isAllow() ) {
          parentAllowBits.addDifference( entryBits, parentDenyBits );
        } else {
          parentDenyBits.addDifference( entryBits, parentAllowBits );
        }
      }
      if ( ace.isAllow() ) {
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
      ItemImpl item = itemMgr.getItem( absPath );
      if ( item.isNode() ) {
        node = (NodeImpl) item;
        existingNode = true;
      } else {
        node = (NodeImpl) item.getParent();
      }
    } catch ( RepositoryException e ) {
      // path points to a non-persisted item.
      // -> find the nearest persisted node starting from the root.
      Path.Element[] elems = absPath.getElements();
      NodeImpl parent = (NodeImpl) session.getRootNode();
      for ( int i = 1; i < elems.length - 1; i++ ) {
        Name name = elems[i].getName();
        int index = elems[i].getIndex();
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
    return buildResult( node, existingNode, isAcItem, new EntryFilterImpl( principalNames, absPath, session ) );
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
      filter = new EntryFilterImpl( principalNames, id, session );
    } else {
      filter = new EntryFilterImpl( principalNames, path, session );
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
      for ( AccessControlEntry accessControlEntry : entryCollector.collectEntries( node, filter ) ) {
        ACLTemplate.Entry ace = (ACLTemplate.Entry) accessControlEntry;
        if ( ace.getPrivilegeBits().includesRead() ) {
          canRead = ace.isAllow();
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
   * @see org.apache.jackrabbit.core.security.authorization.AccessControlListener#
   *      acModified(org.apache.jackrabbit.core.security.authorization.AccessControlModifications)
   */
  public void acModified( AccessControlModifications modifications ) {
    // ignore the details of the modifications and clear all caches.
    clearCache();
  }

  /**
   * Returns stored entriesInheriting flag for given node
   */
  private boolean isEntriesInheriting( final NodeImpl node ) throws RepositoryException {
   return JcrRepositoryFileAclUtils.getAclMetadata( session, node.getPath(), new ACLTemplate( node.getNode(
        AccessControlConstants.N_POLICY ) ) ).isEntriesInheriting();
  }

  private boolean isBelowRootFolder( final NodeImpl node ) throws RepositoryException {

    final String tenantRootFolderPath = ServerRepositoryPaths.getTenantRootFolderPath() + RepositoryFile.SEPARATOR;

    return node != null && node.getPath().startsWith( tenantRootFolderPath )
            && node.getPath().replace( tenantRootFolderPath , "" ).length() > 0;
  }

  private boolean isValidPentahoNode( final NodeImpl node ) throws RepositoryException {
    return node != null && isBelowRootFolder( node )
            && JcrRepositoryFileUtils.isPentahoHierarchyNode( session, new PentahoJcrConstants( session ), node );
  }
}
