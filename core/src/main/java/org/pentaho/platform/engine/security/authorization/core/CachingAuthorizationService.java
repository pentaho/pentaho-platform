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
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationOptions;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRule;
import org.pentaho.platform.api.engine.security.authorization.caching.IAuthorizationDecisionCache;
import org.pentaho.platform.api.engine.security.authorization.decisions.IAuthorizationDecision;
import org.pentaho.platform.api.engine.security.authorization.exceptions.AuthorizationFailureException;
import org.springframework.util.Assert;

public class CachingAuthorizationService extends AuthorizationService {
  private static final Log logger = LogFactory.getLog( CachingAuthorizationService.class );

  @NonNull
  private final IAuthorizationDecisionCache decisionCache;

  /**
   * Constructs an instance of the authorization service with a default root rule that always abstains.
   *
   * @param actionService The service providing access to authorization actions.
   * @param decisionCache The cache for authorization decisions.
   */
  public CachingAuthorizationService( @NonNull IAuthorizationActionService actionService,
                                      @NonNull IAuthorizationDecisionCache decisionCache ) {
    super( actionService );

    Assert.notNull( decisionCache, "Argument 'decisionCache' is required" );

    this.decisionCache = decisionCache;
  }

  /**
   * Constructs an instance of the authorization service with a given root rule.
   *
   * @param actionService The service providing access to authorization actions.
   * @param rootRule      The root authorization rule.
   * @param decisionCache The cache for authorization decisions.
   */
  public CachingAuthorizationService( @NonNull IAuthorizationActionService actionService,
                                      @NonNull IAuthorizationRule<? extends IAuthorizationRequest> rootRule,
                                      @NonNull IAuthorizationDecisionCache decisionCache ) {
    super( actionService, rootRule );

    Assert.notNull( decisionCache, "Argument 'decisionCache' is required" );

    this.decisionCache = decisionCache;
  }

  private class CachingAuthorizationContext extends AuthorizationContext {
    public CachingAuthorizationContext( @NonNull IAuthorizationOptions options ) {
      super( options );
    }

    @NonNull
    @Override
    protected IAuthorizationDecision authorizeCore( @NonNull IAuthorizationRequest request ) {
      return decisionCache.get( request, getOptions(),
        key -> super.authorizeCore( request ) );
    }
  }

  @NonNull
  @Override
  public IAuthorizationDecision authorize( @NonNull IAuthorizationRequest request,
                                           @NonNull IAuthorizationOptions options )
    throws AuthorizationFailureException {

    IAuthorizationDecision decision = super.authorize( request, options );

    // Don't really know at this point if recordStats is on. Logging only for trace level makes
    // it less bad if disabled. When disabled, will just log 0s as stats.
    // Also, this method is just one of the top-level authorization methods (authorizeRule is not logging stats).
    if ( logger.isTraceEnabled() ) {
      logger.trace( decisionCache );
    }

    return decision;
  }

  /**
   * Determines whether an authorization request with given options should use caching.
   *
   * @param options The authorization options.
   * @return {@code true}, if caching should be used; {@code false}, otherwise
   */
  protected boolean shouldUseCache( @NonNull IAuthorizationOptions options ) {
    return options.getAuthorizationRuleOverrider() == null;
  }

  @NonNull
  @Override
  protected AuthorizationContext createContext( @NonNull IAuthorizationOptions options ) {
    return shouldUseCache( options ) ? new CachingAuthorizationContext( options ) : super.createContext( options );
  }
}
