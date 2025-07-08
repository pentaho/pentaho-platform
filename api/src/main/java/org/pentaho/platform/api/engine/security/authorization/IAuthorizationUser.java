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

import java.util.Map;
import java.util.Set;

public interface IAuthorizationUser {
  @NonNull
  String getName();

  // ???
  @NonNull
  Map<String, Object> getAttributes();

  // ???
  @NonNull
  Set<String> getRoles();
}
