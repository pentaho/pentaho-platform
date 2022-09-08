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
 * Copyright (c) 2022 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.web.http.filters;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.http.HttpSession;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CustomSessionTimeoutFilterTest {

  public static final int A_CUSTOM_SESSION_TIMEOUT = 1234567;

  public static final String A_CUSTOM_SESSION_TIMEOUT_STR = "" + A_CUSTOM_SESSION_TIMEOUT;

  private MockHttpServletRequest request;
  private CustomSessionTimeoutFilter filter;

  @Before
  public void setUp() throws Exception {
    request = new MockHttpServletRequest();
  }


  @Test
  public void loadValidTimeout() throws Exception {
    MockFilterConfig cfg = new MockFilterConfig();
    cfg.addInitParameter( CustomSessionTimeoutFilter.CUSTOM_SESSION_TIMEOUT_PROPERTY_NAME,
      A_CUSTOM_SESSION_TIMEOUT_STR );

    filter = new CustomSessionTimeoutFilter();
    filter.init( cfg );

    filter.doFilter( request, new MockHttpServletResponse(), new MockFilterChain() );

    HttpSession session = request.getSession();
    assertNotNull( session );
    assertEquals( A_CUSTOM_SESSION_TIMEOUT, session.getMaxInactiveInterval() );
  }

  @Test
  public void loadInvalidTimeout() throws Exception {
    MockFilterConfig cfg = new MockFilterConfig();

    // This is clearly not a number
    cfg.addInitParameter( CustomSessionTimeoutFilter.CUSTOM_SESSION_TIMEOUT_PROPERTY_NAME,
      CustomSessionTimeoutFilter.CUSTOM_SESSION_TIMEOUT_PROPERTY_NAME );

    filter = new CustomSessionTimeoutFilter();
    filter.init( cfg );

    filter.doFilter( request, new MockHttpServletResponse(), new MockFilterChain() );

    HttpSession session = request.getSession();
    assertNotNull( session );
    assertEquals( CustomSessionTimeoutFilter.CUSTOM_SESSION_TIMEOUT_DEFAULT_VALUE, session.getMaxInactiveInterval() );
  }
}
