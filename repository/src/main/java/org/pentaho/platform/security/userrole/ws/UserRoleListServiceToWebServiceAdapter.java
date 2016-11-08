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
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.core.mt.Tenant;

import java.util.List;

/**
 * Converts calls to {@link IUserRoleListService} into {@link IUserRoleListWebService}. This is how client code
 * remains unaware of server code location.
 * 
 * @author rmansoor
 */

public class UserRoleListServiceToWebServiceAdapter implements IUserRoleListService {

  private IUserRoleListWebService userRoleListWebService;

  public UserRoleListServiceToWebServiceAdapter( IUserRoleListWebService userRoleListWebService ) {
    super();
    this.userRoleListWebService = userRoleListWebService;
  }

  @Override
  public List<String> getAllRoles() {
    return userRoleListWebService.getAllRoles();
  }

  @Override
  public List<String> getAllUsers() {
    return userRoleListWebService.getAllUsers();
  }

  public UserRoleInfo getUserRoleInfo() {
    return userRoleListWebService.getUserRoleInfo();
  }

  @Override
  public List<String> getAllRoles( ITenant tenant ) {
    return userRoleListWebService.getAllRolesForTenant( (Tenant) tenant );
  }

  @Override
  public List<String> getAllUsers( ITenant tenant ) {
    return userRoleListWebService.getAllUsersForTenant( (Tenant) tenant );
  }

  @Override
  public List<String> getUsersInRole( ITenant tenant, String role ) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<String> getRolesForUser( ITenant tenant, String username ) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<String> getSystemRoles() {
    throw new UnsupportedOperationException();
  }
}
