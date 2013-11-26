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

package org.pentaho.platform.plugin.services.connections.sql;

import org.pentaho.platform.api.util.PentahoChainedException;

/**
 * This exception just signals that an exception occurred during DB interaction
 */
public class SQLResultSetException extends PentahoChainedException {

  private static final long serialVersionUID = 1063956390289262889L;

  /**
   * lame ass ctor that really shouldn't be used.
   * 
   */
  public SQLResultSetException() {
  }

  public SQLResultSetException( final String message, final Throwable reas ) {
    super( message, reas );
  }

  public SQLResultSetException( final String message ) {
    super( message );
  }

  public SQLResultSetException( final Throwable reas ) {
    super( reas );
  }
}
