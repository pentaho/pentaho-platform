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

import org.pentaho.platform.api.security.ILoginAttemptService;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

public class PreventBruteForceAuthenticationFailureListener implements
  ApplicationListener<AuthenticationFailureBadCredentialsEvent> {

  private ILoginAttemptService loginAttemptService;

  public PreventBruteForceAuthenticationFailureListener( ILoginAttemptService loginAttemptService ) {
    super();
    this.loginAttemptService = loginAttemptService;
  }


  public void onApplicationEvent( AuthenticationFailureBadCredentialsEvent e ) {
    WebAuthenticationDetails auth = (WebAuthenticationDetails) e.getAuthentication().getDetails();
    loginAttemptService.loginFailed( auth.getRemoteAddress() );
  }

}
