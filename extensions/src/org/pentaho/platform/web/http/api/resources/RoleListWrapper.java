package org.pentaho.platform.web.http.api.resources;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

import org.pentaho.platform.api.engine.security.userroledao.IPentahoRole;

@XmlRootElement(name="roleList")
public class RoleListWrapper {
  List<String> roles = new ArrayList<String>();

  public RoleListWrapper() {
  }
  
  public RoleListWrapper(List<IPentahoRole> roles) {
    List<String> roleList = new ArrayList<String>();
    for(IPentahoRole role:roles) {
      roleList.add(role.getName());
    }
    this.roles.addAll(roleList);
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
