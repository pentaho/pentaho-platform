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
import org.pentaho.platform.api.engine.IAuthorizationAction;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.decisions.IAuthorizationDecision;
import org.pentaho.platform.engine.security.messages.Messages;

import java.text.MessageFormat;

/**
 * The {@code DerivedActionAuthorizationDecision} class represents an authorization decision that is derived from
 * the authorization decision for another action.
 */
public class DerivedActionAuthorizationDecision extends DerivedAuthorizationDecision {

  private static final String GRANTED_JUSTIFICATION =
    Messages.getInstance().getString( "DerivedActionAuthorizationDecision.JUSTIFICATION" );

  public DerivedActionAuthorizationDecision( @NonNull IAuthorizationRequest request,
                                             @NonNull IAuthorizationDecision derivedFromDecision ) {
    super( request, derivedFromDecision );
  }

  /**
   * Gets the action that this decision is derived from.
   *
   * @return The action that this decision is derived from.
   */
  @NonNull
  public IAuthorizationAction getDerivedFromAction() {
    return getDerivedFromDecision().getRequest().getAction();
  }


  @NonNull
  @Override
  protected String getShortJustificationGranted() {
    // Example: "Has Read Content permission"
    return MessageFormat.format( GRANTED_JUSTIFICATION, getDerivedFromAction().getLocalizedDisplayName() );
  }

  @Override
  public String toString() {
    // Example: "DerivedActionAuthorizationDecision[Granted, from: org.pentaho.repository.read]"
    return String.format(
      "%s[%s, from: %s]",
      getClass().getSimpleName(),
      getGrantedLogText(),
      getDerivedFromAction() );
  }
}
