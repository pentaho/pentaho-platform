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

/**
 * The {@code AuthorizationDecisions} class provides static utility methods to create instances of various authorization
 * decisions.
 * <p>
 * The static methods roughly mirror those of the {@link IAuthorizationDecisionFactory} interface, allowing for easy
 * use of this basic functionality, possibly via static imports. For more advanced uses, or for more control or
 * modularity, an instance of {@link IAuthorizationDecisionFactory} can be used directly.
 * <p>
 * This class uses a singleton instance of {@link AuthorizationDecisionFactory} class to create decisions, which can be
 * obtained via {@link #getFactory()}.
 */
public class AuthorizationDecisions {

  private AuthorizationDecisions() {
    // static utility class.
  }

  private static final IAuthorizationDecisionFactory decisionFactory = new AuthorizationDecisionFactory();

  @NonNull
  public static IAuthorizationDecisionFactory getFactory() {
    return decisionFactory;
  }

  @NonNull
  public static IAuthorizationDecision grant( @NonNull AuthorizationRequest request ) {
    return decisionFactory.grant( request );
  }

  @NonNull
  public static IAuthorizationDecision deny( @NonNull AuthorizationRequest request ) {
    return decisionFactory.deny( request );
  }

  @NonNull
  public static IAuthorizationDecision granted( @NonNull AuthorizationRequest request, boolean granted ) {
    return decisionFactory.granted( request, granted );
  }

  @NonNull
  public static IAnyAuthorizationDecision anyOf( @NonNull AuthorizationRequest request,
                                                 boolean granted,
                                                 @NonNull Set<IAuthorizationDecision> decisions ) {
    return decisionFactory.anyOf( request, granted, decisions );
  }

  @NonNull
  public static IAllAuthorizationDecision allOf( @NonNull AuthorizationRequest request,
                                                 boolean granted,
                                                 @NonNull Set<IAuthorizationDecision> decisions ) {
    return decisionFactory.allOf( request, granted, decisions );
  }

  @NonNull
  public static IOpposedAuthorizationDecision opposedTo( @NonNull IAuthorizationDecision opposedToDecision ) {
    return decisionFactory.opposedTo( opposedToDecision );
  }

  @NonNull
  public static IImpliedAuthorizationDecision impliedFrom( @NonNull AuthorizationRequest request,
                                                           @NonNull IAuthorizationDecision impliedByDecision ) {
    return decisionFactory.impliedFrom( request, impliedByDecision );
  }
}
