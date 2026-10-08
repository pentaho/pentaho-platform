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

@RunWith( MockitoJUnitRunner.class )
public class CachedJcrSessionTest {

  @Mock private Session session;

  @Before
  public void setUp() {
    when( session.isLive() ).thenReturn( true );
  }

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

  @Test
  public void boundUseCannotReviveSessionAfterFinalLeaseWasReleased() {
    CachedJcrSession cachedSession = new CachedJcrSession( session );
    cachedSession.retire();

    assertFalse( cachedSession.acquireForBoundUse() );
    verify( session, times( 1 ) ).logout();
  }

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

  @Test
  public void retirementWithoutActiveUsesLogsOutImmediatelyAndOnlyOnce() {
    CachedJcrSession cachedSession = new CachedJcrSession( session );

    cachedSession.retire();
    cachedSession.retire();

    verify( session, times( 1 ) ).logout();
  }

  @Test
  public void unmatchedReleaseDoesNotLogOutSession() {
    CachedJcrSession cachedSession = new CachedJcrSession( session );

    cachedSession.release();

    verify( session, never() ).isLive();
    verify( session, never() ).logout();
  }

  @Test
  public void retirementDoesNotLogOutSessionThatIsAlreadyClosed() {
    when( session.isLive() ).thenReturn( false );
    CachedJcrSession cachedSession = new CachedJcrSession( session );

    cachedSession.retire();

    verify( session, never() ).logout();
  }

  @Test
  public void logoutFailureDoesNotEscapeRetirement() {
    doThrow( new IllegalStateException( "logout failed" ) ).when( session ).logout();
    CachedJcrSession cachedSession = new CachedJcrSession( session );

    cachedSession.retire();

    verify( session, times( 1 ) ).logout();
  }

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
