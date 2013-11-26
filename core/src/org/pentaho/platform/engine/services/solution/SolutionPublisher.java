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
