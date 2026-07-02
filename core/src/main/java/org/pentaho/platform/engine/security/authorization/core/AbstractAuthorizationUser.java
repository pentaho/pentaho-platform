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

import org.pentaho.platform.api.engine.security.authorization.IAuthorizationUser;

import java.util.Objects;

public abstract class AbstractAuthorizationUser extends AbstractAuthorizationPrincipal
  implements IAuthorizationUser {

  @Override
  public boolean equals( Object o ) {
    return o instanceof IAuthorizationUser that
      && Objects.equals( getName(), that.getName() );

  }

  // have hash code be sensitive to the interface type, so that it is
  // possible to put principals of various types into a map.
  @Override
  public int hashCode() {
    int result = IAuthorizationUser.class.hashCode();
    result = 31 * result + Objects.hashCode( getName() );
    return result;
  }
}
