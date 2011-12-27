package org.pentaho.platform.repository2.unified.exception;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.UnifiedRepositoryReferentialIntegrityException;
import org.pentaho.platform.repository2.messages.Messages;
import org.pentaho.platform.repository2.unified.ExceptionLoggingDecorator.ExceptionConverter;

public class ReferentialIntegrityExceptionConverter implements ExceptionConverter {

  @Override
  public RuntimeException convertException(final Exception exception, final String activityMessage, final String refNum) {
    RepositoryFileDaoReferentialIntegrityException re = (RepositoryFileDaoReferentialIntegrityException) exception;
    return new UnifiedRepositoryReferentialIntegrityException(
        Messages
            .getInstance()
            .getString(
                "ExceptionLoggingDecorator.referentialIntegrityException", activityMessage, re.getTarget().getPath(), getReferrerPaths(re.getReferrers()), refNum)); //$NON-NLS-1$
  }

  private List<String> getReferrerPaths(final Set<RepositoryFile> referrers) {
    List<String> paths = new ArrayList<String>();
    for (RepositoryFile referrer : referrers) {
      paths.add(referrer.getPath());
    }
    return paths;
  }
}
