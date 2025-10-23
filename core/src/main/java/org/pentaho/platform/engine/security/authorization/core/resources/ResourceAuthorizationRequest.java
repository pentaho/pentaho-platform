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
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationPrincipal;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.resources.IAuthorizationResource;
import org.pentaho.platform.api.engine.security.authorization.resources.IResourceAuthorizationRequest;
import org.pentaho.platform.engine.security.authorization.core.AuthorizationRequest;
import org.springframework.util.Assert;

/**
 * The {@code ResourceAuthorizationRequest} class represents an authorization request for a specific resource.
 * It extends the basic authorization request to include resource-specific authorization context.
 */
public class ResourceAuthorizationRequest extends AuthorizationRequest
  implements IResourceAuthorizationRequest {

  @NonNull
  private final IAuthorizationResource resource;

  /**
   * Constructs a new resource authorization request.
   *
   * @param principal The principal (user, role, etc.) requesting authorization.
   * @param action    The action to be authorized.
   * @param resource  The resource for which authorization is requested.
   * @throws IllegalArgumentException if any parameter is {@code null}.
   */
  public ResourceAuthorizationRequest( @NonNull IAuthorizationPrincipal principal,
                                       @NonNull IAuthorizationAction action,
                                       @NonNull IAuthorizationResource resource ) {
    super( principal, action );

    Assert.notNull( resource, "Argument 'resource' is required" );

    this.resource = resource;
  }

  /**
   * Gets the resource for which authorization is being requested.
   *
   * @return The authorization resource.
   */
  @NonNull
  @Override
  public IAuthorizationResource getResource() {
    return resource;
  }

  // Helper method so that rules can easily evaluate dependent permissions, for the same user.

  @NonNull
  @Override
  public IResourceAuthorizationRequest withAction( @NonNull IAuthorizationAction action ) {
    return new ResourceAuthorizationRequest( getPrincipal(), action, getResource() );
  }

  @NonNull
  @Override
  public IResourceAuthorizationRequest withResource( @NonNull IAuthorizationResource resource ) {
    return new ResourceAuthorizationRequest( getPrincipal(), getAction(), resource );
  }

  @NonNull
  @Override
  public IAuthorizationRequest asGeneral() {
    return new AuthorizationRequest( getPrincipal(), getAction() );
  }

  @Override
  public boolean equals( Object o ) {
    if ( !super.equals( o ) ) {
      return false;
    }

    IResourceAuthorizationRequest that = (IResourceAuthorizationRequest) o;
    return resource.equals( that.getResource() );
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + resource.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return String.format(
      "%s [principal=%s, action=%s, resource=%s]",
      getClass().getSimpleName(),
      getPrincipal(),
      getAction(),
      getResource() );
  }
}
