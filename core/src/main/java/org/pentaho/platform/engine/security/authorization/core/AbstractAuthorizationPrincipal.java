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
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationPrincipal;

import java.util.Map;

/**
 * The {@code AbstractAuthorizationPrincipal} class implements the {@link IAuthorizationPrincipal} interface.
 * <p>
 * This implementation implements the {@link Object#equals(Object)}, {@link Object#hashCode()} and
 * {@link Object#toString()} methods.
 * <p>
 * This implementation also provides a default implementation of the {@link IAuthorizationPrincipal#getAttributes()}
 * method, which returns an empty map.
 */
public abstract class AbstractAuthorizationPrincipal implements IAuthorizationPrincipal {

  @NonNull
  @Override
  public Map<String, Object> getAttributes() {
    return Map.of();
  }

  @Override
  public String toString() {
    return String.format( "%s[%s]", getClass().getSimpleName(), getName() );
  }
}
