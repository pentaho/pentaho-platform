package org.pentaho.platform.api.security;

public interface ILoginAttemptService {

  void loginSucceeded( String key );
  void loginFailed( String key );
  boolean isBlocked( String key );
}
