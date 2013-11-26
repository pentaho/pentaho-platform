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

package org.pentaho.platform.security.userroledao.ws;

import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.security.policy.rolebased.actions.AdministerSecurityAction;
import org.pentaho.platform.security.userroledao.messages.Messages;

import javax.jws.WebService;

/**
 * Same as {@link UserRoleWebService} except that it uses task permissions to determine administrator status
 * instead of {@code ISecurityHelper.isPentahoAdministrator}.
 * 
 * @author Will Gorman (wgorman@pentaho.com)
 */
@WebService( endpointInterface = "org.pentaho.platform.security.userroledao.ws.IUserRoleWebService",
    serviceName = "userRoleService", portName = "userRoleServicePort",
    targetNamespace = "http://www.pentaho.org/ws/1.0" )
public class AuthorizationPolicyBasedUserRoleWebService extends UserRoleWebService {

  @Override
  protected boolean isAdmin() {
    IAuthorizationPolicy policy = PentahoSystem.get( IAuthorizationPolicy.class );
    if ( policy == null ) {
      throw new IllegalStateException( Messages.getInstance().getString(
          "AuthorizationPolicyBasedUserRoleWebService.ERROR_0001_MISSING_AUTHZ_POLICY" ) ); //$NON-NLS-1$
    }
    return policy.isAllowed( AdministerSecurityAction.NAME ); //$NON-NLS-1$
  }
}
