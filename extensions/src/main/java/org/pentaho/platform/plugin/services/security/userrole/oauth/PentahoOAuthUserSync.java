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

/**
 * PentahoOAuthUserSync is responsible for synchronizing Pentaho OAuth users with the OAuth provider.
 * It retrieves all OAuth users from the user role DAO and performs synchronization for each user.
 */
public class PentahoOAuthUserSync {

  IUserRoleDao userRoleDao;

  PentahoOAuthProviderFactory pentahoOAuthProviderFactory;

  public PentahoOAuthUserSync( IUserRoleDao userRoleDao, PentahoOAuthProviderFactory pentahoOAuthProviderFactory ) {
    this.userRoleDao = userRoleDao;
    this.pentahoOAuthProviderFactory = pentahoOAuthProviderFactory;
  }

  /**
   * This method is responsible for reading all users from the user role DAO
   *
   * @return a list of all Pentaho OAuth users
   */
  public List<IPentahoUser> readAllUsers() {
    return userRoleDao.getAllOAuthUsers();
  }

  /**
   * This method is responsible for synchronizing users invoked by the scheduler
   */
  public void performSync() {
    List<IPentahoUser> pentahoUsers = this.readAllUsers();

    pentahoUsers.forEach( pentahoUser -> performSyncForUser( (PentahoOAuthUser) pentahoUser ) );
  }

  /**
   * This method is responsible for synchronizing a single user
   *
   * @param pentahoOAuthUser the user to synchronize
   */
  public void performSyncForUser( PentahoOAuthUser pentahoOAuthUser ) {
    PentahoOAuthHandler pentahoOAuthHandler =
      pentahoOAuthProviderFactory.getInstance( pentahoOAuthUser.getRegistrationId() );
    pentahoOAuthHandler.performSyncForUser( pentahoOAuthUser );
  }

}
