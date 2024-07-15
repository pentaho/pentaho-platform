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
 * Copyright (c) 2002-2024 Hitachi Vantara. All rights reserved.
 *
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
   * Invokes the {@link IAction} {@code action}.
   *
   * @param action The {@link IAction} to be invoked
   * @param user   The user invoking the action
   * @param params the {@link Map} or parameters needed to invoke the {@link IAction}
   * @return the {@link IActionInvokeStatus} object containing information about the action invocation
   * @throws Exception if the action cannot be run for some reason
   */
  IActionInvokeStatus invokeAction( IAction action, final String user, final Map<String, Object> params )
        throws Exception;

  /**
   * Predicate that tells whether an {@link IActionInvoker} can handle a given {@link IAction}
   *
   * @param action The {@link IAction} to be handled
   * @return true if the {@link IActionInvoker} can handle a given {@link IAction}
   */
  boolean isSupportedAction( IAction action );
}
