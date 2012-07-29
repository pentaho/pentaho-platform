package org.pentaho.platform.security.userroledao.jackrabbit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.jcr.Credentials;
import javax.jcr.NamespaceException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.AuthorizableExistsException;
import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.security.authentication.CryptedSimpleCredentials;
import org.apache.jackrabbit.core.security.principal.PrincipalImpl;
import org.apache.jackrabbit.core.security.user.UserManagerImpl;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.NameFactory;
import org.apache.jackrabbit.spi.commons.name.NameFactoryImpl;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoRole;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoUser;
import org.pentaho.platform.api.engine.security.userroledao.IUserRoleDao;
import org.pentaho.platform.api.engine.security.userroledao.NotFoundException;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.core.mt.Tenant;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.TenantUtils;
import org.pentaho.platform.repository2.unified.IRepositoryFileAclDao;
import org.pentaho.platform.repository2.unified.IRepositoryFileDao;
import org.pentaho.platform.repository2.unified.ServerRepositoryPaths;
import org.pentaho.platform.security.userroledao.PentahoRole;
import org.pentaho.platform.security.userroledao.PentahoUser;

public abstract class AbstractJcrBackedUserRoleDao implements IUserRoleDao {

  NameFactory NF = NameFactoryImpl.getInstance();

  Name P_PRINCIPAL_NAME = NF.create(Name.NS_REP_URI, "principalName"); //$NON-NLS-1$

  protected ITenantedPrincipleNameResolver tenantedUserNameUtils;

  protected ITenantedPrincipleNameResolver tenantedRoleNameUtils;

  String pPrincipalName = "rep:principalName"; //$NON-NLS-1$

  IRepositoryFileAclDao repositoryFileAclDao;

  IRepositoryFileDao repositoryFileDao;

  String defaultTenant;

  String authenticatedRoleName;

  String tenantAdminRoleName;

  String repositoryAdminUsername;

  HashMap<String, UserManagerImpl> userMgrMap = new HashMap<String, UserManagerImpl>();

  public AbstractJcrBackedUserRoleDao(ITenantedPrincipleNameResolver userNameUtils,
      ITenantedPrincipleNameResolver roleNameUtils, String authenticatedRoleName, String tenantAdminRoleName,
      String repositoryAdminUsername, IRepositoryFileAclDao repositoryFileAclDao, IRepositoryFileDao repositoryFileDao)
      throws NamespaceException {
    this.tenantedUserNameUtils = userNameUtils;
    this.tenantedRoleNameUtils = roleNameUtils;
    this.authenticatedRoleName = authenticatedRoleName;
    this.tenantAdminRoleName = tenantAdminRoleName;
    this.repositoryAdminUsername = repositoryAdminUsername;
    this.repositoryFileAclDao = repositoryFileAclDao;
    this.repositoryFileDao = repositoryFileDao;
  }

  public void setRoleMembers(Session session, final ITenant theTenant, final String roleName,
      final String[] memberUserNames) throws RepositoryException, NotFoundException {
    Group jackrabbitGroup = getJackrabbitGroup(theTenant, roleName, session);

    if ((jackrabbitGroup == null)
        || !TenantUtils.isAccessibleTenant(theTenant == null ? tenantedRoleNameUtils.getTenant(jackrabbitGroup.getID()) : theTenant)) {
      throw new NotFoundException("Role not found");
    }
    HashMap<String, User> currentlyAssignedUsers = new HashMap<String, User>();
    Iterator<Authorizable> currentMembers = jackrabbitGroup.getMembers();
    while (currentMembers.hasNext()) {
      Authorizable member = currentMembers.next();
      if (member instanceof User) {
        currentlyAssignedUsers.put(member.getID(), (User) member);
      }
    }

    HashMap<String, User> finalCollectionOfAssignedUsers = new HashMap<String, User>();
    if (memberUserNames != null) {
      ITenant tenant = theTenant == null ? getTenant(roleName, false) : theTenant;
      for (String user : memberUserNames) {
        User jackrabbitUser = getJackrabbitUser(tenant, user, session);
        if (jackrabbitUser != null) {
          finalCollectionOfAssignedUsers.put(tenantedRoleNameUtils.getPrincipleId(tenant, user), jackrabbitUser);
        }
      }
    }

    ArrayList<String> usersToRemove = new ArrayList<String>(currentlyAssignedUsers.keySet());
    usersToRemove.removeAll(finalCollectionOfAssignedUsers.keySet());

    ArrayList<String> usersToAdd = new ArrayList<String>(finalCollectionOfAssignedUsers.keySet());
    usersToAdd.removeAll(currentlyAssignedUsers.keySet());

    for (String userId : usersToRemove) {
      jackrabbitGroup.removeMember(currentlyAssignedUsers.get(userId));
    }

    for (String userId : usersToAdd) {
      jackrabbitGroup.addMember(finalCollectionOfAssignedUsers.get(userId));
    }
  }

  public void setUserRoles(Session session, final ITenant theTenant, final String userName, final String[] roles)
      throws RepositoryException, NotFoundException {
    Set<String> roleSet = new HashSet<String>();
    if (roles != null) {
      roleSet.addAll(Arrays.asList(roles));
    }
    roleSet.add(authenticatedRoleName);

    User jackrabbitUser = getJackrabbitUser(theTenant, userName, session);

    if ((jackrabbitUser == null)
        || !TenantUtils.isAccessibleTenant(theTenant == null ? tenantedUserNameUtils.getTenant(jackrabbitUser.getID()) : theTenant)) {
      throw new NotFoundException("User not found");
    }
    HashMap<String, Group> currentlyAssignedGroups = new HashMap<String, Group>();
    Iterator<Group> currentGroups = jackrabbitUser.memberOf();
    while (currentGroups.hasNext()) {
      Group currentGroup = currentGroups.next();
      currentlyAssignedGroups.put(currentGroup.getID(), currentGroup);
    }

    HashMap<String, Group> finalCollectionOfAssignedGroups = new HashMap<String, Group>();
    ITenant tenant = theTenant == null ? getTenant(userName, true) : theTenant;
    for (String role : roleSet) {
      Group jackrabbitGroup = getJackrabbitGroup(tenant, role, session);
      if (jackrabbitGroup != null) {
        finalCollectionOfAssignedGroups.put(tenantedRoleNameUtils.getPrincipleId(tenant, role), jackrabbitGroup);
      }
    }

    ArrayList<String> groupsToRemove = new ArrayList<String>(currentlyAssignedGroups.keySet());
    groupsToRemove.removeAll(finalCollectionOfAssignedGroups.keySet());

    ArrayList<String> groupsToAdd = new ArrayList<String>(finalCollectionOfAssignedGroups.keySet());
    groupsToAdd.removeAll(currentlyAssignedGroups.keySet());

    for (String groupId : groupsToRemove) {
      currentlyAssignedGroups.get(groupId).removeMember(jackrabbitUser);
    }

    for (String groupId : groupsToAdd) {
      finalCollectionOfAssignedGroups.get(groupId).addMember(jackrabbitUser);
    }
  }

  public IPentahoRole createRole(Session session, final ITenant theTenant, final String roleName,
      final String description, final String[] memberUserNames) throws AuthorizableExistsException, RepositoryException {
    ITenant tenant = theTenant;
    String role = roleName;
    if (tenant == null) {
      tenant = getTenant(roleName, false);
      role = getPrincipalName(roleName, false);
    }
    if (tenant == null || tenant.getId() == null) {
      tenant = getCurrentTenant();
    }
    if (!TenantUtils.isAccessibleTenant(tenant)) {
      throw new NotFoundException("Tenant " + theTenant.getId() + " not found");
    }
    String roleId = tenantedRoleNameUtils.getPrincipleId(tenant, role);

    UserManager tenantUserMgr = getUserManager(tenant, session);
    // Intermediate path will always be an empty string. The path is already provided while creating a user manager
    tenantUserMgr.createGroup(new PrincipalImpl(roleId), "");
    setRoleMembers(session, tenant, role, memberUserNames);
    setRoleDescription(session, tenant, role, description);
    return getRole(session, theTenant, roleName);
  }

  public IPentahoUser createUser(Session session, final ITenant theTenant, final String userName,
      final String password, final String description, final String[] roles) throws AuthorizableExistsException,
      RepositoryException {
    ITenant tenant = theTenant;
    String user = userName;
    if (tenant == null) {
      tenant = getTenant(userName, true);
      user = getPrincipalName(userName, true);
    }
    if (tenant == null || tenant.getId() == null) {
      tenant = getCurrentTenant();
    }
    if (!TenantUtils.isAccessibleTenant(tenant)) {
      throw new NotFoundException("Tenant " + theTenant.getId() + " not found");
    }
    String userId = tenantedUserNameUtils.getPrincipleId(tenant, user);
    UserManager tenantUserMgr = getUserManager(tenant, session);
    tenantUserMgr.createUser(userId, password, new PrincipalImpl(userId), "");
    setUserRoles(session, tenant, user, roles);
    setUserDescription(session, tenant, user, description);
    
    createUserHomeFolder(tenant, user);
    return getUser(session, tenant, userName);
  }

  public void deleteRole(Session session, final IPentahoRole role) throws NotFoundException, RepositoryException {
    Group jackrabbitGroup = getJackrabbitGroup(role.getTenant(), role.getName(), session);
    if (jackrabbitGroup != null && TenantUtils.isAccessibleTenant(tenantedRoleNameUtils.getTenant(jackrabbitGroup.getID()))) {
      jackrabbitGroup.remove();
    } else {
      throw new NotFoundException("");
    }
  }

  public void deleteUser(Session session, final IPentahoUser user) throws NotFoundException, RepositoryException {
    User jackrabbitUser = getJackrabbitUser(user.getTenant(), user.getUsername(), session);
    if (jackrabbitUser != null && TenantUtils.isAccessibleTenant(tenantedUserNameUtils.getTenant(jackrabbitUser.getID()))) {
      jackrabbitUser.remove();
    } else {
      throw new NotFoundException("");
    }
  }

  public List<IPentahoRole> getRoles(Session session) throws RepositoryException {
    return getRoles(session, getCurrentTenant());
  }

  private IPentahoUser convertToPentahoUser(User jackrabbitUser) throws RepositoryException {
    IPentahoUser pentahoUser = null;
    Value[] propertyValues = null;

    String description = null;
    try {
      propertyValues = jackrabbitUser.getProperty("description");
      description = propertyValues.length > 0 ? propertyValues[0].getString() : null;
    } catch (Exception ex) {
    }

    Credentials credentials = jackrabbitUser.getCredentials();
    String password = null;
    if (credentials instanceof CryptedSimpleCredentials) {
      password = new String(((CryptedSimpleCredentials) credentials).getPassword());
    }

    pentahoUser = new PentahoUser(tenantedUserNameUtils.getTenant(jackrabbitUser.getID()),
        tenantedUserNameUtils.getPrincipleName(jackrabbitUser.getID()), password, description,
        !jackrabbitUser.isDisabled());

    return pentahoUser;
  }

  private IPentahoRole convertToPentahoRole(Group jackrabbitGroup) throws RepositoryException {
    IPentahoRole role = null;
    Value[] propertyValues = null;

    String description = null;
    try {
      propertyValues = jackrabbitGroup.getProperty("description");
      description = propertyValues.length > 0 ? propertyValues[0].getString() : null;
    } catch (Exception ex) {
    }

    role = new PentahoRole(tenantedRoleNameUtils.getTenant(jackrabbitGroup.getID()),
        tenantedRoleNameUtils.getPrincipleName(jackrabbitGroup.getID()), description);
    return role;
  }

  public List<IPentahoUser> getUsers(Session session) throws RepositoryException {
    return getUsers(session, getCurrentTenant());
  }

  public void setRoleDescription(Session session, final ITenant theTenant, final String roleName,
      final String description) throws NotFoundException, RepositoryException {
    Group jackrabbitGroup = getJackrabbitGroup(theTenant, roleName, session);
    if (jackrabbitGroup != null
        && TenantUtils.isAccessibleTenant(theTenant == null ? tenantedRoleNameUtils.getTenant(jackrabbitGroup.getID()) : theTenant)) {
      if (description == null) {
        jackrabbitGroup.removeProperty("description");
      } else {
        jackrabbitGroup.setProperty("description", session.getValueFactory().createValue(description));
      }
    } else {
      throw new NotFoundException("Role not found");
    }
  }

  public void setUserDescription(Session session, final ITenant theTenant, final String userName,
      final String description) throws NotFoundException, RepositoryException {
    User jackrabbitUser = getJackrabbitUser(theTenant, userName, session);
    if ((jackrabbitUser == null)
        || !TenantUtils.isAccessibleTenant(theTenant == null ? tenantedUserNameUtils.getTenant(jackrabbitUser.getID()) : theTenant)) {
      throw new NotFoundException("User not found");
    }
    if (description == null) {
      jackrabbitUser.removeProperty("description");
    } else {
      jackrabbitUser.setProperty("description", session.getValueFactory().createValue(description));
    }
  }

  public void setPassword(Session session, final ITenant theTenant, final String userName, final String password)
      throws NotFoundException, RepositoryException {
    User jackrabbitUser = getJackrabbitUser(theTenant, userName, session);
    if ((jackrabbitUser == null)
        || !TenantUtils.isAccessibleTenant(theTenant == null ? tenantedUserNameUtils.getTenant(jackrabbitUser.getID()) : theTenant)) {
      throw new NotFoundException("User not found");
    }
    jackrabbitUser.changePassword(password);
  }

  public ITenantedPrincipleNameResolver getTenantedUserNameUtils() {
    return tenantedUserNameUtils;
  }

  public ITenantedPrincipleNameResolver getTenantedRoleNameUtils() {
    return tenantedRoleNameUtils;
  }

  public List<IPentahoRole> getRoles(Session session, ITenant tenant) throws RepositoryException, NamespaceException {
    return getRoles(session, tenant, false);
  }

  public List<IPentahoRole> getRoles(Session session, final ITenant theTenant, boolean includeSubtenants)
      throws RepositoryException {
    ArrayList<IPentahoRole> roles = new ArrayList<IPentahoRole>();
    if (TenantUtils.isAccessibleTenant(theTenant)) {
      UserManager userMgr = getUserManager(theTenant, session);
      pPrincipalName = ((SessionImpl) session).getJCRName(P_PRINCIPAL_NAME);
      Iterator<Authorizable> it = userMgr.findAuthorizables(pPrincipalName, null, UserManager.SEARCH_TYPE_GROUP);
      while (it.hasNext()) {
        Group group = (Group) it.next();
        IPentahoRole pentahoRole = convertToPentahoRole(group);
        if (includeSubtenants) {
          roles.add(pentahoRole);
        } else {
          if (pentahoRole.getTenant() != null && pentahoRole.getTenant().equals(theTenant)) {
            roles.add(pentahoRole);
          }
        }
      }
    }
    return roles;
  }

  public List<IPentahoUser> getUsers(Session session, ITenant tenant) throws RepositoryException {
    return getUsers(session, tenant, false);
  }

  public List<IPentahoUser> getUsers(Session session, final ITenant theTenant, boolean includeSubtenants)
      throws RepositoryException {
    ArrayList<IPentahoUser> users = new ArrayList<IPentahoUser>();
    if (TenantUtils.isAccessibleTenant(theTenant)) {
      UserManager userMgr = getUserManager(theTenant, session);
      pPrincipalName = ((SessionImpl) session).getJCRName(P_PRINCIPAL_NAME);
      Iterator<Authorizable> it = userMgr.findAuthorizables(pPrincipalName, null, UserManager.SEARCH_TYPE_USER);
      while (it.hasNext()) {
        User user = (User) it.next();
        IPentahoUser pentahoUser = convertToPentahoUser(user);
        if (includeSubtenants) {
          users.add(pentahoUser);
        } else {
          if (pentahoUser.getTenant() != null && pentahoUser.getTenant().equals(theTenant)) {
            users.add(pentahoUser);
          }
        }
      }
    }
    return users;
  }

  public IPentahoRole getRole(Session session, final ITenant tenant, final String name) throws RepositoryException {
    Group jackrabbitGroup = getJackrabbitGroup(tenant, name, session);
    return jackrabbitGroup != null
        && TenantUtils.isAccessibleTenant(tenant == null ? tenantedRoleNameUtils.getTenant(jackrabbitGroup.getID()) : tenant) ? convertToPentahoRole(jackrabbitGroup)
        : null;
  }

  private UserManagerImpl getUserManager(ITenant theTenant, Session session) throws RepositoryException {
    Properties tenantProperties = new Properties();
    tenantProperties.put(UserManagerImpl.PARAM_USERS_PATH, UserManagerImpl.USERS_PATH + theTenant.getRootFolderAbsolutePath());
    tenantProperties.put(UserManagerImpl.PARAM_GROUPS_PATH, UserManagerImpl.GROUPS_PATH + theTenant.getRootFolderAbsolutePath());
    return new UserManagerImpl((SessionImpl) session, session.getUserID(), tenantProperties);
  }

  public IPentahoUser getUser(Session session, final ITenant tenant, final String name) throws RepositoryException {
    User jackrabbitUser = getJackrabbitUser(tenant, name, session);
    return jackrabbitUser != null
        && TenantUtils.isAccessibleTenant(tenant == null ? tenantedUserNameUtils.getTenant(jackrabbitUser.getID()) : tenant) ? convertToPentahoUser(jackrabbitUser)
        : null;
  }

  private Group getJackrabbitGroup(ITenant theTenant, String name, Session session) throws RepositoryException {
    Group jackrabbitGroup = null;
    String roleId = name;
    String roleName = name;
    ITenant tenant = theTenant;

    if (tenant == null) {
      tenant = getTenant(roleName, false);
      roleName = getPrincipalName(roleName, false);
    }
    if (tenant == null || tenant.getId() == null) {
      tenant = getCurrentTenant();
    }
    if(tenant == null || tenant.getId() == null) {
      tenant = getDefaultTenant();
    }
    roleId = tenantedRoleNameUtils.getPrincipleId(tenant, roleName);

    UserManager userMgr = getUserManager(tenant, session);
    Authorizable authorizable = userMgr.getAuthorizable(roleId);
    if (authorizable instanceof Group) {
      jackrabbitGroup = (Group) authorizable;
    }
    return jackrabbitGroup;
  }

  private User getJackrabbitUser(ITenant theTenant, String name, Session session) throws RepositoryException {
    User jackrabbitUser = null;
    String userId = name;
    String userName = name;
    ITenant tenant = theTenant;
    if (tenant == null) {
      tenant = getTenant(userName, true);
      userName = getPrincipalName(userName, true);
    }
    if (tenant == null || tenant.getId() == null) {
      tenant = getCurrentTenant();
    }
    if(tenant == null || tenant.getId() == null) {
      tenant = getDefaultTenant();
    }
      
    if(tenant != null) {
      userId = tenantedUserNameUtils.getPrincipleId(tenant, userName);
  
      UserManager userMgr = getUserManager(tenant, session);
      Authorizable authorizable = userMgr.getAuthorizable(userId);
      if (authorizable instanceof User) {
        jackrabbitUser = (User) authorizable;
      }
    }
    return jackrabbitUser;
  }

  protected boolean tenantExists(String tenantName) {
    return tenantName != null && tenantName.trim().length() > 0;
  }

  protected ITenant getTenant(String principalId, boolean isUser) {
    ITenant tenant = null;
    ITenantedPrincipleNameResolver nameUtils = isUser ? tenantedUserNameUtils : tenantedRoleNameUtils;
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
    ITenantedPrincipleNameResolver nameUtils = isUser ? tenantedUserNameUtils : tenantedRoleNameUtils;
    if (nameUtils != null) {
      principalName = nameUtils.getPrincipleName(principalId);
    }
    return principalName;
  }

  public List<IPentahoUser> getRoleMembers(Session session, final ITenant theTenant, final String roleName)
      throws RepositoryException {
    List<IPentahoUser> users = new ArrayList<IPentahoUser>();
    Group jackrabbitGroup = getJackrabbitGroup(theTenant, roleName, session);
    if ((jackrabbitGroup != null)
        && TenantUtils.isAccessibleTenant(theTenant == null ? tenantedRoleNameUtils.getTenant(jackrabbitGroup.getID()) : theTenant)) {
      Iterator<Authorizable> authorizables = jackrabbitGroup.getMembers();
      while (authorizables.hasNext()) {
        Authorizable authorizable = authorizables.next();
        if (authorizable instanceof User) {
          users.add(convertToPentahoUser((User) authorizable));
        }
      }
    }
    return users;
  }

  public List<IPentahoRole> getUserRoles(Session session, final ITenant theTenant, final String userName)
      throws RepositoryException {
    ArrayList<IPentahoRole> roles = new ArrayList<IPentahoRole>();
    User jackrabbitUser = getJackrabbitUser(theTenant, userName, session);
    if ((jackrabbitUser != null)
        && TenantUtils.isAccessibleTenant(theTenant == null ? tenantedUserNameUtils.getTenant(jackrabbitUser.getID()) : theTenant)) {
      Iterator<Group> groups = jackrabbitUser.memberOf();
      while (groups.hasNext()) {
        roles.add(convertToPentahoRole(groups.next()));
      }
    }
    return roles;
  }

  protected ITenant getCurrentTenant() {
    if(PentahoSessionHolder.getSession() != null) {
      String tenantId = (String) PentahoSessionHolder.getSession().getAttribute(IPentahoSession.TENANT_ID_KEY);
      return tenantId != null ? new Tenant(tenantId, true) : null;
    } else return null;
  }

  protected ITenant getDefaultTenant() {
    return new Tenant(ServerRepositoryPaths.getPentahoRootFolderPath() + RepositoryFile.SEPARATOR + TenantUtils.TENANTID_SINGLE_TENANT, true);
  }
  
}