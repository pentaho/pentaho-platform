package org.pentaho.test.mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.security.userroledao.AlreadyExistsException;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoRole;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoUser;
import org.pentaho.platform.api.engine.security.userroledao.IUserRoleDao;
import org.pentaho.platform.api.engine.security.userroledao.NotFoundException;
import org.pentaho.platform.api.engine.security.userroledao.UncategorizedUserRoleDaoException;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.core.mt.Tenant;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.TenantUtils;



public class MockUserRoleDao implements IUserRoleDao {

  HashSet<ITenant> tenants = new HashSet<ITenant>();
  HashMap<ITenant, HashSet<IPentahoUser>> tenantUsers = new HashMap<ITenant, HashSet<IPentahoUser>>();
  HashMap<ITenant, HashSet<IPentahoRole>> tenantRoles = new HashMap<ITenant, HashSet<IPentahoRole>>();
  HashMap<IPentahoUser, HashSet<IPentahoRole>> userRoles = new HashMap<IPentahoUser, HashSet<IPentahoRole>>();
  HashMap<IPentahoRole, HashSet<IPentahoUser>> roleMembers = new HashMap<IPentahoRole, HashSet<IPentahoUser>>();
  
  protected ITenantedPrincipleNameResolver userNameResolver;
  protected ITenantedPrincipleNameResolver roleNameResolver;
  protected String authenticatedRoleName;
  
  public MockUserRoleDao(ITenantedPrincipleNameResolver userNameResolver, ITenantedPrincipleNameResolver roleNameResolver, String authenticatedRoleName) {
    this.userNameResolver = userNameResolver;
    this.roleNameResolver = roleNameResolver;
    this.authenticatedRoleName = authenticatedRoleName;
  }
  
  private void addTenant(ITenant tenant) {
    if (!tenants.contains(tenant)) {
      tenants.add(tenant);
      tenantUsers.put(tenant, new HashSet<IPentahoUser>());
      tenantRoles.put(tenant, new HashSet<IPentahoRole>());
      createRole(tenant, authenticatedRoleName, "", new String[0]);
    }
  }
  
  public IPentahoRole createRole(ITenant tenant, String roleName, String description, String[] memberUserNames) throws AlreadyExistsException, UncategorizedUserRoleDaoException {
    if (tenant == null) {
      tenant = getTenant(roleName, false);
      roleName = getPrincipalName(roleName, false);
    }
    if (tenant == null || tenant.getId() == null) {
      tenant = getCurrentTenant();
    }
    
    addTenant(tenant);
    MockPentahoRole role = new MockPentahoRole(tenant, roleName, description);
    if (!tenantRoles.get(tenant).contains(role)) {
      tenantRoles.get(tenant).add(role);
      roleMembers.put(role, new HashSet<IPentahoUser>());
    }
    setRoleMembers(tenant, roleName, memberUserNames);
    return role;
  }
  
  public IPentahoUser createUser(ITenant tenant, String username, String password, String description, String[] roles) throws AlreadyExistsException, UncategorizedUserRoleDaoException {
    if (tenant == null) {
      tenant = getTenant(username, true);
      username = getPrincipalName(username, true);
    }
    if (tenant == null || tenant.getId() == null) {
      tenant = getCurrentTenant();
    }
    
    addTenant(tenant);
    MockPentahoUser user = new MockPentahoUser(tenant, username, password, description, true);
    if (!tenantUsers.get(tenant).contains(user)) {
      tenantUsers.get(tenant).add(user);
      userRoles.put(user, new HashSet<IPentahoRole>());
    }
    setUserRoles(tenant, username, roles);
    return user;
  }
  
  public RepositoryFile createUserHomeFolder(ITenant tenant, String arg1) {
    throw new UnsupportedOperationException();
  }
  
  public void deleteRole(IPentahoRole role) throws NotFoundException, UncategorizedUserRoleDaoException {
    tenantRoles.get(role.getTenant()).remove(role);
    roleMembers.remove(role);
    for (HashSet<IPentahoRole> roles : userRoles.values()) {
      roles.remove(role);
    }
  }
  
  public void deleteUser(IPentahoUser user) throws NotFoundException, UncategorizedUserRoleDaoException {
    tenantUsers.get(user.getTenant()).remove(user);
    userRoles.remove(user);
    for (HashSet<IPentahoUser> users : roleMembers.values()) {
      users.remove(user);
    }
  }
  
  public IPentahoRole getRole(ITenant tenant, String roleName) throws UncategorizedUserRoleDaoException {
    if (tenant == null) {
      tenant = getTenant(roleName, false);
      roleName = getPrincipalName(roleName, false);
    }
    if (tenant == null || tenant.getId() == null) {
      tenant = getCurrentTenant();
    }
    
    HashSet<IPentahoRole> roles = tenantRoles.get(tenant);
    if (roles != null) {
      for (IPentahoRole role : roles) {
        if (role.getName().equals(roleName)) {
          return role;
        }
      }
    }
    return null;
  }
  
  public List<IPentahoUser> getRoleMembers(ITenant tenant, String roleName) throws UncategorizedUserRoleDaoException {
    if (tenant == null) {
      tenant = getTenant(roleName, false);
      roleName = getPrincipalName(roleName, false);
    }
    if (tenant == null || tenant.getId() == null) {
      tenant = getCurrentTenant();
    }
    
    IPentahoRole role = getRole(tenant, roleName);
    return Collections.list(Collections.enumeration(roleMembers.get(role)));
  }
  
  public List<IPentahoRole> getRoles() throws UncategorizedUserRoleDaoException {
    return getRoles(getCurrentTenant());
  }
  
  public List<IPentahoRole> getRoles(ITenant tenant, boolean includeSubTenants) throws UncategorizedUserRoleDaoException {
    ArrayList<IPentahoRole> roles = new ArrayList<IPentahoRole>();
          roles.addAll(getRoles(tenant));
    if (includeSubTenants) {
      for (ITenant tmpTenant : tenants) {
        if (tmpTenant.getRootFolderAbsolutePath().startsWith(tenant.getRootFolderAbsolutePath() + "/", 0)) {
          roles.addAll(getRoles(tmpTenant));
        }
      }
    }
    return roles;
  }
  
  public List<IPentahoRole> getRoles(ITenant tenant) throws UncategorizedUserRoleDaoException {
    if(TenantUtils.getCurrentTenant().equals(tenant)) {
      return Collections.list(Collections.enumeration(tenantRoles.get(tenant)));  
    } else {
      return Collections.emptyList();
    }
  }
  
  public IPentahoUser getUser(ITenant tenant, String userName) throws UncategorizedUserRoleDaoException {
    if (tenant == null) {
      tenant = getTenant(userName, true);
      userName = getPrincipalName(userName, true);
    }
    if (tenant == null || tenant.getId() == null) {
      tenant = getCurrentTenant();
    }
    
    HashSet<IPentahoUser> users = tenantUsers.get(tenant);
    if (users != null) {
      for (IPentahoUser user : users) {
        if (user.getUsername().equals(userName)) {
          return user;
        }
      }
    }
    return null;
  }
  
  public List<IPentahoRole> getUserRoles(ITenant tenant, String userName) throws UncategorizedUserRoleDaoException {
    if (tenant == null) {
      tenant = getTenant(userName, true);
      userName = getPrincipalName(userName, true);
    }
    if (tenant == null || tenant.getId() == null) {
      tenant = getCurrentTenant();
    }
    
    IPentahoUser user = getUser(tenant, userName);
    return Collections.list(Collections.enumeration(userRoles.get(user)));
  }
  
  public List<IPentahoUser> getUsers() throws UncategorizedUserRoleDaoException {
    return getUsers(getCurrentTenant());
  }
  
  public List<IPentahoUser> getUsers(ITenant tenant, boolean includeSubTenants) throws UncategorizedUserRoleDaoException {
    ArrayList<IPentahoUser> users = new ArrayList<IPentahoUser>();
          users.addAll(getUsers(tenant));
    if (includeSubTenants) {
      for (ITenant tmpTenant : tenants) {
        if (tmpTenant.getRootFolderAbsolutePath().startsWith(tenant.getRootFolderAbsolutePath() + "/", 0)) {
          users.addAll(getUsers(tmpTenant));
        }
      }
    }
    return users;
  }
  
  public List<IPentahoUser> getUsers(ITenant tenant) throws UncategorizedUserRoleDaoException {
    if(TenantUtils.getCurrentTenant().equals(tenant)) {
      return Collections.list(Collections.enumeration(tenantUsers.get(tenant)));  
    } else {
      return Collections.emptyList();
    }
  }
  
  public void setPassword(ITenant tenant, String userName, String password) throws NotFoundException, UncategorizedUserRoleDaoException {
    throw new UnsupportedOperationException();
  }
  
  public void setRoleDescription(ITenant tenant, String roleName, String description) throws NotFoundException, UncategorizedUserRoleDaoException {
    throw new UnsupportedOperationException();
  }
  
  public void setRoleMembers(ITenant tenant, String roleName, String[] userNames) throws NotFoundException, UncategorizedUserRoleDaoException {
    if (tenant == null) {
      tenant = getTenant(roleName, false);
      roleName = getPrincipalName(roleName, false);
    }
    if (tenant == null || tenant.getId() == null) {
      tenant = getCurrentTenant();
    }
    
    IPentahoRole role = getRole(tenant, roleName);
    HashSet<IPentahoUser> users = roleMembers.get(role);
    users.clear();
    if(userNames != null) {
      for (String userName : userNames) {
        IPentahoUser user = getUser(tenant, userName);
        if (user != null) {
          users.add(user);
        }
      }
    }
  }
  
  public void setUserDescription(ITenant tenant, String userName, String description) throws NotFoundException, UncategorizedUserRoleDaoException {
    throw new UnsupportedOperationException();
  }
  
  public void setUserRoles(ITenant tenant, String userName, String[] roleNames) throws NotFoundException, UncategorizedUserRoleDaoException {
    if (tenant == null) {
      tenant = getTenant(userName, true);
      userName = getPrincipalName(userName, true);
    }
    if (tenant == null || tenant.getId() == null) {
      tenant = getCurrentTenant();
    }
    
    Set<String> roleSet = new HashSet<String>();
    if (roleNames != null) {
      roleSet.addAll(Arrays.asList(roleNames));
    }
    roleSet.add(authenticatedRoleName);
    
    IPentahoUser user = getUser(tenant, userName);
    HashSet<IPentahoRole> roles = userRoles.get(user);
    roles.clear();
    for (String roleName : roleSet) {
      IPentahoRole role = getRole(tenant, roleName);
      if (role != null) {
        roles.add(role);
      }
    }
  }
  
  protected ITenant getCurrentTenant() {
    if(PentahoSessionHolder.getSession() != null) {
      String tenantId = (String) PentahoSessionHolder.getSession().getAttribute(IPentahoSession.TENANT_ID_KEY);
      return tenantId != null ? new Tenant(tenantId, true) : null;
    }
    return null;
  }
  
  protected ITenant getTenant(String principalId, boolean isUser) {
    ITenant tenant = null;
    ITenantedPrincipleNameResolver nameUtils = isUser ? userNameResolver : roleNameResolver;
    if (nameUtils != null) {
      tenant = nameUtils.getTenant(principalId);
    }
    if (tenant == null || tenant.getId() == null) {
      tenant = getCurrentTenant();
    }
    return tenant;
  }
  
  protected String getPrincipalName(String principalId, boolean isUser) {
    String principalName = null;
    ITenantedPrincipleNameResolver nameUtils = isUser ? userNameResolver : roleNameResolver;
    if (nameUtils != null) {
      principalName = nameUtils.getPrincipleName(principalId);
    }
    return principalName;
  }

}
