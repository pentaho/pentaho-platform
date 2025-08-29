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

/**
 * Allows an Action to do some preliminary work prior to execution. This API also allows an Action to fail early
 * and thereby not actually execute. Typically this is used in combination with {@link IDefinitionAwareAction} to
 * verify that the required inputs are provided prior to execution.
 * 
 * @see IAction
 * @see IDefinitionAwareAction
 * @author aphillips
 * @since 3.6
 */
public interface IPreProcessingAction extends IAction {

  /**
   * This method is called on an Action just prior to execution. An Action can fail early for any reason here by
   * throwing an exception. If an exception is thrown here, actual execution of the Action will not occur. If you
   * need to do pre-execution validation of inputs, see {@link IDefinitionAwareAction}
   * 
   * @throws ActionPreProcessingException
   *           if the Action is not able to proceed with execution
   */
  public void doPreExecution() throws ActionPreProcessingException;

}
