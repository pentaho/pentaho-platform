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

import com.sun.xml.ws.transport.http.servlet.ServletAdapterList;
import com.sun.xml.ws.transport.http.servlet.SpringBinding;
import com.sun.xml.ws.transport.http.servlet.WSServletDelegate;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.context.support.XmlWebApplicationContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;

@SuppressWarnings( "serial" )
/**
 * Services soap requests to JAX-WS Spring beans found in system/pentahoServices.spring.xml
 * 
 * This class is a workaround since we cannot figure out how to lazily init the ws- and wss- namespaced beans. 
 * Possibly related bug: https://issues.apache.org/jira/browse/XBEAN-97 (The ws and wss use XBean.)
 * 
 * If the beans could be lazily initialized, then we could replace this servlet with 
 * <a href="https://jax-ws-commons.dev.java.net/spring/">com.sun.xml.ws.transport.http.servlet.WSSpringServlet</a>.
 */
public class PentahoWSSpringServlet extends HttpServlet {
  private WSServletDelegate delegate;

  public void init( ServletConfig servletConfig ) throws ServletException {
    super.init( servletConfig );

    delegate = createPluginWSDelegate( servletConfig );
  }

  @SuppressWarnings( "unchecked" )
  protected WSServletDelegate createPluginWSDelegate( ServletConfig servletConfig ) {
    ApplicationContext appContext = getAppContext();

    Set<SpringBinding> bindings = new LinkedHashSet<SpringBinding>();

    bindings.addAll( appContext.getBeansOfType( SpringBinding.class ).values() );

    ServletAdapterList adapters = new ServletAdapterList();
    for ( SpringBinding binding : bindings ) {
      binding.create( adapters );
    }

    return new WSServletDelegate( adapters, getServletContext() );
  }

  protected ApplicationContext getAppContext() {
    ConfigurableWebApplicationContext wac = new XmlWebApplicationContext() {
      @Override
      protected Resource getResourceByPath( String path ) {
        return new FileSystemResource( new File( path ) );
      }
    };

    wac.setServletContext( getServletContext() );
    wac.setServletConfig( getServletConfig() );
    wac.setNamespace( getServletName() );
    String springFile =
        PentahoSystem.getApplicationContext()
            .getSolutionPath( "system" + File.separator + "pentahoServices.spring.xml" ); //$NON-NLS-1$ //$NON-NLS-2$
    wac.setConfigLocations( new String[] { springFile } );
    wac.refresh();

    return wac;
  }

  protected void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException {
    delegate.doPost( request, response, getServletContext() );
  }

  protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException {
    delegate.doGet( request, response, getServletContext() );
  }

  protected void doPut( HttpServletRequest request, HttpServletResponse response ) throws ServletException {
    delegate.doPut( request, response, getServletContext() );
  }

  protected void doDelete( HttpServletRequest request, HttpServletResponse response ) throws ServletException {
    delegate.doDelete( request, response, getServletContext() );
  }
}
