package org.pentaho.platform.engine.security.authorization.core.rules;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.engine.security.authorization.AuthorizationDecisionReportingMode;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.decisions.IAuthorizationDecision;
import org.pentaho.platform.api.engine.security.authorization.decisions.ICompositeAuthorizationDecision;
import org.pentaho.platform.engine.security.authorization.core.decisions.AllAuthorizationDecision;

import java.util.Set;

public class AllResultBuilder extends AbstractCompositeResultBuilder {

  public AllResultBuilder( @NonNull AuthorizationDecisionReportingMode reportingMode ) {
    super( reportingMode );
  }

  @NonNull
  @Override
  protected ICompositeAuthorizationDecision createDecision( @NonNull IAuthorizationRequest request,
                                                            boolean isGranted,
                                                            @NonNull Set<IAuthorizationDecision> decisions ) {
    return new AllAuthorizationDecision( request, isGranted, decisions );
  }

  @Override
  protected boolean getSettledGrantedStatus() {
    // Denied
    return false;
  }
}
