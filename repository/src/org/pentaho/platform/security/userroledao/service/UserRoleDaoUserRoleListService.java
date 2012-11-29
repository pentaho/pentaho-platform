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
 * Copyright 2007 - 2009 Pentaho Corporation.  All rights reserved.
 *
*/
package org.pentaho.platform.security.userroledao.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoRole;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoUser;
import org.pentaho.platform.api.engine.security.userroledao.IUserRoleDao;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.springframework.dao.DataAccessException;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.security.userdetails.UsernameNotFoundException;

/**
 * An {@link IUserRoleListService} that delegates to an {@link IUserRoleDao}.
 * 
 * @author mlowery
 */
public class UserRoleDaoUserRoleListService implements IUserRoleListService {

  // ~ Static fields/initializers ====================================================================================== 

  // ~ Instance fields =================================================================================================

  private IUserRoleDao userRoleDao;

  private UserDetailsService userDetailsService;
  
  private ITenantedPrincipleNameResolver tenantedRoleNameUtils;
  
  private ITenantedPrincipleNameResolver tenantedUserNameUtils;

  // ~ Constructors ====================================================================================================
  public UserRoleDaoUserRoleListService() {
    super();
  }

  public UserRoleDaoUserRoleListService(IUserRoleDao userRoleDao, UserDetailsService userDetailsService) {
    super();
    this.userRoleDao = userRoleDao;
    this.userDetailsService = userDetailsService;
  }
  public UserRoleDaoUserRoleListService(ITenantedPrincipleNameResolver tenantedUserNameUtils, ITenantedPrincipleNameResolver tenantedRoleNameUtils, IUserRoleDao userRoleDao, UserDetailsService userDetailsService) {
    super();
    this.tenantedUserNameUtils = tenantedUserNameUtils;
    this.tenantedRoleNameUtils = tenantedRoleNameUtils;
    this.userRoleDao = userRoleDao;
    this.userDetailsService = userDetailsService;
  }
  
  // ~ Methods =========================================================================================================

  private List<String> getAllRoles(List<IPentahoRole> roles) {
    List<String> auths = new ArrayList<String>(roles.size());

    for (IPentahoRole role : roles) {
      auths.add(role.getName());
    }

    return auths;
  }
  
  @Override
  public List<String> getAllRoles() {
    return getAllRoles(userRoleDao.getRoles());
  }

  @Override
  public List<String> getAllRoles(ITenant tenant) {
    return getAllRoles(userRoleDao.getRoles(tenant));
  }
  
  private List<String> getAllUsers(List<IPentahoUser> users) {
    List<String> usernames = new ArrayList<String>();

    for (IPentahoUser user : users) {
      usernames.add(user.getUsername());
    }

    return usernames;
  }
  
  @Override
  public List<String> getAllUsers() {
    return getAllUsers(userRoleDao.getUsers());
  }

  @Override
  public List<String> getAllUsers(ITenant tenant) {
    return getAllUsers(userRoleDao.getUsers(tenant));
  }
  
  @Override
  public List<String> getRolesForUser(ITenant tenant, String username) throws UsernameNotFoundException,
      DataAccessException {
    UserDetails user = userDetailsService.loadUserByUsername(tenantedUserNameUtils.getPrincipleId(tenant, username));
    List<String> roles = new ArrayList<String>(user.getAuthorities().length);
    for (GrantedAuthority role : user.getAuthorities()) {
      String principalName = role.getAuthority(); 
      if(tenantedRoleNameUtils != null) {
        principalName = tenantedRoleNameUtils.getPrincipleName(principalName);
      }
      roles.add(principalName);
    }
    return roles;
  }
  public List<String> getUsersInRole(ITenant tenant, IPentahoRole role, String roleName) {
    if (role == null) {
      return Collections.emptyList();
    }
    List<IPentahoUser> users = null;
    List<String> usernames = new ArrayList<String>();
    if(tenant == null) {
      users = userRoleDao.getRoleMembers(null, roleName);
    } else {
      users = userRoleDao.getRoleMembers(tenant, roleName);
    }
    
    for (IPentahoUser user : users) {
      usernames.add(user.getUsername());
    }

    return usernames;
  }

  @Override
  public List<String> getUsersInRole(ITenant tenant, String roleName) {
    return getUsersInRole(tenant, userRoleDao.getRole(tenant, roleName), roleName);
  }
  
  public void setUserRoleDao(IUserRoleDao userRoleDao) {
    this.userRoleDao = userRoleDao;
  }

  public void setUserDetailsService(UserDetailsService userDetailsService) {
    this.userDetailsService = userDetailsService;
  }

  public void setTenantedRoleNameUtils(ITenantedPrincipleNameResolver tenantedRoleNameUtils) {
    this.tenantedRoleNameUtils = tenantedRoleNameUtils;
  }

  public ITenantedPrincipleNameResolver getTenantedRoleNameUtils() {
    return tenantedRoleNameUtils;
  }
  
  public ITenantedPrincipleNameResolver getTenantedUserNameUtils() {
    return tenantedUserNameUtils;
  }

  public void setTenantedUserNameUtils(ITenantedPrincipleNameResolver tenantedUserNameUtils) {
    this.tenantedUserNameUtils = tenantedUserNameUtils;
  }
}
