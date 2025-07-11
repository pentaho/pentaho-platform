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

import java.util.Map;
import java.util.Set;

/**
 * The {@code IAuthorizationUser} interface represents a user in the authorization system.
 * It provides methods to access the user's name, attributes, and roles.
 * <p>
 * The username is a unique identifier for the user, while attributes are key-value pairs.
 * <p>
 * Implementations of this interface must ensure that the methods {@link Object#equals(Object)} and
 * {@link Object#hashCode()} use the username as the identifying property.
 * <p>
 * Implementations should ensure that {@link Object#toString()} describes the user in a way appropriate for logging and
 * debugging purposes.
 */
public interface IAuthorizationUser {
  @NonNull
  String getName();

  // TODO: check user attributes
  @NonNull
  Map<String, Object> getAttributes();

  @NonNull
  Set<String> getRoles();
}
