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

import org.apache.commons.lang.StringUtils;
import org.pentaho.platform.web.http.messages.Messages;
import org.springframework.beans.factory.InitializingBean;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CheckRefererFilter implements Filter, InitializingBean {

  private String refererPrefix;
  private String redirectTo;
  private boolean checked;

  public void afterPropertiesSet() throws Exception {
    if ( StringUtils.isBlank( refererPrefix ) ) {
      throw new ServletException( Messages.getInstance().getErrorString(
        "CheckRefererFilter.ERROR_0001_REFERER_PREFIX_NOT_SPECIFIED" ) ); //$NON-NLS-1$
    }
    if ( StringUtils.isBlank( redirectTo ) ) {
      throw new ServletException( Messages.getInstance().getErrorString(
          "CheckRefererFilter.ERROR_0002_REDIRECT_NOT_SPECIFIED" ) ); //$NON-NLS-1$
    }
    checked = true;
  }

  public void setRefererPrefix( String value ) {
    this.refererPrefix = value;
  }

  public void setRedirectTo( String value ) {
    this.redirectTo = value;
  }

  public void doFilter( ServletRequest request, ServletResponse response, FilterChain chain ) throws IOException,
    ServletException {
    HttpServletRequest req = (HttpServletRequest) request;
    String header = req.getHeader( "referer" ); //$NON-NLS-1$
    if ( ( header != null ) && ( header.startsWith( refererPrefix ) ) ) {
      chain.doFilter( request, response );
    } else {
      // Illegal Referer - cloaked, missing, or invalid
      System.out.println( "***** No Referrer: " + req.getRequestURL() ); //$NON-NLS-1$
      HttpServletResponse resp = (HttpServletResponse) response;
      resp.sendRedirect( redirectTo );
    }
  }

  public void destroy() {
    // Required to be here...
  }

  public void init( FilterConfig filterConfig ) throws ServletException {
    String pfx = filterConfig.getInitParameter( "refererPrefix" ); //$NON-NLS-1$
    String redirect = filterConfig.getInitParameter( "redirectTo" ); //$NON-NLS-1$
    if ( !( StringUtils.isBlank( pfx ) ) ) {
      this.setRefererPrefix( pfx );
    }
    if ( !( StringUtils.isBlank( redirect ) ) ) {
      this.setRedirectTo( redirect );
    }
    if ( !checked ) {
      try {
        afterPropertiesSet();
      } catch ( Exception e ) {
        throw new ServletException( e );
      }
    }
  }

}
