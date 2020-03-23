package org.pentaho.platform.engine.security;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.pentaho.platform.api.security.ILoginAttemptService;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class LoginAttemptService implements ILoginAttemptService {

  private LoadingCache<String, Integer> attemptsCache;
  private final int maxAttempt;
  private final int cacheMinutes;


  public LoginAttemptService( int maxAttempt, int cacheMinutes ) {
    this.maxAttempt = maxAttempt;
    this.cacheMinutes = cacheMinutes;
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
