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

import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

/**
 * The {@code UserNavigationAwareAuthenticationEntryPoint} class is an authentication entry point that determines the
 * type of request based on the presence of the `sec-fetch-user` HTTP request header. If the request is determined to be
 * a user navigation request, it delegates to the user authentication entry point specified in the constructor.
 * Otherwise, it delegates to the specified API authentication entry point.
 * <p>
 * This approach allows using the most appropriate authentication method depending on if the request is made directly by
 * a user or by an API client.
 */
public class UserNavigationAwareAuthenticationEntryPoint implements AuthenticationEntryPoint {
  /**
   * The `sec-fetch-user` HTTP request header is included by web browsers to indicate that a request was initiated by
   * a <i>user navigation</i>.
   * <p>
   * See
   * <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Sec-Fetch-User">MDN Sec-Fetch-User Header</a>.
   */
  static final String HEADER_SEC_FETCH_USER = "sec-fetch-user";

  /**
   * The value of the `sec-fetch-user` request header is a Structured Field Boolean.
   * <p>
   * See
   * <a href="https://www.rfc-editor.org/rfc/rfc9651.html#name-abnf">Structured Field Values for HTTP specification</a>.
   */
  static final String HEADER_SF_TRUE = "?1";

  private final AuthenticationEntryPoint userAuthenticationEntryPoint;
  private final AuthenticationEntryPoint apiAuthenticationEntryPoint;

  public UserNavigationAwareAuthenticationEntryPoint(
    @NonNull AuthenticationEntryPoint userAuthenticationEntryPoint,
    @NonNull AuthenticationEntryPoint apiAuthenticationEntryPoint ) {
    this.userAuthenticationEntryPoint = Objects.requireNonNull( userAuthenticationEntryPoint );
    this.apiAuthenticationEntryPoint = Objects.requireNonNull( apiAuthenticationEntryPoint );
  }

  @Override
  public void commence( HttpServletRequest request,
                        HttpServletResponse response,
                        AuthenticationException authException )
    throws IOException, ServletException {

    boolean isUserNavigation = HEADER_SF_TRUE.equals( request.getHeader( HEADER_SEC_FETCH_USER ) );
    if ( isUserNavigation ) {
      userAuthenticationEntryPoint.commence( request, response, authException );
    } else {
      apiAuthenticationEntryPoint.commence( request, response, authException );
    }
  }
}
