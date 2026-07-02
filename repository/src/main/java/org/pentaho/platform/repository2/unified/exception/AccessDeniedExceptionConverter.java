/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.platform.repository2.unified.exception;

import org.pentaho.platform.api.repository2.unified.UnifiedRepositoryAccessDeniedException;
import org.pentaho.platform.api.repository2.unified.UnifiedRepositoryException;
import org.pentaho.platform.repository2.messages.Messages;
import org.pentaho.platform.repository2.unified.ExceptionLoggingDecorator.ExceptionConverter;

public class AccessDeniedExceptionConverter implements ExceptionConverter {

  @Override
  public UnifiedRepositoryException convertException( final Exception exception, final String activityMessage,
      final String refNum ) {
    return new UnifiedRepositoryAccessDeniedException( Messages.getInstance().getString(
        "ExceptionLoggingDecorator.accessDeniedException", activityMessage, refNum ), exception );
  }

}
