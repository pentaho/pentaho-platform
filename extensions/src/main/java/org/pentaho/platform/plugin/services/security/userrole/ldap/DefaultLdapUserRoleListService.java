/*!
 *
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
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.plugin.services.security.userrole.ldap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.engine.security.IAuthenticationRoleMapper;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.security.userrole.ldap.search.LdapSearch;
import org.pentaho.platform.repository2.unified.jcr.JcrTenantUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.util.Assert;

public class DefaultLdapUserRoleListService implements IUserRoleListService, InitializingBean {

  // ~ Static fields/initializers
  // ======================================================================================

  // ~ Instance fields
  // =================================================================================================

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
   * Used only for <code>getAuthoritiesForUser</code>. This is preferred over an <code>LdapSearch</code> in
   * <code>authoritiesForUserSearch</code> as it keeps roles returned by <code>UserDetailsService</code> and roles
   * returned by <code>DefaultLdapUserRoleListService</code> consistent.
   */
  private UserDetailsService userDetailsService;

  private ITenantedPrincipleNameResolver userNameUtils;

  private ITenantedPrincipleNameResolver roleNameUtils;

  private List<String> systemRoles;

  private List<String> extraRoles;

  private IAuthenticationRoleMapper roleMapper;

  // ~ Constructors
  // ====================================================================================================

  public DefaultLdapUserRoleListService() {
    super();
    this.extraRoles = PentahoSystem.get( ArrayList.class, "extraSystemAuthorities", PentahoSessionHolder.getSession() );
  }

  public DefaultLdapUserRoleListService( final Comparator<String> usernameComparator,
      final Comparator<String> roleComparator ) {
    super();
    this.usernameComparator = usernameComparator;
    this.roleComparator = roleComparator;
  }

  public DefaultLdapUserRoleListService( final Comparator<String> usernameComparator,
      final Comparator<String> roleComparator, final IAuthenticationRoleMapper roleMapper ) {
    this( usernameComparator, roleComparator );
    this.roleMapper = roleMapper;
  }

  // ~ Methods
  // =========================================================================================================

  @Override
  public void afterPropertiesSet() throws Exception {
  }

  @Override
  public List<String> getAllRoles() {
    List<GrantedAuthority> results = allAuthoritiesSearch.search( new Object[0] );
    Set<String> roles = ( roleComparator != null ) ? new TreeSet<String>( roleComparator ) : new LinkedHashSet<String>( results.size() );
    for ( GrantedAuthority role : results ) {
      String roleString =
          ( roleMapper != null ) ? roleMapper.toPentahoRole( role.getAuthority() ) : role.getAuthority();
      if ( roleString != null && extraRoles != null && !extraRoles.contains( roleString ) ) {
        roles.add( roleString );
      }
    }
    // Now add extra role if it does not exist in the list
    for ( String extraRole : extraRoles ) {
      roles.add( extraRole );
    }

    return new ArrayList<String>( roles );
  }

  @Override
  public List<String> getAllUsers() {
    List<String> results = allUsernamesSearch.search( new Object[0] );
    if ( null != usernameComparator ) {
      Collections.sort( results, usernameComparator );
    }
    return results;
  }

  @Override
  public List<String> getUsersInRole( final ITenant tenant, final String role ) {
    if ( tenant != null && !tenant.equals( JcrTenantUtils.getDefaultTenant() ) ) {
      throw new UnsupportedOperationException( "only allowed to access to default tenant" );
    }
    String updateRole = roleNameUtils.getPrincipleName( role );
    // User Role mapper to get the equivalent ldap role
    List<String> results = usernamesInRoleSearch.search( new Object[] { roleMapper.fromPentahoRole( updateRole ) } );
    if ( null != usernameComparator ) {
      Collections.sort( results, usernameComparator );
    }
    return results;
  }

  @Override
  public List<String> getRolesForUser( final ITenant tenant, final String username ) {
    if ( tenant != null && !tenant.equals( JcrTenantUtils.getDefaultTenant() ) ) {
      throw new UnsupportedOperationException( "only allowed to access to default tenant" );
    }
    UserDetails user = userDetailsService.loadUserByUsername( userNameUtils.getPrincipleName( username ) );
    Collection<? extends GrantedAuthority> results = user.getAuthorities();
    Set<String> roles = ( roleComparator != null ) ? new TreeSet<String>( roleComparator ) : new LinkedHashSet<String>( results.size() );
    for ( GrantedAuthority role : results ) {
      roles.add( role.getAuthority() );
    }
    // Now add extra role if it does not exist in the list
    for ( String extraRole : extraRoles ) {
      roles.add( extraRole );
    }

    return new ArrayList<String>( roles );
  }

  public void setAllUsernamesSearch( final LdapSearch allUsernamesSearch ) {
    this.allUsernamesSearch = allUsernamesSearch;
  }

  public void setAllAuthoritiesSearch( final LdapSearch allAuthoritiesSearch ) {
    this.allAuthoritiesSearch = allAuthoritiesSearch;
  }

  public void setUsernamesInRoleSearch( final LdapSearch usernamesInRoleSearch ) {
    this.usernamesInRoleSearch = usernamesInRoleSearch;
  }

  public void setUserDetailsService( final UserDetailsService userDetailsService ) {
    this.userDetailsService = userDetailsService;
  }

  public void setRoleComparator( final Comparator<String> roleComparator ) {
    this.roleComparator = roleComparator;
  }

  public void setUsernameComparator( final Comparator<String> usernameComparator ) {
    Assert.notNull( usernameComparator );
    this.usernameComparator = usernameComparator;
  }

  public ITenantedPrincipleNameResolver getUserNameUtils() {
    return userNameUtils;
  }

  public void setUserNameUtils( ITenantedPrincipleNameResolver userNameUtils ) {
    this.userNameUtils = userNameUtils;
  }

  public ITenantedPrincipleNameResolver getRoleNameUtils() {
    return roleNameUtils;
  }

  public void setRoleNameUtils( ITenantedPrincipleNameResolver roleNameUtils ) {
    this.roleNameUtils = roleNameUtils;
  }

  @Override
  public List<String> getAllRoles( ITenant tenant ) {
    if ( tenant != null && !tenant.equals( JcrTenantUtils.getDefaultTenant() ) ) {
      throw new UnsupportedOperationException( "only allowed to access to default tenant" );
    }
    return getAllRoles();
  }

  @Override
  public List<String> getAllUsers( ITenant tenant ) {
    if ( tenant != null && !tenant.equals( JcrTenantUtils.getDefaultTenant() ) ) {
      throw new UnsupportedOperationException( "only allowed to access to default tenant" );
    }
    return getAllUsers();
  }

  @Override
  public List<String> getSystemRoles() {
    return systemRoles;
  }

  public void setSystemRoles( List<String> systemRoles ) {
    this.systemRoles = systemRoles;
  }

  public void setExtraRoles( List<String> extraRoles ) {
    this.extraRoles = extraRoles;
  }

  public List<String> getExtraRoles() {
    return extraRoles;
  }
}
