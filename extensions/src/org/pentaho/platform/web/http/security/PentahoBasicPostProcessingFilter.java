/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.web.http.security;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
    response.setStatus( 401, "Unauthorized" );
    response.addHeader( "WWW-Authenticate", "basic realm=\"Pentaho Realm\"" );
  }
}
