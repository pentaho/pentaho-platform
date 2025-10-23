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

import java.io.InputStream;

/**
 * The interface for Actions that want to process the contents of a stream provided by the caller. Actions that
 * process the contents of a file contained in the Hitachi Vantara JCR repository and want the ability to be scheduled to
 * run should implement this method. The Pentaho scheduler will, upon execution of this action, open an input
 * stream to the file scheduled for execution and pass the input stream to this action.
 * 
 * @see IAction
 * @author arodriguez
 */
public interface IStreamProcessingAction extends IAction {

  /**
   * Sets the input stream containing the contents to be processed.
   * 
   * @param inputStream
   *          the input stream
   */
  public void setInputStream( InputStream inputStream );

}
