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
