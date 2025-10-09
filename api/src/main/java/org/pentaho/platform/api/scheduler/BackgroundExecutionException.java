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


package org.pentaho.platform.api.scheduler;

import org.pentaho.platform.api.util.PentahoCheckedChainedException;

public class BackgroundExecutionException extends PentahoCheckedChainedException {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public BackgroundExecutionException() {
    super();
  }

  public BackgroundExecutionException( String message ) {
    super( message );
  }

  public BackgroundExecutionException( Throwable cause ) {
    super( cause );
  }

  public BackgroundExecutionException( String message, Throwable cause ) {
    super( message, cause );
  }
}
