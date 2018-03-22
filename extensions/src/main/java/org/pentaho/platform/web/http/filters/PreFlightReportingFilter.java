/*!
 *
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
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.web.http.filters;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.HttpMethod;
import java.io.IOException;

public class PreFlightReportingFilter implements Filter {

  public void init( FilterConfig filterConfig ) throws ServletException {
  }

  public void destroy() {
  }

  public void doFilter( ServletRequest request, ServletResponse response, FilterChain chain ) throws IOException,
    ServletException {
    HttpServletRequest httpRequest = (HttpServletRequest) request;

    if ( isPreFlight( httpRequest ) ) {
      final HttpServletResponse servletResponse = (HttpServletResponse) response;
      servletResponse.sendError( HttpServletResponse.SC_METHOD_NOT_ALLOWED );
    } else {
      chain.doFilter( request, response );
    }
  }

  private boolean isPreFlight( final HttpServletRequest request ) {
    return ( HttpMethod.OPTIONS.equals( request.getMethod() ) || HttpMethod.HEAD.equals( request.getMethod() ) );
  }

}
