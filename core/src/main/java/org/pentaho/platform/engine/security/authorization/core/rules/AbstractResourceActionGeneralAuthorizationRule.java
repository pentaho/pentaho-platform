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
import org.pentaho.platform.api.engine.security.authorization.IResourceAuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.decisions.IAuthorizationDecision;

import java.util.Optional;

public abstract class AbstractResourceActionGeneralAuthorizationRule extends AbstractAuthorizationRule {
  @NonNull
  @Override
  public final Optional<IAuthorizationDecision> authorize( @NonNull IAuthorizationRequest request,
                                                           @NonNull IAuthorizationContext context ) {
    if ( !request.getAction().isResourceAction() || ( request instanceof IResourceAuthorizationRequest ) ) {
      return abstain();
    }

    return authorizeCore( request, context );
  }

  @NonNull
  protected abstract Optional<IAuthorizationDecision> authorizeCore(
    @NonNull IAuthorizationRequest request,
    @NonNull IAuthorizationContext context );
}
