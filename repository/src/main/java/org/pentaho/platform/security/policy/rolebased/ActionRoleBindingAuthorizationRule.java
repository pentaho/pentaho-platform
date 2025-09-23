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

package org.pentaho.platform.security.policy.rolebased;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.engine.security.authorization.AuthorizationDecisionReportingMode;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationContext;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRole;
import org.pentaho.platform.api.engine.security.authorization.decisions.IAuthorizationDecision;
import org.pentaho.platform.api.engine.security.authorization.resources.IResourceAuthorizationRequest;
import org.pentaho.platform.engine.security.authorization.core.rules.AbstractAuthorizationRule;
import org.springframework.util.Assert;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ActionRoleBindingAuthorizationRule extends AbstractAuthorizationRule<IAuthorizationRequest> {

  @NonNull
  private final IRoleAuthorizationPolicyRoleBindingDao roleBindingDao;

  public ActionRoleBindingAuthorizationRule( @NonNull IRoleAuthorizationPolicyRoleBindingDao roleBindingDao ) {

    Assert.notNull( roleBindingDao, "Argument 'roleBindingDao' is required" );

    this.roleBindingDao = roleBindingDao;
  }

  @NonNull
  @Override
  public Class<IAuthorizationRequest> getRequestType() {
    return IAuthorizationRequest.class;
  }

  /**
   * Authorizes the given request by checking if the principal has the necessary role-action bindings
   * for the requested action.
   * <p>
   * If the principal is a user, this rule checks if any of the user's roles are bound to the action being requested.
   * If at least one role is bound to the action, the request is granted; otherwise, it is denied.
   * <p>
   * If the principal is a role, this rule checks if that specific role is bound to the action being requested.
   * If it is, the request is granted; otherwise, it is denied.
   * <p>
   * If the request is a resource-specific authorization request, this rule abstains from making a decision.
   *
   * @param request The authorization request to evaluate.
   * @param context The authorization context providing additional information for decision-making.
   * @return An {@code Optional} containing the authorization decision, or empty if the rule abstains.
   * @throws IllegalArgumentException if the request or context is {@code null}.
   */
  @NonNull
  @Override
  public Optional<IAuthorizationDecision> authorize( @NonNull IAuthorizationRequest request,
                                                     @NonNull IAuthorizationContext context ) {
    // Rule only applies to self or resource-general requests
    if ( request instanceof IResourceAuthorizationRequest ) {
      return abstain();
    }

    // <Role, Action>
    // Which role(s) actually are bound to the action?
    String actionName = request.getAction().getName();
    AuthorizationDecisionReportingMode decisionReportingMode = context.getOptions().getDecisionReportingMode();

    Set<IAuthorizationRole> rolesWithBinding = new LinkedHashSet<>();

    for ( IAuthorizationRole role : request.getAllRoles() ) {
      if ( hasRoleActionBinding( role, actionName ) ) {
        rolesWithBinding.add( role );

        if ( decisionReportingMode == AuthorizationDecisionReportingMode.SETTLED ) {
          // If we are in first-match mode, we can stop checking further roles.
          break;
        }
      }
    }

    // Grant or deny (denies when empty set).
    return Optional.of( new ActionRoleBindingAuthorizationDecision( request, rolesWithBinding ) );
  }

  private boolean hasRoleActionBinding( @NonNull IAuthorizationRole role, @NonNull String actionName ) {
    return roleBindingDao.getBoundLogicalRoleNames( List.of( role.getName() ) ).contains( actionName );
  }
}
