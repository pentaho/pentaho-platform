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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.pentaho.platform.web.servlet.matchers.UserNavigationSecFetchRequestMatcher.HEADER_SEC_FETCH_DEST;
import static org.pentaho.platform.web.servlet.matchers.UserNavigationSecFetchRequestMatcher.HEADER_SEC_FETCH_MODE;
import static org.pentaho.platform.web.servlet.matchers.UserNavigationSecFetchRequestMatcher.HEADER_SEC_FETCH_SITE;
import static org.pentaho.platform.web.servlet.matchers.UserNavigationSecFetchRequestMatcher.HEADER_SEC_FETCH_USER;
import static org.pentaho.platform.web.servlet.matchers.UserNavigationSecFetchRequestMatcher.HEADER_SF_TRUE;

public class UserNavigationSecFetchRequestMatcherTest {

  private static final String HEADER_SF_FALSE = "?0";

  private HttpServletRequest request;

  @Before
  public void setUp() {
    request = mock( HttpServletRequest.class );
  }

  private void expectMatch(
    String secFetchUser,
    String secFetchDest,
    String secFetchMode,
    String secFetchSite,
    boolean expectMatches
  ) {
    when( request.getHeader( HEADER_SEC_FETCH_USER ) ).thenReturn( secFetchUser );
    when( request.getHeader( HEADER_SEC_FETCH_DEST ) ).thenReturn( secFetchDest );
    when( request.getHeader( HEADER_SEC_FETCH_MODE ) ).thenReturn( secFetchMode );
    when( request.getHeader( HEADER_SEC_FETCH_SITE ) ).thenReturn( secFetchSite );

    var requestMatcher = new UserNavigationSecFetchRequestMatcher();

    assertEquals( expectMatches, requestMatcher.matches( request ) );
  }

  @Test
  public void test_False_WhenNoSecFetchHeadersArePresent() {
    expectMatch( null, null, null, null, false );
  }

  @Test
  public void test_False_WhenSecFetchUserHeaderIsPresentWithFalseValueAndNoOtherHeadersPresent() {
    expectMatch( HEADER_SF_FALSE, null, null, null, false );
  }

  @Test
  public void test_True_WhenSecFetchUserHeaderIsPresentWithTrueValue() {
    expectMatch( HEADER_SF_TRUE, null, null, null, true );
  }

  @Test
  public void test_True_OtherUserNavigationCasesWhenSecFetchUserIsNotPresent() {
    expectMatch( null, "document", "navigate", "same-origin", true );
    expectMatch( null, "iframe", "navigate", "none", true );
  }

  @Test
  public void test_False_OtherNotUserNavigationCasesWhenSecFetchUserIsNotPresent() {
    expectMatch( null, "image", "navigate", "same-origin", false );
    expectMatch( null, "document", null, "same-origin", false );
    expectMatch( null, "document", "cors", "same-origin", false );
    expectMatch( null, "document", "navigate", "cross-site", false );
  }
}
