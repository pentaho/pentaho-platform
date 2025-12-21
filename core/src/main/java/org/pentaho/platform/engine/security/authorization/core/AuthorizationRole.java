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
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRole;
import org.springframework.util.Assert;

import java.util.Objects;

public class AuthorizationRole extends AbstractAuthorizationPrincipal
  implements IAuthorizationRole {

  @NonNull
  private final String name;

  public AuthorizationRole( @NonNull String name ) {
    Assert.hasText( name, "Argument 'name' must not be null or empty." );
    this.name = name;
  }

  @NonNull
  @Override
  public String getName() {
    return name;
  }

  @Override
  public boolean equals( Object o ) {
    return o instanceof IAuthorizationRole that
      && Objects.equals( getName(), that.getName() );

  }

  // have hash code be sensitive to the interface type, so that it is
  // possible to put principals of various types into a map.
  @Override
  public int hashCode() {
    int result = IAuthorizationRole.class.hashCode();
    result = 31 * result + name.hashCode();
    return result;
  }
}
