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
 * Classes that implement this interface can be notified about key system events, in order to perform one-time
 * operations such as initialization, maintenance or other tasks.
 * 
 * @version 1.0
 */
public interface IPentahoSystemListener {

  /**
   * Notification of system startup. This event fires at the end of system initialization, after all system
   * components have started successfully.
   */
  public boolean startup( IPentahoSession session );

  /**
   * Notification of system shutdown. This event fires right before the server context is shutdown.
   */
  public void shutdown();

}
