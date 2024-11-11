/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


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
