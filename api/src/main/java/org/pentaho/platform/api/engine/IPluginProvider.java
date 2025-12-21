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

import java.util.List;

/**
 * A plugin provider is responsible for serving up {@link IPlatformPlugin}s to whoever is asking, typically
 * {@link IPluginManager}. The plugin provider is not responsible for integrating the plugins into the platform.
 * It's only role is to render plugin definitions, {@link IPlatformPlugin}s. A plugin provider might load plugin
 * definitions from an xml file, or a properties file. You might also create a plugin provider that creates plugins
 * programmatically. It won't matter what mechanism you use to define your plugins so long as you implement
 * {@link IPluginProvider}.
 * 
 * @author aphillips
 */
public interface IPluginProvider {

  /**
   * Returns a list of {@link IPlatformPlugin}s defined by this plugin provider. These plugins have not been
   * initialized or registered within the platform at this point.
   * 
   * @param session
   *          the current session
   * @return a list of platform plugins
   * @throws PlatformPluginRegistrationException
   *           if there is a problem looking for plugins
   */
  public List<IPlatformPlugin> getPlugins( IPentahoSession session ) throws PlatformPluginRegistrationException;
}
