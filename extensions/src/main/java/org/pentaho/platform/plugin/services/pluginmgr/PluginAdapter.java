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
