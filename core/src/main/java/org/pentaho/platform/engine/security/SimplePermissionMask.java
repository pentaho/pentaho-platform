/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.platform.engine.security;

import org.pentaho.platform.api.engine.IPermissionMask;

@Deprecated
public class SimplePermissionMask implements IPermissionMask {
  int permissionMask;

  public SimplePermissionMask() {
  }

  public SimplePermissionMask( final int permissionMask ) {
    this.permissionMask = permissionMask;
  }

  public void setPermissionMask( final int permissionMask ) {
    this.permissionMask = permissionMask;
  }

  public int getMask() {
    return permissionMask;
  }

  public void addPermission( final int permissionMask ) {
    this.permissionMask |= permissionMask;
  }

  public void addPermissions( final int[] permissionMasks ) {
    for ( int element : permissionMasks ) {
      this.permissionMask |= element;
    }
  }

  public void deletePermission( int permissionMask ) {
    this.permissionMask &= ~permissionMask;
  }

  public void deletePermissions( final int[] permissionMasks ) {
    for ( int i = 0; i < permissionMasks.length; i++ ) {
      this.permissionMask &= ~permissionMasks[i];
    }
  }

  @Override
  public boolean equals( final Object obj ) {
    return ( obj instanceof SimplePermissionMask )
      && ( permissionMask == ( (SimplePermissionMask) obj ).permissionMask );
  }

  @Override
  public int hashCode() {
    return permissionMask;
  }

  @Override
  public String toString() {
    return String.format( "SimplePermissionMask[permissionMask=%d]", permissionMask ); //$NON-NLS-1$
  }
}
