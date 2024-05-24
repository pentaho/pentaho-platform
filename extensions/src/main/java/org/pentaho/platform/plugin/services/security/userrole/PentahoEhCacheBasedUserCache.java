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

package org.pentaho.platform.plugin.services.security.userrole;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.cache.EhCacheBasedUserCache;

public class PentahoEhCacheBasedUserCache extends EhCacheBasedUserCache {

  private static final Log logger = LogFactory.getLog( PentahoEhCacheBasedUserCache.class );
  private boolean caseSensitive = false;

  public boolean isCaseSensitive() {
    return caseSensitive;
  }

  public void setCaseSensitive( boolean caseSensitive ) {
    this.caseSensitive = caseSensitive;
  }

  @Override public UserDetails getUserFromCache( String username ) {
    return super.getUserFromCache( caseSensitive ? username : username.toLowerCase() );
  }

  @Override public void putUserInCache(UserDetails user) {
    org.ehcache.CacheManager cacheManager = org.ehcache.config.builders.CacheManagerBuilder.newCacheManagerBuilder().build();
    cacheManager.init();

    org.ehcache.Cache<String, org.springframework.security.core.userdetails.UserDetails> cache = cacheManager.createCache("userCache",
            org.ehcache.config.builders.CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, UserDetails.class,
                            org.ehcache.config.builders.ResourcePoolsBuilder.heap(10))
                    .withExpiry(org.ehcache.expiry.Expirations.timeToLiveExpiration(org.ehcache.expiry.Duration.of(20, java.util.concurrent.TimeUnit.SECONDS))));

    String key = caseSensitive ? user.getUsername() : user.getUsername().toLowerCase();
    cache.put(key, user);

    if (logger.isDebugEnabled()) {
      logger.debug("Cache put: " + key);
    }

    cacheManager.close();
  }

  @Override public void removeUserFromCache( String username ) {
    super.removeUserFromCache( caseSensitive ? username : username.toLowerCase() );
  }
}
