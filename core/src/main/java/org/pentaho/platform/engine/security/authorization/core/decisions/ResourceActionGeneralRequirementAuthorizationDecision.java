/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/
package org.pentaho.platform.engine.security.authorization.core.decisions;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.decisions.IAuthorizationDecision;
import org.pentaho.platform.engine.security.messages.Messages;

public class ResourceActionGeneralRequirementAuthorizationDecision extends DerivedAuthorizationDecision {
  private static final String GRANTED_JUSTIFICATION =
    Messages.getInstance().getString( "ResourceActionGeneralRequirementAuthorizationDecision.JUSTIFICATION" );

  public ResourceActionGeneralRequirementAuthorizationDecision( @NonNull
                                                                IAuthorizationRequest request,
                                                                @NonNull
                                                                IAuthorizationDecision derivedFromDecision ) {
    super( request, derivedFromDecision );
  }

  @NonNull
  @Override
  protected String getShortJustificationGranted() {
    // Example: "Has general permission"
    return GRANTED_JUSTIFICATION;
  }

  @Override
  public String toString() {
    // Example: "ResourceActionGeneralRequirementAuthorizationDecision[Granted, from: <general decision>]"
    return String.format(
      "%s[%s, from: %s]",
      getClass().getSimpleName(),
      getGrantedLogText(),
      getDerivedFromDecision() );
  }
}
