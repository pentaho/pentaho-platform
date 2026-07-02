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
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.platform.util.RequestIdUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class RequestIdFilterTest {

  private HttpServletRequest request;
  private HttpServletResponse response;
  private FilterChain chain;

  @Before
  public void setUp() {
    request = Mockito.mock( HttpServletRequest.class );
    response = Mockito.mock( HttpServletResponse.class );
    chain = Mockito.mock( FilterChain.class );
  }

  @Test
  public void testRequestFilterNoIdProvided( ) throws ServletException, IOException {

    RequestIdFilter requestIdFilter = new RequestIdFilter( );
    requestIdFilter.doFilter( request, response, chain );

    verify( response ).setHeader( eq(  RequestIdUtil.X_REQUEST_ID ), nullable( String.class ) );
  }

  @Test
  public void testRequestFilterIdProvided() throws ServletException, IOException {

    String requestId = UUID.randomUUID().toString();
    when( request.getHeader( RequestIdUtil.X_REQUEST_ID ) ).thenReturn( requestId );
    RequestIdFilter requestIdFilter = new RequestIdFilter();
    requestIdFilter.doFilter( request, response, chain );

    verify( response ).setHeader( eq( RequestIdUtil.X_REQUEST_ID ), eq( requestId ) );
  }
}
