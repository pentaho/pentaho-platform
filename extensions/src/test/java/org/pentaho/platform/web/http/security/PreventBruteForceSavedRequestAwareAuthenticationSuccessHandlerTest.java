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


package org.pentaho.platform.web.http.security;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.security.ILoginAttemptService;
import org.pentaho.platform.engine.security.LoginAttemptService;
import org.springframework.security.core.Authentication;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PreventBruteForceSavedRequestAwareAuthenticationSuccessHandlerTest {

  private ILoginAttemptService mockLoginAttemptService;
  private HttpServletRequest mockRequest;
  private HttpServletResponse mockResponse;
  private Authentication mockAuthentication;

  private String ip;

  @Before
  public void setUp() throws Exception {
    mockLoginAttemptService = mock( LoginAttemptService.class );
    mockRequest = mock( HttpServletRequest.class );
    mockResponse = mock( HttpServletResponse.class );
    mockAuthentication = mock( Authentication.class );

    ip = "192.168.0.25";
    when( mockRequest.getHeader( "X-Forwarded-For" ) ).thenReturn( null );
    when( mockRequest.getRemoteAddr() ).thenReturn( ip );
  }

  @Test
  public void testOnAuthenticationSuccess() throws ServletException, IOException {
    PreventBruteForceSavedRequestAwareAuthenticationSuccessHandler handler =
      new PreventBruteForceSavedRequestAwareAuthenticationSuccessHandler( mockLoginAttemptService );
    handler.onAuthenticationSuccess( mockRequest, mockResponse, mockAuthentication );
    verify( mockLoginAttemptService, times( 1 ) ).loginSucceeded( ip );
  }
}
