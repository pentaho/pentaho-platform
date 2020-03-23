package org.pentaho.platform.web.http.security;

import org.pentaho.platform.api.security.ILoginAttemptService;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class PreventBruteForceSimpleUrlAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

  private ILoginAttemptService loginAttemptService;

  public PreventBruteForceSimpleUrlAuthenticationFailureHandler( ILoginAttemptService loginAttemptService ) {
    super();
    this.loginAttemptService = loginAttemptService;
  }

  public PreventBruteForceSimpleUrlAuthenticationFailureHandler( String defaultFailureUrl, ILoginAttemptService loginAttemptService ) {
    super( defaultFailureUrl );
    this.loginAttemptService = loginAttemptService;
  }

  @Override public void onAuthenticationFailure( HttpServletRequest request, HttpServletResponse response,
                                                 AuthenticationException exception )
    throws IOException, ServletException {
    super.onAuthenticationFailure( request, response, exception );
  }

  private String getClientIp( HttpServletRequest request ) {
    String xfHeader = request.getHeader( "X-Forwarded-For" );
    if ( xfHeader == null ) {
      return request.getRemoteAddr();
    }
    return xfHeader.split( "," )[0];
  }

}
