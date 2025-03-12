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


package org.pentaho.platform.api.action;

import org.apache.commons.logging.Log;

/**
 * The interface for an Action that wants to be provided with a logger.
 * 
 * @see IAction
 * @author aphillips
 * @since 3.6
 */
public interface ILoggingAction extends IAction {

  /**
   * Sets an apache commons logger for the Action component to use
   * 
   * @param log
   *          the commons logging log that the Action can write to
   */
  public void setLogger( Log log );

}
