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
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationUser;

import java.util.Objects;

/**
 * The {@code AuthorizationRequest} class is a basic implementation of the {@link IAuthorizationRequest} interface,
 */
public class AuthorizationRequest implements IAuthorizationRequest {
  @NonNull
  private final IAuthorizationUser user;

  @NonNull
  private final IAuthorizationAction action;

  /**
   * Constructs an {@code AuthorizationRequest} with the specified user and action name.
   *
   * @param user   The user for whom the authorization is being evaluated.
   * @param action The action to be evaluated.
   * @throws IllegalArgumentException if the user or action are null.
   */
  public AuthorizationRequest( @NonNull IAuthorizationUser user, @NonNull IAuthorizationAction action ) {
    this.user = Objects.requireNonNull( user );
    this.action = Objects.requireNonNull( action );
  }

  @NonNull
  @Override
  public IAuthorizationUser getUser() {
    return user;
  }

  @NonNull
  @Override
  public IAuthorizationAction getAction() {
    return action;
  }

  // Helper method so that rules can easily evaluate dependent permissions, for the same user.

  @NonNull
  @Override
  public IAuthorizationRequest withAction( @NonNull IAuthorizationAction action ) {
    return new AuthorizationRequest( user, action );
  }

  @Override
  public boolean equals( Object o ) {
    if ( o == null || getClass() != o.getClass() ) {
      return false;
    }

    AuthorizationRequest that = (AuthorizationRequest) o;
    return Objects.equals( user, that.user )

      // TODO: should we add #equals to the IAuthorizationAction's interface? And require implementations to update?
      // IAuthorizationAction's don't usually override equals, so we compare by name.
      && Objects.equals( action.getName(), that.getAction().getName() );
  }

  @Override
  public int hashCode() {
    return Objects.hash( user, action.getName() );
  }

  @Override
  public String toString() {
    return String.format(
      "%s [user=`%s`, action='%s']",
      getClass().getSimpleName(),
      user.getName(),
      action.getName() );
  }
}
