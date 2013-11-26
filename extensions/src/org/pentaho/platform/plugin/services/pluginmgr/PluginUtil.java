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

package org.pentaho.platform.plugin.services.pluginmgr;

import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;

public class PluginUtil {

  /**
   * If the service specified by <code>serviceId</code> is supplied by a plugin, the ClassLoader used to load classes
   * for the associated plugin is returned. This is a handy method to use if you want to find resources associated with
   * a particular (plugin-supplied) service, such as a GWT serialization policy file, properties files, etc. If the
   * service was not supplied by a plugin, then <code>null</code> is returned.
   * 
   * @param path
   *          a path to a plugin resource
   * @return the ClassLoader that serves the (plugin-supplied) service, or <code>null</code> if the service was not
   *         plugin-supplied or the plugin manager cannot identify the service.
   */
  public static ClassLoader getClassLoaderForService( String path ) {
    IPluginManager pluginManager = PentahoSystem.get( IPluginManager.class, PentahoSessionHolder.getSession() );
    return pluginManager.getClassLoader( getPluginIdFromPath( path ) );
  }

  /**
   * If the service specified by <code>serviceId</code> is supplied by a plugin, plugin id is returned.
   * 
   * @param path
   *          a path to a plugin resource
   * @return the Id of the plugin
   */
  public static String getPluginIdFromPath( String path ) {
    IPluginManager pluginManager = PentahoSystem.get( IPluginManager.class, PentahoSessionHolder.getSession() );
    if ( path.startsWith( "content" ) || path.startsWith( "/content" ) ) { //$NON-NLS-1$
      path = path.substring( path.indexOf( '/', 1 ) );
    }
    // The plugin manager can tell us which plugin handles requests like the one for the serialization file
    //
    String servicePluginId = pluginManager.getServicePlugin( path );

    if ( servicePluginId == null ) {
      int start = path.indexOf( "repos/" ) + "repos/".length(); //$NON-NLS-1$ //$NON-NLS-2$
      servicePluginId = path.substring( start, path.indexOf( '/', start ) );
    }

    return servicePluginId;
  }

}
