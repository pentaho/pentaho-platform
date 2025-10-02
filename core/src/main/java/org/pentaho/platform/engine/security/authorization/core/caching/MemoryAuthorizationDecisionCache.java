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

package org.pentaho.platform.engine.security.authorization.core.caching;

import com.google.common.base.Objects;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.UncheckedExecutionException;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationOptions;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.caching.IAuthorizationDecisionCache;
import org.pentaho.platform.api.engine.security.authorization.caching.IAuthorizationDecisionCacheKey;
import org.pentaho.platform.api.engine.security.authorization.decisions.IAuthorizationDecision;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;

public class MemoryAuthorizationDecisionCache implements IAuthorizationDecisionCache {
  @NonNull
  private final Cache<IAuthorizationDecisionCacheKey, IAuthorizationDecision> cache;

  public MemoryAuthorizationDecisionCache(
    long expireAfterWrite,
    long maximumSize,
    boolean recordStats ) {

    final var cacheBuilder = CacheBuilder.newBuilder()
      .expireAfterWrite( expireAfterWrite, TimeUnit.SECONDS )
      .maximumSize( maximumSize );

    if ( recordStats ) {
      cacheBuilder.recordStats();
    }

    cache = cacheBuilder.build();
  }

  @NonNull
  protected IAuthorizationDecisionCacheKey createKey( @NonNull IAuthorizationRequest request,
                                                      @NonNull IAuthorizationOptions options ) {
    return new AuthorizationDecisionCacheKey( request, options );
  }

  @NonNull
  @Override
  public Optional<IAuthorizationDecision> get( @NonNull IAuthorizationRequest request,
                                               @NonNull IAuthorizationOptions options ) {
    return Optional.ofNullable( cache.getIfPresent( createKey( request, options ) ) );
  }

  @NonNull
  @Override
  public IAuthorizationDecision get( @NonNull IAuthorizationRequest request,
                                     @NonNull IAuthorizationOptions options,
                                     @NonNull
                                     Function<IAuthorizationDecisionCacheKey, IAuthorizationDecision> loader ) {
    try {
      final var key = createKey( request, options );
      return cache.get( key, () -> loader.apply( key ) );
    } catch ( ExecutionException e ) {
      throw new UncheckedExecutionException( e );
    }
  }

  @Override
  public void put( @NonNull IAuthorizationRequest request,
                   @NonNull IAuthorizationOptions options,
                   @NonNull IAuthorizationDecision decision ) {
    cache.put( createKey( request, options ), decision );
  }

  @Override
  public void invalidate( @NonNull IAuthorizationRequest request, @NonNull IAuthorizationOptions options ) {
    cache.invalidate( createKey( request, options ) );
  }

  @Override
  public void invalidateAll( @NonNull Predicate<IAuthorizationDecisionCacheKey> predicate ) {

    var invalidateRequests = cache
      .asMap()
      .keySet()
      .stream()
      .filter( predicate )
      .toList();

    cache.invalidateAll( invalidateRequests );
  }

  @Override
  public void invalidateAll() {
    cache.invalidateAll();
  }

  @Override
  public String toString() {
    return String.format( "MemoryAuthorizationDecisionCache[stats=%s]", cache.stats() );
  }

  private static class AuthorizationDecisionCacheKey implements IAuthorizationDecisionCacheKey {
    @NonNull
    private final IAuthorizationRequest request;
    @NonNull
    private final IAuthorizationOptions options;

    public AuthorizationDecisionCacheKey( @NonNull IAuthorizationRequest request,
                                          @NonNull IAuthorizationOptions options ) {
      this.request = request;
      this.options = options;
    }

    @NonNull
    @Override
    public IAuthorizationRequest getRequest() {
      return request;
    }

    @NonNull
    @Override
    public IAuthorizationOptions getOptions() {
      return options;
    }

    @Override
    public boolean equals( Object o ) {
      if ( this == o ) {
        return true;
      }

      if ( !( o instanceof IAuthorizationDecisionCacheKey that ) ) {
        return false;
      }

      return request.equals( that.getRequest() ) && options.equals( that.getOptions() );
    }

    @Override
    public int hashCode() {
      return Objects.hashCode( request, options );
    }

    @Override
    public String toString() {
      return String.format( "AuthorizationDecisionCacheKey[request=%s, options=%s]", request, options );
    }
  }
}
