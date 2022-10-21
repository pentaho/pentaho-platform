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
import org.pentaho.platform.web.http.messages.Messages;
import org.springframework.beans.factory.InitializingBean;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
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
 * {@value CUSTOM_SESSION_TIMEOUT_DEFAULT_VALUE} seconds.
 */
public class CustomSessionTimeoutFilter implements Filter, InitializingBean {

  private static final Log logger = LogFactory.getLog( CustomSessionTimeoutFilter.class );

  /**
   * The name of the property to be defined to customize the TimeOut
   */
  public static final String CUSTOM_SESSION_TIMEOUT_PROPERTY_NAME = "customSessionTimeout";
  /**
   * The default value is 2 hours, i.e. (2 * 60 * 60) seconds
   */
  public static final int CUSTOM_SESSION_TIMEOUT_DEFAULT_VALUE = 2 * 60 * 60;

  private int customSessionTimeout = CUSTOM_SESSION_TIMEOUT_DEFAULT_VALUE;

  @Override
  public void doFilter( ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain )
    throws IOException, ServletException {
    HttpSession httpSession = ( (HttpServletRequest) servletRequest ).getSession( true );

    if ( CustomSessionTimeoutFilter.logger.isDebugEnabled() ) {
      String messageKey =
        ( customSessionTimeout > 0 ) ? "CustomSessionTimeoutFilter.CHANGING_MAXIMUM_INACTIVE_INTERVAL" :
          "CustomSessionTimeoutFilter.CHANGING_MAXIMUM_INACTIVE_INTERVAL_NO_TIMEOUT";

      CustomSessionTimeoutFilter.logger.debug( Messages.getInstance()
        .getString( messageKey, httpSession.getMaxInactiveInterval(), customSessionTimeout,
          ( (HttpServletRequest) servletRequest ).getRequestURI() ) );
    }

    httpSession.setMaxInactiveInterval( customSessionTimeout );

    // Continue the filter chain
    try {
      filterChain.doFilter( servletRequest, servletResponse );
    } finally {
      int configuredInactiveInterval = servletRequest.getServletContext().getSessionTimeout() * 60;

      if ( CustomSessionTimeoutFilter.logger.isDebugEnabled() ) {
        CustomSessionTimeoutFilter.logger.debug( Messages.getInstance()
          .getString( "CustomSessionTimeoutFilter.CHANGING_BACK_MAXIMUM_INACTIVE_INTERVAL", customSessionTimeout,
            configuredInactiveInterval, ( (HttpServletRequest) servletRequest ).getRequestURI() ) );
      }

      httpSession.setMaxInactiveInterval( configuredInactiveInterval );
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    // Nothing to do here.
  }

  public int getCustomSessionTimeout() {
    return customSessionTimeout;
  }

  public void setCustomSessionTimeout( int customSessionTimeout ) {
    this.customSessionTimeout = customSessionTimeout;
  }
}
