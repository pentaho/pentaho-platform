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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;

public class PentahoOAuthUserCreationFilter extends GenericFilterBean {

  private PentahoOAuthUserRoleService userRoleListService;

  public PentahoOAuthUserCreationFilter( PentahoOAuthUserRoleService userRoleListService ) {
    this.userRoleListService = userRoleListService;
  }

  /**
   * This method is called for each request to the servlet. It checks if OAuth is enabled and if the current
   * authentication is an instance of OAuth2AuthenticationToken. If so, it creates a user using the
   * PentahoOAuthUserRoleService in jackrabbit.
   *
   * @param request  The servlet request
   * @param response The servlet response
   * @param chain    The filter chain
   * @throws IOException      If an I/O error occurs
   * @throws ServletException If a servlet error occurs
   */
  @Override
  public void doFilter( ServletRequest request, ServletResponse response, FilterChain chain )
    throws IOException, ServletException {

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if ( PentahoOAuthUtility.isOAuthEnabled() && authentication instanceof OAuth2AuthenticationToken ) {
      userRoleListService.createUser( (OAuth2AuthenticationToken) authentication );
    }

    chain.doFilter( request, response );
  }

}
