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

package org.pentaho.platform.api.engine.security.authorization.caching;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationOptions;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.decisions.IAuthorizationDecision;
import org.pentaho.platform.api.engine.security.authorization.exceptions.AuthorizationFailureException;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * The {@code IAuthorizationDecisionCache} interface represents a cache for authorization decisions,
 * and which follows the "loading cache" pattern.
 */
public interface IAuthorizationDecisionCache {
  /**
   * Gets a cached authorization decision for a specific authorization request and options, if available.
   *
   * @param request The authorization request.
   * @param options The authorization options.
   * @return An optional with the cached decision, if found; an empty optional, if not found.
   */
  @NonNull
  Optional<IAuthorizationDecision> get( @NonNull IAuthorizationRequest request,
                                        @NonNull IAuthorizationOptions options );

  /**
   * Gets a cached authorization decision for a specific authorization request and options,
   * loading it from the given loader function and storing it in the cache, if not available.
   *
   * @param request The authorization request.
   * @param options The authorization options.
   * @param loader  A function that computes the authorization decision if it is not found in the cache.
   * @return The authorization decision.
   * @throws AuthorizationFailureException When the loader function fails to compute the decision.
   */
  @NonNull
  IAuthorizationDecision get( @NonNull IAuthorizationRequest request,
                              @NonNull IAuthorizationOptions options,
                              @NonNull Function<IAuthorizationDecisionCacheKey, IAuthorizationDecision> loader )
    throws AuthorizationFailureException;

  /**
   * Caches an authorization decision for a specific authorization request and options.
   * <p>
   * This operation is a no-op if the cache is disabled.
   *
   * @param request  The authorization request.
   * @param options The authorization options.
   * @param decision The authorization decision to cache.
   */
  void put( @NonNull IAuthorizationRequest request,
            @NonNull IAuthorizationOptions options,
            @NonNull IAuthorizationDecision decision );

  /**
   * Clears the cached authorization decision for a specific authorization request and options, if any.
   *
   * @param request The authorization request.
   * @param options The authorization options.
   */
  void invalidate( @NonNull IAuthorizationRequest request, @NonNull IAuthorizationOptions options );

  /**
   * Clears all cached authorization decisions for requests and options that match the given predicate.
   *
   * @param predicate A predicate that matches authorization requests and options to clear from the cache.
   */
  void invalidateAll( @NonNull Predicate<IAuthorizationDecisionCacheKey> predicate );

  /**
   * Clears all cached authorization decisions.
   * <p>
   * This operation is a no-op if the cache is disabled.
   */
  void invalidateAll();
}
