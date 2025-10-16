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

import java.util.Set;

/**
 * The {@code ICompositeAuthorizationDecision} interface represents a decision for an authorization request which is
 * justified in part or in full by a set of other decisions for the same authorization request.
 * <p>
 * The exact semantics and nature of the composition is defined by extension interfaces.
 * <p>
 * The value of {@link #getRequest()} must be the same as that of all decisions in the set {@link #getDecisions()}.
 * @see IAnyAuthorizationDecision
 * @see IAllAuthorizationDecision
 */
public interface ICompositeAuthorizationDecision extends IAuthorizationDecision {
  /**
   * Gets the set of decisions that constitute this composite decision.
   * <p>
   * The returned set must be immutable.
   * <p>
   * For reproducible results, the set should be order-preserving.
   *
   * @return A non-null, possibly empty, list of {@link IAuthorizationDecision} objects.
   */
  @NonNull
  Set<IAuthorizationDecision> getDecisions();
}
