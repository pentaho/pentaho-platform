package org.pentaho.platform.api.engine.security.authorization.authng;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.engine.security.authorization.authng.decisions.IAuthorizationDecision;
import org.pentaho.platform.api.engine.security.authorization.authng.exceptions.AuthorizationCycleException;
import org.pentaho.platform.api.engine.security.authorization.authng.exceptions.AuthorizationException;
import org.pentaho.platform.api.engine.security.authorization.authng.exceptions.AuthorizationRuleContractException;

/**
 * The {@code IAuthorizationRulesEngine} interface provides top-level authorization operations.
 * <p>
 * The authorization process combines the authorization results of multiple rules. Individual authorization rules can
 * abstain, grant or deny permission.
 */
public interface IAuthorizationEngine {
  /**
   * Authorizes a given authorization request, with specific options.
   *
   * @param request The authorization request.
   * @param options The authorization options.
   * @return The evaluation decision, never {@code null}.
   * @throws AuthorizationCycleException        If an evaluation cycle is detected during the authorization process.
   * @throws AuthorizationRuleContractException If an authorization rule fails to meet the expected contract.
   * @throws AuthorizationException             If an error occurs during the authorization process.
   */
  @NonNull
  IAuthorizationDecision authorize( @NonNull AuthorizationRequest request, @NonNull AuthorizationOptions options )
    throws AuthorizationException;

  // region Sugar
  /**
   * Authorizes a given authorization request, with default options.
   * <p>
   * This method is a convenience method equivalent to calling:
   * {@code instance.authorize( request, AuthorizationOptions.getDefault() )}.
   *
   * @param request The authorization request.
   * @return An {@link IAuthorizationDecision} instance, never {@code null}.
   * @throws AuthorizationCycleException        If an evaluation cycle is detected.
   * @throws AuthorizationRuleContractException If an authorization rule fails to meet the expected contract.
   * @throws AuthorizationException             If an evaluation error occurs.
   */
  @NonNull
  default IAuthorizationDecision authorize( @NonNull AuthorizationRequest request )
    throws AuthorizationException {
    return authorize( request, AuthorizationOptions.getDefault() );
  }
  // endregion
}
