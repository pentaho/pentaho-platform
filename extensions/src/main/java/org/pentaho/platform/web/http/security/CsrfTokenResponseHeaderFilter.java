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
 * Copyright (c) 2019 Hitachi Vantara. All rights reserved.
 *
 */
package org.pentaho.platform.web.http.security;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.FilterChain;
import java.io.IOException;

/**
 * Binds a {@link org.springframework.security.web.csrf.CsrfToken} to the {@link HttpServletResponse} headers if the
 * Spring {@link org.springframework.security.web.csrf.CsrfFilter} has placed one in the {@link HttpServletRequest}.
 *
 * <p>
 *   Based on the work found in: http://stackoverflow.com/questions/20862299/with-spring-security-3-2-0-release-how-can-i-get-the-csrf-token-in-a-page-that
 * <p>
 *
 * Code from https://github.com/aditzel/spring-security-csrf-filter/blob/master/src/main/java/com/allanditzel/springframework/security/web/csrf/CsrfTokenResponseHeaderBindingFilter.java
 */
public class CsrfTokenResponseHeaderFilter extends OncePerRequestFilter {
  private static final String REQUEST_ATTRIBUTE_NAME = "_csrf";
  private static final String RESPONSE_HEADER_NAME = "X-CSRF-HEADER";
  private static final String RESPONSE_PARAM_NAME = "X-CSRF-PARAM";
  private static final String RESPONSE_TOKEN_NAME = "X-CSRF-TOKEN";

  @Override
  protected void doFilterInternal( HttpServletRequest request,
                                   HttpServletResponse response,
                                   FilterChain filterChain ) throws ServletException, IOException {
    final CsrfToken token = (CsrfToken) request.getAttribute( REQUEST_ATTRIBUTE_NAME );

    if ( token != null ) {
      final String tokenHeaderName = token.getHeaderName();
      response.setHeader( RESPONSE_HEADER_NAME, tokenHeaderName );

      final String tokenParameterName = token.getParameterName();
      response.setHeader( RESPONSE_PARAM_NAME, tokenParameterName );

      final String tokenValue = token.getToken();
      response.setHeader( RESPONSE_TOKEN_NAME , tokenValue );

      return;
    }

    filterChain.doFilter( request, response );
  }
}
