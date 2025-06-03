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


package org.pentaho.platform.web.servlet;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.objfac.spring.SpringScopeSessionHolder;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.regex.Pattern;
import org.glassfish.jersey.servlet.ServletContainer;

/**
 * This should only be used by a plugin in the plugin.spring.xml file to initialize a Jersey. The presence of this
 * servlet in the spring file will make it possible to write JAX-RS POJOs in your plugin.
 *
 * @author Aaron Phillips
 */
public class JAXRSPluginServlet extends ServletContainer implements ApplicationContextAware {

  private static final long serialVersionUID = 457538570048660945L;
  private static final String APPLICATION_WADL = "application.wadl";

  static final int UNDER_KNOWN_ERROR_RANGE = 399;
  static final int OVER_KNOWN_ERROR_RANGE = 600;

  // Matches: application.wadl, application.wadl/xsd0.xsd
  // Does not match: application.wadl/.xsd, application.wadl/xsd0/xsd0.xsd, application.wadl/a.xml
  private static final Pattern WADL_PATTERN = Pattern.compile( "(.*)" + APPLICATION_WADL + "(/[A-Za-z0-9_]+(.xsd)+)*" );

  private ApplicationContext applicationContext;

  public static final ThreadLocal requestThread = new ThreadLocal();

  private static final Log logger = LogFactory.getLog( JAXRSPluginServlet.class );

  public void setApplicationContext( ApplicationContext applicationContext ) throws BeansException {
    this.applicationContext = applicationContext;
  }


  protected ConfigurableApplicationContext getContext() {
    return ( ConfigurableApplicationContext ) applicationContext;
  }

  @Override
  public void service( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException {
    logger.debug( "servicing request for resource " + request.getPathInfo() ); //$NON-NLS-1$

    // Jersey's Servlet only responds to 'application.wadl', Plugin requests always have 'plugin/PLUGIN_NAME/api' as a
    // predicate i.e. /plugin/data-access/api/application.wadl.
    //
    // If the request ends with application.wadl, dispatch a Proxied request that rewrites the url to plain
    // 'application.wadl'. This is using a JDK Dynamic Proxy which increases overhead, but these requests should be
    // seldom and don't need to be that performant.
    if ( WADL_PATTERN.matcher( request.getPathInfo() ).find() ) {
      final HttpServletRequest originalRequest = request;
      final String appWadlUrl = request.getPathInfo().substring(
        request.getPathInfo().indexOf( APPLICATION_WADL ), request.getPathInfo().length() );
      request =
        (HttpServletRequest) Proxy.newProxyInstance( getClass().getClassLoader(),
          new Class[]{HttpServletRequest.class}, new InvocationHandler() {
            public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable {
              if ( method.getName().equals( "getPathInfo" ) ) {
                return appWadlUrl;
              } else if ( method.getName().equals( "getRequestURL" ) ) {
                String url = originalRequest.getRequestURL().toString();
                return new StringBuffer(
                  url.substring( 0, url.indexOf( originalRequest.getPathInfo() ) ) + "/" + appWadlUrl );
              } else if ( method.getName().equals( "getRequestURI" ) ) {
                String uri = originalRequest.getRequestURI();
                return uri.substring( 0, uri.indexOf( originalRequest.getPathInfo() ) ) + "/" + appWadlUrl;
              }
              // We don't care about the Method, delegate out to real Request object.
              return method.invoke( originalRequest, args );
            }
          }
        );
      if ( originalRequest.getRequestURL() != null ) {
        requestThread.set( originalRequest.getRequestURL().toString() );
      } else if ( originalRequest.getRequestURI() != null ) {
        requestThread.set( originalRequest.getRequestURI().toString() );
      }
    }

    // Ensure that Resource beans (and others these consume) can use Spring scope="session".
    // See StandaloneSpringPentahoObjectFactory. There's likely a better way to solve this issue.
    // Perhaps in HttpSessionPentahoSessionIntegrationFilter, or even PentahoSessionHolder,
    // but this is the place which is solving the current need, and with a lesser impact.
    IPentahoSession previousSpringSession = SpringScopeSessionHolder.SESSION.get();
    SpringScopeSessionHolder.SESSION.set( PentahoSessionHolder.getSession() );
    try {
      callParentServiceMethod( request, response );
    } finally {
      SpringScopeSessionHolder.SESSION.set( previousSpringSession );
    }

    // JAX-RS Response return objects do not trigger the "error state" in the HttpServletResponse
    // Forcing all HTTP Error Status into "sendError" enables the configuration of custom error
    // pages in web.xml.
    if ( !response.isCommitted() && response.getStatus() > UNDER_KNOWN_ERROR_RANGE && response.getStatus() < OVER_KNOWN_ERROR_RANGE ) {
      response.sendError( response.getStatus() );
    }
  }

  // wrapped in its own method for easier stubbing
  @VisibleForTesting
  protected void callParentServiceMethod( HttpServletRequest request, HttpServletResponse response )
    throws ServletException, IOException {
    super.service( request, response );
  }

  @Override
  public void service( ServletRequest req, ServletResponse res ) throws ServletException, IOException {
    super.service( req, res );
  }
}
