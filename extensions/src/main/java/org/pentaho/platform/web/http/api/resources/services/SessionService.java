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
