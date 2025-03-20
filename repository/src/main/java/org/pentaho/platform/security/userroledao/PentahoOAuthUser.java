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

import java.util.Objects;

/**
 * PentahoOAuthUser is a subclass of PentahoUser that adds registrationId and userId properties.
 * It is used to represent a user authenticated via OAuth stored in jackrabbit.
 */
public class PentahoOAuthUser extends PentahoUser {

  private String registrationId;

  private String userId;

  public PentahoOAuthUser( PentahoUser pentahoUser, String registrationId, String userId ) {
    super( pentahoUser.getTenant(), pentahoUser.getUsername(), pentahoUser.getPassword(), pentahoUser.getDescription(),
      pentahoUser.isEnabled() );
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

  @Override
  public boolean equals( Object o ) {
    if ( !( o instanceof PentahoOAuthUser ) ) {
      return false;
    }
    if ( !super.equals( o ) ) {
      return false;
    }
    PentahoOAuthUser that = (PentahoOAuthUser) o;
    return Objects.equals( registrationId, that.registrationId ) && Objects.equals( userId,
      that.userId );
  }

  @Override
  public int hashCode() {
    return Objects.hash( super.hashCode(), registrationId, userId );
  }

}
