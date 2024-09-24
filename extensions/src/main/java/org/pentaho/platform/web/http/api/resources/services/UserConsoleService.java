/*!
 *
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
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.web.http.api.resources.services;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.web.http.api.resources.utils.SystemUtils;

import java.util.List;

public class UserConsoleService {

  private static final Log logger = LogFactory.getLog( UserConsoleService.class );

  public static IPentahoSession getPentahoSession() {
    return PentahoSessionHolder.getSession();
  }

  /**
   * Returns whether the current user is an administrator
   *
   * @return boolean value depending on the current user being the administrator
   */
  public boolean isAdministrator() {
    return SystemUtils.canAdminister();
  }

  /**
   * Returns whether the user is authenticated or not
   *
   * @return boolean value depending on the current user being authenticated
   */
  public boolean isAuthenticated() {
    return UserConsoleService.getPentahoSession() != null && UserConsoleService.getPentahoSession().isAuthenticated();
  }

  /**
   * Returns a List of plugins registered to the pentaho system
   *
   * @return List of registered plugins
   */
  public List<String> getRegisteredPlugins() {
    return PentahoSystem.get( IPluginManager.class, UserConsoleService.getPentahoSession() ).getRegisteredPlugins();
  }
}
