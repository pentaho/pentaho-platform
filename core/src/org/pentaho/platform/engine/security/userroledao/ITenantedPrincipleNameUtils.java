package org.pentaho.platform.engine.security.userroledao;

public interface ITenantedPrincipleNameUtils {
  public String getTenantName(String principleId);
  public String getPrincipleName(String principleId);
  public String getPrincipleId(String tenantName, String principalName);
}
