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


package org.pentaho.platform.api.engine.security.userroledao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UserRoleInfo implements Serializable {

  private static final long serialVersionUID = 420L;

  List<String> users = new ArrayList<String>();

  List<String> roles = new ArrayList<String>();

  public UserRoleInfo() {
  }

  public List<String> getUsers() {
    Collections.sort( users );
    return users;
  }

  public void setUsers( List<String> users ) {
    this.users = users;
  }

  public List<String> getRoles() {
    Collections.sort( roles );
    return roles;
  }

  public void setRoles( List<String> roles ) {
    this.roles = roles;
  }
}
