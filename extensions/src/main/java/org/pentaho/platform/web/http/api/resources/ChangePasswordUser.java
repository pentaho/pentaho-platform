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


package org.pentaho.platform.web.http.api.resources;

import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement( name = "ChangePasswordUser" )
public class ChangePasswordUser {

  private String userName;
  private String newPassword;
  private String oldPassword;

  public ChangePasswordUser() {
  }

  public ChangePasswordUser( String pUserName, String pNewPassword, String pOldPassword ) {
    userName = pUserName;
    newPassword = pNewPassword;
    oldPassword = pOldPassword;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName( String userName ) {
    this.userName = userName;
  }

  public String getNewPassword() {
    return newPassword;
  }

  public void setNewPassword( String newPassword ) {
    this.newPassword = newPassword;
  }

  public String getOldPassword() {
    return oldPassword;
  }

  public void setOldPassword( String oldPassword ) {
    this.oldPassword = oldPassword;
  }
}
