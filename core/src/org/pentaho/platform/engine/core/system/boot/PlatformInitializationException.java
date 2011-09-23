package org.pentaho.platform.engine.core.system.boot;

public class PlatformInitializationException extends Exception {
  private static final long serialVersionUID = 6886731993305469276L;

  public PlatformInitializationException(String message, Throwable cause) {
    super(message, cause);
  }

  public PlatformInitializationException(String message) {
    super(message);
  }
}
