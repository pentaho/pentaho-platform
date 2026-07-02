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

package org.pentaho.platform.engine.security.authorization.core.rules;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRule;
import org.pentaho.platform.api.engine.security.authorization.decisions.IAuthorizationDecision;
import org.pentaho.platform.engine.security.messages.Messages;

import java.util.Optional;

/**
 * The {@code AbstractAuthorizationRule} class is an optional base class for implementing authorization rules.
 * It implements the generic {@link IAuthorizationRule} interface, preserving the generic type parameter to allow
 * subclasses to specify their own specific request types. Subclasses should specify the appropriate request type they
 * handle and implement the {@link #getRequestType()} method accordingly.
 * <p>
 * It provides a default implementation of the {@link #toString()} method, which returns the class's simple name.
 *
 * @param <T> The specific type of authorization request this rule can handle, must extend
 * {@link IAuthorizationRequest}.
 */
public abstract class AbstractAuthorizationRule<T extends IAuthorizationRequest> implements IAuthorizationRule<T> {

  protected static final String LIST_SEPARATOR =
    Messages.getInstance().getString( "AbstractAuthorizationDecision.LIST_SEPARATOR" );

  @NonNull
  protected final Optional<IAuthorizationDecision> abstain() {
    return Optional.empty();
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}
