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
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationContext;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRule;

/**
 * The {@code IRuleErrorAuthorizationDecision} interface represents an authorization decision resulting from an error
 * occurred during the evaluation of an authorization rule.
 * <p>
 * A rule error decision is always a denied decision, given that is generally the safest result to return.
 * <p>
 * The rule error decision contains an exception, {@link #getCause()} further describing the error. This exception
 * may be
 * caused by a rule causing an evaluation cycle, to a rule throwing a runtime exception during a call to its
 * {@link IAuthorizationRule#authorize(IAuthorizationRequest, IAuthorizationContext)} method.
 * <p>
 * The {@link Object#toString()} implementation should describe the error that occurred, possibly including the
 * message of the actual exception.
 * The {@link #getShortJustification()} method, however, should only provide a general, brief message.
 */
public interface IRuleErrorAuthorizationDecision extends IAuthorizationDecision {
  /**
   * Gets an exception describing the error.
   *
   * @return The exception.
   */
  @NonNull
  Exception getCause();
}
