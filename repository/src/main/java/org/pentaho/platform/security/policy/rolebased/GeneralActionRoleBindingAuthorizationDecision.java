package org.pentaho.platform.security.policy.rolebased;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRequest;
import org.pentaho.platform.engine.security.authorization.core.decisions.AbstractAuthorizationDecision;

import java.util.Set;

public class GeneralActionRoleBindingAuthorizationDecision extends AbstractAuthorizationDecision {

  @NonNull
  private final Set<String> boundRoles;

  public GeneralActionRoleBindingAuthorizationDecision( @NonNull IAuthorizationRequest request, Set<String> boundRoles ) {
    super( request, !boundRoles.isEmpty() );
    this.boundRoles = boundRoles;
  }

  @NonNull
  public Set<String> getBoundRoles() {
    return boundRoles;
  }
}
