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
import org.pentaho.platform.api.engine.IAuthorizationAction;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationContext;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.decisions.IAuthorizationDecision;
import org.pentaho.platform.engine.security.authorization.core.decisions.DerivedActionAuthorizationDecision;
import org.springframework.util.Assert;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The {@code DerivedActionAuthorizationRule} class represents an authorization rule that grants permission to perform
 * one or more actions (the derived ones) based on the existing permission to perform another action (the base one) for
 * a request otherwise equal to the one being authorized.
 * <p>
 * The rule abstains for requests that do not ask for one of the derived actions. Otherwise, the permission is
 * granted to perform the derived action if there is permission to perform the base action, denying if not.
 * <p>
 * The decisions taken by this rule are always of type {@link DerivedActionAuthorizationDecision}, having as its
 * {@link DerivedActionAuthorizationDecision#getDerivedFromDecision() derived-from decision} the result of authorizing
 * an equal request but with the base action instead, and as its
 * {@link DerivedActionAuthorizationDecision#getDerivedFromAction() derived-from action}, the base action.
 */
public class DerivedActionAuthorizationRule extends AbstractAuthorizationRule<IAuthorizationRequest> {

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
    Assert.notNull( baseAction, "Argument 'baseAction' is required" );

    this.baseAction = baseAction;
    this.derivedActions = Set.copyOf( derivedActions );
  }

  @NonNull
  @Override
  public Class<IAuthorizationRequest> getRequestType() {
    return IAuthorizationRequest.class;
  }

  @NonNull
  @Override
  public Optional<IAuthorizationDecision> authorize( @NonNull IAuthorizationRequest request,
                                                     @NonNull IAuthorizationContext context ) {

    // If not asking about one of the derived actions, abstain.
    if ( !derivedActions.contains( request.getAction() ) ) {
      return abstain();
    }

    var baseRequest = request.withAction( baseAction );
    var baseDecision = context.authorize( baseRequest );
    var derivedDecision = new DerivedActionAuthorizationDecision( request, baseDecision );
    return Optional.of( derivedDecision );
  }

  @Override
  public String toString() {
    return String.format(
      "%s[base=%s, derived=%s]",
      getClass().getSimpleName(),
      baseAction,
      getDerivedActionsLogText()
    );
  }

  @NonNull
  private String getDerivedActionsLogText() {
    return derivedActions
      .stream()
      .map( Objects::toString )
      .collect( Collectors.joining( LIST_SEPARATOR ) );
  }
}
