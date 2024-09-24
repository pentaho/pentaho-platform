/*!
 *
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

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