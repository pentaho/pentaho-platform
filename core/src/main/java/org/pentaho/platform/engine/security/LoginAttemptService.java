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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.pentaho.platform.api.security.ILoginAttemptService;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class LoginAttemptService implements ILoginAttemptService {

  @VisibleForTesting
  protected LoadingCache<String, Integer> attemptsCache;
  @VisibleForTesting
  protected final int maxAttempt;

  public LoginAttemptService( int maxAttempt, int cacheMinutes ) {
    this.maxAttempt = maxAttempt;
    attemptsCache = CacheBuilder.newBuilder().
      expireAfterWrite( cacheMinutes, TimeUnit.MINUTES ).build( new CacheLoader<String, Integer>() {
        public Integer load( String key ) {
          return 0;
        }
      } );
  }

  @Override
  public void loginSucceeded( String key ) {
    attemptsCache.invalidate( key );
  }

  @Override
  public void loginFailed( String key ) {
    int attempts = 0;
    try {
      attempts = attemptsCache.get( key );
    } catch ( ExecutionException e ) {
      attempts = 0;
    }
    attempts++;
    attemptsCache.put( key, attempts );
  }

  @Override
  public boolean isBlocked( String key ) {
    try {
      return attemptsCache.get( key ) >= maxAttempt;
    } catch ( ExecutionException e ) {
      return false;
    }
  }
}
