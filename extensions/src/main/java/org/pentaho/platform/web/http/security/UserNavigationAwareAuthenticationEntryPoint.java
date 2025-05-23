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
import edu.umd.cs.findbugs.annotations.Nullable;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
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
   * a <i>user navigation</i> and having a user activation state.
   * <p>
   * See
   * <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Sec-Fetch-User">MDN Sec-Fetch-User Header</a>.
   */
  static final String HEADER_SEC_FETCH_USER = "sec-fetch-user";

  /**
   * The `sec-fetch-dest` HTTP request header is included by web browsers to indicate the destination of the request.
   * <p>
   * This header is used to determine if the request is a navigation request, when `sec-fetch-user` is not set to `?1`.
   * <p>
   * See
   * <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Sec-Fetch-Dest">MDN Sec-Fetch-Dest Header</a>.
   */
  static final String HEADER_SEC_FETCH_DEST = "sec-fetch-dest";

  /**
   * The `sec-fetch-mode` HTTP request header is included by web browsers to indicate the mode of the request.
   * <p>
   * This header is used to determine if the request is a navigation request, when `sec-fetch-user` is not set to `?1`.
   * <p>
   * See
   * <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Sec-Fetch-Mode">MDN Sec-Fetch-Mode Header</a>.
   */
  static final String HEADER_SEC_FETCH_MODE = "sec-fetch-mode";

  /**
   * The `sec-fetch-site` HTTP request header is included by web browsers to indicate the relationship between the
   * origin of the request and the origin of the resource being requested.
   * <p>
   * This header is used to determine if the request is a safe navigation request, when `sec-fetch-user` is not set
   * to `?1`.
   * <p>
   * See
   * <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Sec-Fetch-Site">MDN Sec-Fetch-Site Header</a>.
   */
  static final String HEADER_SEC_FETCH_SITE = "sec-fetch-site";

  /**
   * The value of the `sec-fetch-user` request header is a Structured Field Boolean.
   * <p>
   * See
   * <a href="https://www.rfc-editor.org/rfc/rfc9651.html#name-abnf">Structured Field Values for HTTP specification</a>.
   */
  static final String HEADER_SF_TRUE = "?1";

  /**
   * The possible values of the `sec-fetch-dest` request header for a navigation request.
   * <p>
   * See
   * <a href="https://fetch.spec.whatwg.org/#navigation-request">Navigation Requests Destinations</a>.
   */
  static final List<String> HEADER_SF_DEST_NAVIGATION = List.of( "document", "embed", "frame", "iframe", "object" );

  /**
   * The value of the `sec-fetch-mode` request header for a navigate request.
   * <p>
   * See
   * <a href="https://fetch.spec.whatwg.org/#concept-request-mode">Request Mode</a>.
   */
  static final String HEADER_SF_MODE_NAVIGATE = "navigate";

  /**
   * The possible values of the `sec-fetch-site` request header for a safe navigation request.
   * <p>
   * Safe navigation requests are those that are same-origin or have no origin.
   * <p>
   * See
   * <a href="https://fetch.spec.whatwg.org/#concept-request-site">Request Site</a>.
   */
  static final List<String> HEADER_SF_SITE_SAFE = List.of( "same-origin", "none" );

  @NonNull
  private final AuthenticationEntryPoint userAuthenticationEntryPoint;
  @NonNull
  private final AuthenticationEntryPoint apiAuthenticationEntryPoint;

  public UserNavigationAwareAuthenticationEntryPoint(
    @NonNull AuthenticationEntryPoint userAuthenticationEntryPoint,
    @NonNull AuthenticationEntryPoint apiAuthenticationEntryPoint ) {
    this.userAuthenticationEntryPoint = Objects.requireNonNull( userAuthenticationEntryPoint );
    this.apiAuthenticationEntryPoint = Objects.requireNonNull( apiAuthenticationEntryPoint );
  }

  @Override
  public void commence( @NonNull HttpServletRequest request,
                        @NonNull HttpServletResponse response,
                        @NonNull AuthenticationException authException )
    throws IOException, ServletException {

    boolean isUserNavigation = isUserNavigation( request );
    if ( isUserNavigation ) {
      userAuthenticationEntryPoint.commence( request, response, authException );
    } else {
      apiAuthenticationEntryPoint.commence( request, response, authException );
    }
  }

  /**
   * Determines if the request is a user navigation request based on the presence of the `sec-fetch-user` header.
   * <p>
   * If the `sec-fetch-user` header is set to `?1`, it indicates that the request was initiated by a user and has
   * user activation.
   * <p>
   * Otherwise, other navigation request headers are checked to determine if it is a safe navigation request.
   * Specifically, the following headers are considered to also indicate safe user navigation requests:
   * <ul>
   *   <li>sec-fetch-dest: document / embed / frame / iframe / object</li>
   *   <li>sec-fetch-mode: navigate</li>
   *   <li>sec-fetch-site: same-origin / none</li>
   * </ul>
   * <p>
   * These header combinations can happen due to either meta refreshes or 302-redirects:
   * <ul>
   *   <li>
   *     <pre>
   *       <META HTTP-EQUIV="refresh" CONTENT="0;URL=./Home">
   *     <pre>
   *   </li>
   *   <li>HTTP 302 redirects with a Location header.</li>
   * </ul>
   * <p>
   * In Pentaho, this can happen when the user navigates to localhost:8080.
   * The response is meta-redirected to localhost:8080/pentaho.
   * Then, the server 302-redirects to localhost:8080/pentaho/.
   * Finally, the response is meta-redirected to localhost:8080/pentaho/Home.
   *
   * @param request The HTTP request.
   * @return {@code true} if the request is a user navigation request; {@code false} otherwise.
   */
  protected boolean isUserNavigation( @NonNull HttpServletRequest request ) {
    if ( HEADER_SF_TRUE.equals( request.getHeader( HEADER_SEC_FETCH_USER ) ) ) {
      return true;
    }

    return contains( HEADER_SF_DEST_NAVIGATION, request.getHeader( HEADER_SEC_FETCH_DEST ) )
      && HEADER_SF_MODE_NAVIGATE.equals( request.getHeader( HEADER_SEC_FETCH_MODE ) )
      && contains( HEADER_SF_SITE_SAFE, request.getHeader( HEADER_SEC_FETCH_SITE ) );
  }

  protected boolean contains( @NonNull List<String> list, @Nullable String value ) {
    return value != null && list.contains( value );
  }
}
