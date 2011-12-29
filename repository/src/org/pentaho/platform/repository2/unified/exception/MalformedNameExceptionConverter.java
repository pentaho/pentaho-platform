package org.pentaho.platform.repository2.unified.exception;

import org.pentaho.platform.api.repository2.unified.UnifiedRepositoryException;
import org.pentaho.platform.api.repository2.unified.UnifiedRepositoryMalformedNameException;
import org.pentaho.platform.repository2.messages.Messages;
import org.pentaho.platform.repository2.unified.ExceptionLoggingDecorator.ExceptionConverter;

public class MalformedNameExceptionConverter implements ExceptionConverter {

  @Override
  public UnifiedRepositoryException convertException(final Exception exception, final String activityMessage,
      final String refNum) {
    RepositoryFileDaoMalformedNameException me = (RepositoryFileDaoMalformedNameException) exception;
    return new UnifiedRepositoryMalformedNameException(Messages.getInstance().getString(
        "ExceptionLoggingDecorator.malformedNameException", activityMessage, me.getName(), refNum)); //$NON-NLS-1$

  }

}
