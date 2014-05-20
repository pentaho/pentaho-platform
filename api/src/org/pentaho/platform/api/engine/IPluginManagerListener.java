package org.pentaho.platform.api.engine;

/**
 * A sinple listener for the {@link IPluginManager}. It receives notification of events from the IPluginManager.
 * <p/>
 * Created by nbaker on 4/25/14.
 */
public interface IPluginManagerListener {
  /**
   * Called when the {@link IPluginManager} is reloaded.
   */
  void onReload();
}
