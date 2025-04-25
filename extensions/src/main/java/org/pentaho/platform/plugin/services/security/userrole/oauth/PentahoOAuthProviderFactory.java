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

import org.apache.tika.utils.StringUtils;

public class PentahoOAuthProviderFactory {

  AzurePentahoOAuthHandler azurePentahoOAuthHandler;

  OktaPentahoOAuthHandler oktaPentahoOAuthHandler;

  public void setAzurePentahoOAuthHandler( AzurePentahoOAuthHandler azurePentahoOAuthHandler ) {
    this.azurePentahoOAuthHandler = azurePentahoOAuthHandler;
  }

  public void setOktaPentahoOAuthHandler( OktaPentahoOAuthHandler oktaPentahoOAuthHandler ) {
    this.oktaPentahoOAuthHandler = oktaPentahoOAuthHandler;
  }

  /**
   * Returns the appropriate PentahoOAuthHandler instance based on the registrationId.
   *
   * @param registrationId The registration ID to determine which handler to return.
   * @return The corresponding PentahoOAuthHandler instance, or null if no match is found.
   */
  public PentahoOAuthHandler getInstance( String registrationId ) {
    if ( StringUtils.isBlank( registrationId ) ) {
      throw new IllegalArgumentException( "Provider is null" );
    }

    PentahoOAuthProvider pentahoOAuthProvider = PentahoOAuthProvider.valueOf( registrationId.toUpperCase() );

    switch ( pentahoOAuthProvider ) {
      case AZURE:
        return azurePentahoOAuthHandler;
      case OKTA:
        return oktaPentahoOAuthHandler;
      default:
        throw new IllegalArgumentException( "Unknown Provider: " + registrationId );
    }
  }

}
