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

package org.pentaho.platform.engine.security.acls;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.pentaho.platform.api.engine.IPentahoAclObjectIdentity;
import org.pentaho.platform.api.engine.IPentahoBasicAclEntry;

import java.util.Arrays;

/**
 * This is a port from spring-security 2.0.8.RELEASE
 * @see https://github.com/spring-projects/spring-security/blob/2.0.8.RELEASE/core/src/main/java/org/springframework/security/acl/basic/AbstractBasicAclEntry.java
 */
public abstract class PentahoAbstractBasicAclEntry implements IPentahoBasicAclEntry {
  //~ Static fields/initializers =====================================================================================

  private static final Log logger = LogFactory.getLog( PentahoAbstractBasicAclEntry.class );

  //~ Instance fields ================================================================================================

  private IPentahoAclObjectIdentity aclObjectIdentity;
  private IPentahoAclObjectIdentity aclObjectParentIdentity;
  private Object recipient;
  private int[] validPermissions;
  private int mask = 0; // default means no permissions

  //~ Constructors ===================================================================================================

  public PentahoAbstractBasicAclEntry( Object recipient, IPentahoAclObjectIdentity aclObjectIdentity,
                               IPentahoAclObjectIdentity aclObjectParentIdentity, int mask ) {
    assert recipient != null;

    assert aclObjectIdentity != null;

    validPermissions = getValidPermissions();
    Arrays.sort( validPermissions );

    for ( int i = 0; i < validPermissions.length; i++ ) {
      if ( logger.isDebugEnabled() ) {
        logger.debug( "Valid permission:   " + printPermissionsBlock( validPermissions[i] ) + " "
          + printBinary( validPermissions[i] ) + " (" + validPermissions[i] + ")" );
      }
    }

    this.recipient = recipient;
    this.aclObjectIdentity = aclObjectIdentity;
    this.aclObjectParentIdentity = aclObjectParentIdentity;
    this.mask = mask;
  }

  /**
   * A protected constructor for use by Hibernate.
   */
  protected PentahoAbstractBasicAclEntry() {
    validPermissions = getValidPermissions();
    Arrays.sort( validPermissions );
  }

  //~ Methods ========================================================================================================

  public int addPermission( int permissionToAdd ) {
    return addPermissions( new int[] { permissionToAdd } );
  }

  public int addPermissions( int[] permissionsToAdd ) {
    if ( logger.isDebugEnabled() ) {
      logger.debug( "BEFORE Permissions: " + printPermissionsBlock( mask ) + " " + printBinary( mask ) + " (" + mask
        + ")" );
    }

    for ( int i = 0; i < permissionsToAdd.length; i++ ) {
      if ( logger.isDebugEnabled() ) {
        logger.debug( "Add permission: " + printPermissionsBlock( permissionsToAdd[i] ) + " "
          + printBinary( permissionsToAdd[i] ) + " (" + permissionsToAdd[i] + ")" );
      }

      this.mask |= permissionsToAdd[i];
    }

    if ( Arrays.binarySearch( validPermissions, this.mask ) < 0 ) {
      throw new IllegalArgumentException( "Resulting permission set will be invalid." );
    } else {
      if ( logger.isDebugEnabled() ) {
        logger.debug( "AFTER  Permissions: " + printPermissionsBlock( mask ) + " " + printBinary( mask ) + " ("
          + mask + ")" );
      }

      return this.mask;
    }
  }

  public int deletePermission( int permissionToDelete ) {
    return deletePermissions( new int[] { permissionToDelete } );
  }

  public int deletePermissions( int[] permissionsToDelete ) {
    if ( logger.isDebugEnabled() ) {
      logger.debug( "BEFORE Permissions: " + printPermissionsBlock( mask ) + " " + printBinary( mask ) + " (" + mask
        + ")" );
    }

    for ( int i = 0; i < permissionsToDelete.length; i++ ) {
      if ( logger.isDebugEnabled() ) {
        logger.debug( "Delete  permission: " + printPermissionsBlock( permissionsToDelete[i] ) + " "
          + printBinary( permissionsToDelete[i] ) + " (" + permissionsToDelete[i] + ")" );
      }

      this.mask &= ~permissionsToDelete[i];
    }

    if ( Arrays.binarySearch( validPermissions, this.mask ) < 0 ) {
      throw new IllegalArgumentException( "Resulting permission set will be invalid." );
    } else {
      if ( logger.isDebugEnabled() ) {
        logger.debug( "AFTER  Permissions: " + printPermissionsBlock( mask ) + " " + printBinary( mask ) + " ("
          + mask + ")" );
      }

      return this.mask;
    }
  }

  public IPentahoAclObjectIdentity getAclObjectIdentity() {
    return this.aclObjectIdentity;
  }

  public IPentahoAclObjectIdentity getAclObjectParentIdentity() {
    return this.aclObjectParentIdentity;
  }

  public int getMask() {
    return this.mask;
  }

  public Object getRecipient() {
    return this.recipient;
  }

  /**
   * Subclasses must indicate the permissions they support. Each base permission should be an integer with a
   * base 2. ie: the first permission is 2^^0 (1), the second permission is 2^^1 (2), the third permission is 2^^2
   * (4) etc. Each base permission should be exposed by the subclass as a <code>public static final int</code>. It
   * is further recommended that valid combinations of permissions are also exposed as <code>public static final
   * int</code>s.<P>This method returns all permission integers that are allowed to be used together. <B>This
   * must include any combinations of valid permissions</b>. So if the permissions indicated by 2^^2 (4) and 2^^1
   * (2) can be used together, one of the integers returned by this method must be 6 (4 + 2). Otherwise attempts to
   * set the permission will be rejected, as the final resulting mask will be rejected.</p>
   *  <P>Whilst it may seem unduly time onerous to return every valid permission <B>combination</B>, doing so
   * delivers maximum flexibility in ensuring ACLs only reflect logical combinations. For example, it would be
   * inappropriate to grant a "read" and "write" permission along with an "unrestricted" permission, as the latter
   * implies the former permissions.</p>
   *
   * @return <b>every</b> valid combination of permissions
   */
  public abstract int[] getValidPermissions();

  public boolean isPermitted( int permissionToCheck ) {
    return isPermitted( this.mask, permissionToCheck );
  }

  protected boolean isPermitted( int maskToCheck, int permissionToCheck ) {
    return ( ( maskToCheck & permissionToCheck ) == permissionToCheck );
  }

  private String printBinary( int i ) {
    String s = Integer.toString( i, 2 );

    String pattern = "................................";

    String temp1 = pattern.substring( 0, pattern.length() - s.length() );

    String temp2 = temp1 + s;

    return temp2.replace( '0', '.' );
  }

  /**
   * Outputs the permissions in a human-friendly format. For example, this method may return "CR-D" to
   * indicate the passed integer permits create, permits read, does not permit update, and permits delete.
   *
   * @param i the integer containing the mask which should be printed
   *
   * @return the human-friend formatted block
   */
  public abstract String printPermissionsBlock( int i );

  /**
   * Outputs the permissions in human-friendly format for the current <code>AbstractBasicAclEntry</code>'s
   * mask.
   *
   * @return the human-friendly formatted block for this instance
   */
  public String printPermissionsBlock() {
    return printPermissionsBlock( this.mask );
  }

  public void setAclObjectIdentity( IPentahoAclObjectIdentity aclObjectIdentity ) {
    this.aclObjectIdentity = aclObjectIdentity;
  }

  public void setAclObjectParentIdentity( IPentahoAclObjectIdentity aclObjectParentIdentity ) {
    this.aclObjectParentIdentity = aclObjectParentIdentity;
  }

  public void setMask( int mask ) {
    this.mask = mask;
  }

  public void setRecipient( Object recipient ) {
    this.recipient = recipient;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append( getClass().getName() );
    sb.append( "[" ).append( aclObjectIdentity ).append( "," ).append( recipient );
    sb.append( "=" ).append( printPermissionsBlock( mask ) ).append( " " );
    sb.append( printBinary( mask ) ).append( " (" );
    sb.append( mask ).append( ")" ).append( "]" );

    return sb.toString();
  }

  public int togglePermission( int permissionToToggle ) {
    this.mask ^= permissionToToggle;

    if ( Arrays.binarySearch( validPermissions, this.mask ) < 0 ) {
      throw new IllegalArgumentException( "Resulting permission set will be invalid." );
    } else {
      return this.mask;
    }
  }
}
