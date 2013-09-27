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
 * System Exit Point implementors are called with the action on a thread completes. This action gets invoked through the
 * PentahoSystem static method <code>systemExitPoint().</code> The exit point is mainly implemented to handle relational
 * object persistence through Hibernate, but other objects that need to setup and teardown objects and state could add
 * themselves to the ApplicationContext list of entry and exit point notifications.
 * 
 */

public interface IPentahoSystemExitPoint {

  /**
   * Perform any system cleanup actions after the thread executes.
   */
  public void systemExitPoint();
}
