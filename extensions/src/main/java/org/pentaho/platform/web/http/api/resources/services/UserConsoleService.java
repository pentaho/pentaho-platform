/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

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
