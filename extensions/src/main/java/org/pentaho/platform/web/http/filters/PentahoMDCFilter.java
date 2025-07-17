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

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.audit.MDCUtil;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * PentahoMDCFilter is a Servlet filter that adds and clears MDC logging information. The primary use case is to track
 * access to sensitive data back to a user/session/IP.
 * 
 * @author Benny
 *
 */
public class PentahoMDCFilter implements Filter {

  public void init( final FilterConfig filterConfig ) throws ServletException {
    // Nothing to do
  }

  public void doFilter( final ServletRequest request, final ServletResponse response, final FilterChain filterChain )
    throws IOException, ServletException {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    IPentahoSession session = PentahoSessionHolder.getSession();

    MDC.put( MDCUtil.REMOTE_ADDR, request.getRemoteAddr() );
    MDC.put( MDCUtil.REMOTE_HOST, request.getRemoteHost() );
    MDC.put( MDCUtil.REMOTE_PORT, String.valueOf( request.getRemotePort() ) );
    MDC.put( MDCUtil.SERVER_NAME, request.getServerName() );
    MDC.put( MDCUtil.SERVER_PORT, String.valueOf( request.getServerPort() ) );
    if ( request.getLocalAddr() != null ) { // Fix BISERVER-14710
      MDC.put( MDCUtil.LOCAL_ADDR, request.getLocalAddr() );
    }
    MDC.put( MDCUtil.LOCAL_NAME, request.getLocalName() );
    MDC.put( MDCUtil.LOCAL_PORT, String.valueOf( request.getLocalPort() ) );
    if ( authentication != null ) {
      MDC.put( MDCUtil.SESSION_NAME, session.getName() );
      MDC.put( MDCUtil.SESSION_ID, session.getId() );
    }
    try {
      filterChain.doFilter( request, response );
    } finally {
      MDCUtil.clear();
    }
  }

  public void destroy() {
    // Nothing to do
  }

}
