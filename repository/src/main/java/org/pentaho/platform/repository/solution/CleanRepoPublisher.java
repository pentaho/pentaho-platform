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
