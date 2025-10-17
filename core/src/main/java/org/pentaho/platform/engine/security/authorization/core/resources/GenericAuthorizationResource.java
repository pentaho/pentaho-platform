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
import org.pentaho.platform.api.engine.security.authorization.resources.IAuthorizationResource;
import org.springframework.util.Assert;

import java.util.Objects;

public class GenericAuthorizationResource implements IAuthorizationResource {
  @NonNull
  private final String typeId;

  @NonNull
  private final String id;

  public GenericAuthorizationResource( @NonNull String typeId, @NonNull String id ) {
    Assert.hasText( typeId, "Argument `typeId` must not be null or empty" );
    Assert.hasText( id, "Argument `id` must not be null or empty" );

    this.typeId = typeId;
    this.id = id;
  }

  @NonNull
  @Override
  public String getType() {
    return typeId;
  }

  @NonNull
  @Override
  public String getId() {
    return id;
  }

  @Override
  public boolean equals( Object o ) {
    if ( !( o instanceof IAuthorizationResource ) ) {
      return false;
    }

    IAuthorizationResource that = (IAuthorizationResource) o;
    return Objects.equals( typeId, that.getType() )
      && Objects.equals( id, that.getId() );
  }

  @Override
  public int hashCode() {
    int result = typeId.hashCode();
    result = 31 * result + id.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return String.format( "AuthorizationResource[typeId='%s', id='%s']", typeId, id );
  }
}
