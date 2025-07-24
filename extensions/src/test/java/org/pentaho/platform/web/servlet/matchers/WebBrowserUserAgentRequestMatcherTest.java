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

import org.junit.Before;
import org.junit.Test;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WebBrowserUserAgentRequestMatcherTest {

  private WebBrowserUserAgentRequestMatcher matcher;

  @Before
  public void setUp() {
    matcher = new WebBrowserUserAgentRequestMatcher();
  }

  private void assertUserAgentMatch( String userAgent, boolean shouldMatch ) {
    HttpServletRequest request = mock( HttpServletRequest.class );
    when( request.getHeader( "user-agent" ) ).thenReturn( userAgent );
    if ( shouldMatch ) {
      assertTrue( matcher.matches( request ) );
    } else {
      assertFalse( matcher.matches( request ) );
    }
  }

  // region Normal Browsers
  @Test
  public void testMatchesWithChromeUserAgent() {
    assertUserAgentMatch(
      "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0 Safari/537.36", true );
  }

  @Test
  public void testMatchesWithFirefoxUserAgent() {
    assertUserAgentMatch(

      "Mozilla/5.0 (Windows NT 10.0; rv:109.0) Gecko/20100101 Firefox/109.0", true );
  }

  @Test
  public void testMatchesWithEdgeUserAgent() {
    assertUserAgentMatch(
      "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) "
        + "Chrome/120.0.6099.71 Safari/537.36 Edg/120.0.6094.50",
      true );
  }

  @Test
  public void testMatchesWithSafariUserAgent() {
    assertUserAgentMatch(
      "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.1 "
        + "Safari/605.1.15",
      true );
  }
  // endregion

  // region Headless Browsers
  @Test
  public void testMatchesWithHeadlessChromeUserAgent() {
    assertUserAgentMatch(
      "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) "
        + "HeadlessChrome/120.0.6099.71 Safari/537.36",
      true );
  }

  @Test
  public void testMatchesWithHeadlessFirefoxUserAgent() {
    assertUserAgentMatch(
      "Mozilla/5.0 (X11; Linux x86_64; rv:109.0) Gecko/20100101 Firefox/109.0 Headless",
      true );
  }
  // endregion

  // region Non-browser User Agents
  @Test
  public void testDoesNotMatchWithCurlUserAgent() {
    assertUserAgentMatch( "curl/7.68.0", false );
  }

  @Test
  public void testDoesNotMatchWithWgetUserAgent() {
    assertUserAgentMatch( "Wget/1.20.3 (linux-gnu)", false );
  }

  @Test
  public void testDoesNotMatchWithBotUserAgent() {
    assertUserAgentMatch( "Googlebot/2.1 (+http://www.google.com/bot.html)", false );
  }
  // endregion

  @Test
  public void testDoesNotMatchWithEmptyUserAgent() {
    assertUserAgentMatch( "", false );
  }

  @Test
  public void testDoesNotMatchWithNullUserAgent() {
    assertUserAgentMatch( null, false );
  }

  @Test
  public void testMatchesWithCustomBrowserPatterns() {
    List<String> customBrowserPatterns = List.of( "\\bmybrowser/\\b" );
    List<String> customNonBrowserPatterns = List.of( "\\bnotabrowser/\\b" );
    WebBrowserUserAgentRequestMatcher customMatcher =
      new WebBrowserUserAgentRequestMatcher( customBrowserPatterns, customNonBrowserPatterns );

    HttpServletRequest request = mock( HttpServletRequest.class );
    when( request.getHeader( "user-agent" ) ).thenReturn( "MyBrowser/1.0" );
    assertTrue( customMatcher.matches( request ) );
  }

  @Test
  public void testDoesNotMatchWithCustomNonBrowserPattern() {
    List<String> customBrowserPatterns = List.of( "\\bmybrowser/\\b" );
    List<String> customNonBrowserPatterns = List.of( "\\bnotabrowser/\\b" );
    WebBrowserUserAgentRequestMatcher customMatcher =
      new WebBrowserUserAgentRequestMatcher( customBrowserPatterns, customNonBrowserPatterns );

    HttpServletRequest request = mock( HttpServletRequest.class );
    when( request.getHeader( "user-agent" ) ).thenReturn( "MyBrowser/1.0 NotABrowser/2.0" );
    assertFalse( customMatcher.matches( request ) );
  }
}
