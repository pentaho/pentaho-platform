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
