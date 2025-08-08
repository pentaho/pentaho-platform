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
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRule;
import org.pentaho.platform.api.engine.security.authorization.decisions.IAuthorizationDecision;
import org.pentaho.platform.api.engine.security.authorization.decisions.IOpposedAuthorizationDecision;
import org.pentaho.platform.engine.security.authorization.core.decisions.OpposedAuthorizationDecision;

import java.util.Objects;
import java.util.Optional;

/**
 * The {@code OpposedAuthorizationRule} class represents an authorization rule whose decision is always opposed to that
 * of another rule. Abstentions are preserved.
 * <p>
 * The decisions made by this rule are always of type {@link IOpposedAuthorizationDecision}, and have as its
 * {@link IOpposedAuthorizationDecision#getOpposedToDecision() opposed-to decision} the result of authorizing the
 * opposed-to rule for the same request.
 */
public class OpposedAuthorizationRule extends AbstractAuthorizationRule {

  @NonNull
  private final IAuthorizationRule opposedToRule;

  public OpposedAuthorizationRule( @NonNull IAuthorizationRule opposedToRule ) {
    this.opposedToRule = Objects.requireNonNull( opposedToRule );
  }

  @NonNull
  @Override
  public Optional<IAuthorizationDecision> authorize( @NonNull IAuthorizationRequest request,
                                                     @NonNull IAuthorizationContext context ) {

    return opposedToRule
      .authorize( request, context )
      .map( OpposedAuthorizationDecision::new );
  }
}
