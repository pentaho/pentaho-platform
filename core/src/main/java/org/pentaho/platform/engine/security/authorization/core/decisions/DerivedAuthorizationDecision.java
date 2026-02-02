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
import org.pentaho.platform.api.engine.security.authorization.decisions.IDerivedAuthorizationDecision;
import org.pentaho.platform.engine.security.messages.Messages;

import java.text.MessageFormat;

/**
 * The {@code DerivedAuthorizationDecision} class is an implementation of the {@link IDerivedAuthorizationDecision}
 * interface.
 */
public class DerivedAuthorizationDecision extends AbstractAuthorizationDecision
  implements IDerivedAuthorizationDecision {

  private static final String GRANTED_JUSTIFICATION =
    Messages.getInstance().getString( "DerivedAuthorizationDecision.JUSTIFICATION" );
  private static final String DENIED_JUSTIFICATION =
    Messages.getInstance().getString( "DerivedAuthorizationDecision.Denied.JUSTIFICATION" );

  @NonNull
  private final IAuthorizationDecision derivedFromDecision;

  public DerivedAuthorizationDecision( @NonNull IAuthorizationRequest request,
                                       @NonNull IAuthorizationDecision derivedFromDecision ) {
    // Same granted state of the derived-from decision.
    super( request, derivedFromDecision.isGranted() );

    this.derivedFromDecision = derivedFromDecision;

    if ( request.equals( derivedFromDecision.getRequest() ) ) {
      throw new IllegalArgumentException(
        "Argument 'request' cannot be equal to the request of argument 'derivedFromDecision'." );
    }
  }

  @NonNull
  @Override
  public IAuthorizationDecision getDerivedFromDecision() {
    return derivedFromDecision;
  }

  @NonNull
  @Override
  public String getShortJustification( boolean granted ) {
    // Example: "From <derived-from decision justification>"
    return MessageFormat.format( granted ? GRANTED_JUSTIFICATION : DENIED_JUSTIFICATION, derivedFromDecision );
  }

  @Override
  public String toString() {
    // Example: "Derived[Granted, from: GeneralRoleBased[Granted, role=Administrator]]"
    return String.format( "Derived[%s, from: %s]", getGrantedLogText(), derivedFromDecision );
  }
}
