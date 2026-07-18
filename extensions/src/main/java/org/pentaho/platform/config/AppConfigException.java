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



package org.pentaho.platform.config;

public class AppConfigException extends CheckedException {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public AppConfigException() {
    super();
  }

  public AppConfigException( String message ) {
    super( message );
  }

  public AppConfigException( String message, Throwable cause ) {
    super( message, cause );
  }

  public AppConfigException( Throwable cause ) {
    super( cause );
  }
}
