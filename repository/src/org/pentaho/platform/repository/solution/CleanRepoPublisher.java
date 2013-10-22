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

package org.pentaho.platform.repository.solution;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.engine.core.system.BasePublisher;
import org.pentaho.platform.engine.services.solution.SolutionHelper;
import org.pentaho.platform.repository.messages.Messages;

import java.util.HashMap;

public class CleanRepoPublisher extends BasePublisher {

  private static final long serialVersionUID = -4584778481507215709L;

  private static final Log logger = LogFactory.getLog( CleanRepoPublisher.class );

  @Override
  public Log getLogger() {
    return CleanRepoPublisher.logger;
  }

  public String getName() {
    return Messages.getInstance().getString( "CleanRepoPublisher.CLEAN_REPO" ); //$NON-NLS-1$
  }

  public String getDescription() {
    return Messages.getInstance().getString( "CleanRepoPublisher.CLEAN_REPO_DESCRIPTION" ); //$NON-NLS-1$ 
  }

  @Override
  public String publish( final IPentahoSession localSession ) {
    try {
      HashMap<String, String> parameters = new HashMap<String, String>();
      ISolutionEngine engine = SolutionHelper.execute( "publisher", localSession, "admin/clean_repository.xaction", //$NON-NLS-1$//$NON-NLS-2$
          parameters, null );
      IRuntimeContext context = engine.getExecutionContext();
      int status = context.getStatus();
      if ( status != IRuntimeContext.RUNTIME_STATUS_SUCCESS ) {
        return Messages.getInstance().getString( "CleanRepoPublisher.ERROR_0001_CLEAN_REPO_FAILED" ); //$NON-NLS-1$
      }
    } catch ( Throwable t ) {
      error(
          Messages.getInstance().getErrorString( "CleanRepoPublisher.ERROR_0001_CLEAN_REPO_FAILED", t.getMessage() ), t ); //$NON-NLS-1$
      return Messages.getInstance().getString(
          "CleanRepoPublisher.ERROR_0001_CLEAN_REPO_FAILED", t.getLocalizedMessage() ); //$NON-NLS-1$
    }
    return Messages.getInstance().getString( "CleanRepoPublisher.CLEAN_REPO_DONE" ); //$NON-NLS-1$
  }
}
