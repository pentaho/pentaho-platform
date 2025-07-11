/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.platform.engine.security.authorization.authng;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.engine.security.authorization.authng.AuthorizationOptions;
import org.pentaho.platform.api.engine.security.authorization.authng.AuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.authng.IAuthorizationContext;
import org.pentaho.platform.api.engine.security.authorization.authng.IAuthorizationEngine;
import org.pentaho.platform.api.engine.security.authorization.authng.IAuthorizationRule;
import org.pentaho.platform.api.engine.security.authorization.authng.decisions.IAuthorizationDecision;
import org.pentaho.platform.api.engine.security.authorization.authng.exceptions.AuthorizationCycleException;
import org.pentaho.platform.api.engine.security.authorization.authng.exceptions.AuthorizationException;
import org.pentaho.platform.engine.security.authorization.authng.rules.AnyAuthorizationRule;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

import static org.pentaho.platform.engine.security.authorization.authng.decisions.AuthorizationDecisions.deny;

public class AuthorizationEngine implements IAuthorizationEngine {
  /**
   * The {@code AuthorizationEvaluationContext} represents a single authorization evaluation process.
   * It holds the authorization evaluation request, as well as the options for the evaluation.
   * Additionally, it tracks the evaluation path, to detect cycles in the evaluation process.
   * <p>
   * For authorization rules implementations, the context provides evaluation methods allowing rules to base their
   * decisions on the results of the evaluation for other users/actions.
   * <p>
   * The context design also allows for the engine itself to be thread-safe, as each evaluation context is independent.
   */
  protected class AuthorizationContext implements IAuthorizationContext {

    @NonNull
    private final Deque<AuthorizationRequest> evaluationPath = new ArrayDeque<>();

    @NonNull
    private final AuthorizationOptions options;

    public AuthorizationContext( @NonNull AuthorizationOptions options ) {
      this.options = Objects.requireNonNull( options );
    }

    @NonNull
    @Override
    public AuthorizationOptions getOptions() {
      return options;
    }

    @NonNull
    @Override
    public IAuthorizationEngine getEngine() {
      return AuthorizationEngine.this;
    }

    @NonNull
    @Override
    public IAuthorizationDecision authorize( @NonNull AuthorizationRequest request ) throws AuthorizationException {
      Objects.requireNonNull( request );

      if ( evaluationPath.contains( request ) ) {
        throw new AuthorizationCycleException( evaluationPath, request );
      }

      evaluationPath.push( request );
      try {
        return getRootRule()
          .authorize( request, this )
          .orElseGet( () -> getDefaultDecision( request ) );
      } finally {
        evaluationPath.pop();
      }
    }

    /**
     * Gets the default decision for when all rules abstain, or no rules are defined.
     * <p>
     * The default implementation returns a denied decision.
     *
     * @param request The authorization request.
     * @return The default decision.
     */
    @NonNull
    protected IAuthorizationDecision getDefaultDecision( @NonNull AuthorizationRequest request ) {
      return deny( request );
    }
  }

  @NonNull
  private final IAuthorizationRule rootRule;

  /***
   * Constructs an instance of the authorization engine with a given root rule.
   *
   * @param rootRule The root authorization rule.
   */
  public AuthorizationEngine( @NonNull IAuthorizationRule rootRule ) {
    this.rootRule = Objects.requireNonNull( rootRule );
  }

  /**
   * Constructs an instance of the authorization engine with a list of authorization rules combined using an "any-of"
   * logic.
   *
   * @param anyOfRules The list of rules.
   */
  public AuthorizationEngine( @NonNull List<IAuthorizationRule> anyOfRules ) {
    this( new AnyAuthorizationRule( anyOfRules ) );
  }

  @NonNull
  @Override
  public IAuthorizationDecision authorize( @NonNull AuthorizationRequest request,
                                           @NonNull AuthorizationOptions options )
    throws AuthorizationException {
    return createContext( options ).authorize( request );
  }

  /**
   * Creates a new authorization context with given options.
   * <p>
   * Inheritors may override this method to provide a custom context implementation.
   *
   * @param options The options of the authorization context.
   * @return The new authorization context.
   */
  @NonNull
  protected AuthorizationContext createContext( @NonNull AuthorizationOptions options ) {
    return new AuthorizationContext( options );
  }

  /**
   * Gets the root authorization rule.
   *
   * @return The root rule.
   */
  @NonNull
  protected final IAuthorizationRule getRootRule() {
    return rootRule;
  }
}
