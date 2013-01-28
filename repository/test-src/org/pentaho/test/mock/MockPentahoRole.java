package org.pentaho.test.mock;

import org.pentaho.platform.api.engine.security.userroledao.IPentahoRole;
import org.pentaho.platform.api.mt.ITenant;

public class MockPentahoRole implements IPentahoRole {

  String name;
  String description ;
  ITenant tenant;
  
  
  public MockPentahoRole () {
    
  }
  
  public MockPentahoRole(ITenant tenant, String name, String description) {
    this.tenant = tenant;
    this.name = name;
    this.description = description;
  }
  
  public String getDescription() {
    return description;
  }

  public String getName() {
    return name;
  }

  public ITenant getTenant() {
    return tenant;
  }

  public void setDescription(String arg0) {
    this.description = arg0;

  }

  public void setName(String name) {
    this.name = name;
  }

  public void setTenant(ITenant tenant) {
    this.tenant = tenant;
  }

  public int hashCode() {
    if ((tenant != null) && (tenant.getId() != null)) {
      return tenant.getId().concat(name).hashCode();
    }
    return name.hashCode();
  }
  
  
}
