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

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.observation.Event;
import javax.jcr.observation.ObservationManager;
import javax.jcr.security.AccessControlEntry;
import javax.jcr.security.AccessControlList;
import javax.jcr.security.AccessControlManager;
import javax.jcr.security.AccessControlPolicy;
import javax.jcr.security.Privilege;

import org.apache.jackrabbit.api.security.principal.PrincipalManager;
import org.apache.jackrabbit.core.NodeImpl;
import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.id.NodeId;
import org.apache.jackrabbit.core.security.authorization.CompiledPermissions;
import org.pentaho.platform.api.engine.ISystemConfig;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Customization of {@link ACLProvider}.
 * 
 * @author mlowery
 */
public class PentahoACLProvider extends ACLProvider {

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
  public void init( final Session systemSession, final Map conf ) throws RepositoryException {
    // this.configuration = conf; (from when we got it from repository.xml, we now override it on next line)
    overrideConf();
    ISystemConfig settings = PentahoSystem.get( ISystemConfig.class );
    if ( settings != null ) {
      useCachingEntryCollector = "true".equals( settings.getProperty( "system.cachingEntryCollector" ) );
    }
    super.init( systemSession, conf );
    // original initRootACL should run during super.init call above
    updateRootAcl( (SessionImpl) systemSession, new ACLEditor( session, this, false /* allowUnknownPrincipals */) );
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

  @SuppressWarnings( "unchecked" )
  private void overrideConf() {
    this.configuration = new Properties();
    // this magic ace gives admins full permission to their tenant root and all children
    configuration.put( "magicAceDefinition0", "{0};org.pentaho.security.administerSecurity;jcr:all;true;true;false" );
    // this magic ace gives abs read role permission to read all ancestors of the tenant, necessary for access of
    // repository
    configuration.put( "magicAceDefinition1",
        "{0};org.pentaho.repository.read;jcr:read,jcr:readAccessControl;true;false;true" );
    // this magic ace gives abs read role permission to read all /etc and children of /etc (except shared database
    // objects)
    configuration.put( "magicAceDefinition2",
        "{0}/etc;org.pentaho.repository.read;jcr:read,jcr:readAccessControl;true;true;false;{0}/etc/pdi/databases" );
    // this magic ace gives abs create role permission to read/write/acl all /etc and children of /etc (except shared
    // database objects)
    configuration.put( "magicAceDefinition3",
        "{0}/etc;org.pentaho.repository.create;jcr:read,jcr:readAccessControl,jcr:write,"
            + "jcr:modifyAccessControl,jcr:lockManagement,jcr:versionManagement,"
            + "jcr:nodeTypeManagement;true;true;false;{0}/etc/pdi/databases" );
    // this magic ace gives abs publish role permission to read/write/acl all /etc and children of /etc (except shared
    // database objects)
    configuration.put( "magicAceDefinition4",
        "{0}/etc;org.pentaho.security.publish;jcr:read,jcr:readAccessControl,jcr:write,"
            + "jcr:modifyAccessControl,jcr:lockManagement,jcr:versionManagement,"
            + "jcr:nodeTypeManagement;true;true;false;{0}/etc/pdi/databases" );
  }

}
