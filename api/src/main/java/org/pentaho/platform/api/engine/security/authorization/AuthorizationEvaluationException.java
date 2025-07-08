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

package org.pentaho.platform.api.engine.security.authorization;

/**
 * The {@code AuthorizationEvaluationException} class is the base exception class for exceptions thrown when an error
 * occurs during the authorization evaluation process.
 */
public class AuthorizationEvaluationException extends Exception {
  public AuthorizationEvaluationException( String message ) {
    super( message );
  }

  public AuthorizationEvaluationException( Throwable cause ) {
    super( cause );
  }

  public AuthorizationEvaluationException( String message, Throwable cause ) {
    super( message, cause );
  }
}
