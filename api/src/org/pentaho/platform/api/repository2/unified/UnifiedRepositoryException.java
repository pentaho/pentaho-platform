package org.pentaho.platform.api.repository2.unified;

/**
 * An exception that can be thrown from {@code IUnifiedRepository} implementations.
 * 
 * @author mlowery
 */
public class UnifiedRepositoryException extends RuntimeException {

  private static final long serialVersionUID = -3180298582920444104L;

  public UnifiedRepositoryException() {
    super();
  }

  public UnifiedRepositoryException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public UnifiedRepositoryException(final String message) {
    super(message);
  }

  public UnifiedRepositoryException(final Throwable cause) {
    super(cause);
  }

}
