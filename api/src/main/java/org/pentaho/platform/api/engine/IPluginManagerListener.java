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
 * A sinple listener for the {@link IPluginManager}. It receives notification of events from the IPluginManager.
 * <p/>
 * Created by nbaker on 4/25/14.
 */
public interface IPluginManagerListener {
  /**
   * Called after all plugins have been loaded (or reloaded), but before the final {@link #onReload()} method is called.
   * <p>
   * This method allows listeners to perform additional loading logic before the plugins are considered fully loaded.
   */
  default void onAfterPluginsLoaded() {
    // noop
  }

  /**
   * Called when the {@link IPluginManager} is reloaded.
   */
  void onReload();
}
