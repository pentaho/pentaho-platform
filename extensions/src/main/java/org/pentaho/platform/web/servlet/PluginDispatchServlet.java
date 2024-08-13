/*!
 *
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
 *
 * Copyright (c) 2002-2024 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.web.servlet;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.IPluginManagerListener;
import org.pentaho.platform.api.websocket.IWebsocketEndpointConfig;
import org.pentaho.platform.engine.core.audit.MDCUtil;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.websocket.DeploymentException;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerContainer;
import javax.websocket.server.ServerEndpointConfig;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Dispatches requests to Servlets provided by BIServer plugins. To define a Servlet in a plugin, simply add a bean
 * element for the Servlet class in the plugin.spring.xml file in your plugin root directory.
 *
 * @author Aaron Phillips
 */
public class PluginDispatchServlet implements Servlet {

  private static final Log logger = LogFactory.getLog( PluginDispatchServlet.class );

  private ServletConfig servletConfig;

  private boolean initialized = false;

  private IPluginManager pluginManager = PentahoSystem.get( IPluginManager.class );

  private Map<String, Servlet> pluginServletMap = new HashMap<String, Servlet>();

  private static final String WEBSOCKET_PLUGIN_PATH_PREFIX = "websocket";

  public PluginDispatchServlet() {
    super();
  }

  public void destroy() {
    for ( Map.Entry<String, Servlet> pluginServlet : pluginServletMap.entrySet() ) {
      pluginServlet.getValue().destroy();
    }
  }

  public void service( final ServletRequest req, final ServletResponse res ) throws ServletException, IOException {
    if ( !initialized ) {
      doInit();
    }
    if ( !( req instanceof HttpServletRequest ) ) {
      throw new IllegalArgumentException( PluginDispatchServlet.class.getSimpleName()
        + " cannot handle non HTTP requests" ); //$NON-NLS-1$
    }

    HttpServletRequest request = (HttpServletRequest) req;
    HttpServletResponse response = (HttpServletResponse) res;

    Servlet pluginServlet = getTargetServlet( request, response );

    if ( pluginServlet == null ) {
      response.sendError( 404 );
      // FIXME: log more detail here for debugging
      return;
    }

    pluginServlet.service( req, res );
  }

  protected Servlet getTargetServlet( HttpServletRequest request, HttpServletResponse response )
    throws ServletException {
    String dispatchKey = getDispatchKey( request );

    if ( StringUtils.isEmpty( dispatchKey ) ) {
      if ( logger.isDebugEnabled() ) {
        logger
          .debug( "dispatcher servlet is invoked but there is nothing telling it where to dispatch to" ); //$NON-NLS-1$
      }
      return null;
    }

    Servlet targetServlet = null;
    String checkPath = dispatchKey;
    do {
      logger.debug(
        "checking for servlet registered to service request for \"" + checkPath + "\"" ); //$NON-NLS-1$//$NON-NLS-2$
      targetServlet = pluginServletMap.get( checkPath );
      if ( targetServlet != null ) {
        logger.debug( "servlet " + targetServlet.getClass().getName() + " will service request for \"" + dispatchKey
          //$NON-NLS-1$//$NON-NLS-2$
          + "\"" ); //$NON-NLS-1$
        return targetServlet;
      }
      if ( checkPath.contains( "/" ) ) { //$NON-NLS-1$
        checkPath = checkPath.substring( 0, checkPath.lastIndexOf( "/" ) ); //$NON-NLS-1$
      } else {
        checkPath = null;
      }
    } while ( checkPath != null );

    if ( targetServlet == null ) {
      logger
        .debug( "no servlet registered to service request for \"" + dispatchKey + "\"" ); //$NON-NLS-1$ //$NON-NLS-2$
    }
    return targetServlet;
  }

  /**
   * Returns the dispatch key for this request. This name is the part of the request path beyond the servlet base path.
   * I.e. if the PluginDispatchServlet is mapped to the "/plugin" context in web.xml, then this method will return
   * "myPlugin/myServlet" given a request to "http://localhost:8080/pentaho/plugin/myPlugin/myServlet".
   *
   * @param request the HTTP servlet request containing the extra URL path information.
   *
   * @return the part of the request url used to dispatch the request without leading or trailing forward slashes, or
   *         <code>null</code> if the URL does not have any extra path information.
   */
  public String getDispatchKey( HttpServletRequest request ) {
    // path info will give us what we want with
    String requestPathInfo = request.getPathInfo();

    if ( StringUtils.isEmpty( requestPathInfo ) ) {
      return null;
    }

    if ( requestPathInfo.startsWith( "/" ) ) {
      requestPathInfo = requestPathInfo.substring( 1 );
    }

    if ( requestPathInfo.endsWith( "/" ) ) {
      requestPathInfo = requestPathInfo.substring( 0, requestPathInfo.length() - 1 );
    }

    return requestPathInfo;
  }

  public void init( final ServletConfig config ) throws ServletException {
    this.servletConfig = config;
    pluginManager.addPluginManagerListener( new IPluginManagerListener() {
      @Override public void onReload() {
        try {
          initialized = false;
          doInit();
        } catch ( ServletException e ) {
          logger.error( e );
        }
      }
    } );
    doInit();
  }

  @SuppressWarnings( "unchecked" )
  /** Restore the caching once the Plugin Type Tracking system is in place, for now we'll look-up every time **/
  private synchronized void doInit() throws ServletException {
    if ( logger.isDebugEnabled() ) {
      logger.debug( "PluginDispatchServlet.init" ); //$NON-NLS-1$
    }

    if ( initialized ) {
      return;
    }
    pluginServletMap.clear();

    Map<String, ListableBeanFactory> pluginBeanFactoryMap = getPluginBeanFactories();

    for ( Map.Entry<String, ListableBeanFactory> pluginBeanFactoryEntry : pluginBeanFactoryMap.entrySet() ) {

      Map<String, Servlet> beans =
        BeanFactoryUtils.beansOfTypeIncludingAncestors( pluginBeanFactoryEntry.getValue(),
            Servlet.class, true, true );

      if ( logger.isDebugEnabled() ) {
        logger.debug(
          "found " + beans.size() + " servlets in " + pluginBeanFactoryEntry.getKey() ); //$NON-NLS-1$//$NON-NLS-2$
      }

      for ( Map.Entry<String, Servlet> beanEntry : beans.entrySet() ) {
        Servlet pluginServlet = (Servlet) beanEntry.getValue();
        String servletId = beanEntry.getKey();

        String pluginId = pluginBeanFactoryEntry.getKey();
        String context = pluginId + "/" + servletId; //$NON-NLS-1$

        pluginServletMap.put( context, pluginServlet );
        if ( logger.isDebugEnabled() ) {
          logger
            .debug( "calling init on servlet " + pluginServlet.getClass().getName() + " serving context "
              + context ); //$NON-NLS-1$//$NON-NLS-2$
        }
        try {
          pluginServlet.init( servletConfig );
        } catch ( Throwable t ) {
          logger.error( "Could not load servlet '" + context + "'", t ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
      }

      //configure websockets for the plugin, if any
      configurePluginWebsockets( pluginBeanFactoryEntry );
    }

    // Set initialized to true at the end of the synchronized method, so
    // that invocations of service() before this method has completed will not
    // cause NullPointerException
    initialized = true;
  }

  public ServletConfig getServletConfig() {
    return servletConfig;
  }

  public String getServletInfo() {
    return "A servlet to dispatch requests to Servlets defined in BIServer plugins"; //$NON-NLS-1$
  }

  protected Map<String, ListableBeanFactory> getPluginBeanFactories() {
    Collection<String> pluginIds = pluginManager.getRegisteredPlugins();
    Map<String, ListableBeanFactory> pluginBeanFactoryMap = new HashMap<String, ListableBeanFactory>();
    for ( String pluginId : pluginIds ) {
      ListableBeanFactory beanFactory = pluginManager.getBeanFactory( pluginId );
      if ( beanFactory != null ) {
        pluginBeanFactoryMap.put( pluginId, beanFactory );
      }
    }
    return pluginBeanFactoryMap;
  }

  /**
   * Configures a platform plugin websocket endpoints.
   *
   * @param pluginBeanFactoryEntry the plugin bean factory entry
   */
  @VisibleForTesting
  protected void configurePluginWebsockets( Map.Entry<String, ListableBeanFactory> pluginBeanFactoryEntry ) {
    // Register websocket endpoints configured in the plugin by finding EndpointConfig beans from the plugin spring config
    Map<String, IWebsocketEndpointConfig> wsBeans = getWebsocketEndpointConfigBeans( pluginBeanFactoryEntry.getValue() );

    if ( logger.isDebugEnabled() ) {
      logger.debug( "Found " + wsBeans.size() + " websocket endpoints in " + pluginBeanFactoryEntry.getKey() );
    }

    ServletContext servletContext = getServletConfig().getServletContext();
    String servletContextPath = servletContext.getContextPath();
    String pluginId = pluginBeanFactoryEntry.getKey();

    for ( Map.Entry<String, IWebsocketEndpointConfig> beanEntry : wsBeans.entrySet() ) {
      //Get specific endpoint configuration
      IWebsocketEndpointConfig websocketEndpointConfig = beanEntry.getValue();
      Predicate<String> isOriginAllowedPredicate = websocketEndpointConfig.getIsOriginAllowedPredicate();
      Class<?> pluginEndpointClass = websocketEndpointConfig.getEndpointImpl();
      List<String> subProtocols = websocketEndpointConfig.getSubProtocolAccepted();
      String endpointConfigPath = websocketEndpointConfig.getUrlSufix();
      String context = "/" + pluginId + "/" + WEBSOCKET_PLUGIN_PATH_PREFIX + "/" + endpointConfigPath;

      //register the websocket endpoint
      try {
        ServerEndpointConfig serverConfig = ServerEndpointConfig.Builder.create( pluginEndpointClass, context )
            .configurator( getServerWebsocketEndpointConfigurator( servletContextPath, isOriginAllowedPredicate ) )
            .subprotocols( subProtocols )
            .build();

        serverConfig.getUserProperties().put( websocketEndpointConfig.getServletContextPathPropertyName(), servletContextPath );
        serverConfig.getUserProperties().put( websocketEndpointConfig.getMaxMessagePropertyName(), websocketEndpointConfig.getMaxMessageBytesLength() );

        //add the new endpoint to the server container
        ServerContainer serverContainer = (ServerContainer) servletContext.getAttribute( ServerContainer.class.getName() );
        if ( serverContainer != null ) {
          serverContainer.addEndpoint( serverConfig );
          if ( logger.isInfoEnabled() ) {
            logger.info( pluginId + " plugin Websocket available in path " + context );
          }
        } else {
          logger.warn( pluginId + " plugin Websocket could not be configured because ServerContainer is missing" );
        }
      } catch ( DeploymentException ex ) {
        logger.error( "Failed to register endpoint for " + beanEntry.getClass() + " on " + context + ": " + ex.getMessage(), ex );
      }
    }
  }

  /**
   * Get the Server Endpoint Config configurator used for registering websocket endpoints.
   * @param servletContextPath The servlet context path.
   * @param isOriginAllowedPredicate A predicate that validates if a origin is allowed on new connections. Can be null,
   *                                 and in that scenario it will be ignored.
   * @return The configurator instance.
   */
  @VisibleForTesting
  protected ServerEndpointConfig.Configurator getServerWebsocketEndpointConfigurator( String servletContextPath,
                                                                                      Predicate<String> isOriginAllowedPredicate ) {
    return new ServerEndpointConfig.Configurator() {

      @Override
      public void modifyHandshake( ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response ) {

        // These properties can be used by the actual endpoint to "run as" the current user.

        // Based on HttpSessionPentahoSessionIntegrationFilter#readPentahoSessionFromHttpSession.
        IPentahoSession pentahoSession = null;
        Authentication authentication = null;
        HttpSession httpSession = (HttpSession) request.getHttpSession();
        if ( httpSession != null ) {
          pentahoSession = (IPentahoSession) httpSession.getAttribute( PentahoSystem.PENTAHO_SESSION_KEY );
          if ( pentahoSession != null ) {
            authentication = SecurityContextHolder.getContext().getAuthentication();
          }
        }

        sec.getUserProperties().put( PentahoSystem.PENTAHO_SESSION_KEY, pentahoSession );
        sec.getUserProperties().put( PentahoSystem.PENTAHO_AUTH_KEY, authentication );
        sec.getUserProperties().put( PentahoSystem.PENTAHO_MDC_KEY, new MDCUtil() );
      }

      /**
       * Override the default checkOrigin to comply with CORS enforced in the plugins.
       * @param originHeaderValue the received origin from the request
       * @return true if the request should be honored.
       */
      @Override
      public boolean checkOrigin( String originHeaderValue ) {
        //we need to check regular request from the same origin as the server, and accept them
        String localServerOrigin = getServerUrl( servletContextPath );
        if ( localServerOrigin.equals( originHeaderValue ) ) {
          return true;
        }
        //check CORS enabled requests, using the provided predicate function
        if ( isOriginAllowedPredicate != null ) {
          return isOriginAllowedPredicate.test( originHeaderValue );
        }
        return false;
      }

    };
  }

  /**
   * Gets the beans that implement the {@link IWebsocketEndpointConfig} interface from the factory received as
   * parameter.
   * @param pluginBeanFactory The factory where the beans will be looked on.
   * @return A map with the beans found.
   */
  @VisibleForTesting
  protected Map<String, IWebsocketEndpointConfig> getWebsocketEndpointConfigBeans( ListableBeanFactory pluginBeanFactory ) {
    return BeanFactoryUtils.beansOfTypeIncludingAncestors( pluginBeanFactory,
        IWebsocketEndpointConfig.class, true, true );
  }

  /**
   * Gets the server URL up until the application context path.
   * @param contextPath the application context path, used to know where to split the server URL
   * @return The server URL like http://localhost:8080/
   */
  @VisibleForTesting
  protected String getServerUrl( String contextPath ) {
    String fullyQualifiedServerURL = PentahoSystem.getApplicationContext().getFullyQualifiedServerURL();
    fullyQualifiedServerURL = fullyQualifiedServerURL.replaceFirst( "/?" + contextPath + "/?", "" );
    return fullyQualifiedServerURL;
  }
}
