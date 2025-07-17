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

import jakarta.persistence.PersistenceException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.MappingException;
import org.hibernate.SessionFactory;
import org.hibernate.cache.internal.EnabledCaching;
import org.hibernate.cache.jcache.internal.JCacheAccessImpl;
import org.hibernate.cache.spi.DirectAccessRegion;
import org.hibernate.cache.spi.RegionFactory;
import org.hibernate.cache.spi.support.StorageAccess;
import org.hibernate.cache.spi.support.TimestampsRegionTemplate;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.pentaho.platform.api.cache.ILastModifiedCacheItem;

import javax.cache.Cache;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * User: rfellows Date: 10/25/11 Time: 3:53 PM
 */
public class LastModifiedCache extends EnabledCaching implements ILastModifiedCacheItem, HvCache {
  private final DirectAccessRegion directAccessRegion;  //Should be an instanceof HvTimestampsRegion
  private long lastModified;

  protected static final Log LOGGER = LogFactory.getLog( LastModifiedCache.class );

  public LastModifiedCache( DirectAccessRegion directAccessRegion, SessionFactory sessionFactory ) {
    this( directAccessRegion, sessionFactory, new Date().getTime() );
  }

  public LastModifiedCache( DirectAccessRegion directAccessRegion, SessionFactory sessionFactory, long lastModified ) {
    super( (SessionFactoryImplementor) sessionFactory );
    this.directAccessRegion = directAccessRegion;
    this.lastModified = lastModified;
  }

  @Override
  public long getLastModified() {
    return lastModified;
  }

  @Override public String getCacheKey() {
    return directAccessRegion.getName();
  }

  public void setLastModified( long lastModified ) {
    this.lastModified = lastModified;
  }

  @SuppressWarnings("unchecked")
  public <T> T unwrap(Class<T> cls) {
    if ( org.hibernate.Cache.class.isAssignableFrom( cls ) ) {
      return (T) this;
    }

    if ( RegionFactory.class.isAssignableFrom( cls ) ) {
      return (T) getRegionFactory();
    }

    throw new PersistenceException( "Hibernate cannot unwrap Cache as " + cls.getName() );
  }

  @Override public Set<Object> getAllKeys() {
    Cache<Object, Object> cache = ( (JCacheAccessImpl) getStorageAccess() ).getUnderlyingCache();
    Set<Object> keys = ConcurrentHashMap.newKeySet();
    if ( cache != null ) {
      cache.forEach(entry -> keys.add( entry.getKey() ) );
    }
    return new HashSet<>( keys );
  }

  @Override public DirectAccessRegion getDirectAccessRegion() {
    return directAccessRegion;
  }

  @Override public StorageAccess getStorageAccess(){
    return ( ( TimestampsRegionTemplate ) directAccessRegion ).getStorageAccess();
  }

  @Override public void evictEntityData( String entityName ) {
    try {
       super.evictEntityData( entityName );
    } catch ( MappingException e ) {
      //Nothing to do if the entry is not there.
    }
  }
}
