package org.pentaho.platform.web.http.filters;

import org.junit.Test;
import org.springframework.mock.web.MockFilterConfig;

import javax.servlet.ServletException;

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