/*!
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
* Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
*/

package org.pentaho.platform.web.http.security;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.web.http.messages.Messages;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.Authentication;
import org.springframework.security.AuthenticationException;
import org.springframework.security.AuthenticationManager;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.security.ui.AuthenticationEntryPoint;
import org.springframework.security.ui.WebAuthenticationDetails;
import org.springframework.util.Assert;

/**
 * Processes Request Parameter authorization, putting the result
 * into the <code>SecurityContextHolder</code>.
 * 
 * <p>
 * In summary, this filter looks for request parameters with the userid/password
 * </p>
 * 
 * <P>
 * If authentication is successful, the resulting {@link Authentication} object
 * will be placed into the <code>SecurityContextHolder</code>.
 * </p>
 * 
 * <p>
 * If authentication fails and <code>ignoreFailure</code> is <code>false</code>
 * (the default), an {@link AuthenticationEntryPoint} implementation is
 * called. Usually this should be {@link RequestParameterFilterEntryPoint}.
 * </p>
 * 
 * <p>
 * <b>Do not use this class directly.</b> Instead configure
 * <code>web.xml</code> to use the {@link
 * org.springframework.security.util.FilterToBeanProxy}.
 * </p>
 *
 */
public class RequestParameterAuthenticationFilter implements Filter, InitializingBean {
  //~ Static fields/initializers =============================================

  private static final Log logger = LogFactory.getLog(RequestParameterAuthenticationFilter.class);

  //~ Instance fields ========================================================

  private AuthenticationEntryPoint authenticationEntryPoint;

  private AuthenticationManager authenticationManager;

  private boolean ignoreFailure = false;

  private static final String DefaultUserNameParameter = "userid"; //$NON-NLS-1$

  private static final String DefaultPasswordParameter = "password"; //$NON-NLS-1$

  private String userNameParameter = RequestParameterAuthenticationFilter.DefaultUserNameParameter;

  private String passwordParameter = RequestParameterAuthenticationFilter.DefaultPasswordParameter;

  //~ Methods ================================================================

  public void afterPropertiesSet() throws Exception {
    Assert.notNull(this.authenticationManager, Messages.getInstance()
        .getErrorString("RequestParameterAuthenticationFilter.ERROR_0001_AUTHMGR_REQUIRED")); //$NON-NLS-1$
    Assert.notNull(this.authenticationEntryPoint, Messages.getInstance()
        .getErrorString("RequestParameterAuthenticationFilter.ERROR_0002_AUTHM_ENTRYPT_REQUIRED")); //$NON-NLS-1$

    Assert.hasText(this.userNameParameter, Messages.getInstance()
        .getString("RequestParameterAuthenticationFilter.ERROR_0003_USER_NAME_PARAMETER_MISSING")); //$NON-NLS-1$
    Assert.hasText(this.passwordParameter, Messages.getInstance()
        .getString("RequestParameterAuthenticationFilter.ERROR_0004_PASSWORD_PARAMETER_MISSING")); //$NON-NLS-1$
  }

  public void destroy() {
  }

  public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException,
      ServletException {
    if (!(request instanceof HttpServletRequest)) {
      throw new ServletException(Messages.getInstance()
          .getErrorString("RequestParameterAuthenticationFilter.ERROR_0005_HTTP_SERVLET_REQUEST_REQUIRED")); //$NON-NLS-1$
    }

    if (!(response instanceof HttpServletResponse)) {
      throw new ServletException(Messages.getInstance()
          .getErrorString("RequestParameterAuthenticationFilter.ERROR_0006_HTTP_SERVLET_RESPONSE_REQUIRED")); //$NON-NLS-1$
    }

    HttpServletRequest httpRequest = (HttpServletRequest) request;

    InputStream in = httpRequest.getInputStream();
    byte[] bytes = IOUtils.toByteArray(in);
    // Do something with Bytes.

    final BufferedInputStream newStream = new BufferedInputStream(new ByteArrayInputStream(bytes));
    final Map parameterMap = request.getParameterMap();

    HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper(httpRequest){

      @Override
      public Map getParameterMap() {
        return parameterMap;
      }

      @Override
      public ServletInputStream getInputStream() throws IOException {
        return new ServletInputStream(){
          @Override
          public int read() throws IOException {
            return newStream.read();
          }
        };
      }
    };


    String username = httpRequest.getParameter(this.userNameParameter);
    String password = httpRequest.getParameter(this.passwordParameter);

    if (RequestParameterAuthenticationFilter.logger.isDebugEnabled()) {
      RequestParameterAuthenticationFilter.logger.debug(Messages.getInstance().getString("RequestParameterAuthenticationFilter.DEBUG_AUTH_USERID", username)); //$NON-NLS-1$
    }

    if ((username != null) && (password != null)) {
      // Only reauthenticate if username doesn't match SecurityContextHolder and user isn't authenticated (see SEC-53)
      Authentication existingAuth = SecurityContextHolder.getContext().getAuthentication();

      if ((existingAuth == null) || !existingAuth.getName().equals(username) || !existingAuth.isAuthenticated()) {
        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username, password);
        authRequest.setDetails(new WebAuthenticationDetails(httpRequest));

        Authentication authResult;

        try {
          authResult = authenticationManager.authenticate(authRequest);
        } catch (AuthenticationException failed) {
          // Authentication failed
          if (RequestParameterAuthenticationFilter.logger.isDebugEnabled()) {
            RequestParameterAuthenticationFilter.logger.debug(Messages.getInstance().getString(
                "RequestParameterAuthenticationFilter.DEBUG_AUTHENTICATION_REQUEST", username, failed.toString())); //$NON-NLS-1$
          }

          SecurityContextHolder.getContext().setAuthentication(null);

          if (ignoreFailure) {
            chain.doFilter(wrapper, response);
          } else {
            authenticationEntryPoint.commence(wrapper, response, failed);
          }

          return;
        }

        // Authentication success
        if (RequestParameterAuthenticationFilter.logger.isDebugEnabled()) {
          RequestParameterAuthenticationFilter.logger.debug(Messages.getInstance().getString(
              "RequestParameterAuthenticationFilter.DEBUG_AUTH_SUCCESS", authResult.toString())); //$NON-NLS-1$
        }

        SecurityContextHolder.getContext().setAuthentication(authResult);
      }
    }

    chain.doFilter(wrapper, response);
  }

  public AuthenticationEntryPoint getAuthenticationEntryPoint() {
    return authenticationEntryPoint;
  }

  public AuthenticationManager getAuthenticationManager() {
    return authenticationManager;
  }

  public void init(final FilterConfig arg0) throws ServletException {
  }

  public boolean isIgnoreFailure() {
    return ignoreFailure;
  }

  public void setAuthenticationEntryPoint(final AuthenticationEntryPoint authenticationEntryPoint) {
    this.authenticationEntryPoint = authenticationEntryPoint;
  }

  public void setAuthenticationManager(final AuthenticationManager authenticationManager) {
    this.authenticationManager = authenticationManager;
  }

  public void setIgnoreFailure(final boolean ignoreFailure) {
    this.ignoreFailure = ignoreFailure;
  }

  public String getUserNameParameter() {
    return userNameParameter;
  }

  public String getPasswordParameter() {
    return passwordParameter;
  }

  public void setUserNameParameter(final String value) {
    userNameParameter = value;
  }

  public void setPasswordParameter(final String value) {
    passwordParameter = value;
  }

}
