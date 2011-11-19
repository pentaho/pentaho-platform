package org.pentaho.platform.api.cache;

import java.util.List;

/**
 * This interface defines the API for a registry of <code>ILastModifiedCacheItem</code>'s.
 * All items in the registry will have a last modified timestamp that can be used to
 * determine if a cache item is up-to-date or not
 *
 * @author rfellows
 * @see org.pentaho.platform.api.cache.ILastModifiedCacheItem
 */
public interface ICacheExpirationRegistry {
  /**
   * Adds an item to the registry
   * @param cacheItem
   */
  public void register(ILastModifiedCacheItem cacheItem);

  /**.
   * Removes an item from the registry
   * @param cacheItem
   */
  public void unRegister(ILastModifiedCacheItem cacheItem);

  /**
   * Gets a <code>List</code> of all items registered
   * @return
   */
  public List<ILastModifiedCacheItem> getCachedItems();
}
