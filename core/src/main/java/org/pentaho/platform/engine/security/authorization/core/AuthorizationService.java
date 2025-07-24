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

package org.pentaho.platform.engine.security.authorization.core;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationContext;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationOptions;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRule;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationService;
import org.pentaho.platform.api.engine.security.authorization.decisions.IAuthorizationDecision;
import org.pentaho.platform.engine.security.authorization.core.decisions.CycleAuthorizationDecision;
import org.pentaho.platform.engine.security.authorization.core.decisions.DefaultAuthorizationDecision;
import org.pentaho.platform.engine.security.authorization.core.rules.AnyAuthorizationRule;
import org.springframework.util.Assert;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class AuthorizationService implements IAuthorizationService {

  private static final Log logger = LogFactory.getLog( AuthorizationService.class );

  /**
   * The {@code AuthorizationEvaluationContext} represents a single authorization evaluation process.
   * It holds the authorization evaluation request, as well as the options for the evaluation.
   * Additionally, it tracks the evaluation path, to detect cycles in the evaluation process.
   * <p>
   * For authorization rules implementations, the context provides evaluation methods allowing rules to base their
   * decisions on the results of the evaluation for other users/actions.
   * <p>
   * The context design also allows for the service itself to be thread-safe, as each evaluation context is independent.
   */
  protected class AuthorizationContext implements IAuthorizationContext {

    @NonNull
    private final Deque<IAuthorizationRequest> evaluationPath = new ArrayDeque<>();

    @NonNull
    private final IAuthorizationOptions options;

    public AuthorizationContext( @NonNull IAuthorizationOptions options ) {
      this.options = Objects.requireNonNull( options );
    }

    @NonNull
    @Override
    public IAuthorizationOptions getOptions() {
      return options;
    }

    @NonNull
    @Override
    public IAuthorizationService getService() {
      return AuthorizationService.this;
    }

    @NonNull
    @Override
    public IAuthorizationDecision authorize( @NonNull IAuthorizationRequest request ) {
      Objects.requireNonNull( request );

      if ( logger.isDebugEnabled() ) {
        logger.debug( String.format(
          "Authorize BEGIN - request: %s",
          request ) );
      }

      if ( evaluationPath.contains( request ) ) {
        var cycleDecision = new CycleAuthorizationDecision( request, evaluationPath );

        if ( logger.isErrorEnabled() ) {
          logger.error( String.format( "Authorize END - CYCLE - %s", cycleDecision ) );
        }

        return cycleDecision;
      }

      evaluationPath.push( request );
      try {
        return authorizeRule( request, getRootRule() )
          .orElseGet( () -> getDefaultDecision( request ) );
      } finally {
        evaluationPath.pop();

        if ( logger.isDebugEnabled() ) {
          logger.debug( String.format(
            "Authorize END - request: %s",
            request ) );
        }
      }
    }

    @NonNull
    public Optional<IAuthorizationDecision> authorizeRule( @NonNull IAuthorizationRequest request,
                                                           @NonNull IAuthorizationRule rule ) {
      if ( logger.isDebugEnabled() ) {
        logger.debug( String.format(
          "AuthorizeRule BEGIN - request: %s rule: %s",
          request,
          rule ) );
      }

      try {
        Optional<IAuthorizationDecision> result = rule.authorize( request, this );

        Objects.requireNonNull( result, "Rule must return a non-null result" );

        if ( logger.isDebugEnabled() ) {
          logger.debug( String.format(
            "AuthorizeRule END - SUCCESS - request: %s rule: %s result: %s",
            request,
            rule,
            result.isEmpty() ? "Abstained" : result.get() ) );
        }

        return result;
      } catch ( Exception e ) {

        logger.error( String.format(
          "AuthorizeRule END - ERROR - request: %s rule: %s. Abstaining.",
          request,
          rule
        ), e );

        // Abstention for a failed rule is like pretending the rule did not exist.
        // TODO: Change to error decision?
        return Optional.empty();
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
    protected IAuthorizationDecision getDefaultDecision( @NonNull IAuthorizationRequest request ) {
      return new DefaultAuthorizationDecision( request, false );
    }
  }

  @NonNull
  private final IAuthorizationRule rootRule;

  /***
   * Constructs an instance of the authorization service with a given root rule.
   *
   * @param rootRule The root authorization rule.
   */
  public AuthorizationService( @NonNull IAuthorizationRule rootRule ) {
    Assert.notNull( rootRule, "Argument 'rootRule' is required" );

    this.rootRule = rootRule;
  }

  /**
   * Constructs an instance of the authorization service with a list of authorization rules combined using an "any-of"
   * logic.
   *
   * @param anyOfRules The list of rules.
   */
  public AuthorizationService( @NonNull List<IAuthorizationRule> anyOfRules ) {
    this( new AnyAuthorizationRule( anyOfRules ) );
  }

  @NonNull
  @Override
  public IAuthorizationDecision authorize( @NonNull IAuthorizationRequest request,
                                           @NonNull IAuthorizationOptions options ) {
    return createContext( options ).authorize( request );
  }

  @NonNull
  @Override
  public Optional<IAuthorizationDecision> authorizeRule( @NonNull IAuthorizationRequest request,
                                                         @NonNull IAuthorizationRule rule,
                                                         @NonNull IAuthorizationOptions options ) {
    return createContext( options ).authorizeRule( request, rule );
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
  protected AuthorizationContext createContext( @NonNull IAuthorizationOptions options ) {
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
