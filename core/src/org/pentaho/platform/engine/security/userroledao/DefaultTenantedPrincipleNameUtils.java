package org.pentaho.platform.engine.security.userroledao;

public class DefaultTenantedPrincipleNameUtils implements ITenantedPrincipleNameUtils {

  public static final String DEFAULT_DELIMETER = "|";
  
  public boolean userNameNaturallyContainsEmbeddedTenantName = false;
  private String delimeter = DEFAULT_DELIMETER;
  private boolean principalNameFollowsTenantName = false;
  
  public String getTenantName(String principalId) {
    String tenantName = null;
    int delimiterIndex = principalId.indexOf(getDelimeter());
    if ( delimiterIndex >= 0) {
      tenantName = (getUserNameFollowsTenantName() ? principalId.substring(0, delimiterIndex - 1) : principalId.substring(delimiterIndex + 1));
    } else if (principalNameFollowsTenantName) {
      tenantName = principalId;
    }
    // TODO Auto-generated method stub
    return tenantName;
  }

  public String getPrincipleName(String principalId) {
    String userName = null;
    int delimiterIndex = principalId.indexOf(getDelimeter());
    if ( delimiterIndex >= 0) {
      if (getUserNameNaturallyContainsEmbeddedTenantName()) {
        userName = principalId;
      } else {
        userName = (getUserNameFollowsTenantName() ?  principalId.substring(delimiterIndex + 1) : principalId.substring(0, delimiterIndex));
      }
    } else if (!principalNameFollowsTenantName) {
      userName = principalId;
    }
    return userName;
  }
  

  public String getPrincipleId(String tenantName, String principleName) {
    String id = getDelimeter();
    if (tenantName == null) {
      id = principalNameFollowsTenantName ? getDelimeter() + principleName : principleName + getDelimeter();
    } else {
      id = principalNameFollowsTenantName ? tenantName + getDelimeter() + principleName : principleName + getDelimeter() + tenantName;
    }
    // TODO Auto-generated method stub
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
