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


package org.pentaho.platform.web.http.filters;

import org.junit.Test;
import org.springframework.mock.web.MockFilterConfig;

import jakarta.servlet.ServletException;

public class CheckRefererFilterTest {

  @Test(expected = ServletException.class)
  public void blankReferrerPrefixCausesException() throws Exception {
    MockFilterConfig cfg = new MockFilterConfig();
    cfg.addInitParameter( "refererPrefix", "" );
    cfg.addInitParameter( "redirectTo", "qwerty" );

    CheckRefererFilter filter = new CheckRefererFilter();
    filter.init( cfg );
  }

  @Test(expected = ServletException.class)
  public void blankRedirectToCausesException() throws Exception {
    MockFilterConfig cfg = new MockFilterConfig();
    cfg.addInitParameter( "refererPrefix", "qwerty" );
    cfg.addInitParameter( "redirectTo", "" );

    CheckRefererFilter filter = new CheckRefererFilter();
    filter.init( cfg );
  }
}