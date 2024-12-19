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


package org.pentaho.platform.api.data;

import org.pentaho.platform.api.util.PentahoCheckedChainedException;

public class DBDatasourceServiceException extends PentahoCheckedChainedException {

  /**
   * 
   */
  private static final long serialVersionUID = -6089798664483298023L;

  /**
   * 
   */
  public DBDatasourceServiceException() {
    super();
  }

  /**
   * @param message
   */
  public DBDatasourceServiceException( String message ) {
    super( message );
  }

  /**
   * @param message
   * @param reas
   */
  public DBDatasourceServiceException( String message, Throwable reas ) {
    super( message, reas );
  }

  /**
   * @param reas
   */
  public DBDatasourceServiceException( Throwable reas ) {
    super( reas );
  }

}
