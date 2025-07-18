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

package org.pentaho.platform.api.engine.security.authorization.authng.exceptions;

/**
 * The {@code AuthorizationException} class is the base exception class for errors occurring during the authorization
 * process.
 */
public class AuthorizationException extends Exception {
  public AuthorizationException( String message ) {
    super( message );
  }

  public AuthorizationException( Throwable cause ) {
    super( cause );
  }

  public AuthorizationException( String message, Throwable cause ) {
    super( message, cause );
  }
}
