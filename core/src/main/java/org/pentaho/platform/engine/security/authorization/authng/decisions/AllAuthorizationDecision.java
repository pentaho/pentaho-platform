package org.pentaho.platform.engine.security.authorization.authng.decisions;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.engine.security.authorization.authng.AuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.authng.decisions.IAllAuthorizationDecision;
import org.pentaho.platform.api.engine.security.authorization.authng.decisions.IAuthorizationDecision;

import java.util.Set;

public class AllAuthorizationDecision extends AbstractCompositeAuthorizationDecision
  implements IAllAuthorizationDecision {

  public AllAuthorizationDecision( @NonNull AuthorizationRequest request,
                                   boolean granted,
                                   @NonNull Set<IAuthorizationDecision> decisions ) {
    super( request, granted, decisions );
  }

  @Override
  public String toString() {
    // Example: "All[Denied, of: <contained decision 1 text>, <contained decision 2 text>]"
    return String.format( "All[%s, of: %s]", getGrantedLogText(), getDecisionsLogText() );
  }
}
