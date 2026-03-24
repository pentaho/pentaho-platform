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

import org.hibernate.cache.CacheException;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class CacheManagerGetOrCreateConcurrencyTest {

  @Test
  public void shouldCreateValueOnlyOnceForConcurrentCallers() throws Exception {
    TestableCacheManager cacheManager = new TestableCacheManager();
    int threadCount = 8;
    ExecutorService executor = Executors.newFixedThreadPool( threadCount );
    try {
      CountDownLatch ready = new CountDownLatch( threadCount );
      CountDownLatch start = new CountDownLatch( 1 );
      AtomicInteger creationCount = new AtomicInteger();

      List<Callable<Object>> tasks = new ArrayList<>();
      for ( int i = 0; i < threadCount; i++ ) {
        tasks.add( () -> {
          ready.countDown();
          assertTrue( start.await( 5, TimeUnit.SECONDS ) );
          return cacheManager.getOrCreateFromRegionCache( "region-a", "key-a", () -> {
            creationCount.incrementAndGet();
            try {
              Thread.sleep( 100 );
            } catch ( InterruptedException e ) {
              Thread.currentThread().interrupt();
              throw new RuntimeException( e );
            }
            return new Object();
          } );
        } );
      }

      List<Future<Object>> futures = new ArrayList<>();
      for ( Callable<Object> task : tasks ) {
        futures.add( executor.submit( task ) );
      }

      assertTrue( ready.await( 5, TimeUnit.SECONDS ) );
      start.countDown();

      Object first = null;
      for ( Future<Object> future : futures ) {
        Object value = future.get( 5, TimeUnit.SECONDS );
        if ( first == null ) {
          first = value;
        }
        assertSame( first, value );
      }

      assertEquals( 1, creationCount.get() );
      assertEquals( 1, cacheManager.entryCount() );
    } finally {
      executor.shutdownNow();
    }
  }

  @Test
  public void shouldPropagateCreationFailureToAllWaitersAndAllowRetry() throws Exception {
    TestableCacheManager cacheManager = new TestableCacheManager();
    int threadCount = 6;
    ExecutorService executor = Executors.newFixedThreadPool( threadCount );
    try {
      CountDownLatch ready = new CountDownLatch( threadCount );
      CountDownLatch start = new CountDownLatch( 1 );
      CountDownLatch creatorStarted = new CountDownLatch( 1 );
      CountDownLatch allowFailure = new CountDownLatch( 1 );
      AtomicInteger failingCreationCount = new AtomicInteger();

      List<Callable<Object>> tasks = new ArrayList<>();
      for ( int i = 0; i < threadCount; i++ ) {
        tasks.add( () -> {
          ready.countDown();
          assertTrue( start.await( 5, TimeUnit.SECONDS ) );
          return cacheManager.getOrCreateFromRegionCache( "region-b", "key-b", () -> {
            failingCreationCount.incrementAndGet();
            creatorStarted.countDown();
            try {
              assertTrue( allowFailure.await( 5, TimeUnit.SECONDS ) );
            } catch ( InterruptedException e ) {
              Thread.currentThread().interrupt();
              throw new RuntimeException( e );
            }
            throw new IllegalStateException( "simulated failure" );
          } );
        } );
      }

      List<Future<Object>> futures = new ArrayList<>();
      for ( Callable<Object> task : tasks ) {
        futures.add( executor.submit( task ) );
      }

      assertTrue( ready.await( 5, TimeUnit.SECONDS ) );
      start.countDown();
      assertTrue( creatorStarted.await( 5, TimeUnit.SECONDS ) );
      Thread.sleep( 100 );
      allowFailure.countDown();

      for ( Future<Object> future : futures ) {
        try {
          future.get( 5, TimeUnit.SECONDS );
          fail( "Expected IllegalStateException" );
        } catch ( Exception e ) {
          Throwable cause = e.getCause() == null ? e : e.getCause();
          assertTrue( cause instanceof IllegalStateException );
          assertEquals( "simulated failure", cause.getMessage() );
        }
      }

      assertEquals( 1, failingCreationCount.get() );

      AtomicInteger retryCreationCount = new AtomicInteger();
      Object retried = cacheManager.getOrCreateFromRegionCache( "region-b", "key-b", () -> {
        retryCreationCount.incrementAndGet();
        return "recovered";
      } );

      assertEquals( "recovered", retried );
      assertEquals( 1, retryCreationCount.get() );

      Object cached = cacheManager.getOrCreateFromRegionCache( "region-b", "key-b", () -> {
        fail( "Creator should not run once value is cached" );
        return "unused";
      } );

      assertEquals( "recovered", cached );
      assertSame( retried, cached );
    } finally {
      executor.shutdownNow();
    }
  }

  @Test
  public void shouldFailFastForRecursiveCreationOnSameThread() {
    TestableCacheManager cacheManager = new TestableCacheManager();

    try {
      cacheManager.getOrCreateFromRegionCache( "region-c", "key-c", () ->
        cacheManager.getOrCreateFromRegionCache( "region-c", "key-c", () -> "value" ) );
      fail( "Expected CacheException" );
    } catch ( CacheException e ) {
      assertTrue( e.getMessage().contains( "Recursive cache creation detected" ) );
    }
  }

  private static final class TestableCacheManager extends CacheManager {

    private final Map<TestKey, Object> values = new ConcurrentHashMap<>();

    private TestableCacheManager() {
      ReflectionTestUtils.setField( this, "cacheEnabled", true );
    }

    @Override
    public boolean cacheEnabled( String region ) {
      return true;
    }

    @Override
    public boolean addCacheRegion( String region ) {
      return true;
    }

    @Override
    public Object getFromRegionCache( String region, Object key ) {
      return values.get( new TestKey( region, key ) );
    }

    @Override
    public void putInRegionCache( String region, Object key, Object value ) {
      values.put( new TestKey( region, key ), value );
    }

    private int entryCount() {
      return values.size();
    }
  }

  private static final class TestKey {
    private final String region;
    private final Object key;

    private TestKey( String region, Object key ) {
      this.region = region;
      this.key = key;
    }

    @Override
    public boolean equals( Object obj ) {
      if ( this == obj ) {
        return true;
      }
      if ( !( obj instanceof TestKey other ) ) {
        return false;
      }
      return Objects.equals( region, other.region ) && Objects.equals( key, other.key );
    }

    @Override
    public int hashCode() {
      return Objects.hash( region, key );
    }
  }
}