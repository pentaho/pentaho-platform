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
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.decisions.IAuthorizationDecision;
import org.pentaho.platform.api.engine.security.authorization.resources.IResourceAuthorizationRequest;
import org.pentaho.platform.engine.security.authorization.core.decisions.ImpliedAuthorizationDecision;

import java.util.Optional;

public class ResourceActionGeneralRequirementAuthorizationRule extends AbstractAuthorizationRule {
  @NonNull
  @Override
  public Optional<IAuthorizationDecision> authorize( @NonNull IAuthorizationRequest request,
                                                     @NonNull IAuthorizationContext context ) {
    if ( !( request instanceof IResourceAuthorizationRequest ) ) {
      return abstain();
    }

    return authorizeCore( (IResourceAuthorizationRequest) request, context );
  }

  @NonNull
  protected Optional<IAuthorizationDecision> authorizeCore(
    @NonNull IResourceAuthorizationRequest resourceRequest,
    @NonNull IAuthorizationContext context ) {

    var generalRequest = resourceRequest.asGeneral();
    var generalDecision = context.authorize( generalRequest );

    return Optional.of( new ImpliedAuthorizationDecision( resourceRequest, generalDecision ) );
  }
}
