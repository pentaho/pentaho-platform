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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.springframework.test.util.ReflectionTestUtils;

import javax.cache.Cache;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * BISERVER-15667: iterating a JCache backed by Ehcache can yield a {@code null} {@link Cache.Entry} when the cache is
 * modified concurrently. The session cache eviction and the key/value listings must tolerate it instead of throwing a
 * {@link NullPointerException}.
 */
public class CacheManagerNullEntryTest {

  private static final String SESSION_1 = "session-1";
  private static final String SESSION_2 = "session-2";
  private static final String KEY_1 = SESSION_1 + "\tkey";
  private static final String KEY_2 = SESSION_2 + "\tkey";

  private CacheManager cacheManager;

  @Before
  public void setUp() {
    cacheManager = new CacheManager();
    ReflectionTestUtils.setField( cacheManager, "cacheEnabled", true );
  }

  @After
  public void tearDown() {
    cacheManager = null;
  }

  @Test
  public void killSessionCacheSkipsNullEntryAndEvictsMatchingKeys() {
    Cache<Object, Object> cache = cacheYielding( entry( KEY_1, "v1" ), null, entry( KEY_2, "v2" ) );
    registerSessionRegion( cache );

    cacheManager.killSessionCache( session( SESSION_1 ) );

    verify( cache ).remove( KEY_1 );
    verify( cache, never() ).remove( KEY_2 );
  }

  @Test
  public void killSessionCacheEvictsEntriesAfterNullEntry() {
    Cache<Object, Object> cache = cacheYielding( null, entry( KEY_1, "v1" ) );
    registerSessionRegion( cache );

    cacheManager.killSessionCache( session( SESSION_1 ) );

    verify( cache ).remove( KEY_1 );
  }

  @Test
  public void getAllValuesFromRegionCacheSkipsNullEntry() {
    registerSessionRegion( cacheYielding( entry( KEY_1, "v1" ), null, entry( KEY_2, "v2" ) ) );

    List<Object> values = cacheManager.getAllValuesFromRegionCache( ICacheManager.SESSION );

    assertEquals( 2, values.size() );
    assertTrue( values.containsAll( Arrays.asList( "v1", "v2" ) ) );
  }

  @Test
  public void getAllEntriesFromRegionCacheSkipsNullEntry() {
    registerSessionRegion( cacheYielding( entry( KEY_1, "v1" ), null, entry( KEY_2, "v2" ) ) );

    Set<?> values = cacheManager.getAllEntriesFromRegionCache( ICacheManager.SESSION );

    assertEquals( 2, values.size() );
    assertTrue( values.containsAll( Arrays.asList( "v1", "v2" ) ) );
  }

  @Test
  public void getAllKeysFromRegionCacheSkipsNullEntry() {
    registerSessionRegion( cacheYielding( entry( KEY_1, "v1" ), null, entry( KEY_2, "v2" ) ) );

    Set<?> keys = cacheManager.getAllKeysFromRegionCache( ICacheManager.SESSION );

    assertEquals( 2, keys.size() );
    assertTrue( keys.containsAll( Arrays.asList( KEY_1, KEY_2 ) ) );
  }

  /**
   * Hammers a real Ehcache JCache from several threads doing what concurrent logouts do. Without the null-entry guards
   * the iteration throws a {@link NullPointerException} within milliseconds.
   */
  @Test
  public void concurrentSessionEvictionAndListingNeverFailOnRealEhcache() throws Exception {
    javax.cache.CacheManager jcacheManager =
      Caching.getCachingProvider( "org.ehcache.jsr107.EhcacheCachingProvider" ).getCacheManager();
    MutableConfiguration<Object, Object> config = new MutableConfiguration<>();
    config.setExpiryPolicyFactory( CreatedExpiryPolicy.factoryOf( new Duration( TimeUnit.MINUTES, 5 ) ) );
    String cacheName = "CacheManagerNullEntryTest-" + System.nanoTime();
    Cache<Object, Object> cache = jcacheManager.createCache( cacheName, config );
    try {
      registerSessionRegion( cache );

      int threads = 4;
      long runMillis = 2000;
      long stopAt = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos( runMillis );
      AtomicLong iterations = new AtomicLong();
      List<Throwable> failures = Collections.synchronizedList( new ArrayList<>() );
      CountDownLatch start = new CountDownLatch( 1 );
      ExecutorService executor = Executors.newFixedThreadPool( threads );
      try {
        List<Future<?>> futures = new ArrayList<>();
        for ( int t = 0; t < threads; t++ ) {
          IPentahoSession session = session( "thread-" + t );
          futures.add( executor.submit( () -> {
            try {
              start.await();
              long i = 0;
              while ( System.nanoTime() < stopAt ) {
                for ( int k = 0; k < 20; k++ ) {
                  cache.put( session.getId() + "\tkey" + ( i++ % 50 ), "value" );
                }
                cacheManager.killSessionCache( session );
                cacheManager.getAllKeysFromRegionCache( ICacheManager.SESSION );
                cacheManager.getAllValuesFromRegionCache( ICacheManager.SESSION );
                cacheManager.getAllEntriesFromRegionCache( ICacheManager.SESSION );
                iterations.incrementAndGet();
              }
            } catch ( Throwable e ) {
              failures.add( e );
            }
          } ) );
        }
        start.countDown();
        for ( Future<?> future : futures ) {
          future.get( 30, TimeUnit.SECONDS );
        }
      } finally {
        executor.shutdownNow();
      }

      assertTrue( "Concurrent cache access failed: " + failures, failures.isEmpty() );
      assertTrue( "No iteration completed", iterations.get() > 0 );
    } finally {
      jcacheManager.destroyCache( cacheName );
    }
  }

  /**
   * Registers a {@link LastModifiedCache} (without a Hibernate session factory) whose storage is the given JCache as
   * the session region.
   */
  private void registerSessionRegion( Cache<Object, Object> cache ) {
    LastModifiedCache region = mock( LastModifiedCache.class, CALLS_REAL_METHODS );
    doReturn( new JCacheAccessImpl( cache ) ).when( region ).getStorageAccess();
    ConcurrentHashMap<String, Object> regions = new ConcurrentHashMap<>();
    regions.put( ICacheManager.SESSION, region );
    ReflectionTestUtils.setField( cacheManager, "regionCache", regions );
  }

  /**
   * A JCache whose iteration ({@code iterator()} and {@code forEach}) yields exactly the given entries, including
   * {@code null}s.
   */
  @SuppressWarnings( "unchecked" )
  private static Cache<Object, Object> cacheYielding( Cache.Entry<Object, Object>... entries ) {
    Cache<Object, Object> cache = mock( Cache.class );
    when( cache.iterator() ).thenAnswer( invocation -> Arrays.asList( entries ).iterator() );
    doAnswer( invocation -> {
      Consumer<Cache.Entry<Object, Object>> consumer = invocation.getArgument( 0 );
      Arrays.asList( entries ).forEach( consumer );
      return null;
    } ).when( cache ).forEach( any() );
    return cache;
  }

  @SuppressWarnings( "unchecked" )
  private static Cache.Entry<Object, Object> entry( Object key, Object value ) {
    Cache.Entry<Object, Object> entry = mock( Cache.Entry.class );
    when( entry.getKey() ).thenReturn( key );
    when( entry.getValue() ).thenReturn( value );
    return entry;
  }

  private static IPentahoSession session( String id ) {
    IPentahoSession session = mock( IPentahoSession.class );
    when( session.getId() ).thenReturn( id );
    return session;
  }
}
