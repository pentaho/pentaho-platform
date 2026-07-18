/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.platform.web.http.api.resources;

import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class UserChangePasswordDTO extends User {
  private String administratorPassword;

  public UserChangePasswordDTO() {
  }

  public UserChangePasswordDTO( String userName, String password, String administratorPassword ) {
    super( userName, password );

    this.administratorPassword = administratorPassword;
  }

  public String getAdministratorPassword() {
    return administratorPassword;
  }

  public void setAdministratorPassword( String administratorPassword ) {
    this.administratorPassword = administratorPassword;
  }
}
