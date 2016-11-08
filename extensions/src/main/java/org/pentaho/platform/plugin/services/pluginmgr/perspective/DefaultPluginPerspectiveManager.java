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
