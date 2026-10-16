/* ****************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/

package org.pentaho.platform.repository2.unified.jcr.sejcr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Session;

/**
 * Coordinates use and eviction of one cached JCR session. Once retired, the session cannot be acquired again and is
 * logged out as soon as its last active user releases it.
 */
final class CachedJcrSession {
  private static final Logger LOG = LoggerFactory.getLogger( CachedJcrSession.class );

  /** Session attribute under which the lifecycle holder is published to template and factory code. */
  static final String SESSION_ATTRIBUTE = "cached_jcr_session";

  private final Session session;
  /** Active factory handoffs, template callbacks, and transaction bindings retaining this session. */
  private int activeUses;
  /** Factory acquisitions not yet transferred to a template or explicitly rolled back. */
  private int pendingFactoryLeases;
  /** Whether the cache or owner has permanently made this holder unavailable for new cache acquisitions. */
  private boolean retired;
  /** Whether the single logout attempt has been claimed by a thread. */
  private boolean logoutStarted;

  /**
   * Creates a lifecycle holder for a repository session.
   *
   * @param session the JCR session whose use and logout lifecycle this instance coordinates
   */
  CachedJcrSession( Session session ) {
    this.session = session;
  }

  /**
   * Returns the underlying JCR session.
   *
   * @return the coordinated session
   */
  Session getSession() {
    return session;
  }

  /**
   * Starts a factory-to-template handoff if the session has not been retired.
   * Both an active-use lease and a pending factory lease are recorded; the latter must be consumed by
   * {@link #consumeFactoryLease()} or rolled back by {@link #releaseUnhandedFactoryLease()}.
   *
   * @return {@code true} when the caller owns a new factory lease; {@code false} after retirement or logout begins
   */
  synchronized boolean acquire() {
    if ( retired || logoutStarted ) {
      return false;
    }
    activeUses++;
    pendingFactoryLeases++;
    return true;
  }

  /**
   * Transfers one pending factory lease to a template execution without changing the active-use count.
   *
   * @return {@code true} if a pending factory lease was consumed; {@code false} if no lease was available
   */
  synchronized boolean consumeFactoryLease() {
    if ( pendingFactoryLeases == 0 ) {
      return false;
    }
    pendingFactoryLeases--;
    return true;
  }

  /**
   * Retains a session that is already bound to a Spring operation, including a nested callback after cache retirement.
   * A retired holder may be extended only while an existing active use still keeps the session alive.
   *
   * @return {@code true} if the bound use was retained; {@code false} if logout has started or no active use remains
   */
  synchronized boolean acquireForBoundUse() {
    if ( logoutStarted || ( retired && activeUses == 0 ) ) {
      return false;
    }
    activeUses++;
    return true;
  }

  /**
   * Releases one active-use lease and logs out the session if retirement has made this the final lease.
   */
  void release() {
    boolean shouldLogout;
    synchronized ( this ) {
      if ( activeUses == 0 ) {
        LOG.warn( "Ignoring unmatched release for cached JCR session {}", System.identityHashCode( session ) );
        return;
      }
      activeUses--;
      shouldLogout = markLogoutIfReady();
    }
    logoutIfRequired( shouldLogout );
  }

  /**
   * Rolls back a factory lease that could not be handed to a template and releases its active-use protection.
   * If retirement is pending, this release may trigger the session's single logout attempt.
   */
  void releaseUnhandedFactoryLease() {
    boolean shouldLogout;
    synchronized ( this ) {
      if ( pendingFactoryLeases > 0 ) {
        pendingFactoryLeases--;
      }
      if ( activeUses == 0 ) {
        LOG.warn( "Ignoring unmatched factory lease release for cached JCR session {}",
          System.identityHashCode( session ) );
        return;
      }
      activeUses--;
      shouldLogout = markLogoutIfReady();
    }
    logoutIfRequired( shouldLogout );
  }

  /**
   * Rolls back a factory lease only if no template callback has consumed it. A transaction begin takes a factory
   * lease that only a template callback consumes; a transaction that never runs one must hand the lease back when it
   * completes, or the session could never be closed after eviction. Does nothing when no lease is pending.
   */
  void releaseUnconsumedFactoryLease() {
    synchronized ( this ) {
      if ( pendingFactoryLeases == 0 ) {
        return;
      }
    }
    releaseUnhandedFactoryLease();
  }

  /**
   * Permanently prevents new cache acquisitions and logs out immediately when no active leases remain.
   * Repeated calls are safe; logout is claimed at most once.
   */
  void retire() {
    logoutIfRequired( markRetired() );
  }

  /**
   * Permanently prevents new cache acquisitions without performing any repository I/O. This is the state change
   * of {@link #retire()} on its own, so a caller that must not block (the cache's removal listener) can retire the
   * holder immediately and run the logout elsewhere.
   * <p>
   * If the session is still in use, its last release logs it out and this method returns {@code false}. If it is
   * idle, this call claims the single logout and returns {@code true}; the caller must then call
   * {@link #performClaimedLogout()} exactly once.
   *
   * @return {@code true} when this call claimed the logout and the caller must perform it
   */
  synchronized boolean markRetired() {
    retired = true;
    return markLogoutIfReady();
  }

  /**
   * Logs out the session after {@link #markRetired()} returned {@code true}. Safe to run on any thread; a failing
   * logout is logged and contained.
   */
  void performClaimedLogout() {
    logoutIfRequired( true );
  }

  /** Claims the sole logout attempt when retirement and the final release have both occurred. */
  private boolean markLogoutIfReady() {
    if ( retired && activeUses == 0 && !logoutStarted ) {
      logoutStarted = true;
      return true;
    }
    return false;
  }

  /** Performs the claimed logout outside the holder monitor so repository I/O cannot block state transitions. */
  private void logoutIfRequired( boolean shouldLogout ) {
    if ( !shouldLogout ) {
      return;
    }
    try {
      if ( session.isLive() ) {
        session.logout();
      }
    } catch ( RuntimeException e ) {
      LOG.warn( "Could not log out retired cached JCR session {}", System.identityHashCode( session ), e );
    }
  }
}