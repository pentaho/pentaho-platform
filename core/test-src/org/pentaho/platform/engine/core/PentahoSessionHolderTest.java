/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.engine.core;

import org.jmock.Mockery;
import org.junit.Test;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.StandaloneSession;

import java.util.Hashtable;
import java.util.Map;

import static org.junit.Assert.*;

@SuppressWarnings( "nls" )
public class PentahoSessionHolderTest {

  Mockery mockery = new Mockery();

  static Map<Thread, Boolean> threadFailureMap = new Hashtable<Thread, Boolean>();

  @Test
  public void testStandaloneSessionLifecycle() {
    PentahoSessionHolder.removeSession();
    assertNull( "session should be null at first", PentahoSessionHolder.getSession() );

    StandaloneSession session = new StandaloneSession();
    PentahoSessionHolder.setSession( session );
    assertSame( "StandaloneSession was not set as the thread-bound session", session,
      PentahoSessionHolder.getSession() );

    PentahoSessionHolder.removeSession();
    assertNull( "session should be null after a remove", PentahoSessionHolder.getSession() );
  }

  @Test
  public void testNullSession() {
    final IPentahoSession mockSession = mockery.mock( IPentahoSession.class );
    PentahoSessionHolder.setSession( mockSession );
    assertSame( mockSession, PentahoSessionHolder.getSession() );
    PentahoSessionHolder.removeSession();
    assertNull( "session should be null", PentahoSessionHolder.getSession() );
  }

  @Test
  public void testThreadsManageOwnSessions() throws InterruptedException {
    final IPentahoSession mockSession = mockery.mock( IPentahoSession.class );
    final IPentahoSession threadMockSession = mockery.mock( IPentahoSession.class, "threadMockSession" );

    PentahoSessionHolder.setSession( mockSession );
    assertSame( mockSession, PentahoSessionHolder.getSession() );

    Thread t = new Thread( "testThreadsManageOwnSessions-1" ) {
      public void run() {
        PentahoSessionHolder.setSession( threadMockSession );
        if ( PentahoSessionHolder.getSession() != threadMockSession ) {
          threadFailureMap.put( this, true );
        }
        try {
          sleep( 1000 );
        } catch ( InterruptedException e ) {
          e.printStackTrace();
        }
      }
    };
    t.start();
    assertSame( "(while child thread runs) parent thread should still have it's original session", mockSession,
        PentahoSessionHolder.getSession() );
    t.join( 2000 );
    assertSame( "(when child thread has terminated) parent thread should still have it's original session",
        mockSession, PentahoSessionHolder.getSession() );

    for ( boolean failure : threadFailureMap.values() ) {
      assertFalse( "An assertion in a thread failed.  Check the log for what failed", failure );
    }
  }

  @Test
  public void testThreadedInheritsSession() throws InterruptedException {
    final IPentahoSession mockSession = mockery.mock( IPentahoSession.class );
    PentahoSessionHolder.setSession( mockSession );

    Thread t = new Thread( "testThreadedInheritsSession-1" ) {
      public void run() {
        if ( PentahoSessionHolder.getSession() != mockSession ) {
          System.err.println( getName() + " - should have inherited session from parent thread" );
          threadFailureMap.put( this, true );
        }
      }
    };
    t.start();
    t.join( 5000 );

    for ( boolean failure : threadFailureMap.values() ) {
      assertFalse( "An assertion in a thread failed.  Check the log for what failed", failure );
    }
  }
}
