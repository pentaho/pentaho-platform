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

public interface IPentahoOAuthHandler {

  boolean isUserAccountEnabled( String registrationId, String clientCredentialsToken, String userId, boolean retry );

  List<String> getAppRoleAssignmentsForUser( String registrationId, String clientCredentialsToken, String userId, boolean retry );

  void setUserRoles( ITenant tenant, String userId, String[] roles );

  void performSyncForUser( PentahoOAuthUser pentahoOAuthUser );

}
