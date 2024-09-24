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
 *
 * Copyright (c) 2023 Hitachi Vantara. All rights reserved.
 */

package org.pentaho.platform.web.servlet;

import com.sun.xml.ws.transport.http.servlet.ServletAdapterList;
import com.sun.xml.ws.transport.http.servlet.SpringBinding;
import com.sun.xml.ws.transport.http.servlet.WSServletDelegate;
import org.pentaho.platform.api.engine.IPlatformPlugin;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.pluginmgr.PentahoSystemPluginManager;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.web.context.support.XmlWebApplicationContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PentahoPluginWSSpringServlet extends HttpServlet {

  private Pattern uriPattern = Pattern.compile( ".*/webservices/plugins/([-\\w]+)/.+" );
  Map<String, WSServletDelegate> delegateMap = new HashMap<>();
  ServletAdapterList adapters = new ServletAdapterList();
  public void init( ServletConfig servletConfig ) throws ServletException {
    super.init( servletConfig );
  }

  @SuppressWarnings( "unchecked" )
  protected WSServletDelegate createPluginWSDelegate( String pluginPath, ClassLoader pluginClassLoader ) {
    ClassLoader origClassLoader = Thread.currentThread().getContextClassLoader();
    try {
      // this needs to be set because when the Spring service tries to create the webservice endpoint, something
      // buried in the webservice implementation tries to use the thread's classloader to instantiate the object
      Thread.currentThread().setContextClassLoader( pluginClassLoader );
      ApplicationContext appContext = getAppContext( pluginPath, pluginClassLoader );

      Set<SpringBinding> bindings = new LinkedHashSet<>();

      bindings.addAll( appContext.getBeansOfType( SpringBinding.class ).values() );

      adapters = new ServletAdapterList();
      for ( SpringBinding binding : bindings ) {
        binding.create( adapters );
      }

      return new WSServletDelegate( adapters, getServletContext() );
    } finally {
      Thread.currentThread().setContextClassLoader( origClassLoader );
    }
  }

  public ServletAdapterList getAdapters() {
    return adapters;
  }

  protected ApplicationContext getAppContext( String pluginPath, ClassLoader pluginClassLoader ) {
    XmlWebApplicationContext wac = new XmlWebApplicationContext() {
      @Override
      protected Resource getResourceByPath( String path ) {
        return new FileSystemResource( new File( path ) );
      }

      @Override
      protected DefaultListableBeanFactory createBeanFactory() {
        DefaultListableBeanFactory beanFactory = super.createBeanFactory();
        beanFactory.setBeanClassLoader( pluginClassLoader );
        return beanFactory;
      }
    };

    wac.setServletContext( getServletContext() );
    wac.setServletConfig( getServletConfig() );
    wac.setNamespace( getServletName() );
    wac.setClassLoader( pluginClassLoader );

    String springFile = pluginPath + File.separator + "plugin.ws.spring.xml";  //$NON-NLS-1$ //$NON-NLS-2$
    wac.setConfigLocations( springFile );
    wac.refresh();

    return wac;
  }

  protected void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException {
    WSServletDelegate delegate = delegateMap.get( request.getRequestURI() );
    if ( null == delegate ) {
      delegate = getWsServletDelegate( request );
      delegateMap.put( request.getRequestURI(), delegate );
    }
    delegate.doPost( request, response, getServletContext() );
}

  protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException {
    WSServletDelegate delegate = delegateMap.get( request.getRequestURI() );
    if ( null == delegate ) {
      delegate = getWsServletDelegate( request );
      delegateMap.put( request.getRequestURI(), delegate );
    }
    delegate.doGet( request, response, getServletContext() );
  }

  protected void doPut( HttpServletRequest request, HttpServletResponse response ) throws ServletException {
    WSServletDelegate delegate = delegateMap.get( request.getRequestURI() );
    if ( null == delegate ) {
      delegate = getWsServletDelegate( request );
      delegateMap.put( request.getRequestURI(), delegate );
    }
    delegate.doPut( request, response, getServletContext() );
  }

  protected void doDelete( HttpServletRequest request, HttpServletResponse response ) throws ServletException {
    WSServletDelegate delegate = delegateMap.get( request.getRequestURI() );
    if ( null == delegate ) {
      delegate = getWsServletDelegate( request );
      delegateMap.put( request.getRequestURI(), delegate );
    }
    delegate.doDelete( request, response, getServletContext() );
  }

  private WSServletDelegate getWsServletDelegate( HttpServletRequest request ) {
    IPlatformPlugin plugin = PentahoSystem.get( IPlatformPlugin.class, null,
      Collections.singletonMap( PentahoSystemPluginManager.PLUGIN_ID, getPluginNameFromUri( request.getRequestURI() ) ) );
    GenericApplicationContext beanFactory = PentahoSystem
      .get( GenericApplicationContext.class, null,
        Collections.singletonMap( PentahoSystemPluginManager.PLUGIN_ID, plugin.getId() ) );
    return createPluginWSDelegate(
      PentahoSystem.getApplicationContext().getSolutionPath( "system/" + plugin.getSourceDescription() )
      , beanFactory.getClassLoader() );
  }

  private String getPluginNameFromUri( String uri ) {
    Matcher m = uriPattern.matcher( uri );
    if ( m.matches() ) {
      return m.group( 1 );
    } else {
      return "";
    }
  }
}
