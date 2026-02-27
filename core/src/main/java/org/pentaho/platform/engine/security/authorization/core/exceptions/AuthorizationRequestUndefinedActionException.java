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

package org.pentaho.platform.engine.security.authorization.core.exceptions;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.exceptions.AuthorizationFailureException;
import org.springframework.util.Assert;

/**
 * The {@code AuthorizationRequestCycleException} exception is used to signal when the authorization process receives
 * a request having an undefined action.
 * <p>
 * An undefined action is one which is not defined in the system's
 * {@link org.pentaho.platform.api.engine.security.authorization.IAuthorizationActionService}.
 */
public class AuthorizationRequestUndefinedActionException extends AuthorizationFailureException {

  public AuthorizationRequestUndefinedActionException( @NonNull IAuthorizationRequest request ) {
    super( createMessage( request ) );
  }

  @NonNull
  private static String createMessage( @NonNull IAuthorizationRequest request ) {
    Assert.notNull( request, "Argument 'request' is required" );

    return String.format( "Authorization request references an undefined action: '%s'.",
      request.getAction() );
  }
}
