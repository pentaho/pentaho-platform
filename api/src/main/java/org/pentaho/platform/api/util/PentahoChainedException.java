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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

/**
 *
 * This is the base class for all Pentaho exceptions. This class handles
 * chaining of exceptions so that it's possible to chain back to the root
 * of an exception.
 *
 */

package org.pentaho.platform.api.util;

import java.io.PrintStream;

/**
 * This is the base Pentaho Exception class that handles chained exceptions.
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
