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
 * Copyright (c) 2002-2021 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.web.http.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

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
