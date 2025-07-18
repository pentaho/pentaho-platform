package org.pentaho.platform.engine.security.authorization.authng.decisions;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.engine.security.authorization.authng.AuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.authng.decisions.IAnyAuthorizationDecision;
import org.pentaho.platform.api.engine.security.authorization.authng.decisions.IAuthorizationDecision;

import java.util.Set;

public class AnyAuthorizationDecision extends AbstractCompositeAuthorizationDecision
  implements IAnyAuthorizationDecision {

  public AnyAuthorizationDecision( @NonNull AuthorizationRequest request,
                                   boolean granted,
                                   @NonNull Set<IAuthorizationDecision> decisions ) {
    super( request, granted, decisions );
  }

  @Override
  public String toString() {
    // Example: "Any[Granted, of: <contained decision 1 text>, <contained decision 2 text>]"
    return String.format( "Any[%s, of: %s]", getGrantedLogText(), getDecisionsLogText() );
  }
}
