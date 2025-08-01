package org.pentaho.platform.engine.security.authorization.core.rules;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.engine.security.authorization.AuthorizationDecisionReportingMode;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.decisions.IAuthorizationDecision;
import org.pentaho.platform.api.engine.security.authorization.decisions.ICompositeAuthorizationDecision;
import org.pentaho.platform.engine.security.authorization.core.decisions.AnyAuthorizationDecision;

import java.util.Set;

public class AnyResultBuilder extends AbstractCompositeResultBuilder {

  public AnyResultBuilder( @NonNull AuthorizationDecisionReportingMode reportingMode ) {
    super( reportingMode );
  }

  @NonNull
  @Override
  protected ICompositeAuthorizationDecision createDecision( @NonNull IAuthorizationRequest request,
                                                            boolean isGranted,
                                                            @NonNull Set<IAuthorizationDecision> decisions ) {
    return new AnyAuthorizationDecision( request, isGranted, decisions );
  }

  @Override
  protected boolean getSettledGrantedStatus() {
    // Granted
    return true;
  }
}
