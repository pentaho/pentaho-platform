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
import org.apache.jackrabbit.api.security.user.Group;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAce;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid;
import org.pentaho.platform.repository2.messages.Messages;
import org.pentaho.platform.repository2.unified.jcr.IAclMetadataStrategy.AclMetadata;
import org.pentaho.platform.repository2.unified.jcr.JcrRepositoryFileAclDao.IPermissionConversionHelper;
import org.pentaho.platform.repository2.unified.jcr.jackrabbit.security.SpringSecurityRolePrincipal;
import org.pentaho.platform.repository2.unified.jcr.jackrabbit.security.SpringSecurityUserPrincipal;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.security.AccessControlEntry;
import javax.jcr.security.AccessControlList;
import javax.jcr.security.AccessControlManager;
import javax.jcr.security.AccessControlPolicy;
import javax.jcr.security.AccessControlPolicyIterator;
import javax.jcr.security.Privilege;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.security.Principal;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * ACL utilities.
 * 
 * <p>
 * These utility methods are static because they are used from within Jackrabbit.
 * </p>
 * 
 * @author mlowery
 */
public class JcrRepositoryFileAclUtils {

  // ~ Static fields/initializers
  // ======================================================================================

  private static final Log logger = LogFactory.getLog( JcrRepositoryFileAclUtils.class );

  public static final String DEFAULT = "DEFAULT"; //$NON-NLS-1$

  public static final String SYSTEM_PROPERTY = "pentaho.repository.server.aclMetadataStrategy"; //$NON-NLS-1$

  private static String strategyName = System.getProperty( SYSTEM_PROPERTY );

  private static IAclMetadataStrategy strategy;

  static {
    initialize();
  }

  // ~ Instance fields
  // =================================================================================================

  // ~ Constructors
  // ====================================================================================================

  private JcrRepositoryFileAclUtils() {
    super();
  }

  // ~ Methods
  // =========================================================================================================

  private static void initialize() {
    if ( ( strategyName == null ) || "".equals( strategyName ) ) { //$NON-NLS-1$
      strategyName = DEFAULT;
    }

    if ( strategyName.equals( DEFAULT ) ) {
      strategy = new JcrAclMetadataStrategy();
    } else {
      // Try to load a custom strategy
      try {
        Class<?> clazz = Class.forName( strategyName );
        Constructor<?> customStrategy = clazz.getConstructor( new Class[] {} );
        strategy = (IAclMetadataStrategy) customStrategy.newInstance( new Object[] {} );
      } catch ( Exception e ) {
        throw new RuntimeException( e );
      }
    }

    logger.debug( "JcrRepositoryFileAclUtils initialized: strategy=" + strategyName ); //$NON-NLS-1$
  }

  public static AclMetadata getAclMetadata( final Session session, final String path, final AccessControlList acList )
    throws RepositoryException {
    return strategy.getAclMetadata( session, path, acList );
  }

  public static void setAclMetadata( final Session session, final String path, final AccessControlList acList,
      final AclMetadata aclMetadata ) throws RepositoryException {
    strategy.setAclMetadata( session, path, acList, aclMetadata );
  }

  public static List<AccessControlEntry> removeAclMetadata( final List<AccessControlEntry> acEntries )
    throws RepositoryException {
    return strategy.removeAclMetadata( acEntries );
  }

  /**
   * Expands all aggregate privileges.
   * 
   * @param privileges
   *          input privileges
   * @param expandNonStandardOnly
   *          if {@code true} expand only privileges outside of jcr: namespace
   * @return expanded privileges
   */
  public static Privilege[] expandPrivileges( final Privilege[] privileges, final boolean expandNonStandardOnly ) {
    // find all aggregate privileges and expand
    Set<Privilege> expandedPrivileges = new HashSet<Privilege>();
    expandedPrivileges.addAll( Arrays.asList( privileges ) );
    while ( true ) {
      boolean foundAggregatePrivilege = false;
      Set<Privilege> iterable = new HashSet<Privilege>( expandedPrivileges );
      for ( Privilege privilege : iterable ) {
        // expand impl custom privileges (e.g. rep:write) but keep aggregates like jcr:write intact
        if ( !expandNonStandardOnly || expandNonStandardOnly && !privilege.getName().startsWith( "jcr:" ) ) { //$NON-NLS-1$
          if ( privilege.isAggregate() ) {
            expandedPrivileges.remove( privilege );
            expandedPrivileges.addAll( Arrays.asList( privilege.getAggregatePrivileges() ) );
            foundAggregatePrivilege = true;
          }
        }
      }
      if ( !foundAggregatePrivilege ) {
        break;
      }
    }
    return expandedPrivileges.toArray( new Privilege[0] );
  }

  public static RepositoryFileAcl createAcl( Session session, PentahoJcrConstants pentahoJcrConstants,
      Serializable fileId, RepositoryFileAcl acl ) throws ItemNotFoundException, RepositoryException {
    Node node = session.getNodeByIdentifier( fileId.toString() );
    String absPath = node.getPath();
    AccessControlManager acMgr = session.getAccessControlManager();
    AccessControlList acList = getAccessControlList( acMgr, absPath );
    acMgr.setPolicy( absPath, acList );
    return internalUpdateAcl( session, pentahoJcrConstants, fileId, acl );
  }

  public static void
  addPermission( final Session session, final PentahoJcrConstants pentahoJcrConstants,
                   final Serializable fileId,
      final RepositoryFileSid recipient, final EnumSet<RepositoryFilePermission> permissions )
    throws RepositoryException {
    addAce( session, pentahoJcrConstants, fileId, recipient, permissions );
  }

  public static void setOwner( final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final RepositoryFile file, final RepositoryFileSid owner ) throws RepositoryException {
    RepositoryFileSid newOwnerSid = owner;
    if ( JcrTenantUtils.getUserNameUtils().getTenant( owner.getName() ) == null ) {
      newOwnerSid = new RepositoryFileSid( JcrTenantUtils.getTenantedUser( owner.getName() ), owner.getType() );
    }
    RepositoryFileAcl acl = getAcl( session, pentahoJcrConstants, file.getId() );
    RepositoryFileAcl newAcl = new RepositoryFileAcl.Builder( acl ).owner( newOwnerSid ).build();
    updateAcl( session, newAcl );
  }

  public static void setFullControl( final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Serializable fileId, final RepositoryFileSid sid ) throws RepositoryException {
    addAce( session, pentahoJcrConstants, fileId, sid, EnumSet.of( RepositoryFilePermission.ALL ) );
  }

  public static void addAce( final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Serializable id, final RepositoryFileSid recipient, final EnumSet<RepositoryFilePermission> permission )
    throws RepositoryException {
    RepositoryFileSid newRecipient = recipient;
    if ( JcrTenantUtils.getUserNameUtils().getTenant( recipient.getName() ) == null ) {
      newRecipient = new RepositoryFileSid( JcrTenantUtils.getTenantedUser( recipient.getName() ),
        recipient.getType() );
    }
    RepositoryFileAcl acl = getAcl( session, pentahoJcrConstants, id );
    RepositoryFileAcl updatedAcl = new RepositoryFileAcl.Builder( acl ).ace( newRecipient, permission ).build();
    updateAcl( session, updatedAcl );
  }

  private static RepositoryFileAcl internalUpdateAcl( final Session session,
      final PentahoJcrConstants pentahoJcrConstants, final Serializable fileId, final RepositoryFileAcl acl )
    throws RepositoryException {
    Node node = session.getNodeByIdentifier( fileId.toString() );
    if ( node == null ) {
      throw new RepositoryException( "Node not found" ); //$NON-NLS-1$
    }
    String absPath = node.getPath();
    AccessControlManager acMgr = session.getAccessControlManager();
    AccessControlList acList = getAccessControlList( acMgr, absPath );

    // clear all entries
    AccessControlEntry[] acEntries = acList.getAccessControlEntries();
    for ( int i = 0; i < acEntries.length; i++ ) {
      acList.removeAccessControlEntry( acEntries[i] );
    }

    JcrRepositoryFileAclUtils.setAclMetadata( session, absPath, acList, new AclMetadata( acl.getOwner().getName(), acl
        .isEntriesInheriting() ) );

    // add entries to now empty list but only if not inheriting; force user to start with clean slate
    if ( !acl.isEntriesInheriting() ) {
      for ( RepositoryFileAce ace : acl.getAces() ) {
        Principal principal = null;
        if ( RepositoryFileSid.Type.ROLE == ace.getSid().getType() ) {
          principal = new SpringSecurityRolePrincipal( JcrTenantUtils.getTenantedRole( ace.getSid().getName() ) );
        } else {
          principal = new SpringSecurityUserPrincipal( JcrTenantUtils.getTenantedUser( ace.getSid().getName() ) );
        }
        IPermissionConversionHelper permissionConversionHelper = new DefaultPermissionConversionHelper( session );
        acList.addAccessControlEntry( principal, permissionConversionHelper.pentahoPermissionsToPrivileges( session,
            ace.getPermissions() ) );
      }
    }
    acMgr.setPolicy( absPath, acList );
    session.save();
    return getAcl( session, pentahoJcrConstants, fileId );
  }

  public static void updateAcl( final Session session, final RepositoryFileAcl acl ) throws RepositoryException {
    PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants( session );
    JcrRepositoryFileUtils.checkoutNearestVersionableFileIfNecessary( session, pentahoJcrConstants, acl.getId() );
    internalUpdateAcl( session, pentahoJcrConstants, acl.getId(), acl );
    JcrRepositoryFileUtils.checkinNearestVersionableFileIfNecessary( session, pentahoJcrConstants, acl.getId(), null,
        null, true );
  }

  public static RepositoryFileAcl getAcl( final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Serializable id ) throws RepositoryException {

    Node node = session.getNodeByIdentifier( id.toString() );
    if ( node == null ) {
      throw new RepositoryException( Messages.getInstance().getString(
          "JackrabbitRepositoryFileAclDao.ERROR_0001_NODE_NOT_FOUND", id.toString() ) ); //$NON-NLS-1$
    }
    String absPath = node.getPath();
    AccessControlManager acMgr = session.getAccessControlManager();
    AccessControlList acList = getAccessControlList( acMgr, absPath );

    RepositoryFileSid owner = null;
    String ownerString = JcrTenantUtils.getUserNameUtils().getPrincipleName( getOwner( session, absPath, acList ) );

    if ( ownerString != null ) {
      // for now, just assume all owners are users; only has UI impact
      owner = new RepositoryFileSid( ownerString, RepositoryFileSid.Type.USER );
    }

    RepositoryFileAcl.Builder aclBuilder = new RepositoryFileAcl.Builder( id, owner );

    aclBuilder.entriesInheriting( isEntriesInheriting( session, absPath, acList ) );

    List<AccessControlEntry> cleanedAcEntries =
        JcrRepositoryFileAclUtils.removeAclMetadata( Arrays.asList( acList.getAccessControlEntries() ) );

    for ( AccessControlEntry acEntry : cleanedAcEntries ) {
      aclBuilder.ace( toAce( session, acEntry ) );
    }
    return aclBuilder.build();

  }

  private static AccessControlList getAccessControlList( final AccessControlManager acMgr, final String path )
    throws RepositoryException {
    AccessControlPolicyIterator applicablePolicies = acMgr.getApplicablePolicies( path );
    while ( applicablePolicies.hasNext() ) {
      AccessControlPolicy policy = applicablePolicies.nextAccessControlPolicy();
      if ( policy instanceof AccessControlList ) {
        return (AccessControlList) policy;
      }
    }
    AccessControlPolicy[] policies = acMgr.getPolicies( path );
    for ( int i = 0; i < policies.length; i++ ) {
      if ( policies[i] instanceof AccessControlList ) {
        return (AccessControlList) policies[i];
      }
    }
    throw new IllegalStateException( "no access control list applies or is bound to node" );
  }

  private static String getOwner( final Session session, final String path, final AccessControlList acList )
    throws RepositoryException {
    AclMetadata aclMetadata = JcrRepositoryFileAclUtils.getAclMetadata( session, path, acList );
    if ( aclMetadata != null ) {
      return aclMetadata.getOwner();
    } else {
      return null;
    }
  }

  private static boolean isEntriesInheriting( final Session session, final String path, final AccessControlList acList )
    throws RepositoryException {
    AclMetadata aclMetadata = JcrRepositoryFileAclUtils.getAclMetadata( session, path, acList );
    if ( aclMetadata != null ) {
      return aclMetadata.isEntriesInheriting();
    } else {
      return false;
    }
  }

  private static RepositoryFileAce toAce( final Session session, final AccessControlEntry acEntry )
    throws RepositoryException {
    Principal principal = acEntry.getPrincipal();
    RepositoryFileSid sid = null;
    if ( principal instanceof Group ) {
      sid = new RepositoryFileSid( principal.getName(), RepositoryFileSid.Type.ROLE );
    } else {
      sid = new RepositoryFileSid( principal.getName(), RepositoryFileSid.Type.USER );
    }
    Privilege[] privileges = acEntry.getPrivileges();
    IPermissionConversionHelper permissionConversionHelper = new DefaultPermissionConversionHelper( session );
    return new RepositoryFileAce( sid, permissionConversionHelper.privilegesToPentahoPermissions( session,
      privileges ) );
  }

}
