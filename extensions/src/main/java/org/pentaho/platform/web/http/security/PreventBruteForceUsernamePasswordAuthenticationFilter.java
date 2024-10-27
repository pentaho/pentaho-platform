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
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
}
