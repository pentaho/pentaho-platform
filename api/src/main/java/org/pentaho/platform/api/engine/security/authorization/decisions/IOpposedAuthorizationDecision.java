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

/**
 * The {@code IOpposedAuthorizationDecision} interface represents an authorization decision that is opposed (i.e., is
 * the opposite of) another decision for the <i>same</i> authorization request.
 * <p>
 * The decision is granted if the {@link #getOpposedToDecision() opposed-to decision} is denied, or denied if the
 * opposed-to decision is granted. Abstentions are preserved.
 * <p>
 * The value of {@link #getRequest()} must be the same as that of the {@link #getOpposedToDecision() opposed-to
 * decision}.
 */
public interface IOpposedAuthorizationDecision extends IAuthorizationDecision {
  /**
   * Gets the decision that this one is opposed to.
   *
   * @return The opposed-to decision.
   */
  @NonNull
  IAuthorizationDecision getOpposedToDecision();
}
