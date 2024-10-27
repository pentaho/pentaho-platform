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

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.springframework.security.core.userdetails.UserCache;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.userdetails.UserDetails;

public class PentahoEhCacheBasedUserCache implements UserCache {

  private static final Log logger = LogFactory.getLog( PentahoEhCacheBasedUserCache.class );
  private CacheManager cacheManager;
  private Cache<String, UserDetails> userCache;
  private boolean caseSensitive = false;

  public void setCacheManager ( CacheManager cacheManager ) {
    this.cacheManager = cacheManager;
   }

   public void setUserCache ( Cache<String, UserDetails> userCache ) {
    this.userCache = userCache;
  }
  public void setCaseSensitive ( boolean caseSensitive ) {
    this.caseSensitive = caseSensitive;
  }

  public PentahoEhCacheBasedUserCache( boolean caseSensitive ) {
    this.caseSensitive = caseSensitive;
    cacheManager = CacheManagerBuilder.newCacheManagerBuilder ().build ( true );
    userCache = cacheManager.createCache ( "userCache", CacheConfigurationBuilder.newCacheConfigurationBuilder ( String.class, UserDetails.class, ResourcePoolsBuilder.heap ( 10 ) ) );

  }

  @Override public UserDetails getUserFromCache( String username ) {
    if ( caseSensitive ) {
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
    userCache.put (user.getUsername (), user);
  }

  @Override public void removeUserFromCache( String username ) {
      userCache.remove( username );
  }
}
