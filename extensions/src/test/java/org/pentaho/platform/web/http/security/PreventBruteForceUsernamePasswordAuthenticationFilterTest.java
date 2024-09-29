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
 * Copyright (c) 2020-2024 Hitachi Vantara. All rights reserved.
 *
 */
package org.pentaho.platform.web.http.security;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.security.ILoginAttemptService;
import org.pentaho.platform.engine.security.LoginAttemptService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class PreventBruteForceUsernamePasswordAuthenticationFilterTest {

  private ILoginAttemptService mockLoginAttemptService;
  private HttpServletRequest mockRequest;
  private HttpServletResponse mockResponse;
  private AuthenticationManager mockAuthenticationManager;
  private Authentication mockAuthentication;
  private String ip;

  @Before
  public void setUp() throws Exception {
    mockLoginAttemptService = mock( LoginAttemptService.class );
    mockRequest = mock( HttpServletRequest.class );
    mockResponse = mock( HttpServletResponse.class );
    mockAuthenticationManager = mock( AuthenticationManager.class );
    mockAuthentication = mock( Authentication.class );
    ip = "192.168.0.25";
    when( mockRequest.getHeader( "X-Forwarded-For" ) ).thenReturn( null );
    when( mockRequest.getRemoteAddr() ).thenReturn( ip );
    when( mockRequest.getMethod() ).thenReturn( "POST" );
  }


  @Test
  public void testAttemptAuthenticationWithoutBlock() {
    PreventBruteForceUsernamePasswordAuthenticationFilter authenticationFilter =
      spy( new PreventBruteForceUsernamePasswordAuthenticationFilter( mockLoginAttemptService ) );

    when( mockLoginAttemptService.isBlocked( ip ) ).thenReturn( false );
    when( mockAuthenticationManager.authenticate( any() ) ).thenReturn( mockAuthentication );

    authenticationFilter.setAuthenticationManager( mockAuthenticationManager );

    authenticationFilter.attemptAuthentication( mockRequest, mockResponse );
    verify( mockLoginAttemptService, times( 1 ) ).isBlocked( ip );

  }

  @Test( expected = PreventBruteForceException.class )
  public void testAttemptAuthenticationWithBlock() {
    PreventBruteForceUsernamePasswordAuthenticationFilter authenticationFilter =
      spy( new PreventBruteForceUsernamePasswordAuthenticationFilter( mockLoginAttemptService ) );

    when( mockLoginAttemptService.isBlocked( ip ) ).thenReturn( true );
    authenticationFilter.attemptAuthentication( mockRequest, mockResponse );
  }
}
