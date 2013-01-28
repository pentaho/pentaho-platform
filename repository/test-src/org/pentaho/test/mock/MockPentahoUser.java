package org.pentaho.test.mock;

import org.pentaho.platform.api.engine.security.userroledao.IPentahoUser;
import org.pentaho.platform.api.mt.ITenant;

public class MockPentahoUser implements IPentahoUser {

  String description;
  String password;
  ITenant tenant;
  String userName;
  boolean enabled;
  
  public MockPentahoUser() {    
  }
  
  public MockPentahoUser(ITenant tenant, String userName, String password, String description, boolean enabled) {
    this.tenant = tenant;
    this.userName = userName;
    this.password = password;
    this.description = description;
    this.enabled = enabled;
  }
  
  public String getDescription() {
    return description;
  }
  
  public void setDescription(String description) {
    this.description = description;
  }
  
  public String getPassword() {
    return password;
  }
  
  public void setPassword(String password) {
    this.password = password;
  }
  
  public ITenant getTenant() {
    return tenant;
  }
  
  public void setTenant(ITenant tenant) {
    this.tenant = tenant;
  }
  
  public String getUsername() {
    return userName;
  }
  
  public void setUsername(String userName) {
    this.userName = userName;
  }
  
  public boolean isEnabled() {
    return enabled;
  }
  
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }
  
  public int hashCode() {
    if ((tenant != null) && (tenant.getId() != null)) {
      return tenant.getId().concat(userName).hashCode();
    }
    return userName.hashCode();
  }
}
