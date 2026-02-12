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
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationUser;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.Set;

public class AuthorizationUser extends AbstractAuthorizationUser
  implements IAuthorizationUser {

  @NonNull
  private final String name;

  @NonNull
  private final Set<IAuthorizationRole> roles;

  public AuthorizationUser( @NonNull String name, @NonNull Set<IAuthorizationRole> roles ) {
    Assert.hasText( name, "Argument 'name' must not be null or empty." );
    Assert.notNull( roles, "Argument 'roles' must not be null." );
    this.name = name;
    this.roles = Collections.unmodifiableSet( roles );
  }

  @NonNull
  @Override
  public String getName() {
    return name;
  }

  @NonNull
  @Override
  public Set<IAuthorizationRole> getRoles() {
    return roles;
  }
}
