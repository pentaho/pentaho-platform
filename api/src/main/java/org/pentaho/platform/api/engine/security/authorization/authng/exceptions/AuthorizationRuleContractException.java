package org.pentaho.platform.api.engine.security.authorization.authng.exceptions;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.pentaho.platform.api.engine.security.authorization.authng.IAuthorizationRule;

import java.util.Objects;

public class AuthorizationRuleContractException extends AuthorizationException {
  @NonNull
  private final IAuthorizationRule rule;

  public AuthorizationRuleContractException( @NonNull IAuthorizationRule rule, String message ) {
    this( rule, message, null );
  }

  public AuthorizationRuleContractException( @NonNull IAuthorizationRule rule, @NonNull Throwable cause ) {
    this( rule, null, cause );
  }

  public AuthorizationRuleContractException( @NonNull IAuthorizationRule rule,
                                             @Nullable String message,
                                             @Nullable Throwable cause ) {
    super( message, cause );
    this.rule = Objects.requireNonNull( rule );
  }

  @NonNull
  public IAuthorizationRule getRule() {
    return rule;
  }
}
