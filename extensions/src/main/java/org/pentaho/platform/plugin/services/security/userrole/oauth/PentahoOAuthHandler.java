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

import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.security.userroledao.PentahoOAuthUser;

import java.util.List;

/**
 * This interface is used to handle OAuth user role assignments and account status checks.
 * Implementations of this interface should provide the logic for checking if a user account is enabled,
 * retrieving app role assignments for a user, setting user roles, and performing sync for a user.
 */
public interface PentahoOAuthHandler {

  /**
   * Checks if the user account is enabled in Identity Provider.
   *
   * @param registrationId         The ID of the OAuth registration.
   * @param clientCredentialsToken The token for client credentials.
   * @param userId                 The ID of the user.
   * @param retry                  Whether to retry the operation.
   * @return true if the user account is enabled, false otherwise.
   */
  boolean isUserAccountEnabled( String registrationId, String clientCredentialsToken, String userId, boolean retry );

  /**
   * Retrieves the latest app role assignments for a user in Identity provider.
   *
   * @param registrationId         The ID of the OAuth registration.
   * @param clientCredentialsToken The token for client credentials.
   * @param userId                 The ID of the user.
   * @param retry                  Whether to retry the operation.
   * @return A list of app role assignments for the user.
   */
  List<String> getAppRoleAssignmentsForUser( String registrationId, String clientCredentialsToken, String userId,
                                             boolean retry );

  /**
   * Sets the user roles in the Identity Provider.
   *
   * @param tenant The tenant of a user for which the roles are being set.
   * @param userId The ID of the user.
   * @param roles  The roles to be set for the user.
   */
  void setUserRoles( ITenant tenant, String userId, String[] roles );

  /**
   * Performs sync for a user in Jackrabbit by reading information Identity Provider.
   *
   * @param pentahoOAuthUser The PentahoOAuthUser object representing the user.
   */
  void performSyncForUser( PentahoOAuthUser pentahoOAuthUser );

}
