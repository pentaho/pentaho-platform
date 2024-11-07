/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.platform.web.http.api.resources;

import javax.xml.bind.annotation.XmlRootElement;

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
