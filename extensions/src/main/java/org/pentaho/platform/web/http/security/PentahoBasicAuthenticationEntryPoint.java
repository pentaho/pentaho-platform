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
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class PentahoBasicAuthenticationEntryPoint extends BasicAuthenticationEntryPoint implements
  AuthenticationEntryPoint, InitializingBean {

  public PentahoBasicAuthenticationEntryPoint() {
  }

  @Override
  public void commence( HttpServletRequest request,
                        HttpServletResponse response,
                        AuthenticationException authException ) throws IOException {

    // The BasicAuthenticationEntryPoint will send a WWW-Authenticate header and a 401 status code back to the browser,
    // forcing the user to authenticate. If there is a session end cookie present, this will trigger a second
    // authentication.
    // In order to prevent a second authentication, we must clear the session end cookie before sending the 401
    // status code.
    PentahoBasicProcessingFilter.clearSessionFlushedCookie( request, response );

    super.commence( request, response, authException );
  }
}
