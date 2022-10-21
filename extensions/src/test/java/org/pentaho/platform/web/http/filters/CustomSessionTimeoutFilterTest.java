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

import com.mockrunner.mock.web.MockHttpSession;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;


public class CustomSessionTimeoutFilterTest {

  private MockHttpServletRequest request = null;

  private HttpSession httpSession = null;

  private static final int BOGUS_DEFAULT_TIMEOUT_IN_MINUTES = 12;
  private static final int BOGUS_DEFAULT_TIMEOUT_IN_SECONDS = 60 * BOGUS_DEFAULT_TIMEOUT_IN_MINUTES;

  @Before
  public void setUp() throws Exception {
    // Set the necessary initial values for the context.
    // Currently, only 'Session Timeout' value is needed
    ServletContext servletContext = mock( ServletContext.class );
    doReturn( BOGUS_DEFAULT_TIMEOUT_IN_MINUTES ).when( servletContext ).getSessionTimeout();

    request = new MockHttpServletRequest( servletContext );

    // Inject an HttpSession so that we can freely interact with it.
    httpSession = spy( new MockHttpSession() );
    request.setSession( httpSession );

    // So that logging code is tested (improves coverage)
    Configurator.setAllLevels( CustomSessionTimeoutFilter.class.getCanonicalName(), Level.ALL );
  }

  @Test
  public void test_SetGetCustomSessionTimeout() {
    CustomSessionTimeoutFilter customSessionTimeoutFilter = new CustomSessionTimeoutFilter();
    int aCustomTimeout = 12345;

    // Initially there is a default value set
    assertEquals( CustomSessionTimeoutFilter.CUSTOM_SESSION_TIMEOUT_DEFAULT_VALUE,
      customSessionTimeoutFilter.getCustomSessionTimeout() );

    customSessionTimeoutFilter.setCustomSessionTimeout( aCustomTimeout );
    assertEquals( aCustomTimeout, customSessionTimeoutFilter.getCustomSessionTimeout() );
  }

  @Test
  public void test_PositiveTimeout() throws Exception {
    execute_test( 12345 );
  }

  @Test
  public void test_ZeroTimeout() throws Exception {
    execute_test( 0 );
  }

  @Test
  public void test_NegativeTimeout() throws Exception {
    execute_test( -10 );
  }

  private void execute_test( int timeoutValue ) throws Exception {
    CustomSessionTimeoutFilter customSessionTimeoutFilter = spy( new CustomSessionTimeoutFilter() );

    customSessionTimeoutFilter.setCustomSessionTimeout( timeoutValue );
    customSessionTimeoutFilter.afterPropertiesSet();

    customSessionTimeoutFilter.doFilter( request, new MockHttpServletResponse(), new MockFilterChain() );

    // Did it ever set the intended new timeout value?
    verify( httpSession ).setMaxInactiveInterval( timeoutValue );
    // Did it ever set the Servlet's default timeout value?
    verify( httpSession ).setMaxInactiveInterval( BOGUS_DEFAULT_TIMEOUT_IN_SECONDS );

    // Check if, after executing this filter, the timeout value returned to the Servlet's default
    HttpSession session = request.getSession( false );
    // Just guaranteeing that we're using the right instance
    assertNotNull( session );
    assertEquals( httpSession, session );
    // Check the value
    assertEquals( BOGUS_DEFAULT_TIMEOUT_IN_SECONDS, session.getMaxInactiveInterval() );
  }
}
