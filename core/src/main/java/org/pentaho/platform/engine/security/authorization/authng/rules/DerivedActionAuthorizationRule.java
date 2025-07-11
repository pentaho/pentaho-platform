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

package org.pentaho.platform.engine.security.authorization.authng.rules;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.engine.IAuthorizationAction;
import org.pentaho.platform.api.engine.security.authorization.authng.AuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.authng.IAuthorizationContext;
import org.pentaho.platform.api.engine.security.authorization.authng.decisions.IAuthorizationDecision;
import org.pentaho.platform.api.engine.security.authorization.authng.exceptions.AuthorizationException;
import org.pentaho.platform.engine.security.authorization.authng.decisions.DerivedActionAuthorizationDecision;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * The {@code DerivedActionAuthorizationRule} class represents an authorization rule that grants permission to perform
 * one or more actions (the derived / implied / consequent ones) based on the existing permission to perform another
 * action (the base / implied-from / implied-by / antecedent one) for a request otherwise equal to the one being
 * authorized.
 * <p>
 * If permission is granted to perform the base action, then permission is also granted to perform any of the derived
 * actions. In all other cases, the rule abstains from making a decision.
 * <p>
 * The decisions taken by this rule are always of type {@link DerivedActionAuthorizationDecision}, having as its
 * {@link DerivedActionAuthorizationDecision#getImpliedFromDecision() implied-from decision} the result of authorizing
 * an equal request but with the base action instead, and as its
 * {@link DerivedActionAuthorizationDecision#getDerivedFromAction() derived-from action} the base action.
 */
public class DerivedActionAuthorizationRule extends AbstractAuthorizationRule {

  @NonNull
  private final IAuthorizationAction baseAction;

  @NonNull
  private final Set<IAuthorizationAction> derivedActions;

  public DerivedActionAuthorizationRule( @NonNull IAuthorizationAction baseAction,
                                         @NonNull IAuthorizationAction derivedAction ) {
    this( baseAction, Set.of( Objects.requireNonNull( derivedAction ) ) );
  }

  public DerivedActionAuthorizationRule( @NonNull IAuthorizationAction baseAction,
                                         @NonNull Set<IAuthorizationAction> derivedActions ) {
    this.baseAction = Objects.requireNonNull( baseAction );
    this.derivedActions = Set.copyOf( derivedActions );
  }

  @NonNull
  @Override
  public Optional<IAuthorizationDecision> authorize( @NonNull AuthorizationRequest request,
                                                     @NonNull IAuthorizationContext context )
    throws AuthorizationException {

    // If not asking about one of the potentially derived actions, abstain.
    if ( !derivedActions.contains( request.getAction() ) ) {
      return Optional.empty();
    }

    // TODO: Consider if we should have an own decision class for this rule, to make it easier to later identify,
    // or if it's enough to use the implied-from decision.

    var baseDecision = context.authorize( request.withAction( baseAction ) );
    return baseDecision.isDenied()
      // If denied for the base action, abstain.
      ? Optional.empty()
      // Else grant for the derived action.
      : Optional.of( new DerivedActionAuthorizationDecision( request, baseDecision ) );
  }
}
