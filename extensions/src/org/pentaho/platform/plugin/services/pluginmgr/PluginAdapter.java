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

import org.pentaho.platform.api.engine.IPentahoPublisher;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoSystemListener;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.platform.util.logging.Logger;

public class PluginAdapter implements IPentahoSystemListener, IPentahoPublisher {

  public boolean startup( IPentahoSession session ) {

    // from IPentahoSystemListener
    IPluginManager pluginManager = PentahoSystem.get( IPluginManager.class, "IPluginManager", session );
    if ( pluginManager == null ) {
      // we cannot continue without the PluginSettings
      Logger.error( getClass().toString(), Messages.getInstance().getErrorString(
          "PluginAdapter.ERROR_0001_PLUGIN_MANAGER_NOT_CONFIGURED" ) ); //$NON-NLS-1$
      return false;
    }
    pluginManager.reload();
    return true;
  }

  public void shutdown() {
    IPluginManager pluginManager = PentahoSystem.get( IPluginManager.class, null );
    if ( pluginManager != null ) {
      pluginManager.unloadAllPlugins();
    }
  }

  public String getDescription() {
    // from IPentahoPublisher
    return Messages.getInstance().getString( "PluginAdapter.USER_REFRESH_PLUGINS" ); //$NON-NLS-1$
  }

  public String getName() {
    // from IPentahoPublisher
    return Messages.getInstance().getString( "PluginAdapter.USER_PLUGIN_MANAGER" ); //$NON-NLS-1$
  }

  public String publish( IPentahoSession session, int loggingLevel ) {
    // from IPentahoPublisher
    try {
      PluginMessageLogger.clear();
      IPluginManager pluginManager = PentahoSystem.get( IPluginManager.class, "IPluginManager", session );
      if ( pluginManager == null ) {
        // we cannot continue without the PluginSettings
        Logger.error( getClass().toString(), Messages.getInstance().getErrorString(
            "PluginAdapter.ERROR_0001_PLUGIN_MANAGER_NOT_CONFIGURED" ) ); //$NON-NLS-1$
        return Messages.getInstance().getString( "PluginAdapter.ERROR_0001_PLUGIN_MANAGER_NOT_CONFIGURED" ); //$NON-NLS-1$
      }
      pluginManager.reload( session );
      String rtn = PluginMessageLogger.getAll().toString();
      return rtn;
    } finally {
      PluginMessageLogger.clear();
    }
  }

}
