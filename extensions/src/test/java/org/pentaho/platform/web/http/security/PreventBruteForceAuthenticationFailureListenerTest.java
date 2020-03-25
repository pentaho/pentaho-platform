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
 * Copyright (c) 2020 Hitachi Vantara. All rights reserved.
 *
 */

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
