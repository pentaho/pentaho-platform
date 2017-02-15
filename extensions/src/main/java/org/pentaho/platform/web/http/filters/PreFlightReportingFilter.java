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
 * Copyright (c) 2002-2017 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.web.http.filters;

import org.pentaho.platform.engine.core.system.PentahoSystem;

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
import java.util.regex.Pattern;

public class PreFlightReportingFilter implements Filter {


  private static final String DEFAULT_PATTERN = "^.*(reporting/api/jobs|api/repos/.*\\.prpt).*$";
  private static final String PRE_FLIGHT_PATTERN = "pre-flight-pattern";
  private final Pattern preFlightPattern;


  public PreFlightReportingFilter() {
    final String regex = PentahoSystem.getSystemSetting( PRE_FLIGHT_PATTERN, DEFAULT_PATTERN );
    preFlightPattern = Pattern.compile( regex );
  }

  public void init( FilterConfig filterConfig ) throws ServletException {
  }

  public void destroy() {
  }

  public void doFilter( ServletRequest request, ServletResponse response, FilterChain chain ) throws IOException,
    ServletException {
    HttpServletRequest httpRequest = (HttpServletRequest) request;

    if ( isReportingPreFlight( httpRequest ) ) {
      final HttpServletResponse servletResponse = (HttpServletResponse) response;
      servletResponse.setStatus( HttpServletResponse.SC_OK );
    } else {
      chain.doFilter( request, response );
    }
  }

  boolean isReportingPreFlight( final HttpServletRequest request ) {
    return ( HttpMethod.OPTIONS.equals( request.getMethod() )
      || HttpMethod.HEAD.equals( request.getMethod() ) ) && preFlightPattern
      .matcher( request.getRequestURI() )
      .matches();
  }

}
