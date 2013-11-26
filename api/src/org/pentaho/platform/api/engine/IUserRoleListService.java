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

package org.pentaho.platform.api.engine;

import org.pentaho.platform.api.mt.ITenant;

import java.util.List;

public interface IUserRoleListService {

  /**
   * Returns all authorities known to the provider. Cannot return <code>null</code>
   * 
   * @return the authorities (never <code>null</code>)
   */
  public List<String> getAllRoles();

  /**
   * Returns all System authorities known to the provider. Cannot return <code>null</code>
   * 
   * @return the authorities (never <code>null</code>)
   */
  public List<String> getSystemRoles();

  /**
   * Returns all authorities known to the provider for a given tenant. Cannot return <code>null</code>
   * 
   * @param tenant
   * @return the authorities (never <code>null</code>)
   */
  public List<String> getAllRoles( ITenant tenant );

  /**
   * Returns all user names known to the provider. Cannot return <code>null</code>
   * 
   * @return the users (never <code>null</code>)
   */
  public List<String> getAllUsers();

  /**
   * Returns all user names known to the provider for a given tenant. Cannot return <code>null</code>
   * 
   * @param tenant
   * @return the users (never <code>null</code>)
   */
  public List<String> getAllUsers( ITenant tenant );

  /**
   * Returns all known users in the specified role. Cannot return <code>null</code>
   * 
   * @param tenant
   *          . tenant information
   * @param authority
   *          The authority to look users up by. Cannot be <code>null</code>
   * @return the users. (never <code>null</code>)
   */
  public List<String> getUsersInRole( ITenant tenant, String role );

  /**
   * Returns all authorities granted for a specified user.
   * 
   * @param tenant
   *          information
   * @param username
   *          The name of the user to look up authorities for
   * @return the authorities. (Never <code>null</code>)
   */
  public List<String> getRolesForUser( ITenant tenant, String username );

}
