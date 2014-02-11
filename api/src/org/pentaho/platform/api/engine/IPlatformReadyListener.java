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
 * This interface is meant to augment IPluginLifecycleListener.  Plugins which have a lifecycle listener
 * may also implement this interface and will be notified when the platform is booted and ready.
 * 
 * @author mdamour
 */
public interface IPlatformReadyListener {

  /**
   * Called after the platform has been booted and is ready to receive requests.  All plugins have been
   * initialized and loaded, spring has been loaded and all beans are ready.  All components and sub
   * systems have been started - scheduler/repository/reporting/mondrian - etc.
   * 
   * @throws PluginLifecycleException
   *           if an error occurred
   */
  public void ready() throws PluginLifecycleException;

}
