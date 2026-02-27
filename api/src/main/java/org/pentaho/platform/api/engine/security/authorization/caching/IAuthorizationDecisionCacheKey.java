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

package org.pentaho.platform.api.engine.security.authorization.caching;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationOptions;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRequest;

/**
 * The {@code IAuthorizationDecisionCacheKey} interface represents the key for cached authorization decision.
 * It is constituted by an authorization request and options.
 * <p>
 * Implementations must implement proper {@code equals()} and {@code hashCode()} methods.
 */
public interface IAuthorizationDecisionCacheKey {
  @NonNull
  IAuthorizationRequest getRequest();

  @NonNull
  IAuthorizationOptions getOptions();
}
