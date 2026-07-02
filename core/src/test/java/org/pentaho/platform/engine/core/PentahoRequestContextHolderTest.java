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


package org.pentaho.platform.engine.core;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;

import java.util.Hashtable;
import java.util.Map;

import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.platform.api.engine.IPentahoRequestContext;
import org.pentaho.platform.engine.core.system.PentahoRequestContextHolder;

@SuppressWarnings( "nls" )
public class PentahoRequestContextHolderTest {

  // Mockery mockery = new Mockery();

  static Map<Thread, Boolean> threadFailureMap = new Hashtable<Thread, Boolean>();

  @Test
  public void testNullRequestContext() {
    final IPentahoRequestContext mockRequestContext = Mockito.mock( IPentahoRequestContext.class );
    PentahoRequestContextHolder.setRequestContext( mockRequestContext );
    assertSame( mockRequestContext, PentahoRequestContextHolder.getRequestContext() );
  }

  @Test
  public void testThreadsManageOwngetRequestContexts() throws InterruptedException {
    final IPentahoRequestContext mockRequestContext = Mockito.mock( IPentahoRequestContext.class );
    final IPentahoRequestContext threadMockRequestContext =
        Mockito.mock( IPentahoRequestContext.class, "threadMockRequestContext" );

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
    final IPentahoRequestContext mockRequestContext = Mockito.mock( IPentahoRequestContext.class );
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
