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

package org.pentaho.platform.api.engine.security.authorization.resources;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * The {@code IAuthorizationResource} interface represents a resource for authorization purposes.
 * <p>
 * Implementations of this interface must ensure that the methods {@link Object#equals(Object)} and
 * {@link Object#hashCode()} respect the uniqueness of the resource based on its type and identifier.
 * <p>
 * Implementations should ensure that {@link Object#toString()} describes the resource in a way appropriate for logging
 * and debugging purposes.
 */
public interface IAuthorizationResource {
  /**
   * Gets the identifier of the resource type.
   *
   * @return The resource type identifier.
   */
  @NonNull
  String getType();

  /**
   * Gets the type-relative identifier of the resource.
   * <p>
   * The resource's type-relative identifier is unique within the context of the resource type.
   *
   * @return The resource identifier.
   */
  @NonNull
  String getId();
}
