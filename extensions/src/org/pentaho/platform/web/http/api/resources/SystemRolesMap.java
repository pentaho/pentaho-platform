package org.pentaho.platform.web.http.api.resources;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class SystemRolesMap extends LogicalRoleAssignments{

  ArrayList<LocalizedLogicalRoleName> localizedRoleNames = new ArrayList<LocalizedLogicalRoleName>();

  public ArrayList<LocalizedLogicalRoleName> getLocalizedRoleNames() {
    return localizedRoleNames;
  }

  public void setLocalizedRoleNames(ArrayList<LocalizedLogicalRoleName> localizedRoleNames) {
    if (localizedRoleNames != this.localizedRoleNames) {
      this.localizedRoleNames.clear();
      if (localizedRoleNames != null) {
        this.localizedRoleNames.addAll(localizedRoleNames);
      }
    }
  }

}
