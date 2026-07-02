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
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class PreventBruteForceSavedRequestAwareAuthenticationSuccessHandler extends
  SavedRequestAwareAuthenticationSuccessHandler {

  private ILoginAttemptService loginAttemptService;

  public PreventBruteForceSavedRequestAwareAuthenticationSuccessHandler( ILoginAttemptService loginAttemptService ) {
    super();
    this.loginAttemptService = loginAttemptService;
  }

  @Override public void onAuthenticationSuccess( HttpServletRequest request, HttpServletResponse response,
                                                 Authentication authentication ) throws ServletException, IOException {
    this.loginAttemptService.loginSucceeded( getClientIp( request ) );
    super.onAuthenticationSuccess( request, response, authentication );
  }

  private String getClientIp( HttpServletRequest request ) {
    String xfHeader = request.getHeader( "X-Forwarded-For" );
    if ( xfHeader == null ) {
      return request.getRemoteAddr();
    }
    return xfHeader.split( "," )[0];
  }
}
