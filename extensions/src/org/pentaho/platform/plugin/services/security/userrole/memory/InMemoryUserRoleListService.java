/*!
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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.plugin.services.security.userrole.memory;

import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.security.userdetails.UsernameNotFoundException;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * An in-memory implementation of <code>UserRoleListService</code>.
 * 
 * @author mlowery
 */
public class InMemoryUserRoleListService implements IUserRoleListService, InitializingBean {

  /**
   * Case-sensitive by default.
   */
  private Comparator<String> roleComparator;

  /**
   * Case-sensitive by default.
   */
  private Comparator<String> usernameComparator;

  private List<String> allRoles;

  private UserRoleListEnhancedUserMap userRoleListEnhancedUserMap;

  private UserDetailsService userDetailsService;

  private List<String> systemRoles;

  @Override
  public List<String> getAllRoles() {
    List<String> results = new ArrayList<String>( allRoles );
    if ( null != roleComparator ) {
      Collections.sort( results, roleComparator );
    }
    return results;
  }

  @Override
  public List<String> getAllUsers() {
    List<String> results = Arrays.asList( userRoleListEnhancedUserMap.getAllUsers() );
    if ( null != usernameComparator ) {
      Collections.sort( results, usernameComparator );
    }
    return results;
  }

  @Override
  public List<String> getUsersInRole( final ITenant tenant, final String role ) {
    List<String> results = Arrays.asList( userRoleListEnhancedUserMap.getUserNamesInRole( role ) );
    if ( null != usernameComparator ) {
      Collections.sort( results, usernameComparator );
    }
    return results;
  }

  public void setAllRoles( final List<String> allRoles ) {
    this.allRoles = new ArrayList<String>( allRoles );
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    Assert.notNull( userRoleListEnhancedUserMap, Messages.getInstance().getErrorString(
      "InMemoryUserRoleListService.ERROR_0001_PROPERTY_LIST_NOT_SPECIFIED" ) ); //$NON-NLS-1$
    Assert.notNull( allRoles, Messages.getInstance().getString(
      "InMemoryUserRoleListService.ERROR_0002_ALL_AUTHORITIES_NOT_SPECIFIED" ) ); //$NON-NLS-1$
    Assert.notNull( userDetailsService, Messages.getInstance().getString(
      "InMemoryUserRoleListService.ERROR_0003_USERDETAILSSERVICE_NOT_SPECIFIED" ) ); //$NON-NLS-1$
  }

  @Override
  public List<String> getRolesForUser( final ITenant tenant, final String username ) throws UsernameNotFoundException {
    UserDetails user = userDetailsService.loadUserByUsername( username );
    List<GrantedAuthority> results = Arrays.asList( user.getAuthorities() );
    List<String> roles = new ArrayList<String>( results.size() );
    for ( GrantedAuthority role : results ) {
      roles.add( role.getAuthority() );
    }
    if ( null != roleComparator ) {
      Collections.sort( roles, roleComparator );
    }
    return roles;
  }

  public void setUserRoleListEnhancedUserMap( final UserRoleListEnhancedUserMap userRoleListEnhancedUserMap ) {
    this.userRoleListEnhancedUserMap = userRoleListEnhancedUserMap;
  }

  public UserDetailsService getUserDetailsService() {
    return userDetailsService;
  }

  public void setUserDetailsService( final UserDetailsService userDetailsService ) {
    this.userDetailsService = userDetailsService;
  }

  public UserRoleListEnhancedUserMap getUserRoleListEnhancedUserMap() {
    return userRoleListEnhancedUserMap;
  }

  public void setRoleComparator( final Comparator<String> roleComparator ) {
    Assert.notNull( roleComparator );
    this.roleComparator = roleComparator;
  }

  public void setUsernameComparator( final Comparator<String> usernameComparator ) {
    Assert.notNull( usernameComparator );
    this.usernameComparator = usernameComparator;
  }

  @Override
  public List<String> getAllRoles( ITenant tenant ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<String> getAllUsers( ITenant tenant ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<String> getSystemRoles() {
    return systemRoles;
  }

  public void setSystemRoles( List<String> systemRoles ) {
    this.systemRoles = systemRoles;
  }
}
