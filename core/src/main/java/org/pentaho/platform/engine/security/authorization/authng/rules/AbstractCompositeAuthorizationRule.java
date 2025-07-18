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

package org.pentaho.platform.engine.security.authorization.authng.rules;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.security.authorization.authng.AuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.authng.IAuthorizationContext;
import org.pentaho.platform.api.engine.security.authorization.authng.IAuthorizationRule;
import org.pentaho.platform.api.engine.security.authorization.authng.decisions.IAuthorizationDecision;
import org.pentaho.platform.api.engine.security.authorization.authng.decisions.IAuthorizationDecisionFactory;
import org.pentaho.platform.api.engine.security.authorization.authng.exceptions.AuthorizationException;
import org.pentaho.platform.engine.security.authorization.authng.decisions.AuthorizationDecisions;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public abstract class AbstractCompositeAuthorizationRule extends AbstractAuthorizationRule
  implements IAuthorizationRule {

  private static final Log logger = LogFactory.getLog( AbstractCompositeAuthorizationRule.class );

  @NonNull
  private final IAuthorizationDecisionFactory decisionFactory;

  @NonNull
  private final List<IAuthorizationRule> rules;

  public AbstractCompositeAuthorizationRule( @NonNull List<IAuthorizationRule> rules ) {
    this( rules, AuthorizationDecisions.getFactory() );
  }

  public AbstractCompositeAuthorizationRule( @NonNull List<IAuthorizationRule> rules,
                                             @NonNull IAuthorizationDecisionFactory decisionFactory ) {
    this.rules = List.copyOf( Objects.requireNonNull( rules ) );
    this.decisionFactory = Objects.requireNonNull( decisionFactory );
  }

  @NonNull
  public List<IAuthorizationRule> getRules() {
    return rules;
  }

  @NonNull
  protected IAuthorizationDecisionFactory getDecisionFactory() {
    return decisionFactory;
  }

  @NonNull
  @Override
  public Optional<IAuthorizationDecision> authorize( @NonNull AuthorizationRequest request,
                                                     @NonNull IAuthorizationContext context )
    throws AuthorizationException {

    AbstractCompositeResultBuilder resultBuilder = createResultBuilder( context );

    for ( IAuthorizationRule rule : getRules() ) {

      Optional<IAuthorizationDecision> ruleResult = authorizeRule( rule, request, context );
      if ( ruleResult.isPresent() ) {
        resultBuilder.withDecision( ruleResult.get() );

        if ( resultBuilder.isImmutable() ) {
          // If the decision is immutable, no need to evaluate other rules.
          break;
        }
      }
    }

    return resultBuilder.build( request );
  }

  @NonNull
  protected abstract AbstractCompositeResultBuilder createResultBuilder( @NonNull IAuthorizationContext context );

  @NonNull
  protected Optional<IAuthorizationDecision> authorizeRule( @NonNull IAuthorizationRule rule,
                                                            @NonNull AuthorizationRequest request,
                                                            @NonNull IAuthorizationContext context )
    throws AuthorizationException {

    try {
      Optional<IAuthorizationDecision> result = rule.authorize( request, context );

      if ( logger.isDebugEnabled() ) {
        logger.debug( String.format(
          "Rule '%s' authorize: %s, result: %s",
          rule,
          request,
          result.isEmpty() ? "abstained" : result.get() ) );
      }

      return result;

    } catch ( AuthorizationException e ) {
      // This exception may be the AuthorizationCycleException, thrown by the context itself.
      // Or it may be a more specific exception, thrown by the rule.
      // Any unchecked exceptions are never caught, are considered unrecoverable from, and will cause an overall
      // failure of the evaluation.

      // TODO: Consider defining an engine setting that controls whether to log and skip the rule, or to throw.

      // Throw back the exception. Log it with an error level.
      logger.error( String.format(
        "Rule '%s' failed authorize: %s. Interrupting evaluation.",
        rule,
        request
      ), e );

      throw e;
    }
  }
}
