/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright 2010 Pentaho Corporation.  All rights reserved.
 */
package org.pentaho.platform.engine.security.userroledao.ws;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jws.WebService;

import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.engine.security.userroledao.IPentahoRole;
import org.pentaho.platform.engine.security.userroledao.IPentahoUser;
import org.pentaho.platform.engine.security.userroledao.IUserRoleDao;
import org.pentaho.platform.engine.security.userroledao.PentahoRole;
import org.pentaho.platform.engine.security.userroledao.messages.Messages;

/**
 * This class implements a concrete form of IUserRoleDao, wrapping the underlying IUserRoleDao implementation. 
 *
 * @author Will Gorman (wgorman@pentaho.com)
 */
@WebService(endpointInterface = "org.pentaho.platform.engine.security.userroledao.ws.IUserRoleWebService", serviceName = "userRoleService", portName = "userRoleServicePort", targetNamespace = "http://www.pentaho.org/ws/1.0")
public class UserRoleWebService implements IUserRoleWebService {

  public UserRoleWebService() {}

  protected boolean isAdmin() {
    return SecurityHelper.getInstance().isPentahoAdministrator(PentahoSessionHolder.getSession());
  }
  
  protected IUserRoleDao getDao() throws UserRoleException {
    if (!isAdmin()) {
      throw new UserRoleException(Messages.getInstance().getErrorString("UserRoleWebService.ERROR_0001_NOT_ADMIN")); //$NON-NLS-1$
    }
    IUserRoleDao dao = PentahoSystem.get(IUserRoleDao.class, "txnUserRoleDao", PentahoSessionHolder.getSession()); //$NON-NLS-1$
    if (dao == null) {
      throw new UserRoleException(Messages.getInstance().getErrorString("UserRoleWebService.ERROR_0002_IUSERROLEDAO_NOT_AVAILABLE")); //$NON-NLS-1$
    }
    return dao;
  }
  
  public UserRoleSecurityInfo getUserRoleSecurityInfo() throws UserRoleException {
    UserRoleSecurityInfo userRoleSecurityInfo = new UserRoleSecurityInfo();
    List<IPentahoUser> users = getDao().getUsers();
    if (users != null) {
      for (IPentahoUser user : users) {
        userRoleSecurityInfo.getUsers().add(ProxyPentahoUserRoleHelper.toProxyUser(user));
        Set<IPentahoRole> roles = user.getRoles();
        if (roles != null) {
          for (IPentahoRole role : roles) {
            userRoleSecurityInfo.getAssignments().add(new UserToRoleAssignment(user.getUsername(), role.getName()));
          }
        }
      }
    }
    userRoleSecurityInfo.getRoles().addAll(Arrays.asList(getRoles()));
    return userRoleSecurityInfo;
  }

  // ~ User/Role Methods ===============================================================================================
  
  public boolean createUser(ProxyPentahoUser proxyUser) throws UserRoleException {
    IPentahoUser user = ProxyPentahoUserRoleHelper.syncUsers(null, proxyUser);
    getDao().createUser(user);
    return true;
  }

  public boolean deleteUsers(ProxyPentahoUser[] users) throws UserRoleException {
    IPentahoUser[] persistedUsers = new IPentahoUser[users.length];
    for (int i = 0; i < users.length; i++) {
      persistedUsers[i] = getDao().getUser(users[i].getName());
      if (persistedUsers[i] == null) {
        throw new UserRoleException(Messages.getInstance().getErrorString("UserRoleWebService.ERROR_0003_USER_DELETION_FAILED_NO_USER", users[i].getName())); //$NON-NLS-1$
      }
    }
    for (int i = 0; i < persistedUsers.length; i++) {
      getDao().deleteUser(persistedUsers[i]);
    }
    return true;
  }

  public ProxyPentahoUser getUser(String pUserName) throws UserRoleException {
    ProxyPentahoUser proxyPentahoUser = null;
    IPentahoUser user = getDao().getUser(pUserName);
    if (user != null) {
      proxyPentahoUser = ProxyPentahoUserRoleHelper.toProxyUser(user);
    }
    return proxyPentahoUser;
  }

  public ProxyPentahoUser[] getUsers() throws UserRoleException {
    List<IPentahoUser> users = getDao().getUsers();
    if (users != null) {
      ProxyPentahoUser[] proxyUsers = new ProxyPentahoUser[users.size()];
      int i = 0;
  
      for (IPentahoUser user : users) {
        proxyUsers[i++] = ProxyPentahoUserRoleHelper.toProxyUser(user);
      }
      return proxyUsers;
    }
    return null;
  }

  public ProxyPentahoUser[] getUsersForRole(ProxyPentahoRole proxyRole) throws UserRoleException {
    ArrayList<ProxyPentahoUser> users = new ArrayList<ProxyPentahoUser>();
    IPentahoRole role = getDao().getRole(proxyRole.getName());
    if (role != null && role.getUsers() != null) {
      for (IPentahoUser user : role.getUsers()) {
        users.add(ProxyPentahoUserRoleHelper.toProxyUser(user));
      }
    } else {
      throw new UserRoleException(Messages.getInstance().getErrorString("UserRoleWebService.ERROR_0005_FAILED_TO_FIND_ROLE", proxyRole.getName())); //$NON-NLS-1$
    }
    return users.toArray(new ProxyPentahoUser[0]);
  }

  public boolean updateUser(ProxyPentahoUser proxyUser) throws UserRoleException {
    IPentahoUser user = getDao().getUser(proxyUser.getName());
    if (user == null) {
      throw new UserRoleException(Messages.getInstance().getErrorString("UserRoleWebService.ERROR_0004_FAILED_TO_FIND_USER", proxyUser.getName())); //$NON-NLS-1$
    }
    getDao().updateUser(ProxyPentahoUserRoleHelper.syncUsers(user, proxyUser));
    return true;
  }
  
  public void setRoles(ProxyPentahoUser proxyUser, ProxyPentahoRole[] assignedRoles) throws UserRoleException {
    IPentahoUser user = getDao().getUser( proxyUser.getName() );
    if (user == null) {
      throw new UserRoleException(Messages.getInstance().getErrorString("UserRoleWebService.ERROR_0004_FAILED_TO_FIND_USER", proxyUser.getName())); //$NON-NLS-1$
    }
    Set<IPentahoRole> rolesToSet = new HashSet<IPentahoRole>();
    for (ProxyPentahoRole proxyRole : assignedRoles) {
      rolesToSet.add(ProxyPentahoUserRoleHelper.syncRoles(null, proxyRole));
    }
    user.setRoles(rolesToSet);
    getDao().updateUser(user);
  }
  
  public void setUsers( ProxyPentahoRole proxyRole, ProxyPentahoUser[] assignedUsers ) throws UserRoleException {
    IPentahoRole role = getDao().getRole(proxyRole.getName());
    if (role == null) {
      throw new UserRoleException(Messages.getInstance().getErrorString("UserRoleWebService.ERROR_0005_FAILED_TO_FIND_ROLE", proxyRole.getName() )); //$NON-NLS-1$
    }
    Set<IPentahoUser> usersToSet = new HashSet<IPentahoUser>();
    for (ProxyPentahoUser proxyUser : assignedUsers) {
      usersToSet.add(ProxyPentahoUserRoleHelper.syncUsers(null, proxyUser));
    }
    role.setUsers(usersToSet);
    getDao().updateRole(role);
  }

  public void updateRole(String roleName, String description, List<String> usernames) throws UserRoleException {
    IPentahoRole role = getDao().getRole(roleName);
    if (role == null) {
      throw new UserRoleException(Messages.getInstance().getErrorString("UserRoleWebService.ERROR_0006_ROLE_UPDATE_FAILED", roleName)); //$NON-NLS-1$
    }
    Set<IPentahoUser> users = new HashSet<IPentahoUser>();
    for (String username : usernames) {
      IPentahoUser user = getDao().getUser(username);
      if (user == null) {
        throw new UserRoleException(Messages.getInstance().getErrorString("UserRoleWebService.ERROR_0006_ROLE_UPDATE_FAILED", roleName)); //$NON-NLS-1$
      }
      users.add(user);
    }
    role.setDescription(description);
    role.setUsers(users);
    getDao().updateRole(role);
  }

  public boolean createRole(ProxyPentahoRole proxyRole) throws UserRoleException {
    IPentahoRole role = new PentahoRole(proxyRole.getName());
    getDao().createRole(ProxyPentahoUserRoleHelper.syncRoles(role, proxyRole));
    return false;
  }

  public boolean deleteRoles(ProxyPentahoRole[] roles) throws UserRoleException {
    IPentahoRole[] persistedRoles;
    persistedRoles = new IPentahoRole[roles.length];
    for (int i = 0; i < roles.length; i++) {
      persistedRoles[i] = getDao().getRole(roles[i].getName());
      if (persistedRoles[i] == null) {
        throw new UserRoleException(
            Messages.getInstance().getErrorString("UserRoleWebService.ERROR_0007_ROLE_DELETION_FAILED_NO_ROLE", roles[i].getName() ) ); //$NON-NLS-1$
      }
    }
    for (int i = 0; i < persistedRoles.length; i++) {
      getDao().deleteRole( persistedRoles[i] );
    }
    return true;
  }

  public ProxyPentahoRole[] getRolesForUser(ProxyPentahoUser proxyUser) throws UserRoleException {
    List<ProxyPentahoRole> proxyRoles = new ArrayList<ProxyPentahoRole>();
    IPentahoUser user = getDao().getUser( proxyUser.getName());
    if (user != null && user.getRoles() != null) {
      for (IPentahoRole role : user.getRoles()) {
        proxyRoles.add(ProxyPentahoUserRoleHelper.toProxyRole(role));
      }
    } else {
      throw new UserRoleException(Messages.getInstance().getErrorString("UserRoleWebService.ERROR_0004_FAILED_TO_FIND_USER", proxyUser.getName() )); //$NON-NLS-1$
    }
    return proxyRoles.toArray(new ProxyPentahoRole[0]);
  }
  
  public ProxyPentahoRole[] getRoles() throws UserRoleException {
    List<ProxyPentahoRole> proxyRoles = new ArrayList<ProxyPentahoRole>();
    List<IPentahoRole> roles = getDao().getRoles();
    if (roles != null) {
      for (IPentahoRole role : roles) {
        proxyRoles.add(ProxyPentahoUserRoleHelper.toProxyRole(role));
      }
    }
    return proxyRoles.toArray(new ProxyPentahoRole[0]);
  }

  public boolean updateRoleObject(ProxyPentahoRole proxyPentahoRole) throws UserRoleException {
    IPentahoRole role = getDao().getRole(proxyPentahoRole.getName());
    if (role == null) {
      throw new UserRoleException(
          Messages.getInstance().getErrorString("UserRoleWebService.ERROR_0008_ROLE_UPDATE_FAILED_DOES_NOT_EXIST", proxyPentahoRole.getName()) ); //$NON-NLS-1$
    }
    getDao().updateRole(ProxyPentahoUserRoleHelper.syncRoles(role, proxyPentahoRole));
    return true;
  }

}
