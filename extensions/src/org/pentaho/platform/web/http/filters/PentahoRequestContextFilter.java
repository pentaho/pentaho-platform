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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoRequestContext;
import org.pentaho.platform.engine.core.system.BasePentahoRequestContext;
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

/**
 * Populates the {@link PentahoRequestContextHolder} with context information obtained from the
 * <code>HttpServletRequest</code>.
 */

public class PentahoRequestContextFilter implements Filter {

  private static final Log logger = LogFactory.getLog( PentahoRequestContextFilter.class );
  static final String FILTER_APPLIED = "__pentaho_request_context_filter_applied"; //$NON-NLS-1$

  private volatile IPentahoRequestContext requestContext;

  private synchronized void initContext( HttpServletRequest httpRequest ) {
    if ( requestContext == null ) {
      requestContext = new BasePentahoRequestContext( httpRequest.getContextPath() );
    }
  }

  /**
   * 
   * @param filterConfig
   *          ignored
   * @throws ServletException
   *           ignored
   */
  public void init( FilterConfig filterConfig ) throws ServletException {
  }

  /**
   */
  public void destroy() {
  }

  public void doFilter( ServletRequest request, ServletResponse response, FilterChain chain ) throws IOException,
    ServletException {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;

    if ( httpRequest.getAttribute( FILTER_APPLIED ) != null ) {
      // ensure that filter is only applied once per request
      chain.doFilter( httpRequest, httpResponse );
      return;
    }

    httpRequest.setAttribute( FILTER_APPLIED, Boolean.TRUE );

    try {
      if ( requestContext == null ) {
        initContext( httpRequest );
      }
      PentahoRequestContextHolder.setRequestContext( requestContext );
      chain.doFilter( httpRequest, httpResponse );
    } finally {
      PentahoRequestContextHolder.removeRequestContext();
      httpRequest.removeAttribute( FILTER_APPLIED );
      if ( logger.isDebugEnabled() ) {
        logger.debug( "PentahoRequestContextHolder now cleared, as request processing completed" );
      }
    }

  }
}
