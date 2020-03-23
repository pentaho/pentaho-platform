package org.pentaho.platform.web.http.security;

import org.springframework.security.core.AuthenticationException;

public class PreventBruteForceException extends AuthenticationException {

  public PreventBruteForceException( String message ) {
    super( message );
  }

  public PreventBruteForceException( String message, Throwable throwable ) {
    super( message, throwable );
  }
}
