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
 * The {@code IAuthorizationErrorDecision} interface represents an authorization decision resulting from an error
 * occurred during the evaluation of an authorization request.
 * <p>
 * An error decision is always a denied decision, given that is generally the safest result to return.
 * <p>
 * An error decision contains an exception, {@link #getCause()} further describing the error. This exception may be
 * caused by a rule causing an evaluation cycle, by a rule throwing a runtime exception during a call to its
 * {@link IAuthorizationRule#authorize(IAuthorizationRequest, IAuthorizationContext)} method, or by passing the
 * authorization process an invalid request, such as when its action is not defined.
 * <p>
 * The {@link Object#toString()} implementation should describe the error that occurred, possibly including the
 * message of the actual exception.
 * The {@link #getShortJustification()} method, however, should only provide a general, brief message.
 */
public interface IAuthorizationErrorDecision extends IAuthorizationDecision {
  /**
   * Gets an exception describing the error.
   *
   * @return The exception.
   */
  @NonNull
  Exception getCause();
}
