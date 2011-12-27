package org.pentaho.platform.repository2.unified.exception;

import org.pentaho.platform.api.repository2.unified.UnifiedRepositoryFileExistsException;
import org.pentaho.platform.repository2.messages.Messages;
import org.pentaho.platform.repository2.unified.ExceptionLoggingDecorator.ExceptionConverter;

public class FileExistsExceptionConverter implements ExceptionConverter {

  @Override
  public RuntimeException convertException(final Exception exception, final String activityMessage, final String refNum) {
    RepositoryFileDaoFileExistsException re = (RepositoryFileDaoFileExistsException) exception;
    return new UnifiedRepositoryFileExistsException(Messages.getInstance().getString(
        "ExceptionLoggingDecorator.fileExistsException", activityMessage, re.getFile().getPath(), refNum)); //$NON-NLS-1$

  }

}
