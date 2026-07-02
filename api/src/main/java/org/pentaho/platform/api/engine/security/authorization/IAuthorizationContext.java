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

package org.pentaho.platform.api.engine.security.authorization;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.engine.security.authorization.decisions.IAuthorizationDecision;
import org.pentaho.platform.api.engine.security.authorization.exceptions.AuthorizationFailureException;

import java.util.Optional;

/**
 * The {@code IAuthorizationContext} interface represents a single authorization process.
 * <p>
 * An authorization process, and the corresponding context, are initiated by calling one of the authorization service's
 * authorization methods, such as {@link IAuthorizationService#authorize(IAuthorizationRequest, IAuthorizationOptions)}.
 * <p>
 * The authorization context holds the specific options, and the owning authorization service.
 * Notably, it offers authorization rules the ability to safely and conveniently perform sub-authorizations that affect
 * their own decision, using the same options and in a way that protects against cycles in the process. The context
 * tracks authorization requests to detect and prevent cycles in the evaluation process.
 * <p>
 * The service/context separation aids with the service being thread-safe, as each authorization's context is
 * independent,
 * and each authorization is executed in a single thread.
 */
public interface IAuthorizationContext {
  /**
   * Gets the options of the authorization context.
   *
   * @return The authorization options.
   */
  @NonNull
  IAuthorizationOptions getOptions();

  /**
   * Gets the authorization service that owns this context.
   *
   * @return The owning authorization service.
   */
  @NonNull
  IAuthorizationService getService();

  /**
   * Authorizes a given authorization request.
   * <p>
   * This method performs a <i>sub-authorization</i> on this context. It uses the same options specified in the
   * top-level authorization request.
   *
   * @param request The authorization request.
   * @return The evaluation decision, never {@code null}.
   * @throws AuthorizationFailureException When the authorization process fails.
   */
  @NonNull
  IAuthorizationDecision authorize( @NonNull IAuthorizationRequest request ) throws AuthorizationFailureException;

  /**
   * Authorizes a given authorization request using a specific rule.
   * <p>
   * This method should be used by authorization rules which evaluate other rules as part of their decision-making
   * process, instead of calling the rules'
   * {@link IAuthorizationRule#authorize(IAuthorizationRequest, IAuthorizationContext)} method directly. This allows for
   * centralized logging and error handling.
   *
   * @param request The authorization request.
   * @param rule    The authorization rule to evaluate.
   * @return An empty optional, for abstaining from the decision; an optional with a decision object, for granting or
   * denying the authorization request.
   * @throws AuthorizationFailureException When the authorization process fails.
   */
  @NonNull
  Optional<IAuthorizationDecision> authorizeRule( @NonNull IAuthorizationRequest request,
                                                  @NonNull IAuthorizationRule<? extends IAuthorizationRequest> rule )
    throws AuthorizationFailureException;
}
