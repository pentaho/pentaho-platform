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



package org.pentaho.platform.api.engine;

public class ServiceInitializationException extends Exception {

  private static final long serialVersionUID = -2151267602926525247L;

  public ServiceInitializationException() {
    super();
  }

  public ServiceInitializationException( final String message ) {
    super( message );
  }

  public ServiceInitializationException( final String message, final Throwable reas ) {
    super( message, reas );
  }

  public ServiceInitializationException( final Throwable reas ) {
    super( reas );
  }
}
