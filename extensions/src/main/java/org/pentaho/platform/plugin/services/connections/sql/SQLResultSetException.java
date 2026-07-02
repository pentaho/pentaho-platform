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
