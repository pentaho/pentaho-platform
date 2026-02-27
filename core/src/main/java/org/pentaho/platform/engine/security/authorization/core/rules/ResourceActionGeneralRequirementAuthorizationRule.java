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
import org.pentaho.platform.engine.security.authorization.core.decisions.ResourceActionGeneralRequirementAuthorizationDecision;

import java.util.Optional;

public class ResourceActionGeneralRequirementAuthorizationRule
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

    var generalRequest = resourceRequest.asGeneral();
    var generalDecision = context.authorize( generalRequest );
    return Optional.of( new ResourceActionGeneralRequirementAuthorizationDecision( resourceRequest, generalDecision ) );
  }
}
