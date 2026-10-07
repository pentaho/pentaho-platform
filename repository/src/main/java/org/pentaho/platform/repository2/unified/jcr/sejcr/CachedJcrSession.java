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

  static final String SESSION_ATTRIBUTE = "cached_jcr_session";

  private final Session session;
  private int activeUses;
  private boolean retired;
  private boolean logoutStarted;

  CachedJcrSession( Session session ) {
    this.session = session;
  }

  Session getSession() {
    return session;
  }

  synchronized boolean acquire() {
    if ( retired || logoutStarted ) {
      return false;
    }
    activeUses++;
    return true;
  }

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

  void retire() {
    boolean shouldLogout;
    synchronized ( this ) {
      retired = true;
      shouldLogout = markLogoutIfReady();
    }
    logoutIfRequired( shouldLogout );
  }

  private boolean markLogoutIfReady() {
    if ( retired && activeUses == 0 && !logoutStarted ) {
      logoutStarted = true;
      return true;
    }
    return false;
  }

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