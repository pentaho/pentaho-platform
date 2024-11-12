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
