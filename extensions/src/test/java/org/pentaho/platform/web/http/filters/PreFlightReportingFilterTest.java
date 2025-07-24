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

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.platform.api.engine.ISystemSettings;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.HttpMethod;
import java.io.IOException;

public class PreFlightReportingFilterTest {


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
  public void preFlightOptions() throws Exception {
    testMethodAndURI( HttpMethod.OPTIONS, request, response, chain );
  }

  @Test
  public void preFlightHead() throws Exception {
    testMethodAndURI( HttpMethod.HEAD, request, response, chain );
  }

  private void testMethodAndURI( final String method, final HttpServletRequest request,
                                 final HttpServletResponse response, final FilterChain chain )
    throws IOException, ServletException {
    Mockito.when( request.getMethod() ).thenReturn( method );
    new PreFlightReportingFilter().doFilter( request, response, chain );
    Mockito.verify( chain, Mockito.never() ).doFilter( Mockito.any(), Mockito.any() );
    Mockito.verify( response, Mockito.times( 1 ) ).sendError( HttpServletResponse.SC_METHOD_NOT_ALLOWED );
  }

}
