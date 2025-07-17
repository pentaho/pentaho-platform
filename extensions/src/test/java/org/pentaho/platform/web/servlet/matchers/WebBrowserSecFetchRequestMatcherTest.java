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

import org.junit.Test;

import jakarta.servlet.http.HttpServletRequest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WebBrowserSecFetchRequestMatcherTest {

  @Test
  public void test_True_WhenSecFetchDestHeaderIsPresent() {
    HttpServletRequest request = mock( HttpServletRequest.class );
    when( request.getHeader( WebBrowserSecFetchRequestMatcher.HEADER_SEC_FETCH_DEST ) ).thenReturn( "document" );

    WebBrowserSecFetchRequestMatcher matcher = new WebBrowserSecFetchRequestMatcher();
    assertTrue( matcher.matches( request ) );
  }

  @Test
  public void test_False_WhenSecFetchDestHeaderIsAbsent() {
    HttpServletRequest request = mock( HttpServletRequest.class );
    when( request.getHeader( WebBrowserSecFetchRequestMatcher.HEADER_SEC_FETCH_DEST ) ).thenReturn( null );

    WebBrowserSecFetchRequestMatcher matcher = new WebBrowserSecFetchRequestMatcher();
    assertFalse( matcher.matches( request ) );
  }
}
