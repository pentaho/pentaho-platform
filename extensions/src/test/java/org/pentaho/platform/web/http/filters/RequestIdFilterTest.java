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
 * Copyright (c) 2002-2021 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.web.http.filters;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.platform.util.RequestIdUtil;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
