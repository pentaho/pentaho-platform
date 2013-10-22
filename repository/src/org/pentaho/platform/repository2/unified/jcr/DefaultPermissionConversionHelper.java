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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.repository2.unified.IPentahoJCRPrivilege;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.repository2.unified.jcr.JcrRepositoryFileAclDao.IPermissionConversionHelper;
import org.springframework.util.Assert;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.security.Privilege;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

/**
 * Default {@link IPermissionConversionHelper} implementation.
 * 
 * @author mlowery
 */
public class DefaultPermissionConversionHelper implements IPermissionConversionHelper {

  // ~ Static fields/initializers
  // ======================================================================================

  private static final Log logger = LogFactory.getLog( DefaultPermissionConversionHelper.class );

  // ~ Instance fields
  // =================================================================================================

  protected Multimap<RepositoryFilePermission, String> permissionEnumToPrivilegeNamesMap;

  protected Multimap<String, RepositoryFilePermission> privilegeNameToPermissionEnumsMap;

  // ~ Constructors
  // ====================================================================================================

  public DefaultPermissionConversionHelper( final Session session ) {
    super();
    initMaps( session );

  }

  // ~ Methods
  // =========================================================================================================

  public Privilege[] pentahoPermissionsToPrivileges( final Session session,
      final EnumSet<RepositoryFilePermission> permissions ) throws RepositoryException {
    Assert.notNull( session );
    Assert.notNull( permissions );
    Assert.notEmpty( permissions );

    Set<Privilege> privileges = new HashSet<Privilege>();

    for ( RepositoryFilePermission currentPermission : permissions ) {
      if ( permissionEnumToPrivilegeNamesMap.containsKey( currentPermission ) ) {
        Collection<String> privNames = permissionEnumToPrivilegeNamesMap.get( currentPermission );
        for ( String privName : privNames ) {
          privileges.add( session.getAccessControlManager().privilegeFromName( privName ) );
        }
      } else {
        logger.debug( "skipping permission=" + currentPermission + " as it doesn't have any corresponding privileges" ); //$NON-NLS-1$//$NON-NLS-2$
      }
    }

    Assert.isTrue( !privileges.isEmpty(), "no privileges; see previous 'skipping permission' messages" );

    return privileges.toArray( new Privilege[0] );
  }

  public EnumSet<RepositoryFilePermission> privilegesToPentahoPermissions( final Session session,
      final Privilege[] privileges ) throws RepositoryException {
    Assert.notNull( session );
    Assert.notNull( privileges );

    new PentahoJcrConstants( session );
    EnumSet<RepositoryFilePermission> permissions = EnumSet.noneOf( RepositoryFilePermission.class );

    Privilege[] expandedPrivileges = JcrRepositoryFileAclUtils.expandPrivileges( privileges, true );

    for ( Privilege privilege : expandedPrivileges ) {
      // this privilege name is of the format xyz:blah where xyz is the namespace prefix;
      // convert it to match the Privilege.JCR_* string constants
      String extendedPrivilegeName = privilege.getName();
      String privilegeName = privilege.getName();
      int colonIndex = privilegeName.indexOf( ":" ); //$NON-NLS-1$
      if ( colonIndex > -1 ) {
        String namespaceUri = session.getNamespaceURI( privilegeName.substring( 0, colonIndex ) );
        extendedPrivilegeName = "{" + namespaceUri + "}" + privilegeName.substring( colonIndex + 1 ); //$NON-NLS-1$ //$NON-NLS-2$
      }

      if ( privilegeNameToPermissionEnumsMap.containsKey( extendedPrivilegeName ) ) {
        Collection<RepositoryFilePermission> permEnums = privilegeNameToPermissionEnumsMap.get( extendedPrivilegeName );
        for ( RepositoryFilePermission perm : permEnums ) {
          permissions.add( perm );
        }
      } else {
        logger.debug( "skipping privilege with name=" + extendedPrivilegeName //$NON-NLS-1$
            + " as it doesn't have any corresponding permissions" ); //$NON-NLS-1$
      }
    }

    Assert.isTrue( !permissions.isEmpty(), "no permissions; see previous 'skipping privilege' messages" );

    return permissions;
  }

  protected void initMaps( final Session session ) {
    new PentahoJcrConstants( session );
    permissionEnumToPrivilegeNamesMap = HashMultimap.create();

    // READ
    permissionEnumToPrivilegeNamesMap.put( RepositoryFilePermission.READ, Privilege.JCR_READ );
    permissionEnumToPrivilegeNamesMap.put( RepositoryFilePermission.READ, Privilege.JCR_READ_ACCESS_CONTROL );

    // DELETE
    permissionEnumToPrivilegeNamesMap.put( RepositoryFilePermission.DELETE, Privilege.JCR_REMOVE_NODE );

    // WRITE

    permissionEnumToPrivilegeNamesMap.put( RepositoryFilePermission.WRITE, Privilege.JCR_ADD_CHILD_NODES );
    permissionEnumToPrivilegeNamesMap.put( RepositoryFilePermission.WRITE, Privilege.JCR_REMOVE_CHILD_NODES );
    permissionEnumToPrivilegeNamesMap.put( RepositoryFilePermission.WRITE, Privilege.JCR_VERSION_MANAGEMENT );
    permissionEnumToPrivilegeNamesMap.put( RepositoryFilePermission.WRITE, Privilege.JCR_LOCK_MANAGEMENT );
    permissionEnumToPrivilegeNamesMap.put( RepositoryFilePermission.WRITE, Privilege.JCR_MODIFY_PROPERTIES );
    permissionEnumToPrivilegeNamesMap.put( RepositoryFilePermission.WRITE, Privilege.JCR_NODE_TYPE_MANAGEMENT );
    permissionEnumToPrivilegeNamesMap.put( RepositoryFilePermission.WRITE, Privilege.JCR_MODIFY_ACCESS_CONTROL );

    // ACL_MANAGEMENT
    permissionEnumToPrivilegeNamesMap.put( RepositoryFilePermission.ACL_MANAGEMENT,
        IPentahoJCRPrivilege.PHO_ACLMANAGEMENT );

    // ALL
    permissionEnumToPrivilegeNamesMap.put( RepositoryFilePermission.ALL, Privilege.JCR_ALL );
    permissionEnumToPrivilegeNamesMap.put( RepositoryFilePermission.ALL, IPentahoJCRPrivilege.PHO_ACLMANAGEMENT );

    privilegeNameToPermissionEnumsMap = HashMultimap.create();

    // JCR_READ + JCR_READ_ACCESS_CONTROL
    privilegeNameToPermissionEnumsMap.put( Privilege.JCR_READ, RepositoryFilePermission.READ );
    privilegeNameToPermissionEnumsMap.put( Privilege.JCR_READ_ACCESS_CONTROL, RepositoryFilePermission.READ );

    // JCR_REMOVE_NODE
    privilegeNameToPermissionEnumsMap.put( Privilege.JCR_REMOVE_NODE, RepositoryFilePermission.DELETE );

    // Custom Pentaho Permission
    privilegeNameToPermissionEnumsMap.put( IPentahoJCRPrivilege.PHO_ACLMANAGEMENT,
        RepositoryFilePermission.ACL_MANAGEMENT );

    // JCR_WRITE
    privilegeNameToPermissionEnumsMap.put( Privilege.JCR_ADD_CHILD_NODES, RepositoryFilePermission.WRITE );
    privilegeNameToPermissionEnumsMap.put( Privilege.JCR_REMOVE_CHILD_NODES, RepositoryFilePermission.WRITE );
    privilegeNameToPermissionEnumsMap.put( Privilege.JCR_VERSION_MANAGEMENT, RepositoryFilePermission.WRITE );
    privilegeNameToPermissionEnumsMap.put( Privilege.JCR_LOCK_MANAGEMENT, RepositoryFilePermission.WRITE );
    privilegeNameToPermissionEnumsMap.put( Privilege.JCR_MODIFY_PROPERTIES, RepositoryFilePermission.WRITE );
    privilegeNameToPermissionEnumsMap.put( Privilege.JCR_NODE_TYPE_MANAGEMENT, RepositoryFilePermission.WRITE );
    privilegeNameToPermissionEnumsMap.put( Privilege.JCR_MODIFY_ACCESS_CONTROL, RepositoryFilePermission.WRITE );

    // JCR_ALL
    privilegeNameToPermissionEnumsMap.put( Privilege.JCR_ALL, RepositoryFilePermission.ALL );
    privilegeNameToPermissionEnumsMap.put( IPentahoJCRPrivilege.PHO_ACLMANAGEMENT, RepositoryFilePermission.ALL );

    // None of the following translate into a RepositoryFilePermission:
    // JCR_RETENTION_MANAGEMENT
    // JCR_LIFECYCLE_MANAGEMENT
  }

}
