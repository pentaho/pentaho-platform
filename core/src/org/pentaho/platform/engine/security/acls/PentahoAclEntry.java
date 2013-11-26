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

package org.pentaho.platform.engine.security.acls;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoAclEntry;
import org.pentaho.platform.engine.security.messages.Messages;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.acl.basic.AbstractBasicAclEntry;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Base Pentaho Access Control entry. Subclassed <tt>AbstractBasicAclEntry</tt> from Spring Security project.
 * Provides known access controls.
 * 
 * @author mbatchel
 */

@SuppressWarnings( "deprecation" )
public class PentahoAclEntry extends AbstractBasicAclEntry implements IPentahoAclEntry {

  private static final Log logger = LogFactory.getLog( PentahoAclEntry.class );

  /**
   * Populated lazily in getValidPermissions().
   */
  private static int[] validPermissions;

  private static final long serialVersionUID = -1123574274303339402L;

  private static final int RECIPIENT_STRING = 0;

  private static final int RECIPIENT_GRANTEDAUTHORITY = 1;

  private static final Map validPermissionsNameMap = new HashMap();
  public int recipientType = PentahoAclEntry.RECIPIENT_STRING;

  // Prevent breakage downstream by inadvertant or malicious modifications to validPermissions array
  private int[] lazyPermissionClone;

  static {
    Map solutionPermissionsMap = new HashMap();
    Map allPermissionsMap = new HashMap();

    PentahoAclEntry.validPermissionsNameMap.put( PentahoAclEntry.PERMISSIONS_LIST_SOLUTIONS, Collections
        .unmodifiableMap( solutionPermissionsMap ) );
    PentahoAclEntry.validPermissionsNameMap.put( PentahoAclEntry.PERMISSIONS_LIST_ALL, Collections
        .unmodifiableMap( allPermissionsMap ) );

    // TODO gmoran Are two lists really necessary any more?
    // TODO mlowery Why does PentahoAclEntry know about what permissions are valid for solutions?
    solutionPermissionsMap
        .put(
            Messages.getInstance().getString( "PentahoAclEntry.USER_ADMINISTER" ), new Integer( PentahoAclEntry.PERM_FULL_CONTROL ) ); //$NON-NLS-1$
    solutionPermissionsMap
        .put(
            Messages.getInstance().getString( "PentahoAclEntry.USER_MANAGE_PERMS" ), new Integer( PentahoAclEntry.PERM_UPDATE_PERMS ) ); //$NON-NLS-1$
    solutionPermissionsMap.put(
        Messages.getInstance().getString( "PentahoAclEntry.USER_UPDATE" ), new Integer( PentahoAclEntry.PERM_UPDATE ) ); //$NON-NLS-1$
    solutionPermissionsMap.put(
        Messages.getInstance().getString( "PentahoAclEntry.USER_CREATE" ), new Integer( PentahoAclEntry.PERM_CREATE ) ); //$NON-NLS-1$
    solutionPermissionsMap.put(
        Messages.getInstance().getString( "PentahoAclEntry.USER_DELETE" ), new Integer( PentahoAclEntry.PERM_DELETE ) ); //$NON-NLS-1$
    solutionPermissionsMap
        .put(
            Messages.getInstance().getString( "PentahoAclEntry.USER_EXECUTE" ), new Integer( PentahoAclEntry.PERM_EXECUTE ) ); //$NON-NLS-1$
    solutionPermissionsMap
        .put(
            Messages.getInstance().getString( "PentahoAclEntry.USER_SUBSCRIBE" ), new Integer( PentahoAclEntry.PERM_SUBSCRIBE ) ); //$NON-NLS-1$

    allPermissionsMap.put( Messages.getInstance().getString( "PentahoAclEntry.USER_NONE" ), new Integer( 0 ) ); //$NON-NLS-1$
    allPermissionsMap
        .put(
            Messages.getInstance().getString( "PentahoAclEntry.USER_EXECUTE" ), new Integer( PentahoAclEntry.PERM_EXECUTE ) ); //$NON-NLS-1$
    allPermissionsMap
        .put(
            Messages.getInstance().getString( "PentahoAclEntry.USER_SUBSCRIBE" ), new Integer( PentahoAclEntry.PERM_SUBSCRIBE ) ); //$NON-NLS-1$
    allPermissionsMap.put(
        Messages.getInstance().getString( "PentahoAclEntry.USER_CREATE" ), new Integer( PentahoAclEntry.PERM_CREATE ) ); //$NON-NLS-1$
    allPermissionsMap.put(
        Messages.getInstance().getString( "PentahoAclEntry.USER_UPDATE" ), new Integer( PentahoAclEntry.PERM_UPDATE ) ); //$NON-NLS-1$
    allPermissionsMap.put(
        Messages.getInstance().getString( "PentahoAclEntry.USER_DELETE" ), new Integer( PentahoAclEntry.PERM_DELETE ) ); //$NON-NLS-1$
    allPermissionsMap
        .put(
            Messages.getInstance().getString( "PentahoAclEntry.USER_ALL" ), new Integer( PentahoAclEntry.PERM_FULL_CONTROL ) ); //$NON-NLS-1$

    initializePermissionsArray();
  }

  private static void initializePermissionsArray() {
    if ( null == PentahoAclEntry.validPermissions ) {
      int maxPower = -1;
      Field[] fields = IPentahoAclEntry.class.getDeclaredFields();
      for ( Field field : fields ) {
        // if field is public static final int
        if ( int.class == field.getType() && Modifier.isPublic( field.getModifiers() )
            && Modifier.isStatic( field.getModifiers() ) && Modifier.isFinal( field.getModifiers() )
            && field.getName().startsWith( PERMISSION_PREFIX ) ) {
          if ( PentahoAclEntry.logger.isDebugEnabled() ) {
            PentahoAclEntry.logger.debug( "Candidate field: " + field.getName() ); //$NON-NLS-1$
          }

          // power of two (0-based)
          double powerOfTwo = -1;
          try {
            powerOfTwo = Math.log( field.getInt( null ) ) / Math.log( 2 );
          } catch ( IllegalArgumentException e ) {
            //ignore
          } catch ( IllegalAccessException e ) {
            //ignore
          }
          // if log calculation results in an integer
          if ( powerOfTwo == (int) powerOfTwo ) {
            if ( powerOfTwo > maxPower ) {
              if ( PentahoAclEntry.logger.isDebugEnabled() ) {
                PentahoAclEntry.logger.debug( "Found new power of two." ); //$NON-NLS-1$
              }
              maxPower = (int) powerOfTwo;
            }
          }
        }
      }
      if ( PentahoAclEntry.logger.isDebugEnabled() ) {
        PentahoAclEntry.logger.debug( "Max power of two: " + maxPower ); //$NON-NLS-1$
      }

      int numberOfPermutations = (int) Math.pow( 2, maxPower + 1 );

      PentahoAclEntry.validPermissions = new int[numberOfPermutations + 1];
      for ( int i = 0; i < numberOfPermutations; i++ ) {
        PentahoAclEntry.validPermissions[i] = i;
      }
      PentahoAclEntry.validPermissions[PentahoAclEntry.validPermissions.length - 1] = PentahoAclEntry.PERM_FULL_CONTROL;
    }
  }

  public PentahoAclEntry() {
    super();
  }

  public PentahoAclEntry( final Object recipient, final int mask ) {
    this();
    setRecipient( recipient );
    setMask( mask );
  }

  protected void setRecipientType( final int value ) {
    this.recipientType = value;
  }

  protected int getRecipientType() {
    return this.recipientType;
  }

  protected void setRecipientString( final String value ) {
    if ( this.recipientType == PentahoAclEntry.RECIPIENT_GRANTEDAUTHORITY ) {
      this.setRecipient( new GrantedAuthorityImpl( value ) );
    } else {
      this.setRecipient( value );
    }

  }

  protected String getRecipientString() {
    return this.getRecipient().toString();
  }

  @Override
  public void setRecipient( final Object value ) {
    super.setRecipient( value );
    if ( value instanceof GrantedAuthority ) {
      this.setRecipientType( PentahoAclEntry.RECIPIENT_GRANTEDAUTHORITY );
    } else {
      this.setRecipientType( PentahoAclEntry.RECIPIENT_STRING );
    }
  }

  /**
   * As implemented, this method says that all permission combinations are valid. (Well not all. FULL_CONTROL must
   * stand alone. It cannot be combined with other bits.)
   * 
   * <ol>
   * <li>Find the permission value (call it p) that is the highest power of two.</li>
   * <li>Find n (0-based) such that 2^n = p. (Uses logarithm with base 2.)</li>
   * <li>So there are 2^(n+1) permutations of permission bits.</li>
   * <li>So the valid permission values list consists of those 2^(n+1) permutations plus the FULL_CONTROL perm bit.
   * (i.e. (2^(n+1))+1</li>
   * </ol>
   */
  @Override
  public int[] getValidPermissions() {
    if ( lazyPermissionClone == null ) {
      lazyPermissionClone = new int[validPermissions.length];
      System.arraycopy( validPermissions, 0, lazyPermissionClone, 0, validPermissions.length );
    }
    return this.lazyPermissionClone;
  }

  public static void main( final String[] args ) {
    PentahoAclEntry e = new PentahoAclEntry();
    System.out.println( Arrays.toString( e.getValidPermissions() ) );
    System.out.println( Arrays.toString( e.getValidPermissions() ) );
  }

  @Override
  public String printPermissionsBlock( final int i ) {
    StringBuffer sb = new StringBuffer();

    if ( isPermitted( i, PentahoAclEntry.PERM_EXECUTE ) ) {
      sb.append( 'X' );
    } else {
      sb.append( '-' );
    }

    if ( isPermitted( i, PentahoAclEntry.PERM_SUBSCRIBE ) ) {
      sb.append( 'S' );
    } else {
      sb.append( '-' );
    }

    if ( isPermitted( i, PentahoAclEntry.PERM_CREATE ) ) {
      sb.append( 'C' );
    } else {
      sb.append( '-' );
    }

    if ( isPermitted( i, PentahoAclEntry.PERM_UPDATE ) ) {
      sb.append( 'U' );
    } else {
      sb.append( '-' );
    }

    if ( isPermitted( i, PentahoAclEntry.PERM_DELETE ) ) {
      sb.append( 'D' );
    } else {
      sb.append( '-' );
    }

    if ( isPermitted( i, PentahoAclEntry.PERM_UPDATE_PERMS ) ) {
      sb.append( 'P' );
    } else {
      sb.append( '-' );
    }

    return sb.toString();
  }

  /**
   * @return Returns the validPermissionsNameMap. This method is generally useful for UI work as it returns a Map
   *         of Permission atomic values (as Integer objects) keyed by a human readable permission name.
   */
  public static Map getValidPermissionsNameMap() {
    return PentahoAclEntry.getValidPermissionsNameMap( PentahoAclEntry.PERMISSIONS_LIST_SOLUTIONS );
  }

  /**
   * @return Returns the validPermissionsNameMap. This method is generally useful for UI work as it returns a Map
   *         of Permission atomic values (as Integer objects) keyed by a human readable permission name.
   * @param permissionsListType
   *          - The permissions list for solutions is different than that for other UIs
   */
  public static Map getValidPermissionsNameMap( final String permissionsListType ) {
    return (Map) PentahoAclEntry.validPermissionsNameMap.get( permissionsListType );
  }

  public boolean equals( final Object obj ) {
    if ( obj instanceof PentahoAclEntry == false ) {
      return false;
    }
    if ( this == obj ) {
      return true;
    }
    //
    // MB - If the instanceof above compares to anything other than
    // this object, then the static comparison below of getValidPermissions() should
    // be re-evaluated.
    //
    PentahoAclEntry rhs = (PentahoAclEntry) obj;
    return new EqualsBuilder().append( getRecipient(), rhs.getRecipient() ).append( getRecipientType(),
        rhs.getRecipientType() ).append( getAclObjectIdentity(), rhs.getAclObjectIdentity() ).append(
        getAclObjectParentIdentity(), rhs.getAclObjectParentIdentity() )
    // .append(getValidPermissions(),rhs.getValidPermissions())
        .append( getMask(), rhs.getMask() ).isEquals();
  }

  public int hashCode() {
    return new HashCodeBuilder( 79, 211 ).append( getRecipient() ).append( getRecipientType() ).append(
        getAclObjectIdentity() ).append( getAclObjectParentIdentity() )
    // MB - Commented out because it's not relevant
    // .append(getValidPermissions())
        .append( getMask() ).toHashCode();
  }

}
