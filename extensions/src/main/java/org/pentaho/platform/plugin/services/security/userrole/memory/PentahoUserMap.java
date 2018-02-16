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

package org.pentaho.platform.plugin.services.security.userrole.memory;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.Assert;


/**
 * Brought over from spring-security 3.2.5, as this class no longer exists in spring-security 4.1
 * @see https://github.com/spring-projects/spring-security/blob/3.2.5.RELEASE/core/src/main/java/org/springframework/security/core/userdetails/memory/UserMap.java
 */
public class PentahoUserMap {
  //~ Static fields/initializers =====================================================================================

  private static final Log logger = LogFactory.getLog( PentahoUserMap.class );

  //~ Instance fields ================================================================================================

  private final Map<String, UserDetails> userMap = new HashMap<String, UserDetails>();

  //~ Methods ========================================================================================================

  /**
   * Adds a user to the in-memory map.
   *
   * @param user the user to be stored
   *
   * @throws IllegalArgumentException if a null User was passed
   */
  public void addUser( UserDetails user ) throws IllegalArgumentException {
    Assert.notNull( user, "Must be a valid User" );

    logger.info( "Adding user [" + user + "]" );
    this.userMap.put( user.getUsername().toLowerCase(), user );
  }

  /**
   * Locates the specified user by performing a case insensitive search by username.
   *
   * @param username to find
   *
   * @return the located user
   *
   * @throws UsernameNotFoundException if the user could not be found
   */
  public UserDetails getUser( String username ) throws UsernameNotFoundException {
    UserDetails result = this.userMap.get( username.toLowerCase() );

    if ( result == null ) {
      throw new UsernameNotFoundException( "Could not find user: " + username );
    }

    return result;
  }

  /**
   * Indicates the size of the user map.
   *
   * @return the number of users in the map
   */
  public int getUserCount() {
    return this.userMap.size();
  }

  /**
   * Set the users in this {@link UserMap}. Overrides previously added users.
   *
   * @param users {@link Map} &lt;{@link String}, {@link UserDetails}> with pairs (username, userdetails)
   * @since 1.1
   */
  public void setUsers( Map<String, UserDetails> users ) {
    userMap.clear();
    for ( Map.Entry<String, UserDetails> entry : users.entrySet() ) {
      userMap.put( entry.getKey().toLowerCase(), entry.getValue() );
    }
  }
}
