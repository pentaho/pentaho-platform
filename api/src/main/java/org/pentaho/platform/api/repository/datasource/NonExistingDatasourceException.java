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


package org.pentaho.platform.api.repository.datasource;

import org.pentaho.platform.api.util.PentahoCheckedChainedException;

public class NonExistingDatasourceException extends PentahoCheckedChainedException {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public NonExistingDatasourceException() {
    super();
  }

  public NonExistingDatasourceException( String msg ) {
    super( msg );
  }

  public NonExistingDatasourceException( Throwable cause ) {
    super( cause );
  }

  public NonExistingDatasourceException( String message, Throwable cause ) {
    super( message, cause );
  }
}
