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

/**
 * The {@code IAuthorizationRequest} interface represents an authorization request for a user to perform an action.
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
public interface IAuthorizationRequest {
  /**
   * Gets the user for whom the authorization is being evaluated.
   *
   * @return The user.
   */
  @NonNull
  IAuthorizationUser getUser();

  /**
   * Gets the action to be evaluated.
   *
   * @return The action.
   */
  @NonNull
  IAuthorizationAction getAction();

  /**
   * Creates a new instance of {@code IAuthorizationRequest} with the same user but with a different action.
   *
   * @param action The action to be evaluated.
   * @return The new instance.
   * @throws IllegalArgumentException if the action is null.
   */
  @NonNull
  IAuthorizationRequest withAction( @NonNull IAuthorizationAction action );
}
