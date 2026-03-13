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


package org.pentaho.platform.security.policy.rolebased.ws;

import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.security.policy.rolebased.messages.Messages;

import jakarta.jws.WebService;
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

  @Override
  public void logout() {
    // no-op, handled in PentahoWSSpringServlet
  }
}
