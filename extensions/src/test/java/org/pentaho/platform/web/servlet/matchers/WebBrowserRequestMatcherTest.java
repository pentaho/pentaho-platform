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

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WebBrowserRequestMatcherTest {

  @Test
  public void test_True_WhenSecFetchDestHeaderIsPresent() {
    HttpServletRequest request = mock( HttpServletRequest.class );
    when( request.getHeader( WebBrowserRequestMatcher.HEADER_SEC_FETCH_DEST ) ).thenReturn( "document" );

    WebBrowserRequestMatcher matcher = new WebBrowserRequestMatcher();
    assertTrue( matcher.matches( request ) );
  }

  @Test
  public void test_False_WhenSecFetchDestHeaderIsAbsent() {
    HttpServletRequest request = mock( HttpServletRequest.class );
    when( request.getHeader( WebBrowserRequestMatcher.HEADER_SEC_FETCH_DEST ) ).thenReturn( null );

    WebBrowserRequestMatcher matcher = new WebBrowserRequestMatcher();
    assertFalse( matcher.matches( request ) );
  }
}
