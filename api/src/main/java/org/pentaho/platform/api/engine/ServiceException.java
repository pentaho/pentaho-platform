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


package org.pentaho.platform.api.engine;

public class ServiceException extends Exception {

  private static final long serialVersionUID = -2151267602926525247L;

  public ServiceException() {
    super();
  }

  public ServiceException( final String message ) {
    super( message );
  }

  public ServiceException( final String message, final Throwable reas ) {
    super( message, reas );
  }

  public ServiceException( final Throwable reas ) {
    super( reas );
  }
}
