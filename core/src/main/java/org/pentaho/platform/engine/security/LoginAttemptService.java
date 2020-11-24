/*!
 *
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 *
 * Copyright (c) 2020 Hitachi Vantara. All rights reserved.
 *
 */


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
