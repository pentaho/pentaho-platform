package org.pentaho.platform.web.http.api.resources;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.pentaho.platform.api.engine.security.userroledao.IPentahoUser;

@XmlRootElement(name="userList")
public class UserListWrapper {
  List<String> users = new ArrayList<String>();

  public UserListWrapper() {
  }
  
  public UserListWrapper(List<IPentahoUser> users) {
    List<String> userList = new ArrayList<String>();
    for(IPentahoUser user:users) {
      userList.add(user.getUsername());
    }
    this.users.addAll(userList);
  }
  
  public UserListWrapper(Collection<String> users) {
    this.users.addAll(users);
  }
  
  public List<String> getUsers() {
    return users;
  }

  public void setUsers(List<String> users) {
    if (users != this.users) {
      this.users.clear();
      this.users.addAll(users);
    }
  }
}
