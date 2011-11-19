package org.pentaho.platform.plugin.services.cache;

import org.hibernate.cache.Cache;
import org.hibernate.cache.CacheException;
import org.pentaho.platform.api.cache.ILastModifiedCacheItem;

import java.util.Date;
import java.util.Map;

/**
 * User: rfellows
 * Date: 10/25/11
 * Time: 3:53 PM
 */
public class LastModifiedCache implements ILastModifiedCacheItem, Cache {
  private Cache cache;
  private long lastModified;

  public LastModifiedCache(Cache cache) {
    this.cache = cache;
    setLastModified();
  }
  public LastModifiedCache(Cache cache, long lastModified) {
    this.cache = cache;
    this.lastModified = lastModified;
  }

  @Override
  public long getLastModified() {
    return lastModified;
  }

  public void setLastModified(long lastModified) {
    this.lastModified = lastModified;
  }

  protected void setLastModified() {
    this.lastModified = new Date().getTime();
  }

  @Override
  public String getCacheKey() {
    return cache.getRegionName();
  }

  @Override
  public Object read(Object o) throws CacheException {
    return cache.read(o);
  }

  @Override
  public Object get(Object o) throws CacheException {
    return cache.get(o);
  }

  @Override
  public void put(Object o, Object o1) throws CacheException {
    cache.put(o, o1);
    setLastModified();
  }

  @Override
  public void update(Object o, Object o1) throws CacheException {
    cache.update(o, o1);
    setLastModified();
  }

  @Override
  public void remove(Object o) throws CacheException {
    cache.remove(o);
    setLastModified();
  }

  @Override
  public void clear() throws CacheException {
    cache.clear();
    setLastModified();
  }

  @Override
  public void destroy() throws CacheException {
    cache.destroy();
    setLastModified();
  }

  @Override
  public void lock(Object o) throws CacheException {
    cache.lock(o);
  }

  @Override
  public void unlock(Object o) throws CacheException {
    cache.unlock(o);
  }

  @Override
  public long nextTimestamp() {
    return cache.nextTimestamp();
  }

  @Override
  public int getTimeout() {
    return cache.getTimeout();
  }

  @Override
  public String getRegionName() {
    return cache.getRegionName();
  }

  @Override
  public long getSizeInMemory() {
    return cache.getSizeInMemory();
  }

  @Override
  public long getElementCountInMemory() {
    return cache.getElementCountInMemory();
  }

  @Override
  public long getElementCountOnDisk() {
    return cache.getElementCountOnDisk();
  }

  @Override
  public Map toMap() {
    try {
      return cache.toMap();
    } catch(Exception e) {
      return null;
    }
  }
}
