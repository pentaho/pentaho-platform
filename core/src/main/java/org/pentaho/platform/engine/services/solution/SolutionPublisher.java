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


package org.pentaho.platform.engine.services.solution;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.BasePublisher;
import org.pentaho.platform.engine.services.messages.Messages;

public class SolutionPublisher extends BasePublisher {

  /**
   * 
   */
  private static final long serialVersionUID = -209000084524120620L;

  private static final Log logger = LogFactory.getLog( SolutionPublisher.class );

  @Override
  public Log getLogger() {
    return SolutionPublisher.logger;
  }

  @Override
  public String publish( final IPentahoSession session ) {

    // TODO put any code in here to validate the solution
    return Messages.getInstance().getString( "SolutionPublisher.USER_SOLUTION_REPOSITORY_UPDATED" ); //$NON-NLS-1$
  }

  public String getName() {
    return Messages.getInstance().getString( "SolutionRepository.USER_PUBLISH_TITLE" ); //$NON-NLS-1$
  }

  public String getDescription() {
    return Messages.getInstance().getString( "SolutionRepository.USER_PUBLISH_DESCRIPTION" ); //$NON-NLS-1$
  }

}
