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

import org.pentaho.platform.util.oauth.PentahoOAuthUtility;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class PentahoOAuthAuthorizationRequestRedirectFilter extends OAuth2AuthorizationRequestRedirectFilter {

  public PentahoOAuthAuthorizationRequestRedirectFilter( ClientRegistrationRepository clientRegistrationRepository ) {
    super( clientRegistrationRepository );
  }

  public PentahoOAuthAuthorizationRequestRedirectFilter( ClientRegistrationRepository clientRegistrationRepository,
                                                         String authorizationRequestBaseUri) {
    super( clientRegistrationRepository, authorizationRequestBaseUri );
  }

  public PentahoOAuthAuthorizationRequestRedirectFilter( OAuth2AuthorizationRequestResolver authorizationRequestResolver ) {
    super( authorizationRequestResolver );
  }

  @Override
  protected void doFilterInternal( HttpServletRequest servletRequest,
                                   HttpServletResponse servletResponse,
                                   FilterChain filterChain ) throws ServletException, IOException {

    if ( PentahoOAuthUtility.isOAuthEnabled()
            && !PentahoOAuthUtility.isUserNamePasswordAuthentication( servletRequest ) ) {
      super.doFilterInternal( servletRequest, servletResponse, filterChain );
    } else {
      filterChain.doFilter( servletRequest, servletResponse );
    }

  }

}
