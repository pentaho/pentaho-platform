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


package org.pentaho.platform.api.action;

/**
 * An API for the object representation of action invocation status, allows us to pass back status
 * of action invocation, whether any exceptions occurred during invocation and whether the action
 * needs to be retired.
 */
public interface IActionInvokeStatus {

  void setRequiresUpdate( final boolean requiresUpdate );

  /**
   * Returns true if the {@link IAction} that was just invoked needs to be resubmitted. Used for scheduling purposes
   * only.
   *
   * @return true if the {@link IAction} that was just invoked needs to be resubmitted and false otherwise
   */
  boolean requiresUpdate();

  void setThrowable( final Throwable throwable );

  /**
   * Returns a {@link Throwable} instance, if any occurred when the {@link IAction} was being invoked.
   *
   * @return a {@link Throwable} instance, if any occurred when the {@link IAction} was being invoked
   */
  Throwable getThrowable();

  /**
   * Returns the object representing the stream provider containing the file associated with the given action.
   *
   * @return the object representing the stream provider containing the file associated with the given action.
   */
  Object getStreamProvider();

  void setStreamProvider( final Object streamProvider );

  /**
   * Return the success/failure of the execution. Added default method to maintain backward compatibility.
   * @return boolean
   */
  default boolean isExecutionSuccessful() {
    return true;
  }

  /**
   * Set the execution status. Added default method to maintain backward compatibility.
   * @param status boolean
   */
  default void setExecutionStatus( boolean status ) {
  }
}
