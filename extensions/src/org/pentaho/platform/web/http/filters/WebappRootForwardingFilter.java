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

package org.pentaho.platform.web.http.filters;

import org.pentaho.platform.engine.core.system.PentahoRequestContextHolder;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Populates the {@link PentahoRequestContextHolder} with context information obtained from the
 * <code>HttpServletRequest</code>.
 */

public class WebappRootForwardingFilter implements Filter {

  private static final Pattern pentahoBasePattern = Pattern.compile( "^/.*/WEBAPP_ROOT/" ); //$NON-NLS-1$

  public void init( FilterConfig filterConfig ) throws ServletException {
  }

  public void destroy() {
  }

  public void doFilter( ServletRequest request, ServletResponse response, FilterChain chain ) throws IOException,
    ServletException {
    HttpServletRequest httpRequest = (HttpServletRequest) request;

    String incomingUrl = httpRequest.getRequestURI();

    Matcher matcher = pentahoBasePattern.matcher( incomingUrl );

    if ( matcher.find() ) {
      String pentahoBasePrefix = matcher.group();
      String newUrl = incomingUrl.substring( pentahoBasePrefix.length() ); //$NON-NLS-1$

      // Use either ServletRequest#getRequestDispatcher() and then RequestDispatcher#forward() to forward the
      // request/response to the new URL (server-side redirect, not reflected in browser address bar)
      // RequestDispatcher request_Dispatcher = request.getRequestDispatcher(newUrl);
      // request_Dispatcher.forward(request, response);
      // Or cast the incoming ServletResponse to HttpServletResponse and then HttpServletResponse#sendRedirect() to
      // redirect the response to the new URL (client side redirect, reflected in browser address bar).
      ( (HttpServletResponse) response ).sendRedirect( PentahoRequestContextHolder.getRequestContext().getContextPath()
        + newUrl );
    } else {
      chain.doFilter( request, response );
    }
  }
}
