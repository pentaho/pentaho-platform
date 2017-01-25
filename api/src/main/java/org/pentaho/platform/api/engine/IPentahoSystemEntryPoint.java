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
 * System Entry Point implementors get called upon entry to the system after they've been registered with the
 * ApplicationContext (<tt>IApplicationContext</tt>). The method is called when entries to the system call
 * <code>PentahoSystem.systemEntryPoint();</code> The purpose of the entry point is to setup the environment as
 * necessary to handle relational object persistence, starting transactions, initializing objects, or whatever
 * needs to take place when some action starts in the server. Example invocations include action execution, agent
 * startup, etc.
 * 
 */

public interface IPentahoSystemEntryPoint {
  /**
   * Perform operations necessary upon entry to the system.
   */
  public void systemEntryPoint();

}
