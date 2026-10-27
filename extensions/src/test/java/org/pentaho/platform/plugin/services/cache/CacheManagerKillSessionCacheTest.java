/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/


package org.pentaho.platform.plugin.services.cache;

import org.hibernate.cache.jcache.internal.JCacheAccessImpl;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.springframework.test.util.ReflectionTestUtils;

import javax.cache.Cache;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Pins the {@link CacheManager#killSessionCache} contract: which entries are evicted and every early-exit path.
 */
public class CacheManagerKillSessionCacheTest {

  private CacheManager cacheManager;

  @Before
  public void setUp() {
    cacheManager = new CacheManager();
  }

  @Test
  public void doesNothingWhenCacheIsDisabled() {
    Cache<Object, Object> cache = mock( Cache.class );
    registerSessionRegion( cache );

    cacheManager.killSessionCache( session( "s1" ) );

    verifyNoInteractions( cache );
  }

  @Test
  public void doesNothingWhenThereIsNoSessionRegion() {
    enableCache();
    ReflectionTestUtils.setField( cacheManager, "regionCache", new ConcurrentHashMap<String, Object>() );

    cacheManager.killSessionCache( session( "s1" ) );
  }

  @Test
  public void doesNothingWhenUnderlyingCacheIsNull() {
    enableCache();
    registerSessionRegion( null );

    cacheManager.killSessionCache( session( "s1" ) );
  }

  @Test
  public void evictsOnlyTheEntriesOfTheGivenSession() {
    enableCache();
    Cache<Object, Object> cache = realCache();
    cache.put( "s1\ta", "v" );
    cache.put( "s1\tb", "v" );
    cache.put( "s2\ta", "v" );
    registerSessionRegion( cache );

    cacheManager.killSessionCache( session( "s1" ) );

    assertFalse( cache.containsKey( "s1\ta" ) );
    assertFalse( cache.containsKey( "s1\tb" ) );
    assertTrue( cache.containsKey( "s2\ta" ) );
    cache.getCacheManager().destroyCache( cache.getName() );
  }

  @Test
  public void onLogoutEvictsTheSessionEntries() {
    enableCache();
    Cache<Object, Object> cache = realCache();
    cache.put( "s1\ta", "v" );
    cache.put( "s2\ta", "v" );
    registerSessionRegion( cache );

    cacheManager.onLogout( session( "s1" ) );

    assertFalse( cache.containsKey( "s1\ta" ) );
    assertEquals( "v", cache.get( "s2\ta" ) );
    cache.getCacheManager().destroyCache( cache.getName() );
  }

  private void enableCache() {
    ReflectionTestUtils.setField( cacheManager, "cacheEnabled", true );
  }

  private void registerSessionRegion( Cache<Object, Object> cache ) {
    LastModifiedCache region = mock( LastModifiedCache.class, CALLS_REAL_METHODS );
    doReturn( new JCacheAccessImpl( cache ) ).when( region ).getStorageAccess();
    ConcurrentHashMap<String, Object> regions = new ConcurrentHashMap<>();
    regions.put( ICacheManager.SESSION, region );
    ReflectionTestUtils.setField( cacheManager, "regionCache", regions );
  }

  private static Cache<Object, Object> realCache() {
    return Caching.getCachingProvider( "org.ehcache.jsr107.EhcacheCachingProvider" ).getCacheManager()
      .createCache( "CacheManagerKillSessionCacheTest-" + System.nanoTime(), new MutableConfiguration<>() );
  }

  private static IPentahoSession session( String id ) {
    IPentahoSession session = mock( IPentahoSession.class );
    when( session.getId() ).thenReturn( id );
    return session;
  }
}
