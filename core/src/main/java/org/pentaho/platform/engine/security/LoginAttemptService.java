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

package org.pentaho.platform.engine.security;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.pentaho.platform.api.security.ILoginAttemptService;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class LoginAttemptService implements ILoginAttemptService {

  protected LoadingCache<String, Integer> attemptsCache;
  protected final int maxAttempt;

  public LoginAttemptService( int maxAttempt, int cacheMinutes ) {
    if ( maxAttempt < 0L ) {
      throw new IllegalArgumentException( "Maximum attempts cannot be negative" );
    }

    if ( cacheMinutes < 0L ) {
      throw new IllegalArgumentException( "Cache minutes cannot be negative" );
    }

    this.maxAttempt = maxAttempt;
    attemptsCache = CacheBuilder.newBuilder().
      expireAfterWrite( cacheMinutes, TimeUnit.MINUTES ).build( new CacheLoader<>() {
        @Override
        public @Nonnull Integer load( @Nonnull String key ) {
          return 0;
        }
      } );
  }

  /**
   * Called when a login attempt succeeds.
   * It clears the failed attempts counter for the key.
   *
   * @param key the key (IP address or username) that successfully logged in.
   */
  @Override
  public void loginSucceeded( String key ) {
    attemptsCache.invalidate( key );
  }

  /**
   * Called when a login attempt fails.
   * It increments the failed attempt counter for the key.
   *
   * @param key the key (IP address or username) that failed to log in.
   */
  @Override
  public void loginFailed( String key ) {
    int attempts;
    try {
      attempts = attemptsCache.get( key );
    } catch ( ExecutionException e ) {
      attempts = 0;
    }

    attemptsCache.put( key, ++attempts );
  }

  /**
   * Checks if the given key is currently blocked due to too many failed login attempts.
   *
   * @param key the key (IP address or username) to check.
   * @return true if the key is blocked, false otherwise.
   */
  @Override
  public boolean isBlocked( String key ) {
    Integer cacheValue = attemptsCache.getIfPresent( key );
    return ( cacheValue != null ) && ( cacheValue >= maxAttempt );
  }

  /**
   * Get all entries in the attempts cache as an unmodifiable Map.
   * Each Map entry represents a key and its corresponding number of failed login attempts.
   *
   * @return an unmodifiable Map of all cache entries
   */
  @Override
  public Map<String, Integer> getAllAttempts() {
    return Collections.unmodifiableMap( attemptsCache.asMap() );
  }

  /**
   * Remove a specific key from the cache.
   * The key will be unblocked whatever its former state.
   *
   * @param key the key to remove.
   */
  @Override
  public void removeFromCache( String key ) {
    attemptsCache.invalidate( key );
  }

  /**
   * Clear all entries from the cache.
   * All keys will be unblocked whatever their former state.
   */
  @Override
  public void clearCache() {
    attemptsCache.invalidateAll();
  }

  /**
   * Get the maximum number of failed login attempts allowed before blocking a key.
   *
   * @return maximum number of failed login attempts allowed before blocking a key.
   */
  @Override
  public int getMaxAttempt() {
    return maxAttempt;
  }
}
