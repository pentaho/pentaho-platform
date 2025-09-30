package org.pentaho.platform.engine.security.authorization.core.decisions;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.decisions.IAuthorizationDecision;
import org.pentaho.platform.engine.security.messages.Messages;

public class ResourceSpecificAuthorizationDecision extends DerivedAuthorizationDecision {
  private static final String JUSTIFICATION =
    Messages.getInstance().getString( "ResourceSpecificAuthorizationDecision.JUSTIFICATION" );

  public ResourceSpecificAuthorizationDecision( @NonNull IAuthorizationRequest request,
                                                @NonNull IAuthorizationDecision derivedFromDecision ) {
    super( request, derivedFromDecision );
  }

  @NonNull
  @Override
  public String getShortJustification() {
    // Example: "From specific permission"
    return JUSTIFICATION;
  }

  @Override
  public String toString() {
    // Example: "ResourceSpecificAuthorizationDecision[Granted, from: <specific decision>]"
    return String.format(
      "%s[%s, from: %s]",
      getClass().getSimpleName(),
      getGrantedLogText(),
      getDerivedFromDecision() );
  }
}
