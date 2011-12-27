package org.pentaho.platform.api.repository2.unified;

/**
 * An exception that can be thrown from {@code IUnifiedRepository} implementations.
 * 
 * @author mlowery
 */
public class UnifiedRepositoryTransactionException extends UnifiedRepositoryException {

  private static final long serialVersionUID = -7800484179397724352L;

  public UnifiedRepositoryTransactionException() {
    super();
  }

  public UnifiedRepositoryTransactionException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public UnifiedRepositoryTransactionException(final String message) {
    super(message);
  }

  public UnifiedRepositoryTransactionException(final Throwable cause) {
    super(cause);
  }

}
