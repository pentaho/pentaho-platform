/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.security.userrole.ws;

/**
 * JAX-WS-safe version of {@code IUserRoleListService}.
 * 
 * 
 * @author rmansoor
 */

import org.pentaho.platform.api.engine.security.userroledao.UserRoleInfo;
import org.pentaho.platform.core.mt.Tenant;

import javax.jws.WebService;
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

}
