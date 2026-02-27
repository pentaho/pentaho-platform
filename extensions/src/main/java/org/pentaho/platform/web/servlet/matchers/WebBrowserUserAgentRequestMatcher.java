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
 * The {@code WebBrowserUserAgentRequestMatcher} determines if a request is made by a web browser based on the
 * presence and value of the `User-Agent` HTTP request header.
 * <p>
 * The matcher does not distinguish whether the request is a user navigation request or not. The only criterion is
 * whether the request is made by a web browser. To test if a request is a user navigation request, the corresponding
 * weak matcher, {@link UserNavigationAcceptRequestMatcher} can be used.
 * <p>
 * This matcher immediately returns `false` if the `sec-fetch-dest` is present in the request, as in that case, the
 * "weaker" logic should not be used. Instead, this matcher should be combined
 * <p>
 * In Pentaho, this matcher is used to select an appropriate authentication failure response, depending on whether the
 * request is made by a web browser or a tool.
 */
public class WebBrowserUserAgentRequestMatcher implements RequestMatcher {
  /**
   * The `user-agent` HTTP request header is included by web browsers to indicate the type of web browser.
   * <p>
   * See
   * <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/User-Agent">MDN User-Agent Header</a>.
   */
  static final String HEADER_USER_AGENT = "user-agent";

  /**
   * Default patterns for matching web browsers in the `User-Agent` header.
   * This includes common web browsers and headless browsers.
   */
  private static final List<String> DEFAULT_BROWSER_PATTERNS = List.of(
    "\\bchrome/\\b", "\\bheadlesschrome/\\b", "\\bfirefox/\\b", "\\bsafari/\\b", "\\bedge/\\b",
    "\\btrident/\\b", "\\bmsie/\\b", "\\bopera/\\b", "\\bopr/\\b"
  );

  /**
   * Default patterns for matching non-browser user agents in the `User-Agent` header.
   * This includes common tools and libraries that are not web browsers.
   * This exclusion list is important because non-browser user agents sometimes preserve browser tokens.
   */
  private static final List<String> DEFAULT_NON_BROWSER_PATTERNS = List.of(
    "\\bcurl/\\b", "\\bwget/\\b", "\\bpostmanruntime/\\b", "\\bpython-requests/\\b",
    "\\bbot\\b", "\\bcrawler\\b", "\\bspider\\b", "\\bjava/\\b", "\\bphp/\\b", "\\bruby/\\b"
  );

  @NonNull
  private final Pattern browserPattern;
  @NonNull
  private final Pattern nonBrowserPattern;

  public WebBrowserUserAgentRequestMatcher() {
    this( DEFAULT_BROWSER_PATTERNS, DEFAULT_NON_BROWSER_PATTERNS );
  }

  public WebBrowserUserAgentRequestMatcher( @NonNull List<String> browserPatterns,
                                            @NonNull List<String> nonBrowserPatterns ) {

    Objects.requireNonNull( browserPatterns );
    Objects.requireNonNull( nonBrowserPatterns );

    // Compile browser tokens into a single regex (e.g., \bchrome/\b|\bheadlesschrome/\b|...)
    this.browserPattern = Pattern.compile(
      String.join( "|", browserPatterns ),
      Pattern.CASE_INSENSITIVE
    );

    // Compile non-browser tokens into a single regex (e.g., \bcurl/\b|\bbot\b|...)
    this.nonBrowserPattern = Pattern.compile(
      String.join( "|", nonBrowserPatterns ),
      Pattern.CASE_INSENSITIVE
    );
  }

  protected boolean isBrowser( String userAgent ) {
    if ( StringUtils.isEmpty( userAgent  ) ) {
      return false;
    }

    return browserPattern.matcher( userAgent ).find()
      && !nonBrowserPattern.matcher( userAgent ).find();
  }

  @Override
  public boolean matches( @NonNull HttpServletRequest request ) {
    return isBrowser( request.getHeader( HEADER_USER_AGENT ) );
  }
}
