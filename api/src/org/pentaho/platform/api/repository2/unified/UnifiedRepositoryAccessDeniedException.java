package org.pentaho.platform.api.repository2.unified;

/**
 * An exception that can be thrown from {@code IUnifiedRepository} implementations.
 * 
 * @author mlowery
 */
public class UnifiedRepositoryAccessDeniedException extends UnifiedRepositoryException {

  private static final long serialVersionUID = -7800484179397724352L;

  public UnifiedRepositoryAccessDeniedException() {
    super();
  }

  public UnifiedRepositoryAccessDeniedException(String message, Throwable cause) {
    super(message, cause);
  }

  public UnifiedRepositoryAccessDeniedException(String message) {
    super(message);
  }

  public UnifiedRepositoryAccessDeniedException(Throwable cause) {
    super(cause);
  }

}
