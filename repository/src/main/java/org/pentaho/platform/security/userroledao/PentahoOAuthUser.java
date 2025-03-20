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

package org.pentaho.platform.security.userroledao;

public class PentahoOAuthUser extends PentahoUser {

  private String registrationId;

  private String userId;

  public PentahoOAuthUser( PentahoUser pentahoUser, String registrationId, String userId ) {
    super( pentahoUser.getTenant(), pentahoUser.getUsername(), pentahoUser.getPassword(), pentahoUser.getDescription(), pentahoUser.isEnabled() );
    this.registrationId = registrationId;
    this.userId = userId;
  }

  public String getRegistrationId() {
    return registrationId;
  }

  public void setRegistrationId( String registrationId ) {
    this.registrationId = registrationId;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId( String userId ) {
    this.userId = userId;
  }

}
