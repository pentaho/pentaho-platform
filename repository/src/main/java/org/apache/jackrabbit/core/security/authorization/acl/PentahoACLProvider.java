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

import org.apache.jackrabbit.api.security.principal.PrincipalManager;
import org.apache.jackrabbit.core.NodeImpl;
import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.id.NodeId;
import org.apache.jackrabbit.core.security.authorization.CompiledPermissions;
import org.pentaho.platform.api.engine.ISystemConfig;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.observation.Event;
import javax.jcr.observation.ObservationManager;
import javax.jcr.security.AccessControlEntry;
import javax.jcr.security.AccessControlList;
import javax.jcr.security.AccessControlManager;
import javax.jcr.security.AccessControlPolicy;
import javax.jcr.security.Privilege;
import java.security.Principal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Customization of {@link ACLProvider}.
 * 
 * @author mlowery
 */
public class PentahoACLProvider extends ACLProvider {

  @SuppressWarnings( "rawtypes" )
  private Map configuration;

  // Overrides to CompiledPermissions creation require we keep an extra reference
  // because this is private in ACLProvider
  private EntryCollector entryCollector;
  private Map<Integer, PentahoCompiledPermissionsImpl> compiledPermissionsCache =
      new HashMap<Integer, PentahoCompiledPermissionsImpl>();
  private boolean useCachingEntryCollector;
  private Logger logger = LoggerFactory.getLogger( getClass().getName() );
  private boolean initialized;

  /**
   * Overridden to:
   * <ul>
   * <li>Store {@code configuration} for later passing to {@link PentahoEntryCollector}.</li>
   * <li>Add JCR_READ_ACCESS_CONTROL to root ACL. This is harmless and avoids more customization.</li>
   * </ul>
   */
  @Override
  @SuppressWarnings( "rawtypes" )
  public void init( final Session systemSession, final Map conf ) throws RepositoryException {
    this.configuration = conf;
    ISystemConfig settings = PentahoSystem.get( ISystemConfig.class );
    if ( settings != null ) {
      useCachingEntryCollector = "true".equals( settings.getProperty( "system.cachingEntryCollector" ) );
    }
    super.init( systemSession, conf );
    // original initRootACL should run during super.init call above
    updateRootAcl( (SessionImpl) systemSession, new ACLEditor( session, this, false /* allowUnknownPrincipals */ ) );
    this.initialized = true;
    registerEntryCollectorWithObservationManager( systemSession );
  }

  protected void registerEntryCollectorWithObservationManager( Session systemSession ) throws RepositoryException {
    // Register Entry Collector to receive node events
    if ( entryCollector != null && this.initialized ) {
      ObservationManager observationMgr = systemSession.getWorkspace().getObservationManager();
      observationMgr.addEventListener( entryCollector, Event.NODE_ADDED | Event.NODE_REMOVED | Event.NODE_REMOVED, "/",
          true, null, null, false );
    }

  }

  /**
   * Adds ACE so that everyone can read access control. This allows Jackrabbit's default collectAcls to work without
   * change. Otherwise, you have to be an admin to call acMgr.getEffectivePolicies.
   */
  protected void updateRootAcl( SessionImpl systemSession, ACLEditor editor ) throws RepositoryException {
    String rootPath = session.getRootNode().getPath();
    AccessControlPolicy[] acls = editor.getPolicies( rootPath );
    if ( acls.length > 0 ) {
      PrincipalManager pMgr = systemSession.getPrincipalManager();
      AccessControlManager acMgr = session.getAccessControlManager();
      Principal everyone = pMgr.getEveryone();
      Privilege[] privs =
          new Privilege[] { acMgr.privilegeFromName( Privilege.JCR_READ ),
            acMgr.privilegeFromName( Privilege.JCR_READ_ACCESS_CONTROL ) };
      AccessControlList acList = (AccessControlList) acls[0];
      AccessControlEntry[] acEntries = acList.getAccessControlEntries();
      for ( AccessControlEntry acEntry : acEntries ) {
        if ( acEntry.getPrincipal().equals( everyone ) ) {
          acList.removeAccessControlEntry( acEntry );
        }
      }
      acList.addAccessControlEntry( everyone, privs );
      editor.setPolicy( rootPath, acList );
      session.save();
    }
  }

  /**
   * Returns true if the root acl needs updating (if the JCR_READ_ACCESS_CONTROL privilege is missing from the
   * 'everyone' principle) and false otherwise.
   */
  protected boolean requireRootAclUpdate( ACLEditor editor ) throws RepositoryException {
    final String rootPath = session.getRootNode().getPath();
    final AccessControlPolicy[] acls = editor.getPolicies( rootPath );
    if ( acls != null &&  acls.length > 0 ) {
      final PrincipalManager pMgr = session.getPrincipalManager();
      final AccessControlManager acMgr = session.getAccessControlManager();
      final Privilege jcrReadAccessControlPriv =  acMgr.privilegeFromName( Privilege.JCR_READ_ACCESS_CONTROL );
      final Principal everyone = pMgr.getEveryone();
      final AccessControlList acList = (AccessControlList) acls[0];
      final AccessControlEntry[] acEntries = acList.getAccessControlEntries();
      if ( acEntries != null ) {
        for ( AccessControlEntry acEntry : acEntries ) {
          if ( acEntry.getPrincipal() != null && acEntry.getPrincipal().equals( everyone )
            && acEntry.getPrivileges() != null ) {
            return !Arrays.asList( acEntry.getPrivileges() ).contains( jcrReadAccessControlPriv );
          }
        }
      }
    }
    return true;
  }

  /**
   * Overridden to:
   * <ul>
   * <li>Return custom {@code EntryCollector}.
   * <li>Later access to the {@code EntryCollector}
   * </ul>
   */
  @Override
  protected EntryCollector createEntryCollector( SessionImpl systemSession ) throws RepositoryException {
    if ( entryCollector != null ) {
      return entryCollector;
    }
    // keep our own private reference; the one in ACLProvider is private
    if ( useCachingEntryCollector ) {
      entryCollector = new CachingPentahoEntryCollector( systemSession, getRootNodeId(), configuration );
      logger.debug( "Using Caching EntryCollector" );
    } else {
      entryCollector = new PentahoEntryCollector( systemSession, getRootNodeId(), configuration );
      logger.debug( "Using Non-Caching EntryCollector" );
    }

    registerEntryCollectorWithObservationManager( systemSession );

    return entryCollector;
  }

  /**
   * Overridden to:
   * <ul>
   * <li>Return custom {@code CompiledPermissions}.
   * </ul>
   * 
   * @see PentahoCompiledPermissionsImpl
   */
  @Override
  public CompiledPermissions compilePermissions( Set<Principal> principals ) throws RepositoryException {
    checkInitialized();
    if ( isAdminOrSystem( principals ) ) {
      return getAdminPermissions();
    } else if ( isReadOnly( principals ) ) {
      return getReadOnlyPermissions();
    } else {
      return getCompiledPermissions( principals );
    }
  }

  protected PentahoCompiledPermissionsImpl getCompiledPermissions( Set<Principal> principals )
    throws RepositoryException {
    // check the cache first
    if ( compiledPermissionsCache.containsKey( principals.hashCode() ) ) {
      return compiledPermissionsCache.get( principals.hashCode() );
    }

    PentahoCompiledPermissionsImpl compiledPermissions =
        new PentahoCompiledPermissionsImpl( principals, session, entryCollector, this, true );
    compiledPermissionsCache.put( principals.hashCode(), compiledPermissions );
    return compiledPermissions;
  }

  /**
   * Overridden to:
   * <ul>
   * <li>Use custom {@code CompiledPermissions}.
   * </ul>
   * 
   * @see PentahoCompiledPermissionsImpl
   */
  @Override
  public boolean canAccessRoot( Set<Principal> principals ) throws RepositoryException {
    checkInitialized();
    if ( isAdminOrSystem( principals ) ) {

      return true;
    } else {
      CompiledPermissions cp = getCompiledPermissions( principals );
      try {
        return cp.canRead( null, getRootNodeId() );
      } finally {
        cp.close();
      }
    }
  }

  private NodeId getRootNodeId() throws RepositoryException {
    // TODO: how expensive is this? Should we keep a reference?
    return ( (NodeImpl) session.getRootNode() ).getNodeId();
  }
}
