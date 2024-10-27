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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoRequestContext;
import org.pentaho.platform.engine.core.system.BasePentahoRequestContext;
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
