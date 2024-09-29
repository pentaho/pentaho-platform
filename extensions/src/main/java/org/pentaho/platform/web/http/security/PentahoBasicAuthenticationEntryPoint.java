/*!
 *
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
 * Copyright (c) 2002-2022 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.web.http.security;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
