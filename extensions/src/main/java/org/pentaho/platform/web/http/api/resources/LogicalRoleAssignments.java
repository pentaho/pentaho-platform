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

import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlSeeAlso;
import java.util.ArrayList;

@XmlRootElement
@XmlSeeAlso( SystemRolesMap.class )
public class LogicalRoleAssignments {
  ArrayList<LogicalRoleAssignment> assignments = new ArrayList<LogicalRoleAssignment>();

  public ArrayList<LogicalRoleAssignment> getAssignments() {
    return assignments;
  }

  public void setAssignments( ArrayList<LogicalRoleAssignment> roleAssignments ) {
    if ( roleAssignments != this.assignments ) {
      this.assignments.clear();
      if ( roleAssignments != null ) {
        this.assignments.addAll( roleAssignments );
      }
    }
  }
}
