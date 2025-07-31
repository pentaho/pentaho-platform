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
import org.pentaho.platform.api.engine.security.authorization.decisions.IAuthorizationDecision;
import org.pentaho.platform.api.engine.security.authorization.decisions.IImpliedAuthorizationDecision;
import org.pentaho.platform.engine.security.messages.Messages;

import java.text.MessageFormat;

/**
 * The {@code ImpliedAuthorizationDecision} class is an implementation of the {@link IImpliedAuthorizationDecision}
 * interface.
 */
public class ImpliedAuthorizationDecision extends AbstractAuthorizationDecision
  implements IImpliedAuthorizationDecision {

  private static final String JUSTIFICATION =
    Messages.getInstance().getString( "ImpliedAuthorizationDecision.JUSTIFICATION" );

  @NonNull
  private final IAuthorizationDecision impliedFromDecision;

  public ImpliedAuthorizationDecision( @NonNull IAuthorizationRequest request,
                                       @NonNull IAuthorizationDecision impliedFromDecision ) {
    // Same granted state of the implied from decision.
    super( request, impliedFromDecision.isGranted() );

    this.impliedFromDecision = impliedFromDecision;

    if ( request.equals( impliedFromDecision.getRequest() ) ) {
      throw new IllegalArgumentException(
        "Argument 'request' cannot be equal to the request of argument 'impliedFromDecision'." );
    }
  }

  @NonNull
  @Override
  public IAuthorizationDecision getImpliedFromDecision() {
    return impliedFromDecision;
  }

  @NonNull
  @Override
  public String getShortJustification() {
    // Example: "From <implied-from decision justification>"
    return MessageFormat.format( JUSTIFICATION, impliedFromDecision );
  }

  @Override
  public String toString() {
    // Example: "Implied[Granted, from: GeneralRoleBased[Granted, role=Administrator]]"
    return String.format( "Implied[%s, from: %s]", getGrantedLogText(), impliedFromDecision );
  }
}
