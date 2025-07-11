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

package org.pentaho.platform.engine.security.authorization.authng.rules;

import org.pentaho.platform.api.engine.security.authorization.authng.IAuthorizationRule;

/**
 * The {@code AbstractAuthorizationRule} class is an optional base class for implementing authorization rules.
 * <p>
 * It provides a default implementation of the {@link #toString()} method, which returns the fully qualified class name.
 */
public abstract class AbstractAuthorizationRule implements IAuthorizationRule {
  @Override
  public String toString() {
    return getClass().getTypeName();
  }
}
