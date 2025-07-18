package org.pentaho.platform.engine.security.authorization.authng.decisions;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.engine.security.authorization.authng.AuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.authng.decisions.IAuthorizationDecision;
import org.pentaho.platform.api.engine.security.authorization.authng.decisions.ICompositeAuthorizationDecision;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractCompositeAuthorizationDecision extends AbstractAuthorizationDecision
  implements ICompositeAuthorizationDecision {

  @NonNull
  private final Set<IAuthorizationDecision> decisions;

  protected AbstractCompositeAuthorizationDecision( @NonNull AuthorizationRequest request,
                                                    boolean granted,
                                                    @NonNull Set<IAuthorizationDecision> decisions ) {
    super( request, granted );
    this.decisions = Collections.unmodifiableSet( decisions );
  }

  @NonNull
  public Set<IAuthorizationDecision> getDecisions() {
    return decisions;
  }

  @NonNull
  protected String getDecisionsLogText() {
    return getDecisions()
      .stream()
      .map( Object::toString )
      .collect( Collectors.joining( ", " ) );
  }
}
