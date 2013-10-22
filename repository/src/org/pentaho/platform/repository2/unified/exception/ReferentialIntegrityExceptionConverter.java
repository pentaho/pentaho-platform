/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

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
                "ExceptionLoggingDecorator.referentialIntegrityException", activityMessage, re.getTarget().getPath(), getReferrerPaths( re.getReferrers() ), refNum ) ); //$NON-NLS-1$
  }

  private List<String> getReferrerPaths( final Set<RepositoryFile> referrers ) {
    List<String> paths = new ArrayList<String>();
    for ( RepositoryFile referrer : referrers ) {
      paths.add( referrer.getPath() );
    }
    return paths;
  }
}
