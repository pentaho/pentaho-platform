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

package org.pentaho.platform.web.http.security;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.web.http.PreAuthenticatedSessionHolder;
import org.springframework.security.context.SecurityContextHolder;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * This filter handles the assignment of IPentahoSession and Authentication based on a previously stored in the
 * IPreAuthenticatedSessionHolder.
 * 
 * User: nbaker Date: 6/28/12
 */
public class PreAuthenticatedFilter implements Filter {

  private static Log log = LogFactory.getLog( PreAuthenticatedFilter.class );
  private PreAuthenticatedSessionHolder preAuthHolder;
  private static final String ASSO_KEY = "asso";

  public PreAuthenticatedFilter( PreAuthenticatedSessionHolder sessionHolder ) {
    this.preAuthHolder = sessionHolder;
  }

  @Override
  public void destroy() {
    preAuthHolder.close();
  }

  @Override
  public void init( FilterConfig filterConfig ) throws ServletException {

  }

  @Override
  public void doFilter( ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain )
    throws IOException, ServletException {
    try {
      String assoKey = servletRequest.getParameter( ASSO_KEY );
      if ( StringUtils.isEmpty( assoKey ) ) {
        return;
      }
      preAuthHolder.restoreSession( assoKey );

    } catch ( Exception e ) {
      log.error( e );
      // ensure that no credentials remain.
      SecurityContextHolder.getContext().setAuthentication( null );
      PentahoSessionHolder.removeSession();
    } finally {
      filterChain.doFilter( servletRequest, servletResponse );
    }
  }

}
