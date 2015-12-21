/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2015 Pentaho Corporation..  All rights reserved.
 */
package org.pentaho.platform.api.engine;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * 
 * @author tkafalas
 *
 */
public class ServiceBarrierThreadTest {
  final static String BARRIER_ID = "JCRBarrier";

  /**
   * Test simulates 3 features waiting for the main thread to release the barrier before continuing.
   * 
   * @throws Exception
   */
  @Test
  public void testMultipleThreads1() throws Exception {

    final ServiceBarrierManager mngr = new ServiceBarrierManager();
    IServiceBarrier barrier = mngr.getServiceBarrier( BARRIER_ID );
    assertTrue( barrier.isAvailable() );

    // barrier.hold();
    barrier.hold();

    Thread t1 = new Thread( new ServiceTestThread( mngr, 1 ) );
    Thread t2 = new Thread( new ServiceTestThread( mngr, 2 ) );
    Thread t3 = new Thread( new ServiceTestThread( mngr, 3 ) );
    t1.start();
    t2.start();
    t3.start();

    Thread.sleep( 3000 );
    // barrier.release();
    assertTrue( t1.getState() == Thread.State.WAITING );
    assertTrue( t2.getState() == Thread.State.WAITING );
    assertTrue( t3.getState() == Thread.State.WAITING );
    barrier.release();

    while ( t1.isAlive() || t2.isAlive() || t3.isAlive() ) {
      Thread.sleep( 10 );
    }

    System.out.println( "barrier availability test 1 is " + barrier.isAvailable() + " "  + barrier.getHoldCount() );
  }

  /**
   * Test simulates 3 features waiting for the main thread to release the barrier before continuing. The main thread
   * terminates the 3 waiting threads, presumably due to fatal error.
   * 
   * @throws Exception
   */
  @Test
  public void testMultipleThreads2() throws Exception {

    final ServiceBarrierManager mngr = new ServiceBarrierManager();
    IServiceBarrier barrier = mngr.getServiceBarrier( BARRIER_ID );
    assertTrue( barrier.isAvailable() );

    // barrier.hold();
    barrier.hold();

    Thread t1 = new Thread( new ServiceTestThread( mngr, 1 ) );
    Thread t2 = new Thread( new ServiceTestThread( mngr, 2 ) );
    Thread t3 = new Thread( new ServiceTestThread( mngr, 3 ) );
    t1.start();
    t2.start();
    t3.start();

    Thread.sleep( 3000 );
    // barrier.release();
    assertTrue( t1.getState() == Thread.State.WAITING );
    assertTrue( t2.getState() == Thread.State.WAITING );
    assertTrue( t3.getState() == Thread.State.WAITING );
    barrier.terminate();

    while ( t1.isAlive() || t2.isAlive() || t3.isAlive() ) {
      Thread.sleep( 10 );
    }
    assertTrue( barrier.isTerminated() );
    System.out.println( "barrier availability test 2 is " + barrier.isAvailable() + " "  + barrier.getHoldCount() );
  }

  public static class ServiceTestThread implements Runnable {
    private int threadNum;
    ServiceBarrierManager mngr;

    public ServiceTestThread( ServiceBarrierManager mngr, int threadNum ) {
      this.mngr = mngr;
      this.threadNum = threadNum;
    }

    @Override
    public void run() {
      IServiceBarrier barrier = mngr.getServiceBarrier( BARRIER_ID );
      assertFalse( barrier.isAvailable() );
      try {
        System.out.println( "Thread " + threadNum + " waiting." );
        barrier.awaitAvailability();
        System.out.println( "Thread " + threadNum + " running" );
      } catch ( InterruptedException e ) {
        System.out.println( "Thread " + threadNum + " terminated" );
        e.printStackTrace();
        assertTrue( barrier.isTerminated() );
      } finally {
        barrier.release();
      }
    }
  }

}
