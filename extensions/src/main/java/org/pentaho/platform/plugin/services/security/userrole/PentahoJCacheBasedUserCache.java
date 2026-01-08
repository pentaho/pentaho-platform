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


package org.pentaho.platform.plugin.services.security.userrole;

import org.eclipse.core.runtime.Assert;
import org.springframework.security.core.userdetails.UserCache;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.userdetails.UserDetails;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;
import java.net.URISyntaxException;

public class PentahoJCacheBasedUserCache implements UserCache {

  private static final Log logger = LogFactory.getLog( PentahoJCacheBasedUserCache.class );
  private final Cache<String, UserDetails> userCache;
  private boolean caseSensitive;

  public PentahoJCacheBasedUserCache( boolean caseSensitive ) {

    if ( logger.isDebugEnabled() ) {
      logger.debug( "Cache create with caseSensitive : " + caseSensitive );
    }

    this.caseSensitive = caseSensitive;
    try {
      CachingProvider cachingProvider = Caching.getCachingProvider();
      java.net.URI ehCacheURI = getClass().getClassLoader().getResource( "ehcache.xml" ).toURI();
      CacheManager cacheManager = cachingProvider.getCacheManager( ehCacheURI, cachingProvider.getDefaultClassLoader() );
      userCache = cacheManager.getCache( "userCache" );
    } catch (URISyntaxException e) {
      // This should never happen
      logger.error( "Cache creation failed.", e);
      throw new IllegalStateException( "Failed to initialize user cache", e );
    }
  }

  public void setCaseSensitive ( boolean caseSensitive ) {
    this.caseSensitive = caseSensitive;
  }

  public boolean isCaseSensitive() {
    return caseSensitive;
  }

  @Override public UserDetails getUserFromCache(String username ) {

    if ( logger.isDebugEnabled() ) {
      logger.debug( "Cache get UserDetails for username : " + username );
    }

    if ( isCaseSensitive() ) {
      return userCache.get( username );
    } else {
      for ( Cache.Entry<String, UserDetails> entry : userCache ) {
        String key = entry.getKey();
        if ( key.equalsIgnoreCase( username ) ) {
          return entry.getValue();
        }
      }
    }

    return null;
  }

  @Override public void putUserInCache( UserDetails user ) {
    Assert.isNotNull( user );
    Assert.isNotNull( user.getUsername() );

    if ( logger.isDebugEnabled() ) {
      logger.debug( "Cache put UserDetails for username : " + user.getUsername() );
    }

    userCache.put ( isCaseSensitive() ? user.getUsername() : user.getUsername().toLowerCase(), user );
  }

  @Override public void removeUserFromCache( String username ) {
    Assert.isNotNull( username );
    if ( logger.isDebugEnabled() ) {
      logger.debug( "Cache remove UserDetails for username : " + username );
    }

    userCache.remove( isCaseSensitive() ? username : username.toLowerCase() );
  }
}
