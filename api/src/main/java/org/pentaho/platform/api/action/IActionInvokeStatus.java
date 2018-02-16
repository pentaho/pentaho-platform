/*!
 *
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
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

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
