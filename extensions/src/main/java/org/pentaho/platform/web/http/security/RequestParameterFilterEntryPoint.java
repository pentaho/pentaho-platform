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


package org.pentaho.platform.web.http.security;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Used by the <code>SecurityEnforcementFilter</code> to commence authentication via the {@link PentahoBasicProcessingFilter}.
 * 
 * <P>
 * Once a user agent is authenticated using Request Parameter authentication, logout requires that the browser be closed
 * or an unauthorized (401) header be sent. The simplest way of achieving the latter is to call the
 * {@link #commence(HttpServletRequest, HttpServletResponse, AuthenticationException)} method below. This will indicate to the browser its credentials
 * are no longer authorized, causing it to prompt the user to login again.
 * </p>
 */
public class RequestParameterFilterEntryPoint implements AuthenticationEntryPoint, InitializingBean {
  // ~ Instance fields ========================================================

  // ~ Methods ================================================================

  public void afterPropertiesSet() throws Exception {
    // Everything is OK
  }

  @Override
  public void commence( final HttpServletRequest request, final HttpServletResponse response, final AuthenticationException authException ) throws IOException, ServletException {
    response.sendError( HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage() );
  }
}
