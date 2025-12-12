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


package org.pentaho.platform.security.userroledao.ws;

import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.security.policy.rolebased.actions.AdministerSecurityAction;
import org.pentaho.platform.security.userroledao.messages.Messages;

import jakarta.jws.WebService;

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

  @Override
  public void logout() {
    // no-op, handled in PentahoWSSpringServlet
  }
}
