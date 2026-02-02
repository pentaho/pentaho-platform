package org.pentaho.platform.engine.security.authorization.core.decisions;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.decisions.IAuthorizationDecision;
import org.pentaho.platform.engine.security.messages.Messages;

public class ResourceActionGeneralRequirementAuthorizationDecision extends DerivedAuthorizationDecision {
  private static final String GRANTED_JUSTIFICATION =
    Messages.getInstance().getString( "ResourceActionGeneralRequirementAuthorizationDecision.JUSTIFICATION" );
  private static final String DENIED_JUSTIFICATION =
    Messages.getInstance().getString( "ResourceActionGeneralRequirementAuthorizationDecision.Denied.JUSTIFICATION" );

  public ResourceActionGeneralRequirementAuthorizationDecision( @NonNull
                                                                IAuthorizationRequest request,
                                                                @NonNull
                                                                IAuthorizationDecision derivedFromDecision ) {
    super( request, derivedFromDecision );
  }

  @NonNull
  @Override
  public String getShortJustification( boolean granted ) {
    // Example: "From general permission"
    return granted ? GRANTED_JUSTIFICATION : DENIED_JUSTIFICATION;
  }

  @Override
  public String toString() {
    // Example: "ResourceActionGeneralRequirementAuthorizationDecision[Granted, from: <general decision>]"
    return String.format(
      "%s[%s, from: %s]",
      getClass().getSimpleName(),
      getGrantedLogText(),
      getDerivedFromDecision() );
  }
}
