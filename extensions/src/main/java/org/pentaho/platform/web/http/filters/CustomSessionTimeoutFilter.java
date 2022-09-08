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
 * Copyright (c) 2022 Hitachi Vantara. All rights reserved.
 */

package org.pentaho.platform.web.http.filters;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * This Filter allows the customization of the value used for the timeout through the attribute
 * {@value CUSTOM_SESSION_TIMEOUT_PROPERTY_NAME}.<br/>
 * The default timeout value (which is used when the configured value is invalid) is
 * {@value CUSTOM_SESSION_TIMEOUT_DEFAULT_VALUE} milliseconds.
 */
public class CustomSessionTimeoutFilter implements Filter, InitializingBean {

  protected final Log logger = LogFactory.getLog( CustomSessionTimeoutFilter.class );

  /**
   * The name of the property to be defined to customize the TimeOut
   */
  public static final String CUSTOM_SESSION_TIMEOUT_PROPERTY_NAME = "customSessionTimeout";
  /**
   * The default value is 2 hours, i.e. (2 * 60 * 60 * 1000) milliseconds
   */
  public static final int CUSTOM_SESSION_TIMEOUT_DEFAULT_VALUE = 2 * 60 * 60 * 1000;

  private int maxInactiveInterval = CUSTOM_SESSION_TIMEOUT_DEFAULT_VALUE;

  @Override
  public void init( FilterConfig filterConfig ) throws ServletException {
    String customSessionTimeout = filterConfig.getInitParameter( CUSTOM_SESSION_TIMEOUT_PROPERTY_NAME );

    try {
      maxInactiveInterval = Integer.parseInt( customSessionTimeout );
    } catch ( NumberFormatException e ) {
      logger.error( "Invalid value for [" + CUSTOM_SESSION_TIMEOUT_PROPERTY_NAME + "]: ["
        + customSessionTimeout + "], using default value [" + CUSTOM_SESSION_TIMEOUT_DEFAULT_VALUE + "].", e );
      maxInactiveInterval = CUSTOM_SESSION_TIMEOUT_DEFAULT_VALUE;
    }
  }

  @Override
  public void doFilter( ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain )
    throws IOException, ServletException {
    HttpSession httpSession = ( (HttpServletRequest) servletRequest ).getSession( true );

    logger.debug( "Changing Maximum Inactive Interval from [" + httpSession.getMaxInactiveInterval() + "] to ["
      + maxInactiveInterval + "]" );

    httpSession.setMaxInactiveInterval( maxInactiveInterval );

    // Continue the filter chain
    filterChain.doFilter( servletRequest, servletResponse );
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    // Nothing to do
  }
}
