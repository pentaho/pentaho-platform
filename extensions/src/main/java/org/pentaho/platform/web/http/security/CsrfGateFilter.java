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
 *
 * Copyright (c) 2019 Hitachi Vantara. All rights reserved.
 */
package org.pentaho.platform.web.http.security;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.CsrfProtectionDefinition;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.IPluginManagerListener;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.web.WebUtil;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

public class CsrfGateFilter implements Filter {

  private static final Log logger = LogFactory.getLog( CsrfGateFilter.class );

  private org.springframework.security.web.csrf.CsrfFilter innerCsrfFilter;
  private boolean isCsrfProtectionEnabled = true;
  private boolean initialized = false;

  public CsrfGateFilter( CsrfTokenRepository csrfTokenRepository ) {
    this.innerCsrfFilter = new org.springframework.security.web.csrf.CsrfFilter( csrfTokenRepository );
  }

  @VisibleForTesting
  boolean getIsCsrfProtectionEnabled() {
    return isCsrfProtectionEnabled;
  }

  @VisibleForTesting
  org.springframework.security.web.csrf.CsrfFilter getInnerCsrfFilter() {
    return this.innerCsrfFilter;
  }

  @VisibleForTesting
  boolean getInitialized() {
    return this.initialized;
  }

  @VisibleForTesting
  void setInitialized( boolean initialized ) {
    this.initialized = initialized;
  }

  /**
   * Specifies an {@link AccessDeniedHandler} that should be used when CSRF protection fails.
   *
   * @param accessDeniedHandler the {@link AccessDeniedHandler} to use
   */
  public void setAccessDeniedHandler( AccessDeniedHandler accessDeniedHandler ) {
    this.innerCsrfFilter.setAccessDeniedHandler( accessDeniedHandler );
  }

  @Override
  public void init( FilterConfig filterConfig ) throws ServletException {

    this.innerCsrfFilter.init( filterConfig );

    PentahoSystem.get( IPluginManager.class ).addPluginManagerListener( new IPluginManagerListener() {
      @Override
      public void onReload() {
        try {
          CsrfGateFilter.this.initialized = false;
          CsrfGateFilter.this.doInit();
        } catch ( ServletException e ) {
          logger.error( e );
        }
      }
    } );

    this.doInit();
  }

  private synchronized void doInit() throws ServletException {
    if ( logger.isDebugEnabled() ) {
      logger.debug( "CsrfGateFilter.init" );
    }

    if ( this.initialized ) {
      return;
    }

    this.isCsrfProtectionEnabled = PentahoSystem.isCsrfProtectionEnabled();
    if ( this.isCsrfProtectionEnabled ) {

      RequestMatcher requestMatcher =
          WebUtil.buildCsrfRequestMatcher( PentahoSystem.getAll( CsrfProtectionDefinition.class ) );
      if ( requestMatcher == null ) {
        this.isCsrfProtectionEnabled = false;
      } else {
        this.innerCsrfFilter.setRequireCsrfProtectionMatcher( requestMatcher );
      }
    }

    initialized = true;
  }

  @Override
  public void doFilter( ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain )
      throws IOException, ServletException {

    if ( !this.initialized ) {
      doInit();
    }

    if ( this.isCsrfProtectionEnabled ) {
      this.innerCsrfFilter.doFilter( servletRequest, servletResponse, filterChain );
    } else {
      filterChain.doFilter( servletRequest, servletResponse );
    }
  }

  @Override
  public void destroy() {
    this.innerCsrfFilter.destroy();
  }
}
