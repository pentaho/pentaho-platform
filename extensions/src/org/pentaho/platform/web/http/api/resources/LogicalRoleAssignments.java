package org.pentaho.platform.web.http.api.resources;

import java.util.ArrayList;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class LogicalRoleAssignments {
  ArrayList<LogicalRoleAssignment> assignments = new ArrayList<LogicalRoleAssignment>();

  public ArrayList<LogicalRoleAssignment> getAssignments() {
    return assignments;
  }

  public void setAssignments(ArrayList<LogicalRoleAssignment> roleAssignments) {
    if (roleAssignments != this.assignments) {
      this.assignments.clear();
      if (roleAssignments != null) {
        this.assignments.addAll(roleAssignments);
      }
    }
  }
}
