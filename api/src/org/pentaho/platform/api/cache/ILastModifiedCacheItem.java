package org.pentaho.platform.api.cache;

/**
 * API for cached items that contain a last modified timestamp
 * @author rfellows
 */
public interface ILastModifiedCacheItem {
  /**
   * Get the timestamp of the last time this cache item was modified
   * @return
   */
  public long getLastModified();

  /**
   * Get the lookup key for this cache item
   * @return
   */
  public String getCacheKey();
}
