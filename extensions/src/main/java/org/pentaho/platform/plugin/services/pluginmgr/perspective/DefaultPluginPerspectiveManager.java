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


package org.pentaho.platform.plugin.services.pluginmgr.perspective;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.perspective.IPluginPerspectiveManager;
import org.pentaho.platform.api.engine.perspective.pojo.IPluginPerspective;
import org.pentaho.platform.engine.core.system.PentahoSystem;

public class DefaultPluginPerspectiveManager implements IPluginPerspectiveManager {

  private static final Log logger = LogFactory.getLog( DefaultPluginPerspectiveManager.class );

  private ArrayList<IPluginPerspective> pluginPerspectives = new ArrayList<IPluginPerspective>();

  public List<IPluginPerspective> getPluginPerspectives() {
    // Compatibility check for old PerspectiveManager behavior. If the pluginPerspectives is empty,
    // we'll look in PentahoSystem
    List<IPluginPerspective> perspectives =
        ( pluginPerspectives.isEmpty() ) ? PentahoSystem.getAll( IPluginPerspective.class, null ) : pluginPerspectives;

    // don't return the actual list, otherwise the user could remove items without going through our api
    ArrayList<IPluginPerspective> allowedPerspectives = new ArrayList<IPluginPerspective>();

    for ( IPluginPerspective perspective : perspectives ) {
      ArrayList<String> actions = perspective.getRequiredSecurityActions();
      IAuthorizationPolicy policy = PentahoSystem.get( IAuthorizationPolicy.class );
      boolean allowed = true;
      if ( policy != null && actions != null && !actions.isEmpty() ) {
        // we're going to have to check the user
        for ( String actionName : actions ) {
          allowed = policy.isAllowed( actionName );
          if ( !allowed ) {
            // don't need to check anymore
            break;
          }
        }
      }
      if ( allowed ) {
        allowedPerspectives.add( perspective );
      }
    }

    return allowedPerspectives;
  }

  public void addPluginPerspective( IPluginPerspective pluginPerspective ) {
    // TODO: invoke any IPluginPerspective load/init methods
    pluginPerspectives.add( pluginPerspective );
  }

  public void removePluginPerspective( IPluginPerspective pluginPerspective ) {
    // TODO: invoke any IPluginPersepctive unload/destroy methods
    pluginPerspectives.remove( pluginPerspective );
  }

  public void clearPluginPerspectives() {
    // iterate over plugins to allow unload/destroy
    // for (IPluginPerspective pluginPerspective : pluginPerspectives) {
    // pluginPerspective.unload();
    // }
    pluginPerspectives.clear();
  }

}
