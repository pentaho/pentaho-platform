package org.pentaho.platform.repository2.unified.exception;

import javax.jcr.lock.LockException;

import org.pentaho.platform.api.repository2.unified.UnifiedRepositoryLockException;
import org.pentaho.platform.repository2.messages.Messages;
import org.pentaho.platform.repository2.unified.ExceptionLoggingDecorator.ExceptionConverter;

public class LockExceptionConverter implements ExceptionConverter {

  @Override
  public RuntimeException convertException(final Exception exception, final String activityMessage, final String refNum) {
    LockException le = (LockException) exception;
    return new UnifiedRepositoryLockException(Messages.getInstance().getString(
        "ExceptionLoggingDecorator.lockException", activityMessage, refNum)); //$NON-NLS-1$
  }

}
