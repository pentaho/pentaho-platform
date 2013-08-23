package org.pentaho.platform.web.http.security;

import org.springframework.security.Authentication;
import org.springframework.security.BadCredentialsException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This class's sole purpose is to defeat the persistence of Basic-Auth credentials in the browser. The mechanism used
 * to accomplish this is to detect an expired (invalid) HttpSession from the client.
 *
 * If the first request after a session becomes invalid is a Basic-Auth request, we automatically deny, forcing
 * re-authentication.
 *
 * The second path is if the first request after session invalidation is not a basic-auth (user manually logged out
 * and was presented with the login page), we drop a cookie in the response noting the event. The next request with
 * Basic-Auth and a valid HttpSession checks for this cookie and if present, forces reauthentication.
 *
 *
 * User: nbaker
 * Date: 8/15/13
 */
public class PentahoBasicProcessingFilter extends org.springframework.security.ui.basicauth.BasicProcessingFilter {

  @Override
  public void doFilterHttp(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
    if(request.getRequestedSessionId() != null
        && !request.isRequestedSessionIdValid()){
      // expired session detected.
      Cookie expiredCookie = null;
      for(Cookie c : request.getCookies()){
        if("JSESSIONID".equals(c.getName())){
          expiredCookie = c; // cache that this definitely is a browser with an expired session.
          break;
        }
      }
      String header = request.getHeader("Authorization");
      if(header != null && header.indexOf("Basic") == 0){
        // Session is expired and a Basic-Auth request is coming in. We'll drop a cookie to note this and force
        // re-authentication
        for(Cookie c : request.getCookies()){
          if("session-flushed".equals(c.getName())){
            c.setMaxAge(0);
            response.addCookie(c);
            break;
          }
        }
        // force the prompt for credentials
        getAuthenticationEntryPoint().commence(request, response, new BadCredentialsException("Clearing Basic-Auth"));
        return;
      } else if(expiredCookie != null){
        // Session is expired but this request does not include basic-auth, drop a cookie to keep track of this event.
        Cookie c = new Cookie("session-flushed", "true");
        c.setPath(request.getContextPath() != null ? request.getContextPath() : "/");
        c.setMaxAge(-1);
        response.addCookie(c);
      }
    } else {
      String header = request.getHeader("Authorization");
      if(header != null && header.indexOf("Basic") == 0){
        // Session is valid, but Basic-auth is supplied. Check to see if the session end cookie we created is present,
        // if so, force reauthentication.
        for(Cookie c : request.getCookies()){
          if("session-flushed".equals(c.getName())){
            c.setMaxAge(0);
            c.setPath(request.getContextPath() != null ? request.getContextPath() : "/");
            response.addCookie(c);

            getAuthenticationEntryPoint().commence(request, response, new BadCredentialsException("Clearing Basic-Auth"));
            return;
          }
        }
      }
    }

    super.doFilterHttp(request, response, chain);    //To change body of overridden methods use File | Settings | File Templates.
  }

  @Override
  protected void onSuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, Authentication authResult) throws IOException {
    super.onSuccessfulAuthentication(request, response, authResult);
    request.getSession().setAttribute("BasicAuth", "true");
  }
}
