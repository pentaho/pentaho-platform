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

import net.sf.ehcache.Ehcache;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.MappingException;
import org.hibernate.SessionFactory;
import org.hibernate.cache.ehcache.internal.StorageAccessImpl;
import org.hibernate.cache.spi.DirectAccessRegion;
import org.hibernate.cache.spi.RegionFactory;
import org.hibernate.cache.spi.access.EntityDataAccess;
import org.hibernate.cache.spi.access.NaturalIdDataAccess;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.metamodel.model.domain.NavigableRole;
import org.hibernate.metamodel.spi.MetamodelImplementor;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.pretty.MessageHelper;
import org.pentaho.platform.api.cache.ILastModifiedCacheItem;
import javax.persistence.PersistenceException;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * User: rfellows Date: 10/25/11 Time: 3:53 PM
 */
public class LastModifiedCache implements ILastModifiedCacheItem, HvCache {
  private DirectAccessRegion directAccessRegion;  //Should be an instanceof HvTimestampsRegion
  private SessionFactoryImplementor sessionFactory;
  private RegionFactory regionFactory;

  private long lastModified;
  protected static final Log LOGGER = LogFactory.getLog( LastModifiedCache.class );

  public LastModifiedCache( DirectAccessRegion directAccessRegion, SessionFactory sessionFactory ) {
    setInstanceVariables( directAccessRegion, sessionFactory );
    setLastModified();
  }

  public LastModifiedCache( DirectAccessRegion directAccessRegion, SessionFactory sessionFactory, long lastModified ) {
    setInstanceVariables( directAccessRegion, sessionFactory );
    this.lastModified = lastModified;
  }

  private void setInstanceVariables( DirectAccessRegion directAccessRegion, SessionFactory sessionFactory ) {
    this.directAccessRegion = directAccessRegion;
    this.sessionFactory = (SessionFactoryImplementor) sessionFactory;
    this.regionFactory =
      getSessionFactory().getSessionFactoryOptions().getServiceRegistry().getService( RegionFactory.class );
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

  protected void setLastModified() {
    this.lastModified = new Date().getTime();
  }

  @Override public SessionFactoryImplementor getSessionFactory() {
    return sessionFactory;
  }

  @Override public boolean containsEntity( Class entityClass, Serializable identifier ) {
    return this.containsEntity(entityClass.getName(), identifier);
  }

  @Override public boolean containsEntity( String entityName, Serializable identifier ) {
    EntityPersister entityDescriptor = this.sessionFactory.getMetamodel().entityPersister(entityName);
    EntityDataAccess cacheAccess = entityDescriptor.getCacheAccessStrategy();
    if (cacheAccess == null) {
      return false;
    } else {
      Object key = cacheAccess.generateCacheKey(identifier, entityDescriptor, this.sessionFactory, (String) null);
      return cacheAccess.contains(key);
    }
  }

  @Override public void evictEntityData( Class entityClass, Serializable identifier ) {
    evictEntityData( entityClass.getName(), identifier );
  }

  @Override public void evictEntityData( String entityName, Serializable identifier ) {
    final EntityPersister entityDescriptor = sessionFactory.getMetamodel().entityPersister( entityName );
    final EntityDataAccess cacheAccess = entityDescriptor.getCacheAccessStrategy();
    if ( cacheAccess == null ) {
      return;
    }

    if ( LOGGER.isDebugEnabled() ) {
      LOGGER.debug( String.format( "Evicting second-level cache: %s",
        MessageHelper.infoString( entityDescriptor, identifier, sessionFactory ) ) );
    }

    final Object key = cacheAccess.generateCacheKey( identifier, entityDescriptor, sessionFactory, null );
    cacheAccess.evict( key );
  }

  @Override public void evictEntityData( Class entityClass ) {
    evictEntityData( entityClass.getName() );
  }

  @Override public void evictEntityData( String entityName ) {
    try {
      evictEntityData( getSessionFactory().getMetamodel().entityPersister( entityName ) );
    } catch ( MappingException e) {
      //Nothing to do if the entry is not there.
    }
  }

  @Override public void evictEntityData() {
    throwNotImplemented();
  }

  @Override
  public void evictNaturalIdData( Class entityClass ) {
    throwNotImplemented();
  }

  @Override
  public void evictNaturalIdData( String entityName ) {
    throwNotImplemented();
  }

  @Override
  public void evictNaturalIdData() {
    throwNotImplemented();
  }

  private void evictNaturalIdData(NavigableRole rootEntityRole, NaturalIdDataAccess cacheAccess) {
    throwNotImplemented();
  }

  @Override
  public boolean containsCollection(String role, Serializable ownerIdentifier) {
    throwNotImplemented();
    return false;
  }

  @Override
  public void evictCollectionData(String role, Serializable ownerIdentifier) {
    throwNotImplemented();
    }

  @Override
  public void evictCollectionData(String role) {
    throwNotImplemented();
  }

  private void evictCollectionData( CollectionPersister collectionDescriptor) {
    throwNotImplemented();
  }

  @Override public void evictCollectionData() {
    throwNotImplemented();
  }

  @Override public boolean containsQuery( String regionName ) {
    throwNotImplemented();
    return false;
  }

  @Override public void evictDefaultQueryRegion() {
    throwNotImplemented();
  }

  @Override public void evictQueryRegion( String regionName ) {
    throwNotImplemented();
  }

  @Override public void evictQueryRegions() {
    throwNotImplemented();
  }

  @Override public void evictRegion( String regionName ) {
    throwNotImplemented();
  }

  @Override
  public boolean contains(Class cls, Object primaryKey) {
    // JPA
    return containsEntity( cls, (Serializable) primaryKey );
  }

  @Override
  public void evict(Class cls, Object primaryKey) {
    // JPA call
    evictEntityData( cls, (Serializable) primaryKey );
  }

  @Override public void evict( Class aClass ) {
    // JPA
    evictEntityData( aClass );
  }

  @SuppressWarnings("unchecked")
  public <T> T unwrap(Class<T> cls) {
    if ( org.hibernate.Cache.class.isAssignableFrom( cls ) ) {
      return (T) this;
    }

    if ( RegionFactory.class.isAssignableFrom( cls ) ) {
      return (T) regionFactory;
    }

    throw new PersistenceException( "Hibernate cannot unwrap Cache as " + cls.getName() );
  }

  @Override public Set getAllKeys() {
    Ehcache ehcache = getStorageAccess().getCache();
    return new HashSet( ehcache.getKeys() );
  }

  @Override public DirectAccessRegion getDirectAccessRegion() {
    return directAccessRegion;
  }

  private void evictEntityData( EntityPersister entityDescriptor ) {
    EntityPersister rootEntityDescriptor = entityDescriptor;
    if ( entityDescriptor.isInherited()
      && !entityDescriptor.getEntityName().equals( entityDescriptor.getRootEntityName() ) ) {
      rootEntityDescriptor = ( (MetamodelImplementor) getSessionFactory().getMetamodel() ).locateEntityPersister(
        entityDescriptor.getRootEntityName() );
    }

    evictEntityData( rootEntityDescriptor.getNavigableRole(), rootEntityDescriptor.getCacheAccessStrategy() );

  }

  private void evictEntityData( NavigableRole navigableRole, EntityDataAccess cacheAccess) {
    if ( cacheAccess == null ) {
      return;
    }

    if ( LOGGER.isDebugEnabled() ) {
      LOGGER.debug( String.format( "Evicting entity cache: %s", navigableRole.getFullPath() ) );
    }

    cacheAccess.evictAll();
  }

  @Override public StorageAccessImpl getStorageAccess(){
    return ( (HvTimestampsRegion) directAccessRegion ).getStorageAccess();
  }

  @Override public Ehcache getCache() {
    return getStorageAccess().getCache();
  }

  private void throwNotImplemented(){
    throw new NotImplementedException( "Method not Implemented with upgrade to hibernate 5.4.24");
  }
}
