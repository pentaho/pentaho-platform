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
