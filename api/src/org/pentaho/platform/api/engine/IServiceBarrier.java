package org.pentaho.platform.api.engine;

/**
 * A basic barrier class. It can be used to allow others to block execution until certain conditions are meet.
 * <p/>
 * Barriers allow for extensible coordination between units of code unaware of the other. The coupling is to the barrier
 * instead of each other.
 * <p/>
 * <p/>
 * Created by nbaker on 1/27/15.
 */
public interface IServiceBarrier {
  /**
   * Add a hold on the barrier. The barrier will become unavailable until the hold is released.
   *
   * @return
   */
  int hold();

  /**
   * Release a hold on the barrier. If this was the final hold threads waiting for availability will unblock.
   *
   * @return
   */
  int release();

  /**
   * Return the number of holds on the barrier
   *
   * @return
   */
  int getHoldCount();

  /**
   * Check the availability of the barrier
   *
   * @return
   */
  boolean isAvailable();

  /**
   * Wait for the barrier to become available. Returns immediately if the barrier is available.
   *
   * @return
   * @throws InterruptedException
   */
  int awaitAvailability() throws InterruptedException;

  /**
   * Terminate the barrier. Any blocked threads will be interrupted.
   */
  void terminate();

  /**
   * Check if the barrier has been terminated.
   *
   * @return
   */
  boolean isTerminated();
}
