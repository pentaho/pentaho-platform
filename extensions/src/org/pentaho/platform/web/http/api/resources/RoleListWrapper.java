package org.pentaho.platform.web.http.api.resources;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="roleList")
public class RoleListWrapper {
  List<String> roles = new ArrayList<String>();

  public RoleListWrapper() {
  }
  
  public RoleListWrapper(Collection<String> roles) {
    this.roles.addAll(roles);
  }
  
  public List<String> getRoles() {
    return roles;
  }

  public void setRoles(List<String> roles) {
    if (roles != this.roles) {
      this.roles.clear();
      this.roles.addAll(roles);
    }
  }
}
