/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.platform.api.engine;

import org.pentaho.platform.api.util.PentahoCheckedChainedException;

public class PentahoAccessControlException extends PentahoCheckedChainedException {

  /**
   * 
   */
  private static final long serialVersionUID = 5367573435716588545L;

  /**
   * 
   */
  public PentahoAccessControlException() {
    super();
  }

  /**
   * @param message
   */
  public PentahoAccessControlException( final String message ) {
    super( message );
  }

  /**
   * @param message
   * @param reas
   */
  public PentahoAccessControlException( final String message, final Throwable reas ) {
    super( message, reas );
  }

  /**
   * @param reas
   */
  public PentahoAccessControlException( final Throwable reas ) {
    super( reas );
  }
}
