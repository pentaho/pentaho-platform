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

import org.pentaho.platform.engine.core.system.PentahoSystem;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
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
