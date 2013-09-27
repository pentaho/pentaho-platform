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

package org.pentaho.platform.engine.security;

import org.pentaho.platform.api.engine.IPermissionMask;

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
