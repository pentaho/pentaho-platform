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

import org.pentaho.platform.api.mt.ITenant;

import java.util.List;

public interface IUserRoleListService {

  /**
   * Returns all authorities known to the provider. Cannot return <code>null</code>.
   * 
   * @return Returns the authorities.  Cannot return <code>null</code>.
   */
  public List<String> getAllRoles();

  /**
   * Returns all system authorities known to the provider. Cannot return <code>null</code>.
   * 
   * @return Returns the authorities.  Cannot return <code>null</code>.
   */
  public List<String> getSystemRoles();

  /**
   * Returns all authorities known to the provider for a given tenant. Cannot return <code>null</code>.
   * 
   * @param tenant To be used for searching authorities.
   * @return Returns the authorities.  Cannot return <code>null</code>.
   */
  public List<String> getAllRoles( ITenant tenant );

  /**
   * Returns all user names known to the provider. Cannot return <code>null</code>.
   * 
   * @return Returns the users.  Cannot return <code>null</code>.
   */
  public List<String> getAllUsers();

  /**
   * Returns all user names known to the provider for a given tenant. Cannot return <code>null</code>.
   * 
   * @param tenant Tenant to be used for searching users.
   * @return Returns the users.  Cannot return <code>null</code>.
   */
  public List<String> getAllUsers( ITenant tenant );

  /**
   * Returns all known users in the specified role. Cannot return <code>null</code>.
   * 
   * @param tenant
   *          Tenant information.
   * @param authority
   *          Indicates the authority to look users up by. Cannot be <code>null</code>.
   * @return Returns the users with the specified roles in scope of the specified tenant.  Cannot return <code>null</code>.
   */
  public List<String> getUsersInRole( ITenant tenant, String role );

  /**
   * Returns all authorities granted for a specified user.
   * 
   * @param tenant
   *          Tenant information.
   * @param username
   *          Indicates the name of the user to look up authorities for.
   * @return Returns the authorities of the user. Cannot return <code>null</code>.
   */
  public List<String> getRolesForUser( ITenant tenant, String username );

}
