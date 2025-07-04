/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


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
