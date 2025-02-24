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
 * Actions are the lightweight alternative to platform components (see
 * {@link org.pentaho.platform.api.engine.IComponent}). Actions participate in action sequences and can be provided
 * inputs and resources by the typical means, as defined in xaction solution files. The {@link IAction} family of
 * interfaces focuses on describing the *minimal* contract between the Pentaho BI Platform and an Action in the
 * same way that {@link org.pentaho.platform.api.engine.IComponent} describes a similar but more involved contract.
 * <p>
 * The Pentaho BI Platform expects Action objects to be Java bean API compliant with respect to setting inputs,
 * setting resources, and getting outputs. In other words, if your action needs takes a string input, the action
 * definition in the xaction solution file will specify this string input, and the Action framework will cause that
 * value to be set via a setter method on the Action object. You do not see parameter Maps and such in the Action
 * API for this reason. All inputs, output, and resources IO will involve Java bean reflection on your Action
 * object to find the appropriate IO methods.
 * 
 * @see IStreamingAction
 * @see ILoggingAction
 * @see ISessionAwareAction
 * @see IVarArgsAction
 * @see IPreProcessingAction
 * @see IDefinitionAwareAction
 * @author aphillips
 * @since 3.6
 * 
 */
public interface IAction {

  /**
   * The method in your Action that does the work.
   * 
   * @throws Exception
   *           if there was an error executing the Action
   */
  public void execute() throws Exception;

  /**
   * Provide the execution status of last Action. For backward compatibility, it is declared as default method
   * which returns true
   * @return boolean Indicate true for success and false for failure
   */
  default boolean isExecutionSuccessful() {
    return true;
  }


}
