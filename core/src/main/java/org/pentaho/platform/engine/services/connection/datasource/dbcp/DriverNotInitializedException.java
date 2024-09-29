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


package org.pentaho.platform.engine.services.connection.datasource.dbcp;

public class DriverNotInitializedException extends RuntimeException {

  public DriverNotInitializedException() {
  }

  public DriverNotInitializedException( String message ) {
    super( message );
  }


  public DriverNotInitializedException( String message, Throwable reas ) {
    super( message, reas );
  }

  public DriverNotInitializedException( Throwable reas ) {
    super( reas );
  }

}
