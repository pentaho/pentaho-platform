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
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class PreventBruteForceAuthenticationFailureListenerTest {

  private ILoginAttemptService mockLoginAttemptService;
  private AuthenticationFailureBadCredentialsEvent mockAuthenticationFailureBadCredentialsEvent;
  private Authentication mockAuthentication;
  private WebAuthenticationDetails mockWebAuthenticationDetails;
  private String ip;

  @Before
  public void setUp() throws Exception {
    mockLoginAttemptService = mock( LoginAttemptService.class );
    mockAuthenticationFailureBadCredentialsEvent = mock( AuthenticationFailureBadCredentialsEvent.class );
    mockAuthentication = mock( Authentication.class );
    mockWebAuthenticationDetails = mock( WebAuthenticationDetails.class );

    ip = "192.168.0.25";
    when( mockAuthenticationFailureBadCredentialsEvent.getAuthentication() ).thenReturn( mockAuthentication );
    when( mockAuthentication.getDetails() ).thenReturn( mockWebAuthenticationDetails );
    when( mockWebAuthenticationDetails.getRemoteAddress() ).thenReturn( ip );
  }

  @Test
  public void onApplicationEvent() {
    PreventBruteForceAuthenticationFailureListener listener =
      new PreventBruteForceAuthenticationFailureListener( mockLoginAttemptService );

    listener.onApplicationEvent( mockAuthenticationFailureBadCredentialsEvent );
    verify( mockLoginAttemptService, times( 1 ) ).loginFailed( ip );
  }
}
