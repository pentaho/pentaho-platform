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

package org.pentaho.platform.api.engine.security.authorization.authng;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.engine.IAuthorizationAction;

import java.util.Objects;

/**
 * The {@code AuthorizationRequest} class represents an authorization request for a user to perform an action.
 * <p>
 * This type can be derived to include any additional information characterizing an authorization request. In this case,
 * the {@link #withAction(IAuthorizationAction)} method should be overridden to ensure that the new request includes all
 * properties of the original request, except for the action, which will be replaced by the new one.
 * <p>
 * Equality is based on the user, action and any other key properties, meaning two requests are considered
 * equal if they have the same user, action and value of the other key properties.
 * The {@link Object#equals(Object)} and {@link Object#hashCode()} methods must ensure this behavior.
 * <p>
 * The string representation of this request should be appropriate for logging and debugging purposes. Inheritors should
 * override the {@link Object#toString()} method to include any additional relevant properties.
 */
public class AuthorizationRequest {
  @NonNull
  private final IAuthorizationUser user;

  @NonNull
  private final IAuthorizationAction action;

  /**
   * Constructs an {@code AuthorizationEvaluationRequest} with the specified user and action name.
   *
   * @param user   The user for whom the authorization is being evaluated.
   * @param action The action to be evaluated.
   * @throws IllegalArgumentException if the user or action are null.
   */
  public AuthorizationRequest( @NonNull IAuthorizationUser user, @NonNull IAuthorizationAction action ) {
    this.user = Objects.requireNonNull( user );
    this.action = Objects.requireNonNull( action );
  }

  /**
   * Gets the user for whom the authorization is being evaluated.
   *
   * @return The user.
   */
  @NonNull
  public IAuthorizationUser getUser() {
    return user;
  }

  /**
   * Gets the action to be evaluated.
   *
   * @return The action.
   */
  @NonNull
  public IAuthorizationAction getAction() {
    return action;
  }

  // Helper method so that rules can easily evaluate dependent permissions, for the same user.

  /**
   * Creates a new instance of {@code AuthorizationRequest} with the same user but with a different action.
   *
   * @param action The action to be evaluated.
   * @return The new instance.
   * @throws IllegalArgumentException if the action is null.
   */
  @NonNull
  public AuthorizationRequest withAction( @NonNull IAuthorizationAction action ) {
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
