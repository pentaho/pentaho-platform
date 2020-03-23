package org.pentaho.platform.web.http.security;

import org.pentaho.platform.api.security.ILoginAttemptService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class PreventBruteForceUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

  private ILoginAttemptService loginAttemptService;

  public PreventBruteForceUsernamePasswordAuthenticationFilter( ILoginAttemptService loginAttemptService ) {
    super();
    this.loginAttemptService = loginAttemptService;
  }

  @Override public Authentication attemptAuthentication( HttpServletRequest request, HttpServletResponse response )
    throws AuthenticationException {
    String clientIp = getClientIp( request );
    if ( this.loginAttemptService.isBlocked( clientIp ) ) {
      throw new PreventBruteForceException( "Authentication blocked to prevent brute force login" );
    }
    return super.attemptAuthentication( request, response );
  }

  private String getClientIp( HttpServletRequest request ) {
    String xfHeader = request.getHeader( "X-Forwarded-For" );
    if ( xfHeader == null ) {
      return request.getRemoteAddr();
    }
    return xfHeader.split( "," )[0];
  }
}
