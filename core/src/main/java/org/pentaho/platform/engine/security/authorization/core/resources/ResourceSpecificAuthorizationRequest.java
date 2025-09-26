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

package org.pentaho.platform.engine.security.authorization.core.resources;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.engine.IAuthorizationAction;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationUser;
import org.pentaho.platform.api.engine.security.authorization.resources.IAuthorizationResource;
import org.pentaho.platform.api.engine.security.authorization.resources.IResourceAuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.resources.IResourceSpecificAuthorizationRequest;

/**
 * The {@code ResourceSpecificAuthorizationRequest} class is a basic implementation of the
 * {@link IResourceSpecificAuthorizationRequest} interface.
 */
public class ResourceSpecificAuthorizationRequest extends ResourceAuthorizationRequest
  implements IResourceSpecificAuthorizationRequest {

  /**
   * Constructs an {@code ResourceSpecificAuthorizationRequest} with the specified user and action name.
   *
   * @param resourceRequest The resource request to initialize from.
   */
  public ResourceSpecificAuthorizationRequest( @NonNull ResourceAuthorizationRequest resourceRequest ) {
    super( resourceRequest.getUser(), resourceRequest.getAction(), resourceRequest.getResource() );
  }

  /**
   * Constructs an {@code ResourceSpecificAuthorizationRequest} with the specified user and action name.
   *
   * @param user     The user for whom the authorization is being evaluated.
   * @param action   The action to be evaluated.
   * @param resource The resource for which the authorization is being evaluated.
   */
  public ResourceSpecificAuthorizationRequest( @NonNull IAuthorizationUser user,
                                               @NonNull IAuthorizationAction action,
                                               @NonNull IAuthorizationResource resource ) {
    super( user, action, resource );
  }

  @NonNull
  @Override
  public IResourceAuthorizationRequest withAction( @NonNull IAuthorizationAction action ) {
    return new ResourceSpecificAuthorizationRequest( getUser(), action, getResource() );
  }

  @NonNull
  @Override
  public IResourceAuthorizationRequest withResource( @NonNull IAuthorizationResource resource ) {
    return new ResourceSpecificAuthorizationRequest( getUser(), getAction(), resource );
  }

  @NonNull
  @Override
  public IResourceSpecificAuthorizationRequest asSpecific() {
    return this;
  }
}
