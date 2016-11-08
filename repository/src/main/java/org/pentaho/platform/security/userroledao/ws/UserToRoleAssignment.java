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
