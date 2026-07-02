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

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.engine.IAuthorizationAction;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

/**
 * The {@code IAuthorizationRequest} interface represents an authorization request for a principal to perform an action.
 * <p>
 * This type can be derived to include any additional information characterizing an authorization request. In this case,
 * the {@link #withAction(IAuthorizationAction)} method should be overridden to ensure that the new request includes all
 * properties of the original request, except for the action, which will be replaced by the new one.
 * <p>
 * Equality is based on the principal, action and any other key properties, meaning two requests are considered
 * equal if they have the same principal, action and value of the other key properties.
 * The {@link Object#equals(Object)} and {@link Object#hashCode()} methods must ensure this behavior.
 * <p>
 * The string representation of this request should be appropriate for logging and debugging purposes. Inheritors should
 * override the {@link Object#toString()} method to include any additional relevant properties.
 */
public interface IAuthorizationRequest {
  /**
   * Gets the principal for whom the authorization is being evaluated.
   *
   * @return The principal (user, role, etc.).
   */
  @NonNull
  IAuthorizationPrincipal getPrincipal();

  /**
   * Gets the user for whom the authorization is being evaluated.
   *
   * @return The user or {@code null} if the principal is not a user.
   */
  @NonNull
  default Optional<IAuthorizationUser> getPrincipalAsUser() {
    IAuthorizationPrincipal principal = getPrincipal();
    return Optional.ofNullable( principal instanceof IAuthorizationUser ? (IAuthorizationUser) principal : null );
  }

  /**
   * Gets the role for which the authorization is being evaluated.
   *
   * @return The role or {@code null} if the principal is not a role.
   */
  @NonNull
  default Optional<IAuthorizationRole> getPrincipalAsRole() {
    IAuthorizationPrincipal principal = getPrincipal();
    return Optional.ofNullable( principal instanceof IAuthorizationRole ? (IAuthorizationRole) principal : null );
  }

  /**
   * Gets all roles associated with the principal for whom the authorization is being evaluated.
   * <p>
   * If the principal is a user, this method returns all roles assigned to that user.
   * If the principal is a role, this method returns a set containing only that role.
   * If the principal is neither a user nor a role, this method returns an empty set.
   *
   * @return A set of roles associated with the principal, never {@code null}.
   */
  @NonNull
  default Set<IAuthorizationRole> getAllRoles() {
    Optional<IAuthorizationUser> user = getPrincipalAsUser();
    if ( user.isPresent() ) {
      return user.get().getRoles();
    }

    Optional<IAuthorizationRole> role = getPrincipalAsRole();
    return role.map( Set::of )
      .orElse( Collections.emptySet() );
  }

  /**
   * Gets the action to be evaluated.
   *
   * @return The action.
   */
  @NonNull
  IAuthorizationAction getAction();

  /**
   * Creates a new instance of {@code IAuthorizationRequest} with the same principal but with a different action.
   *
   * @param action The new action.
   * @return A new request with the same principal but different action.
   */
  @NonNull
  IAuthorizationRequest withAction( @NonNull IAuthorizationAction action );
}
