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

package org.pentaho.platform.web.servlet;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.IPluginManagerListener;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
      response.setStatus( 404 );
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
   * @return the part of the request url used to dispatch the request
   */
  public String getDispatchKey( HttpServletRequest request ) {
    // path info will give us what we want with
    String requestPathInfo = request.getPathInfo();
    if ( requestPathInfo.startsWith( "/" ) ) { //$NON-NLS-1$
      requestPathInfo = requestPathInfo.substring( 1 );
    }
    if ( requestPathInfo.endsWith( "/" ) ) { //$NON-NLS-1$
      requestPathInfo = requestPathInfo.substring( requestPathInfo.length() );
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

      Map<String, Object> beans =
        BeanFactoryUtils.beansOfTypeIncludingAncestors( pluginBeanFactoryEntry.getValue(),
          Servlet.class, true, true );

      if ( logger.isDebugEnabled() ) {
        logger.debug(
          "found " + beans.size() + " servlets in " + pluginBeanFactoryEntry.getKey() ); //$NON-NLS-1$//$NON-NLS-2$
      }

      for ( Map.Entry<String, Object> beanEntry : beans.entrySet() ) {
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
}
