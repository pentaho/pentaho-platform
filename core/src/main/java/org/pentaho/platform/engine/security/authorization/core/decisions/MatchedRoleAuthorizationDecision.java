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
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRole;
import org.pentaho.platform.engine.security.messages.Messages;
import org.springframework.util.Assert;

import java.text.MessageFormat;

/**
 * The {@code MatchedRoleAuthorizationDecision} class represents an authorization decision that is granted when the user
 * of an authorization request has a specific role, and denied otherwise.
 */
public class MatchedRoleAuthorizationDecision extends AbstractAuthorizationDecision {
  private static final String GRANTED_JUSTIFICATION =
    Messages.getInstance().getString( "MatchedRoleAuthorizationDecision.JUSTIFICATION" );

  private static final String DENIED_JUSTIFICATION =
    Messages.getInstance().getString( "MatchedRoleAuthorizationDecision.Denied.JUSTIFICATION" );

  @NonNull
  private final IAuthorizationRole role;

  public MatchedRoleAuthorizationDecision( @NonNull IAuthorizationRequest request,
                                           boolean granted,
                                           @NonNull IAuthorizationRole role ) {
    super( request, granted );

    Assert.notNull( role, "Argument 'role' is required" );

    this.role = role;
  }

  @NonNull
  public IAuthorizationRole getRole() {
    return role;
  }

  @NonNull
  @Override
  public String getShortJustification( boolean granted ) {
    // Example: "Has role 'Administrator'".
    return MessageFormat.format( granted ? GRANTED_JUSTIFICATION : DENIED_JUSTIFICATION, role.getName() );
  }

  @Override
  public String toString() {
    // Example: "MatchedRole[Granted, name: 'Administrator']"
    return String.format( "MatchedRole[%s, name: '%s']", getGrantedLogText(), role.getName() );
  }
}
