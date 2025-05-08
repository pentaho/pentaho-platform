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


package org.pentaho.platform.security.userroledao.ws;

import java.io.Serializable;

public class UserToRoleAssignment implements Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = 5514888410858029649L;
  String userId;
  String roleId;

  public UserToRoleAssignment() {
  }

  public UserToRoleAssignment( String userId, String roleId ) {
    this.userId = userId;
    this.roleId = roleId;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId( String userId ) {
    this.userId = userId;
  }

  public String getRoleId() {
    return roleId;
  }

  public void setRoleId( String roleId ) {
    this.roleId = roleId;
  }

  public boolean equals( Object o ) {
    return ( o instanceof UserToRoleAssignment ) && ( userId != null )
        && userId.equals( ( (UserToRoleAssignment) o ).getUserId() ) && ( roleId != null )
        && roleId.equals( ( (UserToRoleAssignment) o ).getRoleId() );
  }
}
