package org.pentaho.platform.security.policy.rolebased;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.engine.security.authorization.authng.AuthorizationDecisionReportingMode;
import org.pentaho.platform.api.engine.security.authorization.authng.AuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.authng.IAuthorizationContext;
import org.pentaho.platform.api.engine.security.authorization.authng.decisions.IAuthorizationDecision;
import org.pentaho.platform.engine.security.authorization.authng.rules.AbstractAuthorizationRule;
import org.springframework.util.Assert;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class GeneralActionRoleBindingAuthorizationRule extends AbstractAuthorizationRule {

  @NonNull
  private final IRoleAuthorizationPolicyRoleBindingDao roleBindingDao;

  public GeneralActionRoleBindingAuthorizationRule( @NonNull IRoleAuthorizationPolicyRoleBindingDao roleBindingDao ) {

    Assert.notNull( roleBindingDao, "Argument 'roleBindingDao' is required" );

    this.roleBindingDao = roleBindingDao;
  }

  @NonNull
  @Override
  public Optional<IAuthorizationDecision> authorize( @NonNull AuthorizationRequest request,
                                                     @NonNull IAuthorizationContext context ) {

    // This rule only applies to general/self actions. Abstain, otherwise.
    if ( !request.getAction().isSelfAction() ) {
      return Optional.empty();
    }

    // Which role(s) actually are bound to the action?
    String actionName = request.getAction().getName();
    AuthorizationDecisionReportingMode decisionReportingMode = context.getOptions().getDecisionReportingMode();

    Set<String> rolesWithBinding = new LinkedHashSet<>();

    for ( String role : request.getUser().getRoles() ) {
      if ( hasRoleActionBinding( role, actionName ) ) {
        rolesWithBinding.add( role );

        if ( decisionReportingMode == AuthorizationDecisionReportingMode.SETTLED ) {
          // If we are in first-match mode, we can stop checking further roles.
          break;
        }
      }
    }

    // Grant or deny, according to empty.
    return Optional.of( new GeneralActionRoleBindingAuthorizationDecision( request, rolesWithBinding ) );
  }

  protected boolean hasRoleActionBinding( @NonNull String role, @NonNull String actionName ) {
    return roleBindingDao.getBoundLogicalRoleNames( List.of( role ) ).contains( actionName );
  }
}
