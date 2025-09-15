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
import org.pentaho.platform.api.engine.security.authorization.resources.IResourceSpecificAuthorizationRequest;
import org.pentaho.platform.engine.security.authorization.core.decisions.ResourceSpecificAuthorizationDecision;

import java.util.Optional;

public class ResourceSpecificAuthorizationRule
  extends AbstractAuthorizationRule<IResourceAuthorizationRequest> {

  @NonNull
  @Override
  public Class<IResourceAuthorizationRequest> getRequestType() {
    return IResourceAuthorizationRequest.class;
  }

  @NonNull
  @Override
  public Optional<IAuthorizationDecision> authorize( @NonNull IResourceAuthorizationRequest resourceRequest,
                                                     @NonNull IAuthorizationContext context ) {
    // Rule only applies to abstract/effective/top-level resource requests
    if ( resourceRequest instanceof IResourceSpecificAuthorizationRequest ) {
      return abstain();
    }

    // Dispatch a corresponding specific request.
    var specificRequest = resourceRequest.asSpecific();
    var specificDecision = context.authorize( specificRequest );
    return Optional.of( new ResourceSpecificAuthorizationDecision( resourceRequest, specificDecision ) );
  }
}
