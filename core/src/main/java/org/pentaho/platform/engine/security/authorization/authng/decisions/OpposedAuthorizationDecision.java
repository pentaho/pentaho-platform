package org.pentaho.platform.engine.security.authorization.authng.decisions;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.engine.security.authorization.authng.decisions.IAuthorizationDecision;
import org.pentaho.platform.api.engine.security.authorization.authng.decisions.IOpposedAuthorizationDecision;
import org.pentaho.platform.engine.security.messages.Messages;

import java.text.MessageFormat;

public class OpposedAuthorizationDecision extends AbstractAuthorizationDecision
  implements IOpposedAuthorizationDecision {

  private static final String OPPOSED_TO_JUSTIFICATION =
    Messages.getInstance().getString( "AuthorizationDecisionFactory.OPPOSED_TO_JUSTIFICATION" );

  @NonNull
  private final IAuthorizationDecision opposedToDecision;

  public OpposedAuthorizationDecision( @NonNull IAuthorizationDecision opposedToDecision ) {
    // Negate the granted state of the opposed decision.
    super( opposedToDecision.getRequest(), !opposedToDecision.isGranted() );

    this.opposedToDecision = opposedToDecision;
  }

  @NonNull
  @Override
  public IAuthorizationDecision getOpposedToDecision() {
    return opposedToDecision;
  }

  @Override
  public String getShortJustification() {
    // Example: "Opposing: <opposed decision justification>"
    return MessageFormat.format( OPPOSED_TO_JUSTIFICATION, opposedToDecision );
  }

  @Override
  public String toString() {
    // Example: "Opposed(Granted, to: DerivedFromAction[Denied, ...])"
    return String.format( "Opposed[%s, to: %s]", getGrantedLogText(), opposedToDecision );
  }
}
