/*!
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
 * Copyright (c) 2002-2017 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.web.http.filters;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.platform.api.engine.ISystemSettings;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.HttpMethod;
import java.io.IOException;

public class PreFlightReportingFilterTest {

  private static final String PENTAHO_API_NOTMATCHING = "/pentaho/api/notmatching";

  private static final String SYNC_URL =
    "/pentaho/api/repos/%3Apublic%3ASteel%20Wheels%3AInventory%20List%20(report).prpt/";

  private static final String ASYNC_URL =
    "/pentaho/plugin/reporting/api/jobs/5a41f746-55f4-4acf-a804-f7f5348d59bb/";

  private HttpServletRequest request;
  private HttpServletResponse response;
  private FilterChain chain;
  private static ISystemSettings systemSettings;

  @BeforeClass
  public static void setUpClass() {
    systemSettings = PentahoSystem.getSystemSettings();
  }

  @Before
  public void setUp() {
    request = Mockito.mock( HttpServletRequest.class );
    response = Mockito.mock( HttpServletResponse.class );
    chain = Mockito.mock( FilterChain.class );
    PentahoSystem.setSystemSettingsService( systemSettings );
  }


  @Test
  public void notPreFlight() throws Exception {
    Mockito.when( request.getMethod() ).thenReturn( HttpMethod.POST );
    new PreFlightReportingFilter().doFilter( request, response, chain );
    Mockito.verify( chain, Mockito.times( 1 ) ).doFilter( request, response );
  }

  @Test
  public void notMatchingUrl() throws Exception {
    Mockito.when( request.getMethod() ).thenReturn( HttpMethod.OPTIONS );
    Mockito.when( request.getRequestURI() ).thenReturn( PENTAHO_API_NOTMATCHING );
    new PreFlightReportingFilter().doFilter( request, response, chain );
    Mockito.verify( chain, Mockito.times( 1 ) ).doFilter( request, response );
  }

  @Test
  public void syncPreFlight() throws Exception {
    testMethodAndURI( HttpMethod.OPTIONS, SYNC_URL, request, response, chain );
  }


  @Test
  public void asyncPreFlight() throws Exception {
    testMethodAndURI( HttpMethod.OPTIONS, ASYNC_URL, request, response, chain );
  }

  @Test
  public void asyncPreFlightHead() throws Exception {
    testMethodAndURI( HttpMethod.HEAD, ASYNC_URL, request, response, chain );
  }

  @Test
  public void nonDefaultPattern() throws Exception {
    final ISystemSettings mockSystemSettings = Mockito.mock( ISystemSettings.class );
    Mockito.when( mockSystemSettings.getSystemSetting( Mockito.eq( "pre-flight-pattern" ), Mockito.anyString() ) )
      .thenReturn( "^.*excel.*$" );
    PentahoSystem.setSystemSettingsService( mockSystemSettings );
    testMethodAndURI( HttpMethod.OPTIONS, "/pentaho/anotherexcelurl", request, response, chain );
  }

  private void testMethodAndURI( final String method, final String uri, final HttpServletRequest request,
                                 final HttpServletResponse response, final FilterChain chain )
    throws IOException, ServletException {
    Mockito.when( request.getMethod() ).thenReturn( method );
    Mockito.when( request.getRequestURI() ).thenReturn( uri );
    new PreFlightReportingFilter().doFilter( request, response, chain );
    Mockito.verify( chain, Mockito.never() ).doFilter( Mockito.any(), Mockito.any() );
    Mockito.verify( response, Mockito.times( 1 ) ).setStatus( HttpServletResponse.SC_OK );
  }

}
