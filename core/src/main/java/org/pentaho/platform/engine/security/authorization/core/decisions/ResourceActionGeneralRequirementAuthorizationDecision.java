package org.pentaho.platform.engine.security.authorization.core.decisions;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.decisions.IAuthorizationDecision;
import org.pentaho.platform.engine.security.messages.Messages;

public class ResourceActionGeneralRequirementAuthorizationDecision extends ImpliedAuthorizationDecision {
  private static final String JUSTIFICATION =
    Messages.getInstance().getString( "ResourceActionGeneralRequirementAuthorizationDecision.JUSTIFICATION" );

  public ResourceActionGeneralRequirementAuthorizationDecision( @NonNull
                                                                IAuthorizationRequest request,
                                                                @NonNull
                                                                IAuthorizationDecision impliedFromDecision ) {
    super( request, impliedFromDecision );
  }

  @NonNull
  @Override
  public String getShortJustification() {
    // Example: "From general permission"
    return JUSTIFICATION;
  }

  @Override
  public String toString() {
    // Example: "ResourceActionGeneralRequirementAuthorizationDecision[Granted, from: <general decision>]"
    return String.format(
      "%s[%s, from: %s]",
      getClass().getSimpleName(),
      getGrantedLogText(),
      getImpliedFromDecision() );
  }
}
