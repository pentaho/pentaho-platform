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
import org.pentaho.platform.api.engine.IPentahoRequestContext;
import org.pentaho.platform.engine.core.system.PentahoRequestContextHolder;

import java.util.Hashtable;
import java.util.Map;

import static org.junit.Assert.*;

@SuppressWarnings( "nls" )
public class PentahoRequestContextHolderTest {

  Mockery mockery = new Mockery();

  static Map<Thread, Boolean> threadFailureMap = new Hashtable<Thread, Boolean>();

  @Test
  public void testNullRequestContext() {
    final IPentahoRequestContext mockRequestContext = mockery.mock( IPentahoRequestContext.class );
    PentahoRequestContextHolder.setRequestContext( mockRequestContext );
    assertSame( mockRequestContext, PentahoRequestContextHolder.getRequestContext() );
    PentahoRequestContextHolder.setRequestContext( null );
    assertNull( "request context should be null", PentahoRequestContextHolder.getRequestContext() );
  }

  @Test
  public void testThreadsManageOwngetRequestContexts() throws InterruptedException {
    final IPentahoRequestContext mockRequestContext = mockery.mock( IPentahoRequestContext.class );
    final IPentahoRequestContext threadMockRequestContext =
        mockery.mock( IPentahoRequestContext.class, "threadMockRequestContext" );

    PentahoRequestContextHolder.setRequestContext( mockRequestContext );
    assertSame( mockRequestContext, PentahoRequestContextHolder.getRequestContext() );

    Thread t = new Thread( "testThreadsManageOwngetRequestContexts-1" ) {
      public void run() {
        PentahoRequestContextHolder.setRequestContext( threadMockRequestContext );
        if ( PentahoRequestContextHolder.getRequestContext() != threadMockRequestContext ) {
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
    assertSame( "(while child thread runs) parent thread should still have it's original request context",
        mockRequestContext, PentahoRequestContextHolder.getRequestContext() );
    t.join( 2000 );
    assertSame( "(when child thread has terminated) parent thread should still have it's original request context",
        mockRequestContext, PentahoRequestContextHolder.getRequestContext() );

    for ( boolean failure : threadFailureMap.values() ) {
      assertFalse( "An assertion in a thread failed.  Check the log for what failed", failure );
    }
  }

  @Test
  public void testThreadedInheritsRequestContext() throws InterruptedException {
    final IPentahoRequestContext mockRequestContext = mockery.mock( IPentahoRequestContext.class );
    PentahoRequestContextHolder.setRequestContext( mockRequestContext );

    Thread t = new Thread( "testThreadedInheritsRequestContext-1" ) {
      public void run() {
        if ( PentahoRequestContextHolder.getRequestContext() != mockRequestContext ) {
          System.err.println( getName() + " - should have inherited request context from parent thread" );
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
