/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.platform.plugin.services.security.userrole.memory;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class UserRoleListEnhancedUserMap {
  // ~ Static fields/initializers
  // =============================================

  private Logger logger = LoggerFactory.getLogger( UserRoleListEnhancedUserMap.class );

  // ~ Instance fields
  // ========================================================

  private final Map<String, UserDetails> userRoleListEnhanceduserMap = new HashMap<String, UserDetails>();

  private final Map<GrantedAuthority, Set<String>> rolesToUsersMap = new HashMap<GrantedAuthority, Set<String>>();

  // ~ Methods
  // ================================================================

  public void addUser( final UserDetails user ) throws IllegalArgumentException {
    Assert.notNull( user, "Must be a valid User" );
    logger.info( "Adding user [" + user + "]" );
    this.userRoleListEnhanceduserMap.put( user.getUsername().toLowerCase(), user );
    Collection<? extends GrantedAuthority> auths = user.getAuthorities();
    for ( GrantedAuthority anAuthority : auths ) {
      Set<String> userListForAuthority = getRolesToUsersMap().get( anAuthority );
      if ( userListForAuthority == null ) {
        userListForAuthority = new TreeSet<String>();
        getRolesToUsersMap().put( anAuthority, userListForAuthority );
      }
      userListForAuthority.add( user.getUsername() );
    }
  }

  public String[] getAllAuthorities() {
    Set<GrantedAuthority> authoritiesSet = getRolesToUsersMap().keySet();
    List<String> roles = new ArrayList<String>( authoritiesSet.size() );
    for ( GrantedAuthority role : authoritiesSet ) {
      roles.add( role.getAuthority() );
    }
    return roles.toArray( new String[0] );
  }

  public String[] getAllUsers() {
    String[] rtn = new String[getUserRoleListEnhanceduserMap().size()];
    Iterator it = getUserRoleListEnhanceduserMap().values().iterator();
    int i = 0;
    while ( it.hasNext() ) {
      rtn[i] = ( (UserDetails) it.next() ).getUsername();
      i++;
    }
    return rtn;
  }

  public String[] getUserNamesInRole( final String role ) {
    Set<String> userListForAuthority = getRolesToUsersMap().get( new SimpleGrantedAuthority( role ) );
    String[] typ = {};
    if ( userListForAuthority != null ) {
      return userListForAuthority.toArray( typ );
    } else {
      return typ;
    }
  }

  public void setUsers( final Map users ) {
    getUserRoleListEnhanceduserMap().clear();
    Iterator iter = users.values().iterator();
    while ( iter.hasNext() ) {
      addUser( (UserDetails) iter.next() );
    }
  }

  /**
   * Indicates the size of the user map.
   *
   * @return the number of users in the map
   */
  public int getUserCount() {
    return getUserRoleListEnhanceduserMap() != null ? getUserRoleListEnhanceduserMap().size() : 0;
  }

  protected Map<String, UserDetails> getUserRoleListEnhanceduserMap() {
    return userRoleListEnhanceduserMap;
  }

  protected Map<GrantedAuthority, Set<String>> getRolesToUsersMap() {
    return rolesToUsersMap;
  }
}
