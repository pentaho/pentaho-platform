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

import com.sun.xml.ws.transport.http.servlet.ServletAdapterList;
import com.sun.xml.ws.transport.http.servlet.WSServletDelegate;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.web.jaxws.spring.SpringBinding;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
    String soapAction = request.getHeader("SOAPAction");
    // Handle logout requests by invoking Spring Security logout handler
    delegate.doPost( request, response, getServletContext() );
    if ( soapAction.endsWith( "logoutRequest\"" ) ) {
      IPentahoSession userSession = PentahoSessionHolder.getSession();
      PentahoSystem.invokeLogoutListeners( userSession );
      SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
      logoutHandler.logout( request, response, null );
    }
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
