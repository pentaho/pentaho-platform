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
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationContext;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRule;
import org.pentaho.platform.api.engine.security.authorization.decisions.IAuthorizationDecision;

import java.util.Optional;

/**
 * The {@code AbstractAuthorizationRule} class is an optional base class for implementing authorization rules.
 * <p>
 * It provides a default implementation of the {@link #toString()} method, which returns the fully qualified class name.
 */
public abstract class AbstractAuthorizationRule implements IAuthorizationRule {

  @NonNull
  @Override
  public abstract Optional<IAuthorizationDecision> authorize( @NonNull IAuthorizationRequest request,
                                                              @NonNull IAuthorizationContext context );

  @NonNull
  protected final Optional<IAuthorizationDecision> abstain() {
    return Optional.empty();
  }

  @Override
  public String toString() {
    return getClass().getTypeName();
  }
}
