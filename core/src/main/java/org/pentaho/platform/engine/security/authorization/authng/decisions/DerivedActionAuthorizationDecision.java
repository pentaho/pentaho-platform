package org.pentaho.platform.engine.security.authorization.authng.decisions;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.engine.IAuthorizationAction;
import org.pentaho.platform.api.engine.security.authorization.authng.AuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.authng.decisions.IAuthorizationDecision;

/**
 * The {@code DerivedActionAuthorizationDecision} class represents an authorization decision that is derived from
 * the authorization decision for another action.
 */
public class DerivedActionAuthorizationDecision extends ImpliedAuthorizationDecision {

  public DerivedActionAuthorizationDecision( @NonNull
                                             AuthorizationRequest request,
                                             @NonNull
                                             IAuthorizationDecision impliedFromDecision ) {
    super( request, impliedFromDecision );
  }

  /**
   * Gets the action that this decision is derived from.
   *
   * @return The action that this decision is derived from.
   */
  @NonNull
  public IAuthorizationAction getDerivedFromAction() {
    return getImpliedFromDecision().getRequest().getAction();
  }
}
