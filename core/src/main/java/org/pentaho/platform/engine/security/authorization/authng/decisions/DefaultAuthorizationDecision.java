package org.pentaho.platform.engine.security.authorization.authng.decisions;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.engine.security.authorization.authng.AuthorizationRequest;

/**
 * The {@code DefaultAuthorizationDecision} class represents a default authorization decision, granted or denied without
 * additional information.
 */
public class DefaultAuthorizationDecision extends AbstractAuthorizationDecision {

  public DefaultAuthorizationDecision( @NonNull
                                       AuthorizationRequest request,
                                       boolean granted ) {
    super( request, granted );
  }
}
