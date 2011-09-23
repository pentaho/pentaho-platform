package org.pentaho.platform.engine.core.system;

import org.pentaho.platform.api.engine.IPentahoSession;

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
  public static String getTenantId() {
    IPentahoSession pentahoSession = PentahoSessionHolder.getSession();
    if (pentahoSession == null) {
      throw new IllegalStateException();
    }
    String tenantId = (String) pentahoSession.getAttribute(IPentahoSession.TENANT_ID_KEY);
    if (tenantId == null) {
      tenantId = TENANTID_SINGLE_TENANT;
    }
    return tenantId;
  }

}