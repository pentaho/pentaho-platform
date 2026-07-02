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
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRule;
import org.pentaho.platform.api.engine.security.authorization.exceptions.AuthorizationFailureException;
import org.springframework.util.Assert;

/**
 * The {@code AuthorizationRuleException} exception is used to wrap exceptions thrown by a rule from its
 * {@link org.pentaho.platform.api.engine.security.authorization.IAuthorizationRule#authorize} method.
 */
public class AuthorizationRuleException extends AuthorizationFailureException {

  public AuthorizationRuleException( @NonNull IAuthorizationRequest request, @NonNull IAuthorizationRule<?> rule ) {
    super( createMessage( request, rule ) );
  }

  public AuthorizationRuleException( @NonNull IAuthorizationRequest request,
                                     @NonNull IAuthorizationRule<?> rule,
                                     @NonNull Throwable cause ) {
    super( createMessage( request, rule ), cause );
  }

  @NonNull
  private static String createMessage( @NonNull IAuthorizationRequest request, @NonNull IAuthorizationRule<?> rule ) {
    Assert.notNull( request, "Argument 'request' is required" );
    Assert.notNull( rule, "Argument 'rule' is required" );

    return String.format(
      "Error authorizing request: %s with rule: %s",
      request,
      rule );
  }
}
