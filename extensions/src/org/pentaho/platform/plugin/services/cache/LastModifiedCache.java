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

import org.hibernate.cache.Cache;
import org.hibernate.cache.CacheException;
import org.pentaho.platform.api.cache.ILastModifiedCacheItem;

import java.util.Date;
import java.util.Map;

/**
 * User: rfellows Date: 10/25/11 Time: 3:53 PM
 */
public class LastModifiedCache implements ILastModifiedCacheItem, Cache {
  private Cache cache;
  private long lastModified;

  public LastModifiedCache( Cache cache ) {
    this.cache = cache;
    setLastModified();
  }

  public LastModifiedCache( Cache cache, long lastModified ) {
    this.cache = cache;
    this.lastModified = lastModified;
  }

  @Override
  public long getLastModified() {
    return lastModified;
  }

  public void setLastModified( long lastModified ) {
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
  public Object read( Object o ) throws CacheException {
    return cache.read( o );
  }

  @Override
  public Object get( Object o ) throws CacheException {
    return cache.get( o );
  }

  @Override
  public void put( Object o, Object o1 ) throws CacheException {
    cache.put( o, o1 );
    setLastModified();
  }

  @Override
  public void update( Object o, Object o1 ) throws CacheException {
    cache.update( o, o1 );
    setLastModified();
  }

  @Override
  public void remove( Object o ) throws CacheException {
    cache.remove( o );
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
  public void lock( Object o ) throws CacheException {
    cache.lock( o );
  }

  @Override
  public void unlock( Object o ) throws CacheException {
    cache.unlock( o );
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
    } catch ( Exception e ) {
      return null;
    }
  }
}
