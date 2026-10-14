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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Session;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A short claim on a cached JCR session, for code that uses a session outside any {@link PentahoJcrTemplate}
 * callback, such as lazily loaded repository objects that outlive the callback that created them.
 *
 * <p>While the lease is held, eviction can retire the session but cannot log it out; the logout happens when the
 * last lease is released. This closes the window between checking that a session is live and using it.</p>
 *
 * <p>Typical use:</p>
 * <pre>
 * Optional&lt;SessionLease&gt; lease = SessionLease.tryAcquire( session );
 * if ( lease.isPresent() ) {
 *   try ( SessionLease held = lease.get() ) {
 *     // use the session
 *   }
 * } else {
 *   // the session is already closed or closing: obtain a fresh one
 * }
 * </pre>
 */
public final class SessionLease implements AutoCloseable {
  private static final Logger LOG = LoggerFactory.getLogger( SessionLease.class );

  /** Shared lease for sessions without a lifecycle holder; there is nothing to retain or release. */
  private static final SessionLease UNTRACKED = new SessionLease( null );

  private final CachedJcrSession holder;
  private final AtomicBoolean released = new AtomicBoolean();

  private SessionLease( CachedJcrSession holder ) {
    this.holder = holder;
  }

  /**
   * Tries to retain a session for the duration of the caller's use.
   *
   * @param session the session about to be used
   * @return a lease to close after use; a no-op lease when the session is not lifecycle-tracked (it then has no
   * deferred logout to protect against); or empty when the session is tracked but already closed or closing, in
   * which case the caller must not use it
   */
  public static Optional<SessionLease> tryAcquire( Session session ) {
    Object attribute;
    try {
      attribute = session.getAttribute( CachedJcrSession.SESSION_ATTRIBUTE );
    } catch ( RuntimeException e ) {
      LOG.debug( "Could not read the lifecycle holder of a JCR session; treating it as unavailable", e );
      return Optional.empty();
    }
    if ( !( attribute instanceof CachedJcrSession ) ) {
      return Optional.of( UNTRACKED );
    }
    CachedJcrSession cachedSession = (CachedJcrSession) attribute;
    return cachedSession.acquireForBoundUse() ? Optional.of( new SessionLease( cachedSession ) ) : Optional.empty();
  }

  /** Releases the claim; the session is logged out now if it was retired in the meantime. Safe to call twice. */
  @Override
  public void close() {
    if ( holder != null && released.compareAndSet( false, true ) ) {
      holder.release();
    }
  }
}
