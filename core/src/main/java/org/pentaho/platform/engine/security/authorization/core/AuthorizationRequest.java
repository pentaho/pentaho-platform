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

package org.pentaho.platform.engine.security.authorization.core;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.engine.IAuthorizationAction;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationPrincipal;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRequest;
import org.springframework.util.Assert;

import java.util.Objects;

/**
 * The {@code AuthorizationRequest} class is a basic implementation of the {@link IAuthorizationRequest} interface.
 */
public class AuthorizationRequest implements IAuthorizationRequest {
  @NonNull
  private final IAuthorizationPrincipal principal;

  @NonNull
  private final IAuthorizationAction action;

  /**
   * Constructs an {@code AuthorizationRequest} with the specified principal and action.
   *
   * @param principal The principal (user, role, etc.) for whom the authorization is being evaluated.
   * @param action The action to be evaluated.
   * @throws IllegalArgumentException if the principal or action are {@code null}.
   */
  public AuthorizationRequest( @NonNull IAuthorizationPrincipal principal, @NonNull IAuthorizationAction action ) {
    Assert.notNull( principal, "Argument 'principal' is required" );
    Assert.notNull( action, "Argument 'action' is required" );

    this.principal = principal;
    this.action = action;
  }

  @NonNull
  @Override
  public IAuthorizationPrincipal getPrincipal() {
    return principal;
  }

  @NonNull
  @Override
  public IAuthorizationAction getAction() {
    return action;
  }

  // Helper method so that rules can easily evaluate dependent permissions, for the same principal.
  @NonNull
  @Override
  public IAuthorizationRequest withAction( @NonNull IAuthorizationAction action ) {
    return new AuthorizationRequest( principal, action );
  }

  @Override
  public boolean equals( Object o ) {
    if ( o == null || getClass() != o.getClass() ) {
      return false;
    }

    AuthorizationRequest that = (AuthorizationRequest) o;
    return Objects.equals( principal, that.principal )
      && Objects.equals( action, that.getAction() );
  }

  @Override
  public int hashCode() {
    int result = principal.hashCode();
    result = 31 * result + action.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return String.format(
      "%s [principal=%s, action=%s]",
      getClass().getSimpleName(),
      principal,
      action );
  }
}
