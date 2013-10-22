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

import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.engine.security.userroledao.UserRoleInfo;
import org.pentaho.platform.core.mt.Tenant;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import javax.jws.WebService;
import java.util.List;

/**
 * Implementation of {@link IUserRoleListWebService} that delegates to an {@link IUserRoleListService} instance.
 * 
 * @author rmansoor
 */
@WebService( endpointInterface = "org.pentaho.platform.security.userrole.ws.IUserRoleListWebService",
    serviceName = "userRoleListService", portName = "userRoleListServicePort",
    targetNamespace = "http://www.pentaho.org/ws/1.0" )
public class DefaultUserRoleListWebService implements IUserRoleListWebService {

  // ~ Static fields/initializers
  // ======================================================================================

  // ~ Instance fields
  // =================================================================================================

  private IUserRoleListService userRoleListService;

  // ~ Constructors
  // ====================================================================================================

  /**
   * No-arg constructor for when in Pentaho BI Server.
   */
  public DefaultUserRoleListWebService() {
    super();
    userRoleListService = PentahoSystem.get( IUserRoleListService.class );
    if ( userRoleListService == null ) {
      throw new IllegalStateException( "no IUserRoleListService implementation" );
    }
  }

  public DefaultUserRoleListWebService( final IUserRoleListService userRoleListService ) {
    super();
    this.userRoleListService = userRoleListService;
  }

  // ~ Methods
  // =========================================================================================================

  @Override
  public List<String> getAllRoles() {
    return userRoleListService.getAllRoles();
  }

  @Override
  public List<String> getAllUsers() {
    return userRoleListService.getAllUsers();
  }

  public List<String> getUsersInRole( String role ) {
    return userRoleListService.getUsersInRole( null, role );
  }

  public List<String> getRolesForUser( String username ) {
    return userRoleListService.getRolesForUser( null, username );
  }

  @Override
  public UserRoleInfo getUserRoleInfo() {
    UserRoleInfo userRoleInfo = new UserRoleInfo();
    userRoleInfo.setRoles( getAllRoles() );
    userRoleInfo.setUsers( getAllUsers() );
    return userRoleInfo;
  }

  @Override
  public List<String> getAllRolesForTenant( Tenant tenant ) {
    return userRoleListService.getAllRoles( tenant );
  }

  @Override
  public List<String> getAllUsersForTenant( Tenant tenant ) {
    return userRoleListService.getAllUsers( tenant );
  }

}
