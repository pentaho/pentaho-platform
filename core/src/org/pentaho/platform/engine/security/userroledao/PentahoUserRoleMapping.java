package org.pentaho.platform.engine.security.userroledao;

import java.io.Serializable;

public class PentahoUserRoleMapping {

  private Id id;

  public Id getId() {
    return id;
  }

  public void setId(Id id) {
    this.id = id;
  }

  public static class Id implements Serializable {
    private static final long serialVersionUID = -2387185346376315677L;

    private String user;

    private String role;

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((role == null) ? 0 : role.hashCode());
      result = prime * result + ((user == null) ? 0 : user.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      Id other = (Id) obj;
      if (role == null) {
        if (other.role != null)
          return false;
      } else if (!role.equals(other.role))
        return false;
      if (user == null) {
        if (other.user != null)
          return false;
      } else if (!user.equals(other.user))
        return false;
      return true;
    }

    public String getUser() {
      return user;
    }

    public String getRole() {
      return role;
    }

    public void setUser(String user) {
      this.user = user;
    }

    public void setRole(String role) {
      this.role = role;
    }

  }

}
