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
 * The {@code IAuthorizationService} interface provides top-level authorization operations.
 * <p>
 * The authorization process combines the authorization results of multiple rules. Individual authorization rules can
 * abstain, grant or deny permission.
 */
public interface IAuthorizationService {
  /**
   * Authorizes a given authorization request, with specific options.
   *
   * @param request The authorization request.
   * @param options The authorization options.
   * @return The evaluation decision, never {@code null}.
   * @throws AuthorizationFailureException When the authorization process fails.
   */
  @NonNull
  IAuthorizationDecision authorize( @NonNull IAuthorizationRequest request, @NonNull IAuthorizationOptions options )
    throws AuthorizationFailureException;

  /**
   * Authorizes a given authorization request, with default options.
   * <p>
   * This method is a convenience method equivalent to calling:
   * {@code instance.authorize( request, AuthorizationOptions.getDefault() )}.
   *
   * @param request The authorization request.
   * @return An {@link IAuthorizationDecision} instance, never {@code null}.
   * @throws AuthorizationFailureException When the authorization process fails.
   */
  @NonNull
  default IAuthorizationDecision authorize( @NonNull IAuthorizationRequest request )
    throws AuthorizationFailureException {
    return authorize( request, IAuthorizationOptions.getDefault() );
  }

  /**
   * Authorizes a given authorization request using a specific rule, with specific options.
   *
   * @param request The authorization request.
   * @param rule The authorization rule to evaluate.
   * @param options The authorization options.
   * @return An empty optional, for abstaining from the decision; an optional with a decision object, for granting or
   *         denying the authorization request.
   * @throws AuthorizationFailureException When the authorization process fails.
   */
  @NonNull
  Optional<IAuthorizationDecision> authorizeRule( @NonNull IAuthorizationRequest request,
                                                  @NonNull IAuthorizationRule<? extends IAuthorizationRequest> rule,
                                                  @NonNull IAuthorizationOptions options )
    throws AuthorizationFailureException;
}
