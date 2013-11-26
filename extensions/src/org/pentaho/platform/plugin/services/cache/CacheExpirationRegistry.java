/*!
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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.plugin.services.cache;

import org.pentaho.platform.api.cache.ICacheExpirationRegistry;
import org.pentaho.platform.api.cache.ILastModifiedCacheItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: rfellows Date: 10/26/11 Time: 9:19 AM
 */
public class CacheExpirationRegistry implements ICacheExpirationRegistry {
  private Map<String, ILastModifiedCacheItem> cacheItems;

  public CacheExpirationRegistry() {
    this.cacheItems = Collections.synchronizedMap( new HashMap<String, ILastModifiedCacheItem>() );
  }

  @Override
  public void register( ILastModifiedCacheItem cacheItem ) {
    if ( cacheItem == null || cacheItem.getCacheKey() == null || cacheItem.getCacheKey().trim().length() == 0 ) {
      return;
    }
    cacheItems.put( cacheItem.getCacheKey(), cacheItem );
  }

  @Override
  public void unRegister( ILastModifiedCacheItem cacheItem ) {
    if ( cacheItem == null || cacheItem.getCacheKey() == null || cacheItem.getCacheKey().trim().length() == 0 ) {
      return;
    }
    cacheItems.remove( cacheItem.getCacheKey() );
  }

  @Override
  public List<ILastModifiedCacheItem> getCachedItems() {
    return new ArrayList<ILastModifiedCacheItem>( cacheItems.values() );
  }

}
