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
