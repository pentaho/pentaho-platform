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


import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.repository2.ClientRepositoryPaths;

public class SessionService {

  /**
   * Returns the current user's workspace folder path
   *
   * @return workspace folder path
   */
  public String doGetCurrentUserDir() {
    return ClientRepositoryPaths.getUserHomeFolderPath( PentahoSessionHolder.getSession().getName() ) + "/workspace";
  }

  /**
   * Returns the workspace folder path for the selected user.
   *
   * @param user (user name)
   * @return workspace folder path
   */
  public String doGetUserDir( String user ) {
    return ClientRepositoryPaths.getUserHomeFolderPath( user ) + "/workspace";
  }
}
