package org.pentaho.platform.engine.security.userroledao.jackrabbit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

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
import org.pentaho.platform.engine.core.system.TenantUtils;
import org.pentaho.platform.engine.security.userroledao.IPentahoRole;
import org.pentaho.platform.engine.security.userroledao.IPentahoUser;
import org.pentaho.platform.engine.security.userroledao.ITenantedPrincipleNameUtils;
import org.pentaho.platform.engine.security.userroledao.NotFoundException;
import org.pentaho.platform.engine.security.userroledao.PentahoRole;
import org.pentaho.platform.engine.security.userroledao.PentahoUser;

public class JackrabbitUserRoleService  {
  
  NameFactory NF = NameFactoryImpl.getInstance();
  Name P_PRINCIPAL_NAME = NF.create(Name.NS_REP_URI, "principalName"); //$NON-NLS-1$
  
//  private SessionImpl session;
  private ITenantedPrincipleNameUtils tenantedUserNameUtils;
  private ITenantedPrincipleNameUtils tenantedRoleNameUtils;
  String pPrincipalName = "rep:principalName";  
  String defaultTenant;
  HashMap<String, UserManagerImpl> userMgrMap = new HashMap<String, UserManagerImpl>();
  
  public JackrabbitUserRoleService() throws NamespaceException {
  }
  
//  public JackrabbitUserRoleDao(SessionImpl superUserSession) throws NamespaceException {
//    session = superUserSession;
//  }
  
  public void setRoleMembers(Session session, final String tenantName, final String roleName, final String[] memberUserNames) throws RepositoryException, NotFoundException {
    Group jackrabbitGroup = getJackrabbitGroup(tenantName, roleName, session);

    if (jackrabbitGroup == null) {
      throw new NotFoundException("Role not found");
    }
    
    
    HashMap<String, User> currentlyAssignedUsers = new HashMap<String, User>();
    Iterator<Authorizable> currentMembers = jackrabbitGroup.getMembers();
    while (currentMembers.hasNext()) {
      Authorizable member = currentMembers.next();
      if (member instanceof User) {
        currentlyAssignedUsers.put(member.getID(), (User)member);
      }
    }
    
    HashMap<String, User> finalCollectionOfAssignedUsers = new HashMap<String, User>();
    if (memberUserNames != null) {
      String tenant = tenantName == null ? getTenantName(roleName, false) : tenantName;
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
  
  public void setRoleMembers(Session session, String roleName, String[] memberUserNames) throws RepositoryException {
    setRoleMembers(session, null, roleName, memberUserNames);
  }
  
  public void setUserRoles(Session session, final String tenantName, final String userName, final String[] roles) throws RepositoryException, NotFoundException {
      User jackrabbitUser = getJackrabbitUser(tenantName, userName, session);

      if (jackrabbitUser == null) {
        throw new NotFoundException("User not found");
      }
      
      
      HashMap<String, Group> currentlyAssignedGroups = new HashMap<String, Group>();
      Iterator<Group> currentGroups = jackrabbitUser.memberOf();
      while (currentGroups.hasNext()) {
        Group currentGroup = currentGroups.next();
        currentlyAssignedGroups.put(currentGroup.getID(), currentGroup);
      }
      
      HashMap<String, Group> finalCollectionOfAssignedGroups = new HashMap<String, Group>();
      if (roles != null) {
        String tenant = tenantName == null ? getTenantName(userName, true) : tenantName;
        for (String role : roles) {
          Group jackrabbitGroup = getJackrabbitGroup(tenant, role, session);
          if (jackrabbitGroup != null) {
            finalCollectionOfAssignedGroups.put(tenantedRoleNameUtils.getPrincipleId(tenant, role), jackrabbitGroup);
          }
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
  
  public void setUserRoles(Session session, String userName, String[] roles) throws NotFoundException, RepositoryException {
    setUserRoles(session, null, userName, roles);
  }
  
  public IPentahoRole createRole(Session session, String roleName, String description, String[] memberUserNames) throws AuthorizableExistsException, RepositoryException {
    return createRole(session, null, roleName, description, memberUserNames);
  }
  
  public IPentahoRole createRole(Session session, final String tenantName, final String roleName, final String description, final String[] memberUserNames) throws AuthorizableExistsException, RepositoryException {
    String tenant = tenantName;
    String role = roleName;
    if (tenant == null) {
      tenant = getTenantName(roleName, false);
      role = getPrincipalName(roleName, false);
    }
    
    String roleId = tenantedRoleNameUtils.getPrincipleId(tenant, role);
    
    UserManager tenantUserMgr = getUserManager(tenant, session);
    tenantUserMgr.createGroup(new PrincipalImpl(roleId), tenant);
    setRoleMembers(session, tenant, role, memberUserNames);
    setRoleDescription(session, tenant, role, description);
    return getRole(session, tenantName, roleName);
  }
  
  public IPentahoUser createUser(Session session, final String tenantName, final String userName, final String password, final String description, final String[] roles) throws AuthorizableExistsException, RepositoryException {
    String tenant = tenantName;
    String user = userName;
    if (tenant == null) {
      tenant = getTenantName(userName, true);
      user = getPrincipalName(userName, true);
    }
    
    String userId = tenantedUserNameUtils.getPrincipleId(tenant, user);
    
    UserManager tenantUserMgr = getUserManager(tenant, session);
    tenantUserMgr.createUser(userId, password, new PrincipalImpl(userId), tenant);
    setUserRoles(session, tenant, user, roles);
    setUserDescription(session, tenant, user, description);
    return getUser(session, tenantName, userName);
  }

  public IPentahoUser createUser(Session session, String userName, String password, String description, String[] roles) throws AuthorizableExistsException, RepositoryException {
    return createUser(session, null, userName, password, description, roles);
  }

  public void deleteRole(Session session, final IPentahoRole role) throws NotFoundException, RepositoryException {
      Group jackrabbitGroup = getJackrabbitGroup(role.getTenant(), role.getName(), session);
      if (jackrabbitGroup != null) {
        jackrabbitGroup.remove();
      } else {
        throw new NotFoundException("");
      }
  }

  public void deleteUser(Session session, final IPentahoUser user) throws NotFoundException, RepositoryException {
      User jackrabbitUser = getJackrabbitUser(user.getTenant(), user.getUsername(), session);
      if (jackrabbitUser != null) {
        jackrabbitUser.remove();
      } else {
        throw new NotFoundException("");
      }
  }

  public List<IPentahoRole> getRoles(Session session) throws RepositoryException {
    return getRoles(session, getDefaultTenant());
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
      password  = new String(((CryptedSimpleCredentials)credentials).getPassword());
    }
    
    pentahoUser = new PentahoUser(tenantedUserNameUtils.getTenantName(jackrabbitUser.getID()), tenantedUserNameUtils.getPrincipleName(jackrabbitUser.getID()), password, description, !jackrabbitUser.isDisabled());
    
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
    
    role = new PentahoRole(tenantedRoleNameUtils.getTenantName(jackrabbitGroup.getID()), tenantedRoleNameUtils.getPrincipleName(jackrabbitGroup.getID()), description);
    return role;
  }
  

  public List<IPentahoUser> getUsers(Session session) throws RepositoryException {
    return getUsers(session, getDefaultTenant());
  }

  public void setRoleDescription(Session session, final String tenantName, final String roleName, final String description) throws NotFoundException, RepositoryException {
      Group jackrabbitGroup = getJackrabbitGroup(tenantName, roleName, session);
      if (jackrabbitGroup != null) {
        if (description == null) {
          jackrabbitGroup.removeProperty("description");
        } else {
          jackrabbitGroup.setProperty("description", session.getValueFactory().createValue(description));
        }
      } else {
        throw new NotFoundException("Role not found");
      }
  }
  
  public void setRoleDescription(Session session, String roleName, String description) throws NotFoundException, RepositoryException {
    setRoleDescription(session, null, roleName, description);
  }
  
  public void setUserDescription(Session session, final String tenantName, final String userName, final String description) throws NotFoundException, RepositoryException {
      User jackrabbitUser = getJackrabbitUser(tenantName, userName, session); 
      if (jackrabbitUser == null) {
        throw new NotFoundException("User not found");
      }
      if (description == null) {
        jackrabbitUser.removeProperty("description");
      } else {
        jackrabbitUser.setProperty("description", session.getValueFactory().createValue(description));
      }
  }
  
  public void setUserDescription(Session session, String userName, String description) throws NotFoundException, RepositoryException {
    setUserDescription(session, null, userName, description);
  }
  
  public void setPassword(Session session, final String tenantName, final String userName, final String password) throws NotFoundException, RepositoryException {
    User jackrabbitUser = getJackrabbitUser(tenantName, userName, session); 
    if (jackrabbitUser == null) {
      throw new NotFoundException("User not found");
    }
    jackrabbitUser.changePassword(password);
  }
    
  public void setPassword(Session session, String userName, String password) throws NotFoundException, RepositoryException {
    setPassword(session, null, userName, password);
  }
  
  public ITenantedPrincipleNameUtils getTenantedUserNameUtils() {
    return tenantedUserNameUtils;
  }

  public void setTenantedUserNameUtils(ITenantedPrincipleNameUtils tenantedUserNameParser) {
    this.tenantedUserNameUtils = tenantedUserNameParser;
  }

  public ITenantedPrincipleNameUtils getTenantedRoleNameUtils() {
    return tenantedRoleNameUtils;
  }

  public void setTenantedRoleNameUtils(ITenantedPrincipleNameUtils tenantedRoleNameParser) {
    this.tenantedRoleNameUtils = tenantedRoleNameParser;
  }

  public List<IPentahoRole> getRoles(Session session, String tenant) throws RepositoryException, NamespaceException {
    return getRoles(session, tenant, false);
  }

  public List<IPentahoRole> getRoles(Session session, final String tenant, boolean includeSubtenants) throws RepositoryException {
    ArrayList<IPentahoRole> roles = new ArrayList<IPentahoRole>();
    UserManager userMgr = getUserManager(tenant, session);
    pPrincipalName = ((SessionImpl)session).getJCRName(P_PRINCIPAL_NAME);  
    Iterator<Authorizable> it = userMgr.findAuthorizables(pPrincipalName, null, UserManager.SEARCH_TYPE_GROUP);
    while (it.hasNext()) {
      Group group = (Group)it.next();
      roles.add(convertToPentahoRole(group));
    }
    return roles;
  }
  
  public List<IPentahoUser> getUsers(Session session, String tenant) throws RepositoryException {
    return getUsers(session, tenant, false);
  }

  public List<IPentahoUser> getUsers(Session session, final String tenant, boolean includeSubtenants) throws RepositoryException  {
    ArrayList<IPentahoUser> users = new ArrayList<IPentahoUser>();
    UserManager userMgr = getUserManager(tenant, session);
    pPrincipalName = ((SessionImpl)session).getJCRName(P_PRINCIPAL_NAME);  
    Iterator<Authorizable> it = userMgr.findAuthorizables(pPrincipalName, null, UserManager.SEARCH_TYPE_USER);
    while (it.hasNext()) {
      User user = (User)it.next();
      users.add(convertToPentahoUser(user));
    }
    return users;
 }
  
  public IPentahoRole getRole(Session session, String name) throws RepositoryException {
    return getRole(session, null, name);
  }

  public IPentahoRole getRole(Session session, final String tenant, final String name) throws RepositoryException {
    Group jackrabbitGroup = getJackrabbitGroup(tenant, name, session);
    return jackrabbitGroup != null ? convertToPentahoRole(jackrabbitGroup) : null;
  }
  
  private UserManagerImpl getUserManager(String tenantName, Session session) throws RepositoryException {
    Properties tenantProperties = new Properties();
    tenantProperties.put(UserManagerImpl.PARAM_USERS_PATH, UserManagerImpl.USERS_PATH + "/" + tenantName);
    tenantProperties.put(UserManagerImpl.PARAM_GROUPS_PATH, UserManagerImpl.GROUPS_PATH + "/" + tenantName);      
    return new UserManagerImpl((SessionImpl)session, session.getUserID(), tenantProperties);
  }
  
  public IPentahoUser getUser(Session session, String name) throws RepositoryException {
    return getUser(session, null, name);
  }
  
  public IPentahoUser getUser(Session session, final String tenant, final String name) throws RepositoryException {
    User jackrabbitUser = getJackrabbitUser(tenant, name, session);
    return jackrabbitUser != null ? convertToPentahoUser(jackrabbitUser) : null;
  }
  
  private Group getJackrabbitGroup(String tenant, String name, Session session) throws RepositoryException {
    Group jackrabbitGroup = null;
    String roleId = name;
    String roleName = name;
    String tenantName = tenant;

    if (tenantName == null) {
      tenantName = getTenantName(roleName, false);
      roleName = getPrincipalName(roleName, false);
    }
    roleId = tenantedRoleNameUtils.getPrincipleId(tenantName, roleName);
      
    UserManager userMgr = getUserManager(tenantName, session);
    Authorizable authorizable = userMgr.getAuthorizable(roleId);
    if (authorizable instanceof Group) {
      jackrabbitGroup = (Group)authorizable;
    }
    return jackrabbitGroup;
  }
  
  private User getJackrabbitUser(String tenant, String name, Session session) throws RepositoryException {
    User jackrabbitUser = null;
    String userId = name;
    String userName = name;
    String tenantName = tenant;
    
    if (tenantName == null) {
      tenantName = getTenantName(userName, true);
      userName = getPrincipalName(userName, true);
    }
    
    userId = tenantedUserNameUtils.getPrincipleId(tenantName, userName);
      
    UserManager userMgr = getUserManager(tenantName, session);
    Authorizable authorizable = userMgr.getAuthorizable(userId);
    if (authorizable instanceof User) {
      jackrabbitUser = (User)authorizable;
    }
    return jackrabbitUser;  
  }

  protected boolean tenantExists(String tenantName) {
    return tenantName != null && tenantName.trim().length() > 0;
  }
  
  private String getTenantName(String principalId, boolean isUser) {
    String tenantName = null;
    ITenantedPrincipleNameUtils nameUtils = isUser ? tenantedUserNameUtils : tenantedRoleNameUtils;
    if (nameUtils != null) {
      tenantName = tenantedUserNameUtils.getTenantName(principalId);
    }
    if (tenantName == null) {
      tenantName = getDefaultTenant();
    }
    return tenantName;    
  }

  private String getPrincipalName(String principalId, boolean isUser) {
    String principalName = null;
    ITenantedPrincipleNameUtils nameUtils = isUser ? tenantedUserNameUtils : tenantedRoleNameUtils;
    if (nameUtils != null) {
      principalName = tenantedUserNameUtils.getPrincipleName(principalId);
    }
    return principalName;    
  }
  
  

  public List<IPentahoUser> getRoleMembers(Session session, final String tenantName, final String roleName) throws RepositoryException {
    List<IPentahoUser> users = new ArrayList<IPentahoUser>();
    Group jackrabbitGroup = getJackrabbitGroup(tenantName, roleName, session);
    if (jackrabbitGroup != null) {
      Iterator<Authorizable> authorizables = jackrabbitGroup.getMembers();
      while (authorizables.hasNext()) {
        Authorizable authorizable = authorizables.next();
        if (authorizable instanceof User) {
          users.add(convertToPentahoUser((User)authorizable));
        }
      }
    }
    return users;
  }

  public List<IPentahoUser> getRoleMembers(Session session, String roleName) throws RepositoryException {
    return getRoleMembers(session, null, roleName);
  }

  public List<IPentahoRole> getUserRoles(Session session, final String tenantName, final String userName) throws RepositoryException {
    ArrayList<IPentahoRole> roles = new ArrayList<IPentahoRole>();
    User jackrabbitUser = getJackrabbitUser(tenantName, userName, session);
    if (jackrabbitUser != null) {
      Iterator<Group> groups = jackrabbitUser.memberOf();
      while (groups.hasNext()) {
        roles.add(convertToPentahoRole(groups.next()));
      }
    }
    return roles;
  }

  public List<IPentahoRole> getUserRoles(Session session, String userName) throws RepositoryException {
    return getUserRoles(session, null, userName);
  }
  
  public String getDefaultTenant() {
    return TenantUtils.getTenantId();
  }
}

