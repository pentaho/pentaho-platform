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
 * The {@code IImpliedAuthorizationDecision} interface represents a decision for an authorization request which is
 * implied from/by the decision for a different, but related, request, both decisions sharing the same granted status
 * (whether granted or denied).
 * <p>
 * This decision is called the <i>consequent</i>, or <i>implied</i> decision, while the other decision the
 * <i>antecedent</i>, or <i>implied-from</i> decision and is the value of {@link #getImpliedFromDecision()}.
 * <p>
 * The {@link #getRequest() request} of both decisions must not be equal, according to {@link Object#equals(Object)}.
 * <p>
 * The {@link #isGranted() granted status} of both decisions must be the same.
 * <p>
 * Typically, decisions of this type are the result of "implication rules", having the form (where <code>A</code> stands
 * for the <i>antecedent</i> decision, and <code>C</code> for the <i>consequent</i> decision):
 * <pre>
 * if A is granted
 * then C is granted
 * else abstention
 * </pre>
 * It's important to note that when the antecedent is not granted (or is an abstention), the implication cannot conclude
 * anything about the consequent — whether B should be granted or denied —, and so the result should be an abstention.
 * <p>
 * Because of this property, when the implied decision is a denial, it must have been because it was instead the result
 * of an implication rule having the (less common) form:
 * <pre>
 * if A is denied
 * then C is denied
 * else abstention
 * </pre>
 */
public interface IImpliedAuthorizationDecision extends IAuthorizationDecision {
  /**
   * Gets the decision from which this one is implied.
   *
   * @return The implied-from decision.
   */
  @NonNull
  IAuthorizationDecision getImpliedFromDecision();
}
