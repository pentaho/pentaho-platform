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

/**
 * The {@code IAuthorizationPrincipal} interface represents a user or role in the authorization system.
 * It provides methods to access the user or role's name and attributes.
 * <p>
 * The principal's type and name form its unique identifier.
 * <p>
 * Implementations of this interface must ensure that the methods {@link Object#equals(Object)} and
 * {@link Object#hashCode()} use the principal's type and name as the identifying properties.
 * <p>
 * Implementations should ensure that {@link Object#toString()} describes the principal in a way appropriate for logging
 * and debugging purposes.
 */
public interface IAuthorizationPrincipal {
  @NonNull
  String getName();

  @NonNull
  Map<String, Object> getAttributes();
}
