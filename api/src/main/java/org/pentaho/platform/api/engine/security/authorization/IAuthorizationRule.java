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

import java.util.Optional;

/**
 * The {@code IAuthorizationRule} interface defines a rule for evaluating whether a user can perform a specific action.
 * Authorization rules provide a means to distribute the authorization logic across different components.
 * The authorization service combines the results of multiple authorization rules to determine if a user is
 * authorized to perform an action.
 * <p>
 * Implementations of this interface should be thread-safe.
 * <p>
 * Implementations should override the {@link Object#toString()} method to provide a meaningful description of the rule,
 * appropriate for including in exception messages, and for logging and debugging purposes.
 *
 * @param <T> The specific type of authorization request this rule can handle, must extend {@link IAuthorizationRequest}.
 */
public interface IAuthorizationRule<T extends IAuthorizationRequest> {
  /**
   * Gets the specific type of authorization request this rule can handle.
   * <p>
   * Other requests that do not match this type will not be processed by this rule,
   * resulting, in practice, in an abstention from the decision.
   *
   * @return The class type of the authorization request this rule processes.
   */
  @NonNull
  Class<T> getRequestType();

  /**
   * Authorizes a given authorization request under a given authorization context.
   *
   * @param request The authorization request.
   * @param context The authorization context.
   * @return An empty optional, for abstaining from the decision; an optional with a decision object, for granting or
   *         denying the authorization request.
   */
  @NonNull
  Optional<IAuthorizationDecision> authorize( @NonNull T request,
                                              @NonNull IAuthorizationContext context );
}
