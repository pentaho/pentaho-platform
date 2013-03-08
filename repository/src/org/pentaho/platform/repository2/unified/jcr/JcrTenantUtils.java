package org.pentaho.platform.repository2.unified.jcr;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.core.mt.Tenant;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.TenantUtils;
import org.pentaho.platform.repository2.unified.ServerRepositoryPaths;

public class JcrTenantUtils {
  
  private static ITenantedPrincipleNameResolver userNameUtils;
  private static ITenantedPrincipleNameResolver roleNameUtils;
  private static String repositoryAdminUsername;
  public static ITenantedPrincipleNameResolver getUserNameUtils() {
    if (userNameUtils == null && PentahoSystem.getInitializedOK()) {
      userNameUtils = PentahoSystem.get(ITenantedPrincipleNameResolver.class, "tenantedUserNameUtils",
          PentahoSessionHolder.getSession());
    }
    return userNameUtils;
   }

  public static ITenantedPrincipleNameResolver getRoleNameUtils() {
    if (roleNameUtils == null && PentahoSystem.getInitializedOK()) {
      roleNameUtils = PentahoSystem.get(ITenantedPrincipleNameResolver.class, "tenantedRoleNameUtils",
          PentahoSessionHolder.getSession());
    } 
    return roleNameUtils;
  }

  private static String getRepositoryAdminUserName() {
    
    if (repositoryAdminUsername == null && PentahoSystem.getInitializedOK()) {
      repositoryAdminUsername = PentahoSystem.get(String.class, "repositoryAdminUsername", PentahoSessionHolder.getSession());
    }
    return repositoryAdminUsername;
  }
  public static String getTenantedRole(String principal) {
    if (principal != null && !principal.equals("administrators") && getRoleNameUtils() != null) {
      ITenant tenant = getRoleNameUtils().getTenant(principal);
      if (tenant == null || tenant.getId() == null) {
        IPentahoSession pentahoSession = PentahoSessionHolder.getSession();
        String tenantId = (String) pentahoSession.getAttribute(IPentahoSession.TENANT_ID_KEY);
        if(tenantId == null) {
          tenantId = getDefaultTenantPath();
        }
        tenant = new Tenant(tenantId, true);
        return getRoleNameUtils().getPrincipleId(tenant, principal);
      } else {
        return principal;
      }
    } else {
      return principal;
    }
  }

  public static String getTenantedUser(String username) {
    if (username != null && !username.equals(getRepositoryAdminUserName()) && getUserNameUtils() != null) {
      ITenant tenant = getUserNameUtils().getTenant(username);
      if (tenant == null || tenant.getId() == null) {
        IPentahoSession pentahoSession = PentahoSessionHolder.getSession();
        String tenantId = (String) pentahoSession.getAttribute(IPentahoSession.TENANT_ID_KEY);
        if(tenantId == null) {
          tenantId = getDefaultTenantPath();
        }
        tenant = new Tenant(tenantId, true);
        return getUserNameUtils().getPrincipleId(tenant, username);
      } else {
        return username;
      }
    } else {
      return username;
    }
  }

  public static ITenant getTenant(String principalId, boolean isUser) {
    ITenant tenant = null;
    ITenantedPrincipleNameResolver nameUtils = isUser ? getUserNameUtils() : getRoleNameUtils();
    if (nameUtils != null) {
      tenant = nameUtils.getTenant(principalId);
    }
    if (tenant == null || tenant.getId() == null) {
      tenant = getCurrentTenant();
    }
    if(tenant == null || tenant.getId() == null) {
      tenant = getDefaultTenant();
    }
    return tenant;
  }

  public static ITenant getTenant() {
    ITenant tenant = getCurrentTenant();
    if(tenant == null || tenant.getId() == null) {
      tenant = getDefaultTenant();
    }
    return tenant;
  }
  public static String getPrincipalName(String principalId, boolean isUser) {
    String principalName = null;
    ITenantedPrincipleNameResolver nameUtils = isUser ? getUserNameUtils() : getRoleNameUtils();
    if (nameUtils != null) {
      principalName = nameUtils.getPrincipleName(principalId);
    }
    return principalName;
  }
  
  public static ITenant getCurrentTenant() {
    if(PentahoSessionHolder.getSession() != null) {
      String tenantId = (String) PentahoSessionHolder.getSession().getAttribute(IPentahoSession.TENANT_ID_KEY);
      return tenantId != null ? new Tenant(tenantId, true) : null;
    } else return null;
  }
  
  public static String getDefaultTenantPath() {
    return ServerRepositoryPaths.getPentahoRootFolderPath() + RepositoryFile.SEPARATOR + TenantUtils.TENANTID_SINGLE_TENANT;
  }

  public static  ITenant getDefaultTenant() {
    return new Tenant(getDefaultTenantPath(), true);
  }
}
