/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.platform.plugin.services.cache;

import net.sf.ehcache.Ehcache;
import org.hibernate.cache.ehcache.internal.StorageAccessImpl;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.internal.SessionImpl;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CacheManagerNullEntryTest {

  private CacheManager cacheManager;
  private StorageAccessImpl storageAccess;
  private Ehcache ehcache;

  @Before
  public void setUp() {
    cacheManager = new CacheManager();
    LastModifiedCache region = mock( LastModifiedCache.class );
    storageAccess = mock( StorageAccessImpl.class );
    ehcache = mock( Ehcache.class );
    SessionFactoryImplementor sessionFactory = mock( SessionFactoryImplementor.class );
    when( region.getStorageAccess() ).thenReturn( storageAccess );
    when( region.getSessionFactory() ).thenReturn( sessionFactory );
    when( sessionFactory.openSession() ).thenReturn( mock( SessionImpl.class ) );
    when( storageAccess.getCache() ).thenReturn( ehcache );
    when( ehcache.getKeys() ).thenReturn( Collections.emptyList() );

    Map<String, Object> regions = new HashMap<>();
    regions.put( ICacheManager.SESSION, region );
    regions.put( "region", region );
    ReflectionTestUtils.setField( cacheManager, "regionCache", regions );
    ReflectionTestUtils.setField( cacheManager, "cacheEnabled", true );
  }

  private void cacheContains( Map<Object, Object> map ) {
    when( ehcache.getAll( any() ) ).thenReturn( (Map) map );
  }

  private Map<Object, Object> mapWithNullValue() {
    Map<Object, Object> map = new HashMap<>();
    map.put( "a", "va" );
    map.put( "b", null );
    return map;
  }

  @Test
  public void getAllValuesIgnoresNullValues() {
    cacheContains( mapWithNullValue() );

    List values = cacheManager.getAllValuesFromRegionCache( "region" );

    assertEquals( Arrays.asList( "va" ), values );
  }

  @Test
  public void getAllEntriesIgnoresNullValues() {
    cacheContains( mapWithNullValue() );

    Set entries = cacheManager.getAllEntriesFromRegionCache( "region" );

    assertEquals( 1, entries.size() );
    assertTrue( entries.contains( "va" ) );
  }

  @Test
  public void killSessionCacheEvictsOnlyMatchingSession() {
    Map<Object, Object> map = new HashMap<>();
    map.put( "s1\ta", "v" );
    map.put( "s2\ta", "v" );
    cacheContains( map );
    IPentahoSession session = mock( IPentahoSession.class );
    when( session.getId() ).thenReturn( "s1" );

    cacheManager.killSessionCache( session );

    verify( storageAccess ).removeFromCache( eq( "s1\ta" ), any() );
    verify( storageAccess, never() ).removeFromCache( eq( "s2\ta" ), any() );
  }

  @Test
  public void killSessionCacheDoesNothingWhenCacheDisabled() {
    ReflectionTestUtils.setField( cacheManager, "cacheEnabled", false );
    IPentahoSession session = mock( IPentahoSession.class );

    cacheManager.killSessionCache( session );

    verify( session, never() ).getId();
    verify( storageAccess, never() ).removeFromCache( any(), any() );
  }

  @Test
  public void killSessionCacheDoesNothingWhenThereIsNoSessionRegion() {
    ReflectionTestUtils.setField( cacheManager, "regionCache", new HashMap<String, Object>() );
    IPentahoSession session = mock( IPentahoSession.class );

    cacheManager.killSessionCache( session );

    verify( session, never() ).getId();
    verify( storageAccess, never() ).removeFromCache( any(), any() );
  }

  @Test
  public void killSessionCacheDoesNothingWhenCacheReturnsNoEntries() {
    cacheContains( null );
    IPentahoSession session = mock( IPentahoSession.class );

    cacheManager.killSessionCache( session );

    verify( session, never() ).getId();
    verify( storageAccess, never() ).removeFromCache( any(), any() );
  }

  @Test
  public void killSessionCacheSkipsNullKeys() {
    Map<Object, Object> map = new HashMap<>();
    map.put( null, "v" );
    map.put( "s1\ta", "v" );
    cacheContains( map );
    IPentahoSession session = mock( IPentahoSession.class );
    when( session.getId() ).thenReturn( "s1" );

    cacheManager.killSessionCache( session );

    verify( storageAccess ).removeFromCache( eq( "s1\ta" ), any() );
    verify( storageAccess, times( 1 ) ).removeFromCache( any(), any() );
  }
}
