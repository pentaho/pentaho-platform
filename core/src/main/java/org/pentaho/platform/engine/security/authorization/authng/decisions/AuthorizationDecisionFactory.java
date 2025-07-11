package org.pentaho.platform.engine.security.authorization.authng.decisions;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.engine.security.authorization.authng.AuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.authng.decisions.IAllAuthorizationDecision;
import org.pentaho.platform.api.engine.security.authorization.authng.decisions.IAnyAuthorizationDecision;
import org.pentaho.platform.api.engine.security.authorization.authng.decisions.IAuthorizationDecision;
import org.pentaho.platform.api.engine.security.authorization.authng.decisions.IAuthorizationDecisionFactory;
import org.pentaho.platform.api.engine.security.authorization.authng.decisions.IImpliedAuthorizationDecision;
import org.pentaho.platform.api.engine.security.authorization.authng.decisions.IOpposedAuthorizationDecision;

import java.util.Set;

// TODO: Docs
public class AuthorizationDecisionFactory implements IAuthorizationDecisionFactory {
  @NonNull
  @Override
  public IAuthorizationDecision grant( @NonNull AuthorizationRequest request ) {
    return new DefaultAuthorizationDecision( request, true );
  }

  @NonNull
  @Override
  public IAuthorizationDecision deny( @NonNull AuthorizationRequest request ) {
    return new DefaultAuthorizationDecision( request, false );
  }

  @NonNull
  @Override
  public IAnyAuthorizationDecision anyOf( @NonNull AuthorizationRequest request,
                                          boolean granted,
                                          @NonNull Set<IAuthorizationDecision> decisions ) {
    return new AnyAuthorizationDecision( request, granted, decisions );
  }

  @NonNull
  @Override
  public IAllAuthorizationDecision allOf( @NonNull AuthorizationRequest request,
                                          boolean granted,
                                          @NonNull Set<IAuthorizationDecision> decisions ) {
    return new AllAuthorizationDecision( request, granted, decisions );
  }

  @NonNull
  @Override
  public IOpposedAuthorizationDecision opposedTo( @NonNull IAuthorizationDecision opposedToDecision ) {
    return new OpposedAuthorizationDecision( opposedToDecision );
  }

  @NonNull
  @Override
  public IImpliedAuthorizationDecision impliedFrom( @NonNull AuthorizationRequest request,
                                                    @NonNull IAuthorizationDecision impliedByDecision ) {
    return new ImpliedAuthorizationDecision( request, impliedByDecision );
  }
}
