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

package org.pentaho.platform.web.http.api.resources.services;

import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.security.policy.rolebased.actions.AdministerSecurityAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryCreateAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryReadAction;
import org.pentaho.platform.web.http.api.resources.SystemResourceUtil;
import org.pentaho.platform.web.http.api.resources.UserListWrapper;

public class UserRoleListService {

  public String doGetRolesForUser( String user ) throws Exception {
    if ( canAdminister() ) {
      return getRolesForUser( user );
    } else {
      throw new UnauthorizedException();
    }
  }

  public String doGetUsersInRole( String role ) throws Exception {
    if ( canAdminister() ) {
      return getUsersInRole( role );
    } else {
      throw new UnauthorizedException();
    }
  }

  public UserListWrapper getUsers() {
    IUserRoleListService service = getUserRoleListService();
    return new UserListWrapper( service.getAllUsers() );
  }

  protected boolean canAdminister() {
    IAuthorizationPolicy policy = PentahoSystem.get( IAuthorizationPolicy.class );
    return policy.isAllowed( RepositoryReadAction.NAME ) && policy.isAllowed( RepositoryCreateAction.NAME )
        && ( policy.isAllowed( AdministerSecurityAction.NAME ) );
  }

  protected String getRolesForUser( String user ) throws Exception {
    return SystemResourceUtil.getRolesForUser( user ).asXML();
  }

  protected String getUsersInRole( String role ) throws Exception {
    return SystemResourceUtil.getUsersInRole( role ).asXML();
  }

  protected IUserRoleListService getUserRoleListService() {
    return PentahoSystem.get( IUserRoleListService.class );
  }

  public class UnauthorizedException extends Exception {
  }
}
