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

public class SolutionRepositoryServiceException extends PentahoCheckedChainedException {

  /**
   * 
   */
  private static final long serialVersionUID = -1842098457110711029L;

  /**
   * 
   */
  public SolutionRepositoryServiceException() {
    super();
  }

  /**
   * @param message
   */
  public SolutionRepositoryServiceException( final String message ) {
    super( message );
  }

  /**
   * @param message
   * @param reas
   */
  public SolutionRepositoryServiceException( final String message, final Throwable reas ) {
    super( message, reas );
  }

  /**
   * @param reas
   */
  public SolutionRepositoryServiceException( final Throwable reas ) {
    super( reas );
  }

}
