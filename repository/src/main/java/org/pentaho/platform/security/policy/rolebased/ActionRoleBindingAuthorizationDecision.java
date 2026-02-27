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

package org.pentaho.platform.security.policy.rolebased;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRole;
import org.pentaho.platform.engine.security.authorization.core.decisions.AbstractAuthorizationDecision;
import org.pentaho.platform.security.policy.rolebased.messages.Messages;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.Set;

public class ActionRoleBindingAuthorizationDecision extends AbstractAuthorizationDecision {

  protected static final String JUSTIFICATION =
    Messages.getInstance().getString( "ActionRoleBindingAuthorizationDecision.JUSTIFICATION" );

  @NonNull
  private final Set<IAuthorizationRole> boundRoles;

  public ActionRoleBindingAuthorizationDecision( @NonNull IAuthorizationRequest request,
                                                 @NonNull Set<IAuthorizationRole> boundRoles ) {
    super( request, !boundRoles.isEmpty() );
    this.boundRoles = Collections.unmodifiableSet( boundRoles );
  }

  @NonNull
  public Set<IAuthorizationRole> getBoundRoles() {
    return boundRoles;
  }

  @NonNull
  @Override
  public String getShortJustification() {
    // Example: "Has Manage Data Source permission from role(s): Power User, Business Analyst"
    return MessageFormat.format( JUSTIFICATION,
      getBoundRolesText(),
      getRequest().getAction().getLocalizedDisplayName() );
  }

  @Override
  public String toString() {
    // Example: "ActionRoleBindingAuthorizationDecision[Granted, roles: Power User, Business Analyst]"
    return String.format( "%s[%s roles: %s]",
      getClass().getSimpleName(),
      getGrantedLogText(),
      getBoundRolesText() );
  }

  @NonNull
  private String getBoundRolesText() {
    return getBoundRoles()
      .stream()
      .map( IAuthorizationRole::getName )
      .collect( java.util.stream.Collectors.joining( LIST_SEPARATOR ) );
  }
}
