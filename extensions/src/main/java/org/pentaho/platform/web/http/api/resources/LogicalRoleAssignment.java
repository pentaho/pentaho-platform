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


package org.pentaho.platform.web.http.api.resources;

import java.util.ArrayList;
import java.util.List;

public class LogicalRoleAssignment {

  String roleName;
  ArrayList<String> logicalRoles = new ArrayList<String>();
  boolean immutable;

  public LogicalRoleAssignment() {

  }

  public LogicalRoleAssignment( String roleName, List<String> logicalRoleAssignments, boolean immutable ) {
    this.roleName = roleName;
    this.logicalRoles.addAll( logicalRoleAssignments );
    this.immutable = immutable;
  }

  public String getRoleName() {
    return roleName;
  }

  public void setRoleName( String roleName ) {
    this.roleName = roleName;
  }

  public ArrayList<String> getLogicalRoles() {
    return logicalRoles;
  }

  public void setLogicalRoles( ArrayList<String> logicalRoles ) {
    if ( logicalRoles != this.logicalRoles ) {
      this.logicalRoles.clear();
      if ( logicalRoles != null ) {
        this.logicalRoles.addAll( logicalRoles );
      }
    }
  }

  public boolean isImmutable() {
    return immutable;
  }

  public void setImmutable( boolean immutable ) {
    this.immutable = immutable;
  }
}
