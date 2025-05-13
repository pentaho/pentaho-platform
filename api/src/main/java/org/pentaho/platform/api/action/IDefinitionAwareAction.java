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

import java.util.List;

/**
 * Makes an Action privy to certain details about the action definition that is responsible for executing it. This
 * is the only Action interface that should know anything about an action definition or the fact that it is even
 * being executed by way of an action sequence at all, for that matter. This interface is often used in concert
 * with {@link IPreProcessingAction}.
 * 
 * @see IAction
 * @author aphillips
 * @since 3.6
 */
public interface IDefinitionAwareAction extends IAction {

  /**
   * Informs the Action of the inputs that will be given to it, as specified in the action definition. This method
   * is called on an Action just prior to execution.
   * 
   * @param inputNames
   *          names of the action definition inputs
   */
  public void setInputNames( List<String> inputNames );

  /**
   * Informs the Action of the outputs that will be expected of it, as specified in the action definition. This
   * method is called on an Action just prior to execution.
   * 
   * @param inputNames
   *          names of the action definition inputs
   */
  public void setOutputNames( List<String> outputNames );
}
