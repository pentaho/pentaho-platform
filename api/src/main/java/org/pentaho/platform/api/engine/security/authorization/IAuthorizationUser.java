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

package org.pentaho.platform.api.engine.security.authorization;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.Set;

/**
 * The {@code IAuthorizationUser} interface represents a user principal in the authorization system.
 * It provides methods to access a user's username, attributes and roles.
 */
public interface IAuthorizationUser extends IAuthorizationPrincipal {
  @NonNull
  Set<IAuthorizationRole> getRoles();
}
