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
