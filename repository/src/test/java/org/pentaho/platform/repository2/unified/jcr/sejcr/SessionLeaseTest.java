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
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import javax.jcr.Session;

import org.junit.Test;

/**
 * Unit tests for {@link SessionLease}. The lifecycle holder is real; only the JCR session is a test double, because
 * the behavior under test is when the holder allows that session to be logged out.
 */
public class SessionLeaseTest {

  private final Session session = mock( Session.class );

  /** A session without a lifecycle holder has nothing to defer, so the caller may use it as before. */
  @Test
  public void untrackedSessionGetsANoOpLease() {
    when( session.getAttribute( CachedJcrSession.SESSION_ATTRIBUTE ) ).thenReturn( null );

    Optional<SessionLease> lease = SessionLease.tryAcquire( session );

    assertTrue( lease.isPresent() );
    lease.get().close();
    verify( session, never() ).logout();
  }

  /** Eviction during the lease retires the session but must not close it until the lease is released. */
  @Test
  public void evictionDuringTheLeaseDefersLogoutUntilItIsReleased() {
    CachedJcrSession holder = trackedHolder();

    SessionLease lease = SessionLease.tryAcquire( session ).get();
    holder.retire();
    verify( session, never() ).logout();

    lease.close();
    verify( session, times( 1 ) ).logout();
  }

  /** Releasing a lease on a session that was never retired leaves it open for the cache to reuse. */
  @Test
  public void releasingALeaseOnALiveSessionDoesNotLogItOut() {
    trackedHolder();

    SessionLease.tryAcquire( session ).get().close();

    verify( session, never() ).logout();
  }

  /** Once the last lease of a retired session is gone the session is closed and cannot be leased again. */
  @Test
  public void aClosedSessionCannotBeLeased() {
    CachedJcrSession holder = trackedHolder();
    holder.retire();

    assertFalse( SessionLease.tryAcquire( session ).isPresent() );
  }

  /** A second close must not release another party's lease on the same session. */
  @Test
  public void closingTwiceReleasesOnlyOnce() {
    CachedJcrSession holder = trackedHolder();
    SessionLease first = SessionLease.tryAcquire( session ).get();
    SessionLease second = SessionLease.tryAcquire( session ).get();
    holder.retire();

    first.close();
    first.close();
    verify( session, never() ).logout();

    second.close();
    verify( session, times( 1 ) ).logout();
  }

  /** A session whose attributes cannot be read is unusable; the caller must reload instead. */
  @Test
  public void aSessionThatCannotReportItsHolderIsUnavailable() {
    when( session.getAttribute( CachedJcrSession.SESSION_ATTRIBUTE ) )
      .thenThrow( new IllegalStateException( "session is closed" ) );

    assertFalse( SessionLease.tryAcquire( session ).isPresent() );
  }

  /** Publishes a live lifecycle holder for the test session, as the cache factory does for real sessions. */
  private CachedJcrSession trackedHolder() {
    when( session.isLive() ).thenReturn( true );
    CachedJcrSession holder = new CachedJcrSession( session );
    when( session.getAttribute( CachedJcrSession.SESSION_ATTRIBUTE ) ).thenReturn( holder );
    return holder;
  }
}
