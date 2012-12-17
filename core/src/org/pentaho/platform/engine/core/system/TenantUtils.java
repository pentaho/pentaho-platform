package org.pentaho.platform.engine.core.system;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.core.mt.Tenant;

/**
 * Utilities relating to multi-tenancy.
 * 
 * @author mlowery
 */
public class TenantUtils {

  /**
   * TODO mlowery make this configurable
   */
  public static final String TENANTID_SINGLE_TENANT = "tenant0"; //$NON-NLS-1$

  /**
   * Returns the tenant ID of the current user.
   */
  public static ITenant getCurrentTenant() {
    IPentahoSession pentahoSession = PentahoSessionHolder.getSession();
    if (pentahoSession == null) {
      throw new IllegalStateException();
    }
    
    String tenantId  = (String) pentahoSession.getAttribute(IPentahoSession.TENANT_ID_KEY);
    
    if(tenantId == null) {
      ITenantedPrincipleNameResolver tenantedUserNameUtils = PentahoSystem.get(ITenantedPrincipleNameResolver.class, "tenantedUserNameUtils", pentahoSession);
      if(tenantedUserNameUtils != null) {
        ITenant tenant = tenantedUserNameUtils.getTenant(pentahoSession.getId());
        pentahoSession.setAttribute(IPentahoSession.TENANT_ID_KEY, tenant.getId());
        return new Tenant(tenant.getId(), true);
      }
    }
    
    return new Tenant(tenantId, true);
  }

  public static String getDefaultTenant() {
    return TENANTID_SINGLE_TENANT;
  }
  
  public static boolean isAccessibleTenant(ITenant tenant) {
    ITenant currentTenant = TenantUtils.getCurrentTenant();
    try {
      return currentTenant.getId() == null || tenant.getRootFolderAbsolutePath().startsWith(currentTenant.getRootFolderAbsolutePath() + RepositoryFile.SEPARATOR)
          || tenant.equals(currentTenant);
    } catch (NullPointerException ex) {
    }
    return false;
  }
}