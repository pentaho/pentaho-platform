/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.platform.api.repository.datasource;

import org.pentaho.platform.api.util.PentahoCheckedChainedException;

public class DuplicateDatasourceException extends PentahoCheckedChainedException {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public DuplicateDatasourceException() {
    super();
  }

  public DuplicateDatasourceException( String msg ) {

  }

  /**
   * @param message
   * @param reas
   */
  public DuplicateDatasourceException( String message, Throwable reas ) {
    super( message, reas );
  }

  /**
   * @param reas
   */
  public DuplicateDatasourceException( Throwable reas ) {
    super( reas );
  }
}
