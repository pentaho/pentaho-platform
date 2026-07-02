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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * The {@code AbstractCompositeAuthorizationRule} is an abstract class for rules that combine multiple rules.
 * Composite rules handle any type of authorization request.
 */
public abstract class AbstractCompositeAuthorizationRule extends AbstractAuthorizationRule<IAuthorizationRequest>
  implements IAuthorizationRule<IAuthorizationRequest> {

  @NonNull
  private final List<IAuthorizationRule<IAuthorizationRequest>> rules;

  protected AbstractCompositeAuthorizationRule( @NonNull List<IAuthorizationRule<IAuthorizationRequest>> rules ) {
    this.rules = Collections.unmodifiableList( rules );
  }

  @NonNull
  @Override
  public Class<IAuthorizationRequest> getRequestType() {
    return IAuthorizationRequest.class;
  }

  @NonNull
  public List<IAuthorizationRule<IAuthorizationRequest>> getRules() {
    return rules;
  }

  @NonNull
  @Override
  public Optional<IAuthorizationDecision> authorize( @NonNull IAuthorizationRequest request,
                                                     @NonNull IAuthorizationContext context ) {

    AbstractCompositeResultBuilder resultBuilder = createResultBuilder( context );

    for ( var rule : getRules() ) {
      Optional<IAuthorizationDecision> ruleResult = context.authorizeRule( request, rule );
      if ( ruleResult.isPresent() ) {
        resultBuilder.withDecision( ruleResult.get() );

        if ( resultBuilder.isImmutable() ) {
          // If the decision is immutable, no need to evaluate other rules.
          break;
        }
      }
    }

    return resultBuilder.build( request );
  }

  @NonNull
  protected abstract AbstractCompositeResultBuilder createResultBuilder( @NonNull IAuthorizationContext context );
}
