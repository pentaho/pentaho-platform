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
 * The {@code AuthorizationEvaluationCycleException} class is thrown when a cycle is detected during the authorization
 * evaluation process. This indicates that the evaluation logic has entered an infinite loop or recursive cycle, which
 * should not happen in a well-configured authorization system. A cycle occurs when an authorization rule evaluates
 * the permission for a user to perform an action, using the
 * {@link IAuthorizationContext#evaluate(IAuthorizationUser, String)} method, for a combination of user and action which
 * is already being evaluated in the current evaluation process.
 */
public class AuthorizationEvaluationCycleException extends AuthorizationEvaluationException {
  public AuthorizationEvaluationCycleException( String message ) {
    super( message );
  }

  public AuthorizationEvaluationCycleException( Throwable cause ) {
    super( cause );
  }

  public AuthorizationEvaluationCycleException( String message, Throwable cause ) {
    super( message, cause );
  }
}
