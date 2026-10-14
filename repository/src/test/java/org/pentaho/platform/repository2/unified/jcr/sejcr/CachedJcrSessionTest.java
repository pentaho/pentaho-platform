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



package org.pentaho.platform.repository2.unified.jcr.sejcr;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Unit tests for the lease state machine that coordinates cached JCR session use and retirement.
 * The mocked session isolates lifecycle accounting from repository behavior; assertions focus on rejecting new
 * cache users after retirement, preserving existing owners, and attempting logout exactly once.
 */
@RunWith( MockitoJUnitRunner.class )
public class CachedJcrSessionTest {

  @Mock private Session session;

  /** Configures each test session as live so logout behavior is observable. */
  @Before
  public void setUp() {
    when( session.isLive() ).thenReturn( true );
  }

  /** Verifies retirement rejects new acquisitions and defers logout until both existing leases are released. */
  @Test
  public void retirementWaitsForAllActiveUsesAndRejectsNewAcquisitions() {
    CachedJcrSession cachedSession = new CachedJcrSession( session );

    assertTrue( cachedSession.acquire() );
    assertTrue( cachedSession.acquire() );
    cachedSession.retire();

    assertFalse( "A retired session must not be acquired again", cachedSession.acquire() );
    verify( session, never() ).logout();

    cachedSession.release();
    verify( session, never() ).logout();

    cachedSession.release();
    cachedSession.retire();
    verify( session, times( 1 ) ).logout();
  }

  /** Verifies an unconsumed factory lease is rolled back once, and that nothing happens when none is pending. */
  @Test
  public void unconsumedFactoryLeaseIsRolledBackOnlyWhilePending() {
    CachedJcrSession cachedSession = new CachedJcrSession( session );

    assertTrue( cachedSession.acquire() );
    assertTrue( cachedSession.acquire() );
    assertTrue( cachedSession.consumeFactoryLease() );
    cachedSession.retire();

    cachedSession.releaseUnconsumedFactoryLease();
    verify( session, never() ).logout();
    cachedSession.releaseUnconsumedFactoryLease();
    verify( session, never() ).logout();

    cachedSession.release();
    verify( session, times( 1 ) ).logout();
  }

  /** Verifies an already-active bound operation can nest after retirement without reviving cache access. */
  @Test
  public void boundNestedUseCanExtendRetiredSessionWhileOuterLeaseRemainsActive() {
    CachedJcrSession cachedSession = new CachedJcrSession( session );

    assertTrue( cachedSession.acquire() );
    assertTrue( "The owning template consumes the factory handoff", cachedSession.consumeFactoryLease() );
    assertFalse( "A factory handoff can only be consumed once", cachedSession.consumeFactoryLease() );
    cachedSession.retire();

    assertFalse( "Retired sessions are not available to new cache callers", cachedSession.acquire() );
    assertTrue( "An already-bound operation may extend an active lease", cachedSession.acquireForBoundUse() );
    cachedSession.release();
    verify( session, never() ).logout();

    cachedSession.release();
    verify( session, times( 1 ) ).logout();
  }

  /** Verifies a bound-use lease cannot resurrect a retired session after its final active lease is gone. */
  @Test
  public void boundUseCannotReviveSessionAfterFinalLeaseWasReleased() {
    CachedJcrSession cachedSession = new CachedJcrSession( session );
    cachedSession.retire();

    assertFalse( cachedSession.acquireForBoundUse() );
    verify( session, times( 1 ) ).logout();
  }

  /** Verifies each factory acquisition has one consumable handoff and that completed uses balance before logout. */
  @Test
  public void eachFactoryAcquisitionCreatesOneTemplateHandoff() {
    CachedJcrSession cachedSession = new CachedJcrSession( session );

    assertTrue( cachedSession.acquire() );
    assertTrue( cachedSession.consumeFactoryLease() );
    assertTrue( cachedSession.acquire() );
    assertTrue( cachedSession.consumeFactoryLease() );
    assertFalse( cachedSession.consumeFactoryLease() );

    cachedSession.release();
    cachedSession.release();
    cachedSession.retire();
    verify( session, times( 1 ) ).logout();
  }

  /** Verifies a failed handoff can be rolled back without leaving a stale factory lease for the next acquisition. */
  @Test
  public void failedFactoryHandoffCanBeFollowedByANewTemplateHandoff() {
    CachedJcrSession cachedSession = new CachedJcrSession( session );

    assertTrue( cachedSession.acquire() );
    cachedSession.releaseUnhandedFactoryLease();
    assertFalse( "A failed factory acquisition must not leave a pending handoff",
      cachedSession.consumeFactoryLease() );

    assertTrue( cachedSession.acquire() );
    assertTrue( "The next successful factory acquisition must have its own handoff",
      cachedSession.consumeFactoryLease() );
    cachedSession.release();
    cachedSession.retire();

    verify( session, times( 1 ) ).logout();
  }

  /** Verifies retirement with no active owners logs out immediately and remains idempotent. */
  @Test
  public void retirementWithoutActiveUsesLogsOutImmediatelyAndOnlyOnce() {
    CachedJcrSession cachedSession = new CachedJcrSession( session );

    cachedSession.retire();
    cachedSession.retire();

    verify( session, times( 1 ) ).logout();
  }

  /** Verifies an unmatched release is ignored rather than causing an unowned session to be logged out. */
  @Test
  public void unmatchedReleaseDoesNotLogOutSession() {
    CachedJcrSession cachedSession = new CachedJcrSession( session );

    cachedSession.release();

    verify( session, never() ).isLive();
    verify( session, never() ).logout();
  }

  /** Verifies rolling back a factory lease that was never taken is ignored and leaves the holder usable. */
  @Test
  public void unmatchedFactoryLeaseRollbackDoesNotLogOutSession() {
    CachedJcrSession cachedSession = new CachedJcrSession( session );

    cachedSession.releaseUnhandedFactoryLease();

    assertTrue( "The holder must remain usable after an ignored rollback", cachedSession.acquire() );
    verify( session, never() ).logout();
  }

  /** Verifies retirement does not invoke logout again when the underlying session is already closed. */
  @Test
  public void retirementDoesNotLogOutSessionThatIsAlreadyClosed() {
    when( session.isLive() ).thenReturn( false );
    CachedJcrSession cachedSession = new CachedJcrSession( session );

    cachedSession.retire();

    verify( session, never() ).logout();
  }

  /** Verifies a runtime failure from logout is contained by retirement cleanup. */
  @Test
  public void logoutFailureDoesNotEscapeRetirement() {
    doThrow( new IllegalStateException( "logout failed" ) ).when( session ).logout();
    CachedJcrSession cachedSession = new CachedJcrSession( session );

    cachedSession.retire();

    verify( session, times( 1 ) ).logout();
  }

  /**
   * Races retirement against the final release and verifies both paths claim at most one logout attempt.
   * A shared start latch maximizes overlap; worker failures are relayed to the test thread.
   */
  @Test
  public void concurrentRetirementAndFinalReleaseLogOutExactlyOnce() throws Exception {
    CachedJcrSession cachedSession = new CachedJcrSession( session );
    assertTrue( cachedSession.acquire() );

    CountDownLatch start = new CountDownLatch( 1 );
    CountDownLatch completed = new CountDownLatch( 2 );
    AtomicReference<Throwable> workerFailure = new AtomicReference<>();
    Thread retirement = new Thread( () -> runAfterSignal( start, completed, workerFailure, cachedSession::retire ) );
    Thread release = new Thread( () -> runAfterSignal( start, completed, workerFailure, cachedSession::release ) );
    retirement.start();
    release.start();
    start.countDown();

    assertTrue( "Lifecycle workers should complete", completed.await( 5, TimeUnit.SECONDS ) );
    assertNull( "Lifecycle worker failed", workerFailure.get() );
    verify( session, times( 1 ) ).logout();
  }

  /**
   * Waits for a shared start signal, invokes one lifecycle operation, and records any worker failure for the test.
   *
   * @param start latch that releases the worker into the race
   * @param completed latch counted down when the worker exits
   * @param failure first failure observed by any worker
   * @param operation lifecycle operation assigned to this worker
   */
  private void runAfterSignal( CountDownLatch start, CountDownLatch completed, AtomicReference<Throwable> failure,
                               Runnable operation ) {
    try {
      if ( !start.await( 5, TimeUnit.SECONDS ) ) {
        throw new AssertionError( "Timed out waiting to start lifecycle race" );
      }
      operation.run();
    } catch ( InterruptedException e ) {
      Thread.currentThread().interrupt();
      failure.compareAndSet( null, e );
    } catch ( RuntimeException | AssertionError e ) {
      failure.compareAndSet( null, e );
    } finally {
      completed.countDown();
    }
  }
}
