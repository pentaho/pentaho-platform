package org.pentaho.platform.api.mt;

/**
 * Utility to go between principal ids (user/role ids) and principal name and tenant.
 * For example let's assume that user "joe" belongs to tenant "acme". This resolver class
 * class is responsible for determining the user's unique id within a multi-tenanted environment where
 * multiple users with the same name may exist across tenants. Conversely this class must be
 * able to convert a unique user id to a user name & tenant.
 * 
 * @author rmansoor
 *
 */
public interface ITenantedPrincipleNameResolver {

  /**
   * Extract the tenant from the principleId
   * 
   * @param principleId
   * @return tenant
   */
  public ITenant getTenant(String principleId);
  
  /**
   * Extract the principle name from the principleId
   * 
   * @param principleId
   * @return principle name
   */
  public String getPrincipleName(String principleId);
  
  /**
   * Construct a principle Id from tenant and principle name
   * 
   * @param tenant
   * @param principalName
   * @return principle id
   */
  public String getPrincipleId(ITenant tenant, String principalName);
}
