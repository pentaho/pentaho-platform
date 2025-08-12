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

package org.pentaho.platform.api.engine.security.authorization.decisions;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRule;

/**
 * The {@code IDerivedAuthorizationDecision} interface represents a decision for an authorization request which is
 * derived from the decision for a derived request, both decisions sharing the same granted status (whether granted or
 * denied).
 * <p>
 * Derived decisions result from sub-authorization requests evaluated via one of the context's authorization methods,
 * such as
 * {@link org.pentaho.platform.api.engine.security.authorization.IAuthorizationContext#authorize(IAuthorizationRequest)}
 * or
 * {@link org.pentaho.platform.api.engine.security.authorization.IAuthorizationContext#authorizeRule(IAuthorizationRequest, IAuthorizationRule)}.
 * <p>
 * A derived decision may be useful when it is desired to provide a justification, via {@link #getShortJustification()},
 * independent of that offered by the derived-from decision, and provide visibility to the underlying rule by which the
 * derived decision was made.
 * <p>
 * If desired, the derived-from decision can be used instead of the derived decision.
 * <p>
 * The derived-from decision is available via the {@link #getDerivedFromDecision()} method. Its
 * {@link IAuthorizationDecision#getRequest()} is the derived request.
 * <p>
 * The {@link #getRequest() request} of both decisions must not be equal, according to {@link Object#equals(Object)}.
 * <p>
 * The {@link #isGranted() granted status} of both decisions must be the same.
 */
public interface IDerivedAuthorizationDecision extends IAuthorizationDecision {
  /**
   * Gets the decision from which this one is derived.
   *
   * @return The derived-from decision.
   */
  @NonNull
  IAuthorizationDecision getDerivedFromDecision();
}
