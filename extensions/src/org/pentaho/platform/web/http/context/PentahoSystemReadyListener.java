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

package org.pentaho.platform.web.http.context;

import java.util.List;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.lang.StringUtils;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPlatformPlugin;
import org.pentaho.platform.api.engine.IPlatformReadyListener;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.IPluginProvider;
import org.pentaho.platform.api.engine.PlatformPluginRegistrationException;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.logging.Logger;

public class PentahoSystemReadyListener implements ServletContextListener {

  @Override
  public void contextInitialized( ServletContextEvent servletContextEvent ) {
    IPluginManager pluginManager = PentahoSystem.get( IPluginManager.class );

    IPentahoSession session = PentahoSessionHolder.getSession();
    IPluginProvider pluginProvider = PentahoSystem.get( IPluginProvider.class, "IPluginProvider", session );
    try {
      List<IPlatformPlugin> providedPlugins = pluginProvider.getPlugins( session );
      for ( IPlatformPlugin plugin : providedPlugins ) {
        try {
          if ( !StringUtils.isEmpty( plugin.getLifecycleListenerClassname() ) ) {
            ClassLoader loader = pluginManager.getClassLoader( plugin.getId() );
            Object listener = loader.loadClass( plugin.getLifecycleListenerClassname() ).newInstance();
            if ( IPlatformReadyListener.class.isAssignableFrom( listener.getClass() ) ) {
              ( (IPlatformReadyListener) listener ).ready();
            }
          }
        } catch ( Exception e ) {
          Logger.warn( PentahoSystemReadyListener.class.getName(), e.getMessage(), e );
        }
      }
    } catch ( PlatformPluginRegistrationException e ) {
      Logger.warn( PentahoSystemReadyListener.class.getName(), e.getMessage(), e );
    }

  }

  @Override
  public void contextDestroyed( ServletContextEvent servletContextEvent ) {
  }

}
