package org.pentaho.platform.security.policy.rolebased;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.jcr.NamespaceException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.apache.commons.collections.map.LRUMap;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.security.userroledao.NotFoundException;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.core.mt.Tenant;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.TenantUtils;
import org.pentaho.platform.repository2.unified.ServerRepositoryPaths;
import org.pentaho.platform.repository2.unified.jcr.PentahoJcrConstants;
import org.pentaho.platform.security.policy.rolebased.messages.Messages;
import org.springframework.util.Assert;

import com.google.common.collect.HashMultimap;

public abstract class AbstractJcrBackedRoleBindingDao implements IRoleAuthorizationPolicyRoleBindingDao {

  protected ITenantedPrincipleNameResolver tenantedRoleNameUtils;
  
  protected Map<String, List<String>> immutableRoleBindings;
  
  protected Map<String, List<String>> bootstrapRoleBindings;
  
  protected String superAdminRoleName;
  
  private Set<String> logicalRoles = new HashSet<String>();
  
  public static final String FOLDER_NAME_AUTHZ = ".authz"; //$NON-NLS-1$

  public static final String FOLDER_NAME_ROLEBASED = "roleBased"; //$NON-NLS-1$

  public static final String FOLDER_NAME_RUNTIMEROLES = "runtimeRoles"; //$NON-NLS-1$
  /**
   * Key: runtime role name; value: list of logical role names
   */
  @SuppressWarnings("unchecked")
  protected Map boundLogicalRoleNamesCache = Collections.synchronizedMap(new LRUMap());
  
  public AbstractJcrBackedRoleBindingDao(final List<String> logicalRoleNames, final Map<String, List<String>> immutableRoleBindings, final Map<String, List<String>> bootstrapRoleBindings,
      final String superAdminRoleName, final ITenantedPrincipleNameResolver tenantedRoleNameUtils) {
    super();
    Assert.notNull(immutableRoleBindings);
    Assert.notNull(bootstrapRoleBindings);
    Assert.notNull(superAdminRoleName);
    this.logicalRoles.addAll(IAuthorizationPolicy.PREDEFINED_SYSTEM_LOGICAL_ROLES);
    if (logicalRoleNames != null) {
      this.logicalRoles.addAll(logicalRoleNames);
    }
    this.immutableRoleBindings = immutableRoleBindings;
    this.bootstrapRoleBindings = bootstrapRoleBindings;
    this.superAdminRoleName = superAdminRoleName;
    this.tenantedRoleNameUtils = tenantedRoleNameUtils;
  }
  
  public List<String> getBoundLogicalRoleNames(Session session, List<String> runtimeRoleNames) throws NamespaceException, RepositoryException {
    Set<String> boundRoleNames = new HashSet<String>();
    HashMap<ITenant, List<String>> tenantMap = new HashMap<ITenant, List<String>>();
    boolean includeSuperAdminLogicalRoles = false;
    for (String runtimeRoleName : runtimeRoleNames) {
      if (!superAdminRoleName.equals(runtimeRoleName)) {
        ITenant tenant = tenantedRoleNameUtils.getTenant(runtimeRoleName);
        if (tenant == null || tenant.getId() == null) {
          tenant = getCurrentTenant();
        }
        if (tenant != null) {
          List<String> runtimeRoles = tenantMap.get(tenant);
          if (runtimeRoles == null) {
            runtimeRoles = new ArrayList<String>();
            tenantMap.put(tenant, runtimeRoles);
          }
          runtimeRoles.add(tenantedRoleNameUtils.getPrincipleName(runtimeRoleName));
        }
      } else {
        includeSuperAdminLogicalRoles = true;
      }
    }
    for (Map.Entry<ITenant, List<String>> mapEntry : tenantMap.entrySet()) {
      boundRoleNames.addAll(getBoundLogicalRoleNames(session, mapEntry.getKey(), mapEntry.getValue()));
    }
    if (includeSuperAdminLogicalRoles) {
      boundRoleNames.addAll(immutableRoleBindings.get(superAdminRoleName));
    }
    return new ArrayList<String>(boundRoleNames);
  }

  public List<String> getBoundLogicalRoleNames(Session session, ITenant tenant, List<String> runtimeRoleNames) throws NamespaceException, RepositoryException {
    if ((tenant == null) || (tenant.getId() == null)){
      return getBoundLogicalRoleNames(session, runtimeRoleNames);
    }
    
    if (!TenantUtils.isAccessibleTenant(tenant)) {
      return new ArrayList<String>();
    }
    
    final List<String> uncachedRuntimeRoleNames = new ArrayList<String>();
    final Set<String> cachedBoundLogicalRoleNames = new HashSet<String>();
    for (String runtimeRoleName : runtimeRoleNames) {
      String roleName = tenantedRoleNameUtils.getPrincipleName(runtimeRoleName);
      String roleId = tenantedRoleNameUtils.getPrincipleId(tenant, runtimeRoleName);
      if (boundLogicalRoleNamesCache.containsKey(roleId)) {
        cachedBoundLogicalRoleNames.addAll((Collection<String>) boundLogicalRoleNamesCache.get(roleId));
      } else {
        uncachedRuntimeRoleNames.add(roleName);
      }
    }
    if (uncachedRuntimeRoleNames.isEmpty()) {
      // no need to hit the repo
      return new ArrayList<String>(cachedBoundLogicalRoleNames);
    }
    
    PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
    final String phoNsPrefix = session.getNamespacePrefix(PentahoJcrConstants.PHO_NS) + ":"; //$NON-NLS-1$
    final String onlyPentahoPattern = phoNsPrefix + "*"; //$NON-NLS-1$
    HashMultimap<String, String> boundLogicalRoleNames = HashMultimap.create();
    Node runtimeRolesFolderNode = getRuntimeRolesFolderNode(session, tenant);
    NodeIterator runtimeRoleNodes = runtimeRolesFolderNode.getNodes(onlyPentahoPattern);
    if (!runtimeRoleNodes.hasNext()) {
      // no bindings setup yet; fall back on bootstrap bindings
      for (String runtimeRoleName : uncachedRuntimeRoleNames) {
        String roleId = tenantedRoleNameUtils.getPrincipleId(tenant, runtimeRoleName);
        if (bootstrapRoleBindings.containsKey(runtimeRoleName)) {
          boundLogicalRoleNames.putAll(roleId, bootstrapRoleBindings.get(runtimeRoleName));
        }
      }
    } else {
      for (String runtimeRoleName : uncachedRuntimeRoleNames) {
        if (runtimeRolesFolderNode.hasNode(phoNsPrefix + runtimeRoleName)) {
          Node runtimeRoleFolderNode = runtimeRolesFolderNode.getNode(phoNsPrefix + runtimeRoleName);
          if (runtimeRoleFolderNode.hasProperty(pentahoJcrConstants.getPHO_BOUNDROLES())) {
            Value[] values = runtimeRoleFolderNode.getProperty(pentahoJcrConstants.getPHO_BOUNDROLES())
                .getValues();
            String roleId = tenantedRoleNameUtils.getPrincipleId(tenant, runtimeRoleName);
            for (Value value : values) {
              boundLogicalRoleNames.put(roleId, value.getString());
            }
          }
        }
      }
    }
    // now add in immutable bound logical role names
    for (String runtimeRoleName : uncachedRuntimeRoleNames) {
      if (immutableRoleBindings.containsKey(runtimeRoleName)) {
        String roleId = tenantedRoleNameUtils.getPrincipleId(tenant, runtimeRoleName);
        boundLogicalRoleNames.putAll(roleId, immutableRoleBindings.get(runtimeRoleName));
      }
    }
    
    // update cache
    boundLogicalRoleNamesCache.putAll(boundLogicalRoleNames.asMap());
    // now add in those runtime roles that have no bindings to the cache
    for (String runtimeRoleName : uncachedRuntimeRoleNames) {
      String roleId = tenantedRoleNameUtils.getPrincipleId(tenant, runtimeRoleName);
      if (!boundLogicalRoleNamesCache.containsKey(roleId)) {
        boundLogicalRoleNamesCache.put(roleId, Collections.emptyList());
      }
    }
    
    // combine cached findings plus ones from repo
    Set<String> res = new HashSet<String>();
    res.addAll(cachedBoundLogicalRoleNames);
    res.addAll(boundLogicalRoleNames.values());
    return new ArrayList<String>(res);
  }

  public void setRoleBindings(Session session, ITenant tenant, String runtimeRoleName, List<String> logicalRoleNames) throws NamespaceException, RepositoryException {
    if (tenant == null) {
      tenant = getTenant(runtimeRoleName);
      runtimeRoleName = getPrincipalName(runtimeRoleName);
    }
    
    if (tenant == null || tenant.getId() == null) {
      tenant = getCurrentTenant();
    }
    
    if (!TenantUtils.isAccessibleTenant(tenant)) {
      throw new NotFoundException("Tenant " + tenant.getId() + " not found");
    }
    
    PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
    final String phoNsPrefix = session.getNamespacePrefix(PentahoJcrConstants.PHO_NS) + ":"; //$NON-NLS-1$
    final String onlyPentahoPattern = phoNsPrefix + "*"; //$NON-NLS-1$
    Node runtimeRolesFolderNode = getRuntimeRolesFolderNode(session, tenant);
    NodeIterator runtimeRoleNodes = runtimeRolesFolderNode.getNodes(onlyPentahoPattern);
    int i = 0;
    while (runtimeRoleNodes.hasNext()) {
      runtimeRoleNodes.nextNode();
      i++;
    }
    if (i == 0) {
      // no bindings setup yet; install bootstrap bindings; bootstrapRoleBindings will now no longer be 
      // consulted
      for (Map.Entry<String, List<String>> entry : bootstrapRoleBindings.entrySet()) {
        JcrRoleAuthorizationPolicyUtils.internalSetBindings(pentahoJcrConstants, runtimeRolesFolderNode, phoNsPrefix
            + entry.getKey(), entry.getValue());
      }
    }
    if (!isImmutable(runtimeRoleName)) {
      JcrRoleAuthorizationPolicyUtils.internalSetBindings(pentahoJcrConstants, runtimeRolesFolderNode, phoNsPrefix + runtimeRoleName,
          logicalRoleNames);
    } else {
      throw new RuntimeException(Messages.getInstance().getString(
          "JcrRoleAuthorizationPolicyRoleBindingDao.ERROR_0001_ATTEMPT_MOD_IMMUTABLE", runtimeRoleName)); //$NON-NLS-1$
    }
    session.save();
    Assert.isTrue(runtimeRolesFolderNode.hasNode(phoNsPrefix + runtimeRoleName));
    
    // update cache
    String roleId = tenantedRoleNameUtils.getPrincipleId(tenant, runtimeRoleName);
    boundLogicalRoleNamesCache.put(roleId, logicalRoleNames);
  }
  
  protected ITenant getCurrentTenant() {
    String tenantId = (String) PentahoSessionHolder.getSession().getAttribute(IPentahoSession.TENANT_ID_KEY);
    return tenantId != null ? new Tenant(tenantId, true) : null;
  }
  
  protected ITenant getTenant(String principalId) {
    ITenant tenant = null;
    if (tenantedRoleNameUtils != null) {
      tenant = tenantedRoleNameUtils.getTenant(principalId);
    }
    if (tenant == null || tenant.getId() == null) {
      tenant = getCurrentTenant();
    }
    return tenant;
  }

  private String getPrincipalName(String principalId) {
    String principalName = null;
    if (tenantedRoleNameUtils != null) {
      principalName = tenantedRoleNameUtils.getPrincipleName(principalId);
    }
    return principalName;
  }
    
  protected boolean isImmutable(final String runtimeRoleName) {
    return immutableRoleBindings.containsKey(runtimeRoleName);
  }

  protected Map<String, List<String>> getRoleBindings(Session session, ITenant tenant) throws RepositoryException {
    Map<String, List<String>> map = new HashMap<String, List<String>>();
    if (tenant == null) {
      tenant = getCurrentTenant();
    }
    if (!TenantUtils.isAccessibleTenant(tenant)) {
      return map;
    }
    PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
    final String phoNsPrefix = session.getNamespacePrefix(PentahoJcrConstants.PHO_NS) + ":"; //$NON-NLS-1$
    final String onlyPentahoPattern = phoNsPrefix + "*"; //$NON-NLS-1$
    Node runtimeRolesFolderNode = getRuntimeRolesFolderNode(session, tenant);
    NodeIterator runtimeRoleNodes = runtimeRolesFolderNode.getNodes(onlyPentahoPattern);
    if (!runtimeRoleNodes.hasNext()) {
      // no bindings setup yet; fall back on bootstrap bindings
      map.putAll(bootstrapRoleBindings);
    } else {
      while (runtimeRoleNodes.hasNext()) {
        Node runtimeRoleNode = runtimeRoleNodes.nextNode();
        if (runtimeRoleNode.hasProperty(pentahoJcrConstants.getPHO_BOUNDROLES())) {
          // get clean runtime role name
          String runtimeRoleName = runtimeRoleNode.getName().substring(phoNsPrefix.length());
          // get logical role names
          List<String> logicalRoleNames = new ArrayList<String>();
          Value[] values = runtimeRoleNode.getProperty(pentahoJcrConstants.getPHO_BOUNDROLES()).getValues();
          for (Value value : values) {
            logicalRoleNames.add(value.getString());
          }
          map.put(runtimeRoleName, logicalRoleNames);
        }
      }
    }
    // add all immutable bindings
    map.putAll(immutableRoleBindings);
    return map;
  }

  public RoleBindingStruct getRoleBindingStruct(Session session, ITenant tenant, String locale) throws RepositoryException {
    return new RoleBindingStruct(getMapForLocale(locale), getRoleBindings(session, tenant));
  }
  
  protected Map<String, String> getMapForLocale(final String localeString) {
    final String UNDERSCORE = "_"; //$NON-NLS-1$
    Locale locale;

    ResourceBundle resourceBundle = null;
    if (localeString == null) {
      resourceBundle = Messages.getInstance().getBundle();
    } else {
      String[] tokens = localeString.split(UNDERSCORE);
      if (tokens.length == 3) {
        locale = new Locale(tokens[0], tokens[1], tokens[2]);
      } else if (tokens.length == 2) {
        locale = new Locale(tokens[0], tokens[1]);
      } else {
        locale = new Locale(tokens[0]);
      }
      resourceBundle = Messages.getInstance().getBundle(locale);
    }

    Map<String, String> map = new HashMap<String, String>();
    for (String logicalRoleName : logicalRoles) {
      map.put(logicalRoleName, resourceBundle.getString(logicalRoleName));
    }
    return map;
  }

  public Node getRuntimeRolesFolderNode(final Session session, ITenant tenant) throws RepositoryException {
    Node tenantRootFolderNode = null;
    try {
      tenantRootFolderNode = (Node) session.getItem(ServerRepositoryPaths.getTenantRootFolderPath(tenant));
    } catch (PathNotFoundException e) {
      Assert.state(false, Messages.getInstance().getString(
          "JcrRoleAuthorizationPolicyRoleBindingDao.ERROR_0002_REPO_NOT_INITIALIZED")); //$NON-NLS-1$
    }
    Node authzFolderNode =  tenantRootFolderNode.getNode(FOLDER_NAME_AUTHZ);
    Node roleBasedFolderNode = authzFolderNode.getNode(FOLDER_NAME_ROLEBASED);
    return roleBasedFolderNode.getNode(FOLDER_NAME_RUNTIMEROLES);
  }
}
