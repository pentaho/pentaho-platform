/*
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
 * Copyright 2013 Pentaho Corporation.  All rights reserved.
 *
*/
package org.pentaho.platform.plugin.action.olap;

import java.util.Properties;

/**
 * This interface can be implemented to modify the connection properties
 * before a connection is made.
 */
public interface IOlapConnectionFilter {
  /**
   * Will be called before opening an olap connection. Note that
   * 'user' and 'password' properties might get overridden downstream
   * if they have been set when creating the connection with the
   * {@link IOlapService}.
   * @param properties Properties used for the connection.
   */
  void filterProperties( Properties properties );
}
