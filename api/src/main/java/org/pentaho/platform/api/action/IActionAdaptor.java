/*!
 *
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
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

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
