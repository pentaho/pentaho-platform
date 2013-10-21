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
 * This interface provides entry points to the plugin manager for plugin loading, and unloading. In other words, a
 * plugin can respond to it's state with respect to the plugin manager.
 * 
 * @author mbatchel
 */
public interface IPluginLifecycleListener {

  /**
   * Called just prior to the plugin being registered with the platform. Note: This event does *not* precede the
   * detection of the plugin by any {@link IPluginProvider}s
   * 
   * @throws PluginLifecycleException
   *           if an error occurred
   */
  public void init() throws PluginLifecycleException;

  /**
   * Called after the plugin has been registered with the platform, i.e. all content generators, components, etc.
   * have been loaded.
   * 
   * @throws PluginLifecycleException
   *           if an error occurred
   */
  public void loaded() throws PluginLifecycleException;

  /**
   * Called when the plugin needs to be unloaded. This method should release all resources and return things to a
   * pre-loaded state.
   * 
   * @throws PluginLifecycleException
   *           if an error occurred
   */
  public void unLoaded() throws PluginLifecycleException;

}
