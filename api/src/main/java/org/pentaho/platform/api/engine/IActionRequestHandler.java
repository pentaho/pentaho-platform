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


package org.pentaho.platform.api.engine;

import java.util.List;

/**
 * The basic interface for requests to execute actions .
 * 
 * @author mbatchel
 * 
 */

public interface IActionRequestHandler {

  /**
   * Responsible for executing the action using the solution engine.
   * 
   * @param timeout
   *          Timeout for the execution - currently ignored in the Base
   * @param timeoutType
   *          - currently ignored in the Base
   * @return RuntimeContext from the execution
   * @see BaseRequestHandler
   */
  public IRuntimeContext handleActionRequest( int timeout, int timeoutType );

  /**
   * Executes an action sequence asynchronously. Note - this is currently not implemented in the BaseRequestHandler
   * 
   * @return RuntimeContext created for the asynchronous execution.
   */
  public IRuntimeContext handleActionAsyncRequest();

  /**
   * Gets the runtime. Currently not called anywhere in the platform
   * 
   * @param requestHandle
   * @return the RuntimeContext for the execution
   */
  public IRuntimeContext getRuntime( String requestHandle );

  /**
   * As the execution happens, the action handler is responsible for storing a list of all the messages that occur
   * in the case of component failure. This method returns that list. The items in the list are presented to the
   * user as execution feedback.
   * 
   * @return list of messages
   */
  @SuppressWarnings( "rawtypes" )
  public List getMessages();

  public void setCreateFeedbackParameterCallback( ICreateFeedbackParameterCallback createFeedbackParameterCallback );

  /**
   * Sets whether to force the generation of a prompt page
   * 
   * @param forcePrompt
   */
  public void setForcePrompt( boolean forcePrompt );
}
