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
import org.springframework.security.web.util.matcher.RequestMatcher;

import jakarta.servlet.http.HttpServletRequest;

/**
 * The {@code WebBrowserSecFetchRequestMatcher} determines if a request is made by a web browser, based on the presence
 * of the `sec-fetch-dest` HTTP request header.
 * <p>
 * The matcher does not distinguish whether the request is a user navigation request or not. The only criterion is
 * whether the request is made by a web browser. To test if a request is a user navigation request, the
 * {@link UserNavigationSecFetchRequestMatcher} can be used.
 * <p>
 * The implementation checks the presence of the `sec-fetch-dest` HTTP request header, which is a header specific to
 * web-browsers.
 * <p>
 * Unfortunately, the {@code sec-fetch-dest} header is not always present in requests a web browser considers insecure.
 * Examples of such requests include the server being accessed via an IP address, of public or private range, without
 * HTTPS, or without a valid SSL certificate. For the case of private IP addresses, there are also special protections
 * as determined by the
 * <a href="https://wicg.github.io/private-network-access">Private Network Access (PNA) specification</a>.
 * For these cases, consider using the {@link WebBrowserUserAgentRequestMatcher} as a fallback matcher.
 * <p>
 * In Pentaho, this matcher is used to select an appropriate authentication failure response, depending on whether the
 * request is made by a web browser or a tool.
 */
public class WebBrowserSecFetchRequestMatcher implements RequestMatcher {
  /**
   * The `sec-fetch-dest` HTTP request header is included by web browsers to indicate the destination of the request.
   * <p>
   * See
   * <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Sec-Fetch-Dest">MDN Sec-Fetch-Dest
   * Header</a>.
   */
  static final String HEADER_SEC_FETCH_DEST = "sec-fetch-dest";

  @Override
  public boolean matches( @NonNull HttpServletRequest request ) {
    return request.getHeader( HEADER_SEC_FETCH_DEST ) != null;
  }
}
