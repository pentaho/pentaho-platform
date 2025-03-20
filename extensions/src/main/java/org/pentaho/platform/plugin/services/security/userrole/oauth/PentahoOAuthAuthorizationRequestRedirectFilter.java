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

/**
 * PentahoOAuthAuthorizationRequestRedirectFilter is a custom filter that extends the OAuth2AuthorizationRequestRedirectFilter.
 * It is used to handle OAuth2 authorization request redirection and check if OAuth is enabled.
 */
public class PentahoOAuthAuthorizationRequestRedirectFilter extends OAuth2AuthorizationRequestRedirectFilter {

  public PentahoOAuthAuthorizationRequestRedirectFilter( ClientRegistrationRepository clientRegistrationRepository ) {
    super( clientRegistrationRepository );
  }

  public PentahoOAuthAuthorizationRequestRedirectFilter( ClientRegistrationRepository clientRegistrationRepository,
                                                         String authorizationRequestBaseUri ) {
    super( clientRegistrationRepository, authorizationRequestBaseUri );
  }

  public PentahoOAuthAuthorizationRequestRedirectFilter(
    OAuth2AuthorizationRequestResolver authorizationRequestResolver ) {
    super( authorizationRequestResolver );
  }

  /**
   * This method checks if OAuth is enabled and if the request is not for username/password authentication.
   * If both conditions are met, it calls the super method to handle the OAuth2 authorization request redirection.
   * Otherwise, it simply continues the filter chain without any action.
   *
   * @param servletRequest  The HttpServletRequest object
   * @param servletResponse The HttpServletResponse object
   * @param filterChain     The FilterChain object
   * @throws ServletException If an error occurs during the filter processing
   * @throws IOException      If an I/O error occurs
   */
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
