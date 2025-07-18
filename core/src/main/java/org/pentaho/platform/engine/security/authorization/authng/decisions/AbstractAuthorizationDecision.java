package org.pentaho.platform.engine.security.authorization.authng.decisions;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.engine.security.authorization.authng.AuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.authng.decisions.IAuthorizationDecision;

import java.util.Objects;

public abstract class AbstractAuthorizationDecision implements IAuthorizationDecision {

  @NonNull
  private final AuthorizationRequest request;
  private final boolean granted;

  public AbstractAuthorizationDecision( @NonNull AuthorizationRequest request, boolean granted ) {
    this.request = Objects.requireNonNull( request );
    this.granted = granted;
  }

  @NonNull
  @Override
  public AuthorizationRequest getRequest() {
    return request;
  }

  @Override
  public boolean isGranted() {
    return granted;
  }

  @Override
  public String getShortJustification() {
    return "";
  }

  @Override
  public String toString() {
    // Example: "SomeAuthorizationDecision[Granted]"
    return String.format( "%s[%s]", getClass().getSimpleName(), getGrantedLogText() );
  }

  @NonNull
  protected String getGrantedLogText() {
    return isGranted() ? "Granted" : "Denied";
  }
}
