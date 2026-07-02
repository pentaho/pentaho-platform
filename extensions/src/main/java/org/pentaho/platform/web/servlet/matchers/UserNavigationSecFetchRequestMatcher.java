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

package org.pentaho.platform.web.servlet.matchers;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.springframework.security.web.util.matcher.RequestMatcher;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * The {@code UserNavigationSecFetchRequestMatcher} determines if a request can be considered a user navigation request,
 * based on the presence of the `sec-fetch-user` HTTP request header, or on other related headers, such as
 * `sec-fetch-dest`, `sec-fetch-mode` and `sec-fetch-site`.
 * <p>
 * If the `sec-fetch-user` header is set to `?1`, it indicates that the request was initiated by a user and has
 * user activation, and is considered a user navigation request.
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
 * <p>
 * Unfortunately, the {@code sec-fetch-*} headers are not always present in requests a web browser considers insecure.
 * Examples of such requests include the server being accessed via an IP address, of public or private range, without
 * HTTPS, or without a valid SSL certificate. For the case of private IP addresses, there are also special protections
 * as determined by the
 * <a href="https://wicg.github.io/private-network-access">Private Network Access (PNA) specification</a>.
 * For these cases, consider using the {@link UserNavigationAcceptRequestMatcher} as a fallback matcher.
 */
public class UserNavigationSecFetchRequestMatcher implements RequestMatcher {
  /**
   * The `sec-fetch-user` HTTP request header is included by web browsers to indicate that a request was initiated by
   * a <i>user navigation</i> and having a user activation state.
   * <p>
   * See
   * <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Sec-Fetch-User">MDN Sec-Fetch-User
   * Header</a>.
   */
  static final String HEADER_SEC_FETCH_USER = "sec-fetch-user";

  /**
   * The `sec-fetch-dest` HTTP request header is included by web browsers to indicate the destination of the request.
   * <p>
   * This header is used to determine if the request is a navigation request, when `sec-fetch-user` is not set to `?1`.
   * <p>
   * See
   * <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Sec-Fetch-Dest">MDN Sec-Fetch-Dest
   * Header</a>.
   */
  static final String HEADER_SEC_FETCH_DEST = "sec-fetch-dest";

  /**
   * The `sec-fetch-mode` HTTP request header is included by web browsers to indicate the mode of the request.
   * <p>
   * This header is used to determine if the request is a navigation request, when `sec-fetch-user` is not set to `?1`.
   * <p>
   * See
   * <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Sec-Fetch-Mode">MDN Sec-Fetch-Mode
   * Header</a>.
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
   * <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Sec-Fetch-Site">MDN Sec-Fetch-Site
   * Header</a>.
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

  @Override
  public boolean matches( @NonNull HttpServletRequest request ) {
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
