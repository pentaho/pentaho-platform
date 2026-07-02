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

package org.pentaho.platform.engine.security.authorization.core.rules;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationContext;
import org.pentaho.platform.api.engine.security.authorization.decisions.IAuthorizationDecision;
import org.pentaho.platform.api.engine.security.authorization.resources.IResourceAuthorizationRequest;
import org.pentaho.platform.engine.security.authorization.core.decisions.SupportedActionResourceTypeAuthorizationDecision;

import java.util.Optional;

/**
 * Authorization rule that validates whether an action is supported for a specific resource type.
 * This rule only handles resource-specific authorization requests.
 * This rule denies the request if the action does not support the resource type, and abstains otherwise.
 * It is intended to be used as a "deny-rule".
 */
public class SupportedActionResourceTypeAuthorizationRule extends AbstractAuthorizationRule<IResourceAuthorizationRequest> {

  @NonNull
  @Override
  public Class<IResourceAuthorizationRequest> getRequestType() {
    return IResourceAuthorizationRequest.class;
  }

  @NonNull
  @Override
  public Optional<IAuthorizationDecision> authorize( @NonNull IResourceAuthorizationRequest resourceRequest,
                                                     @NonNull IAuthorizationContext context ) {

    var isSupported = resourceRequest.getAction().performsOnResourceType( resourceRequest.getResource().getType() );
    if ( isSupported ) {
      // This rule is special and exists solely to perform a validation.
      // No point in generating a decision if the request is valid.
      return abstain();
    }

    return Optional.of( new SupportedActionResourceTypeAuthorizationDecision( resourceRequest, false ) );
  }
}
