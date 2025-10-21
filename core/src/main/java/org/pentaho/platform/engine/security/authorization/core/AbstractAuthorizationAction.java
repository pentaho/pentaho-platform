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

import org.apache.commons.lang.StringUtils;
import org.pentaho.platform.api.engine.IAuthorizationAction;

import java.util.Objects;

/**
 * The {@code AbstractAuthorizationAction} class provides a base implementation for authorization actions.
 * <p>
 * This class implements the {@link Object#equals(Object)}, {@link Object#hashCode()} and {@link Object#toString()}
 * methods respecting the equality rules of {@link IAuthorizationAction}.
 */
public abstract class AbstractAuthorizationAction implements IAuthorizationAction {
  @Override
  public boolean equals( Object obj ) {
    if ( this == obj ) {
      return true;
    }

    return obj instanceof IAuthorizationAction other
      && Objects.equals( getName(), other.getName() );

  }

  @Override
  public int hashCode() {
    return getName().hashCode();
  }

  @Override
  public String toString() {
    String localizedDisplayName = getLocalizedDisplayName();
    if ( !StringUtils.isEmpty( localizedDisplayName )
      && !localizedDisplayName.equals( getName() ) ) {
      return String.format( "%s (%s)", localizedDisplayName, getName() );
    }

    return getName();
  }
}
