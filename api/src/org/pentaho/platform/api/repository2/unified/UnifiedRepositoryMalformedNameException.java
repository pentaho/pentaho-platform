package org.pentaho.platform.api.repository2.unified;

/**
 * An exception that can be thrown from {@code IUnifiedRepository} implementations.
 * 
 * @author mlowery
 */
public class UnifiedRepositoryMalformedNameException extends UnifiedRepositoryException {

  private static final long serialVersionUID = -7800484179397724352L;

  public UnifiedRepositoryMalformedNameException() {
    super();
  }

  public UnifiedRepositoryMalformedNameException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public UnifiedRepositoryMalformedNameException(final String message) {
    super(message);
  }

  public UnifiedRepositoryMalformedNameException(final Throwable cause) {
    super(cause);
  }

}
