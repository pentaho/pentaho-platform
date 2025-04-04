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

package org.pentaho.platform.plugin.services.security.userrole.oauth;

import org.pentaho.platform.api.engine.security.userroledao.IPentahoUser;
import org.pentaho.platform.api.engine.security.userroledao.IUserRoleDao;
import org.pentaho.platform.security.userroledao.PentahoOAuthUser;

import java.util.List;

public class PentahoOAuthUserSync {

  IUserRoleDao userRoleDao;

  PentahoOAuthProviderFactory pentahoOAuthProviderFactory;

  public PentahoOAuthUserSync( IUserRoleDao userRoleDao, PentahoOAuthProviderFactory pentahoOAuthProviderFactory ) {
    this.userRoleDao = userRoleDao;
    this.pentahoOAuthProviderFactory = pentahoOAuthProviderFactory;
  }

  public List<IPentahoUser> readAllUsers() {
    return userRoleDao.getAllOAuthUsers();
  }

  public void performSync() {
    List<IPentahoUser> pentahoUsers = this.readAllUsers();

    pentahoUsers.forEach( pentahoUser -> performSyncForUser( (PentahoOAuthUser) pentahoUser ) );
  }

  public void performSyncForUser( PentahoOAuthUser pentahoOAuthUser ) {
    IPentahoOAuthHandler pentahoOAuthHandler =
            pentahoOAuthProviderFactory.getInstance( pentahoOAuthUser.getRegistrationId() );
    pentahoOAuthHandler.performSyncForUser(pentahoOAuthUser);
  }

}
