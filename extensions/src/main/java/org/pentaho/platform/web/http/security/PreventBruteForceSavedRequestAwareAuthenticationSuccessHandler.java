package org.pentaho.platform.web.http.security;

import org.pentaho.platform.api.security.ILoginAttemptService;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
