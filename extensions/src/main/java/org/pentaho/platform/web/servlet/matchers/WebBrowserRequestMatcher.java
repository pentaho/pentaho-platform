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

import javax.servlet.http.HttpServletRequest;

/**
 * The {@code WebBrowserRequestMatcher} determines if a request is made by a web browser.
 * <p>
 * The matcher does not distinguish whether the request is a user navigation request or not. The only criterion is
 * whether the request is made by a web browser. To test if a request is a user navigation request, the
 * {@link UserNavigationRequestMatcher} can be used.
 * <p>
 * The implementation checks the presence of the `sec-fetch-dest` HTTP request header, which is web-browser-specific.
 * <p>
 * In Pentaho, this matcher is used to select an appropriate authentication failure response, depending on whether the
 * request is made by a web browser or a tool.
 */
public class WebBrowserRequestMatcher implements RequestMatcher {
  /**
   * The `sec-fetch-dest` HTTP request header is included by web browsers to indicate the destination of the request.
   * <p>
   * See
   * <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Sec-Fetch-Dest">MDN Sec-Fetch-Dest Header</a>.
   */
  static final String HEADER_SEC_FETCH_DEST = "sec-fetch-dest";

  @Override
  public boolean matches( @NonNull HttpServletRequest request ) {
    return request.getHeader( HEADER_SEC_FETCH_DEST ) != null;
  }
}
