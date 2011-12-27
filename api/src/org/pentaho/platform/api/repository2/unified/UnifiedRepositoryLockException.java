package org.pentaho.platform.api.repository2.unified;

/**
 * An exception that can be thrown from {@code IUnifiedRepository} implementations.
 * 
 * @author mlowery
 */
public class UnifiedRepositoryLockException extends UnifiedRepositoryException {

  private static final long serialVersionUID = -7800484179397724352L;

  public UnifiedRepositoryLockException() {
    super();
  }

  public UnifiedRepositoryLockException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public UnifiedRepositoryLockException(final String message) {
    super(message);
  }

  public UnifiedRepositoryLockException(final Throwable cause) {
    super(cause);
  }

}
