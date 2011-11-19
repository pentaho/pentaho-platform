package org.pentaho.platform.plugin.services.cache;

import org.pentaho.platform.api.cache.ICacheExpirationRegistry;
import org.pentaho.platform.api.cache.ILastModifiedCacheItem;

import java.util.*;

/**
 * User: rfellows
 * Date: 10/26/11
 * Time: 9:19 AM
 */
public class CacheExpirationRegistry implements ICacheExpirationRegistry {
  private Map<String, ILastModifiedCacheItem> cacheItems;

  public CacheExpirationRegistry() {
    this.cacheItems = Collections.synchronizedMap(new HashMap<String, ILastModifiedCacheItem>());
  }

  @Override
  public void register(ILastModifiedCacheItem cacheItem) {
    if(cacheItem == null || cacheItem.getCacheKey() == null || cacheItem.getCacheKey().trim().length() == 0) {
      return;
    }
    cacheItems.put(cacheItem.getCacheKey(), cacheItem);
  }

  @Override
  public void unRegister(ILastModifiedCacheItem cacheItem) {
    if(cacheItem == null || cacheItem.getCacheKey() == null || cacheItem.getCacheKey().trim().length() == 0) {
      return;
    }
    cacheItems.remove(cacheItem.getCacheKey());
  }

  @Override
  public List<ILastModifiedCacheItem> getCachedItems() {
    return new ArrayList<ILastModifiedCacheItem>(cacheItems.values());
  }

}
