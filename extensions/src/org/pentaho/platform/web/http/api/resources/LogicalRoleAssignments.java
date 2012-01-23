package org.pentaho.platform.web.http.api.resources;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class LogicalRoleAssignments {
  ArrayList<LogicalRoleAssignment> logicalRoleAssignments = new ArrayList<LogicalRoleAssignment>();

  public ArrayList<LogicalRoleAssignment> getLogicalRoleAssignments() {
    return logicalRoleAssignments;
  }

  public void setLogicalRoleAssignments(ArrayList<LogicalRoleAssignment> roleAssignments) {
    if (roleAssignments != this.logicalRoleAssignments) {
      this.logicalRoleAssignments.clear();
      if (roleAssignments != null) {
        this.logicalRoleAssignments.addAll(roleAssignments);
      }
    }
  }
}
