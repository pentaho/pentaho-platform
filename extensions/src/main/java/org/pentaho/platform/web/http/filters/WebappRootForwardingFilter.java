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


package org.pentaho.platform.web.http.filters;

import org.pentaho.platform.engine.core.system.PentahoRequestContextHolder;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
