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

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * PentahoBasicPostProcessingFilter is used to invalidate a browsers cached Basic-Auth credentials. This Filter works in
 * concert with /js/postAuth.js. An initial request is sent to the URL bound to this Filter. That initial request will
 * have the valid Basic-Auth credentials, but is denied. A second request will then be sent with known fake credentials.
 * This second request is accepted, causing the browser to replace the old good credentials with bad. The next time the
 * browser's session is timed out the new bad credentials will fail.
 * 
 * User: nbaker Date: 8/16/13
 */
public class PentahoBasicPostProcessingFilter implements Filter {

  @Override
  public void destroy() {

  }

  @Override
  public void init( FilterConfig filterConfig ) throws ServletException {
  }

  @Override
  public void doFilter( ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain )
    throws IOException, ServletException {

    HttpServletResponse response = (HttpServletResponse) servletResponse;
    HttpServletRequest request = (HttpServletRequest) servletRequest;
    String authorization = request.getHeader( "Authorization" );
    if ( authorization != null && authorization.startsWith( "Basic" )
        && authorization.equals( "Basic AAAAAAAAAAAAAAAAAAA=" ) ) {
      response.setStatus( 200 );
      return;
    }
    response.sendError( 401, "Unauthorized" );
    response.addHeader( "WWW-Authenticate", "basic realm=\"Pentaho Realm\"" );
  }
}
