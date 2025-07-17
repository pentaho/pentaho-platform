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
import org.pentaho.platform.util.RequestIdUtil;
import org.slf4j.MDC;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

public class RequestIdFilter implements Filter {

  private static final Log logger = LogFactory.getLog( RequestIdFilter.class );
  public void destroy() {
  }

  public void doFilter( ServletRequest req, ServletResponse resp, FilterChain chain )
    throws ServletException, IOException {

    HttpServletRequest request = (HttpServletRequest) req;
    String requestId = Optional.ofNullable( request.getHeader( RequestIdUtil.X_REQUEST_ID ) ).orElse(
      UUID.randomUUID().toString() );

    try {

      if ( logger.isDebugEnabled() ) {
        logger.debug( "received request with request id of: " + requestId );
      }
      MDC.put( RequestIdUtil.REQUEST_ID, RequestIdUtil.getFormattedRequestUid( requestId ) );

      chain.doFilter( req, resp );

    } finally {

      if ( logger.isDebugEnabled() ) {
        logger.debug( "Exiting request with request id of: " + requestId );
      }
      ( (HttpServletResponse) resp ).setHeader( RequestIdUtil.X_REQUEST_ID, requestId );
      MDC.remove( RequestIdUtil.X_REQUEST_ID );
    }
  }

  public void init( FilterConfig config ) throws ServletException {

  }

}
