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

/**
 * POJO to simply represent authentication providers in SystemResource endpoints
 */

@XmlRootElement
public class AuthenticationProvider {

  private String authenticationType;

  public AuthenticationProvider() {
  }

  public AuthenticationProvider( String authenticationTypeToSet ) {
    this.authenticationType = authenticationTypeToSet;
  }

  public String getAuthenticationType() {
    return authenticationType;
  }

  public void setAuthenticationType( String authenticationTypeToSet ) {
    this.authenticationType = authenticationTypeToSet;
  }

  @Override
  public String toString() {
    return "PentahoAuthenticationProvider{" + "authenticationType='" + authenticationType + '\'' + '}';
  }
}
