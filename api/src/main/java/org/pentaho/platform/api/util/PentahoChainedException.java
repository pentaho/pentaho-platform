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


/**
 *
 * This is the base class for all Hitachi Vantara exceptions. This class handles
 * chaining of exceptions so that it's possible to chain back to the root
 * of an exception.
 *
 */

package org.pentaho.platform.api.util;

import java.io.PrintStream;

/**
 * This is the base Hitachi Vantara Exception class that handles chained exceptions.
 */
public class PentahoChainedException extends RuntimeException {

  /**
   * 
   */
  private static final long serialVersionUID = -2278229062123864979L;

  // private static final String CAUSEDBY = Messages.getString("PENTCHEXCEPT.ERROR_CAUSEDBY"); // Need to NLS... //$NON-NLS-1$

  public PentahoChainedException() {
    super();
  }

  /**
   * Constructor
   * 
   * @param message
   *          The message to be carried by the exception.
   */
  public PentahoChainedException( final String message ) {
    super( message );
  }

  /**
   * Constructor
   * 
   * @param message
   *          The message.
   * @param reas
   *          The root cause of the exception.
   */
  public PentahoChainedException( final String message, final Throwable reas ) {
    super( message, reas );
  }

  /**
   * Constructor
   * 
   * @param reas
   *          The cause of this exception
   */
  public PentahoChainedException( final Throwable reas ) {
    super( reas );
  }

  /**
   * Gets the root cause of the exception.
   */
  public Throwable getRootCause() {
    Throwable aReason = this;
    Throwable lastReason = null;
    while ( ( aReason != null ) ) {
      lastReason = aReason;
      aReason = aReason.getCause();
    }
    return lastReason;
  }

  /**
   * Prints the exception trace to the specified print writer
   */
  @Override
  public synchronized void printStackTrace( final java.io.PrintWriter pw ) {
    super.printStackTrace( pw );
  }

  /**
   * Prints the exception trace to the specified print stream.
   */
  @Override
  public synchronized void printStackTrace( final PrintStream ps ) {
    super.printStackTrace( ps );
  }

}
