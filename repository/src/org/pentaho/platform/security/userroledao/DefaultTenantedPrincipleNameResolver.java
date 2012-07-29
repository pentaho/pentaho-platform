package org.pentaho.platform.security.userroledao;

import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.pentaho.platform.core.mt.Tenant;

public class DefaultTenantedPrincipleNameResolver implements ITenantedPrincipleNameResolver {

  public static final String DEFAULT_DELIMETER = "-";
  
  public boolean userNameNaturallyContainsEmbeddedTenantName = false;
  private String delimeter = DEFAULT_DELIMETER;
  private boolean principalNameFollowsTenantName = false;
  
  public ITenant getTenant(String principalId) {
    String tenantName = null;
    int delimiterIndex = principalId.indexOf(getDelimeter());
    if ( delimiterIndex >= 0) {
      tenantName = (getUserNameFollowsTenantName() ? principalId.substring(0, delimiterIndex - 1) : principalId.substring(delimiterIndex + 1));
    }
    return new Tenant(tenantName, true);
  }

  public String getPrincipleName(String principalId) {
    String userName = principalId;
    int delimiterIndex = principalId.indexOf(getDelimeter());
    if ( delimiterIndex >= 0) {
      if (getUserNameNaturallyContainsEmbeddedTenantName()) {
        userName = principalId;
      } else {
        userName = (getUserNameFollowsTenantName() ?  principalId.substring(delimiterIndex + 1) : principalId.substring(0, delimiterIndex));
      }
    }
    return userName;
  }
  

  public String getPrincipleId(ITenant tenant, String principleName) {
    String id = getDelimeter();
    if ((tenant == null) || (tenant.getId() == null)) {
      id = principleName;
    } else {
      id = principalNameFollowsTenantName ? tenant.getId() + getDelimeter() + principleName : principleName + getDelimeter() + tenant.getId();
    }
    return id;
  }

  public boolean getUserNameNaturallyContainsEmbeddedTenantName() {
    return userNameNaturallyContainsEmbeddedTenantName;
  }

  public void setUserNameNaturallyContainsEmbeddedTenantName(boolean userNameNaturallyContainsEmbeddedTenantName) {
    this.userNameNaturallyContainsEmbeddedTenantName = userNameNaturallyContainsEmbeddedTenantName;
  }

  public String getDelimeter() {
    return delimeter;
  }

  public void setDelimeter(String delimeter) {
    this.delimeter = delimeter;
  }

  public boolean getUserNameFollowsTenantName() {
    return principalNameFollowsTenantName;
  }

  public void setUserNameFollowsTenantName(boolean userNameFollowsTenantName) {
    this.principalNameFollowsTenantName = userNameFollowsTenantName;
  }

}
