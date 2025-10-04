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
import org.springframework.util.Assert;

public class CachingAuthorizationService extends AuthorizationService {
  private static final Log logger = LogFactory.getLog( CachingAuthorizationService.class );

  @NonNull
  private final IAuthorizationDecisionCache decisionCache;

  /**
   * Constructs an instance of the authorization service with a given root rule.
   *
   * @param actionService The service providing access to authorization actions.
   * @param rootRule      The root authorization rule.
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
                                           @NonNull IAuthorizationOptions options ) {
    IAuthorizationDecision decision = super.authorize( request, options );

    if ( logger.isDebugEnabled() ) {
      logger.debug( decisionCache );
    }

    return decision;
  }

  @NonNull
  @Override
  protected AuthorizationContext createContext( @NonNull IAuthorizationOptions options ) {
    return new CachingAuthorizationContext( options );
  }
}
