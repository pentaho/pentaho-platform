package org.pentaho.platform.repository2.unified;

import java.io.Serializable;

import org.pentaho.platform.api.repository2.unified.ITenant;

public class Tenant implements ITenant {
  private static final long serialVersionUID = 1L;
  private Serializable id;
  private String name;
  private String path;

  @Override
  public Serializable getId() {
    return id;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getPath() {
    return path;
  }

  @Override
  public void setId(Serializable id) {
    this.id = id;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public void setPath(String path) {
    this.path = path;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    ITenant tenant = (ITenant) obj;
    if(id.equals(tenant.getId()) && path.equals(tenant.getPath()) && name.equals(tenant.getName())) {
      return true;
    }
    return false;
  }

  @Override
  public int hashCode() {
    return 31 * id.hashCode() + path.hashCode() + name.hashCode();
  }

  public String toString() {
    return "TENANT ID = " + id + " PATH =   " + path + " NAME =   " + name; //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
  }
}
