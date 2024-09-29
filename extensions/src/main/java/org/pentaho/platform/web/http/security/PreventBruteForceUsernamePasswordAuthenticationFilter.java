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
 * Copyright (c) 2020 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.web.http.security;

import org.pentaho.platform.api.security.ILoginAttemptService;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class PreventBruteForceUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

  private ILoginAttemptService loginAttemptService;

  public PreventBruteForceUsernamePasswordAuthenticationFilter( ILoginAttemptService loginAttemptService ) {
    super();
    this.loginAttemptService = loginAttemptService;
  }

  @Override public Authentication attemptAuthentication( HttpServletRequest request, HttpServletResponse response ) {
    String clientIp = getClientIp( request );
    if ( this.loginAttemptService.isBlocked( clientIp ) ) {
      throw new PreventBruteForceException( "Authentication blocked to prevent brute force login" );
    }
    return super.attemptAuthentication( request, response );
  }

  private String getClientIp( HttpServletRequest request ) {
    String xfHeader = request.getHeader( "X-Forwarded-For" );
    if ( xfHeader == null ) {
      return request.getRemoteAddr();
    }
    return xfHeader.split( "," )[0];
  }
}
