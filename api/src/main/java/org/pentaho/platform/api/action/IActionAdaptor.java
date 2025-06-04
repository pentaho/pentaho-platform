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

import java.io.Serializable;
import java.util.Map;

/**
 * This interface will provide a way to create an adaptor between {@link IAction} and
 * any orchestration tool like for example Mesos or Kubernetes etc.
 */
public interface IActionAdaptor {

  /**
   * Execute the request in the orchestration environment
   * @param (@link IAction) that holds the action for execution
   * @param user   The user invoking the action
   * @param (@link Map) of parameters for execution
   * @return the {@link IActionInvokeStatus} object containing result of the action
   *
  */
  public IActionInvokeStatus execute( IAction action, final String user, final Map<String, Serializable> params ) throws Exception;

  /**
   * Schedule the request for execution in the orchestration environment in future
   * @param (@link IAction) that holds the action for execution
   * @param user   The user invoking the action
   * @param (@link Map) of parameters for execution
   * @return the {@link IActionInvokeStatus} object containing result of the action
   *
   */
  public IActionInvokeStatus schedule( IAction action, final String user, final Map<String, Serializable> params ) throws Exception;

  /**
   * Delete the request
   * @param (@link IAction) that holds the action for execution
   * @param user   The user invoking the action
   * @param (@link Map) of parameters for execution
   * @return the {@link IActionInvokeStatus} object containing result of the action
   */
  public IActionInvokeStatus delete( IAction action, final String user, final Map<String, Serializable> params ) throws Exception;


}
