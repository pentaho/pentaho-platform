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
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationResource;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationUser;
import org.pentaho.platform.api.engine.security.authorization.IResourceAuthorizationRequest;

import java.util.Objects;

/**
 * The {@code ResourceAuthorizationRequest} class is a basic implementation of the {@link IResourceAuthorizationRequest}
 * interface.
 */
public class ResourceAuthorizationRequest extends AuthorizationRequest
  implements IResourceAuthorizationRequest {

  @NonNull
  private final IAuthorizationResource resource;

  /**
   * Constructs an {@code ResourceAuthorizationRequest} with the specified user and action name.
   *
   * @param user     The user for whom the authorization is being evaluated.
   * @param action   The action to be evaluated.
   * @param resource The resource for which the authorization is being evaluated.
   */
  public ResourceAuthorizationRequest( @NonNull IAuthorizationUser user,
                                       @NonNull IAuthorizationAction action,
                                       @NonNull IAuthorizationResource resource ) {
    super( user, action );

    this.resource = Objects.requireNonNull( resource );
  }

  @NonNull
  @Override
  public IAuthorizationResource getResource() {
    return resource;
  }

  // Helper method so that rules can easily evaluate dependent permissions, for the same user.

  @NonNull
  @Override
  public IResourceAuthorizationRequest withAction( @NonNull IAuthorizationAction action ) {
    return new ResourceAuthorizationRequest( getUser(), action, getResource() );
  }

  @NonNull
  @Override
  public IResourceAuthorizationRequest withResource( @NonNull IAuthorizationResource resource ) {
    return new ResourceAuthorizationRequest( getUser(), getAction(), resource );
  }

  @NonNull
  @Override
  public IAuthorizationRequest asGeneral() {
    return new AuthorizationRequest( getUser(), getAction() );
  }

  @Override
  public boolean equals( Object o ) {
    if ( !super.equals( o ) ) {
      return false;
    }

    IResourceAuthorizationRequest that = (IResourceAuthorizationRequest) o;
    return Objects.equals( resource, that.getResource() );
  }

  @Override
  public int hashCode() {
    return Objects.hash( super.hashCode(), resource );
  }

  @Override
  public String toString() {
    return String.format(
      "%s [user=`%s`, action='%s', resource=%s]",
      getClass().getSimpleName(),
      getUser().getName(),
      getAction().getName(),
      getResource() );
  }
}
