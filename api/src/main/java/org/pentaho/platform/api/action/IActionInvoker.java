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
 * Copyright (c) 2017 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.api.action;

import java.io.Serializable;
import java.util.Map;

/**
 * The purpose of this interface is to provide functionality needed to invoke an {@link IAction} instance in a
 * generic fashion.
 */
public interface IActionInvoker {

  /**
   * Invokes the {@link IAction} {@code action} in the background.
   *
   * @param action The {@link IAction} to be invoked
   * @param user   The user invoking the action
   * @param params the {@link Map} or parameters needed to invoke the {@link IAction}
   * @return the {@link IActionInvokeStatus} object containing information about the action invocation
   * @throws Exception if the action cannot be run for some reason
   */
  IActionInvokeStatus runInBackground( IAction action, final String user, final Map<String, Serializable> params )
    throws Exception;
}
