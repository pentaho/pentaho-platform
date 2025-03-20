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


package org.pentaho.platform.web.http.security;

import org.pentaho.platform.api.security.ILoginAttemptService;
import org.pentaho.platform.util.oauth.PentahoOAuthUtility;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class PreventBruteForceUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

  private ILoginAttemptService loginAttemptService;

  public PreventBruteForceUsernamePasswordAuthenticationFilter( ILoginAttemptService loginAttemptService ) {
    super();
    this.loginAttemptService = loginAttemptService;
  }

  @Override public Authentication attemptAuthentication( HttpServletRequest request, HttpServletResponse response ) {
    String clientIp = getClientIp( request );
    if ( this.loginAttemptService.isBlocked( clientIp ) ) {
      throw new PreventBruteForceException( "Authentication blocked to prevent brute force login" );
    }
    return super.attemptAuthentication( request, response );
  }

  private String getClientIp( HttpServletRequest request ) {
    String xfHeader = request.getHeader( "X-Forwarded-For" );
    if ( xfHeader == null ) {
      return request.getRemoteAddr();
    }
    return xfHeader.split( "," )[0];
  }

  @Override
  public void doFilter( ServletRequest request, ServletResponse response, FilterChain chain )
          throws IOException, ServletException {

    if ( PentahoOAuthUtility.isOAuthEnabled()
            && !PentahoOAuthUtility.isUserNamePasswordAuthentication( (HttpServletRequest) request ) ) {
      chain.doFilter( request, response );
    } else {
      super.doFilter( request, response, chain );
    }
  }

}
