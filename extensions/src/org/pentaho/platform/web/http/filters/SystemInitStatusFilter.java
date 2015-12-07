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

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.pentaho.platform.api.engine.IServerStatusProvider;
import org.pentaho.platform.api.engine.ServerStatusProvider;

public class SystemInitStatusFilter extends ForwardFilter {

  protected static final String REDIRECT_PAGE_KEY = "redirectPage"; //$NON-NLS-1$
  protected static final String DEFAULT_PAGE = "html/InitState.html"; //$NON-NLS-1$
  private static final String[] IGNORED_RESOURCES = new String[] {
      "/GetResource?serverStatus"
  };

  protected IServerStatusProvider serverStatusProvider;

  @Override
  public void init( FilterConfig filterConfig ) throws ServletException {
    String redirectPage = filterConfig.getInitParameter( REDIRECT_PAGE_KEY );
    redirectPage = StringUtils.defaultIfBlank( redirectPage, DEFAULT_PAGE );
    setRedirectPath( "/" + redirectPage ); //$NON-NLS-1$ 

    serverStatusProvider = IServerStatusProvider.LOCATOR.getProvider();
  }

  @Override
  protected boolean isEnable() {
    return IServerStatusProvider.ServerStatus.STARTED != serverStatusProvider.getStatus();
  }

  @Override
  public void doFilter( ServletRequest rq, ServletResponse rs, FilterChain filterChain ) throws IOException,
    ServletException {
    String path = getPath( rq );
    if ( HttpServletRequest.class.isInstance( rq ) ) {
      final HttpServletRequest httpRq = (HttpServletRequest) rq;
      if ( httpRq.getQueryString() != null ) {
        path += "?" + httpRq.getQueryString();
      }
    }
    if ( path.equalsIgnoreCase( "/content" ) || checkIgnoredResources( path ) ) {
      filterChain.doFilter( rq, rs );
    } else {
      super.doFilter( rq, rs, filterChain );
    }
  }

  private boolean checkIgnoredResources( String uri ) {
    for ( String res : IGNORED_RESOURCES ) {
      if ( uri.equals( res ) ) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void destroy() {
    // nothing
  }

}
