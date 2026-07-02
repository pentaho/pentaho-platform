package org.pentaho.platform.web.http.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * An entry point that always returns an HTTP 401 Unauthorized status code.
 * <p>
 * This entry point differs from {@link PentahoBasicAuthenticationEntryPoint}, which also returns a 401 status code, by
 * not sending a `WWW-Authenticate` header. This non-standard (albeit de facto) behavior is used to prevent some web
 * browsers (e.g. Chrome) from prompting the user for credentials, even if the request is made by a script in a web
 * browser. This way it is possible to mitigate the risks of using Basic Authentication in a web browser context, which
 * can cause credentials leakage during a client session that crosses server sessions.
 * <p>
 * Basic Authentication handling is reserved for non-browser clients, such as tools or scripts, which can explicitly
 * control the lifetime of the credentials stored on the client side.
 */
public class Http401UnauthorizedEntryPoint implements AuthenticationEntryPoint {

  private static final Log logger = LogFactory.getLog( Http401UnauthorizedEntryPoint.class );

  /**
   * Always returns a 401 error code to the client.
   */
  @Override
  public void commence( HttpServletRequest request, HttpServletResponse response, AuthenticationException arg2 )
    throws IOException {
    logger.debug( "HTTP-401 entry point called. Rejecting access" );
    response.sendError( HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.getReasonPhrase());
  }
}
