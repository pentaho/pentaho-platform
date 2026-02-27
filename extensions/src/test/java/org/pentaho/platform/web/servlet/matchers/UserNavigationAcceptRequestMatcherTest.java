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

public class UserNavigationAcceptRequestMatcherTest {

  private UserNavigationAcceptRequestMatcher matcher;

  @Before
  public void setUp() {
    matcher = new UserNavigationAcceptRequestMatcher();
  }

  private void assertAcceptHeaderMatch( String acceptHeader, boolean shouldMatch ) {
    HttpServletRequest request = mock( HttpServletRequest.class );
    when( request.getHeader( "accept" ) ).thenReturn( acceptHeader );
    if ( shouldMatch ) {
      assertTrue( matcher.matches( request ) );
    } else {
      assertFalse( matcher.matches( request ) );
    }
  }

  @Test
  public void testMatchesWithTextHtml() {
    assertAcceptHeaderMatch( "text/html", true );
  }

  @Test
  public void testMatchesWithApplicationXhtmlXml() {
    assertAcceptHeaderMatch( "application/xhtml+xml", true );
  }

  @Test
  public void testMatchesWithMultipleAcceptsIncludingHtml() {
    assertAcceptHeaderMatch( "application/xml,text/html;q=0.9,image/webp,*/*;q=0.8", true );
  }

  @Test
  public void testMatchesWithMultipleAcceptsIncludingXhtml() {
    assertAcceptHeaderMatch( "application/xml,application/xhtml+xml;q=0.9,image/webp,*/*;q=0.8", true );
  }

  @Test
  public void testDoesNotMatchWithJson() {
    assertAcceptHeaderMatch( "application/json", false );
  }

  @Test
  public void testDoesNotMatchWithXmlOnly() {
    assertAcceptHeaderMatch( "application/xml", false );
  }

  @Test
  public void testDoesNotMatchWithEmptyHeader() {
    assertAcceptHeaderMatch( "", false );
  }

  @Test
  public void testDoesNotMatchWithNullHeader() {
    assertAcceptHeaderMatch( null, false );
  }

  @Test
  public void testMatchesWithCustomPattern() {
    List<String> customPatterns = List.of( "\\bapplication/custom\\b" );
    UserNavigationAcceptRequestMatcher customMatcher = new UserNavigationAcceptRequestMatcher( customPatterns );
    HttpServletRequest request = mock( HttpServletRequest.class );
    when( request.getHeader( "accept" ) ).thenReturn( "application/custom" );
    assertTrue( customMatcher.matches( request ) );
  }

  @Test
  public void testDoesNotMatchWithCustomPattern() {
    List<String> customPatterns = List.of( "\\bapplication/custom\\b" );
    UserNavigationAcceptRequestMatcher customMatcher = new UserNavigationAcceptRequestMatcher( customPatterns );
    HttpServletRequest request = mock( HttpServletRequest.class );
    when( request.getHeader( "accept" ) ).thenReturn( "application/other" );
    assertFalse( customMatcher.matches( request ) );
  }
}
