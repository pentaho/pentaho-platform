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

  PentahoOAuthAzureHandler pentahoOAuthAzureHandler;

  PentahoOAuthOktaHandler pentahoOAuthOktaHandler;

  public void setPentahoOAuthAzureHandler( PentahoOAuthAzureHandler pentahoOAuthAzureHandler ) {
    this.pentahoOAuthAzureHandler = pentahoOAuthAzureHandler;
  }

  public void setPentahoOAuthOktaHandler( PentahoOAuthOktaHandler pentahoOAuthOktaHandler ) {
    this.pentahoOAuthOktaHandler = pentahoOAuthOktaHandler;
  }

  public IPentahoOAuthHandler getInstance( String registrationId ) {
    if ( StringUtils.isBlank( registrationId ) ) {
      return null;
    }

    switch ( registrationId ) {
      case "azure":
        return pentahoOAuthAzureHandler;
      case "okta":
        return pentahoOAuthOktaHandler;
      default:
        return null;
    }
  }

}
