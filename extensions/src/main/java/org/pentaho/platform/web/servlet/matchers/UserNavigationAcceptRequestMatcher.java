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
import org.apache.commons.lang.StringUtils;
import org.springframework.security.web.util.matcher.RequestMatcher;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * The {@code UserNavigationAcceptRequestMatcher} determines if a request can be considered a user navigation request,
 * based on the presence and value of the `accept` HTTP request header.
 * <p>
 * By default, if the `accept` header contains one of the values `text/html` or `application/xhtml+xml`,
 * it indicates that the request likely targets a web browser's document, frame, iframe, or object tags. These requests
 * typically correspond to user navigation actions.
 */
public class UserNavigationAcceptRequestMatcher implements RequestMatcher {
  /**
   * The `accept` HTTP request header is included by web browsers to indicate the mime types that the response may use.
   * <p>
   * See
   * <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Accept">MDN Accept Header</a>.
   */
  static final String HEADER_ACCEPT = "accept";

  /**
   * The patterns of the `accept` request header that are considered navigation requests.
   */
  static final List<String> DEFAULT_HEADER_ACCEPT_NAVIGATION_PATTERNS = List.of(
    "\\btext/html\\b",
    "\\bapplication/xhtml\\+xml\\b"
  );

  @NonNull
  private final Pattern acceptNavigationPattern;

  public UserNavigationAcceptRequestMatcher() {
    this( DEFAULT_HEADER_ACCEPT_NAVIGATION_PATTERNS );
  }

  public UserNavigationAcceptRequestMatcher( @NonNull List<String> acceptNavigationPatterns ) {
    super();

    Objects.requireNonNull( acceptNavigationPatterns );

    this.acceptNavigationPattern = Pattern.compile(
      String.join( "|", acceptNavigationPatterns ),
      Pattern.CASE_INSENSITIVE );
  }

  protected boolean isNavigationContent( String accept ) {
    if ( StringUtils.isEmpty( accept ) ) {
      return false;
    }

    return acceptNavigationPattern.matcher( accept ).find();
  }

  @Override
  public boolean matches( @NonNull HttpServletRequest request ) {
    return isNavigationContent( request.getHeader( HEADER_ACCEPT ) );
  }
}
