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

/**
 * The listener interface for receiving notification when a Component execution has completed.
 * <p>
 * At the moment, all Component executions are synchronous, so the notification is sent as when the execution has
 * truly completed. In the near future, when asynchronous executions are implemented, notification may be sent as
 * soon as the execution has launched the asynchronous thread and has returned (in the case of an asynchronous
 * execution).
 */
public interface IActionCompleteListener {

  /**
   * Invoked when a Component execution has completed
   * 
   * @param runtime
   *          the runtime context associated with this action
   */
  public void actionComplete( IRuntimeContext runtime );

}
