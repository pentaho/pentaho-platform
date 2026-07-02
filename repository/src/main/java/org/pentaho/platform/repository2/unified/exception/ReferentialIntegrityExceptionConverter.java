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

import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.UnifiedRepositoryException;
import org.pentaho.platform.api.repository2.unified.UnifiedRepositoryReferentialIntegrityException;
import org.pentaho.platform.repository2.messages.Messages;
import org.pentaho.platform.repository2.unified.ExceptionLoggingDecorator.ExceptionConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ReferentialIntegrityExceptionConverter implements ExceptionConverter {

  @Override
  public UnifiedRepositoryException convertException( final Exception exception, final String activityMessage,
                                                      final String refNum ) {
    RepositoryFileDaoReferentialIntegrityException re = (RepositoryFileDaoReferentialIntegrityException) exception;
    return new UnifiedRepositoryReferentialIntegrityException(
        Messages
            .getInstance()
            .getString(
                "ExceptionLoggingDecorator.referentialIntegrityException", activityMessage, re.getTarget().getPath(),
                getReferrerPaths( re.getReferrers() ), refNum ), exception ); //$NON-NLS-1$
  }

  private List<String> getReferrerPaths( final Set<RepositoryFile> referrers ) {
    List<String> paths = new ArrayList<String>();
    for ( RepositoryFile referrer : referrers ) {
      paths.add( referrer.getPath() );
    }
    return paths;
  }
}
