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


package org.pentaho.platform.web.http;

import org.pentaho.platform.api.util.PentahoChainedException;

public class UIException extends PentahoChainedException {

  /**
   * 
   */
  private static final long serialVersionUID = -1842098457110711029L;

  /**
   * 
   */
  public UIException() {
    super();
  }

  /**
   * @param message
   */
  public UIException( final String message ) {
    super( message );
  }

  /**
   * @param message
   * @param reas
   */
  public UIException( final String message, final Throwable reas ) {
    super( message, reas );
  }

  /**
   * @param reas
   */
  public UIException( final Throwable reas ) {
    super( reas );
  }

}
