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

package org.pentaho.platform.engine.security.authorization.core.decisions;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.decisions.IRuleErrorAuthorizationDecision;
import org.pentaho.platform.engine.security.messages.Messages;

import java.util.Objects;

/**
 * The {@code RuleErrorAuthorizationDecision} class is a basic implementation of the
 * {@link IRuleErrorAuthorizationDecision} interface.
 */
public class RuleErrorAuthorizationDecision extends AbstractAuthorizationDecision
  implements IRuleErrorAuthorizationDecision {

  private static final String JUSTIFICATION =
    Messages.getInstance().getString( "OpposedAuthorizationDecision.JUSTIFICATION" );

  @NonNull
  private final Exception cause;

  public RuleErrorAuthorizationDecision(
    @NonNull IAuthorizationRequest request,
    @NonNull Exception cause ) {

    super( request, false );

    this.cause = Objects.requireNonNull( cause );
  }

  @NonNull
  @Override
  public Exception getCause() {
    return cause;
  }

  @NonNull
  @Override
  public String getShortJustification() {
    return JUSTIFICATION;
  }

  @Override
  public String toString() {
    // Example: `RuleErrorAuthorizationDecision[Denied, "An error occurred while..."]`
    return String.format(
      "%s[%s \"%s\"]",
      getClass().getSimpleName(),
      getGrantedLogText(),
      getCause().getMessage() );
  }
}
