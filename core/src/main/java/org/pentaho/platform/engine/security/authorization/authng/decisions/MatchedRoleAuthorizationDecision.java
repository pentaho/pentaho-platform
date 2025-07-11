package org.pentaho.platform.engine.security.authorization.authng.decisions;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.engine.security.authorization.authng.AuthorizationRequest;
import org.pentaho.platform.engine.security.messages.Messages;

import java.text.MessageFormat;
import java.util.Objects;

/**
 * The {@code MatchedRoleAuthorizationDecision} class represents an authorization decision that is granted when the user
 * of an authorization request has a specific role.
 */
public class MatchedRoleAuthorizationDecision extends AbstractAuthorizationDecision {
  private static final String JUSTIFICATION =
    Messages.getInstance().getString( "MatchedRoleAuthorizationDecision.JUSTIFICATION" );

  @NonNull
  private final String role;

  public MatchedRoleAuthorizationDecision( @NonNull AuthorizationRequest request, @NonNull String role ) {
    super( request, true );

    this.role = Objects.requireNonNull( role );
  }

  @NonNull
  public String getRole() {
    return role;
  }

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
