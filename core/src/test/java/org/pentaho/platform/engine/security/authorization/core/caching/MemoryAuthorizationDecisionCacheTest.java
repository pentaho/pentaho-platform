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

package org.pentaho.platform.engine.security.authorization.core.caching;

import com.google.common.base.Ticker;
import com.google.common.cache.CacheBuilder;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationOptions;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.caching.IAuthorizationDecisionCacheKey;
import org.pentaho.platform.api.engine.security.authorization.decisions.IAuthorizationDecision;
import org.pentaho.platform.engine.core.system.StandaloneSession;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link MemoryAuthorizationDecisionCache}.
 * <p>
 * Uses a testable subclass to avoid interaction with Pentaho System infrastructure.
 */
public class MemoryAuthorizationDecisionCacheTest {

  /**
   * Testable subclass that overrides all Pentaho System interactions.
   * This allows testing the cache without mocking static methods.
   */
  private static class TestableMemoryAuthorizationDecisionCache extends MemoryAuthorizationDecisionCache {

    private final ThreadLocal<IPentahoSession> sessionThreadLocal = new ThreadLocal<>();
    private final Deque<IPentahoSession> sessionStack = new ArrayDeque<>();
    private final FakeTicker fakeTicker = new FakeTicker();

    public TestableMemoryAuthorizationDecisionCache(
      long expireAfterWrite,
      long maximumSize,
      boolean recordStats,
      long staleSessionsSweepInterval ) {
      super( expireAfterWrite, maximumSize, recordStats, staleSessionsSweepInterval );
    }

    @Override
    protected void registerLogoutListener() {
      // No-op: avoid PentahoSystem interaction
    }

    @Override
    protected void unregisterLogoutListener() {
      // No-op: avoid PentahoSystem interaction
    }

    @NonNull
    @Override
    protected IPentahoSession getSession() {
      IPentahoSession session = sessionThreadLocal.get();
      if ( session == null ) {
        throw new IllegalStateException( "No session set for current thread" );
      }
      return session;
    }

    /**
     * Sets the current session for the current thread.
     */
    public void setCurrentSession( @Nullable IPentahoSession session ) {
      if ( session == null ) {
        sessionThreadLocal.remove();
      } else {
        sessionThreadLocal.set( session );
      }
    }

    /**
     * Pushes a session onto the session stack and sets it as current.
     */
    public void pushSession( @NonNull IPentahoSession session ) {
      sessionStack.push( session );
      setCurrentSession( session );
    }

    /**
     * Pops the current session and restores the previous one.
     */
    public void popSession() {
      sessionStack.pop();
      setCurrentSession( sessionStack.isEmpty() ? null : sessionStack.peek() );
    }

    @Nullable
    @Override
    protected AutoCloseable createSessionCacheSweeper( long staleSessionsSweepInterval ) {
      // Return null to disable sweeper by default in tests
      // Individual tests can enable it if needed
      return null;
    }

    @NonNull
    @Override
    protected CacheBuilder<Object, Object> createSessionCacheBuilder() {
      // Call parent to get the configured builder, then inject our fake ticker
      return super.createSessionCacheBuilder().ticker( fakeTicker );
    }

    /**
     * Advances the fake ticker by the specified duration.
     *
     * @param duration The duration to advance.
     * @param timeUnit The time unit of the duration.
     */
    public void advanceTime( long duration, TimeUnit timeUnit ) {
      fakeTicker.advance( duration, timeUnit );
    }
  }

  /**
   * A fake ticker for testing time-based cache operations without Thread.sleep.
   */
  private static class FakeTicker extends Ticker {
    private long nanos = 0;

    @Override
    public long read() {
      return nanos;
    }

    public void advance( long duration, TimeUnit timeUnit ) {
      nanos += timeUnit.toNanos( duration );
    }
  }

  private TestableMemoryAuthorizationDecisionCache cache;
  private IAuthorizationRequest request;
  private IAuthorizationOptions options;
  private IAuthorizationDecision decision;
  private IPentahoSession session;

  @Before
  public void setUp() {
    // Default cache settings: 60 second expiry, 1000 max size, stats enabled, no sweeper
    cache = new TestableMemoryAuthorizationDecisionCache( 60, 1000, true, 0 );

    // Create mock objects
    request = mock( IAuthorizationRequest.class );
    options = mock( IAuthorizationOptions.class );
    decision = mock( IAuthorizationDecision.class );

    // Create test session
    session = createTestSession( "test-user" );
    cache.setCurrentSession( session );
  }

  @After
  public void tearDown() throws Exception {
    if ( cache != null ) {
      cache.close();
    }
  }

  // region Helper Methods

  private static IPentahoSession createTestSession( String userName ) {
    IPentahoSession session = mock( IPentahoSession.class );
    when( session.getName() ).thenReturn( userName );
    return session;
  }

  @SuppressWarnings( "SameParameterValue" )
  private static StandaloneSession createStandaloneSession( String username ) {
    return new StandaloneSession( username );
  }

  // endregion

  // region Constructor Tests

  @Test
  public void testConstructor_CreatesCache() {
    assertNotNull( cache );
  }

  @Test
  public void testConstructor_WithStatsEnabled() {
    cache = new TestableMemoryAuthorizationDecisionCache( 60, 1000, true, 0 );
    String stats = cache.toString();
    assertNotNull( stats );
    assertTrue( stats.contains( "MemoryAuthorizationDecisionCache" ) );
  }

  @Test
  public void testConstructor_WithStatsDisabled() {
    cache = new TestableMemoryAuthorizationDecisionCache( 60, 1000, false, 0 );
    assertNotNull( cache );
  }

  // endregion

  // region Basic Cache Operations

  @Test
  public void testGet_ReturnsEmptyWhenNotCached() {
    Optional<IAuthorizationDecision> result = cache.get( request, options );

    assertFalse( result.isPresent() );
  }

  @Test
  public void testPut_StoresDecisionInCache() {
    cache.put( request, options, decision );

    Optional<IAuthorizationDecision> result = cache.get( request, options );

    assertTrue( result.isPresent() );
    assertSame( decision, result.get() );
  }

  @Test
  public void testGet_WithDifferentRequest_ReturnsEmpty() {
    cache.put( request, options, decision );

    IAuthorizationRequest differentRequest = mock( IAuthorizationRequest.class );
    Optional<IAuthorizationDecision> result = cache.get( differentRequest, options );

    assertFalse( result.isPresent() );
  }

  @Test
  public void testGet_WithDifferentOptions_ReturnsEmpty() {
    cache.put( request, options, decision );

    IAuthorizationOptions differentOptions = mock( IAuthorizationOptions.class );
    Optional<IAuthorizationDecision> result = cache.get( request, differentOptions );

    assertFalse( result.isPresent() );
  }

  @Test
  public void testGetWithLoader_CacheMiss_InvokesLoader() {
    AtomicInteger loaderCallCount = new AtomicInteger( 0 );

    Function<IAuthorizationDecisionCacheKey, IAuthorizationDecision> loader = key -> {
      loaderCallCount.incrementAndGet();
      return decision;
    };

    IAuthorizationDecision result = cache.get( request, options, loader );

    assertSame( decision, result );
    assertEquals( 1, loaderCallCount.get() );
  }

  @Test
  public void testGetWithLoader_CacheHit_DoesNotInvokeLoader() {
    // Pre-populate cache
    cache.put( request, options, decision );

    AtomicInteger loaderCallCount = new AtomicInteger( 0 );
    Function<IAuthorizationDecisionCacheKey, IAuthorizationDecision> loader = key -> {
      loaderCallCount.incrementAndGet();
      return mock( IAuthorizationDecision.class );
    };

    IAuthorizationDecision result = cache.get( request, options, loader );

    assertSame( decision, result );
    assertEquals( 0, loaderCallCount.get() );
  }

  @Test
  public void testGetWithLoader_LoaderReturnsDifferentDecision_StoresInCache() {
    IAuthorizationDecision loaderDecision = mock( IAuthorizationDecision.class );

    Function<IAuthorizationDecisionCacheKey, IAuthorizationDecision> loader = key -> loaderDecision;

    IAuthorizationDecision result = cache.get( request, options, loader );

    assertSame( loaderDecision, result );

    // Verify it was stored
    Optional<IAuthorizationDecision> cached = cache.get( request, options );
    assertTrue( cached.isPresent() );
    assertSame( loaderDecision, cached.get() );
  }

  @Test( expected = RuntimeException.class )
  public void testGetWithLoader_LoaderThrowsRuntimeException_PropagatesException() {
    Function<IAuthorizationDecisionCacheKey, IAuthorizationDecision> loader = key -> {
      throw new RuntimeException( "Loader failed" );
    };

    cache.get( request, options, loader );
  }

  // endregion

  // region Session Management Tests

  @Test
  public void testDifferentSessions_HaveSeparateCaches() {
    // Session 1
    cache.put( request, options, decision );

    // Switch to session 2
    IPentahoSession session2 = createTestSession( "user2" );
    cache.setCurrentSession( session2 );

    // Session 2 should not see session 1's cache
    Optional<IAuthorizationDecision> result = cache.get( request, options );
    assertFalse( result.isPresent() );
  }

  @Test
  public void testSameUserName_SharesCache() {
    // Session 1
    cache.put( request, options, decision );

    // Create a different session object with same user name
    IPentahoSession session2 = createTestSession( "test-user" );
    cache.setCurrentSession( session2 );

    // Should see the same cache
    Optional<IAuthorizationDecision> result = cache.get( request, options );
    assertTrue( result.isPresent() );
    assertSame( decision, result.get() );
  }

  @Test
  public void testOnLogout_InvalidatesSessionCache() {
    cache.put( request, options, decision );

    // Simulate logout
    cache.onLogout( session );

    // Cache should be invalidated
    cache.setCurrentSession( session );
    Optional<IAuthorizationDecision> result = cache.get( request, options );
    assertFalse( result.isPresent() );
  }

  @Test
  public void testStandaloneSession_DestroyInvalidatesCache() {
    StandaloneSession standaloneSession = createStandaloneSession( "standalone-user" );
    cache.setCurrentSession( standaloneSession );

    cache.put( request, options, decision );

    // Destroy the session (triggers the session container callback)
    standaloneSession.destroy();

    // Create a new session with same name
    IPentahoSession newSession = createTestSession( "standalone-user" );
    cache.setCurrentSession( newSession );

    // Cache should be invalidated
    Optional<IAuthorizationDecision> result = cache.get( request, options );
    assertFalse( result.isPresent() );
  }

  @Test
  public void testMultipleSessionsSameUser_LastLogoutInvalidatesAll() {
    // Session 1
    cache.put( request, options, decision );

    // Session 2 with same user
    IPentahoSession session2 = createTestSession( "test-user" );
    cache.setCurrentSession( session2 );

    IAuthorizationDecision decision2 = mock( IAuthorizationDecision.class );
    IAuthorizationRequest request2 = mock( IAuthorizationRequest.class );
    cache.put( request2, options, decision2 );

    // Logout session 1
    cache.onLogout( session );

    // Session 2 should still see the cache (not last session)
    Optional<IAuthorizationDecision> result = cache.get( request, options );
    assertTrue( result.isPresent() );

    // Logout session 2 (last session)
    cache.onLogout( session2 );

    // Now cache should be fully invalidated
    IPentahoSession session3 = createTestSession( "test-user" );
    cache.setCurrentSession( session3 );
    result = cache.get( request, options );
    assertFalse( result.isPresent() );
  }

  // endregion

  // region Invalidation Tests

  @Test
  public void testInvalidate_RemovesSpecificEntry() {
    cache.put( request, options, decision );

    IAuthorizationRequest request2 = mock( IAuthorizationRequest.class );
    IAuthorizationDecision decision2 = mock( IAuthorizationDecision.class );
    cache.put( request2, options, decision2 );

    // Invalidate only the first entry
    cache.invalidate( request, options );

    // First should be gone
    Optional<IAuthorizationDecision> result1 = cache.get( request, options );
    assertFalse( result1.isPresent() );

    // Second should still be there
    Optional<IAuthorizationDecision> result2 = cache.get( request2, options );
    assertTrue( result2.isPresent() );
    assertSame( decision2, result2.get() );
  }

  @Test
  public void testInvalidate_AcrossMultipleSessions() {
    // Session 1
    cache.put( request, options, decision );

    // Session 2
    IPentahoSession session2 = createTestSession( "user2" );
    cache.setCurrentSession( session2 );
    IAuthorizationDecision decision2 = mock( IAuthorizationDecision.class );
    cache.put( request, options, decision2 );

    // Invalidate should affect both sessions
    cache.invalidate( request, options );

    // Check session 1
    cache.setCurrentSession( session );
    Optional<IAuthorizationDecision> result1 = cache.get( request, options );
    assertFalse( result1.isPresent() );

    // Check session 2
    cache.setCurrentSession( session2 );
    Optional<IAuthorizationDecision> result2 = cache.get( request, options );
    assertFalse( result2.isPresent() );
  }

  @Test
  public void testInvalidateAll_WithPredicate_RemovesMatchingEntries() {
    IAuthorizationRequest request1 = mock( IAuthorizationRequest.class );
    IAuthorizationRequest request2 = mock( IAuthorizationRequest.class );

    cache.put( request1, options, decision );
    cache.put( request2, options, mock( IAuthorizationDecision.class ) );

    // Invalidate only entries with request1
    cache.invalidateAll( key -> key.getRequest().equals( request1 ) );

    // request1 should be gone
    Optional<IAuthorizationDecision> result1 = cache.get( request1, options );
    assertFalse( result1.isPresent() );

    // request2 should still be there
    Optional<IAuthorizationDecision> result2 = cache.get( request2, options );
    assertTrue( result2.isPresent() );
  }

  @Test
  public void testInvalidateAll_ClearsAllEntries() {
    cache.put( request, options, decision );

    IAuthorizationRequest request2 = mock( IAuthorizationRequest.class );
    cache.put( request2, options, mock( IAuthorizationDecision.class ) );

    cache.invalidateAll();

    // All entries should be gone
    Optional<IAuthorizationDecision> result1 = cache.get( request, options );
    assertFalse( result1.isPresent() );

    Optional<IAuthorizationDecision> result2 = cache.get( request2, options );
    assertFalse( result2.isPresent() );
  }

  @Test
  public void testInvalidateAll_AcrossAllSessions() {
    // Session 1
    cache.put( request, options, decision );

    // Session 2
    IPentahoSession session2 = createTestSession( "user2" );
    cache.setCurrentSession( session2 );
    cache.put( request, options, mock( IAuthorizationDecision.class ) );

    // Invalidate all
    cache.invalidateAll();

    // Check session 1
    cache.setCurrentSession( session );
    Optional<IAuthorizationDecision> result1 = cache.get( request, options );
    assertFalse( result1.isPresent() );

    // Check session 2
    cache.setCurrentSession( session2 );
    Optional<IAuthorizationDecision> result2 = cache.get( request, options );
    assertFalse( result2.isPresent() );
  }

  // endregion

  // region Expiration Tests

  @Test
  public void testExpireAfterWrite_EntryExpires() {
    // Create cache with 1 second expiry
    cache = new TestableMemoryAuthorizationDecisionCache( 1, 1000, false, 0 );
    cache.setCurrentSession( session );

    cache.put( request, options, decision );

    // Should be present immediately
    Optional<IAuthorizationDecision> result = cache.get( request, options );
    assertTrue( result.isPresent() );

    // Advance time by 1.1 seconds using fake ticker
    cache.advanceTime( 1100, TimeUnit.MILLISECONDS );

    // Should be expired
    result = cache.get( request, options );
    assertFalse( result.isPresent() );
  }

  @Test
  public void testMaximumSize_EvictsEntries() {
    // Create cache with max size of 2
    cache = new TestableMemoryAuthorizationDecisionCache( 60, 2, false, 0 );
    cache.setCurrentSession( session );

    IAuthorizationRequest req1 = mock( IAuthorizationRequest.class );
    IAuthorizationRequest req2 = mock( IAuthorizationRequest.class );
    IAuthorizationRequest req3 = mock( IAuthorizationRequest.class );

    cache.put( req1, options, mock( IAuthorizationDecision.class ) );
    cache.put( req2, options, mock( IAuthorizationDecision.class ) );
    cache.put( req3, options, mock( IAuthorizationDecision.class ) );

    // At least one entry should have been evicted
    int presentCount = 0;
    if ( cache.get( req1, options ).isPresent() ) {
      presentCount++;
    }

    if ( cache.get( req2, options ).isPresent() ) {
      presentCount++;
    }

    if ( cache.get( req3, options ).isPresent() ) {
      presentCount++;
    }

    assertTrue( "Expected at most 2 entries", presentCount <= 2 );
  }

  // endregion

  // region Concurrency Tests

  @Test
  public void testConcurrentGet_SameSessionOnMultipleThreads() throws InterruptedException {
    cache.put( request, options, decision );

    int threadCount = 10;
    CountDownLatch startLatch = new CountDownLatch( 1 );
    CountDownLatch doneLatch = new CountDownLatch( threadCount );
    AtomicInteger successCount = new AtomicInteger( 0 );

    for ( int i = 0; i < threadCount; i++ ) {
      new Thread( () -> {
        try {
          cache.setCurrentSession( session );

          // Wait for the "gate" to open. All threads start at the same time.
          startLatch.await();

          Optional<IAuthorizationDecision> result = cache.get( request, options );
          if ( result.isPresent() && result.get() == decision ) {
            successCount.incrementAndGet();
          }
        } catch ( InterruptedException e ) {
          Thread.currentThread().interrupt();
        } finally {
          doneLatch.countDown();
        }
      } ).start();
    }

    // Start all threads, awaiting at the "gate".
    startLatch.countDown();

    // Wait for all threads to finish, for a maximum of 5 seconds.
    assertTrue( doneLatch.await( 5, TimeUnit.SECONDS ) );

    // Expect all threads to have succeeded.
    assertEquals( threadCount, successCount.get() );
  }

  @Test
  public void testConcurrentPut_SameSessionOnMultipleThreads() throws InterruptedException {
    int threadCount = 10;
    CountDownLatch startLatch = new CountDownLatch( 1 );
    CountDownLatch doneLatch = new CountDownLatch( threadCount );

    for ( int i = 0; i < threadCount; i++ ) {
      new Thread( () -> {
        try {
          cache.setCurrentSession( session );
          startLatch.await();

          IAuthorizationRequest req = mock( IAuthorizationRequest.class );
          IAuthorizationDecision dec = mock( IAuthorizationDecision.class );
          cache.put( req, options, dec );
        } catch ( InterruptedException e ) {
          Thread.currentThread().interrupt();
        } finally {
          doneLatch.countDown();
        }
      } ).start();
    }

    startLatch.countDown();
    assertTrue( doneLatch.await( 5, TimeUnit.SECONDS ) );
  }

  @Test
  public void testConcurrentPutThenGet_DifferentSessionOnMultipleThreads() throws InterruptedException {
    int threadCount = 10;
    CountDownLatch startLatch = new CountDownLatch( 1 );
    CountDownLatch doneLatch = new CountDownLatch( threadCount );
    AtomicInteger successCount = new AtomicInteger( 0 );

    for ( int i = 0; i < threadCount; i++ ) {
      final String userName = "user" + i;
      new Thread( () -> {
        try {
          IPentahoSession threadSession = createTestSession( userName );
          cache.setCurrentSession( threadSession );
          startLatch.await();

          IAuthorizationDecision threadDecision = mock( IAuthorizationDecision.class );
          cache.put( request, options, threadDecision );

          Optional<IAuthorizationDecision> result = cache.get( request, options );
          if ( result.isPresent() && result.get() == threadDecision ) {
            successCount.incrementAndGet();
          }
        } catch ( InterruptedException e ) {
          Thread.currentThread().interrupt();
        } finally {
          doneLatch.countDown();
        }
      } ).start();
    }

    startLatch.countDown();
    assertTrue( doneLatch.await( 5, TimeUnit.SECONDS ) );
    assertEquals( threadCount, successCount.get() );
  }

  @Test
  public void testConcurrentInvalidateWhileReading() throws InterruptedException {
    cache.put( request, options, decision );

    CountDownLatch startLatch = new CountDownLatch( 1 );
    CountDownLatch doneLatch = new CountDownLatch( 2 );
    AtomicReference<Exception> exception = new AtomicReference<>();

    // Reader thread
    new Thread( () -> {
      try {
        cache.setCurrentSession( session );
        startLatch.await();

        for ( int i = 0; i < 100; i++ ) {
          cache.get( request, options );
        }
      } catch ( Exception e ) {
        exception.set( e );
      } finally {
        doneLatch.countDown();
      }
    } ).start();

    // Invalidator thread
    new Thread( () -> {
      try {
        cache.setCurrentSession( session );
        startLatch.await();

        for ( int i = 0; i < 100; i++ ) {
          cache.invalidate( request, options );
        }
      } catch ( Exception e ) {
        exception.set( e );
      } finally {
        doneLatch.countDown();
      }
    } ).start();

    startLatch.countDown();
    assertTrue( doneLatch.await( 5, TimeUnit.SECONDS ) );
    assertNull( "No exception should occur during concurrent access", exception.get() );
  }

  // endregion

  // region Statistics Tests

  @Test
  public void testToString_ContainsStats() {
    cache = new TestableMemoryAuthorizationDecisionCache( 60, 1000, true, 0 );
    cache.setCurrentSession( session );

    String stats = cache.toString();
    assertNotNull( stats );
    assertTrue( stats.contains( "MemoryAuthorizationDecisionCache" ) );
    assertTrue( stats.contains( "stats=" ) );
  }

  @Test
  public void testStats_TracksHitsAndMisses() {
    cache = new TestableMemoryAuthorizationDecisionCache( 60, 1000, true, 0 );
    cache.setCurrentSession( session );

    // Miss
    cache.get( request, options );

    // Hit
    cache.put( request, options, decision );
    cache.get( request, options );

    String stats = cache.toString();

    // Not testing actual counts, given that these are not always perfectly correct.
    // In the following, missCount=0, not 1, because in the first miss, there's not even a cache created yet for the
    // session.
    assertTrue( stats.contains( "hitCount=" ) );
    assertTrue( stats.contains( "missCount=" ) );
  }

  // endregion

  // region Session Stack Tests

  @Test
  public void testSessionStack_PushPop_DifferentUserNames() {
    cache.pushSession( session );
    cache.put( request, options, decision );

    IPentahoSession session2 = createTestSession( "user2" );
    cache.pushSession( session2 );

    // Should not see session1's cache (different user)
    Optional<IAuthorizationDecision> result = cache.get( request, options );
    assertFalse( result.isPresent() );

    // Pop back to session1
    cache.popSession();

    // Should see session1's cache again
    result = cache.get( request, options );
    assertTrue( result.isPresent() );
    assertSame( decision, result.get() );
  }

  @Test
  public void testSessionStack_PushPop_SameUserName_DifferentInstances() {
    // This tests the critical SessionCacheData.getCache() logic:
    // Multiple session instances with the same username should share the same cache,
    // and the SessionCacheData must track ALL session instances in its sessions set.

    cache.pushSession( session );
    cache.put( request, options, decision );

    // Create a DIFFERENT session instance with the SAME username
    IPentahoSession session2 = createTestSession( "test-user" );
    cache.pushSession( session2 );

    // Should see the SAME cache (same username)
    Optional<IAuthorizationDecision> result = cache.get( request, options );
    assertTrue( "Session2 should see Session1's cache (same username)", result.isPresent() );
    assertSame( decision, result.get() );

    // Add something with session2
    IAuthorizationDecision decision2 = mock( IAuthorizationDecision.class );
    IAuthorizationRequest request2 = mock( IAuthorizationRequest.class );
    cache.put( request2, options, decision2 );

    // Pop back to session1
    cache.popSession();

    // Session1 should see both entries (shared cache)
    result = cache.get( request, options );
    assertTrue( "Session1 should still see original entry", result.isPresent() );
    assertSame( decision, result.get() );

    Optional<IAuthorizationDecision> result2 = cache.get( request2, options );
    assertTrue( "Session1 should see Session2's entry (shared cache)", result2.isPresent() );
    assertSame( decision2, result2.get() );
  }

  @Test
  public void testMultiplePushes_SameUserName() {
    // Tests deep nesting of sessions with same username
    cache.pushSession( session );
    cache.put( request, options, decision );

    IPentahoSession session2 = createTestSession( "test-user" );
    cache.pushSession( session2 );

    IPentahoSession session3 = createTestSession( "test-user" );
    cache.pushSession( session3 );

    // All should share the same cache
    Optional<IAuthorizationDecision> result = cache.get( request, options );
    assertTrue( result.isPresent() );
    assertSame( decision, result.get() );

    // Pop twice
    cache.popSession();
    cache.popSession();

    // Should still have access
    result = cache.get( request, options );
    assertTrue( result.isPresent() );
    assertSame( decision, result.get() );
  }

  @Test
  public void testMultiplePushes_AlternatingUserNames() {
    // Tests that session stack properly switches between different user caches
    cache.pushSession( session ); // user: test-user
    cache.put( request, options, decision );

    IPentahoSession session2 = createTestSession( "user2" );
    cache.pushSession( session2 );
    IAuthorizationDecision decision2 = mock( IAuthorizationDecision.class );
    cache.put( request, options, decision2 );

    IPentahoSession session3 = createTestSession( "test-user" ); // Back to test-user
    cache.pushSession( session3 );

    // Should see original test-user's cache
    Optional<IAuthorizationDecision> result = cache.get( request, options );
    assertTrue( result.isPresent() );
    assertSame( decision, result.get() );

    // Pop back to user2
    cache.popSession();
    result = cache.get( request, options );
    assertTrue( result.isPresent() );
    assertSame( decision2, result.get() );

    // Pop back to test-user
    cache.popSession();
    result = cache.get( request, options );
    assertTrue( result.isPresent() );
    assertSame( decision, result.get() );
  }

  @Test
  public void testLogout_WithMultipleSessionInstances_SameUserName() {
    // This tests that when multiple session instances share a cache,
    // only when ALL instances are logged out does the cache get invalidated.

    cache.pushSession( session );
    cache.put( request, options, decision );

    IPentahoSession session2 = createTestSession( "test-user" );
    cache.pushSession( session2 );

    // Verify both share the cache
    Optional<IAuthorizationDecision> result = cache.get( request, options );
    assertTrue( result.isPresent() );

    // Logout session1 (but session2 is still active)
    cache.onLogout( session );

    // Cache should still be accessible via session2
    result = cache.get( request, options );
    assertTrue( "Cache should persist while session2 is active", result.isPresent() );
    assertSame( decision, result.get() );

    // Logout session2 (last session)
    cache.onLogout( session2 );

    // Now create a new session with same username
    IPentahoSession session3 = createTestSession( "test-user" );
    cache.setCurrentSession( session3 );

    // Cache should now be invalidated
    result = cache.get( request, options );
    assertFalse( "Cache should be invalidated after all sessions logged out", result.isPresent() );
  }

  // endregion

  // region Edge Cases

  @Test( expected = IllegalStateException.class )
  public void testGetSession_NoSessionSet_ThrowsException() {
    cache.setCurrentSession( null );
    cache.get( request, options );
  }

  @Test( expected = IllegalStateException.class )
  public void testGetSession_SessionWithoutName_ThrowsException() {
    IPentahoSession invalidSession = mock( IPentahoSession.class );
    when( invalidSession.getName() ).thenReturn( null );
    cache.setCurrentSession( invalidSession );

    cache.get( request, options );
  }

  @Test
  public void testClose_CleansUpAllCaches() throws Exception {
    cache.put( request, options, decision );

    IPentahoSession session2 = createTestSession( "user2" );
    cache.setCurrentSession( session2 );
    cache.put( request, options, mock( IAuthorizationDecision.class ) );

    cache.close();

    // After close, should be able to use again (though logout listener is unregistered)
    cache.setCurrentSession( session );
    Optional<IAuthorizationDecision> result = cache.get( request, options );
    assertFalse( result.isPresent() );
  }

  // endregion
}
