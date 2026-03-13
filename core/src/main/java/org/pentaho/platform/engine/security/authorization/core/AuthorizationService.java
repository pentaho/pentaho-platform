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
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationActionService;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationContext;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationOptions;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRule;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationService;
import org.pentaho.platform.api.engine.security.authorization.decisions.IAuthorizationDecision;
import org.pentaho.platform.api.engine.security.authorization.exceptions.AuthorizationFailureException;
import org.pentaho.platform.engine.security.authorization.core.decisions.DefaultAuthorizationDecision;
import org.pentaho.platform.engine.security.authorization.core.exceptions.AuthorizationRequestCycleException;
import org.pentaho.platform.engine.security.authorization.core.exceptions.AuthorizationRequestUndefinedActionException;
import org.pentaho.platform.engine.security.authorization.core.exceptions.AuthorizationRuleException;
import org.pentaho.platform.engine.security.authorization.core.rules.AbstractAuthorizationRule;
import org.springframework.util.Assert;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.Optional;

/**
 * The {@code AuthorizationService} class is the default implementation of the {@link IAuthorizationService} interface.
 * <p>
 * It performs authorization evaluations based on a root authorization rule, which may delegate to other rules as
 * needed.
 * <p>
 * The implementation is thread-safe, as each authorization evaluation is performed within its own
 * {@link AuthorizationContext}, which tracks the evaluation state for that specific request.
 * Of course, the thread-safety of the overall service also depends on the thread-safety of the provided authorization
 * rules.
 * <p>
 * One exception is setting the root rule, using {@link #setRootRule(IAuthorizationRule)} which is not thread-safe. Care
 * must be taken to avoid inconsistent decisions in concurrent evaluations. This is intended to be used during
 * application initialization.
 */
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
    private final Deque<IAuthorizationRequest> pendingRequests = new ArrayDeque<>();

    @NonNull
    private final IAuthorizationOptions options;

    public AuthorizationContext( @NonNull IAuthorizationOptions options ) {
      Assert.notNull( options, "Argument 'options' is required" );

      this.options = options;
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
    public IAuthorizationDecision authorize( @NonNull IAuthorizationRequest request )
      throws AuthorizationFailureException {

      Assert.notNull( request, "Argument 'request' is required" );

      // Handles logging, error handling and resolving the request's action.

      if ( logger.isDebugEnabled() ) {
        logger.debug( String.format(
          "Authorize BEGIN - request: %s",
          request ) );
      }

      try {
        var decision = authorizeTracked( resolveRequestAction( request ) );

        if ( logger.isDebugEnabled() ) {
          logger.debug( String.format(
            "Authorize END - SUCCESS - request: %s result: %s",
            request,
            decision ) );
        }

        return decision;

      } catch ( AuthorizationFailureException afe ) {
        // Don't log the exception multiple times.
        logger.error( String.format( "Authorize END - ERROR - request: %s. Throwing.", request ) );

        throw afe;
      } catch ( Exception e ) {
        var wrapped = new AuthorizationFailureException( String.format( "Error authorizing request: %s", request ), e );

        logger.error( String.format(
          "Authorize END - ERROR - request: %s. Throwing.",
          request
        ), wrapped );

        throw wrapped;
      }
    }

    @NonNull
    private IAuthorizationDecision authorizeTracked( @NonNull IAuthorizationRequest request )
      throws AuthorizationFailureException {

      if ( pendingRequests.contains( request ) ) {
        throw new AuthorizationRequestCycleException( pendingRequests, request );
      }

      pendingRequests.push( request );
      try {
        return authorizeCore( request );
      } finally {
        pendingRequests.pop();
      }
    }

    @NonNull
    protected IAuthorizationDecision authorizeCore( @NonNull IAuthorizationRequest request ) {
      return authorizeRule( request, getRootRule() )
        .orElseGet( () -> getDefaultDecision( request ) );
    }

    /**
     * Allows overriding of authorization rules before they are evaluated.
     * <p>
     * The default implementation checks the {@link IAuthorizationOptions#getAuthorizationRuleOverrider()} option,
     * and uses it
     * to override the rule if present.
     *
     * @param rule The original authorization rule.
     * @return The overridden authorization rule, or the original rule if no overrider is present
     */
    @NonNull
    protected IAuthorizationRule<? extends IAuthorizationRequest> overrideRule(
      @NonNull IAuthorizationRule<? extends IAuthorizationRequest> rule ) {
      var authorizationRuleOverrider = options.getAuthorizationRuleOverrider();
      if ( authorizationRuleOverrider != null ) {
        var overriddenRule = authorizationRuleOverrider.override( rule );

        if ( logger.isDebugEnabled() && overriddenRule != rule ) {
          logger.debug( String.format( "Overriding rule: %s by: %s", rule, overriddenRule ) );
        }

        return overriddenRule;
      }

      return rule;
    }

    @NonNull
    @Override
    public Optional<IAuthorizationDecision> authorizeRule(
      @NonNull IAuthorizationRequest request,
      @NonNull IAuthorizationRule<? extends IAuthorizationRequest> rule ) {

      rule = overrideRule( rule );

      if ( logger.isDebugEnabled() ) {
        logger.debug( String.format( "AuthorizeRule BEGIN - request: %s rule: %s", request, rule ) );
      }

      try {
        if ( !rule.getRequestType().isAssignableFrom( request.getClass() ) ) {
          if ( logger.isDebugEnabled() ) {
            logger.debug( String.format(
              "AuthorizeRule END - SUCCESS - request: %s rule: %s result: Abstained for request type",
              request,
              rule ) );
          }

          return Optional.empty();
        }

        // Use raw type call since we can't guarantee type safety at runtime
        @SuppressWarnings( "unchecked" )
        IAuthorizationRule<IAuthorizationRequest> rawRule = (IAuthorizationRule<IAuthorizationRequest>) rule;
        Optional<IAuthorizationDecision> result = rawRule.authorize( request, this );

        Objects.requireNonNull( result, "Rule must return a non-null result" );

        if ( logger.isDebugEnabled() ) {
          logger.debug( String.format(
            "AuthorizeRule END - SUCCESS - request: %s rule: %s result: %s",
            request,
            rule,
            result.isEmpty() ? "Abstained" : result.get() ) );
        }

        return result;
      } catch ( AuthorizationFailureException afe ) {
        // Don't log the exception multiple times.
        logger.error( String.format( "AuthorizeRule END - ERROR - request: %s rule: %s. Throwing.", request, rule ) );

        throw afe;
      } catch ( Exception e ) {
        var wrapped = new AuthorizationRuleException( request, rule, e );

        logger.error( String.format(
          "AuthorizeRule END - ERROR - request: %s rule: %s. Throwing.",
          request,
          rule
        ), wrapped );

        throw wrapped;
      }
    }

    @NonNull
    protected IAuthorizationRequest resolveRequestAction( @NonNull IAuthorizationRequest request )
      throws AuthorizationRequestUndefinedActionException {
      // Resolve the action by name.
      var resolvedActionOptional = getActionService().getAction( request.getAction().getName() );
      if ( resolvedActionOptional.isEmpty() ) {
        throw new AuthorizationRequestUndefinedActionException( request );
      }

      // If a different action instance was specified, prefer using the registered one by same name, ensuring that
      // proper metadata is used.
      return resolvedActionOptional.get() != request.getAction()
        ? request.withAction( resolvedActionOptional.get() )
        : request;
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

  /**
   * An authorization rule that always abstains. Used as a default root rule if none is provided.
   */
  private static class AbstainAuthorizationRule extends AbstractAuthorizationRule<IAuthorizationRequest> {

    @NonNull
    @Override
    public Class<IAuthorizationRequest> getRequestType() {
      return IAuthorizationRequest.class;
    }

    @NonNull
    @Override
    public Optional<IAuthorizationDecision> authorize( @NonNull IAuthorizationRequest request,
                                                       @NonNull IAuthorizationContext context ) {
      return abstain();
    }
  }

  @NonNull
  private final IAuthorizationActionService actionService;

  @NonNull
  private IAuthorizationRule<? extends IAuthorizationRequest> rootRule;

  /**
   * Constructs an instance of the authorization service with a default root rule that always abstains.
   *
   * @param actionService The service providing access to authorization actions.
   */
  public AuthorizationService( @NonNull IAuthorizationActionService actionService ) {
    this( actionService, new AbstainAuthorizationRule() );
  }

  /**
   * Constructs an instance of the authorization service with a given root rule.
   *
   * @param actionService The service providing access to authorization actions.
   * @param rootRule      The root authorization rule.
   */
  public AuthorizationService( @NonNull IAuthorizationActionService actionService,
                               @NonNull IAuthorizationRule<? extends IAuthorizationRequest> rootRule ) {
    Assert.notNull( actionService, "Argument 'actionService' is required" );
    Assert.notNull( rootRule, "Argument 'rootRule' is required" );

    this.actionService = actionService;
    this.rootRule = rootRule;
  }

  @NonNull
  @Override
  public IAuthorizationDecision authorize( @NonNull IAuthorizationRequest request,
                                           @NonNull IAuthorizationOptions options )
    throws AuthorizationFailureException {
    return createContext( options ).authorize( request );
  }

  @NonNull
  @Override
  public Optional<IAuthorizationDecision> authorizeRule(
    @NonNull IAuthorizationRequest request,
    @NonNull IAuthorizationRule<? extends IAuthorizationRequest> rule,
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
  public final IAuthorizationRule<? extends IAuthorizationRequest> getRootRule() {
    return rootRule;
  }

  /**
   * Sets the root authorization rule.
   * <p>
   * Warning: changing the root rule at runtime may lead to inconsistent authorization decisions if there are
   * concurrent authorization evaluations. This method should only be used during application initialization.
   *
   * @param rootRule The root rule.
   */
  public final void setRootRule( @NonNull IAuthorizationRule<? extends IAuthorizationRequest> rootRule ) {
    Assert.notNull( rootRule, "Argument 'rootRule' is required" );
    this.rootRule = rootRule;
  }

  /**
   * Gets the authorization action service.
   *
   * @return The action service.
   */
  @NonNull
  protected final IAuthorizationActionService getActionService() {
    return actionService;
  }
}
