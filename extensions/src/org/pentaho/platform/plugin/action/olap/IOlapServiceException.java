/*
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
 * Copyright 2013 Pentaho Corporation.  All rights reserved.
 *
*/
package org.pentaho.platform.plugin.action.olap;

import javax.jcr.AccessDeniedException;

/**
 * Thrown by the {@link IOlapService} when something made a boo-boo.
 *
 * <p>See {@link Reason} for category of the failure. In general:
 *
 * <ul><li><b>{@link Reason#GENERAL}</b> will happen when there is an issue with one of the
 * connections, like bad credentials. See the root cause of the exception for
 * details.</li>
 *
 * <li><b>{@link Reason#ACCESS_DENIED}</b> when the repository's access rights do not allow
 * the provided session to perform the requested operation.</li>
 *
 * <li><b>{@link Reason#ALREADY_EXISTS}</b> when a connection was tentatively saved
 * but one of the same name already exists and the overwrite flag isn't on.</li><ul>
 */
public class IOlapServiceException extends RuntimeException {

  private static final long serialVersionUID = 1852374894433624504L;

  /**
   * Possible causes of an exception in the IOlapService.
   */
  public static enum Reason {
    /**
     * An error occurred. See the root cause for more details.
     */
    GENERAL,
    /**
     * The operation could not be completed because of the security
     * restrictions in effect for the user session passed as a parameter.
     */
    ACCESS_DENIED,
    /**
     * A connection of the same name already exists and the overwrite flag
     * was not used.
     */
    ALREADY_EXISTS;
    public static Reason convert( Throwable t ) {
      if ( t instanceof AccessDeniedException ) {
        return Reason.ACCESS_DENIED;
      }
      return Reason.GENERAL;
    }
  };

  private final Reason reason;

  public IOlapServiceException() {
    this( (String) null );
  }

  public IOlapServiceException( final String msg, final Throwable throwable ) {
    this( msg, throwable, Reason.GENERAL );
  }

  public IOlapServiceException( final String msg, final Throwable throwable, final Reason reason ) {
    super( msg, throwable );
    this.reason = reason;
  }

  public IOlapServiceException( final String msg ) {
    this( msg, Reason.GENERAL );
  }

  public IOlapServiceException( final String msg, final Reason reason ) {
    super( msg );
    this.reason = reason;
  }

  public IOlapServiceException( final Throwable throwable ) {
    this( throwable, Reason.GENERAL );
  }

  public IOlapServiceException( final Throwable throwable, final Reason reason ) {
    super( throwable );
    this.reason = reason;
  }

  /**
   * Provides the rough category of the exception.
   * {@link Reason} for more details.
   */
  public Reason getReason() {
    return reason;
  }
}
