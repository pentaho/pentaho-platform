package org.pentaho.platform.api.engine.security.authorization.authng.decisions;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.engine.security.authorization.authng.AuthorizationRequest;

import java.util.Set;

// TODO: Docs
public interface IAuthorizationDecisionFactory {
  @NonNull
  IAuthorizationDecision grant( @NonNull AuthorizationRequest request );

  @NonNull
  IAuthorizationDecision deny( @NonNull AuthorizationRequest request );

  @NonNull
  default IAuthorizationDecision granted( @NonNull AuthorizationRequest request, boolean granted ) {
    return granted ? grant( request ) : deny( request );
  }

  @NonNull
  IAnyAuthorizationDecision anyOf( @NonNull AuthorizationRequest request,
                                   boolean granted,
                                   @NonNull Set<IAuthorizationDecision> decisions );

  @NonNull
  IAllAuthorizationDecision allOf( @NonNull AuthorizationRequest request,
                                   boolean granted,
                                   @NonNull Set<IAuthorizationDecision> decisions );

  @NonNull
  IOpposedAuthorizationDecision opposedTo( @NonNull IAuthorizationDecision opposingToDecision );

  @NonNull
  IImpliedAuthorizationDecision impliedFrom( @NonNull AuthorizationRequest request,
                                             @NonNull IAuthorizationDecision impliedByDecision );
}
