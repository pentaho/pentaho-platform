package org.pentaho.platform.security.policy.rolebased;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.engine.security.authorization.AuthorizationDecisionReportingMode;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationContext;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.decisions.IAuthorizationDecision;
import org.pentaho.platform.engine.security.authorization.core.rules.AbstractSelfOrResourceGeneralActionAuthorizationRule;
import org.springframework.util.Assert;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class GeneralActionRoleBindingAuthorizationRule extends AbstractSelfOrResourceGeneralActionAuthorizationRule {

  @NonNull
  private final IRoleAuthorizationPolicyRoleBindingDao roleBindingDao;

  public GeneralActionRoleBindingAuthorizationRule( @NonNull IRoleAuthorizationPolicyRoleBindingDao roleBindingDao ) {

    Assert.notNull( roleBindingDao, "Argument 'roleBindingDao' is required" );

    this.roleBindingDao = roleBindingDao;
  }

  @NonNull
  @Override
  protected Optional<IAuthorizationDecision> authorizeCore( @NonNull IAuthorizationRequest request,
                                                            @NonNull IAuthorizationContext context ) {

    // <Role, Action>
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

    // Grant or deny, if empty set.
    return Optional.of( new GeneralActionRoleBindingAuthorizationDecision( request, rolesWithBinding ) );
  }

  protected boolean hasRoleActionBinding( @NonNull String role, @NonNull String actionName ) {
    return roleBindingDao.getBoundLogicalRoleNames( List.of( role ) ).contains( actionName );
  }
}
