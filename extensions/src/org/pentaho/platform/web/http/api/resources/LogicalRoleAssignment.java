package org.pentaho.platform.web.http.api.resources;

import java.util.ArrayList;
import java.util.List;

public class LogicalRoleAssignment {

  String roleName;
  ArrayList<String> logicalRoles = new ArrayList<String>();
  public LogicalRoleAssignment() {
    
  }
  
  public LogicalRoleAssignment(String roleName, List<String> logicalRoleAssignments) {
    this.roleName = roleName;
    this.logicalRoles.addAll(logicalRoleAssignments);
  }
  
  public String getRoleName() {
    return roleName;
  }
  
  public void setRoleName(String roleName) {
    this.roleName = roleName;
  }
  
  public ArrayList<String> getLogicalRoles() {
    return logicalRoles;
  }
  
  public void setLogicalRoles(ArrayList<String> logicalRoles) {
    if (logicalRoles != this.logicalRoles) {
      this.logicalRoles.clear();
      if (logicalRoles != null) {
        this.logicalRoles.addAll(logicalRoles);
      }
    }
  }
}
