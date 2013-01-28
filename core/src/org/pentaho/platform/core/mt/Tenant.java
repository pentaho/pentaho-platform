package org.pentaho.platform.core.mt;

import org.pentaho.platform.api.mt.ITenant;

public class Tenant implements ITenant {
  private static final long serialVersionUID = 1L;
  private String rootFolderAbsolutePath;
  public String getRootFolderAbsolutePath() {
    return rootFolderAbsolutePath;
  }

  public void setRootFolderAbsolutePath(String rootFolderAbsolutePath) {
    this.rootFolderAbsolutePath = rootFolderAbsolutePath;
  }

  private boolean enabled = true;

  public Tenant() {
    
  }
  
  public Tenant(String tenantId, Boolean enabled) {
    rootFolderAbsolutePath = tenantId;
    this.enabled = enabled;
  }
  
  @Override
  public String getId() {
    return rootFolderAbsolutePath;
  }

  @Override
  public String getName() {
    return rootFolderAbsolutePath.substring(rootFolderAbsolutePath.lastIndexOf("/") + 1);
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
    return (this.getId() != null) && this.getId().equals(tenant.getId());
  }

  @Override
  public int hashCode() {
    return 31 * rootFolderAbsolutePath.hashCode();
  }

  public String toString() {
    return "TENANT ID = " + rootFolderAbsolutePath; //$NON-NLS-1$//$NON-NLS-3$
  }

  public boolean isEnabled() {
    return enabled;
  }
  
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }
  
}
