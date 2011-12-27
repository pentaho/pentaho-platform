package org.pentaho.platform.repository2.unified.exception;

import org.pentaho.platform.api.repository2.unified.UnifiedRepositoryAccessDeniedException;
import org.pentaho.platform.repository2.messages.Messages;
import org.pentaho.platform.repository2.unified.ExceptionLoggingDecorator.ExceptionConverter;
import org.springframework.security.AccessDeniedException;

public class AccessDeniedExceptionConverter implements ExceptionConverter {

  @Override
  public RuntimeException convertException(final Exception exception, final String activityMessage, final String refNum) {
    AccessDeniedException ae = (AccessDeniedException) exception;
    return new UnifiedRepositoryAccessDeniedException(Messages.getInstance().getString(
        "ExceptionLoggingDecorator.accessDeniedException", activityMessage, refNum)); //$NON-NLS-1$
  }

}
