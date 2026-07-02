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

package org.pentaho.platform.api.engine.security.authorization.exceptions;

/**
 * The {@code AuthorizationFailureException} is the base class for exceptions that occur during the authorization
 * process.
 * <p>
 * This is a runtime exception, in part to avoid having to declare it in method signatures of the authorization APIs,
 * but also because it generally indicates unrecoverable errors.
 * <p>
 * Authorization failures may result from various issues, such as:
 * <ul>
 *   <li>A rule causing an evaluation cycle.</li>
 *   <li>A rule throwing a runtime exception during a call to its
 *       {@link org.pentaho.platform.api.engine.security.authorization.IAuthorizationRule#authorize} method.
 *   </li>
 *   <li>Passing the authorization process an invalid request, such as when its action is not defined.</li>
 * </ul>
 */
public class AuthorizationFailureException extends RuntimeException {
  public AuthorizationFailureException( String message ) {
    super( message );
  }

  public AuthorizationFailureException( String message, Throwable cause ) {
    super( message, cause );
  }
}
