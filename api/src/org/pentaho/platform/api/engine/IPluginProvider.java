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
