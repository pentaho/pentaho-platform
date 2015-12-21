package org.pentaho.platform.api.engine;

import java.util.concurrent.Phaser;

/**
 * Created by nbaker on 1/29/15.
 */
public class PhaserServiceBarrier implements IServiceBarrier {
  private final Phaser phaser = new Phaser();
  private String serviceId;

  public PhaserServiceBarrier( ) {

  }

  public PhaserServiceBarrier( String serviceId ) {
    this.serviceId = serviceId;
  }

  @Override public int hold() {
    return phaser.register();
  }

  @Override public int release() {
    return phaser.arriveAndDeregister();
  }

  @Override public int getHoldCount() {
    return phaser.getUnarrivedParties();
  }

  @Override public boolean isAvailable() {
    return phaser.getUnarrivedParties() <= 0;
  }

  @Override public int awaitAvailability() throws InterruptedException {
    if ( isAvailable() ) {
      return phaser.getPhase();
    }
    phaser.register();
    phaser.arrive();
    int phase = phaser.awaitAdvanceInterruptibly( phaser.getPhase() );
    if ( phaser.isTerminated() ) {
      throw new InterruptedException();
    }
    return phase;
  }

  @Override public void terminate() {
    phaser.forceTermination();
  }

  @Override public boolean isTerminated() {
    return phaser.isTerminated();
  }
}
