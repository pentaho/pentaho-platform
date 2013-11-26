/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

/**
 * 
 */

package org.pentaho.platform.config;

/**
 * @author RMansoor
 * 
 */
public class AclEntry implements java.io.Serializable {

  private static final long serialVersionUID = 1L;

  private String principalName = ""; //$NON-NLS-1$

  private String permission = ""; //$NON-NLS-1$

  public AclEntry() {
  }

  public AclEntry( String principalName, String permission ) {
    this.principalName = principalName;
    this.permission = permission;
  }

  public String getPrincipalName() {
    return principalName;
  }

  public String getPermission() {
    return this.permission;
  }

  public void setPrincipalName( String principalName ) {
    this.principalName = principalName;
  }

  public void setPermission( String permission ) {
    this.permission = permission;
  }

  public boolean equals( Object o ) {

    if ( o instanceof AclEntry ) {
      AclEntry a = (AclEntry) o;
      if ( ( principalName.equals( a.principalName ) ) && ( permission.equals( a.permission ) ) ) {
        return true;
      }
    }
    return false;
  }

  public int hashCode() {
    return 31 * principalName.hashCode() + permission.hashCode();
  }

  public String toString() {
    return "SERVICE NAME = " + principalName + " ATTRIBUTE NAME =   " + permission; //$NON-NLS-1$//$NON-NLS-2$
  }
}
