package org.pentaho.platform.api.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Before;
import org.junit.Test;

public class PhaserServiceBarrierTest {

  PhaserServiceBarrier barrier;

  @Before
  public void setup(){
    barrier = new PhaserServiceBarrier();
  }


  @Test
  public void testHoldAndRelease() throws Exception {
    assertTrue( barrier.isAvailable() );
    final AtomicBoolean locked = new AtomicBoolean( false );

    Thread t1 = new Thread( new Runnable() {
      @Override public void run() {
        locked.set( true );
        try {
          barrier.awaitAvailability();
        } catch ( InterruptedException e ) {
          fail( e.getMessage() );
          return;
        }
        locked.set( false );
      }
    } );
    barrier.hold();
    t1.start();
    Thread.sleep( 100 );
    assertTrue( locked.get() );
    assertTrue( t1.getState() == Thread.State.WAITING );
    barrier.release();
    while( t1.isAlive() ){
      Thread.sleep( 10 );
    }

    assertFalse( locked.get() );

  }

  @Test
  public void testAwaitWithNoHold() throws Exception {
    assertTrue( barrier.isAvailable() );
    final AtomicBoolean locked = new AtomicBoolean( false );
    final AtomicBoolean secondlock = new AtomicBoolean( false );

    Thread t1 = new Thread( new Runnable() {
      @Override public void run() {
        locked.set( true );
        try {
          barrier.awaitAvailability();
        } catch ( InterruptedException e ) {
          fail( e.getMessage() );
          return;
        }
        locked.set( true );
        barrier.hold();
        try {
          barrier.awaitAvailability();
        } catch ( InterruptedException e ) {
          fail( e.getMessage() );
          return;
        }
        secondlock.set( true );
      }
    } );
    t1.start();
    while( locked.get() == false ){
      Thread.sleep( 10 );
    }
    barrier.release();
    while( t1.isAlive() ){
      Thread.sleep( 10 );
    }

    assertTrue( locked.get() );
    assertTrue( secondlock.get() );

  }

  @Test
  public void testGetHoldCount() throws Exception {
    assertEquals( 0, barrier.getHoldCount() );
    barrier.hold();
    barrier.hold();
    assertEquals( 2, barrier.getHoldCount() );
    barrier.release();
    assertEquals( 1, barrier.getHoldCount() );

  }

  @Test
  public void testIsAvailable() throws Exception {
    assertTrue( barrier.isAvailable() );
    barrier.hold();
    barrier.hold();
    assertFalse( barrier.isAvailable() );
    barrier.release();
    assertFalse( barrier.isAvailable() );
    barrier.release();
    assertTrue( barrier.isAvailable() );
  }

  @Test
  public void testTerminate() throws Exception {

    assertTrue( barrier.isAvailable() );
    final AtomicBoolean locked = new AtomicBoolean( false );
    final AtomicBoolean threwException = new AtomicBoolean( false );

    Thread t1 = new Thread( new Runnable() {
      @Override public void run() {
        barrier.hold();
        locked.set( true );
        try {
          barrier.awaitAvailability();
        } catch ( InterruptedException e ) {
          threwException.set( true );
          return;
        }
        locked.set( false );
      }
    } );

    t1.start();
    Thread.sleep( 100 );
    assertTrue( locked.get() );
    assertTrue( t1.getState() == Thread.State.WAITING );
    barrier.terminate();
    Thread.sleep( 100 );
    assertTrue( locked.get() );
    assertTrue( threwException.get() );
    assertTrue( barrier.isTerminated() );
  }

}