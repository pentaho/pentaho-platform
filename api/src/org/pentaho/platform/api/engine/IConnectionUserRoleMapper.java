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

/**
 * This interface provides for mapping a Platform user to a connection user, or a users' roles to known/valid roles
 * for a connection.
 * 
 */
public interface IConnectionUserRoleMapper {

  /**
   * Maps the user from the given IPentahoSession into a user (or credential) appropriate for the connection
   * 
   * Rules: - If the user has no rights to the specified connectionContextName, you must throw
   * PentahoAccessControlException - If null or empty array is returned, then no mapping is required, and
   * connection can use defaults
   * 
   * @param userSession
   *          The users' Session
   * @param connectionContextName
   *          - The connection name (maybe a datasource name, a catalog, etc)
   * @return
   */
  Object mapConnectionUser( IPentahoSession userSession, String connectionContextName )
    throws PentahoAccessControlException;

  /**
   * Provides a mapping from the roles defined for a user, and roles appropriate for the connection
   * 
   * Rules: - If the user has no rights to the specified connectionContextName, you must throw
   * PentahoAccessControlException - If null or empty array is returned, then no mapping is required, and
   * connection can use defaults
   * 
   * @param userSession
   * @param connectionContextName
   *          - The connection name (maybe a datasource name, a catalog, etc)
   * @return
   */
  String[] mapConnectionRoles( IPentahoSession userSession, String connectionContextName )
    throws PentahoAccessControlException;

}
