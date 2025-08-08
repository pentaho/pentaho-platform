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
   */
  @NonNull
  IAuthorizationDecision authorize( @NonNull IAuthorizationRequest request, @NonNull IAuthorizationOptions options );

  /**
   * Authorizes a given authorization request, with default options.
   * <p>
   * This method is a convenience method equivalent to calling:
   * {@code instance.authorize( request, AuthorizationOptions.getDefault() )}.
   *
   * @param request The authorization request.
   * @return An {@link IAuthorizationDecision} instance, never {@code null}.
   */
  @NonNull
  default IAuthorizationDecision authorize( @NonNull IAuthorizationRequest request ) {
    return authorize( request, IAuthorizationOptions.getDefault() );
  }

  /**
   * Authorizes a given authorization request using a specific authorization rule and options.
   * <p>
   * This method can be used to know the result of a known rule's authorization of a request, possibly to compare it
   * against the overall authorization decision taking all rules into account, obtained via
   * {@link #authorize(IAuthorizationRequest, IAuthorizationOptions)}.
   *
   * @param request The authorization request.
   * @param rule    The authorization rule to evaluate.
   * @param options The authorization options.
   * @return An optional authorization decision; an empty optional, if the rule abstains from making a decision.
   */
  @NonNull
  Optional<IAuthorizationDecision> authorizeRule( @NonNull IAuthorizationRequest request,
                                                  @NonNull IAuthorizationRule rule,
                                                  @NonNull IAuthorizationOptions options );
}
