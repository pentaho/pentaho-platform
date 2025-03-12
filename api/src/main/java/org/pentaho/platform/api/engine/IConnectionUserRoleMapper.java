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
