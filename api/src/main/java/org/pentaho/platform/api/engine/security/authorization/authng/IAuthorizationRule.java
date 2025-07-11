package org.pentaho.platform.api.engine.security.authorization.authng;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.engine.security.authorization.authng.decisions.IAuthorizationDecision;
import org.pentaho.platform.api.engine.security.authorization.authng.exceptions.AuthorizationException;

import java.util.Optional;

/**
 * The {@code IAuthorizationRule} interface defines a rule for evaluating whether a user can perform a specific action.
 * Authorization rules provide a means to distribute the authorization logic across different components.
 * The authorization engine combines the results of multiple authorization rules to determine if a user is authorized to
 * perform an action.
 * <p>
 * Implementations of this interface should be thread-safe.
 * <p>
 * Implementations should override the {@link Object#toString()} method to provide a meaningful description of the rule,
 * appropriate for including in exception messages, and for logging and debugging purposes.
 */
@FunctionalInterface
public interface IAuthorizationRule {
  /**
   * Authorizes a given authorization request under a given authorization context.
   *
   * @param request The authorization request.
   * @param context The authorization context.
   * @return An empty optional, for abstaining from the decision; an optional with a decision object, for granting or
   *         denying the authorization request.
   * @throws AuthorizationException If an error occurs during the authorization process.
   */
  @NonNull
  Optional<IAuthorizationDecision> authorize( @NonNull AuthorizationRequest request,
                                              @NonNull IAuthorizationContext context )
    throws AuthorizationException;
}
