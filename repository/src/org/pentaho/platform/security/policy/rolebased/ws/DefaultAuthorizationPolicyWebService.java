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

package org.pentaho.platform.security.policy.rolebased.ws;

import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.security.policy.rolebased.messages.Messages;

import javax.jws.WebService;
import java.util.List;

/**
 * Implementation of {@link IAuthorizationPolicyWebService} that delegates to an {@link IAuthorizationPolicy}
 * instance.
 * 
 * @author mlowery
 */
@WebService( endpointInterface = "org.pentaho.platform.security.policy.rolebased.ws.IAuthorizationPolicyWebService",
    serviceName = "authorizationPolicy", portName = "authorizationPolicyPort",
    targetNamespace = "http://www.pentaho.org/ws/1.0" )
public class DefaultAuthorizationPolicyWebService implements IAuthorizationPolicyWebService {

  // ~ Static fields/initializers
  // ======================================================================================

  // ~ Instance fields
  // =================================================================================================

  private IAuthorizationPolicy policy;

  // ~ Constructors
  // ====================================================================================================

  /**
   * No-arg constructor for when in Pentaho BI Server.
   */
  public DefaultAuthorizationPolicyWebService() {
    super();
    policy = PentahoSystem.get( IAuthorizationPolicy.class );
    if ( policy == null ) {
      throw new IllegalStateException( Messages.getInstance().getString(
          "DefaultAuthorizationPolicyWebService.ERROR_0001_MISSING_AUTHZ_POLICY" ) ); //$NON-NLS-1$
    }
  }

  public DefaultAuthorizationPolicyWebService( final IAuthorizationPolicy policy ) {
    super();
    this.policy = policy;
  }

  // ~ Methods
  // =========================================================================================================

  public List<String> getAllowedActions( final String actionNamespace ) {
    return policy.getAllowedActions( actionNamespace );
  }

  public boolean isAllowed( final String actionName ) {
    return policy.isAllowed( actionName );
  }

}
