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
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRequest;

public interface IResourceAuthorizationRequest extends IAuthorizationRequest {

  /**
   * Gets the resource for which the authorization is being evaluated.
   *
   * @return The resource.
   */
  @NonNull
  IAuthorizationResource getResource();

  /**
   * Creates a new instance of {@code IAuthorizationRequest} with the same user and action, but without a resource.
   *
   * @return The new instance.
   */
  @NonNull
  IAuthorizationRequest asGeneral();

  /**
   * Creates a new instance of {@code IResourceAuthorizationRequest} with the same user and action, but with a
   * different resource.
   *
   * @param resource The resource to be evaluated.
   * @return The new instance.
   * @throws IllegalArgumentException if the resource is {@code null}.
   */
  @NonNull
  IResourceAuthorizationRequest withResource( @NonNull IAuthorizationResource resource );
}
