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


package org.pentaho.platform.api.repository;

import org.pentaho.platform.api.util.PentahoChainedException;

public class RepositoryException extends PentahoChainedException {

  /**
   * 
   */
  private static final long serialVersionUID = -1842098457110711029L;

  /**
   * 
   */
  public RepositoryException() {
    super();
  }

  /**
   * @param message
   */
  public RepositoryException( final String message ) {
    super( message );
  }

  /**
   * @param message
   * @param reas
   */
  public RepositoryException( final String message, final Throwable reas ) {
    super( message, reas );
  }

  /**
   * @param reas
   */
  public RepositoryException( final Throwable reas ) {
    super( reas );
  }

}
