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
import java.util.ArrayList;
import java.util.List;

public class UserRoleSecurityInfo implements Serializable {

  private static final long serialVersionUID = 420L;

  List<ProxyPentahoUser> users = new ArrayList<ProxyPentahoUser>();

  List<ProxyPentahoRole> roles = new ArrayList<ProxyPentahoRole>();

  List<UserToRoleAssignment> assignments = new ArrayList<UserToRoleAssignment>();

  List<ProxyPentahoRole> defaultRoles = new ArrayList<ProxyPentahoRole>();

  public UserRoleSecurityInfo() {
  }

  public List<ProxyPentahoUser> getUsers() {
    return users;
  }

  public void setUsers( List<ProxyPentahoUser> users ) {
    this.users = users;
  }

  public List<ProxyPentahoRole> getRoles() {
    return roles;
  }

  public void setRoles( List<ProxyPentahoRole> roles ) {
    this.roles = roles;
  }

  public List<UserToRoleAssignment> getAssignments() {
    return assignments;
  }

  public void setAssignments( List<UserToRoleAssignment> assignments ) {
    this.assignments = assignments;
  }

  public List<ProxyPentahoRole> getDefaultRoles() {
    return defaultRoles;
  }

  public void setDefaultRoles( List<ProxyPentahoRole> defaultRoles ) {
    this.defaultRoles = defaultRoles;
  }
}
