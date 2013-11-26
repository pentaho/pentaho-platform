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

import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.AuthenticationException;
import org.springframework.security.ui.AuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Used by the <code>SecurityEnforcementFilter</code> to commence authentication via the {@link BasicProcessingFilter}.
 * 
 * <P>
 * Once a user agent is authenticated using Request Parameter authentication, logout requires that the browser be closed
 * or an unauthorized (401) header be sent. The simplest way of achieving the latter is to call the
 * {@link #commence(ServletRequest, ServletResponse)} method below. This will indicate to the browser its credentials
 * are no longer authorized, causing it to prompt the user to login again.
 * </p>
 */
public class RequestParameterFilterEntryPoint implements AuthenticationEntryPoint, InitializingBean {
  // ~ Instance fields ========================================================

  // ~ Methods ================================================================

  public void afterPropertiesSet() throws Exception {
    // Everything is OK
  }

  public void commence( final ServletRequest request, final ServletResponse response,
      final AuthenticationException authException ) throws IOException, ServletException {
    HttpServletResponse httpResponse = (HttpServletResponse) response;
    httpResponse.sendError( HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage() );
  }
}
