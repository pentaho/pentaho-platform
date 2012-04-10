package org.pentaho.platform.engine.security.userroledao.jackrabbit;

import java.io.IOException;
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
import org.pentaho.platform.engine.security.userroledao.AlreadyExistsException;
import org.pentaho.platform.engine.security.userroledao.IPentahoRole;
import org.pentaho.platform.engine.security.userroledao.IPentahoUser;
import org.pentaho.platform.engine.security.userroledao.ITenantedPrincipleNameUtils;
import org.pentaho.platform.engine.security.userroledao.IUserRoleDao;
import org.pentaho.platform.engine.security.userroledao.NotFoundException;
import org.pentaho.platform.engine.security.userroledao.PentahoRole;
import org.pentaho.platform.engine.security.userroledao.PentahoUser;
import org.pentaho.platform.engine.security.userroledao.UncategorizedUserRoleDaoException;
import org.springframework.dao.DataAccessException;
import org.springframework.extensions.jcr.JcrCallback;
import org.springframework.extensions.jcr.JcrSystemException;
import org.springframework.extensions.jcr.JcrTemplate;

public class JackrabbitUserRoleDao {
  
  NameFactory NF = NameFactoryImpl.getInstance();
  Name P_PRINCIPAL_NAME = NF.create(Name.NS_REP_URI, "principalName"); //$NON-NLS-1$
  
//  private SessionImpl session;
  private ITenantedPrincipleNameUtils tenantedUserNameUtils;
  private ITenantedPrincipleNameUtils tenantedRoleNameUtils;
  String pPrincipalName = "rep:principalName";  
  String defaultTenant;
  JcrTemplate jcrTemplate;
  HashMap<String, UserManagerImpl> userMgrMap = new HashMap<String, UserManagerImpl>();
  
  public JackrabbitUserRoleDao(JcrTemplate jcrTemplate) throws NamespaceException {
    this.jcrTemplate = jcrTemplate;
  }
  
//  public JackrabbitUserRoleDao(SessionImpl superUserSession) throws NamespaceException {
//    session = superUserSession;
//  }
  
  public void setRoleMembers(final String tenantName, final String roleName, final String[] memberUserNames) {
    try {
      jcrTemplate.execute(new JcrCallback() {      
        public Object doInJcr(Session session) throws IOException, RepositoryException {
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
          return null;
        }
      });
    } catch (DataAccessException e) {
      if ((e instanceof JcrSystemException) && (e.getCause() instanceof NotFoundException)) {
        throw (NotFoundException)e.getCause();
      }
      throw new UncategorizedUserRoleDaoException("Error updating role.", e);
    } 
  }
  
  public void setRoleMembers(String roleName, String[] memberUserNames) {
    setRoleMembers(null, roleName, memberUserNames);
  }
  
  public void setUserRoles(final String tenantName, final String userName, final String[] roles) throws NotFoundException, UncategorizedUserRoleDaoException {
    try {
      jcrTemplate.execute(new JcrCallback() {      
        public Object doInJcr(Session session) throws IOException, RepositoryException {
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
          return null;
        }
      });
    } catch (DataAccessException e) {
      if ((e instanceof JcrSystemException) && (e.getCause() instanceof NotFoundException)) {
        throw (NotFoundException)e.getCause();
      }
      throw new UncategorizedUserRoleDaoException("Error updating role.", e);
    }
  }
  
  public void setUserRoles(String userName, String[] roles) throws NotFoundException, UncategorizedUserRoleDaoException {
    setUserRoles(null, userName, roles);
  }
  
  public IPentahoRole createRole(String roleName, String description, String[] memberUserNames) throws AlreadyExistsException, UncategorizedUserRoleDaoException {
    return createRole(null, roleName, description, memberUserNames);
  }
  
  public IPentahoRole createRole(final String tenantName, final String roleName, final String description, final String[] memberUserNames) throws AlreadyExistsException, UncategorizedUserRoleDaoException {
    try {
      jcrTemplate.execute(new JcrCallback() {      
        public Object doInJcr(Session session) throws IOException, RepositoryException {
          String tenant = tenantName;
          String role = roleName;
          if (tenant == null) {
            tenant = getTenantName(roleName, false);
            role = getPrincipalName(roleName, false);
          }
          
          String roleId = tenantedRoleNameUtils.getPrincipleId(tenant, role);
          
          UserManager tenantUserMgr = getUserManager(tenant, session);
          tenantUserMgr.createGroup(new PrincipalImpl(roleId), tenant);
          setRoleMembers(tenant, role, memberUserNames);
          setRoleDescription(tenant, role, description);
          return null;
        }
      });
    } catch (DataAccessException e) {
      if ((e instanceof JcrSystemException) && (e.getCause() instanceof AuthorizableExistsException)) {
        throw new AlreadyExistsException("");
      }
      throw new UncategorizedUserRoleDaoException("Error creating role.", e);
    }
    return getRole(tenantName, roleName);
  }
  
  public IPentahoUser createUser(final String tenantName, final String userName, final String password, final String description, final String[] roles) throws AlreadyExistsException, UncategorizedUserRoleDaoException {
    try {
      jcrTemplate.execute(new JcrCallback() {      
        public Object doInJcr(Session session) throws IOException, RepositoryException {
          String tenant = tenantName;
          String user = userName;
          if (tenant == null) {
            tenant = getTenantName(userName, true);
            user = getPrincipalName(userName, true);
          }
          
          String userId = tenantedUserNameUtils.getPrincipleId(tenant, user);
          
          UserManager tenantUserMgr = getUserManager(tenant, session);
          tenantUserMgr.createUser(userId, password, new PrincipalImpl(userId), tenant);
          setUserRoles(tenant, user, roles);
          setUserDescription(tenant, user, description);
          return null;
        }
      });
    } catch (DataAccessException e) {
      if ((e instanceof JcrSystemException) && (e.getCause() instanceof AuthorizableExistsException)) {
        throw new AlreadyExistsException("");
      }
      throw new UncategorizedUserRoleDaoException("Error creating user.", e);
    }
    return getUser(tenantName, userName);
  }

  public IPentahoUser createUser(String userName, String password, String description, String[] roles) throws AlreadyExistsException, UncategorizedUserRoleDaoException {
    return createUser(null, userName, password, description, roles);
  }

  public void deleteRole(final IPentahoRole role) throws NotFoundException, UncategorizedUserRoleDaoException {
    try {
      jcrTemplate.execute(new JcrCallback() {      
        public Object doInJcr(Session session) throws IOException, RepositoryException {
          Group jackrabbitGroup = getJackrabbitGroup(role.getTenant(), role.getName(), session);
          if (jackrabbitGroup != null) {
            jackrabbitGroup.remove();
          } else {
            throw new NotFoundException("");
          }
          return null;
        }
      });
    } catch (DataAccessException e) {
      if ((e instanceof JcrSystemException) && (e.getCause() instanceof NotFoundException)) {
        throw (NotFoundException)e.getCause();
      }
      throw new UncategorizedUserRoleDaoException("Error deleting role. " + role.getName(), e);
    }
  }

  public void deleteUser(final IPentahoUser user) throws NotFoundException, UncategorizedUserRoleDaoException {
    try {
      jcrTemplate.execute(new JcrCallback() {      
        public Object doInJcr(Session session) throws IOException, RepositoryException {
          User jackrabbitUser = getJackrabbitUser(user.getTenant(), user.getUsername(), session);
          if (jackrabbitUser != null) {
            jackrabbitUser.remove();
          } else {
            throw new NotFoundException("");
          }
          return null;
        }
      });
    } catch (DataAccessException e) {
      if ((e instanceof JcrSystemException) && (e.getCause() instanceof NotFoundException)) {
        throw (NotFoundException)e.getCause();
      }
      throw new UncategorizedUserRoleDaoException("Error deleting user. " + user.getUsername(), e);
    }
  }

  public List<IPentahoRole> getRoles() throws UncategorizedUserRoleDaoException {
    throw new UnsupportedOperationException();
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
  

  public List<IPentahoUser> getUsers() throws UncategorizedUserRoleDaoException {
    return getUsers(getDefaultTenant());
  }

  public void setRoleDescription(final String tenantName, final String roleName, final String description) throws NotFoundException, UncategorizedUserRoleDaoException {
    try {
      jcrTemplate.execute(new JcrCallback() {      
        public Object doInJcr(Session session) throws IOException, RepositoryException {
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
          return null;
        }
      });
    } catch (DataAccessException e) {
      if ((e instanceof JcrSystemException) && (e.getCause() instanceof NotFoundException)) {
        throw (NotFoundException)e.getCause();
      }
      throw new UncategorizedUserRoleDaoException("Error updating role.", e);
    }
  }
  
  public void setRoleDescription(String roleName, String description) throws NotFoundException, UncategorizedUserRoleDaoException {
    setRoleDescription(null, roleName, description);
  }
  
  public void setUserDescription(final String tenantName, final String userName, final String description) throws NotFoundException, UncategorizedUserRoleDaoException {
    try {
      jcrTemplate.execute(new JcrCallback() {      
        public Object doInJcr(Session session) throws IOException, RepositoryException {
          User jackrabbitUser = getJackrabbitUser(tenantName, userName, session); 
          if (jackrabbitUser == null) {
            throw new NotFoundException("User not found");
          }
          if (description == null) {
            jackrabbitUser.removeProperty("description");
          } else {
            jackrabbitUser.setProperty("description", session.getValueFactory().createValue(description));
          }
          return null;
        }
      });
    } catch (DataAccessException e) {
      if ((e instanceof JcrSystemException) && (e.getCause() instanceof NotFoundException)) {
        throw (NotFoundException)e.getCause();
      }
      throw new UncategorizedUserRoleDaoException("Error updating user.", e);
    }
  }
  
  public void setUserDescription(String userName, String description) throws NotFoundException, UncategorizedUserRoleDaoException {
    setUserDescription(null, userName, description);
  }
  
  public void setPassword(final String tenantName, final String userName, final String password) throws NotFoundException, UncategorizedUserRoleDaoException {
    try {
      jcrTemplate.execute(new JcrCallback() {      
        public Object doInJcr(Session session) throws IOException, RepositoryException {
          User jackrabbitUser = getJackrabbitUser(tenantName, userName, session); 
          if (jackrabbitUser == null) {
            throw new NotFoundException("User not found");
          }
          jackrabbitUser.changePassword(password);
          return null;
        }
      });
    } catch (DataAccessException e) {
      if ((e instanceof JcrSystemException) && (e.getCause() instanceof NotFoundException)) {
        throw (NotFoundException)e.getCause();
      }
      throw new UncategorizedUserRoleDaoException("Error setting user password.", e);
    }
  }
    
  public void setPassword(String userName, String password) throws NotFoundException, UncategorizedUserRoleDaoException {
    setPassword(null, userName, password);
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

  public List<IPentahoRole> getRoles(String tenant) throws UncategorizedUserRoleDaoException {
    return getRoles(tenant, false);
  }

  public List<IPentahoRole> getRoles(final String tenant, boolean includeSubtenants) throws UncategorizedUserRoleDaoException {
    try {
      return (List<IPentahoRole>)jcrTemplate.execute(new JcrCallback() {      
        public Object doInJcr(Session session) throws IOException, RepositoryException {
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
      });
    } catch (DataAccessException e) {
      throw new UncategorizedUserRoleDaoException("Error listing roles.", e);
    }   
  }
  
  public List<IPentahoUser> getUsers(String tenant) throws UncategorizedUserRoleDaoException {
    return getUsers(tenant, false);
  }

  public List<IPentahoUser> getUsers(final String tenant, boolean includeSubtenants) throws UncategorizedUserRoleDaoException {
    try {
      return (List<IPentahoUser>)jcrTemplate.execute(new JcrCallback() {      
        public Object doInJcr(Session session) throws IOException, RepositoryException {
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
      });
    } catch (DataAccessException e) {
      throw new UncategorizedUserRoleDaoException("Error listing users.", e);
    }
  }
  
  public IPentahoRole getRole(String name) throws UncategorizedUserRoleDaoException {
    return getRole(null, name);
  }

  public IPentahoRole getRole(final String tenant, final String name) throws UncategorizedUserRoleDaoException {
    try {
      return (IPentahoRole)jcrTemplate.execute(new JcrCallback() {      
        public Object doInJcr(Session session) throws IOException, RepositoryException {
          Group jackrabbitGroup = getJackrabbitGroup(tenant, name, session);
          return jackrabbitGroup != null ? convertToPentahoRole(jackrabbitGroup) : null;
        }
      });
    } catch (DataAccessException e) {
      throw new UncategorizedUserRoleDaoException("Error getting role.", e);
    }
  }
  
  private UserManagerImpl getUserManager(String tenantName, Session session) throws RepositoryException {
    Properties tenantProperties = new Properties();
    tenantProperties.put(UserManagerImpl.PARAM_USERS_PATH, UserManagerImpl.USERS_PATH + "/" + tenantName);
    tenantProperties.put(UserManagerImpl.PARAM_GROUPS_PATH, UserManagerImpl.GROUPS_PATH + "/" + tenantName);      
    return new UserManagerImpl((SessionImpl)session, session.getUserID(), tenantProperties);
  }
  
  public IPentahoUser getUser(String name) throws UncategorizedUserRoleDaoException {
    return getUser(null, name);
  }
  
  public IPentahoUser getUser(final String tenant, final String name) throws UncategorizedUserRoleDaoException {
    try {
      return (IPentahoUser)jcrTemplate.execute(new JcrCallback() {      
        public Object doInJcr(Session session) throws IOException, RepositoryException {
          User jackrabbitUser = getJackrabbitUser(tenant, name, session);
          return jackrabbitUser != null ? convertToPentahoUser(jackrabbitUser) : null;
        }
      });
    } catch (DataAccessException e) {
      throw new UncategorizedUserRoleDaoException("Error getting role.", e);
    }
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
  
  

  public List<IPentahoUser> getRoleMembers(final String tenantName, final String roleName) throws UncategorizedUserRoleDaoException {
    try {
      return (List<IPentahoUser>) jcrTemplate.execute(new JcrCallback() {      
        public Object doInJcr(Session session) throws IOException, RepositoryException {
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
      });
    } catch (DataAccessException e) {
      throw new UncategorizedUserRoleDaoException("Error listing role members.", e);
    }
  }

  public List<IPentahoUser> getRoleMembers(String roleName) throws UncategorizedUserRoleDaoException {
    return getRoleMembers(null, roleName);
  }

  public List<IPentahoRole> getUserRoles(final String tenantName, final String userName) throws UncategorizedUserRoleDaoException {
    try {
      return (List<IPentahoRole>) jcrTemplate.execute(new JcrCallback() {      
        public Object doInJcr(Session session) throws IOException, RepositoryException {
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
      });
    } catch (DataAccessException e) {
      throw new UncategorizedUserRoleDaoException("Error listing role members.", e);
    }
  }

  public List<IPentahoRole> getUserRoles(String userName) throws UncategorizedUserRoleDaoException {
    return getUserRoles(null, userName);
  }
  
  public String getDefaultTenant() {
    return TenantUtils.getTenantId();
  }
}

