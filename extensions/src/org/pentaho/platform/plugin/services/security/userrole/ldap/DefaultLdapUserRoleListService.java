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
package org.pentaho.platform.plugin.services.security.userrole.ldap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.pentaho.platform.core.mt.Tenant;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.plugin.services.security.userrole.ldap.search.LdapSearch;
import org.pentaho.platform.repository2.unified.jcr.JcrTenantUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.util.Assert;

public class DefaultLdapUserRoleListService implements IUserRoleListService, InitializingBean {

  // ~ Static fields/initializers ======================================================================================

  // ~ Instance fields =================================================================================================

  private LdapSearch allUsernamesSearch;

  private LdapSearch allAuthoritiesSearch;

  private LdapSearch usernamesInRoleSearch;

  /**
   * Case-sensitive by default.
   */
  private Comparator<String> roleComparator;

  /**
   * Case-sensitive by default.
   */
  private Comparator<String> usernameComparator;

  /**
   * Used only for <code>getAuthoritiesForUser</code>. This is preferred
   * over an <code>LdapSearch</code> in
   * <code>authoritiesForUserSearch</code> as it keeps roles returned by
   * <code>UserDetailsService</code> and roles returned by
   * <code>DefaultLdapUserRoleListService</code> consistent.
   */
  private UserDetailsService userDetailsService;

  private ITenantedPrincipleNameResolver userNameUtils;

  private ITenantedPrincipleNameResolver roleNameUtils;
  // ~ Constructors ====================================================================================================

  public DefaultLdapUserRoleListService() {
    super();
  }
  
  public DefaultLdapUserRoleListService(final Comparator<String> usernameComparator, final Comparator<String> roleComparator,ITenantedPrincipleNameResolver userNameUtils, ITenantedPrincipleNameResolver roleNameUtils) {
    super();
    this.usernameComparator = usernameComparator;
    this.roleComparator = roleComparator;
    this.userNameUtils = userNameUtils;    
    this.roleNameUtils = roleNameUtils;    
  }
  
  // ~ Methods =========================================================================================================

  @Override
  public void afterPropertiesSet() throws Exception {
  }

  @Override
  public List<String> getAllRoles() {
    List<GrantedAuthority> results = allAuthoritiesSearch.search(new Object[0]);
    List<String> roles = new ArrayList<String>(results.size());
    for (GrantedAuthority role : results) {
      roles.add(role.getAuthority());
    }
    if (null != roleComparator) {
      Collections.sort(roles, roleComparator);
    }
    return roles;
  }

  @Override
  public List<String> getAllUsers() {
    List<String> results = allUsernamesSearch.search(new Object[0]);
    if (null != usernameComparator) {
      Collections.sort(results, usernameComparator);
    }
    return results;
  }

  @Override
  public List<String> getUsersInRole(final ITenant tenant, final String role) {
    if(tenant != null && !tenant.equals(JcrTenantUtils.getDefaultTenant())) {
      throw new UnsupportedOperationException("only allowed to access to default tenant");
    }
    String updateRole = roleNameUtils.getPrincipleName(role);
    List<String> results = usernamesInRoleSearch.search(new Object[] { updateRole });
    if (null != usernameComparator) {
      Collections.sort(results, usernameComparator);
    }
    return results;
  }

  @Override
  public List<String> getRolesForUser(final ITenant tenant, final String username) {
    if(tenant != null && !tenant.equals(JcrTenantUtils.getDefaultTenant())) {
      throw new UnsupportedOperationException("only allowed to access to default tenant");
    }
    UserDetails user = userDetailsService.loadUserByUsername(userNameUtils.getPrincipleName(username));
    List<GrantedAuthority> results = Arrays.asList(user.getAuthorities());
    List<String> roles = new ArrayList<String>(results.size());
    for (GrantedAuthority role : results) {
      roles.add(role.getAuthority());
    }
    if (null != roleComparator) {
      Collections.sort(roles, roleComparator);
    }
    return roles;
  }

  public void setAllUsernamesSearch(final LdapSearch allUsernamesSearch) {
    this.allUsernamesSearch = allUsernamesSearch;
  }

  public void setAllAuthoritiesSearch(final LdapSearch allAuthoritiesSearch) {
    this.allAuthoritiesSearch = allAuthoritiesSearch;
  }

  public void setUsernamesInRoleSearch(final LdapSearch usernamesInRoleSearch) {
    this.usernamesInRoleSearch = usernamesInRoleSearch;
  }

  public void setUserDetailsService(final UserDetailsService userDetailsService) {
    this.userDetailsService = userDetailsService;
  }

  public void setRoleComparator(final Comparator<String> roleComparator) {
    Assert.notNull(roleComparator);
    this.roleComparator = roleComparator;
  }

  public void setUsernameComparator(final Comparator<String> usernameComparator) {
    Assert.notNull(usernameComparator);
    this.usernameComparator = usernameComparator;
  }

  public ITenantedPrincipleNameResolver getUserNameUtils() {
    return userNameUtils;
  }

  public void setUserNameUtils(ITenantedPrincipleNameResolver userNameUtils) {
    this.userNameUtils = userNameUtils;
  }

  public ITenantedPrincipleNameResolver getRoleNameUtils() {
    return roleNameUtils;
  }

  public void setRoleNameUtils(ITenantedPrincipleNameResolver roleNameUtils) {
    this.roleNameUtils = roleNameUtils;
  }
  
  @Override
  public List<String> getAllRoles(ITenant tenant) {
    if(tenant != null && !tenant.equals(JcrTenantUtils.getDefaultTenant())) {
      throw new UnsupportedOperationException("only allowed to access to default tenant");
    }
    return getAllRoles();
  }

  @Override
  public List<String> getAllUsers(ITenant tenant) {
    if(tenant != null && !tenant.equals(JcrTenantUtils.getDefaultTenant())) {
      throw new UnsupportedOperationException("only allowed to access to default tenant");
    }
    return getAllUsers();
  }
}
