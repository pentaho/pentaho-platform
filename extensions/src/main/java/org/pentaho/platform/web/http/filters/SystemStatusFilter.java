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

import org.pentaho.platform.engine.core.system.PentahoSystem;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * The purpose of this filter is to check to make sure that the platform is properly initialized before letting requests
 * in.
 * 
 */
public class SystemStatusFilter implements Filter {

  private String redirectToOnInitError;

  private boolean systemInitializedOk;

  public void init( final FilterConfig filterConfig ) throws ServletException {
    String failurePage = filterConfig.getInitParameter( "initFailurePage" ); //$NON-NLS-1$
    if ( ( failurePage == null ) || ( failurePage.length() == 0 ) ) {
      failurePage = "InitFailure"; //$NON-NLS-1$
    }
    redirectToOnInitError = "/" + failurePage; //$NON-NLS-1$ 
    systemInitializedOk = PentahoSystem.getInitializedOK();
  }

  public void doFilter( final ServletRequest request, final ServletResponse response, final FilterChain filterChain )
    throws IOException, ServletException {
    if ( systemInitializedOk ) {
      filterChain.doFilter( request, response );
    } else {
      HttpServletRequest req = (HttpServletRequest) request;
      if ( req.getServletPath().endsWith( redirectToOnInitError ) ) {
        filterChain.doFilter( request, response );
      } else {
        RequestDispatcher dispatcher = request.getRequestDispatcher( redirectToOnInitError );
        dispatcher.forward( request, response );
      }
    }
  }

  public void destroy() {
  }

}
