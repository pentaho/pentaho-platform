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


package org.pentaho.platform.security.userrole.ws;

/**
 * JAX-WS-safe version of {@code IUserRoleListService}.
 * 
 * 
 * @author rmansoor
 */

import org.pentaho.platform.api.engine.security.userroledao.UserRoleInfo;
import org.pentaho.platform.core.mt.Tenant;

import jakarta.jws.WebService;
import java.util.List;

@WebService
public interface IUserRoleListWebService {
  /**
   * This method returns the entire set of roles in the system
   * 
   * @return List of roles
   * 
   */
  public List<String> getAllRoles();

  /**
   * This method returns the entire set of users in the system
   * 
   * @return List of users
   * 
   */
  public List<String> getAllUsers();

  /**
   * This method returns the entire set of users and roles in the system
   * 
   * @return an info object
   * 
   */
  public UserRoleInfo getUserRoleInfo();

  public List<String> getAllRolesForTenant( Tenant tenant );

  public List<String> getAllUsersForTenant( Tenant tenant );

  public default void logout() { }
}
