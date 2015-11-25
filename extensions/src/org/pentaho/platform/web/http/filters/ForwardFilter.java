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
 * Copyright (c) 2002-2015 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.web.http.filters;

import org.apache.commons.lang.StringUtils;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

public abstract class ForwardFilter implements Filter {

  private String redirectPath = null;

  public void setRedirectPath( String redirectPath ) {
    this.redirectPath = redirectPath;
  }

  public String getRedirectPath() {
    return redirectPath;
  }

  protected abstract boolean isEnable();

  protected static String getPath( ServletRequest rq ) {
    if ( HttpServletRequest.class.isInstance( rq ) ) {
      HttpServletRequest httpRq = (HttpServletRequest) rq;
      return httpRq.getServletPath();
    }
    return "";
  }

  @Override
  public void doFilter( ServletRequest rq, ServletResponse rs, FilterChain filterChain ) throws IOException,
    ServletException {
    if ( isEnable() && StringUtils.isNotBlank( redirectPath ) ) {
      if ( getPath( rq ).endsWith( redirectPath ) ) {
        filterChain.doFilter( rq, rs );
      } else {
        RequestDispatcher dispatcher = rq.getRequestDispatcher( redirectPath );
        dispatcher.forward( rq, rs );
      }
    } else {
      filterChain.doFilter( rq, rs );
    }
  }

}
