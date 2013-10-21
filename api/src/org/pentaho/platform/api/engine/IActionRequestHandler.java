/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

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
