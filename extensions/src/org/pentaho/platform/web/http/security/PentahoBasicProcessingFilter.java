package org.pentaho.platform.web.http.security;

import org.springframework.security.Authentication;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This class's sole purpose is to set an HTTPSession flag indicating that the user was authenticated by way of
 * Basic-Auth.
 *
 * User: nbaker
 * Date: 8/15/13
 */
public class PentahoBasicProcessingFilter extends org.springframework.security.ui.basicauth.BasicProcessingFilter {
  @Override
  protected void onSuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, Authentication authResult) throws IOException {
    super.onSuccessfulAuthentication(request, response, authResult);
    request.getSession().setAttribute("BasicAuth", "true");
  }
}
