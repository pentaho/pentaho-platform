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
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationOptions;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRole;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationUser;
import org.pentaho.platform.api.engine.security.authorization.decisions.IAuthorizationDecision;
import org.pentaho.platform.engine.security.authorization.core.AuthorizationRole;
import org.pentaho.platform.engine.security.authorization.core.decisions.MatchedRoleAuthorizationDecision;
import org.springframework.util.Assert;

import java.util.Optional;

/**
 * The {@code MatchedRoleAuthorizationRule} class represents an authorization rule that matches the
 * {@link IAuthorizationRequest authorization request} against a specific role.
 * <p>
 * More specifically, this rule tests whether the request's {@link IAuthorizationRequest#getPrincipal() principal}
 * matches the specified role in one of two ways:
 * <ul>
 *   <li>If the principal is a user ({@link IAuthorizationUser}), it checks if the role is contained in the user's
 *       {@link IAuthorizationUser#getRoles() roles} collection</li>
 *   <li>If the principal is itself a role ({@link IAuthorizationRole}), it checks if the principal role equals
 *       the target role</li>
 * </ul>
 * <p>
 * The rule grants authorization if either condition is met, and denies it otherwise.
 * In any case, the decision is an instance of {@link MatchedRoleAuthorizationDecision}.
 * <p>
 * If multiple roles are needed, an "Any" composite rule can be created using several instances of this rule, one per
 * role. This approach has the advantage of automatically taking the authorization's
 * {@link IAuthorizationOptions#getDecisionReportingMode() decision reporting mode} option into account for controlling
 * whether to test for the various roles, or if it is enough to test for and report the first one that matches.
 */
public class MatchedRoleAuthorizationRule extends AbstractAuthorizationRule<IAuthorizationRequest> {
  @NonNull
  private final IAuthorizationRole role;

  public MatchedRoleAuthorizationRule( @NonNull String roleName ) {
    this( new AuthorizationRole( roleName ) );
  }

  public MatchedRoleAuthorizationRule( @NonNull IAuthorizationRole role ) {
    Assert.notNull( role, "Argument 'role' is required" );

    this.role = role;
  }

  @NonNull
  @Override
  public Class<IAuthorizationRequest> getRequestType() {
    return IAuthorizationRequest.class;
  }

  /**
   * Authorizes the given request by checking if the principal has the specified role, or if the principal is
   * itself the specified role.
   *
   * @param request The authorization request to evaluate.
   * @param context The authorization context providing additional options.
   * @return An {@link Optional} containing a {@link MatchedRoleAuthorizationDecision} indicating whether the
   *         authorization was granted or denied based on the role match.
   */
  @NonNull
  @Override
  public Optional<IAuthorizationDecision> authorize( @NonNull IAuthorizationRequest request,
                                                     @NonNull IAuthorizationContext context ) {
    boolean hasRole = request.getAllRoles().contains( role );

    return Optional.of( new MatchedRoleAuthorizationDecision( request, hasRole, role ) );
  }

  @Override
  public String toString() {
    return String.format(
      "%s[role=%s]",
      getClass().getSimpleName(),
      role.getName() );
  }
}
