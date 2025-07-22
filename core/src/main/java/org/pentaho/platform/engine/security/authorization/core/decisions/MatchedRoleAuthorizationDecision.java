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
import org.pentaho.platform.engine.security.messages.Messages;

import java.text.MessageFormat;
import java.util.Objects;

/**
 * The {@code MatchedRoleAuthorizationDecision} class represents an authorization decision that is granted when the user
 * of an authorization request has a specific role, and denied otherwise.
 */
public class MatchedRoleAuthorizationDecision extends AbstractAuthorizationDecision {
  private static final String JUSTIFICATION =
    Messages.getInstance().getString( "MatchedRoleAuthorizationDecision.JUSTIFICATION" );

  @NonNull
  private final String role;

  public MatchedRoleAuthorizationDecision( @NonNull IAuthorizationRequest request,
                                           boolean granted,
                                           @NonNull String role ) {
    super( request, granted );

    this.role = Objects.requireNonNull( role );
  }

  @NonNull
  public String getRole() {
    return role;
  }

  @NonNull
  @Override
  public String getShortJustification() {
    // Example: "Has role 'Administrator'".
    return MessageFormat.format( JUSTIFICATION, role );
  }

  @Override
  public String toString() {
    // Example: "MatchedRole[Granted, name: 'Administrator']"
    return String.format( "MatchedRole[%s, name: '%s']", getGrantedLogText(), role );
  }
}
