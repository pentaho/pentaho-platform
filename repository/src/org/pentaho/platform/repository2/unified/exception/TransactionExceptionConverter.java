package org.pentaho.platform.repository2.unified.exception;

import org.pentaho.platform.api.repository2.unified.UnifiedRepositoryException;
import org.pentaho.platform.api.repository2.unified.UnifiedRepositoryTransactionException;
import org.pentaho.platform.repository2.messages.Messages;
import org.pentaho.platform.repository2.unified.ExceptionLoggingDecorator.ExceptionConverter;
import org.springframework.transaction.TransactionSystemException;

public class TransactionExceptionConverter implements ExceptionConverter {

  @Override
  public UnifiedRepositoryException convertException(final Exception exception, final String activityMessage, final String refNum) {
    TransactionSystemException te = (TransactionSystemException) exception;
    return new UnifiedRepositoryTransactionException(Messages.getInstance().getString(
        "ExceptionLoggingDecorator.transactionException", activityMessage, refNum)); //$NON-NLS-1$
  }

}
