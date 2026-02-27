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

import org.pentaho.platform.api.util.PentahoCheckedChainedException;

public class SolutionRepositoryException extends PentahoCheckedChainedException {

  /**
   * 
   */
  private static final long serialVersionUID = -3797348039172526704L;

  /**
   * 
   */
  public SolutionRepositoryException() {
    super();
  }

  /**
   * @param message
   */
  public SolutionRepositoryException( final String message ) {
    super( message );
  }

  /**
   * @param message
   * @param reas
   */
  public SolutionRepositoryException( final String message, final Throwable reas ) {
    super( message, reas );
  }

  /**
   * @param reas
   */
  public SolutionRepositoryException( final Throwable reas ) {
    super( reas );
  }

}
