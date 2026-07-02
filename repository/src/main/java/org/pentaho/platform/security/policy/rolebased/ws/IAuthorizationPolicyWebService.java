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

import jakarta.jws.WebService;

/**
 * JAX-WS-safe version of {@link IAuthorizationPolicy}. In this case, nothing is different but it keeps JAX-WS
 * annotations out of the core classes.
 * 
 * @author mlowery
 */
@WebService
public interface IAuthorizationPolicyWebService extends IAuthorizationPolicy {
  public default void logout() { }
}
