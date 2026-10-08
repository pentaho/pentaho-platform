/*! ******************************************************************************
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

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** Opt-in high-cardinality lifecycle stress test; intentionally excluded from the default Surefire unit-test run. */
public class CachedJcrSessionStressIT {

  @Test
  public void twoHundredThousandSimultaneouslyLiveHoldersRetireAndReleaseExactlyOnce() throws Exception {
    int count = 200_000;
    int partitions = 16;
    CachedJcrSession[] holders = new CachedJcrSession[ count ];
    GuavaCachePoolPentahoJcrSessionStressIT.Metrics metrics =
      new GuavaCachePoolPentahoJcrSessionStressIT.Metrics();
    for ( int i = 0; i < count; i++ ) {
      holders[ i ] = new CachedJcrSession( GuavaCachePoolPentahoJcrSessionStressIT.newSession( metrics ) );
      assertTrue( holders[ i ].acquire() );
      assertTrue( holders[ i ].consumeFactoryLease() );
      assertTrue( holders[ i ].acquireForBoundUse() );
      holders[ i ].release();
    }
    assertEquals( "All 200,000 session objects must be live before the race", 0, metrics.closed.get() );
    ExecutorService executor = Executors.newFixedThreadPool( partitions * 2 );
    CountDownLatch ready = new CountDownLatch( partitions * 2 );
    CountDownLatch start = new CountDownLatch( 1 );
    List<Future<?>> futures = new ArrayList<>();
    long before = System.nanoTime();

    try {
      for ( int partition = 0; partition < partitions; partition++ ) {
        int from = count * partition / partitions;
        int to = count * ( partition + 1 ) / partitions;
        futures.add( executor.submit( () -> race( ready, start, holders, from, to, true ) ) );
        futures.add( executor.submit( () -> race( ready, start, holders, from, to, false ) ) );
      }
      assertTrue( ready.await( 30, TimeUnit.SECONDS ) );
      start.countDown();
      long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos( 30 );
      for ( Future<?> future : futures ) {
        future.get( Math.max( 1, deadline - System.nanoTime() ), TimeUnit.NANOSECONDS );
      }
      assertEquals( count, metrics.closed.get() );
      assertEquals( 0, metrics.duplicateLogouts.get() );
      for ( CachedJcrSession holder : holders ) {
        assertFalse( holder.getSession().isLive() );
        assertFalse( "Retirement must be permanent", holder.acquire() );
        assertFalse( "A bound use must not revive a disposed session", holder.acquireForBoundUse() );
        assertEquals( 1,
          ( (GuavaCachePoolPentahoJcrSessionStressIT.LightweightSession) holder.getSession() ).logoutCalls );
      }
      org.slf4j.LoggerFactory.getLogger( CachedJcrSessionStressIT.class ).info(
        "JCR-HOLDER-STRESS simultaneousObjects={} workers={} raceMs={} closed={}",
        count, partitions * 2, ( System.nanoTime() - before ) / 1_000_000.0, metrics.closed.get() );
    } finally {
      start.countDown();
      futures.forEach( future -> future.cancel( true ) );
      executor.shutdownNow();
      assertTrue( "All holder workers must terminate", executor.awaitTermination( 30, TimeUnit.SECONDS ) );
      for ( CachedJcrSession holder : holders ) {
        if ( holder.getSession().isLive() ) {
          holder.getSession().logout();
        }
      }
    }
  }

  private void race( CountDownLatch ready, CountDownLatch start, CachedJcrSession[] holders, int from, int to,
                     boolean retire ) {
    ready.countDown();
    try {
      if ( !start.await( 30, TimeUnit.SECONDS ) ) {
        throw new AssertionError( "Timed out waiting for holder race" );
      }
      for ( int i = from; i < to; i++ ) {
        if ( retire ) {
          holders[ i ].retire();
          holders[ i ].retire();
        } else {
          holders[ i ].release();
        }
      }
    } catch ( InterruptedException e ) {
      Thread.currentThread().interrupt();
      throw new AssertionError( "Holder worker interrupted", e );
    }
  }
}
